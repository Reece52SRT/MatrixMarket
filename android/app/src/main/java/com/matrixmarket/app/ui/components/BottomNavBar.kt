package com.matrixmarket.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.matrixmarket.app.R
import com.matrixmarket.app.ui.theme.RoyalPurple

// Mirrors the 5-tab layout from the Part 1 mockups: Profile, Trade Meeting,
// Create Listing (center action), Current Listings, Settings.
// Labels are string resources (not literals) so the Settings > Language switcher
// actually changes what's shown here.
enum class BottomNavItem(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    Profile("profile", R.string.nav_profile, Icons.Filled.Person),
    TradeMeeting("trade_meeting", R.string.nav_meet, Icons.Filled.Event),
    CreateListing("create_listing", R.string.nav_sell, Icons.Filled.AddCircle),
    Listings("listings", R.string.nav_listings, Icons.Filled.Folder),
    Settings("settings", R.string.nav_settings, Icons.Filled.Settings)
}

@Composable
fun MatrixBottomNavBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
        BottomNavItem.values().forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalPurple,
                    selectedTextColor = RoyalPurple,
                    indicatorColor = RoyalPurple.copy(alpha = 0.12f)
                )
            )
        }
    }
}
