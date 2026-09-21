package com.matrixmarket.app.ui.screens.listingdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.ui.theme.LavenderCard
import com.matrixmarket.app.ui.theme.LavenderLight
import com.matrixmarket.app.ui.theme.RoyalPurple
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.Resource
import com.matrixmarket.app.util.ViewModelFactory

// Shows a single listing's full detail and lets the buyer make an offer.
// Once the offer is created, we hand the resulting offerId to the caller,
// which the nav graph uses to route straight into the Trade Meeting screen -
// closing the loop from "browse" to "safely arrange the exchange".
@Composable
fun ListingDetailScreen(
    listingId: Int,
    onBack: () -> Unit,
    onOfferAccepted: (offerId: Int) -> Unit
) {
    val viewModel: ListingDetailViewModel = viewModel(
        factory = ViewModelFactory { ListingDetailViewModel(AppContainer.listingRepository, AppContainer.tradeRepository) }
    )
    val listingState by viewModel.listingState.collectAsState()
    val offerState by viewModel.offerState.collectAsState()
    var offerAmount by remember { mutableStateOf("") }

    LaunchedEffect(listingId) { viewModel.loadListing(listingId) }

    LaunchedEffect(offerState) {
        if (offerState is Resource.Success) {
            onOfferAccepted((offerState as Resource.Success).data)
        }
    }

    Surface(color = LavenderLight, modifier = Modifier.fillMaxSize()) {
        when (val state = listingState) {
            is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RoyalPurple)
            }
            is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadListing(listingId) }) { Text("Retry") }
                }
            }
            is Resource.Success -> {
                val listing = state.data
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = RoyalPurple.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(listing.categoryName, color = RoyalPurple, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(listing.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("R ${"%.2f".format(listing.price)} · ${listing.listingType}", color = RoyalPurple, fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).background(LavenderCard, CircleShape), contentAlignment = Alignment.Center) {
                            Text(listing.sellerName.take(1).uppercase(), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(listing.sellerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${listing.sellerKarma} Student Score", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(listing.description, fontSize = 14.sp)

                    listing.isbn?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("ISBN: $it", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(Modifier.weight(1f))

                    if (listing.status != "Active") {
                        Surface(color = Color(0xFFFFE0E0), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("This listing is no longer available (${listing.status}).", modifier = Modifier.padding(14.dp))
                        }
                    } else {
                        OutlinedTextField(
                            value = offerAmount,
                            onValueChange = { offerAmount = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Your offer (R)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (offerState is Resource.Error) {
                            Spacer(Modifier.height(6.dp))
                            Text((offerState as Resource.Error).message, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.makeOffer(listingId, offerAmount.toDoubleOrNull() ?: 0.0) },
                            enabled = offerState !is Resource.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (offerState is Resource.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Make Offer & Schedule Meeting", color = Color.White)
                            }
                        }
                    }
                }
            }
            Resource.Idle -> Unit
        }
    }
}
