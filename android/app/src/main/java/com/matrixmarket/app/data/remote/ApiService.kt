package com.matrixmarket.app.data.remote

import com.matrixmarket.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

// Defines the full contract against the Matrix Market REST API (see backend/README.md
// for the ASP.NET Core implementation of every one of these endpoints).
interface ApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/sso-login")
    suspend fun ssoLogin(@Body request: SsoLoginRequest): Response<AuthResponse>

    @GET("api/v1/users/{userId}/profile")
    suspend fun getProfile(@Path("userId") userId: Int): Response<UserProfileResponse>

    @PUT("api/v1/users/{userId}/settings")
    suspend fun updateSettings(
        @Path("userId") userId: Int,
        @Body request: UpdateSettingsRequest
    ): Response<Unit>

    @GET("api/v1/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("api/v1/listings")
    suspend fun getListings(
        @Query("category") categoryId: Int? = null,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<List<ListingResponse>>

    @GET("api/v1/listings/{id}")
    suspend fun getListingById(@Path("id") id: Int): Response<ListingResponse>

    @POST("api/v1/listings")
    suspend fun createListing(@Body request: CreateListingRequest): Response<ListingResponse>

    @DELETE("api/v1/listings/{id}")
    suspend fun deleteListing(@Path("id") id: Int): Response<Unit>

    @POST("api/v1/offers")
    suspend fun createOffer(@Body request: CreateOfferRequest): Response<OfferResponse>

    @POST("api/v1/trades/schedule")
    suspend fun scheduleTrade(@Body request: ScheduleTradeRequest): Response<TradeMeetingResponse>

    @GET("api/v1/trades/mine")
    suspend fun getMyTradeMeetings(): Response<List<MyTradeMeetingResponse>>

    @GET("api/v1/trades/{meetingId}")
    suspend fun getTradeMeeting(@Path("meetingId") id: Int): Response<TradeMeetingResponse>

    @POST("api/v1/trades/{meetingId}/confirm")
    suspend fun confirmTrade(
        @Path("meetingId") id: Int,
        @Body request: ConfirmTradeRequest
    ): Response<Map<String, Any>>
}
