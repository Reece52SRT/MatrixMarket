package com.matrixmarket.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrixmarket.app.data.repository.AuthRepository
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val authState: StateFlow<Resource<Unit>> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Please enter both your student email and password.")
            return
        }
        _authState.value = Resource.Loading
        viewModelScope.launch {
            when (val result = authRepository.login(email.trim(), password)) {
                is Resource.Success -> _authState.value = Resource.Success(Unit)
                is Resource.Error -> _authState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }

    fun register(email: String, password: String, fullName: String, campus: String?) {
        if (email.isBlank() || password.length < 6 || fullName.isBlank()) {
            _authState.value = Resource.Error("Please fill in all fields (password must be 6+ characters).")
            return
        }
        _authState.value = Resource.Loading
        viewModelScope.launch {
            when (val result = authRepository.register(email.trim(), password, fullName.trim(), campus)) {
                is Resource.Success -> _authState.value = Resource.Success(Unit)
                is Resource.Error -> _authState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }

    // Completes the SSO flow after GoogleAuthManager has already verified the identity.
    fun completeSsoLogin(email: String, fullName: String) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            when (val result = authRepository.ssoLogin(email, fullName)) {
                is Resource.Success -> _authState.value = Resource.Success(Unit)
                is Resource.Error -> _authState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }

    fun resetState() {
        _authState.value = Resource.Idle
    }

    fun reportSsoFailure(message: String) {
        _authState.value = Resource.Error(message)
    }
}
