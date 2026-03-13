package com.example.safemeal.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safemeal.R
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.SafeMealTheme

// Theme Colors
val PrimaryGreen = Color(0xFF267359)
val SecondaryOrange = Color(0xFFD96F2F)
val BackgroundLight = Color(0xFFFBFaf9)

@Composable
fun DashboardPage() {
    Scaffold(
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection()
            MonthlyVisitsCard()
            TopRatedSection()
            RecentActivitySection()
        }
    }
}

@Composable
fun HeaderSection() {
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
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(4.dp, GreyMain.copy(alpha = 0.8f), CircleShape),
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
                    text = "Hello, Arjun!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(44.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(painterResource(R.drawable.reviews), contentDescription = "Notifications")
        }
    }
}


@Composable
fun MonthlyVisitsCard() {
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Monthly Visits", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("View reports", color = PrimaryGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Simplified Chart Representation
                Row(
                    modifier = Modifier.height(100.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Bar(12, 0.6f, "OCT")
                    Bar(15, 0.75f, "NOV")
                    Bar(18, 0.9f, "DEC")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "You're exploring more safe options each month!",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun Bar(value: Int, heightPercent: Float, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(heightPercent)
                .background(PrimaryGreen, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        )
        Text(label, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun TopRatedSection() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
                    "BASED ON VEGAN PREF.",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(listOf("The Green Sprout", "Pure Prana Kitchen", "Zen Bowl")) { name ->
                RestaurantCard(name)
            }
        }
    }
}

@Composable
fun RestaurantCard(name: String) {
    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(R.drawable.pfp),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(4.dp, GreyMain.copy(alpha = 0.8f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.stars), contentDescription = null, tint = SecondaryOrange, modifier = Modifier.size(14.dp))
                        Text("4.8", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondaryOrange)
                    }
                }
                Text("100% Vegan • 1.2 miles away", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
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
}

@Composable
fun RecentActivitySection() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        RecentItem("Zen Garden Cafe", "Visited yesterday • Order #8291")
        Spacer(modifier = Modifier.height(12.dp))
        RecentItem("The Conscious Plate", "Viewed 2 hours ago")
    }
}

@Composable
fun RecentItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(painterResource(R.drawable.chevron_right), contentDescription = null, tint = PrimaryGreen)
    }
}

@Preview
@Composable
fun DashboardPreview(){
    SafeMealTheme {
        DashboardPage()
    }
}