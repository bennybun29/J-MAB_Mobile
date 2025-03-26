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
    private var isConnecting = false
    private const val webSocketUrl = "ws://10.0.2.2:8080"
    private var userId: Int = -1
    private val handler = Handler(Looper.getMainLooper())
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    var messageListener: ((Message) -> Unit)? = null  // Callback for new messages

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Synchronized
    fun connect(userId: Int) {
        Log.d("WebSocket", "connect called for userId: $userId, isConnected: $isConnected, isConnecting: $isConnecting")
        if (isConnected || isConnecting) {
            Log.d("WebSocket", "Already connected or connecting, skipping new connection")
            return
        }

        if (userId == -1) {
            Log.e("WebSocket", "Invalid user ID")
            return
        }

        // Close any existing connection to ensure only one is active
        if (webSocket != null) {
            Log.d("WebSocket", "Closing existing WebSocket before new connection")
            webSocket?.close(1000, "Replacing old connection")
            webSocket = null
        }

        isConnecting = true
        this.userId = userId

        val request = Request.Builder().url(webSocketUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                isConnected = true
                isConnecting = false
                reconnectAttempts = 0 // Reset on successful connection
                Log.d("WebSocket", "Connection opened for userId: $userId")
                val authMessage = JSONObject().apply {
                    put("userId", userId)
                }.toString()
                webSocket.send(authMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                try {
                    val messageJson = JSONObject(text)
                    if (messageJson.has("type") && messageJson.getString("type") == "auth") {
                        return
                    }
                    val message = Message(
                        id = messageJson.optInt("id"),
                        sender_id = messageJson.optInt("sender_id"),
                        receiver_id = messageJson.optInt("receiver_id"),
                        message = messageJson.optString("message"),
                        timestamp = messageJson.optString("timestamp"),
                        status = messageJson.optString("status"),
                        is_read = messageJson.optInt("is_read", 0)
                    )
                    handler.post { // Post to main thread
                        messageListener?.invoke(message)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e("WebSocket", "Connection failed: ${t.message}")
                isConnected = false
                isConnecting = false
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.d("WebSocket", "Connection closed: $reason")
                isConnected = false
                isConnecting = false
                if (code != 1000) { // Reconnect if not a normal closure
                    scheduleReconnect()
                }
            }
        })
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.e("WebSocket", "Max reconnect attempts reached for userId: $userId")
            return
        }

        val delay = (5000L * (reconnectAttempts + 1)) // Exponential backoff: 5s, 10s, 15s, etc.
        reconnectAttempts++
        Log.d("WebSocket", "Scheduling reconnect for userId: $userId, attempt $reconnectAttempts, delay: $delay ms")
        handler.postDelayed({
            connect(userId)
        }, delay)
    }

    fun reconnectIfNeeded() {
        if (!isConnected && !isConnecting && userId != -1) {
            Log.d("WebSocket", "Reconnecting as connection is down for userId: $userId")
            connect(userId)
        }
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

        if (webSocket != null && isConnected) {
            webSocket?.send(messageJson)
            Log.d("WebSocket", "Message sent: $messageJson")
        } else {
            Log.w("WebSocket", "Cannot send message, WebSocket not connected")
            reconnectIfNeeded() // Attempt to reconnect if needed
        }
    }

    fun closeConnection() {
        Log.d("WebSocket", "Closing WebSocket connection")
        webSocket?.close(1000, "Closing WebSocket")
        webSocket = null
        isConnected = false
        isConnecting = false
        reconnectAttempts = 0
        handler.removeCallbacksAndMessages(null) // Cancel any pending reconnects
    }

    fun reset() {
        Log.d("WebSocket", "Resetting WebSocketManager")
        closeConnection()
        webSocket = null
        isConnected = false
        isConnecting = false
        userId = -1
        messageListener = null
    }
}

object NotificationWebSocketManager {
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private const val webSocketUrl = "ws://10.0.2.2:8081" // Use the notification-specific port
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