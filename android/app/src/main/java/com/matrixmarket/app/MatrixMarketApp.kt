package com.matrixmarket.app

import android.app.Application
import android.util.Log
import com.matrixmarket.app.data.local.SessionManager
import com.matrixmarket.app.data.remote.RetrofitClient

class MatrixMarketApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        Log.i("MatrixMarketApp", "Application starting up")

        sessionManager = SessionManager(applicationContext)
        RetrofitClient.sessionManager = sessionManager
    }
}