package com.matrixmarket.app.data.remote.dto

data class RegisterRequest(
    val studentEmail: String,
    val password: String,
    val fullName: String,
    val campus: String? = null
)

data class LoginRequest(
    val studentEmail: String,
    val password: String
)

data class SsoLoginRequest(
    val studentEmail: String,
    val fullName: String,
    val provider: String = "Google"
)

data class AuthResponse(
    val token: String,
    val userId: Int,
    val fullName: String,
    val studentEmail: String,
    val karmaPoints: Int
)
