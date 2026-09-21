package com.matrixmarket.app.data.repository

import android.util.Log
import com.matrixmarket.app.data.local.SessionManager
import com.matrixmarket.app.data.remote.ApiService
import com.matrixmarket.app.data.remote.dto.*
import com.matrixmarket.app.util.Resource

private const val TAG = "AuthRepository"

class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun register(email: String, password: String, fullName: String, campus: String?): Resource<AuthResponse> =
        safeCall("register") { api.register(RegisterRequest(email, password, fullName, campus)) }

    suspend fun login(email: String, password: String): Resource<AuthResponse> =
        safeCall("login") { api.login(LoginRequest(email, password)) }

    // Called once Firebase/Google Sign-In has already verified the user's identity on-device;
    // this exchanges that verified identity for our own backend JWT (completing the SSO flow).
    suspend fun ssoLogin(email: String, fullName: String): Resource<AuthResponse> =
        safeCall("ssoLogin") { api.ssoLogin(SsoLoginRequest(email, fullName)) }

    private suspend fun safeCall(
        tag: String,
        block: suspend () -> retrofit2.Response<AuthResponse>
    ): Resource<AuthResponse> {
        return try {
            val response = block()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveSession(body.token, body.userId, body.fullName, body.studentEmail)
                Log.i(TAG, "$tag succeeded for user ${body.userId}")
                Resource.Success(body)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Authentication failed (${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "$tag failed", e)
            Resource.Error(e.localizedMessage ?: "Network error. Please check your connection.")
        }
    }

    suspend fun logout() = sessionManager.logout()
}
