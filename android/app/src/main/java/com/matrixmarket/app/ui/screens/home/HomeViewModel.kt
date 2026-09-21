package com.matrixmarket.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrixmarket.app.data.remote.dto.CategoryDto
import com.matrixmarket.app.data.remote.dto.ListingResponse
import com.matrixmarket.app.data.repository.ListingRepository
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ListingRepository) : ViewModel() {

    private val _listingsState = MutableStateFlow<Resource<List<ListingResponse>>>(Resource.Loading)
    val listingsState: StateFlow<Resource<List<ListingResponse>>> = _listingsState.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCategories()
        loadListings()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = repository.getCategories()
            if (result is Resource.Success) _categories.value = result.data
        }
    }

    fun loadListings() {
        _listingsState.value = Resource.Loading
        viewModelScope.launch {
            _listingsState.value = repository.getListings(_selectedCategoryId.value, _searchQuery.value.ifBlank { null })
        }
    }

    fun onCategorySelected(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        loadListings()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        loadListings()
    }
}
