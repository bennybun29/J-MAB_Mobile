package com.example.j_mabmobile.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.j_mabmobile.model.Message
import okhttp3.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private const val webSocketUrl = "ws://10.0.2.2:8080"
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
                Log.d("WebSocket", "Connection opened")

                // Send authentication message
                val authMessage = JSONObject().apply {
                    put("userId", userId)
                }.toString()
                webSocket.send(authMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d("WebSocket", "Message received: $text")

                try {
                    val messageJson = JSONObject(text)
                    if (messageJson.has("type") && messageJson.getString("type") == "auth") {
                        // Ignore auth messages
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

                    // Notify the listener (MessagesFragment)
                    messageListener?.invoke(message)

                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message: ${e.message}")
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