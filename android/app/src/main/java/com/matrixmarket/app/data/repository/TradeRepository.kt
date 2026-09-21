package com.matrixmarket.app.data.repository

import android.util.Log
import com.matrixmarket.app.data.remote.ApiService
import com.matrixmarket.app.data.remote.dto.*
import com.matrixmarket.app.util.Resource

private const val TAG = "TradeRepository"

// Backs User Defined Feature 2 (Trade Meeting Scheduler) end-to-end: making an offer,
// scheduling a verified campus meeting spot/time, and confirming the exchange via code.
class TradeRepository(private val api: ApiService) {

    suspend fun makeOffer(listingId: Int, offeredPrice: Double): Resource<OfferResponse> {
        return try {
            val response = api.createOffer(CreateOfferRequest(listingId, offeredPrice))
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!)
            else Resource.Error("Could not submit offer (${response.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "makeOffer failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while submitting offer.")
        }
    }

    suspend fun getMyMeetings(): Resource<List<MyTradeMeetingResponse>> {
        return try {
            val response = api.getMyTradeMeetings()
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!)
            else Resource.Error("Could not load your trade meetings (${response.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "getMyMeetings failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while loading trade meetings.")
        }
    }

    suspend fun scheduleMeeting(offerId: Int, campusSpot: String, meetingTimeIso: String): Resource<TradeMeetingResponse> {
        return try {
            val response = api.scheduleTrade(ScheduleTradeRequest(offerId, campusSpot, meetingTimeIso))
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!)
            else Resource.Error("Could not schedule meeting (${response.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "scheduleMeeting failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while scheduling meeting.")
        }
    }

    suspend fun confirmTrade(meetingId: Int, code: String): Resource<String> {
        return try {
            val response = api.confirmTrade(meetingId, ConfirmTradeRequest(code))
            if (response.isSuccessful) Resource.Success("Trade confirmed! Karma points awarded.")
            else Resource.Error(response.errorBody()?.string() ?: "Incorrect code or trade could not be confirmed.")
        } catch (e: Exception) {
            Log.e(TAG, "confirmTrade failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while confirming trade.")
        }
    }
}
