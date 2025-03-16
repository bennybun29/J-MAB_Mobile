package com.example.j_mabmobile.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.j_mabmobile.model.Message
import com.example.j_mabmobile.model.Notification
import okhttp3.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object MessageWebSocketManager {
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private const val webSocketUrl = "ws://192.168.100.27:8080"
    private var userId: Int = -1

    var messageListener: ((Message) -> Unit)? = null  // Callback for new messages

    fun connect(userId: Int) {
        if (isConnected) return
        this.userId = userId

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder().url(webSocketUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                isConnected = true
                Log.d("WebSocket", "Connection opened for userId: $userId")
                val authMessage = JSONObject().apply {
                    put("userId", userId)
                }.toString()
                webSocket.send(authMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text");
                try {
                    val messageJson = JSONObject(text);
                    if (messageJson.has("type") && messageJson.getString("type") == "auth") {
                        return;
                    }
                    val message = Message(
                        id = messageJson.optInt("id"),
                        sender_id = messageJson.optInt("sender_id"),
                        receiver_id = messageJson.optInt("receiver_id"),
                        message = messageJson.optString("message"),
                        timestamp = messageJson.optString("timestamp"),
                        status = messageJson.optString("status"),
                        is_read = messageJson.optInt("is_read", 0)
                    );
                    messageListener?.invoke(message);
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message: ${e.message}");
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e("WebSocket", "Connection failed: ${t.message}")
                isConnected = false

                // Reconnect after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    connect(userId)
                }, 5000)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.d("WebSocket", "Connection closed: $reason")
                isConnected = false

                // Reconnect if the connection was closed unexpectedly
                if (code != 1000) { // 1000 is a normal closure
                    Handler(Looper.getMainLooper()).postDelayed({
                        connect(userId)
                    }, 5000)
                }
            }
        })
    }

    fun sendMessage(text: String, userId: Int, receiverId: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val messageJson = JSONObject().apply {
            put("sender_id", userId)
            put("receiver_id", receiverId)
            put("message", text)
            put("timestamp", timestamp)
            put("status", "delivered")
            put("is_read", 0)
        }.toString()

        webSocket?.send(messageJson)
    }

    fun closeConnection() {
        webSocket?.close(1000, "Closing WebSocket")
        webSocket = null
        isConnected = false
    }

    fun reset() {
        closeConnection()
        webSocket = null
        isConnected = false
        userId = -1
        messageListener = null
    }
}

object NotificationWebSocketManager {
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private const val webSocketUrl = "ws://192.168.100.27:8081" // Use the notification-specific port
    private var userId: Int = -1
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val handler = Handler(Looper.getMainLooper())
    private var reconnectAttempts = 0

    var notificationListener: ((Notification) -> Unit)? = null  // Callback for new notifications

    fun connect(userId: Int) {
        if (isConnected) return // Prevent multiple connections
        if (userId == -1) {
            Log.e("WebSocket", "Invalid user ID")
            return
        }

        this.userId = userId

        val request = Request.Builder().url("$webSocketUrl/notifications?userId=$userId").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                reconnectAttempts = 0 // Reset attempts on successful connection
                Log.d("WebSocket", "Connected for userId: $userId")

                val authMessage = JSONObject().apply {
                    put("type", "auth")
                    put("userId", userId)
                }.toString()
                webSocket.send(authMessage) // Send authentication
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received: $text")
                try {
                    val notificationJson = JSONObject(text)
                    if (notificationJson.optString("type") == "auth") {
                        Log.d("WebSocket", "Authentication successful")
                        return
                    }

                    val notification = Notification(
                        id = notificationJson.optInt("id", 0),
                        user_id = notificationJson.optInt("user_id"),
                        title = notificationJson.optString("title"),
                        message = notificationJson.optString("message"),
                        is_read = notificationJson.optInt("is_read", 0),
                        created_at = notificationJson.optString("created_at",
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(Date()))
                    )

                    // Post to main thread to avoid threading issues
                    handler.post {
                        notificationListener?.invoke(notification)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing notification: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WebSocket", "Connection failed: ${t.message}")
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("WebSocket", "Closed: $reason")
                if (code != 1000) { // 1000 means normal closure
                    scheduleReconnect()
                }
            }
        })
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= 5) {
            Log.e("WebSocket", "Max reconnect attempts reached.")
            return
        }

        val delay = (5000L * (reconnectAttempts + 1)) // Exponential backoff
        reconnectAttempts++
        handler.postDelayed({
            Log.d("WebSocket", "Reconnecting... Attempt $reconnectAttempts")
            connect(userId)
        }, delay)
    }

    fun reconnectIfNeeded() {
        if (!isConnected && userId != -1) {
            connect(userId)
        }
    }

    fun closeConnection() {
        webSocket?.close(1000, "Closing WebSocket")
        webSocket = null
        isConnected = false
        reconnectAttempts = 0
        handler.removeCallbacksAndMessages(null) // Cancel any pending reconnects
    }
}