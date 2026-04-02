package com.example.safemeal.data.restaurant

import android.util.Log
import com.example.safemeal.AppwriteManger
import com.example.safemeal.AppwriteManger.AppwriteManager.databases
import com.example.safemeal.data.reviews.Review
import com.example.safemeal.data.reviews.ReviewRepository
import io.appwrite.Query
import io.appwrite.services.Storage

object RestaurantRepository {
    private const val DATABASE_ID = "69a59507000db58c7721"
    private const val COLLECTION_ID = "verified_restaurants"

    private const val BUCKET_ID = "safemealimg"

    suspend fun getRestaurantsByTag(tag: String? = null): List<Restaurant> {
        return try {
            val queries = mutableListOf<String>()

            // If a user selects 'Sattvic', only fetch those
            if (tag != null) {
                queries.add(Query.contains("tags", tag))
            }

            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                queries = queries
            )

            response.documents.map { doc ->
                Restaurant.from(doc.data, doc.id)
            }
        } catch (e: Exception) {
            Log.e("Repository", "Fetch failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserDietaryPreference(): String? {
        return try {
            val prefs = AppwriteManger.AppwriteManager.account.getPrefs()

            val choice = prefs.data["dietary_choice"] as? String

            Log.d("Repository", "Prefs found: $choice")
            choice
        } catch (e: Exception) {
            Log.e("Repository", "Failed to get prefs: ${e.message}")
            null
        }
    }

}