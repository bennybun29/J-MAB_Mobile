package com.example.j_mabmobile

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.j_mabmobile.api.MessageWebSocketManager
import com.example.j_mabmobile.api.NotificationWebSocketManager
import com.example.j_mabmobile.model.Notification

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "websocket_notifications",
                "App Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications from WebSocket"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Initialize WebSocket connections
        val sharedPreferences = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            MessageWebSocketManager.connect(userId)
            NotificationWebSocketManager.connect(userId)
            NotificationWebSocketManager.notificationListener = { notification ->
                showNotification(notification)
            }
        }
    }

    private fun showNotification(notification: Notification) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "websocket_notifications")
            .setSmallIcon(R.drawable.bell_icon) // Create this icon in your drawable folder
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification
            if (ActivityCompat.checkSelfPermission(
                    this@MyApp,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notification.id, builder.build())
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        MessageWebSocketManager.closeConnection()      // Close ChatServer connection
        NotificationWebSocketManager.closeConnection() // Close NotificationServer connection
    }
}