package com.lk.lankaboardinghouse.android.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto

@Composable
fun OwnerDashboardScreen(
    ownerId: Long,
    fullName: String,
    onLogout: () -> Unit,
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
            Text(
                text = "Welcome, $fullName",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onLogout) {
                Text("Logout")
            }
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
                }
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
    onEdit: (BoardingHouseResponseDto) -> Unit
) {
    if (viewModel.listingsLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        return
    }

    if (viewModel.myListings.isEmpty()) {
        Text(
            text = "You haven't submitted any boarding houses yet.",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(viewModel.myListings) { listing ->
            ListingCard(listing, onEdit)
        }
    }
}

@Composable
private fun ListingCard(
    listing: BoardingHouseResponseDto,
    onEdit: (BoardingHouseResponseDto) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = listing.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "${listing.townName}, ${listing.districtName}")
            Text(text = "Rs. ${listing.price.toInt()} / month")
            Text(
                text = "Status: ${listing.status}",
                style = MaterialTheme.typography.labelLarge
            )

            if (listing.status == "DECLINED") {
                Text(
                    text = "Reason: ${listing.declineReason ?: "No reason given"}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = { onEdit(listing) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Edit & Resubmit")
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

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            if (isEditing) {
                Text(
                    text = "Fix the issues the admin raised, then resubmit for approval.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.rulesAndRegulations,
                onValueChange = { viewModel.rulesAndRegulations = it },
                label = { Text("Rules and Regulations") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.price,
                onValueChange = { viewModel.price = it },
                label = { Text("Price (Rs. per month)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.addressLine,
                onValueChange = { viewModel.addressLine = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(
                    onClick = { districtExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(viewModel.selectedDistrict?.name ?: "Select District")
                }
                DropdownMenu(
                    expanded = districtExpanded,
                    onDismissRequest = { districtExpanded = false }
                ) {
                    viewModel.districts.forEach { district ->
                        DropdownMenuItem(
                            text = { Text(district.name) },
                            onClick = {
                                viewModel.onDistrictSelected(district)
                                districtExpanded = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(
                    onClick = { if (viewModel.towns.isNotEmpty()) townExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(viewModel.selectedTown?.name ?: "Select Town")
                }
                DropdownMenu(
                    expanded = townExpanded,
                    onDismissRequest = { townExpanded = false }
                ) {
                    viewModel.towns.forEach { town ->
                        DropdownMenuItem(
                            text = { Text(town.name) },
                            onClick = {
                                viewModel.onTownSelected(town)
                                townExpanded = false
                            }
                        )
                    }
                }
            }

            when (val state = viewModel.submitState) {
                is SubmitState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                is SubmitState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                is SubmitState.Success -> Text(
                    text = "Submitted! Waiting for admin approval.",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                else -> {}
            }

            Button(
                onClick = { viewModel.submitListing(ownerId) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(if (isEditing) "Resubmit for Approval" else "Submit for Approval")
            }

            if (isEditing) {
                TextButton(
                    onClick = onCancelEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}