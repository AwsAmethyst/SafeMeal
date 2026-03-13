package com.example.safemeal.data.restaurant

import android.util.Log
import com.example.safemeal.AppwriteManger
import com.example.safemeal.AppwriteManger.AppwriteManager.databases
import com.example.safemeal.data.reviews.Review
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

    class ImageService(private val storage: Storage) {
        suspend fun getRestaurantImageUrl(fileId: String): String {
            // Generates a URL for the image in your 'restaurant_images' bucket
            return storage.getFileView(
                bucketId = BUCKET_ID,
                fileId = fileId
            ).toString()
        }
    }

    suspend fun updateRestaurantImage(
        restaurantId: String,
        newImageId: String
    ): Boolean {
        return try {
            databases.updateDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID, // Matches RESTAURANT table
                documentId = restaurantId,
                data = mapOf(
                    "image_id" to newImageId // The file ID from your 'restaurant_photos' bucket
                )
            )
            true
        } catch (e: Exception) {
            Log.e("AppwriteUpdate", "Error updating image ID: ${e.message}")
            false
        }
    }

    suspend fun getReviewsForRestaurant(restaurantId: String): List<Review> {
        return try {
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = "reviews",
                queries = listOf(
                    io.appwrite.Query.equal("restaurant_id", restaurantId),
                    io.appwrite.Query.orderDesc("\$createdAt") // Show newest first
                )
            )
            response.documents.map { Review.from(it.data, it.id) }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to fetch reviews: ${e.message}")
            emptyList()
        }
    }
}