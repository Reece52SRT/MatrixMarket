using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MatrixMarket.Api.Data;

namespace MatrixMarket.Api.Controllers;

[ApiController]
[Route("api/v1/categories")]
public class CategoriesController : ControllerBase
{
    private readonly AppDbContext _db;
    public CategoriesController(AppDbContext db) => _db = db;

    // GET /api/v1/categories - used to populate the academic category filter/spinner.
    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.Categories.ToListAsync());
}
