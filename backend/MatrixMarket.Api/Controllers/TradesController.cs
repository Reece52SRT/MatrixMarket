using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MatrixMarket.Api.Data;
using MatrixMarket.Api.DTOs;
using MatrixMarket.Api.Models;
using MatrixMarket.Api.Services;

namespace MatrixMarket.Api.Controllers;

// Backs the Trade Meeting Scheduler (User Defined Feature 2): students agree on a
// verified campus meeting spot/time and a 6-digit code that is only marked
// confirmed once both parties are physically present for the exchange.
[ApiController]
[Route("api/v1/trades")]
[Authorize]
public class TradesController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly KarmaService _karmaService;

    public TradesController(AppDbContext db, KarmaService karmaService)
    {
        _db = db;
        _karmaService = karmaService;
    }

    private int CurrentUserId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier) ?? User.FindFirstValue("sub")!);

    // GET /api/v1/trades/mine - every trade meeting the logged-in user is part of,
    // whether they were the buyer (who scheduled it) or the seller (whose listing
    // it's for). This is what lets the SELLER see the meeting spot/time/code too,
    // not just the buyer.
    [HttpGet("mine")]
    public async Task<ActionResult<List<MyTradeMeetingResponse>>> GetMine()
    {
        var userId = CurrentUserId;

        var meetings = await _db.TradeMeetings
            .Include(t => t.Offer).ThenInclude(o => o!.Listing).ThenInclude(l => l!.Seller)
            .Include(t => t.Offer).ThenInclude(o => o!.Buyer)
            .Where(t => t.Offer != null &&
                        (t.Offer.BuyerId == userId || t.Offer.Listing!.SellerId == userId))
            .OrderBy(t => t.MeetingTime)
            .ToListAsync();

        var result = meetings.Select(m =>
        {
            var isBuyer = m.Offer!.BuyerId == userId;
            return new MyTradeMeetingResponse
            {
                MeetingId = m.MeetingId,
                OfferId = m.OfferId,
                CampusSpot = m.CampusSpot,
                MeetingTime = m.MeetingTime,
                ConfirmationCode = m.ConfirmationCode,
                IsConfirmed = m.IsConfirmed,
                ListingTitle = m.Offer.Listing?.Title ?? "",
                Price = m.Offer.OfferedPrice,
                Role = isBuyer ? "Buyer" : "Seller",
                OtherPartyName = isBuyer
                    ? (m.Offer.Listing?.Seller?.FullName ?? "Seller")
                    : (m.Offer.Buyer?.FullName ?? "Buyer")
            };
        }).ToList();

        return Ok(result);
    }

    // POST /api/v1/trades/schedule
    [HttpPost("schedule")]
    public async Task<ActionResult<TradeMeetingResponse>> Schedule(ScheduleTradeRequest request)
    {
        var offer = await _db.Offers.Include(o => o.Listing).FirstOrDefaultAsync(o => o.OfferId == request.OfferId);
        if (offer == null) return NotFound("Offer not found.");

        var code = new Random().Next(100000, 999999).ToString();

        var meeting = new TradeMeeting
        {
            OfferId = request.OfferId,
            CampusSpot = request.CampusSpot,
            MeetingTime = request.MeetingTime,
            ConfirmationCode = code
        };

        _db.TradeMeetings.Add(meeting);
        offer.Status = "Accepted";
        await _db.SaveChangesAsync();

        return Ok(ToResponse(meeting, offer.Listing?.Title ?? ""));
    }

    // GET /api/v1/trades/{meetingId}
    [HttpGet("{meetingId}")]
    public async Task<ActionResult<TradeMeetingResponse>> GetMeeting(int meetingId)
    {
        var meeting = await _db.TradeMeetings
            .Include(t => t.Offer).ThenInclude(o => o!.Listing)
            .FirstOrDefaultAsync(t => t.MeetingId == meetingId);

        return meeting == null ? NotFound() : Ok(ToResponse(meeting, meeting.Offer?.Listing?.Title ?? ""));
    }

    // POST /api/v1/trades/{meetingId}/confirm
    // Validates the confirmation code upon physical exchange, marks the listing sold,
    // and awards Student Score karma points + evaluates trust badges for the seller.
    [HttpPost("{meetingId}/confirm")]
    public async Task<IActionResult> Confirm(int meetingId, ConfirmTradeRequest request)
    {
        var meeting = await _db.TradeMeetings
            .Include(t => t.Offer).ThenInclude(o => o!.Listing)
            .FirstOrDefaultAsync(t => t.MeetingId == meetingId);

        if (meeting == null) return NotFound();
        if (meeting.ConfirmationCode != request.ConfirmationCode)
            return BadRequest("Incorrect confirmation code.");

        meeting.IsConfirmed = true;
        if (meeting.Offer?.Listing != null)
        {
            meeting.Offer.Listing.Status = "Sold";
            meeting.Offer.Status = "Completed";
            await _db.SaveChangesAsync();

            await _karmaService.AwardTradeCompletionAsync(meeting.Offer.Listing.SellerId);
        }
        else
        {
            await _db.SaveChangesAsync();
        }

        return Ok(new { meeting.IsConfirmed, Message = "Trade confirmed. Karma points awarded." });
    }

    private static TradeMeetingResponse ToResponse(TradeMeeting m, string title) => new()
    {
        MeetingId = m.MeetingId,
        OfferId = m.OfferId,
        CampusSpot = m.CampusSpot,
        MeetingTime = m.MeetingTime,
        ConfirmationCode = m.ConfirmationCode,
        IsConfirmed = m.IsConfirmed,
        ListingTitle = title
    };
}
