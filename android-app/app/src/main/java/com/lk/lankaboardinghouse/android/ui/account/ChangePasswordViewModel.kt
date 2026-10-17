package com.lk.lankaboardinghouse.android.ui.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.ChangePasswordRequest
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

class ChangePasswordViewModel : ViewModel() {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }

    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var state by mutableStateOf<ChangePasswordState>(ChangePasswordState.Idle)
        private set

    private fun validate(): String? = when {
        currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() ->
            "Please fill in all fields"
        newPassword.length < MIN_PASSWORD_LENGTH ->
            "New password must be at least $MIN_PASSWORD_LENGTH characters"
        newPassword != confirmPassword ->
            "New passwords do not match"
        newPassword == currentPassword ->
            "New password must be different from the current one"
        else -> null
    }

    fun submit() {
        if (state is ChangePasswordState.Loading) return

        validate()?.let { message ->
            state = ChangePasswordState.Error(message)
            return
        }

        state = ChangePasswordState.Loading

        viewModelScope.launch {
            state = try {
                val response = RetrofitClient.accountApi.changePassword(
                    ChangePasswordRequest(currentPassword, newPassword)
                )
                if (response.isSuccessful) {
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    ChangePasswordState.Success
                } else {
                    ChangePasswordState.Error(readServerMessage(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                ChangePasswordState.Error("Could not connect to server: ${e.message}")
            }
        }
    }

    // The backend answers errors as {"message": "..."}
    private fun readServerMessage(body: String?): String {
        val fallback = "Could not change password"
        if (body.isNullOrBlank()) return fallback
        return try {
            JSONObject(body).optString("message").ifBlank { fallback }
        } catch (_: Exception) {
            fallback
        }
    }
}