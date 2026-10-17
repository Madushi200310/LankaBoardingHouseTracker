package com.lk.lankaboardinghouse.android.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.AdminUserDto
import com.lk.lankaboardinghouse.android.data.model.SetDisabledRequest
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class AdminUsersViewModel : ViewModel() {

    var users by mutableStateOf<List<AdminUserDto>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var actionInProgressId by mutableStateOf<Long?>(null)
        private set

    init {
        loadUsers()
    }

    fun loadUsers() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.listUsers()
                if (response.isSuccessful) {
                    users = response.body() ?: emptyList()
                } else {
                    errorMessage = "Failed to load users"
                }
            } catch (e: Exception) {
                errorMessage = "Could not connect to server: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun setDisabled(user: AdminUserDto, disabled: Boolean) {
        actionInProgressId = user.id
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.setUserDisabled(
                    user.id,
                    SetDisabledRequest(disabled)
                )
                val updated = response.body()
                if (response.isSuccessful && updated != null) {
                    users = users.map { if (it.id == updated.id) updated else it }
                } else {
                    errorMessage = "Could not update the user"
                }
            } catch (e: Exception) {
                errorMessage = "Could not connect to server: ${e.message}"
            } finally {
                actionInProgressId = null
            }
        }
    }
}