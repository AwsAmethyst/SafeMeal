package com.example.safemeal.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safemeal.R
import com.example.safemeal.data.reviews.Review
import com.example.safemeal.data.reviews.ReviewRepository
import com.example.safemeal.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ReviewActivityPage(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var myReviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }

    // Initial Fetch
    LaunchedEffect(Unit) {
        myReviews = ReviewRepository.fetchUserReviews()
        isLoading = false
    }

    // Function to refresh list after changes
    val refreshReviews = {
        scope.launch {
            isLoading = true
            myReviews = ReviewRepository.fetchUserReviews()
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(WhiteMain)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back")
                }
                Text(
                    text = "Review Activity",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Manrope
                )
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenMain)
                }
            } else if (myReviews.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't posted any reviews yet.", color = GreyMain)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(myReviews) { review ->
                        UserReviewCard(
                            review = review,
                            onEdit = {
                                selectedReview = review
                                showEditDialog = true
                            },
                            onDelete = {
                                scope.launch {
                                    ReviewRepository.deleteReview(review.id)
                                    refreshReviews()
                                }
                            }
                        )
                    }
                    // Bottom Spacer for Nav Bar
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // --- EDIT DIALOG ---
        if (showEditDialog && selectedReview != null) {
            EditReviewDialog(
                review = selectedReview!!,
                onDismiss = { showEditDialog = false },
                onConfirm = { newRating, newComment ->
                    scope.launch {
                        ReviewRepository.updateReview(selectedReview!!.id, newRating, newComment)
                        showEditDialog = false
                        refreshReviews()
                    }
                }
            )
        }
    }
}

@Composable
fun UserReviewCard(
    review: Review,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.restaurantName ?: "Restaurant",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Manrope
                )

                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    repeat(5) { index ->
                        Icon(
                            painter = painterResource(R.drawable.stars),
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFD96F2F) else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = "\"${review.comment}\"",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    fontFamily = Manrope,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Icons Column (Matches Sketch)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onDelete) {
                    Icon(painterResource(R.drawable.delete), contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.background(GreyMain.copy(alpha = 0.1f), CircleShape).size(36.dp)
                ) {
                    Icon(painterResource(R.drawable.edit), contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun EditReviewDialog(
    review: Review,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(review.rating) }
    var comment by remember { mutableStateOf(review.comment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Review", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Update your rating:", fontSize = 14.sp, color = GreyMain)
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    repeat(5) { index ->
                        val starValue = index + 1
                        IconButton(onClick = { rating = starValue }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.stars),
                                contentDescription = null,
                                tint = if (starValue <= rating) Color(0xFFD96F2F) else Color.LightGray
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text("Update", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}