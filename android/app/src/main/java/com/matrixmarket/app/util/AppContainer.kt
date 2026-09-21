package com.matrixmarket.app.util

import com.matrixmarket.app.data.local.SessionManager
import com.matrixmarket.app.data.remote.RetrofitClient
import com.matrixmarket.app.data.repository.AuthRepository
import com.matrixmarket.app.data.repository.ListingRepository
import com.matrixmarket.app.data.repository.ProfileRepository
import com.matrixmarket.app.data.repository.TradeRepository

// A lightweight manual service locator (avoids pulling in a full DI framework for
// what is a fairly small app). Repositories are simple and cheap, so plain lazy
// singletons here keep things easy to read, test, and mock.
object AppContainer {
    val sessionManager: SessionManager get() = RetrofitClient.sessionManager

    val authRepository: AuthRepository by lazy {
        AuthRepository(RetrofitClient.apiService, RetrofitClient.sessionManager)
    }
    val listingRepository: ListingRepository by lazy {
        ListingRepository(RetrofitClient.apiService)
    }
    val tradeRepository: TradeRepository by lazy {
        TradeRepository(RetrofitClient.apiService)
    }
    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(RetrofitClient.apiService)
    }
}
