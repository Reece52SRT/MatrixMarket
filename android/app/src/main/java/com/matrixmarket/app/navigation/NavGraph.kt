package com.matrixmarket.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.matrixmarket.app.data.local.SessionManager
import com.matrixmarket.app.ui.screens.createlisting.CreateListingScreen
import com.matrixmarket.app.ui.screens.home.HomeScreen
import com.matrixmarket.app.ui.screens.listingdetail.ListingDetailScreen
import com.matrixmarket.app.ui.screens.login.LoginScreen
import com.matrixmarket.app.ui.screens.login.RegisterScreen
import com.matrixmarket.app.ui.screens.onboarding.OnboardingScreen
import com.matrixmarket.app.ui.screens.profile.ProfileScreen
import com.matrixmarket.app.ui.screens.scanner.BarcodeScannerScreen
import com.matrixmarket.app.ui.screens.settings.SettingsScreen
import com.matrixmarket.app.ui.screens.trademeeting.TradeMeetingScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main" // hosts the bottom-nav'd screens
    const val SCANNER = "scanner"
}

// Top-level graph: decides whether to show onboarding/auth or drop straight into the
// app if a session token already exists (checked once via SessionManager on launch).
@Composable
fun MatrixMarketNavGraph(sessionManager: SessionManager) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (sessionManager.isLoggedIn()) Routes.MAIN else Routes.ONBOARDING
    }

    if (startDestination == null) return // brief splash-less wait while session check resolves

    NavHost(navController = navController, startDestination = startDestination!!) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onContinueWithEmail = { navController.navigate(Routes.LOGIN) },
                onContinueWithGoogle = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(onLoggedOut = {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            })
        }
    }
}

private object MainRoutes {
    const val LISTINGS = "listings"
    const val CREATE_LISTING = "create_listing"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val TRADE_MEETING = "trade_meeting/{offerId}"
    fun tradeMeeting(offerId: Int) = "trade_meeting/$offerId"
    const val SCANNER = "scanner"
    const val LISTING_DETAIL = "listing_detail/{listingId}"
    fun listingDetail(listingId: Int) = "listing_detail/$listingId"
}

// Hosts the 5-tab bottom navigation experience (Profile, Trade Meeting, Sell,
// Listings, Settings) once the user is authenticated.
@Composable
private fun MainScreen(onLoggedOut: () -> Unit) {
    val innerNavController = rememberNavController()
    val backStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    androidx.compose.material3.Scaffold(
        bottomBar = {
            com.matrixmarket.app.ui.components.MatrixBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == com.matrixmarket.app.ui.components.BottomNavItem.CreateListing.route) {
                        innerNavController.navigate(MainRoutes.CREATE_LISTING)
                    } else if (route == com.matrixmarket.app.ui.components.BottomNavItem.Listings.route) {
                        innerNavController.navigate(MainRoutes.LISTINGS) { launchSingleTop = true }
                    } else if (route == com.matrixmarket.app.ui.components.BottomNavItem.Profile.route) {
                        innerNavController.navigate(MainRoutes.PROFILE) { launchSingleTop = true }
                    } else if (route == com.matrixmarket.app.ui.components.BottomNavItem.Settings.route) {
                        innerNavController.navigate(MainRoutes.SETTINGS) { launchSingleTop = true }
                    } else if (route == com.matrixmarket.app.ui.components.BottomNavItem.TradeMeeting.route) {
                        innerNavController.navigate(MainRoutes.tradeMeeting(0)) { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = MainRoutes.LISTINGS,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(MainRoutes.LISTINGS) {
                HomeScreen(onListingClick = { listing ->
                    innerNavController.navigate(MainRoutes.listingDetail(listing.listingId))
                })
            }
            composable(
                MainRoutes.LISTING_DETAIL,
                arguments = listOf(navArgument("listingId") { type = NavType.IntType })
            ) { entry ->
                val listingId = entry.arguments?.getInt("listingId") ?: 0
                ListingDetailScreen(
                    listingId = listingId,
                    onBack = { innerNavController.popBackStack() },
                    onOfferAccepted = { offerId ->
                        innerNavController.navigate(MainRoutes.tradeMeeting(offerId)) {
                            popUpTo(MainRoutes.LISTINGS)
                        }
                    }
                )
            }
            composable(MainRoutes.CREATE_LISTING) { entry ->
                val scannedIsbnState = entry.savedStateHandle
                    .getStateFlow<String?>("scanned_isbn", null)
                    .collectAsState()
                CreateListingScreen(
                    scannedIsbn = scannedIsbnState.value,
                    onScanIsbnClick = { innerNavController.navigate(MainRoutes.SCANNER) },
                    onListingCreated = { innerNavController.navigate(MainRoutes.LISTINGS) { launchSingleTop = true } }
                )
            }
            composable(MainRoutes.SCANNER) {
                BarcodeScannerScreen(
                    onIsbnDetected = { isbn ->
                        innerNavController.previousBackStackEntry
                            ?.savedStateHandle?.set("scanned_isbn", isbn)
                        innerNavController.popBackStack()
                    },
                    onBack = { innerNavController.popBackStack() }
                )
            }
            composable(MainRoutes.PROFILE) {
                ProfileScreen()
            }
            composable(MainRoutes.SETTINGS) {
                SettingsScreen(onLoggedOut = onLoggedOut)
            }
            composable(
                MainRoutes.TRADE_MEETING,
                arguments = listOf(navArgument("offerId") { type = NavType.IntType })
            ) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getInt("offerId") ?: 0
                TradeMeetingScreen(offerId = offerId)
            }
        }
    }
}
