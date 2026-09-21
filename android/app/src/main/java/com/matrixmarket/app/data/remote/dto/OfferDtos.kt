package com.matrixmarket.app.data.remote.dto

data class CreateOfferRequest(
    val listingId: Int,
    val offeredPrice: Double
)

data class OfferResponse(
    val offerId: Int,
    val status: String
)

data class ScheduleTradeRequest(
    val offerId: Int,
    val campusSpot: String,
    val meetingTime: String // ISO-8601, e.g. 2026-09-20T14:00:00
)

data class TradeMeetingResponse(
    val meetingId: Int,
    val offerId: Int,
    val campusSpot: String,
    val meetingTime: String,
    val confirmationCode: String,
    val isConfirmed: Boolean,
    val listingTitle: String
)

data class ConfirmTradeRequest(
    val confirmationCode: String
)

// One row for the Meet tab's list - works for both sides of the trade, since
// "role" tells us whether this user is the Buyer or the Seller for this meeting.
data class MyTradeMeetingResponse(
    val meetingId: Int,
    val offerId: Int,
    val campusSpot: String,
    val meetingTime: String,
    val confirmationCode: String,
    val isConfirmed: Boolean,
    val listingTitle: String,
    val price: Double,
    val role: String, // "Buyer" or "Seller"
    val otherPartyName: String
)
