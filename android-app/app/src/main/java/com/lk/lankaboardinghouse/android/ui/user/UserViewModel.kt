package com.lk.lankaboardinghouse.android.ui.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto
import com.lk.lankaboardinghouse.android.data.model.DistrictDto
import com.lk.lankaboardinghouse.android.data.model.TownDto
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    var districts by mutableStateOf<List<DistrictDto>>(emptyList())
        private set
    var towns by mutableStateOf<List<TownDto>>(emptyList())
        private set

    var selectedDistrict by mutableStateOf<DistrictDto?>(null)
        private set
    var selectedTown by mutableStateOf<TownDto?>(null)
        private set

    var searchResults by mutableStateOf<List<BoardingHouseResponseDto>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)
        private set
    var hasSearched by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadDistricts()
    }

    private fun loadDistricts() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getDistricts()
                if (response.isSuccessful) {
                    districts = response.body() ?: emptyList()
                }
            } catch (_: Exception) {
                // Silently fail for now
            }
        }
    }

    fun onDistrictSelected(district: DistrictDto) {
        selectedDistrict = district
        selectedTown = null
        towns = emptyList()
        searchResults = emptyList()
        hasSearched = false
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTowns(district.id)
                if (response.isSuccessful) {
                    towns = response.body() ?: emptyList()
                }
            } catch (_: Exception) {
                // Silently fail for now
            }
        }
    }

    fun onTownSelected(town: TownDto) {
        selectedTown = town
        search()
    }

    private fun search() {
        val town = selectedTown ?: return
        isSearching = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchBoardingHouses(town.id)
                if (response.isSuccessful) {
                    searchResults = response.body() ?: emptyList()
                } else {
                    errorMessage = "Search failed"
                }
            } catch (e: Exception) {
                errorMessage = "Could not connect to server: ${e.message}"
            } finally {
                isSearching = false
                hasSearched = true
            }
        }
    }
}