package com.matrixmarket.app.ui.screens.createlisting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.R
import com.matrixmarket.app.ui.theme.LavenderLight
import com.matrixmarket.app.ui.theme.RoyalPurple
import com.matrixmarket.app.ui.theme.listingTypeColor
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.Resource
import com.matrixmarket.app.util.ViewModelFactory

// Matches the "Selling / Create new listing" mockup. Also the entry point for
// User Defined Feature 1 (ISBN Barcode Scanner): tapping "Scan Textbook ISBN"
// navigates to the camera scanner screen, which reports the code back here.
@Composable
fun CreateListingScreen(
    scannedIsbn: String?,
    onScanIsbnClick: () -> Unit,
    onListingCreated: () -> Unit
) {
    val viewModel: CreateListingViewModel = viewModel(
        factory = ViewModelFactory { CreateListingViewModel(AppContainer.listingRepository) }
    )
    val categories by viewModel.categories.collectAsState()
    val createState by viewModel.createState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var listingType by remember { mutableStateOf("Sell") }
    val typeColor = listingTypeColor(listingType)
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var isbn by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scannedIsbn) {
        if (!scannedIsbn.isNullOrBlank()) {
            isbn = scannedIsbn
            if (title.isBlank()) title = "Textbook (ISBN $scannedIsbn)"
        }
    }

    LaunchedEffect(createState) {
        if (createState is Resource.Success) {
            title = ""; description = ""; price = ""; isbn = null
            viewModel.resetState()
            onListingCreated()
        }
    }

    Surface(color = LavenderLight, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.selling), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.create_new_listing), color = androidx.compose.ui.graphics.Color.Gray)

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onScanIsbnClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isbn == null) stringResource(R.string.scan_isbn) else "ISBN Scanned: $isbn")
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Title") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Description") }, modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Price (R)") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))
            Text("Category", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.categoryId,
                        onClick = { selectedCategoryId = category.categoryId },
                        label = { Text(category.categoryName, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Listing Type", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Sell", "Trade", "Lend").forEach { type ->
                    val color = listingTypeColor(type)
                    FilterChip(
                        selected = listingType == type,
                        onClick = { listingType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = listingType == type,
                            borderColor = color,
                            selectedBorderColor = color
                        )
                    )
                }
            }

            if (createState is Resource.Error) {
                Spacer(Modifier.height(10.dp))
                Text((createState as Resource.Error).message, color = androidx.compose.ui.graphics.Color.Red, fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    selectedCategoryId?.let {
                        viewModel.createListing(it, title, description, price, listingType, isbn)
                    }
                },
                enabled = createState !is Resource.Loading && selectedCategoryId != null,
                colors = ButtonDefaults.buttonColors(containerColor = typeColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (createState is Resource.Loading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.publish_listing), color = androidx.compose.ui.graphics.Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}