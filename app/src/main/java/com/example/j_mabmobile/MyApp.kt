package com.example.j_mabmobile

import android.app.Application
import com.example.j_mabmobile.api.NotificationWebSocketManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize WebSocket connection
        val sharedPreferences = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            NotificationWebSocketManager.connect(userId)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        NotificationWebSocketManager.closeConnection()
    }
}