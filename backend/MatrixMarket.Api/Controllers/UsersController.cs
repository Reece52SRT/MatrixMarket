using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MatrixMarket.Api.Data;
using MatrixMarket.Api.DTOs;
using MatrixMarket.Api.Services;

namespace MatrixMarket.Api.Controllers;

[ApiController]
[Route("api/v1/users")]
[Authorize]
public class UsersController : ControllerBase
{
    private readonly AppDbContext _db;

    public UsersController(AppDbContext db)
    {
        _db = db;
    }

    // GET /api/v1/users/{userId}/profile
    // Powers the Profile screen: karma points, trust tier, and earned badges
    // (User Defined Feature 3 - Gamified Student Score).
    [HttpGet("{userId}/profile")]
    public async Task<ActionResult<UserProfileResponse>> GetProfile(int userId)
    {
        var user = await _db.Users
            .Include(u => u.Badges)
            .Include(u => u.Listings)
            .FirstOrDefaultAsync(u => u.UserId == userId);

        if (user == null) return NotFound();

        // Trades completed as a SELLER (matches the same count KarmaService uses
        // to decide badge eligibility, so the profile and the badge unlocks agree).
        var tradesCompleted = await _db.TradeMeetings
            .Include(t => t.Offer)
            .Where(t => t.IsConfirmed && t.Offer!.Listing!.SellerId == userId)
            .CountAsync();

        return Ok(new UserProfileResponse
        {
            UserId = user.UserId,
            FullName = user.FullName,
            StudentEmail = user.StudentEmail,
            Campus = user.Campus,
            KarmaPoints = user.KarmaPoints,
            TrustTier = KarmaService.GetTrustTier(user.KarmaPoints),
            Badges = user.Badges.Select(b => b.BadgeType).ToList(),
            ListingsCount = user.Listings.Count,
            MemberSince = user.CreatedAt,
            TradesCompleted = tradesCompleted
        });
    }

    // PUT /api/v1/users/{userId}/settings
    // Backs the Settings screen (profile fields; language/notification prefs are stored locally).
    [HttpPut("{userId}/settings")]
    public async Task<IActionResult> UpdateSettings(int userId, UpdateSettingsRequest request)
    {
        var authedUserId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier) ?? User.FindFirstValue("sub")!);
        if (authedUserId != userId) return Forbid();

        var user = await _db.Users.FindAsync(userId);
        if (user == null) return NotFound();

        if (!string.IsNullOrWhiteSpace(request.FullName)) user.FullName = request.FullName;
        if (request.Campus != null) user.Campus = request.Campus;

        await _db.SaveChangesAsync();
        return NoContent();
    }
}
