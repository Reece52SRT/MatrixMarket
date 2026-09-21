package com.matrixmarket.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "matrix_market_session")

// Persists the logged-in user's JWT token and profile basics locally using DataStore,
// so the session survives app restarts. Used by RetrofitClient to attach auth headers.
class SessionManager(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = intPreferencesKey("user_id")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("student_email")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = stringPreferencesKey("notifications_enabled")
    }

    suspend fun saveSession(token: String, userId: Int, fullName: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = userId
            prefs[Keys.FULL_NAME] = fullName
            prefs[Keys.EMAIL] = email
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.map { it[Keys.TOKEN] }.first()

    suspend fun getUserId(): Int? = context.dataStore.data.map { it[Keys.USER_ID] }.first()

    suspend fun getFullName(): String? = context.dataStore.data.map { it[Keys.FULL_NAME] }.first()

    suspend fun isLoggedIn(): Boolean = getToken() != null

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = languageCode }
    }

    suspend fun getLanguage(): String =
        context.dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }.first()

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled.toString() }
    }

    suspend fun getNotificationsEnabled(): Boolean =
        context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED]?.toBoolean() ?: true }.first()

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}
