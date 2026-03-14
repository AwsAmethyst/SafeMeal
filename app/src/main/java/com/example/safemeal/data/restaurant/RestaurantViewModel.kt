package com.example.safemeal.data.restaurant

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemeal.data.reviews.ReviewRepository
import kotlinx.coroutines.launch

private const val DATABASE_ID = "69a59507000db58c7721"
private const val COLLECTION_ID = "verified_restaurants"

private const val BUCKET_ID = "safemealimg"
class RestaurantViewModel : ViewModel() {
    var restaurants by mutableStateOf<List<Restaurant>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // New State: Store the user's preferred diet (e.g., "Sattvic")
    var userDietaryChoice by mutableStateOf<String?>(null)
        private set

    fun loadPersonalizedContent(userId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Fetch the user's specific dietary choice from the repository
                val preference = RestaurantRepository.getUserDietaryPreference()
                userDietaryChoice = preference

                // 2. Fetch restaurants matching that specific preference
                // If preference is null, it will fetch all (or you can handle as empty)
                restaurants = RestaurantRepository.getRestaurantsByTag(preference)

            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to load personalized content: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Keep your standard fetch for manual searches/overrides
    /*fun fetchRestaurants(tag: String? = null) {
        viewModelScope.launch {
            isLoading = true
            restaurants = RestaurantRepository.getRestaurantsByTag(tag)
            isLoading = false
        }
    }*/
    suspend fun fetchRestaurants(tag: String? = null): List<Restaurant> {
        isLoading = true
        val result = RestaurantRepository.getRestaurantsByTag(tag)
        restaurants = result
        isLoading = false
        return result // Return the list so we can use it immediately
    }
/*
    fun seedData() {
        viewModelScope.launch {
            RestaurantRepository.seedDatabase()
        }
    }
*/
    var selectedRestaurantForDetails by mutableStateOf<Restaurant?>(null)

    // Optional: A helper function to set it
    fun selectRestaurant(restaurant: Restaurant) {
        selectedRestaurantForDetails = restaurant
    }
    // Inside RestaurantViewModel.kt

    // 1. Create the state that the Dashboard will observe
    var hotRestaurants by mutableStateOf<List<Restaurant>>(emptyList())
        private set // Only the ViewModel can change this list

    /*fun fetchHotRestaurants(userChoice: String) {
        viewModelScope.launch {
            if (restaurants.isEmpty()) {
                fetchRestaurants()
            }
            val currentRestaurants = restaurants
            try {
                // Step 1: Get the current list of restaurants (already fetched by fetchRestaurants())
                val baseList = restaurants

                // Step 2: Enrich the list with ratings from the ReviewRepository
                // We use .map to go through every restaurant and "attach" its average rating
                val enrichedList = baseList.map { restaurant ->
                    val summary = com.example.safemeal.data.reviews.ReviewRepository.getRatingSummary(restaurant.id)

                    // Use .copy() to fill the 'rating' slot you just added to the data class
                    restaurant.copy(rating = summary.average)
                }

                // Step 3: Apply your 3 Sorting Layers
                hotRestaurants = enrichedList
                    .filter { restaurant ->
                        // LAYER 1: Filter by user preference (Sattvic, Vegan, etc.)
                        if (userChoice.isNotEmpty()) {
                            restaurant.tags?.contains(userChoice, ignoreCase = true) == true
                        } else true
                    }
                    .sortedWith(
                        // LAYER 2: Highest Average Rating first
                        compareByDescending<Restaurant> { it.rating }
                            // LAYER 3: Tie-breaker (Recency using ID)
                            .thenByDescending { it.id }
                    )
                    .take(5) // Only show the top 5 "Hot" spots

            } catch (e: Exception) {
                Log.e("RestaurantVM", "Failed to sort hot restaurants: ${e.message}")
            }
        }
    }*/
    fun fetchHotRestaurants(userChoice: String) {
        viewModelScope.launch {
            try {
                // LAYER 0: Fetch and WAIT.
                // If the current list is empty, we wait for the network to finish.
                val baseList = restaurants.ifEmpty {
                    fetchRestaurants() // This now waits for the return!
                }

                if (baseList.isEmpty()) return@launch

                // Step 1: Enrich with Ratings
                val enrichedList = baseList.map { restaurant ->
                    val summary = ReviewRepository.getRatingSummary(restaurant.id)
                    restaurant.copy(rating = summary.average)
                }

                // Step 2: Apply Sorting Layers
                val sortedResult = enrichedList
                    .filter { restaurant ->
                        if (userChoice.isNotEmpty()) {
                            restaurant.tags?.contains(userChoice, ignoreCase = true) == true
                        } else true
                    }
                    .sortedWith(
                        compareByDescending<Restaurant> { it.rating }
                            .thenByDescending { it.id }
                    )
                    .take(5)

                // Step 3: Update state to trigger UI
                hotRestaurants = sortedResult

            } catch (e: Exception) {
                Log.e("RestaurantVM", "Failed to sort: ${e.message}")
            }
        }
    }
}