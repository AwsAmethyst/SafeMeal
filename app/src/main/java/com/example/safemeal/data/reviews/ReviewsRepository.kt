package com.example.safemeal.data.reviews

import android.util.Log
import com.example.safemeal.AppwriteManger
import com.example.safemeal.data.restaurant.Restaurant
import io.appwrite.ID
import io.appwrite.Query

object ReviewRepository {
    private const val DATABASE_ID = "69a59507000db58c7721"
    private const val COLLECTION_ID = "reviews"

    suspend fun addReview(restaurantId: String, rating: Int, comment: String,restaurantName: String): String {
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
                    "comment" to comment,
                    "restaurant_name" to restaurantName
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

    data class RatingSummary(
        val average: Double,
        val count: Int
    )

    suspend fun getRatingSummary(restaurantId: String): RatingSummary {
        return try {
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = "reviews",
                queries = listOf(
                    io.appwrite.Query.equal("restaurant_id", restaurantId)
                )
            )

            val ratings = response.documents.map {
                (it.data["rating"] as? Number)?.toDouble() ?: 0.0
            }

            if (ratings.isEmpty()) {
                RatingSummary(0.0, 0)
            } else {
                RatingSummary(
                    average = ratings.average(),
                    count = ratings.size
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepo", "Error calculating summary: ${e.message}")
            RatingSummary(0.0, 0)
        }
    }
    suspend fun getUserReviewCount(userId: String): Int {
        return try {
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                queries = listOf(
                    io.appwrite.Query.equal("user_id", userId)
                )
            )
            // .total is a Long in Appwrite, so we convert it to Int
            response.total.toInt()
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepo", "Error fetching user review count: ${e.message}")
            0 // Return 0 if something fails
        }
    }
    suspend fun getUserRecentReviews(userId: String, limit: Int = 3): List<com.example.safemeal.data.reviews.Review> {
        return try {
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = "reviews",
                queries = listOf(
                    io.appwrite.Query.equal("user_id", userId),
                    io.appwrite.Query.orderDesc("\$createdAt"),
                    io.appwrite.Query.limit(limit)
                )
            )
            response.documents.map { com.example.safemeal.data.reviews.Review.from(it.data, it.id) }
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error fetching recent activity: ${e.message}")
            emptyList()
        }
    }
    suspend fun fetchUserReviews(): List<com.example.safemeal.data.reviews.Review> {
        return try {
            val user = AppwriteManger.AppwriteManager.account.get()
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                queries = listOf(
                    Query.equal("user_id", user.id), // Matches your underscore naming
                    Query.orderDesc("\$createdAt")
                )
            )
            response.documents.map { com.example.safemeal.data.reviews.Review.from(it.data, it.id) }
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error fetching user activity: ${e.message}")
            emptyList()
        }
    }


    suspend fun updateReview(reviewId: String, newRating: Int, newComment: String): String {
        return try {
            AppwriteManger.AppwriteManager.databases.updateDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = reviewId,
                data = mapOf(
                    "rating" to newRating,
                    "comment" to newComment
                )
            )
            "SUCCESS"
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Update failed: ${e.message}")
            "ERROR"
        }
    }

    suspend fun deleteReview(reviewId: String): String {
        return try {
            AppwriteManger.AppwriteManager.databases.deleteDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = reviewId
            )
            "SUCCESS"
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Delete failed: ${e.message}")
            "ERROR"
        }
    }
    suspend fun getMonthlyReviewStats(): List<Pair<String, Int>> {
        return try {
            val user = AppwriteManger.AppwriteManager.account.get()
            val response = AppwriteManger.AppwriteManager.databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = "reviews",
                queries = listOf(io.appwrite.Query.equal("user_id", user.id))
            )

            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val calendar = java.util.Calendar.getInstance()
            val currentMonthIdx = calendar.get(java.util.Calendar.MONTH)

            // Generate the 3-month window (e.g., if current is Mar, window is [Jan, Feb, Mar])
            val lastThreeMonths = mutableListOf<String>()
            for (i in 2 downTo 0) {
                val idx = (currentMonthIdx - i + 12) % 12
                lastThreeMonths.add(months[idx])
            }

            // Initialize counts to zero for these specific months
            val statsMap = lastThreeMonths.associateWith { 0 }.toMutableMap()

            response.documents.forEach { doc ->
                val dateStr = doc.createdAt // "2026-03-24..."
                val monthIndex = dateStr.substring(5, 7).toInt() - 1
                val monthName = months[monthIndex]

                if (statsMap.containsKey(monthName)) {
                    statsMap[monthName] = statsMap[monthName]!! + 1
                }
            }

            // Return the list in the correct chronological order
            lastThreeMonths.map { it to (statsMap[it] ?: 0) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}