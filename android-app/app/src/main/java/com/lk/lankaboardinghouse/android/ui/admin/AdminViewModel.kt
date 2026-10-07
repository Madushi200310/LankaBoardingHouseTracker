package com.lk.lankaboardinghouse.android.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    var pendingListings by mutableStateOf<List<BoardingHouseResponseDto>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var actionInProgressId by mutableStateOf<Long?>(null)
        private set

    init {
        loadPending()
    }

    fun loadPending() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getPendingBoardingHouses()
                if (response.isSuccessful) {
                    pendingListings = response.body() ?: emptyList()
                } else {
                    errorMessage = "Failed to load pending requests"
                }
            } catch (e: Exception) {
                errorMessage = "Could not connect to server: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun approve(id: Long) {
        actionInProgressId = id
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.approveBoardingHouse(id)
                if (response.isSuccessful) {
                    pendingListings = pendingListings.filterNot { it.id == id }
                } else {
                    errorMessage = "Failed to approve listing"
                }
            } catch (e: Exception) {
                errorMessage = "Could not connect to server: ${e.message}"
            } finally {
                actionInProgressId = null
            }
        }
    }

    fun decline(id: Long) {
        actionInProgressId = id
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.declineBoardingHouse(id)
                if (response.isSuccessful) {
                    pendingListings = pendingListings.filterNot { it.id == id }
                } else {
                    errorMessage = "Failed to decline listing"
                }
            } catch (e: Exception) {
                errorMessage = "Could not connect to server: ${e.message}"
            } finally {
                actionInProgressId = null
            }
        }
    }
}