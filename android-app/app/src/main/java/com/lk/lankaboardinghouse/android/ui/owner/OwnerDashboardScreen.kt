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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lk.lankaboardinghouse.android.R
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto

@OptIn(ExperimentalMaterial3Api::class)
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

    val tabMyListings = stringResource(R.string.my_listings)
    val tabEdit = stringResource(R.string.edit_listing)
    val tabAdd = stringResource(R.string.add_new)
    val tabs = listOf(tabMyListings, if (isEditing) tabEdit else tabAdd)

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
                text = stringResource(R.string.welcome_user, fullName),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.logout))
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedTab) {
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
    when {
        viewModel.listingsLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
        viewModel.myListings.isEmpty() -> {
            Text(
                text = stringResource(R.string.no_listings),
                modifier = Modifier.padding(16.dp)
            )
        }
        else -> {
            Column(modifier = Modifier.fillMaxSize()) {
                viewModel.actionError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )
                }

                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(viewModel.myListings) { listing ->
                        ListingCard(
                            listing = listing,
                            isProcessing = viewModel.actionInProgressId == listing.id,
                            onEdit = onEdit,
                            onToggleAvailability = { viewModel.toggleAvailability(it) },
                            onDelete = { viewModel.deleteListing(it) }
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
    onDelete: (Long) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_listing_title)) },
            text = { Text(stringResource(R.string.delete_listing_message, listing.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(listing.id)
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = listing.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "${listing.townName}, ${listing.districtName}")
            Text(text = stringResource(R.string.price_format, listing.price.toInt()))
            Text(
                text = stringResource(R.string.status_format, listing.status),
                style = MaterialTheme.typography.labelLarge
            )

            if (listing.status == "APPROVED") {
                Text(
                    text = if (listing.available) stringResource(R.string.visible_search) else stringResource(R.string.marked_full),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (listing.available) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            if (listing.status == "DECLINED") {
                Text(
                    text = stringResource(R.string.reason_format, listing.declineReason ?: stringResource(R.string.no_reason)),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            } else {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (listing.status == "APPROVED") {
                        OutlinedButton(onClick = { onToggleAvailability(listing) }) {
                            Text(if (listing.available) stringResource(R.string.mark_full) else stringResource(R.string.mark_available))
                        }
                    }
                    if (listing.status == "DECLINED") {
                        Button(onClick = { onEdit(listing) }) {
                            Text(stringResource(R.string.edit_resubmit))
                        }
                    }
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
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

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            if (isEditing) {
                Text(
                    text = stringResource(R.string.fix_issues_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.rulesAndRegulations,
                onValueChange = { viewModel.rulesAndRegulations = it },
                label = { Text(stringResource(R.string.rules)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.price,
                onValueChange = { viewModel.price = it },
                label = { Text(stringResource(R.string.price_month)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.addressLine,
                onValueChange = { viewModel.addressLine = it },
                label = { Text(stringResource(R.string.address)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(
                    onClick = { districtExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(viewModel.selectedDistrict?.name ?: stringResource(R.string.select_district))
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
                    Text(viewModel.selectedTown?.name ?: stringResource(R.string.select_town))
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
                    text = stringResource(R.string.submit_success),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                else -> {}
            }

            Button(
                onClick = { viewModel.submitListing(ownerId) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(if (isEditing) stringResource(R.string.resubmit_approval) else stringResource(R.string.submit_approval))
            }

            if (isEditing) {
                TextButton(
                    onClick = onCancelEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}