package com.matrixmarket.app.data.repository

import android.util.Log
import com.matrixmarket.app.data.remote.ApiService
import com.matrixmarket.app.data.remote.dto.UpdateSettingsRequest
import com.matrixmarket.app.data.remote.dto.UserProfileResponse
import com.matrixmarket.app.util.Resource

private const val TAG = "ProfileRepository"

class ProfileRepository(private val api: ApiService) {

    suspend fun getProfile(userId: Int): Resource<UserProfileResponse> {
        return try {
            val response = api.getProfile(userId)
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!)
            else Resource.Error("Could not load profile (${response.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "getProfile failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while loading profile.")
        }
    }

    suspend fun updateSettings(userId: Int, fullName: String?, campus: String?): Resource<Unit> {
        return try {
            val response = api.updateSettings(userId, UpdateSettingsRequest(campus, fullName))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Could not update settings (${response.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "updateSettings failed", e)
            Resource.Error(e.localizedMessage ?: "Network error while updating settings.")
        }
    }
}
