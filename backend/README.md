# Matrix Market API

ASP.NET Core 8 Web API backing the Matrix Market Android app. Uses SQLite (zero setup)
via EF Core, JWT bearer authentication, and exposes Swagger at `/swagger`.

## Run locally
```bash
cd MatrixMarket.Api
dotnet restore
dotnet run
```
The API starts on `https://localhost:5001` (or check the console output for the exact port).
The SQLite database file (`matrixmarket.db`) and schema are created automatically on first run.

## Deploying (Azure App Service - matches the design doc)
```bash
az webapp up --name matrix-market-api --runtime "DOTNETCORE:8.0"
```
Or build the included `Dockerfile` and deploy to any container host (Azure, Render, Railway).
Before deploying, change `Jwt:Key` in `appsettings.json` to a long random secret.

## Endpoints
| Method | Route | Auth | Description |
|---|---|---|---|
| POST | /api/v1/auth/register | - | Register with student email + password |
| POST | /api/v1/auth/login | - | Email/password login, returns JWT |
| POST | /api/v1/auth/sso-login | - | Create/login a user from a verified Google SSO email |
| GET  | /api/v1/users/{id}/profile | - | Profile, karma points, trust tier, badges |
| PUT  | /api/v1/users/{id}/settings | JWT | Update profile settings |
| GET  | /api/v1/categories | - | Academic categories |
| GET  | /api/v1/listings | - | Filterable marketplace listings |
| POST | /api/v1/listings | JWT | Create a listing |
| DELETE | /api/v1/listings/{id} | JWT | Remove own listing |
| POST | /api/v1/offers | JWT | Make an offer on a listing |
| POST | /api/v1/trades/schedule | JWT | Schedule a trade meeting (generates 6-digit code) |
| POST | /api/v1/trades/{id}/confirm | JWT | Confirm trade via code, awards Student Score karma |

## Connect the Android app
In `android/app/src/main/java/.../data/remote/RetrofitClient.kt`, set `BASE_URL` to your
deployed API URL (or `http://10.0.2.2:5000/` if running the API locally and testing on an emulator).
