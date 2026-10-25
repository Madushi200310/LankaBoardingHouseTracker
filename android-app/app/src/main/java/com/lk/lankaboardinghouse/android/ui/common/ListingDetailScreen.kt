package com.lk.lankaboardinghouse.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listing: BoardingHouseResponseDto,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Photo Gallery
            if (listing.photoUrls.isNotEmpty()) {
                val pagerState = rememberPagerState { listing.photoUrls.size }

                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = listing.photoUrls[page],
                            contentDescription = "Listing Photo ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Page Indicator
                    Row(
                        Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(listing.photoUrls.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp)
                            )
                        }
                    }
                }
            } else {
                // Placeholder if no photos
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No photos available")
                }
            }

            // Details
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = listing.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rs. ${listing.price.toInt()} / month",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(text = "${listing.addressLine}, ${listing.townName}, ${listing.districtName}", style = MaterialTheme.typography.bodyLarge)

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(text = "Description", style = MaterialTheme.typography.titleMedium)
                Text(text = listing.description, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Rules and Regulations", style = MaterialTheme.typography.titleMedium)
                Text(text = listing.rulesAndRegulations, style = MaterialTheme.typography.bodyLarge)

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(text = "Owner Contact", style = MaterialTheme.typography.titleMedium)
                Text(text = listing.ownerName, style = MaterialTheme.typography.bodyLarge)
                Text(text = listing.ownerPhone, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}