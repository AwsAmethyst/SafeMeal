package com.example.safemeal.data.reviews

data class Review(
    val id: String,
    val restaurantId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String
) {
    companion object {
        fun from(map: Map<String, Any>, documentId: String): Review {
            return Review(
                id = documentId,
                restaurantId = map["restaurant_id"] as? String ?: "",
                userId = map["user_id"] as? String ?: "",
                userName = map["user_name"] as? String ?: "Anonymous",
                rating = (map["rating"] as? Number)?.toInt() ?: 0,
                comment = map["comment"] as? String ?: ""
            )
        }
    }
}