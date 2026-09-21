package com.matrixmarket.app.ui.screens.listingdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrixmarket.app.data.remote.dto.ListingResponse
import com.matrixmarket.app.data.repository.ListingRepository
import com.matrixmarket.app.data.repository.TradeRepository
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Bridges browsing and trading: loads one listing's full detail, then lets the
// buyer submit an offer, which produces the offerId the Trade Meeting screen needs.
class ListingDetailViewModel(
    private val listingRepository: ListingRepository,
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _listingState = MutableStateFlow<Resource<ListingResponse>>(Resource.Loading)
    val listingState: StateFlow<Resource<ListingResponse>> = _listingState.asStateFlow()

    private val _offerState = MutableStateFlow<Resource<Int>>(Resource.Idle) // holds the created offerId
    val offerState: StateFlow<Resource<Int>> = _offerState.asStateFlow()

    fun loadListing(listingId: Int) {
        _listingState.value = Resource.Loading
        viewModelScope.launch {
            _listingState.value = listingRepository.getListingById(listingId)
        }
    }

    fun makeOffer(listingId: Int, offeredPrice: Double) {
        if (offeredPrice <= 0) {
            _offerState.value = Resource.Error("Enter a valid offer amount.")
            return
        }
        _offerState.value = Resource.Loading
        viewModelScope.launch {
            when (val result = tradeRepository.makeOffer(listingId, offeredPrice)) {
                is Resource.Success -> _offerState.value = Resource.Success(result.data.offerId)
                is Resource.Error -> _offerState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }

    fun resetOfferState() {
        _offerState.value = Resource.Idle
    }
}
