package com.example.j_mabmobile.api

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.j_mabmobile.MainActivity
import com.example.j_mabmobile.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import com.example.j_mabmobile.model.Notification

class WebSocketService : Service() {

    companion object {
        private const val FOREGROUND_CHANNEL_ID = "websocket_service_channel"
        private const val NOTIFICATION_CHANNEL_ID = "websocket_notifications"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        val sharedPreferences = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            NotificationWebSocketManager.reconnectIfNeeded()
            NotificationWebSocketManager.notificationListener = { notification ->
                showNotification(notification)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Make this service run in the foreground to prevent it from being killed
        val notification = NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("App is running")
            .setContentText("Maintaining connection to server")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSmallIcon(R.drawable.bell_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create low importance channel for foreground service
            val serviceChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Background Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps the app connected to receive notifications"
                setShowBadge(false)
            }

            // Create high importance channel for actual notifications
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Important Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "App notifications that require immediate attention"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setShowBadge(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(notificationChannel)
        }
    }

    private fun showNotification(notification: Notification) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data to open the right screen when notification is tapped
            putExtra("OPEN_FRAGMENT", "NOTIFICATION_FRAGMENT")
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notification.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.bell_icon)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@WebSocketService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notification.id, builder.build())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // No need to close connection here as MyApp will handle it
    }
}