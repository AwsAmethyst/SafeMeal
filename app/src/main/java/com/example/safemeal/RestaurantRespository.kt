package com.example.safemeal

import android.util.Log
import io.appwrite.ID
import io.appwrite.Query

object RestaurantRepository {
    private const val DATABASE_ID = "69a59507000db58c7721"
    private const val COLLECTION_ID = "verified_restaurants"


    suspend fun getRestaurantsByTag(tag: String? = null): List<Restaurant> {
        return try {
            val queries = mutableListOf<String>()

            // If a user selects 'Sattvic', only fetch those
            if (tag != null) {
                queries.add(Query.contains("tags", tag))
            }

            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
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