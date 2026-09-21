package com.matrixmarket.app.data.remote

import com.matrixmarket.app.BuildConfig
import com.matrixmarket.app.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Builds a single shared Retrofit instance. An OkHttp interceptor automatically attaches
// the stored JWT bearer token (from login/SSO) to every outgoing request, so ViewModels
// never need to worry about auth headers manually.
object RetrofitClient {

    lateinit var sessionManager: SessionManager

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { sessionManager.getToken() }
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
