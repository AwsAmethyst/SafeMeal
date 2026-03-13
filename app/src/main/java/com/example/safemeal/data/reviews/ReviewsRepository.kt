package com.example.safemeal.data.reviews

import android.util.Log
import com.example.safemeal.AppwriteManger
import io.appwrite.ID
import io.appwrite.Query

object ReviewRepository {
    private const val DATABASE_ID = "69a59507000db58c7721"
    private const val COLLECTION_ID = "reviews"

    suspend fun addReview(restaurantId: String, rating: Int, comment: String): String {
        return try {
            val user = AppwriteManger.AppwriteManager.account.get()

            // 1. Check if the user has already reviewed THIS restaurant
            val existingReviews = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = "reviews",
                queries = listOf(
                    Query.equal("user_id", user.id),
                    Query.equal("restaurant_id", restaurantId)
                )
            )

            // 2. If the list is not empty, block the new review
            if (existingReviews.total > 0) {
                return "ALREADY_REVIEWED"
            }

            // 3. If no review exists, proceed to create
            AppwriteManger.AppwriteManager.databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = "reviews",
                documentId = io.appwrite.ID.unique(),
                data = mapOf(
                    "restaurant_id" to restaurantId,
                    "user_id" to user.id,
                    "user_name" to user.name,
                    "rating" to rating,
                    "comment" to comment
                )
            )
            "SUCCESS"
        } catch (e: Exception) {
            "ERROR"
        }
    }

    suspend fun fetchReviews(restaurantId: String): List<Review> {
        return try {
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                queries = listOf(
                    Query.equal("restaurant_id", restaurantId),
                    Query.orderDesc("\$createdAt")
                )
            )
            response.documents.map { Review.from(it.data, it.id) }
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error fetching reviews: ${e.message}")
            emptyList()
        }
    }
    // In ReviewRepository.kt
    suspend fun hasUserReviewed(restaurantId: String): Boolean {
        return try {
            val user = AppwriteManger.AppwriteManager.account.get()
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = "69a59507000db58c7721",
                collectionId = "reviews",
                queries = listOf(
                    Query.equal("user_id", user.id),
                    Query.equal("restaurant_id", restaurantId)
                )
            )
            response.total > 0
        } catch (e: Exception) {
            false
        }
    }
}