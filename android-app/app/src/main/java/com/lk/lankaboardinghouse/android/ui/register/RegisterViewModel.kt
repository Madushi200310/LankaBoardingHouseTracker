package com.lk.lankaboardinghouse.android.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.RegisterRequest
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: UserResponse) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel : ViewModel() {

    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var selectedRole by mutableStateOf("USER")
        private set

    var uiState by mutableStateOf<RegisterUiState>(RegisterUiState.Idle)
        private set

    fun onRoleSelected(role: String) {
        selectedRole = role
    }

    fun register() {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || phoneNumber.isBlank()) {
            uiState = RegisterUiState.Error("Please fill in all fields")
            return
        }
        if (password.length < 6) {
            uiState = RegisterUiState.Error("Password must be at least 6 characters")
            return
        }
        if (password != confirmPassword) {
            uiState = RegisterUiState.Error("Passwords do not match")
            return
        }

        uiState = RegisterUiState.Loading

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    fullName = fullName,
                    email = email,
                    password = password,
                    phoneNumber = phoneNumber,
                    role = selectedRole
                )
                val response = RetrofitClient.apiService.register(request)
                if (response.isSuccessful && response.body() != null) {
                    uiState = RegisterUiState.Success(response.body()!!)
                } else if (response.code() == 400) {
                    uiState = RegisterUiState.Error("Email already registered")
                } else {
                    uiState = RegisterUiState.Error("Registration failed")
                }
            } catch (e: Exception) {
                uiState = RegisterUiState.Error("Could not connect to server: ${e.message}")
            }
        }
    }
}