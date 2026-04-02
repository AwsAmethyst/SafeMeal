package com.example.safemeal.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.safemeal.R
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.Manrope
import com.example.safemeal.ui.theme.SafeMealTheme
import com.example.safemeal.ui.theme.WhiteMain
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.safemeal.data.restaurant.RestaurantViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Nairobi CBD as a safe fallback if GPS is unavailable
private const val FALLBACK_LAT = -1.286389
private const val FALLBACK_LNG = 36.817223

// Fetches a fresh GPS fix using FusedLocationProviderClient.
// Falls back to lastLocation if the fresh fix returns null.
// Returns null only if both fail (e.g. permissions not granted).
@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(context: android.content.Context): Pair<Double, Double>? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()

    return suspendCancellableCoroutine { continuation ->
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(Pair(location.latitude, location.longitude))
                } else {
                    // Fresh fix was null — try last known location
                    client.lastLocation
                        .addOnSuccessListener { last ->
                            continuation.resume(
                                if (last != null) Pair(last.latitude, last.longitude) else null
                            )
                        }
                        .addOnFailureListener { continuation.resume(null) }
                }
            }
            .addOnFailureListener { continuation.resume(null) }

        continuation.invokeOnCancellation { cts.cancel() }
    }
}

sealed class Screen(val route: String, val label: String, val icon: Int) {
    object Profile : Screen("profile", "Profile", R.drawable.profile)
    object Discover : Screen("discover", "Discover", R.drawable.explore)
    object Restaurants : Screen("restaurants", "Restaurants", R.drawable.restaurant)
    object Dashboard : Screen("dashboard", "Dashboard", R.drawable.dashboard)
}

@Composable
fun HomePage(mainNavController: NavController) {
    val viewModel: RestaurantViewModel = viewModel()
    val bottomNavController = rememberNavController()
    val items = listOf(Screen.Profile, Screen.Dashboard, Screen.Discover)
    var finalStartDestination by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var userIsAdmin by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        try {
            val user = com.example.safemeal.AppwriteManger.AppwriteManager.account.get()
            userIsAdmin = user.labels.contains("admin")
            // If the user has the "admin" label, set their home to the Admin Dashboard
        } catch (e: Exception) {
            // Fallback to standard dashboard if check fails
            userIsAdmin = false
        }
    }

    if (userIsAdmin == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenMain)
        }
    } else {
        // CHANGE: Determine the dynamic start route for THIS user
        val startRoute = if (userIsAdmin!!) "admin_dashboard" else Screen.Dashboard.route
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = WhiteMain,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val isDashboardItem = item is Screen.Dashboard

                    val isSelected = if (isDashboardItem && userIsAdmin == true) {
                        currentRoute == "admin_dashboard"
                    } else {
                        currentRoute == item.route
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            val targetRoute = if (item == Screen.Dashboard && userIsAdmin == true) {
                                "admin_dashboard"
                            } else {
                                item.route
                            }
                            bottomNavController.navigate(targetRoute) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontWeight = if (currentRoute == item.route) FontWeight.ExtraBold else FontWeight.Normal,
                                fontFamily = Manrope
                            )
                        },
                        icon = { Icon(painterResource(item.icon), contentDescription = item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GreenMain,
                            selectedTextColor = GreenMain,
                            indicatorColor = GreenMain.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 10)
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = startRoute,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = 20.dp
            )
        ) {
            composable(Screen.Profile.route) {
                ProfilePage(innerPadding = innerPadding, onLogout = {
                    mainNavController.navigate("login") {
                        popUpTo("home") { inclusive = true }

                    }
                },
                    onNavigateToReviews = {
                        // This is the route name we gave your Review Activity earlier
                        bottomNavController.navigate("review_activity")
                    })
            }
            composable(Screen.Discover.route) {
                DiscoverPage(navController = bottomNavController, viewModel = viewModel)
            }
            composable(Screen.Restaurants.route) {
                val selected = viewModel.selectedRestaurantForDetails
                if (selected != null) {
                    RestaurantDetailsPage(
                        restaurant = selected,
                        onBack = { bottomNavController.popBackStack() },
                        onNavigate = {
                            val lat = selected.latitude
                            val lng = selected.longitude
                            bottomNavController.navigate("navigation/$lat/$lng")
                        },
                        viewModel = viewModel,
                        navController = bottomNavController,
                        imageUrlProvider = { fileId ->
                            "https://fra.cloud.appwrite.io/v1/storage/buckets/safemealimg/files/$fileId/view?project=safemealapp"
                        }
                    )
                }
            }
            composable(Screen.Dashboard.route) {
                DashboardPage(navController = bottomNavController, viewModel = viewModel)
            }

            // ── Navigation screen with live GPS ──────────────────────────────
            composable(
                route = "navigation/{lat}/{lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lng") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val destLat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: FALLBACK_LAT
                val destLng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: FALLBACK_LNG

                val context = LocalContext.current

                // null = still fetching, non-null = ready to show NavigationScreen
                var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

                LaunchedEffect(Unit) {
                    userLocation = getCurrentLocation(context)
                        ?: Pair(FALLBACK_LAT, FALLBACK_LNG) // GPS unavailable — use fallback
                }

                if (userLocation == null) {
                    // Show a spinner while the GPS fix is being obtained (usually < 1 second)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenMain)
                    }
                } else {
                    NavigationScreen(
                        destLat = destLat,
                        destLng = destLng,
                        userLat = userLocation!!.first,
                        userLng = userLocation!!.second,
                        onBack = { bottomNavController.popBackStack() }
                    )
                }
            }

            composable("manage_restaurant") {
                ManageRestaurantScreen(
                    viewModel = viewModel,
                    existingRestaurant = viewModel.selectedRestaurantForDetails,
                    onSave = { restaurant, isEdit ->
                        viewModel.saveRestaurant(restaurant, isEdit)
                        viewModel.selectedRestaurantForDetails = null
                        bottomNavController.navigate("discover") {
                            popUpTo(bottomNavController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onBack = {
                        viewModel.selectedRestaurantForDetails = null
                        bottomNavController.navigate("discover")
                    }
                )
            }
            composable("review_activity") {
                ReviewActivityPage(
                    onBack = { bottomNavController.popBackStack() }
                )
            }
            composable("admin_dashboard") {
                AdminDashboardPage(
                    navController = bottomNavController,
                    viewModel = viewModel
                )
            }
        }
    }
}
}