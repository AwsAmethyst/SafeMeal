package com.example.safemeal

import android.content.Context
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
    }
}
