package com.matrixmarket.app.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

private const val TAG = "GoogleAuthManager"

class GoogleAuthManager(private val context: Context) {

    private val webClientId = "314417582987-9lrn92g6gvckn91l8ndcss2n666lac17.apps.googleusercontent.com"

    private val googleSignInClient: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    suspend fun getSignInIntent(): Intent {
        // Clear cached account session so account picker shows reliably
        try {
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            Log.w(TAG, "Sign-out prior to sign-in intent failed", e)
        }
        return googleSignInClient.signInIntent
    }

    data class SsoResult(val email: String, val fullName: String)

    suspend fun handleSignInResult(data: Intent?): Result<SsoResult> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
                ?: return Result.failure(Exception("Google Sign-In returned null account"))

            val idToken = account.idToken ?: return Result.failure(Exception("Google ID Token missing"))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Firebase sign-in returned no user"))

            Log.i(TAG, "Google SSO succeeded for ${firebaseUser.email}")
            Result.success(SsoResult(firebaseUser.email ?: "", firebaseUser.displayName ?: ""))
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed with code: ${e.statusCode}", e)
            val customMsg = when (e.statusCode) {
                10 -> "Developer Error (StatusCode 10): Ensure SHA-1 fingerprint is added to Firebase and Web Client ID matches."
                12500 -> "Sign-In Failed (StatusCode 12500): Check Google Play Services or Web Client ID configuration."
                else -> e.localizedMessage ?: "Google ApiException status code: ${e.statusCode}"
            }
            Result.failure(Exception(customMsg, e))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase credential sign-in failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        googleSignInClient.signOut()
        FirebaseAuth.getInstance().signOut()
    }
}