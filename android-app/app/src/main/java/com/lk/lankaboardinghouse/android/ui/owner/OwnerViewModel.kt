package com.lk.lankaboardinghouse.android.ui.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseRequestDto
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto
import com.lk.lankaboardinghouse.android.data.model.DistrictDto
import com.lk.lankaboardinghouse.android.data.model.TownDto
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}

class OwnerViewModel : ViewModel() {

    // Form fields
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var rulesAndRegulations by mutableStateOf("")
    var price by mutableStateOf("")
    var addressLine by mutableStateOf("")

    var districts by mutableStateOf<List<DistrictDto>>(emptyList())
        private set
    var towns by mutableStateOf<List<TownDto>>(emptyList())
        private set

    var selectedDistrict by mutableStateOf<DistrictDto?>(null)
        private set
    var selectedTown by mutableStateOf<TownDto?>(null)

    var submitState by mutableStateOf<SubmitState>(SubmitState.Idle)
        private set

    var myListings by mutableStateOf<List<BoardingHouseResponseDto>>(emptyList())
        private set
    var listingsLoading by mutableStateOf(false)
        private set

    // Non-null while the owner is editing a declined listing to resubmit it
    var editingListingId by mutableStateOf<Long?>(null)
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
    }

    // Fill the form with a declined listing so the owner can fix it and resubmit
    fun startEdit(listing: BoardingHouseResponseDto) {
        editingListingId = listing.id
        title = listing.title
        description = listing.description
        rulesAndRegulations = listing.rulesAndRegulations
        price = if (listing.price % 1.0 == 0.0) listing.price.toLong().toString() else listing.price.toString()
        addressLine = listing.addressLine
        submitState = SubmitState.Idle

        val district = districts.find { it.id == listing.districtId }
        selectedDistrict = district
        selectedTown = null
        towns = emptyList()

        if (district != null) {
            viewModelScope.launch {
                try {
                    val response = RetrofitClient.apiService.getTowns(district.id)
                    if (response.isSuccessful) {
                        towns = response.body() ?: emptyList()
                        selectedTown = towns.find { it.id == listing.townId }
                    }
                } catch (_: Exception) {
                    // Owner can pick the town manually
                }
            }
        }
    }

    fun cancelEdit() {
        editingListingId = null
        clearForm()
        submitState = SubmitState.Idle
    }

    fun submitListing(ownerId: Long) {
        val priceValue = price.toDoubleOrNull()
        val town = selectedTown

        if (title.isBlank() || description.isBlank() || rulesAndRegulations.isBlank() ||
            addressLine.isBlank() || priceValue == null || town == null
        ) {
            submitState = SubmitState.Error("Please fill in all fields correctly")
            return
        }

        submitState = SubmitState.Loading
        val editingId = editingListingId

        viewModelScope.launch {
            try {
                val request = BoardingHouseRequestDto(
                    title = title,
                    description = description,
                    rulesAndRegulations = rulesAndRegulations,
                    price = priceValue,
                    addressLine = addressLine,
                    townId = town.id,
                    ownerId = ownerId
                )
                val response = if (editingId != null) {
                    RetrofitClient.apiService.resubmitBoardingHouse(editingId, request)
                } else {
                    RetrofitClient.apiService.submitBoardingHouseRequest(request)
                }

                if (response.isSuccessful) {
                    submitState = SubmitState.Success
                    editingListingId = null
                    clearForm()
                    loadMyListings(ownerId)
                } else {
                    submitState = SubmitState.Error("Failed to submit listing")
                }
            } catch (e: Exception) {
                submitState = SubmitState.Error("Could not connect to server: ${e.message}")
            }
        }
    }

    fun resetSubmitState() {
        submitState = SubmitState.Idle
    }

    private fun clearForm() {
        title = ""
        description = ""
        rulesAndRegulations = ""
        price = ""
        addressLine = ""
        selectedDistrict = null
        selectedTown = null
        towns = emptyList()
    }

    fun loadMyListings(ownerId: Long) {
        listingsLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getOwnerListings(ownerId)
                if (response.isSuccessful) {
                    myListings = response.body() ?: emptyList()
                }
            } catch (_: Exception) {
                // Silently fail for now
            } finally {
                listingsLoading = false
            }
        }
    }
}