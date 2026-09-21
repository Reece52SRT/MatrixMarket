package com.matrixmarket.app

import com.matrixmarket.app.data.remote.dto.TradeMeetingResponse
import com.matrixmarket.app.data.repository.TradeRepository
import com.matrixmarket.app.ui.screens.trademeeting.TradeMeetingViewModel
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

// Tests for User Defined Feature 2 (Trade Meeting Scheduler) logic.
@OptIn(ExperimentalCoroutinesApi::class)
class TradeMeetingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TradeRepository
    private lateinit var viewModel: TradeMeetingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        viewModel = TradeMeetingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scheduleMeeting rejects a blank campus spot`() = runTest {
        viewModel.scheduleMeeting(offerId = 1, campusSpot = "", meetingTimeIso = "2026-09-20T14:00:00")
        advanceUntilIdle()
        assertTrue(viewModel.scheduleState.value is Resource.Error)
    }

    @Test
    fun `scheduleMeeting returns the generated confirmation code on success`() = runTest {
        val fakeMeeting = TradeMeetingResponse(
            meetingId = 1, offerId = 1, campusSpot = "Library Foyer",
            meetingTime = "2026-09-20T14:00:00", confirmationCode = "482913",
            isConfirmed = false, listingTitle = "Calculus Textbook"
        )
        whenever(repository.scheduleMeeting(any(), any(), any())).thenReturn(Resource.Success(fakeMeeting))

        viewModel.scheduleMeeting(1, "Library Foyer", "2026-09-20T14:00:00")
        advanceUntilIdle()

        val state = viewModel.scheduleState.value
        assertTrue(state is Resource.Success)
        assertEquals("482913", (state as Resource.Success).data.confirmationCode)
    }

    @Test
    fun `confirmTrade rejects a code that is not 6 digits`() = runTest {
        viewModel.confirmTrade(meetingId = 1, code = "123")
        advanceUntilIdle()
        assertTrue(viewModel.confirmState.value is Resource.Error)
    }

    @Test
    fun `confirmTrade succeeds with a valid 6-digit code`() = runTest {
        whenever(repository.confirmTrade(any(), any())).thenReturn(Resource.Success("Trade confirmed! Karma points awarded."))

        viewModel.confirmTrade(meetingId = 1, code = "482913")
        advanceUntilIdle()

        assertTrue(viewModel.confirmState.value is Resource.Success)
    }
}
