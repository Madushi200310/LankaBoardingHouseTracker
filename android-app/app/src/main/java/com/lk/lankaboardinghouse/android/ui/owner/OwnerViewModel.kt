package com.lk.lankaboardinghouse.android.ui.owner

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lk.lankaboardinghouse.android.data.model.*
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    // NEW: Selected Photos
    var selectedPhotoUris by mutableStateOf<List<Uri>>(emptyList())

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
    var listingsError by mutableStateOf<String?>(null)
        private set

    var editingListingId by mutableStateOf<Long?>(null)
        private set

    var actionInProgressId by mutableStateOf<Long?>(null)
        private set
    var actionError by mutableStateOf<String?>(null)
        private set

    init {
        loadDistricts()
    }

    fun addPhotoUri(uri: Uri) {
        if (selectedPhotoUris.size < 5) {
            selectedPhotoUris = selectedPhotoUris + uri
        }
    }

    fun removePhotoUri(uri: Uri) {
        selectedPhotoUris = selectedPhotoUris - uri
    }

    private fun loadDistricts() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getDistricts()
                if (response.isSuccessful) {
                    districts = response.body() ?: emptyList()
                }
            } catch (_: Exception) {}
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
            } catch (_: Exception) {}
        }
    }

    fun onTownSelected(town: TownDto) {
        selectedTown = town
    }

    fun startEdit(listing: BoardingHouseResponseDto) {
        editingListingId = listing.id
        title = listing.title
        description = listing.description
        rulesAndRegulations = listing.rulesAndRegulations
        price = if (listing.price % 1.0 == 0.0) listing.price.toLong().toString() else listing.price.toString()
        addressLine = listing.addressLine
        submitState = SubmitState.Idle
        selectedPhotoUris = emptyList()

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
                } catch (_: Exception) {}
            }
        }
    }

    fun cancelEdit() {
        editingListingId = null
        clearForm()
        submitState = SubmitState.Idle
    }

    fun submitListing(ownerId: Long, context: Context) {
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
                    val savedListing = response.body()
                    if (savedListing != null && selectedPhotoUris.isNotEmpty()) {
                        // Upload photos
                        val parts = selectedPhotoUris.mapNotNull { uri ->
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bytes = inputStream?.readBytes()
                            inputStream?.close()
                            if (bytes != null) {
                                val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                                MultipartBody.Part.createFormData("files", "photo.jpg", reqFile)
                            } else null
                        }
                        RetrofitClient.apiService.uploadPhotos(savedListing.id, parts)
                    }

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
        selectedPhotoUris = emptyList()
    }

    fun loadMyListings(ownerId: Long) {
        listingsLoading = true
        listingsError = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getOwnerListings(ownerId)
                if (response.isSuccessful) {
                    myListings = response.body() ?: emptyList()
                } else {
                    listingsError = "Failed to load listings"
                }
            } catch (e: Exception) {
                listingsError = "Could not connect to server: ${e.message}"
            } finally {
                listingsLoading = false
            }
        }
    }

    fun toggleAvailability(listing: BoardingHouseResponseDto) {
        actionInProgressId = listing.id
        actionError = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.setAvailability(
                    listing.id,
                    AvailabilityRequest(!listing.available)
                )
                val updated = response.body()
                if (response.isSuccessful && updated != null) {
                    myListings = myListings.map { if (it.id == updated.id) updated else it }
                } else {
                    actionError = "Could not update the listing"
                }
            } catch (e: Exception) {
                actionError = "Could not connect to server: ${e.message}"
            } finally {
                actionInProgressId = null
            }
        }
    }

    fun deleteListing(id: Long) {
        actionInProgressId = id
        actionError = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteBoardingHouse(id)
                if (response.isSuccessful) {
                    myListings = myListings.filterNot { it.id == id }
                } else {
                    actionError = "Could not delete the listing"
                }
            } catch (e: Exception) {
                actionError = "Could not connect to server: ${e.message}"
            } finally {
                actionInProgressId = null
            }
        }
    }
}