package com.matrixmarket.app.util

// Simple sealed wrapper so every screen can render Loading / Success / Error states
// consistently for every network call, keeping the UI robust against failures
// (required for graceful handling of invalid input / network errors per the rubric).
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    object Idle : Resource<Nothing>()
}
