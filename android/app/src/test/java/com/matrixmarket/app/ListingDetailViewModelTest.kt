package com.matrixmarket.app

import com.matrixmarket.app.data.remote.dto.ListingResponse
import com.matrixmarket.app.data.remote.dto.OfferResponse
import com.matrixmarket.app.data.repository.ListingRepository
import com.matrixmarket.app.data.repository.TradeRepository
import com.matrixmarket.app.ui.screens.listingdetail.ListingDetailViewModel
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

// Tests the bridge between browsing a listing and starting a Trade Meeting (the
// offer -> offerId hand-off that closes the loop between Home and User Defined Feature 2).
@OptIn(ExperimentalCoroutinesApi::class)
class ListingDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var listingRepository: ListingRepository
    private lateinit var tradeRepository: TradeRepository
    private lateinit var viewModel: ListingDetailViewModel

    private val fakeListing = ListingResponse(
        listingId = 5, title = "Casio fx-991ZA", description = "Barely used scientific calculator",
        price = 200.0, listingType = "Sell", isbn = null, imageUrl = null, status = "Active",
        categoryName = "Electronics", sellerName = "Reece Moodley", sellerId = 2, sellerKarma = 45,
        createdAt = "2026-09-01T00:00:00"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        listingRepository = mock()
        tradeRepository = mock()
        viewModel = ListingDetailViewModel(listingRepository, tradeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadListing populates listingState on success`() = runTest {
        whenever(listingRepository.getListingById(5)).thenReturn(Resource.Success(fakeListing))

        viewModel.loadListing(5)
        advanceUntilIdle()

        val state = viewModel.listingState.value
        assertTrue(state is Resource.Success)
        assertEquals("Casio fx-991ZA", (state as Resource.Success).data.title)
    }

    @Test
    fun `makeOffer rejects a zero or negative amount`() = runTest {
        viewModel.makeOffer(listingId = 5, offeredPrice = 0.0)
        advanceUntilIdle()
        assertTrue(viewModel.offerState.value is Resource.Error)
    }

    @Test
    fun `makeOffer on success exposes the new offerId for Trade Meeting navigation`() = runTest {
        whenever(tradeRepository.makeOffer(any(), any())).thenReturn(Resource.Success(OfferResponse(offerId = 42, status = "Pending")))

        viewModel.makeOffer(listingId = 5, offeredPrice = 180.0)
        advanceUntilIdle()

        val state = viewModel.offerState.value
        assertTrue(state is Resource.Success)
        assertEquals(42, (state as Resource.Success).data)
    }

    @Test
    fun `resetOfferState returns offerState to Idle`() {
        viewModel.resetOfferState()
        assertEquals(Resource.Idle, viewModel.offerState.value)
    }
}
