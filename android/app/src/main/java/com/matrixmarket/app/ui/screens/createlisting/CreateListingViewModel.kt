package com.matrixmarket.app.ui.screens.createlisting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrixmarket.app.data.remote.dto.CategoryDto
import com.matrixmarket.app.data.remote.dto.CreateListingRequest
import com.matrixmarket.app.data.remote.dto.ListingResponse
import com.matrixmarket.app.data.repository.ListingRepository
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateListingViewModel(private val repository: ListingRepository) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    private val _createState = MutableStateFlow<Resource<ListingResponse>>(Resource.Idle)
    val createState: StateFlow<Resource<ListingResponse>> = _createState.asStateFlow()

    // Populated by the ISBN Barcode Scanner screen (User Defined Feature 1) when the
    // user scans a physical textbook before filling out the rest of the listing form.
    private val _scannedIsbn = MutableStateFlow<String?>(null)
    val scannedIsbn: StateFlow<String?> = _scannedIsbn.asStateFlow()

    init {
        viewModelScope.launch {
            val result = repository.getCategories()
            if (result is Resource.Success) _categories.value = result.data
        }
    }

    fun onIsbnScanned(isbn: String) {
        _scannedIsbn.value = isbn
    }

    fun clearScannedIsbn() {
        _scannedIsbn.value = null
    }

    fun createListing(
        categoryId: Int, title: String, description: String,
        price: String, listingType: String, isbn: String?
    ) {
        val priceValue = price.toDoubleOrNull()
        if (title.isBlank() || description.isBlank() || priceValue == null || priceValue <= 0) {
            _createState.value = Resource.Error("Please fill in a title, description, and a valid price.")
            return
        }

        _createState.value = Resource.Loading
        viewModelScope.launch {
            _createState.value = repository.createListing(
                CreateListingRequest(categoryId, title.trim(), description.trim(), priceValue, listingType, isbn)
            )
        }
    }

    fun resetState() {
        _createState.value = Resource.Idle
    }
}
