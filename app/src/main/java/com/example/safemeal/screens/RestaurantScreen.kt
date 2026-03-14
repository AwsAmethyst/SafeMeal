package com.example.safemeal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safemeal.R
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.Manrope
import com.example.safemeal.ui.theme.WhiteMain
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.safemeal.data.restaurant.Restaurant
import com.example.safemeal.data.reviews.ReviewRepository
import kotlinx.coroutines.launch


@Composable
fun RestaurantDetailsPage(
    restaurant: Restaurant,
    onBack: () -> Unit,
    onNavigate: () -> Unit, // Add this lambda for the button action
    imageUrlProvider: (String) -> String
) {
    val menuItems = remember(restaurant) { restaurant.getMenuItemsList() }
    val restaurantImageUrl = remember(restaurant.imgid) {
        imageUrlProvider(restaurant.imgid ?: "")
    }
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var hasAlreadyReviewed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reviewsList by remember { mutableStateOf<List<com.example.safemeal.data.reviews.Review>>(emptyList()) }
    var isLoadingReviews by remember { mutableStateOf(true) }

    LaunchedEffect(restaurant.id) {
        isLoadingReviews = true
        // Fetch the reviews
        reviewsList = ReviewRepository.fetchReviews(restaurant.id)
        isLoadingReviews = false
    }
    LaunchedEffect(restaurant.id) {
        hasAlreadyReviewed = com.example.safemeal.data.reviews.ReviewRepository.hasUserReviewed(restaurant.id)
    }
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onNavigate()
                },
                containerColor = GreenMain,
                contentColor = WhiteMain,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(painterResource(R.drawable.explore), contentDescription = null) },
                text = { Text("Directions", fontFamily = Manrope, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerpadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteMain),
            contentPadding = innerpadding
        ) {
            // 1. Hero Image Section
            item {
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                ) {
                    AsyncImage(
                        model = restaurantImageUrl,
                        contentDescription = "Image of ${restaurant.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        // Fallback in case the network fails
                        placeholder = painterResource(R.drawable.logo2),
                        error = painterResource(R.drawable.logo2)
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(WhiteMain, CircleShape)
                    ) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back")
                    }
                }
            }

            // 2. Restaurant Identity & Info Section
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = restaurant.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = Manrope
                    )

                    Row(modifier = Modifier.padding(vertical = 12.dp)) {
                        restaurant.tags?.let {
                            Text(
                                text = it,

                                )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )

                    // FR4: Location and Hours info
                    InfoRow(
                        icon = R.drawable.explore,
                        title = "Location",
                        subtitle = restaurant.address ?: "Nairobi" // Changed from .location
                    )
                    InfoRow(
                        icon = R.drawable.visibility,
                        title = "Hours",
                        subtitle = restaurant.hours ?: "09:00 AM - 09:00 PM"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )

                    Text(
                        text = "MENU ITEMS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = GreyMain,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            // 3. The Menu List
// The dynamic list
            items(menuItems) { menu ->
                MenuListItem(menu)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            }

            if (menuItems.isEmpty()) {
                item {
                    Text("No menu items found.", modifier = Modifier.padding(20.dp))
                }
            }
            item {
                Text(
                    text = "COMMUNITY REVIEWS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreyMain,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
            }

            if (isLoadingReviews) {
                item {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = GreenMain, modifier = Modifier.size(24.dp))
                    }
                }
            } else if (reviewsList.isEmpty()) {
                item {
                    Text(
                        "No reviews yet. Be the first to share your experience!",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = Manrope
                    )
                }
            } else {
                // 3. Render each review
                items(reviewsList) { review ->
                    ReviewListItem(
                        userName = review.userName,
                        rating = review.rating,
                        comment = review.comment
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp)
                }
            }
            item {
                if (hasAlreadyReviewed) {
                    // Show the "Locked" state UI we created earlier
                    ReviewLockedCard()
                } else {
                    ReviewInputSection(
                        restaurantId = restaurant.id,
                        currentRating = rating,
                        currentComment = comment,
                        isSubmitting = isSubmitting,
                        onRatingChange = { rating = it },
                        onCommentChange = { comment = it },
                        onSubmit = {
                            scope.launch {
                                isSubmitting = true
                                val result = ReviewRepository.addReview(restaurant.id, rating, comment)
                                when (result) {
                                    "SUCCESS" -> {
                                        rating = 0
                                        comment = ""
                                        hasAlreadyReviewed = true
                                        scope.launch {
                                            reviewsList = ReviewRepository.fetchReviews(restaurant.id)
                                        }
                                    }
                                    "ALREADY_REVIEWED" -> {
                                        hasAlreadyReviewed = true
                                        errorMessage = "Already reviewed!"
                                    }
                                    else -> { errorMessage = "Error posting review" }
                                }
                                isSubmitting = false
                            }
                        }
                    )
                }
            }
    }
    }
}
@Composable
fun MenuListItem(menu: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = menu.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = menu.description, color = GreyMain, fontSize = 14.sp)
        }
        Text(
            text = menu.price,
            fontWeight = FontWeight.ExtraBold,
            color = GreenMain,
            fontSize = 16.sp
        )
    }
}


data class MenuItem(val name: String, val description: String, val price: String)
@Composable
fun InfoRow(icon: Int, title: String, subtitle: String?) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), contentDescription = null, tint = GreyMain, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreyMain)
            if (subtitle != null) {
                Text(text = subtitle, fontWeight = FontWeight.Normal, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ReviewInputSection(
    restaurantId: String,
    currentRating: Int,
    currentComment: String,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(GreyMain.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "RATE & REVIEW",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = GreyMain
        )

        // 1. Star Selection
        Row(modifier = Modifier.padding(vertical = 12.dp)) {
            repeat(5) { index ->
                val starValue = index + 1
                Icon(
                    painter = painterResource(id = R.drawable.stars),
                    contentDescription = null,
                    tint = if (starValue <= currentRating) Color(0xFFD96F2F) else Color.LightGray,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onRatingChange(starValue) }
                )
            }
        }

        // 2. Comment Input
        androidx.compose.material3.TextField(
            value = currentComment,
            onValueChange = onCommentChange,
            placeholder = { Text("Share your experience...") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = WhiteMain,
                unfocusedContainerColor = WhiteMain.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(12.dp))

        // 3. Submit Button
        androidx.compose.material3.Button(
            onClick = onSubmit,
            enabled = currentRating > 0 && !isSubmitting,
            modifier = Modifier.align(Alignment.Start), // Aligned to end for better thumb reach
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GreenMain)
        ) {
            if (isSubmitting) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = WhiteMain,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Post Review", color = WhiteMain, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun ReviewLockedCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(GreenMain.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, GreenMain.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.check), // Ensure you have a check icon
            contentDescription = null,
            tint = GreenMain,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Feedback Received",
            fontWeight = FontWeight.Bold,
            fontFamily = Manrope,
            color = GreenMain,
            fontSize = 18.sp
        )
        Text(
            text = "You've already shared your safety experience for this restaurant. Thank you for helping the community!",
            textAlign = TextAlign.Center,
            fontFamily = Manrope,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
@Composable
fun ReviewListItem(userName: String, rating: Int, comment: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = userName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = Manrope
            )
            // Show stars for this specific user's rating
            Row {
                repeat(rating) {
                    Icon(
                        painter = painterResource(R.drawable.stars),
                        contentDescription = null,
                        tint = Color(0xFFD96F2F),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        if (comment.isNotEmpty()) {
            Text(
                text = comment,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp),
                fontFamily = Manrope
            )
        }
    }
}