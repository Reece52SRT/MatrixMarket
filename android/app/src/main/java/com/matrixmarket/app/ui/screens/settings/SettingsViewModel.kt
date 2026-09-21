package com.matrixmarket.app.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrixmarket.app.data.local.SessionManager
import com.matrixmarket.app.data.repository.AuthRepository
import com.matrixmarket.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val fullName: String = "",
    val email: String = "",
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val saveMessage: String? = null
)

// Backs the Settings screen: users can update their profile info, choose a
// language, and toggle notification preferences. Required feature per the brief
// ("the user must be able to change their settings in the app").
class SettingsViewModel(
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                fullName = sessionManager.getFullName() ?: "",
                language = sessionManager.getLanguage(),
                notificationsEnabled = sessionManager.getNotificationsEnabled()
            )
        }
    }

    fun onFullNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name)
    }

    fun onLanguageSelected(languageCode: String) {
        if (_uiState.value.language == languageCode) return

        _uiState.value = _uiState.value.copy(language = languageCode)

        // Set application locales - AppCompat automatically handles persistence across cold starts
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)

        viewModelScope.launch {
            sessionManager.setLanguage(languageCode)
        }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setNotificationsEnabled(enabled)
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveMessage = null)
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isSaving = false, saveMessage = "Not logged in.")
                return@launch
            }
            val result = profileRepository.updateSettings(userId, _uiState.value.fullName, null)
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveMessage = if (result is com.matrixmarket.app.util.Resource.Success) "Settings saved." else "Could not save settings."
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
