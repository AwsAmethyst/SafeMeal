package com.example.safemeal

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RestaurantViewModel : ViewModel() {
    var restaurants by mutableStateOf<List<Restaurant>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // New State: Store the user's preferred diet (e.g., "Sattvic")
    var userDietaryChoice by mutableStateOf<String?>(null)
        private set

    /**
     * FR3: Personalized Fetch
     * This first gets the user's preference, then filters the map pins.
     */
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
    fun fetchRestaurants(tag: String? = null) {
        viewModelScope.launch {
            isLoading = true
            restaurants = RestaurantRepository.getRestaurantsByTag(tag)
            isLoading = false
        }
    }
/*
    fun seedData() {
        viewModelScope.launch {
            RestaurantRepository.seedDatabase()
        }
    }
*/
    var selectedRestaurantForDetails by mutableStateOf<com.example.safemeal.Restaurant?>(null)

    // Optional: A helper function to set it
    fun selectRestaurant(restaurant: com.example.safemeal.Restaurant) {
        selectedRestaurantForDetails = restaurant
    }
}