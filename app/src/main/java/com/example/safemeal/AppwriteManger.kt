package com.example.safemeal

import android.content.Context
import android.util.Log
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.launch


object AppwriteManger {
    object AppwriteManager {
        private lateinit var client: Client
        lateinit var account: Account
        lateinit var databases: Databases
        lateinit var storage: Storage


        fun init(context: Context) {
            client = Client(context)
                .setEndpoint("https://fra.cloud.appwrite.io/v1") // Replace with your endpoint
                .setProject("safemealapp") // Replace with your Project ID

            account = Account(client)
            databases = Databases(client)
            storage = Storage(client)

            kotlinx.coroutines.GlobalScope.launch {
                try {
                    // Fetching a public resource or server info
                    // If this fails, your Endpoint or Project ID is definitely wrong
                    val response = account.get()
                    android.util.Log.d("AppwriteInit", "Handshake successful: $response")
                } catch (e: Exception) {
                    android.util.Log.e("AppwriteInit", "Handshake failed: ${e.message}")
                }
            }
        }
        fun getUserProfilePictureUrl(userId: String): String {
            // Replace "YOUR_PFP_BUCKET_ID" with your actual bucket ID from Appwrite Console
            val bucketId = "safemealimg"
            val projectId = "safemealapp" // Get this from your main config
            val timestamp = System.currentTimeMillis()
            // This forms the standard Appwrite URL for getting a file view
            return "https://cloud.appwrite.io/v1/storage/buckets/$bucketId/files/$userId/view?project=$projectId&t=$timestamp"
        }
        val PFP_BUCKET_ID = "safemealimg" // Update this!

        suspend fun updateProfilePicture(context: android.content.Context, imageUri: android.net.Uri): String? {
            return try {
                val user = AppwriteManger.AppwriteManager.account.get()
                val userId = user.id

                // 1. Attempt to delete existing image (fails silently if none exists)
                try {
                    AppwriteManger.AppwriteManager.storage.deleteFile(
                        bucketId = PFP_BUCKET_ID,
                        fileId = userId
                    )
                } catch (e: Exception) {
                    // No previous image found, which is fine
                    Log.d("PFP", "No old image to delete")
                }

                // 2. Prepare the new file from Uri
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val fileBytes = inputStream?.readBytes() ?: return null
                inputStream.close()

                val file = io.appwrite.models.InputFile.fromBytes(
                    bytes = fileBytes,
                    filename = "$userId.jpg",
                    mimeType = "image/jpeg"
                )

                // 3. Upload with fileId = userId
                val result = AppwriteManger.AppwriteManager.storage.createFile(
                    bucketId = PFP_BUCKET_ID,
                    fileId = userId, // This makes the ID the UserId
                    file = file
                )

                result.id // Return the new ID
            } catch (e: Exception) {
                Log.e("PFP_Error", "Update failed: ${e.message}")
                null
            }
        }
    }
}
