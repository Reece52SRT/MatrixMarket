package com.matrixmarket.app.data.repository

import android.util.Log
import com.matrixmarket.app.data.remote.ApiService
import com.matrixmarket.app.data.remote.dto.CategoryDto
import com.matrixmarket.app.data.remote.dto.CreateListingRequest
import com.matrixmarket.app.data.remote.dto.ListingResponse
import com.matrixmarket.app.util.Resource

private const val TAG = "ListingRepository"

class ListingRepository(private val api: ApiService) {

    suspend fun getListings(categoryId: Int? = null, search: String? = null): Resource<List<ListingResponse>> {
        return try {
            val response = api.getListings(categoryId, search)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Could not load listings (${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getListings failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while loading listings.")
        }
    }

    suspend fun getCategories(): Resource<List<CategoryDto>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful) Resource.Success(response.body() ?: emptyList())
            else Resource.Error("Could not load categories (${response.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "getCategories failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while loading categories.")
        }
    }

    suspend fun getListingById(id: Int): Resource<ListingResponse> {
        return try {
            val response = api.getListingById(id)
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!)
            else Resource.Error("Could not load this listing (${response.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "getListingById failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while loading this listing.")
        }
    }

    suspend fun createListing(request: CreateListingRequest): Resource<ListingResponse> {
        return try {
            val response = api.createListing(request)
            if (response.isSuccessful && response.body() != null) {
                Log.i(TAG, "Listing created: ${response.body()!!.listingId}")
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Could not create listing (${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "createListing failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while creating listing.")
        }
    }
}
