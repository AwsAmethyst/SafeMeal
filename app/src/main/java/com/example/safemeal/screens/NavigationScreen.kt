package com.example.safemeal.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.safemeal.R
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.common.location.Location
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.components.maneuver.view.MapboxManeuverView


@SuppressLint("MissingPermission")
@Composable
fun NavigationScreen(
    destLat: Double,
    destLng: Double,
    userLat: Double,
    userLng: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val mapboxNavigation = MapboxNavigationApp.current()
    val mapView = remember { MapView(context) }
// Tracks if the user has reached the restaurant
    var hasArrived by remember { mutableStateOf(false) }
    // NavigationLocationProvider drives the blue puck on the map.
    val navigationLocationProvider = remember { NavigationLocationProvider() }

// 1. Initialize the API and explicitly turn ON the vanishing feature
    val routeLineApi = remember {
        MapboxRouteLineApi(
            MapboxRouteLineApiOptions.Builder()
                .vanishingRouteLineEnabled(true) // <--- THIS MAKES IT TRIM BEHIND THE PUCK
                .build()
        )
    }

    // 2. Initialize the View and keep the line under the street names and puck
    val routeLineView = remember {
        MapboxRouteLineView(
            MapboxRouteLineViewOptions.Builder(context)
                .routeLineBelowLayerId("road-label")
                .build()
        )
    }
// --- Direction Banner Setup ---
    val distanceFormatterOptions = remember {
        DistanceFormatterOptions.Builder(context).build()
    }
    val maneuverApi = remember {
        MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatterOptions))
    }
    val maneuverView = remember {
        MapboxManeuverView(context).apply {
            // Optional: Hide the banner initially until the first GPS update arrives
            visibility = android.view.View.INVISIBLE
        }
    }
    // Holds the loaded map style
    var mapStyle by remember { mutableStateOf<com.mapbox.maps.Style?>(null) }

    // Link the location puck to NavigationLocationProvider
    /*LaunchedEffect(mapView) {
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
        mapView.location.setLocationProvider(navigationLocationProvider)
    }
*/
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Map ──────────────────────────────────────────────────────────────
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
        AndroidView(
            factory = { maneuverView },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 76.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        )


        // ── Back button ──────────────────────────────────────────────────────
        IconButton(
            onClick = {
                mapboxNavigation?.setNavigationRoutes(emptyList())
                mapboxNavigation?.stopTripSession()
                onBack()
            },
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black, CircleShape)
                .size(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // ── Recenter button ──────────────────────────────────────────────────
        FloatingActionButton(
            onClick = {
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(userLng, userLat))
                        .zoom(16.0)
                        .bearing(0.0)
                        .pitch(45.0)
                        .build()
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 16.dp),
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(
                painter = painterResource(R.drawable.explore),
                contentDescription = "Recenter"
            )
        }
    }

    // ── Location observer ────────────────────────────────────────────────────
    DisposableEffect(mapboxNavigation) {
        val locationObserver = object : LocationObserver {
            override fun onNewRawLocation(rawLocation: Location) {}

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                val enhancedLocation = locationMatcherResult.enhancedLocation

                navigationLocationProvider.changePosition(
                    location = enhancedLocation,
                    keyPoints = locationMatcherResult.keyPoints
                )

                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude))
                        .zoom(16.0)
                        .bearing(enhancedLocation.bearing ?: 0.0)
                        .pitch(45.0)
                        .build()
                )
            }
        }

        mapboxNavigation?.registerLocationObserver(locationObserver)
        mapboxNavigation?.startTripSession()

        onDispose {
            mapboxNavigation?.unregisterLocationObserver(locationObserver)
            mapboxNavigation?.stopTripSession()
        }
    }

    // ── FIX 2: Restored Route progress observer to trim the travelled line ──
// ── Route progress observer ──
    DisposableEffect(mapboxNavigation) {
        val routeProgressObserver = RouteProgressObserver { routeProgress ->

            // 1. Trim the traveled line
            mapView.mapboxMap.getStyle()?.let { style ->
                if (style.isValid()) {
                    routeLineApi.updateWithRouteProgress(routeProgress) { value ->
                        routeLineView.renderRouteLineUpdate(style, value)
                    }
                }
            }

            // 2. Feed progress directly to the Direction Banner
            val maneuvers = maneuverApi.getManeuvers(routeProgress)
            maneuverView.renderManeuvers(maneuvers)
            if (maneuvers.isValue) {
                maneuverView.visibility = android.view.View.VISIBLE
            }

            // 3. FIX: Check for Arrival (If less than 20 meters away)
            val distanceRemaining = routeProgress.distanceRemaining
            if (distanceRemaining < 100.0 && !hasArrived) {
                hasArrived = true
                // Optional: Clear the route line off the map
                routeLineApi.clearRouteLine { value ->
                    mapView.mapboxMap.getStyle()?.let { style ->
                        routeLineView.renderClearRouteLineValue(style, value)
                    }
                }
            }
        }

        mapboxNavigation?.registerRouteProgressObserver(routeProgressObserver)

        onDispose {
            mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)
            maneuverApi.cancel()
        }
    }

    // ── Routes observer — redraws the blue line on Rerouting ──
    DisposableEffect(mapboxNavigation) {
        val routesObserver = RoutesObserver { routeUpdateResult ->
            val newRoutes = routeUpdateResult.navigationRoutes

            mapView.mapboxMap.getStyle()?.let { style ->
                if (style.isValid()) {
                    if (newRoutes.isNotEmpty()) {
                        routeLineApi.setNavigationRoutes(newRoutes) { value ->
                            routeLineView.renderRouteDrawData(style, value)
                        }
                    } else {
                        routeLineApi.clearRouteLine { value ->
                            routeLineView.renderClearRouteLineValue(style, value)
                        }
                    }
                }
            }
        }

        mapboxNavigation?.registerRoutesObserver(routesObserver)

        onDispose {
            mapboxNavigation?.unregisterRoutesObserver(routesObserver)
        }
    }

// ── Load style → then fetch + draw route ─────────────────────────────────
    LaunchedEffect(Unit) {
        val origin = Point.fromLngLat(userLng, userLat)
        val destination = Point.fromLngLat(destLng, destLat)

        mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
            mapStyle = style

            // FIX: Initialize the Location Puck HERE, strictly after the style has loaded!
            // This guarantees the "location-indicator-layer" exists before the route is drawn.
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
            mapView.location.setLocationProvider(navigationLocationProvider)

            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(origin)
                    .zoom(15.0)
                    .pitch(45.0)
                    .build()
            )

            mapboxNavigation?.requestRoutes(
                RouteOptions.builder()
                    .applyDefaultNavigationOptions()
                    .coordinatesList(listOf(origin, destination))
                    .build(),
                object : NavigationRouterCallback {
                    override fun onRoutesReady(
                        routes: List<NavigationRoute>,
                        routerOrigin: String
                    ) {
                        mapboxNavigation.setNavigationRoutes(routes)

                        // Because the puck is now fully loaded above, this line will
                        // successfully render underneath it and trim as you move.
                        routeLineApi.setNavigationRoutes(routes) { value ->
                            routeLineView.renderRouteDrawData(style, value)
                        }

                        Log.d("Nav", "Route ready: ${routes.size} route(s)")
                    }

                    // ... (Keep onFailure and onCanceled as they are)

                    override fun onFailure(
                        reasons: List<RouterFailure>,
                        routeOptions: RouteOptions
                    ) {
                        Log.e("Nav", "Route failed: $reasons")
                    }

                    override fun onCanceled(
                        routeOptions: RouteOptions,
                        routerOrigin: String
                    ) {
                        Log.d("Nav", "Route cancelled")
                    }
                }
            )
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────
    DisposableEffect(Unit) {
        onDispose {
            routeLineApi.cancel()
            routeLineView.cancel()
        }
    }

    // ── Arrival Dialog ────────────────────────────────────────────────────────
    if (hasArrived) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { /* Force user to click the button */ },
            title = {
                androidx.compose.material3.Text("You've Arrived! 🍽️")
            },
            text = {
                androidx.compose.material3.Text("You have reached your destination. Enjoy your dietary-safe meal!")
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        // Clean up and navigate back to the previous screen
                        hasArrived = false
                        mapboxNavigation?.setNavigationRoutes(emptyList())
                        mapboxNavigation?.stopTripSession()
                        onBack()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Black // Matches your Back Button theme
                    )
                ) {
                    androidx.compose.material3.Text("Finish Navigation", color = Color.White)
                }
            }
        )
    }
}