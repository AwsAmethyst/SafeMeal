package com.example.safemeal.screens

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safemeal.R
import com.example.safemeal.data.restaurant.Restaurant
import com.example.safemeal.data.restaurant.RestaurantViewModel
import com.example.safemeal.ui.theme.WhiteMain
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverPage(navController: NavController, // 1. Added this parameter
                 viewModel: RestaurantViewModel) {
    val nairobiPoint = Point.fromLngLat(36.8219, -1.2921)
    //val context = LocalContext.current
    val restaurants = viewModel.restaurants
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    // Use the full path or ensure the correct import is at the top
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(36.8219, -1.2921)) // Nairobi
            zoom(12.0)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.fetchRestaurants()
        viewModel.loadPersonalizedContent("current_user_id")
    }
    Box {
        MapboxMap(
            style = { MapStyle(style = "mapbox://styles/yokai-aws/cmlm3glvm001t01r339siet1d") },
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            mapState = rememberMapState {
                gesturesSettings = GesturesSettings {
                    pinchToZoomEnabled = true // Ensure this is true
                    doubleTapToZoomInEnabled = true
                    pitchEnabled = false
                }
            }
        ) {
            val markerIcon = rememberIconImage(
                key = "marker",
                painter = painterResource(id = R.drawable.location), // Use your search icon as a temporary pin
            )
            Log.d("SafeMealMap", "Total restaurants fetched: ${restaurants.size} ")
            restaurants.forEach { restaurant ->
                //Log.d("SafeMealMap", "${restaurant.longitude} ${restaurant.latitude}")
                PointAnnotation(
                    point = Point.fromLngLat(restaurant.longitude, restaurant.latitude),
                    onClick = {
                        // Update state to show this specific restaurant
                        selectedRestaurant = restaurant
                        showSheet = true
                        true
                    }
                ) {

                    iconImage = markerIcon // Assign the remembered icon image
                    iconSize = 2.0
                }
            }
        }
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = WhiteMain,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                RestaurantDetailContent(
                    restaurant = selectedRestaurant,
                    onViewDetails = { restaurant ->
                        // 1. Save the selected restaurant to the ViewModel for the next screen
                        viewModel.selectedRestaurantForDetails = restaurant
                        // 2. Close the sheet
                        showSheet = false
                        // 3. Navigate to the Restaurants route
                        // Ensure you pass navController: NavController into DiscoverPage
                        navController.navigate(Screen.Restaurants.route)
                    })
            }
        }
        SafeMealSearchBar(
            viewModel = viewModel,
            onRestaurantClick = { restaurant ->
                selectedRestaurant = restaurant
                showSheet = true
                mapViewportState.flyTo(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(Point.fromLngLat(restaurant.longitude, restaurant.latitude))
                        .zoom(15.0) // Zoom in closer for the specific restaurant
                        .build(),
                    com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions {
                        duration(2000) // 2 seconds for a smooth glide
                    }
                )
            }
        )
    }
}



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SafeMealSearchBar(
        viewModel: RestaurantViewModel, // Pass the shared ViewModel
        onRestaurantClick: (Restaurant) -> Unit // Callback for selection
    ) {
        var query by rememberSaveable { mutableStateOf("") }
        var expanded by rememberSaveable { mutableStateOf(false) }

        // Filter logic using your real data
        val filteredResults = viewModel.restaurants.filter {
            it.name.contains(query, ignoreCase = true)
        }

        Box(Modifier.fillMaxSize()) {
            SearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { expanded = false },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("Search Restaurants...") },
                        leadingIcon = { Icon(painterResource(R.drawable.search), null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(painterResource(R.drawable.close), null)
                                }
                            }
                        }
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                LazyColumn {
                    // Use the filtered list instead of hardcoded '5' items
                    items(filteredResults) { restaurant ->
                        ListItem(
                            headlineContent = { Text(restaurant.name) },
                            supportingContent = { Text(restaurant.address ?: "") },
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.restaurant),
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                onRestaurantClick(restaurant) // Trigger map action
                                expanded = false
                                query = "" // Optional: Clear search on select
                            }
                        )
                    }
                    if (filteredResults.isEmpty() && query.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(painterResource(R.drawable.search), null, tint = Color.Gray)
                                Text(
                                    "No restaurants found for \"$query\"",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RestaurantDetailContent(
        restaurant: Restaurant?,
        onViewDetails: (Restaurant) -> Unit // New lambda for navigation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
        ) {
            Text(
                text = restaurant?.name ?: "Unknown Restaurant",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )

            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                restaurant?.tags?.let {
                    Text(
                        text = it,

                        )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.location),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = restaurant?.address ?: "No address available",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.visibility),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open: ${restaurant?.hours ?: "Check App"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- ADDED BUTTON ---
            Spacer(modifier = Modifier.height(24.dp))

            androidx.compose.material3.Button(
                onClick = { restaurant?.let { onViewDetails(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.example.safemeal.ui.theme.GreenMain
                )
            ) {
                Text("View Full Menu & Details", color = WhiteMain)
            }
        }
    }

/*
@Preview
@Composable
fun DetailPreview(){
    SafeMealTheme {
        RestaurantDetailContent( restaurant = null)
}
}
*/
