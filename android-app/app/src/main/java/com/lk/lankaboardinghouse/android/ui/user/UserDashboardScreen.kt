package com.lk.lankaboardinghouse.android.ui.user

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    fullName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Search", "Favourites")

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Find a Boarding House",
                style = MaterialTheme.typography.headlineSmall
            )
            TextButton(onClick = onLogout) {
                Text("Logout")
            }
        }
        Text(
            text = "Welcome, $fullName",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> SearchTab(viewModel)
            1 -> FavouritesTab(viewModel)
        }
    }
}

// ─── Search tab ──────────────────────────────────────────────────────────

@Composable
private fun SearchTab(viewModel: UserViewModel) {
    var districtExpanded by remember { mutableStateOf(false) }
    var townExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
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

            // Filters appear once a town is chosen
            if (viewModel.selectedTown != null) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = viewModel.minPrice,
                        onValueChange = { viewModel.minPrice = it },
                        label = { Text("Min Rs.") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = viewModel.maxPrice,
                        onValueChange = { viewModel.maxPrice = it },
                        label = { Text("Max Rs.") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { sortExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(viewModel.sortOption.label)
                        }
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        viewModel.onSortSelected(option)
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { viewModel.search() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Apply")
                    }
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Clear")
                    }
                }
            }
        }

        when {
            viewModel.isSearching -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            viewModel.errorMessage != null -> {
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            viewModel.hasSearched && viewModel.searchResults.isEmpty() -> {
                Text(
                    text = "No boarding houses match your search.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            viewModel.searchResults.isNotEmpty() -> {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Text(
                            text = "${viewModel.searchResults.size} boarding house(s) found",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(viewModel.searchResults, key = { it.id }) { listing ->
                        SearchResultCard(
                            listing = listing,
                            isFavourite = listing.id in viewModel.favouriteIds,
                            isProcessing = viewModel.favouriteActionId == listing.id,
                            onToggleFavourite = { viewModel.toggleFavourite(listing) }
                        )
                    }
                }
            }
            else -> {
                Text(
                    text = "Select a district and town to search for boarding houses.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ─── Favourites tab ──────────────────────────────────────────────────────

@Composable
private fun FavouritesTab(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Your Favourites",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        viewModel.favouritesError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        when {
            viewModel.favouritesLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            viewModel.favourites.isEmpty() -> {
                Text(
                    text = "You haven't saved any boarding houses yet. Tap the heart on a listing to save it.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(viewModel.favourites, key = { it.id }) { listing ->
                        SearchResultCard(
                            listing = listing,
                            isFavourite = true,
                            isProcessing = viewModel.favouriteActionId == listing.id,
                            onToggleFavourite = { viewModel.toggleFavourite(listing) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Shared card ─────────────────────────────────────────────────────────

@Composable
private fun SearchResultCard(
    listing: BoardingHouseResponseDto,
    isFavourite: Boolean,
    isProcessing: Boolean,
    onToggleFavourite: () -> Unit
) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                } else {
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            imageVector = if (isFavourite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (isFavourite) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Text(text = listing.description, style = MaterialTheme.typography.bodyMedium)

            Text(
                text = "${listing.townName}, ${listing.districtName}",
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(text = listing.addressLine)
            Text(text = "Rs. ${listing.price.toInt()} / month")

            Text(
                text = "Rules: ${listing.rulesAndRegulations}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(
                    onClick = { dialOwner(context, listing.ownerPhone) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Call")
                }
                OutlinedButton(
                    onClick = { whatsAppOwner(context, listing.ownerPhone) },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("WhatsApp")
                }
            }
        }
    }
}

private fun dialOwner(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = "tel:$phone".toUri()
    }
    context.startActivity(intent)
}

private fun whatsAppOwner(context: Context, rawPhone: String) {
    val phone = rawPhone.replace(" ", "").replace("-", "")
    val waPhone = if (phone.startsWith("0")) "94${phone.substring(1)}" else phone
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://wa.me/$waPhone".toUri()
    }
    context.startActivity(intent)
}