package com.matrixmarket.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.R
import com.matrixmarket.app.data.remote.dto.ListingResponse
import com.matrixmarket.app.ui.theme.LavenderCard
import com.matrixmarket.app.ui.theme.LavenderLight
import com.matrixmarket.app.ui.theme.RoyalPurple
import com.matrixmarket.app.ui.theme.listingTypeColor
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.Resource
import com.matrixmarket.app.util.ViewModelFactory

// Matches the "Current Listings" mockup: light lavender theme, grouped by category,
// with search and filter chips at the top. This is the primary REST API integration
// screen - it fetches live listings from GET /api/v1/listings.
@Composable
fun HomeScreen(onListingClick: (ListingResponse) -> Unit) {
    val viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory { HomeViewModel(AppContainer.listingRepository) }
    )
    val listingsState by viewModel.listingsState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Surface(color = LavenderLight, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(stringResource(R.string.current_listings), fontSize = MaterialTheme.typography.headlineMedium.fontSize, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text("All") }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.categoryId,
                        onClick = { viewModel.onCategorySelected(category.categoryId) },
                        label = { Text(category.categoryName) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            when (val state = listingsState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RoyalPurple)
                }
                is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Couldn't load listings.")
                        Text(state.message, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadListings() }) { Text("Retry") }
                    }
                }
                is Resource.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No listings yet. Be the first to sell something!")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.data) { listing ->
                                ListingCard(listing = listing, onClick = { onListingClick(listing) })
                            }
                        }
                    }
                }
                Resource.Idle -> Unit
            }
        }
    }
}

@Composable
private fun ListingCard(listing: ListingResponse, onClick: () -> Unit) {
    val typeColor = listingTypeColor(listing.listingType)

    Surface(
        color = LavenderCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = typeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(listing.categoryName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = typeColor)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f).clickable(onClick = onClick)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(listing.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    Surface(color = typeColor, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            listing.listingType,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("R ${"%.2f".format(listing.price)} · ${listing.categoryName}", fontSize = 13.sp)
            }
        }
    }
}