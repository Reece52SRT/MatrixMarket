using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MatrixMarket.Api.Data;
using MatrixMarket.Api.DTOs;
using MatrixMarket.Api.Models;

namespace MatrixMarket.Api.Controllers;

[ApiController]
[Route("api/v1/listings")]
public class ListingsController : ControllerBase
{
    private readonly AppDbContext _db;
    public ListingsController(AppDbContext db) => _db = db;

    // GET /api/v1/listings?category={id}&search={term}&type={sell|lend|trade}
    // Fetches active marketplace listings with optional filtering - this is the
    // core REST API integration point used by the Home/Current Listings screen.
    [HttpGet]
    public async Task<ActionResult<List<ListingResponse>>> GetListings(
        [FromQuery] int? category, [FromQuery] string? search, [FromQuery] string? type)
    {
        var query = _db.Listings
            .Include(l => l.Category)
            .Include(l => l.Seller)
            .Where(l => l.Status == "Active")
            .AsQueryable();

        if (category.HasValue) query = query.Where(l => l.CategoryId == category.Value);
        if (!string.IsNullOrWhiteSpace(type)) query = query.Where(l => l.ListingType.ToLower() == type.ToLower());
        if (!string.IsNullOrWhiteSpace(search))
            query = query.Where(l => l.Title.Contains(search) || l.Description.Contains(search));

        var results = await query.OrderByDescending(l => l.CreatedAt).ToListAsync();
        return Ok(results.Select(ToResponse));
    }

    // GET /api/v1/listings/{id}
    [HttpGet("{id}")]
    public async Task<ActionResult<ListingResponse>> GetById(int id)
    {
        var listing = await _db.Listings
            .Include(l => l.Category).Include(l => l.Seller)
            .FirstOrDefaultAsync(l => l.ListingId == id);

        return listing == null ? NotFound() : Ok(ToResponse(listing));
    }

    // POST /api/v1/listings (Requires Auth)
    // Also used by the ISBN Barcode Scanner flow (User Defined Feature 1) to publish
    // a textbook listing once the scanned ISBN has been matched to book metadata.
    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ListingResponse>> Create(CreateListingRequest request)
    {
        var sellerId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier) ?? User.FindFirstValue("sub")!);

        var listing = new Listing
        {
            SellerId = sellerId,
            CategoryId = request.CategoryId,
            Title = request.Title,
            Description = request.Description,
            Price = request.Price,
            ListingType = request.ListingType,
            Isbn = request.Isbn,
            ImageUrl = request.ImageUrl
        };

        _db.Listings.Add(listing);
        await _db.SaveChangesAsync();

        await _db.Entry(listing).Reference(l => l.Category).LoadAsync();
        await _db.Entry(listing).Reference(l => l.Seller).LoadAsync();

        return CreatedAtAction(nameof(GetById), new { id = listing.ListingId }, ToResponse(listing));
    }

    // DELETE /api/v1/listings/{id} (Requires Auth, seller only)
    [HttpDelete("{id}")]
    [Authorize]
    public async Task<IActionResult> Delete(int id)
    {
        var sellerId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier) ?? User.FindFirstValue("sub")!);
        var listing = await _db.Listings.FindAsync(id);
        if (listing == null) return NotFound();
        if (listing.SellerId != sellerId) return Forbid();

        _db.Listings.Remove(listing);
        await _db.SaveChangesAsync();
        return NoContent();
    }

    private static ListingResponse ToResponse(Listing l) => new()
    {
        ListingId = l.ListingId,
        Title = l.Title,
        Description = l.Description,
        Price = l.Price,
        ListingType = l.ListingType,
        Isbn = l.Isbn,
        ImageUrl = l.ImageUrl,
        Status = l.Status,
        CategoryName = l.Category?.CategoryName ?? "",
        SellerName = l.Seller?.FullName ?? "",
        SellerId = l.SellerId,
        SellerKarma = l.Seller?.KarmaPoints ?? 0,
        CreatedAt = l.CreatedAt
    };
}
