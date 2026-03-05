package com.example.safemeal.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safemeal.R
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.Manrope
import com.example.safemeal.ui.theme.SafeMealTheme
import com.example.safemeal.ui.theme.WhiteMain
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold


@Composable
fun RestaurantDetailsPage(
    restaurant: com.example.safemeal.Restaurant,
    onBack: () -> Unit,
    onNavigate: () -> Unit // Add this lambda for the button action
) {
    val menuItems = listOf(
        MenuItem("Classic Hummus", "Creamy chickpeas with olive oil", "KES 650"),
        MenuItem("Falafel Wrap", "Crispy falafel with fresh tahini", "KES 850"),
        MenuItem("Sattvic Thali", "Pure vegetarian platter with lentils", "KES 1,200")
    )

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Option A: Trigger your internal Mapbox Navigation logic (FR2)
                    onNavigate()

                    // Option B: Launch an external Map Intent as a fallback
                    /* val gmmIntentUri = Uri.parse("google.navigation:q=${restaurant.location}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    context.startActivity(mapIntent)
                    */
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
                    Image(
                        painter = painterResource(R.drawable.pfp),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
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
            items(menuItems) { menu ->
                MenuListItem(menu)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = GreyMain.copy(alpha = 0.2f)
                )
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
/*
@Preview(showBackground = true, name = "Standard View")
@Composable
fun RestaurantDetailsMockPreview() {

    SafeMealTheme {
        RestaurantDetailsPage(
            restaurant = ,
            onBack = {},
            onNavigate = {}
        )
    }
}*/