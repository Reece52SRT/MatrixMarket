namespace MatrixMarket.Api.DTOs;

public class CreateOfferRequest
{
    public int ListingId { get; set; }
    public decimal OfferedPrice { get; set; }
}

public class ScheduleTradeRequest
{
    public int OfferId { get; set; }
    public string CampusSpot { get; set; } = string.Empty;
    public DateTime MeetingTime { get; set; }
}

public class TradeMeetingResponse
{
    public int MeetingId { get; set; }
    public int OfferId { get; set; }
    public string CampusSpot { get; set; } = string.Empty;
    public DateTime MeetingTime { get; set; }
    public string ConfirmationCode { get; set; } = string.Empty;
    public bool IsConfirmed { get; set; }
    public string ListingTitle { get; set; } = string.Empty;
}

public class ConfirmTradeRequest
{
    public string ConfirmationCode { get; set; } = string.Empty;
}

// Returned by GET /api/v1/trades/mine so BOTH sides of a trade - not just the
// buyer who scheduled it - can see the meeting spot, time, and confirmation code.
public class MyTradeMeetingResponse
{
    public int MeetingId { get; set; }
    public int OfferId { get; set; }
    public string CampusSpot { get; set; } = string.Empty;
    public DateTime MeetingTime { get; set; }
    public string ConfirmationCode { get; set; } = string.Empty;
    public bool IsConfirmed { get; set; }
    public string ListingTitle { get; set; } = string.Empty;
    public decimal Price { get; set; }
    public string Role { get; set; } = string.Empty; // "Buyer" or "Seller"
    public string OtherPartyName { get; set; } = string.Empty;
}
