package com.matrixmarket.app

import com.matrixmarket.app.data.remote.dto.CategoryDto
import com.matrixmarket.app.data.remote.dto.ListingResponse
import com.matrixmarket.app.data.repository.ListingRepository
import com.matrixmarket.app.ui.screens.createlisting.CreateListingViewModel
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CreateListingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ListingRepository
    private lateinit var viewModel: CreateListingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        runTest {
            whenever(repository.getCategories()).thenReturn(
                Resource.Success(listOf(CategoryDto(1, "Textbooks", null)))
            )
        }
        viewModel = CreateListingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createListing rejects blank title with a validation error`() = runTest {
        viewModel.createListing(
            categoryId = 1, title = "", description = "desc", price = "100", listingType = "Sell", isbn = null
        )
        advanceUntilIdle()
        val state = viewModel.createState.value
        assertTrue(state is Resource.Error)
    }

    @Test
    fun `createListing rejects a non-numeric price`() = runTest {
        viewModel.createListing(
            categoryId = 1, title = "Calculus Textbook", description = "Used, good condition",
            price = "not-a-number", listingType = "Sell", isbn = null
        )
        advanceUntilIdle()
        assertTrue(viewModel.createState.value is Resource.Error)
    }

    @Test
    fun `createListing succeeds and returns the created listing on valid input`() = runTest {
        val fakeListing = ListingResponse(
            listingId = 1, title = "Calculus Textbook", description = "Good condition",
            price = 235.0, listingType = "Sell", isbn = "9780134763644", imageUrl = null,
            status = "Active", categoryName = "Textbooks", sellerName = "Test Student",
            sellerId = 1, sellerKarma = 0, createdAt = "2026-09-15T00:00:00"
        )
        whenever(repository.createListing(org.mockito.kotlin.any())).thenReturn(Resource.Success(fakeListing))

        viewModel.createListing(
            categoryId = 1, title = "Calculus Textbook", description = "Good condition",
            price = "235", listingType = "Sell", isbn = "9780134763644"
        )
        advanceUntilIdle()

        val state = viewModel.createState.value
        assertTrue(state is Resource.Success)
        assertEquals("Calculus Textbook", (state as Resource.Success).data.title)
    }

    @Test
    fun `onIsbnScanned updates the scanned isbn state`() {
        viewModel.onIsbnScanned("9780134763644")
        assertEquals("9780134763644", viewModel.scannedIsbn.value)
        viewModel.clearScannedIsbn()
        assertNull(viewModel.scannedIsbn.value)
    }
}
