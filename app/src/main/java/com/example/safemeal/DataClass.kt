package com.example.safemeal

import android.util.Log
import com.example.safemeal.screens.MenuItem
import org.json.JSONArray

data class Restaurant(
    val id: String,          // The unique document ID from Appwrite
    val name: String,        // Matches your 'name' attribute
    val address: String?,    // Matches your 'address' attribute (optional)
    val hours: String?,      // Matches your 'hours' attribute (optional)
    val tags: String?,  // FR3: Matches your 'tags' Enum Array
    val longitude: Double,   // Extracted from the 'location' Point
    val latitude: Double,     // Extracted from the 'location' Point
    val menuJson: String?
) {
    // Helper to convert the JSON string into a list of MenuItems
    fun getMenuItems(): List<MenuItem> {
        if (menuJson.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<MenuItem>()
        val array = JSONArray(menuJson)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(MenuItem(
                obj.getString("name"),
                obj.getString("desc"),
                "KES ${obj.getString("price")}"
            ))
        }
        return list
    }
    companion object {
        fun from(map: Map<String, Any>, documentId: String): Restaurant {
            // 1. Get the raw object without casting
            /*val rawLocation = map["location"]

            // 2. Print the Class Name to the Console
            if (rawLocation != null) {
                Log.d("SafeMealDebug", "Location Key Type: ${rawLocation::class.java.simpleName}")
                Log.d("SafeMealDebug", "Location Raw Value: $rawLocation")
            } else {
                Log.e("SafeMealDebug", "Location key is NULL for document: $documentId")
            }*/
            // Extract the 'location' Point data
            //val location = map["location"] as? Map<*, *>
            //val coordinates = location?.get("coordinates") as? List<*>
            val coordinates = map["location"] as? List<*>

            return Restaurant(
                id = documentId,
                name = map["name"] as? String ?: "Unknown",
                address = map["address"] as? String,
                hours = map["hours"] as? String,
                // Ensure tags are handled as a List
                tags = (map["tags"] as? String),
                //longitude = (coordinates?.get(0) as? Number)?.toDouble() ?: 0.0,
                //latitude = (coordinates?.get(1) as? Number)?.toDouble() ?: 0.0
                longitude = (coordinates?.get(0) as? Number)?.toDouble() ?: 0.0,
                latitude = (coordinates?.get(1) as? Number)?.toDouble() ?: 0.0,
                menuJson = map["menuJson"] as? String
            )
        }
    }
}