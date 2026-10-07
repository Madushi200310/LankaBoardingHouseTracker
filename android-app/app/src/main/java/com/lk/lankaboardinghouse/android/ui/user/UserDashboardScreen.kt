package com.lk.lankaboardinghouse.android.ui.user

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto

@Composable
fun UserDashboardScreen(
    fullName: String,
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel()
) {
    var districtExpanded by remember { mutableStateOf(false) }
    var townExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Find a Boarding House",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Welcome, $fullName",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

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
                    text = "No boarding houses found in this town.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            viewModel.searchResults.isNotEmpty() -> {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(viewModel.searchResults) { listing ->
                        SearchResultCard(listing)
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

@Composable
private fun SearchResultCard(listing: BoardingHouseResponseDto) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = listing.title, style = MaterialTheme.typography.titleMedium)
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
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${listing.ownerPhone}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Call")
                }
                OutlinedButton(
                    onClick = {
                        val phone = listing.ownerPhone.replace(" ", "").replace("-", "")
                        val waPhone = if (phone.startsWith("0")) "94${phone.substring(1)}" else phone
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/$waPhone")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("WhatsApp")
                }
            }
        }
    }
}