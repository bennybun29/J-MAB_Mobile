package com.example.j_mabmobile

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.api.WebSocketManager
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.Message
import com.example.j_mabmobile.model.MessageRequest
import com.example.j_mabmobile.model.MessageResponse
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response as OkHttpResponse
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response as RetrofitResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MessagesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessagesAdapter
    private lateinit var etMessage: EditText
    private lateinit var apiService: ApiService
    private var userId: Int = -1 // Initialize with a default value
    private val messagesList = mutableListOf<Message>()
    private val receiverId = 1  // Admin ID
    private lateinit var noMessageIcon: ImageView
    private lateinit var startConvoTV: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        WebSocketManager.reset()

        // Initialize userId here, after the fragment is attached to a context
        userId = getUserIdFromPreferences()

        apiService = RetrofitClient.getApiService(requireContext())

        noMessageIcon = view.findViewById(R.id.noMessageIcon)
        startConvoTV = view.findViewById(R.id.startConvoTV)

        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        etMessage = view.findViewById(R.id.et_message)
        val btnSend = view.findViewById<ImageView>(R.id.btn_send)

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        // Initialize the adapter with an empty list
        adapter = MessagesAdapter(messagesList, userId)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null

        fetchMessages()  // Fetch existing messages initially
        setupWebSocketConnection()  // Set up WebSocket for real-time messages

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addLocalMessage(text)
                sendMessageViaWebSocket(text)  // Use WebSocket to send
                sendMessageViaApi(text)        // Fallback to API
                etMessage.text.clear()
            }
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true // Retain fragment instance during configuration changes
    }

    override fun onResume() {
        super.onResume()
        if (userId > 0) {
            WebSocketManager.connect(userId)
            setupWebSocketConnection()
        }
    }

    override fun onPause() {
        super.onPause()
        // Clear the message listener when the fragment is paused
        WebSocketManager.messageListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the message listener when the fragment is destroyed
        WebSocketManager.messageListener = null
    }

    private fun setupWebSocketConnection() {
        // Connect to WebSocket if not already connected
        WebSocketManager.connect(userId)

        Log.d("MessagesFragment", "Setting up WebSocket message listener")

        // Set up the message listener
        WebSocketManager.messageListener = { message ->
            Log.d("MessagesFragment", "New message received: $message")
            // Check if message is meant for this user
            if (message.receiver_id == userId) {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isAdded && !isDetached) { // Check if fragment is attached
                        Log.d("MessagesFragment", "Updating UI with new message")
                        // Check if message already exists to avoid duplicates
                        if (!messagesList.any { it.id == message.id }) {
                            messagesList.add(message)
                            // Update the adapter with the new messages list
                            adapter.updateMessages(messagesList)
                            recyclerView.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    private fun sendMessageViaWebSocket(text: String) {
        // Use the WebSocketManager to send messages
        WebSocketManager.sendMessage(text, userId, receiverId)
    }

    // Fallback to REST API if WebSocket fails
    private fun sendMessageViaApi(text: String) {
        val messageRequest = MessageRequest(userId, receiverId, text)

        apiService.sendMessage(messageRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: RetrofitResponse<ApiResponse>) {
                if (isAdded && !isDetached) { // Check if fragment is attached
                    if (!response.isSuccessful) {
                        Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                if (isAdded && !isDetached) { // Check if fragment is attached
                    Log.e("MessagesFragment", "Error sending message: ${t.message}")
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun addLocalMessage(text: String) {
        val tempMessage = Message(
            id = System.currentTimeMillis().toInt(), // Temporary ID that will be replaced when server confirms
            sender_id = userId,
            receiver_id = receiverId,
            message = text,
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            status = "sent",
            is_read = 0
        )

        messagesList.add(tempMessage)
        // Update the adapter with the new messages list
        adapter.updateMessages(messagesList)
        toggleEmptyState()
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun fetchMessages() {
        apiService.getMessages(userId).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: RetrofitResponse<MessageResponse>) {
                if (isAdded && !isDetached) { // Check if fragment is attached
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.messages?.let {
                            messagesList.clear()
                            messagesList.addAll(it.reversed())

                            // Update the adapter with the fetched messages
                            adapter.updateMessages(messagesList)
                            toggleEmptyState()

                            recyclerView.post {
                                if (adapter.itemCount > 0) {
                                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                                }
                            }
                        }
                    } else {
                        toggleEmptyState()
                    }
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                if (isAdded && !isDetached) { // Check if fragment is attached
                    Log.e("MessagesFragment", "Error fetching messages: ${t.message}")
                    Toast.makeText(requireContext(), "Error loading messages", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getUserIdFromPreferences(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1) // Default -1 if not found
    }

    private fun toggleEmptyState() {
        if (messagesList.isEmpty()) {
            noMessageIcon.visibility = View.VISIBLE
            startConvoTV.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noMessageIcon.visibility = View.GONE
            startConvoTV.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

}