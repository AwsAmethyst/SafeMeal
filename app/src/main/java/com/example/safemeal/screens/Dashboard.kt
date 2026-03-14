package com.example.safemeal.screens

import android.R.attr.onClick
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.safemeal.AppwriteManger
import com.example.safemeal.R
import com.example.safemeal.data.restaurant.Restaurant
import com.example.safemeal.data.restaurant.RestaurantRepository
import com.example.safemeal.data.restaurant.RestaurantViewModel
import com.example.safemeal.data.reviews.ReviewRepository
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.WhiteMain
import kotlinx.coroutines.launch

// Theme Colors
val PrimaryGreen = Color(0xFF267359)
val SecondaryOrange = Color(0xFFD96F2F)
val BackgroundLight = Color(0xFFFBFaf9)

@Composable
fun DashboardPage(
    navController: NavController,
    viewModel: RestaurantViewModel
) {
    // 1. DASHBOARD STATES
    var userName by remember { mutableStateOf("User") }
    var reviewCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var userChoice by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var recentActivity by remember { mutableStateOf<List<Pair<Restaurant, String>>>(emptyList()) }


    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val user = AppwriteManger.AppwriteManager.account.get()
            userName = user.name.split(" ")[0]

            viewModel.fetchRestaurants()

            val reviews = ReviewRepository.getUserRecentReviews(user.id)


            recentActivity = reviews.mapNotNull { review ->
                val foundRestaurant = viewModel.restaurants.find { it.id == review.restaurantId }

                // Only add to the list if the restaurant actually exists
                if (foundRestaurant != null) {
                    Pair(foundRestaurant, "You rated this ${review.rating} stars")
                } else null
            }

            val prefs = AppwriteManger.AppwriteManager.account.getPrefs()
            userChoice = prefs.data["dietary_choice"]?.toString() ?: ""


            viewModel.fetchHotRestaurants(userChoice)
            reviewCount = ReviewRepository.getUserReviewCount(user.id)

        } catch (e: Exception) {
            Log.e("Dashboard", "Error: ${e.message}")
        } finally {
            isLoading = false
        }

    }
    val topRestaurants = viewModel.restaurants
        .filter { restaurant ->
            if (userChoice.isNotEmpty()) {
                restaurant.tags?.contains(userChoice, ignoreCase = true) == true
            } else true
        }
        // Sorting Layers: Rating first, then recency (ID tie-breaker)
        .sortedWith(
            compareByDescending<Restaurant> { it.rating }
                .thenByDescending { it.id }
        )
        .take(5)
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    // Create a helper function to reload everything
    val onRefresh = {
        isRefreshing = true
        scope.launch {
            try {
                // Re-fetch the user preference first
                val prefs = AppwriteManger.AppwriteManager.account.getPrefs()
                val userChoice = prefs.data["dietary_choice"]?.toString() ?: ""

                // Trigger the layered sorting again
                viewModel.fetchHotRestaurants(userChoice)

                // Refresh the review count
                val user = AppwriteManger.AppwriteManager.account.get()
                reviewCount = ReviewRepository.getUserReviewCount(user.id)
            } finally {
                isRefreshing = false
            }
        }
    }
    Scaffold(
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            PullToRefreshBox(
                state = refreshState,
                isRefreshing = isRefreshing,
                onRefresh = { onRefresh() },
                modifier = Modifier.padding(padding),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = refreshState,
                        isRefreshing = isRefreshing,
                        containerColor = WhiteMain,
                        color = PrimaryGreen
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    HeaderSection(userName)
                    SafetyImpactCard(reviewCount)
                    TopRatedSection(
                        restaurants = viewModel.hotRestaurants,
                        userTag = userChoice.ifEmpty { "PREF" }.uppercase(),
                        onRestaurantClick = { selected ->
                            // 1. Tell the ViewModel which restaurant to show details for
                            viewModel.selectedRestaurantForDetails = selected

                            // 2. Navigate to the detail route
                            navController.navigate(Screen.Restaurants.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    RecentActivitySection(
                        activityItems = recentActivity,
                        onItemClick = { selected ->
                            // 1. Save to ViewModel so the Details page knows which one to show
                            viewModel.selectedRestaurantForDetails = selected

                            // 2. Navigate to the Restaurants route
                            navController.navigate(Screen.Restaurants.route) {
                                // This ensures we don't build a massive backstack
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun HeaderSection(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.pfp),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(60.dp) // Adjusted size for better layout
                    .clip(CircleShape)
                    .border(2.dp, PrimaryGreen.copy(alpha = 0.2f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "WELCOME BACK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen.copy(alpha = 0.7f)
                )
                Text(
                    text = "Hello, $name!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun SafetyImpactCard(count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "COMMUNITY IMPACT",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = "$count Safety Reviews",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (count > 0) "You're making Nairobi safer for everyone!" else "Help the community with your first review.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Icon(
                painter = painterResource(R.drawable.stars),
                contentDescription = null,
                tint = SecondaryOrange,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun TopRatedSection(restaurants: List<Restaurant>, userTag: String = "YOUR PREF.", onRestaurantClick: (Restaurant) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Top Rated for You", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Surface(
                color = PrimaryGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "BASED ON $userTag",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(restaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = { onRestaurantClick(restaurant) }
                )
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = restaurant.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Surface(color = SecondaryOrange.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.stars), null, tint = SecondaryOrange, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = if (restaurant.rating > 0.0) "%.1f".format(restaurant.rating) else "N/A",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryOrange)
                    }
                }
            }
            Text(
                text = restaurant.tags ?: "Dine-in",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = PrimaryGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    "CERTIFIED SAFE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
    }
}
@Composable
fun RecentActivitySection(activityItems: List<Pair<Restaurant, String>>,
                          onItemClick: (Restaurant) -> Unit
){
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        activityItems.forEach { (restaurant, subtitle) ->
            RecentItem(
                title = restaurant.name,
                subtitle = subtitle,
                onClick = { onItemClick(restaurant) } // Pass the click up
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RecentItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Make the whole row clickable
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.visibility), null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(painterResource(R.drawable.arrow_back), contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
    }
}