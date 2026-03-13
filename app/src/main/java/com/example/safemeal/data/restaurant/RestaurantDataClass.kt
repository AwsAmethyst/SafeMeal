package com.example.safemeal.data.restaurant

import android.util.Log
import com.example.safemeal.screens.MenuItem
import org.json.JSONArray

data class Restaurant(
    val id: String,
    val name: String,
    val address: String?,
    val hours: String?,
    val tags: String?,
    val longitude: Double,
    val latitude: Double,
    val menuJson: String?,
    val imgid: String
) {

    fun getMenuItemsList(): List<MenuItem> {
        if (menuJson.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<MenuItem>()
        try {
            val array = JSONArray(menuJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                // Use optString to prevent crashes if a key is missing
                list.add(MenuItem(
                    name = obj.optString("name", "Unknown Item"),
                    description = obj.optString("desc", ""),
                    price = "KES ${obj.optString("price", "0")}"
                ))
            }
        } catch (e: Exception) {
            Log.e("MenuError", "Failed to parse menu for $name: ${e.message}")
             return emptyList()
        }
        return list
    }
    companion object {
        fun from(map: Map<String, Any>, documentId: String): Restaurant {
            val coordinates = map["location"] as? List<*>

            return Restaurant(
                id = documentId,
                name = map["name"] as? String ?: "Unknown",
                address = map["address"] as? String,
                hours = map["hours"] as? String,
                tags = (map["tags"] as? String),
                longitude = (coordinates?.get(0) as? Number)?.toDouble() ?: 0.0,
                latitude = (coordinates?.get(1) as? Number)?.toDouble() ?: 0.0,
                menuJson = map["menuItems"] as? String,
                imgid = map["imgId"] as String
            )
        }
    }
}