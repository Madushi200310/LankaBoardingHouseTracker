package com.lk.lankaboardinghouse.android.ui.admin

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lk.lankaboardinghouse.android.data.model.AdminUserDto
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    fullName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    listingsViewModel: AdminViewModel = viewModel(),
    usersViewModel: AdminUsersViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Pending Listings", "Users")

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Admin Dashboard",
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
            0 -> PendingListingsTab(listingsViewModel)
            1 -> UsersTab(usersViewModel)
        }
    }
}

@Composable
private fun PendingListingsTab(viewModel: AdminViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Pending Boarding House Requests",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        when {
            viewModel.isLoading -> {
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
            viewModel.pendingListings.isEmpty() -> {
                Text(
                    text = "No pending requests right now.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(viewModel.pendingListings) { listing ->
                        PendingListingCard(
                            listing = listing,
                            isProcessing = viewModel.actionInProgressId == listing.id,
                            onApprove = { viewModel.approve(listing.id) },
                            onDecline = { reason -> viewModel.decline(listing.id, reason) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersTab(viewModel: AdminUsersViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "All Users",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        viewModel.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        when {
            viewModel.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            viewModel.users.isEmpty() -> {
                Text(
                    text = "No users found.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(viewModel.users, key = { it.id }) { user ->
                        UserCard(
                            user = user,
                            isProcessing = viewModel.actionInProgressId == user.id,
                            onToggleDisabled = { disabled ->
                                viewModel.setDisabled(user, disabled)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: AdminUserDto,
    isProcessing: Boolean,
    onToggleDisabled: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.role,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
            Text(text = user.phoneNumber, style = MaterialTheme.typography.bodyMedium)

            Text(
                text = if (user.disabled) "Status: DISABLED" else "Status: Active",
                style = MaterialTheme.typography.labelLarge,
                color = if (user.disabled) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.padding(top = 8.dp)
            )

            // Admin rows never show the toggle (backend also refuses to disable admins).
            if (user.role != "ADMIN") {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                } else {
                    Row(modifier = Modifier.padding(top = 12.dp)) {
                        if (user.disabled) {
                            Button(onClick = { onToggleDisabled(false) }) {
                                Text("Enable Account")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onToggleDisabled(true) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Disable Account")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingListingCard(
    listing: BoardingHouseResponseDto,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onDecline: (String) -> Unit
) {
    var showDeclineDialog by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text("Decline listing") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (the owner will see this)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = reason.isNotBlank(),
                    onClick = {
                        onDecline(reason.trim())
                        showDeclineDialog = false
                        reason = ""
                    }
                ) {
                    Text("Decline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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

            Text(
                text = "Owner: ${listing.ownerName} (${listing.ownerPhone})",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else {
                Row(modifier = Modifier.padding(top = 16.dp)) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = { showDeclineDialog = true },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Decline")
                    }
                }
            }
        }
    }
}