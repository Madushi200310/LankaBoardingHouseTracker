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

enum class SortOption(val label: String, val apiValue: String) {
    NEWEST("Newest first", "newest"),
    PRICE_LOW_HIGH("Price: low to high", "price_asc"),
    PRICE_HIGH_LOW("Price: high to low", "price_desc")
}

class UserViewModel : ViewModel() {

    var districts by mutableStateOf<List<DistrictDto>>(emptyList())
        private set
    var towns by mutableStateOf<List<TownDto>>(emptyList())
        private set

    var selectedDistrict by mutableStateOf<DistrictDto?>(null)
        private set
    var selectedTown by mutableStateOf<TownDto?>(null)
        private set

    // Filters (kept as text so the fields can be edited freely)
    var minPrice by mutableStateOf("")
    var maxPrice by mutableStateOf("")
    var sortOption by mutableStateOf(SortOption.NEWEST)
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
        errorMessage = null
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

    fun onSortSelected(option: SortOption) {
        sortOption = option
        search()
    }

    fun clearFilters() {
        minPrice = ""
        maxPrice = ""
        sortOption = SortOption.NEWEST
        search()
    }

    fun search() {
        val town = selectedTown ?: return

        val min = minPrice.toDoubleOrNull()
        val max = maxPrice.toDoubleOrNull()

        if ((minPrice.isNotBlank() && min == null) || (maxPrice.isNotBlank() && max == null)) {
            errorMessage = "Please enter valid prices (numbers only)"
            return
        }
        if (min != null && max != null && min > max) {
            errorMessage = "Minimum price cannot be higher than maximum price"
            return
        }

        isSearching = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchBoardingHouses(
                    townId = town.id,
                    minPrice = min,
                    maxPrice = max,
                    sort = sortOption.apiValue
                )
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