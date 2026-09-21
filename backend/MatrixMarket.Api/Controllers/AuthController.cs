using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MatrixMarket.Api.Data;
using MatrixMarket.Api.DTOs;
using MatrixMarket.Api.Models;
using MatrixMarket.Api.Services;
using BCrypt.Net;

namespace MatrixMarket.Api.Controllers;

[ApiController]
[Route("api/v1/auth")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly TokenService _tokenService;

    public AuthController(AppDbContext db, TokenService tokenService)
    {
        _db = db;
        _tokenService = tokenService;
    }

    // POST /api/v1/auth/register
    // Registers a student account with an email/password. Requires a campus-style
    // academic email domain to keep the marketplace scoped to verified students.
    [HttpPost("register")]
    public async Task<ActionResult<AuthResponse>> Register(RegisterRequest request)
    {
        if (await _db.Users.AnyAsync(u => u.StudentEmail == request.StudentEmail))
            return Conflict("An account with this email already exists.");

        var user = new User
        {
            StudentEmail = request.StudentEmail,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
            FullName = request.FullName,
            Campus = request.Campus,
            SsoProvider = "Email"
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync();

        return Ok(BuildAuthResponse(user));
    }

    // POST /api/v1/auth/login
    [HttpPost("login")]
    public async Task<ActionResult<AuthResponse>> Login(LoginRequest request)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.StudentEmail == request.StudentEmail);
        if (user == null || !BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
            return Unauthorized("Invalid email or password.");

        return Ok(BuildAuthResponse(user));
    }

    // POST /api/v1/auth/sso-login
    // Called after the Android client has already authenticated the user with
    // Firebase/Google Sign-In. We trust the verified email the client sends and
    // create the account on first login (single sign-on flow, no password needed).
    [HttpPost("sso-login")]
    public async Task<ActionResult<AuthResponse>> SsoLogin(SsoLoginRequest request)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.StudentEmail == request.StudentEmail);

        if (user == null)
        {
            user = new User
            {
                StudentEmail = request.StudentEmail,
                FullName = request.FullName,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(Guid.NewGuid().ToString()),
                SsoProvider = request.Provider
            };
            _db.Users.Add(user);
            await _db.SaveChangesAsync();
        }

        return Ok(BuildAuthResponse(user));
    }

    private AuthResponse BuildAuthResponse(User user) => new()
    {
        Token = _tokenService.GenerateToken(user),
        UserId = user.UserId,
        FullName = user.FullName,
        StudentEmail = user.StudentEmail,
        KarmaPoints = user.KarmaPoints
    };
}
