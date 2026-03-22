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
// FIX: Import ONLY com.mapbox.common.location.Location — do NOT import android.location.Location.
// The LocationObserver interface in Navigation SDK v3 uses com.mapbox.common.location.Location
// for onNewRawLocation. Importing android.location.Location causes the "not abstract" error
// because the compiler sees them as two different types and thinks the method is unimplemented.
import com.mapbox.common.location.Location

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

    // NavigationLocationProvider drives the blue puck on the map.
    // It must be fed location updates from onNewLocationMatcherResult.
    val navigationLocationProvider = remember { NavigationLocationProvider() }

    // Route line — created once, not on every recomposition
    val routeLineApi = remember {
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    }
    val routeLineView = remember {
        MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build())
    }

    // Holds the loaded map style so the RouteProgressObserver can render updates
    var mapStyle by remember { mutableStateOf<com.mapbox.maps.Style?>(null) }

    // Link the location puck to NavigationLocationProvider
    LaunchedEffect(mapView) {
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
        mapView.location.setLocationProvider(navigationLocationProvider)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Map ──────────────────────────────────────────────────────────────
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // ── Back button ──────────────────────────────────────────────────────
        IconButton(
            onClick = {
                // Clear the active route and stop the trip session before leaving
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
            // FIX: Parameter type is com.mapbox.common.location.Location (imported above).
            // Do NOT qualify it as android.location.Location — that's a different class
            // and causes the "does not implement abstract member" compile error.
            override fun onNewRawLocation(rawLocation: Location) {
                // Raw GPS — intentionally empty; use onNewLocationMatcherResult instead
            }

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                val enhancedLocation = locationMatcherResult.enhancedLocation

                // FIX: Feed location into NavigationLocationProvider so the puck actually moves.
                // Previously we only moved the camera but never updated the puck position.
                navigationLocationProvider.changePosition(
                    location = enhancedLocation,
                    keyPoints = locationMatcherResult.keyPoints
                )

                // Move camera to follow user with nav-style bearing + pitch
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

    // ── Route progress observer — trims the travelled part of the blue line ──
    DisposableEffect(mapboxNavigation) {
        val routeProgressObserver = RouteProgressObserver { routeProgress ->
            // updateWithRouteProgress tells the API how far along the route we are.
            // The resulting value, when rendered, hides the already-travelled portion.
            val style = mapStyle ?: return@RouteProgressObserver
            routeLineApi.updateWithRouteProgress(routeProgress) { value ->
                routeLineView.renderRouteLineUpdate(style, value)
            }
        }

        mapboxNavigation?.registerRouteProgressObserver(routeProgressObserver)

        onDispose {
            mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)
        }
    }

    // ── Load style → then fetch + draw route ─────────────────────────────────
    LaunchedEffect(Unit) {
        val origin = Point.fromLngLat(userLng, userLat)
        val destination = Point.fromLngLat(destLng, destLat)

        mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
            mapStyle = style  // save for RouteProgressObserver

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
                        routerOrigin: String         // v3 uses @RouterOrigin String, not RouterOrigin object
                    ) {
                        mapboxNavigation.setNavigationRoutes(routes)

                        // Draw the route line — previously commented out
                        routeLineApi.setNavigationRoutes(routes) { value ->
                            routeLineView.renderRouteDrawData(style, value)
                        }

                        Log.d("Nav", "Route ready: ${routes.size} route(s)")
                    }

                    override fun onFailure(
                        reasons: List<RouterFailure>,
                        routeOptions: RouteOptions
                    ) {
                        Log.e("Nav", "Route failed: $reasons")
                    }

                    override fun onCanceled(
                        routeOptions: RouteOptions,
                        routerOrigin: String         // same here — String not RouterOrigin
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
}