package com.matrixmarket.app.ui.screens.trademeeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrixmarket.app.data.remote.dto.MyTradeMeetingResponse
import com.matrixmarket.app.data.remote.dto.TradeMeetingResponse
import com.matrixmarket.app.data.repository.TradeRepository
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Drives User Defined Feature 2: scheduling a verified on-campus trade meeting spot
// and time for an accepted offer, then confirming the exchange with a 6-digit code.
// Also backs the "Meet" tab's list of all meetings the current user is part of,
// whether they're the buyer or the seller.
class TradeMeetingViewModel(private val repository: TradeRepository) : ViewModel() {

    private val _myMeetingsState = MutableStateFlow<Resource<List<MyTradeMeetingResponse>>>(Resource.Idle)
    val myMeetingsState: StateFlow<Resource<List<MyTradeMeetingResponse>>> = _myMeetingsState.asStateFlow()

    private val _scheduleState = MutableStateFlow<Resource<TradeMeetingResponse>>(Resource.Idle)
    val scheduleState: StateFlow<Resource<TradeMeetingResponse>> = _scheduleState.asStateFlow()

    private val _confirmState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val confirmState: StateFlow<Resource<String>> = _confirmState.asStateFlow()

    fun loadMyMeetings() {
        _myMeetingsState.value = Resource.Loading
        viewModelScope.launch {
            _myMeetingsState.value = repository.getMyMeetings()
        }
    }

    fun scheduleMeeting(offerId: Int, campusSpot: String, meetingTimeIso: String) {
        if (campusSpot.isBlank()) {
            _scheduleState.value = Resource.Error("Please choose a campus meeting spot.")
            return
        }
        _scheduleState.value = Resource.Loading
        viewModelScope.launch {
            _scheduleState.value = repository.scheduleMeeting(offerId, campusSpot, meetingTimeIso)
        }
    }

    fun confirmTrade(meetingId: Int, code: String) {
        if (code.length != 6) {
            _confirmState.value = Resource.Error("Enter the 6-digit confirmation code.")
            return
        }
        _confirmState.value = Resource.Loading
        viewModelScope.launch {
            _confirmState.value = repository.confirmTrade(meetingId, code)
            loadMyMeetings() // refresh the list so a newly-confirmed meeting updates
        }
    }
}
