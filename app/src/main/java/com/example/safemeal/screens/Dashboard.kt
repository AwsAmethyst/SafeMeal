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
import androidx.compose.ui.geometry.Offset
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
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.Manrope
import com.example.safemeal.ui.theme.WhiteMain
import kotlinx.coroutines.launch
// UI & Graphics Imports
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

// Compose UI Core
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var isAdmin by remember { mutableStateOf(false) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) } // New

    var monthlyData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userProfile = AppwriteManger.AppwriteManager.account.get()
            // Check if "admin" is in the user's labels
            isAdmin = userProfile.labels.contains("admin")

            val user = AppwriteManger.AppwriteManager.account.get()
            userName = user.name.split(" ")[0]
            profileImageUrl = AppwriteManger.AppwriteManager.getUserProfilePictureUrl(userProfile.id)

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

            val stats = ReviewRepository.getMonthlyReviewStats()
            monthlyData = stats

        } catch (e: Exception) {
            Log.e("Dashboard", "Error: ${e.message}")
            Log.e("Dashboard", "Admin check failed")
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
                profileImageUrl = AppwriteManger.AppwriteManager.getUserProfilePictureUrl(user.id)
            } finally {
                isRefreshing = false
            }
        }
    }
    Scaffold(
        containerColor = BackgroundLight
    ) { innerpadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            PullToRefreshBox(
                state = refreshState,
                isRefreshing = isRefreshing,
                onRefresh = { onRefresh() },
                modifier = Modifier.padding(top = 0.dp),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = refreshState,
                        isRefreshing = isRefreshing,
                        containerColor = WhiteMain,
                        color = PrimaryGreen,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    HeaderSection(userName,profileImageUrl)
                    //SafetyImpactCard(reviewCount)
                    if (monthlyData.isNotEmpty()) {
                        UserImpactLineGraph(reviewData = monthlyData)
                    }
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
                    if (isAdmin) {
                        Button(
                            onClick = {
                                viewModel.selectedRestaurantForDetails = null
                                navController.navigate("manage_restaurant")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp,
                                    start = 20.dp,
                                    end = 20.dp)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(8.dp))
                                Text("Add New Restaurant", fontWeight = FontWeight.Bold)
                            }

                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
@Composable
fun HeaderSection(name: String,profileImageUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            coil.compose.AsyncImage(
                model = profileImageUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, PrimaryGreen.copy(alpha = 0.2f), CircleShape),
                contentScale = ContentScale.Crop,
                // Fallbacks to your local placeholder if loading fails or URL is null
                placeholder = painterResource(R.drawable.pfp),
                error = painterResource(R.drawable.pfp)
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
fun TopRatedSection(restaurants: List<Restaurant>, userTag: String = "YOUR PREF.",
                    onRestaurantClick: (Restaurant) -> Unit) {
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
@Composable
fun UserImpactLineGraph(reviewData: List<Pair<String, Int>>) {
    // 1. Calculate Max and create 'Headroom' (30% space above the peak)
    val maxCount = reviewData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    val graphMax = maxCount * 1.3f

    // Theme Colors
    val darkOrange = Color(0xFFBF4F00)
    val gridColor = Color.LightGray.copy(alpha = 0.2f)
    val gradientStart = darkOrange.copy(alpha = 0.25f)
    val gradientEnd = darkOrange.copy(alpha = 0.0f)
    val axisColor = Color.LightGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "COMMUNITY CONTRIBUTION TREND",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = GreyMain,
                fontFamily = Manrope
            )

            Spacer(modifier = Modifier.height(30.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(start = 45.dp, bottom = 25.dp, end = 25.dp)
            ) {
                val width = size.width
                val height = size.height

                // --- 1. CALCULATE DATA POINTS (With Headroom Scaling) ---
                val xSpacing = if (reviewData.size > 1) width / (reviewData.size - 1) else width
                val dataPoints = reviewData.mapIndexed { index, pair ->
                    val x = index * xSpacing
                    // Scale relative to graphMax instead of maxCount for space at the top
                    val yScaled = (pair.second.toFloat() / graphMax) * height
                    val y = height - yScaled
                    Offset(x, y)
                }

                // --- 2. DRAW Y-AXIS LABELS ---
                // Draw Max Label at the actual peak's height
                val peakY = dataPoints.minOf { it.y }
                drawContext.canvas.nativeCanvas.drawText(
                    maxCount.toString(),
                    -25f,
                    peakY + 10f, // Aligned with the peak dot
                    android.graphics.Paint().apply {
                        color = darkOrange.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "0",
                    -25f,
                    height,
                    android.graphics.Paint().apply {
                        color = axisColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )

                // --- 3. DRAW GRID PATTERN ---
                val gridStepPx = 35.dp.toPx()
                for (x in 0 until (width / gridStepPx).toInt() + 1) {
                    val lineX = x * gridStepPx
                    if (lineX <= width) {
                        drawLine(color = gridColor, start = Offset(lineX, 0f), end = Offset(lineX, height), strokeWidth = 1.dp.toPx())
                    }
                }
                for (y in 0 until (height / gridStepPx).toInt() + 1) {
                    val lineY = y * gridStepPx
                    if (lineY <= height) {
                        drawLine(color = gridColor, start = Offset(0f, lineY), end = Offset(width, lineY), strokeWidth = 1.dp.toPx())
                    }
                }

                // --- 4. DRAW AREA SHADING (GRADIENT) ---
                if (dataPoints.size > 1) {
                    val fillPath = Path().apply {
                        moveTo(0f, height)
                        dataPoints.forEach { lineTo(it.x, it.y) }
                        lineTo(width, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(gradientStart, gradientEnd),
                            startY = dataPoints.minOf { it.y },
                            endY = height
                        )
                    )
                }

                // --- 5. DRAW THE TREND LINE ---
                if (dataPoints.size > 1) {
                    val linePath = Path().apply {
                        moveTo(dataPoints[0].x, dataPoints[0].y)
                        for (i in 1 until dataPoints.size) { lineTo(dataPoints[i].x, dataPoints[i].y) }
                    }
                    drawPath(
                        path = linePath,
                        color = darkOrange,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    dataPoints.forEach {
                        drawCircle(color = darkOrange, radius = 5.dp.toPx(), center = it)
                        drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = it)
                    }
                }

                // --- 6. DRAW AXES WITH ARROWS ---
                drawLine(color = axisColor, start = Offset(0f, height), end = Offset(width, height), strokeWidth = 1.5.dp.toPx())
                drawLine(color = axisColor, start = Offset(width, height), end = Offset(width - 15f, height - 12f), strokeWidth = 1.5.dp.toPx())
                drawLine(color = axisColor, start = Offset(width, height), end = Offset(width - 15f, height + 12f), strokeWidth = 1.5.dp.toPx())

                drawLine(color = axisColor, start = Offset(0f, 0f), end = Offset(0f, height), strokeWidth = 1.5.dp.toPx())
                drawLine(color = axisColor, start = Offset(0f, 0f), end = Offset(-12f, 15f), strokeWidth = 1.5.dp.toPx())
                drawLine(color = axisColor, start = Offset(0f, 0f), end = Offset(12f, 15f), strokeWidth = 1.5.dp.toPx())

                // --- 7. DRAW MONTH LABELS ---
                reviewData.forEachIndexed { index, pair ->
                    val x = index * xSpacing
                    drawContext.canvas.nativeCanvas.drawText(
                        pair.first.uppercase(),
                        x,
                        height + 45f,
                        android.graphics.Paint().apply {
                            color = GreyMain.toArgb()
                            textSize = 11.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                    )
                }
            }
        }
    }
}