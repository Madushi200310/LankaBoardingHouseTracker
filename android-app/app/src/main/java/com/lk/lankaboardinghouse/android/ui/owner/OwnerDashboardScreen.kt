package com.lk.lankaboardinghouse.android.ui.owner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto

@Composable
fun OwnerDashboardScreen(
    ownerId: Long,
    fullName: String,
    onLogout: () -> Unit,
    onViewDetails: (BoardingHouseResponseDto) -> Unit, // Added for Detail Screen
    modifier: Modifier = Modifier,
    viewModel: OwnerViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isEditing = viewModel.editingListingId != null
    val tabs = listOf("My Listings", if (isEditing) "Edit Listing" else "Add New")

    LaunchedEffect(Unit) {
        viewModel.loadMyListings(ownerId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Welcome, $fullName", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onLogout) { Text("Logout") }
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> MyListingsTab(
                viewModel = viewModel,
                onEdit = { listing ->
                    viewModel.startEdit(listing)
                    selectedTab = 1
                },
                onViewDetails = onViewDetails
            )
            1 -> AddNewListingTab(
                ownerId = ownerId,
                viewModel = viewModel,
                onCancelEdit = {
                    viewModel.cancelEdit()
                    selectedTab = 0
                }
            )
        }
    }
}

@Composable
private fun MyListingsTab(
    viewModel: OwnerViewModel,
    onEdit: (BoardingHouseResponseDto) -> Unit,
    onViewDetails: (BoardingHouseResponseDto) -> Unit
) {
    when {
        viewModel.listingsLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        viewModel.listingsError != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(viewModel.listingsError!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.loadMyListings(1) }) { // Pass ownerId dynamically if possible
                    Text("Retry")
                }
            }
        }
        viewModel.myListings.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't submitted any boarding houses yet.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        else -> {
            Column(modifier = Modifier.fillMaxSize()) {
                viewModel.actionError?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }

                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(viewModel.myListings) { listing ->
                        ListingCard(
                            listing = listing,
                            isProcessing = viewModel.actionInProgressId == listing.id,
                            onEdit = onEdit,
                            onToggleAvailability = { viewModel.toggleAvailability(it) },
                            onDelete = { viewModel.deleteListing(it) },
                            onViewDetails = onViewDetails
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingCard(
    listing: BoardingHouseResponseDto,
    isProcessing: Boolean,
    onEdit: (BoardingHouseResponseDto) -> Unit,
    onToggleAvailability: (BoardingHouseResponseDto) -> Unit,
    onDelete: (Long) -> Unit,
    onViewDetails: (BoardingHouseResponseDto) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete listing?") },
            text = { Text("\"${listing.title}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete(listing.id) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        onClick = { onViewDetails(listing) }
    ) {
        if (listing.photoUrls.isNotEmpty()) {
            AsyncImage(
                model = listing.photoUrls.first(),
                contentDescription = listing.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = listing.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "${listing.townName}, ${listing.districtName}")
            Text(text = "Rs. ${listing.price.toInt()} / month")
            Text(text = "Status: ${listing.status}", style = MaterialTheme.typography.labelLarge)

            if (listing.status == "APPROVED") {
                Text(
                    text = if (listing.available) "Visible in search" else "Marked as FULL (hidden)",
                    color = if (listing.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            if (listing.status == "DECLINED") {
                Text(
                    text = "Reason: ${listing.declineReason ?: "No reason given"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            } else {
                Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (listing.status == "APPROVED") {
                        OutlinedButton(onClick = { onToggleAvailability(listing) }) {
                            Text(if (listing.available) "Mark as Full" else "Mark as Available")
                        }
                    }
                    if (listing.status == "DECLINED" || listing.status == "APPROVED") {
                        Button(onClick = { onEdit(listing) }, modifier = Modifier.padding(start = 8.dp)) {
                            Text(if (listing.status == "DECLINED") "Edit & Resubmit" else "Edit")
                        }
                    }
                    TextButton(onClick = { showDeleteDialog = true }, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddNewListingTab(
    ownerId: Long,
    viewModel: OwnerViewModel,
    onCancelEdit: () -> Unit
) {
    var districtExpanded by remember { mutableStateOf(false) }
    var townExpanded by remember { mutableStateOf(false) }
    val isEditing = viewModel.editingListingId != null
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        uris.forEach { viewModel.addPhotoUri(it) }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            if (isEditing) {
                Text("Edit your listing. Saving approved listings will send them for re-approval.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            }

            OutlinedTextField(value = viewModel.title, onValueChange = { viewModel.title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.description, onValueChange = { viewModel.description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            OutlinedTextField(value = viewModel.rulesAndRegulations, onValueChange = { viewModel.rulesAndRegulations = it }, label = { Text("Rules") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            OutlinedTextField(value = viewModel.price, onValueChange = { viewModel.price = it }, label = { Text("Price (Rs.)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            OutlinedTextField(value = viewModel.addressLine, onValueChange = { viewModel.addressLine = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

            // District & Town dropdowns...
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(onClick = { districtExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(viewModel.selectedDistrict?.name ?: "Select District")
                }
                DropdownMenu(expanded = districtExpanded, onDismissRequest = { districtExpanded = false }) {
                    viewModel.districts.forEach { district ->
                        DropdownMenuItem(text = { Text(district.name) }, onClick = { viewModel.onDistrictSelected(district); districtExpanded = false })
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(onClick = { if (viewModel.towns.isNotEmpty()) townExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(viewModel.selectedTown?.name ?: "Select Town")
                }
                DropdownMenu(expanded = townExpanded, onDismissRequest = { townExpanded = false }) {
                    viewModel.towns.forEach { town ->
                        DropdownMenuItem(text = { Text(town.name) }, onClick = { viewModel.onTownSelected(town); townExpanded = false })
                    }
                }
            }

            // Photo Picker
            Button(
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Add Photos (Max 5)")
            }

            // Photo Preview
            if (viewModel.selectedPhotoUris.isNotEmpty()) {
                LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                    items(viewModel.selectedPhotoUris) { uri ->
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                            )
                            TextButton(
                                onClick = { viewModel.removePhotoUri(uri) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text("X", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            when (val state = viewModel.submitState) {
                is SubmitState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                is SubmitState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                is SubmitState.Success -> Text(text = "Submitted successfully!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                else -> {}
            }

            Button(
                onClick = { viewModel.submitListing(ownerId, context) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(if (isEditing) "Save Changes" else "Submit for Approval")
            }

            if (isEditing) {
                TextButton(onClick = onCancelEdit, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}