using MatrixMarket.Api.Data;
using MatrixMarket.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace MatrixMarket.Api.Services;

// Encapsulates the Student Score / Trust Badge gamification logic (User Defined Feature 3).
public class KarmaService
{
    private readonly AppDbContext _db;

    public KarmaService(AppDbContext db)
    {
        _db = db;
    }

    public static string GetTrustTier(int karmaPoints) => karmaPoints switch
    {
        >= 200 => "Campus Legend",
        >= 100 => "Trusted Trader",
        >= 40 => "Rising Trader",
        _ => "New Trader"
    };

    // Called whenever a trade meeting is successfully confirmed.
    public async Task AwardTradeCompletionAsync(int sellerId)
    {
        var seller = await _db.Users.FindAsync(sellerId);
        if (seller == null) return;

        seller.KarmaPoints += 15;

        await EvaluateBadgesAsync(seller);
        await _db.SaveChangesAsync();
    }

    private async Task EvaluateBadgesAsync(User user)
    {
        var existingBadges = await _db.TrustBadges
            .Where(b => b.UserId == user.UserId)
            .Select(b => b.BadgeType)
            .ToListAsync();

        var completedTrades = await _db.TradeMeetings
            .Include(t => t.Offer)
            .Where(t => t.IsConfirmed && t.Offer!.Listing!.SellerId == user.UserId)
            .CountAsync();

        if (completedTrades >= 1 && !existingBadges.Contains("First Trade"))
        {
            _db.TrustBadges.Add(new TrustBadge { UserId = user.UserId, BadgeType = "First Trade" });
        }
        if (completedTrades >= 5 && !existingBadges.Contains("Top Seller"))
        {
            _db.TrustBadges.Add(new TrustBadge { UserId = user.UserId, BadgeType = "Top Seller" });
        }
        if (user.KarmaPoints >= 100 && !existingBadges.Contains("Trusted Trader"))
        {
            _db.TrustBadges.Add(new TrustBadge { UserId = user.UserId, BadgeType = "Trusted Trader" });
        }
    }
}
