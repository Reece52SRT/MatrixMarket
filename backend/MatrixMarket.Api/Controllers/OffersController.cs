using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MatrixMarket.Api.Data;
using MatrixMarket.Api.DTOs;
using MatrixMarket.Api.Models;

namespace MatrixMarket.Api.Controllers;

[ApiController]
[Route("api/v1/offers")]
[Authorize]
public class OffersController : ControllerBase
{
    private readonly AppDbContext _db;
    public OffersController(AppDbContext db) => _db = db;

    // POST /api/v1/offers - submits a purchase/lend/trade offer for a listing.
    [HttpPost]
    public async Task<IActionResult> Create(CreateOfferRequest request)
    {
        var buyerId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier) ?? User.FindFirstValue("sub")!);

        var listing = await _db.Listings.FindAsync(request.ListingId);
        if (listing == null) return NotFound("Listing not found.");

        var offer = new Offer
        {
            ListingId = request.ListingId,
            BuyerId = buyerId,
            OfferedPrice = request.OfferedPrice
        };

        _db.Offers.Add(offer);
        await _db.SaveChangesAsync();

        return Ok(new { offer.OfferId, offer.Status });
    }
}
