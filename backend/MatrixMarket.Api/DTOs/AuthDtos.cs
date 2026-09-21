namespace MatrixMarket.Api.DTOs;

public class RegisterRequest
{
    public string StudentEmail { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string? Campus { get; set; }
}

public class LoginRequest
{
    public string StudentEmail { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}

// Used when a user authenticates via Google/Firebase SSO on the client,
// and the backend just needs to create/find the matching account.
public class SsoLoginRequest
{
    public string StudentEmail { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string Provider { get; set; } = "Google";
}

public class AuthResponse
{
    public string Token { get; set; } = string.Empty;
    public int UserId { get; set; }
    public string FullName { get; set; } = string.Empty;
    public string StudentEmail { get; set; } = string.Empty;
    public int KarmaPoints { get; set; }
}
