package com.example.j_mabmobile

import android.app.Application
import com.example.j_mabmobile.api.MessageWebSocketManager
import com.example.j_mabmobile.api.NotificationWebSocketManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize WebSocket connections
        val sharedPreferences = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            MessageWebSocketManager.connect(userId)      // Connect to ChatServer
            NotificationWebSocketManager.connect(userId) // Connect to NotificationServer
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        MessageWebSocketManager.closeConnection()      // Close ChatServer connection
        NotificationWebSocketManager.closeConnection() // Close NotificationServer connection
    }
}