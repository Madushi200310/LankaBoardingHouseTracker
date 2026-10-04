package com.lk.lankaboardinghouse.android.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.LoginRequest
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: UserResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun onEmailChange(value: String) {
        email = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            uiState = LoginUiState.Error("Email and password are required")
            return
        }

        uiState = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    uiState = LoginUiState.Success(response.body()!!)
                } else {
                    uiState = LoginUiState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error("Could not connect to server: ${e.message}")
            }
        }
    }
}