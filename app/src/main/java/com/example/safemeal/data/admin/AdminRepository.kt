package com.example.safemeal.data.admin

import android.util.Log
import com.example.safemeal.AppwriteManger
import io.appwrite.Query
import java.util.*

object AdminRepository {
    private const val DATABASE_ID = "69a59507000db58c7721"
    private const val RESTAURANTS_COL = "verified_restaurants" // Replace with your UID
    private const val REVIEWS_COL = "reviews"

    data class AdminStats(
        val totalRestaurants: Int,
        val totalReviews: Int,
        val totalUsers: Int,
        val restaurantGrowth: List<Pair<String, Int>>,
        val dietaryData: List<Pair<String, Float>>,
        val userGrowth: List<Pair<String, Int>>

    )

    suspend fun fetchDashboardStats(): AdminStats? {
        return try {
            val db = AppwriteManger.AppwriteManager.databases


            // 1. Fetch all restaurant documents
            val restaurantRes = db.listDocuments(DATABASE_ID, RESTAURANTS_COL, queries = listOf(Query.limit(5000)))
            val reviewRes = db.listDocuments(DATABASE_ID, REVIEWS_COL, queries = listOf(Query.limit(5000)))
            val reviews = reviewRes.documents
            // 2. Calculate Growth Trend (Last 3 Months)
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val calendar = Calendar.getInstance()
            val currentMonthIdx = calendar.get(Calendar.MONTH)

            val window = mutableListOf<String>()
            for (i in 2 downTo 0) {
                val idx = (currentMonthIdx - i + 12) % 12
                window.add(months[idx])
            }

            val growthMap = window.associateWith { 0 }.toMutableMap()
            restaurantRes.documents.forEach { doc ->
                val date = doc.createdAt // "2026-03-24..."
                val mIndex = date.substring(5, 7).toInt() - 1
                val mName = months[mIndex]
                if (growthMap.containsKey(mName)) {
                    growthMap[mName] = growthMap[mName]!! + 1
                }
            }
// 2. Map every unique user to their first contribution month
            val userFirstSeen = mutableMapOf<String, Int>() // userId -> monthIndex
            reviews.forEach { doc ->
                // CHANGE: Use "user_id" to match your Appwrite console screenshot
                val userId = doc.data["user_id"]?.toString() ?: ""
                val createdAt = doc.createdAt // e.g., "2026-03-24..."

                // Debug log to confirm we are catching the IDs
                Log.d("AdminStats", "Found Review by User: $userId at $createdAt")

                if (userId.isNotEmpty()) {
                    val monthIndex = createdAt.substring(5, 7).toIntOrNull()?.minus(1) ?: -1
                    if (monthIndex != -1) {
                        val existing = userFirstSeen[userId]
                        if (existing == null || monthIndex < existing) {
                            userFirstSeen[userId] = monthIndex
                        }
                    }
                }
            }

// 3. Convert that map into the graph format (Cumulative Growth)
            var runningTotal = 0
            val userGrowthData = window.map { monthName ->
                val monthIndex = months.indexOf(monthName)
                val newUsersInMonth = userFirstSeen.values.count { it == monthIndex }
                runningTotal += newUsersInMonth
                // Ensure we show at least 1 (the Admin) if the database is empty
                monthName to runningTotal.coerceAtLeast(1)

            }
            Log.e("User", "User:$runningTotal")
            // 3. Calculate Dietary Percentages
            val restaurants = restaurantRes.documents
            val total = restaurants.size.toFloat().coerceAtLeast(1f)

            val halal = restaurants.count {
                it.data["tags"]?.toString()?.contains("Halal", ignoreCase = true) == true
            }
            val sattvic = restaurants.count {
                it.data["tags"]?.toString()?.contains("Sattvic", ignoreCase = true) == true
            }
// 1. CHANGE: Count Kosher instead of Vegan
            val kosher = restaurants.count {
                it.data["tags"]?.toString()?.contains("Kosher", ignoreCase = true) == true
            }

            val dietaryData = listOf(
                "Halal" to (halal / total * 100f),
                "Sattvic" to (sattvic / total * 100f),
                "Kosher" to (kosher / total * 100f) // 2. CHANGE: Label as Kosher
            )
            AdminStats(
                totalRestaurants = restaurantRes.total.toInt(),
                totalReviews = reviewRes.total.toInt(),
                restaurantGrowth = window.map { it to (growthMap[it] ?: 0) },
                dietaryData = dietaryData,
                totalUsers = runningTotal.coerceAtLeast(1),
                userGrowth = userGrowthData
            )
        } catch (e: Exception) {
            Log.e("AdminRepo", "Fetch Error: ${e.message}")
            null
        }
    }
}