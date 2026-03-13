package com.example.safemeal.screens


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.safemeal.R
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.Manrope
import com.example.safemeal.ui.theme.SafeMealTheme
import com.example.safemeal.ui.theme.WhiteMain
import androidx.navigation.compose.composable
import com.example.safemeal.data.restaurant.RestaurantViewModel

sealed class Screen(val route: String, val label: String, val icon: Int) {
    object Profile : Screen("profile", "Profile", R.drawable.profile)
    object Discover : Screen("discover", "Discover", R.drawable.explore)
    object Restaurants : Screen("restaurants", "Restaurants", R.drawable.restaurant)

    object Dashboard : Screen("dashboard","Dashboard", R.drawable.dashboard)
}

@Composable
fun HomePage(mainNavController: NavController) {
    val viewModel: RestaurantViewModel = viewModel()
    val bottomNavController = rememberNavController() // Local controller for tabs
    val items = listOf(Screen.Profile, Screen.Dashboard, Screen.Discover) //,Screen.Restaurants)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = WhiteMain,
                tonalElevation = 0.dp // Clean white look
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            // Professional navigation: avoids building up a stack of tabs
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontWeight = if (currentRoute == item.route) FontWeight.ExtraBold else FontWeight.Normal,
                                fontFamily = Manrope // Your custom branding
                            )
                        },
                        icon = { Icon(painterResource(item.icon), contentDescription = item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GreenMain,
                            selectedTextColor = GreenMain,
                            indicatorColor = GreenMain.copy(alpha = 0.1f) // Subtle selection pill
                        )
                    )
                }
            }
        },
        // Allows content (like your map) to draw behind the bars if needed
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // 2. The NavHost manages the actual screen switching
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding) // Fixes the "overlap" issue
        ) {
            composable(Screen.Profile.route) {
                // Pass the padding to ProfilePage to fix the green "peeks"
                ProfilePage(innerPadding = innerPadding, onLogout = {
                    // Logic to go back to the Login screen
                    mainNavController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                })
            }
            composable(Screen.Discover.route) {
                DiscoverPage(navController = bottomNavController, viewModel = viewModel) // Your MapBox implementation
            }
            composable(Screen.Restaurants.route) {
                // Future implementation
// Pull the real restaurant saved in the ViewModel
                val selected = viewModel.selectedRestaurantForDetails

                if (selected != null) {
                    RestaurantDetailsPage(
                        restaurant = selected,
                        onBack = { bottomNavController.popBackStack() },
                        onNavigate = {},
                        imageUrlProvider = { fileId ->
                            // Direct call to your Appwrite Storage service
                            "https://fra.cloud.appwrite.io/v1/storage/buckets/safemealimg/files/$fileId/view?project=safemealapp"
                        }
                    )
            }
        }
            composable(Screen.Dashboard.route){
                DashboardPage()
            }
    }
    }
}

@Preview
@Composable
fun HomePreview(){
    SafeMealTheme {
        val navController = rememberNavController()
        HomePage(mainNavController = navController)
    }
}