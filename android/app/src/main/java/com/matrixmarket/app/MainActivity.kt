package com.matrixmarket.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.matrixmarket.app.navigation.MatrixMarketNavGraph
import com.matrixmarket.app.ui.theme.MatrixMarketTheme

class MainActivity : AppCompatActivity() { // Changed from ComponentActivity to AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.i("MainActivity", "onCreate - Matrix Market launching")

        val app = application as MatrixMarketApp

        setContent {
            MatrixMarketTheme {
                MatrixMarketNavGraph(sessionManager = app.sessionManager)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
    }
}