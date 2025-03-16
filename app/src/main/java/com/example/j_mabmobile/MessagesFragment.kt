package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.NotificationWebSocketManager
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.api.MessageWebSocketManager
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.Message
import com.example.j_mabmobile.model.MessageRequest
import com.example.j_mabmobile.model.MessageResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessagesAdapter
    private lateinit var etMessage: EditText
    private lateinit var apiService: ApiService
    private var userId: Int = -1
    private val receiverId = 1 // Hardcoded to admin ID 1
    private val messagesList = mutableListOf<Message>()
    private lateinit var noMessageIcon: ImageView
    private lateinit var startConvoTV: TextView
    private var lastTempMessage: Message? = null // Track the last temporary message
    private var openedFromOrderInfo = false
    private var openedFromProductScreen = false
    private var sourceProductId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        openedFromOrderInfo = arguments?.getBoolean("FROM_ORDER_INFO", false) ?: false
        openedFromProductScreen = arguments?.getBoolean("FROM_PRODUCT_SCREEN", false) ?: false
        sourceProductId = arguments?.getString("PRODUCT_ID")

        val productName = arguments?.getString("PRODUCT_NAME")

        Log.d("MessagesFragment", "onCreate: fromOrderInfo=$openedFromOrderInfo, fromProductScreen=$openedFromProductScreen, productName=$productName")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        MessageWebSocketManager.reset()
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
        adapter = MessagesAdapter(messagesList, userId)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null

        fetchMessages()
        setupWebSocketConnection()

        val productName = arguments?.getString("PRODUCT_NAME")
        val fromProductScreen = arguments?.getBoolean("FROM_PRODUCT_SCREEN", false) ?: false

        Log.d("MessagesFragment", "onCreateView: productName=$productName, fromProductScreen=$fromProductScreen")

        if (fromProductScreen && !productName.isNullOrEmpty()) {
            val messageText = "I'm interested in $productName"
            Log.d("MessagesFragment", "Setting message field to: $messageText")
            etMessage.setText(messageText)
            etMessage.setSelection(etMessage.text.length)
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addLocalMessage(text)
                sendMessageViaApi(text)
                etMessage.text.clear()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        NotificationWebSocketManager.reconnectIfNeeded()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (openedFromOrderInfo) {
                    val intent = Intent(requireContext(), OrderInfoActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish() // Close MainActivity to prevent back stack issues
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
        if (userId > 0) {
            MessageWebSocketManager.connect(userId)
            setupWebSocketConnection()
        }
    }

    override fun onPause() {
        super.onPause()
        MessageWebSocketManager.messageListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageWebSocketManager.messageListener = null
    }

    private fun setupWebSocketConnection() {
        MessageWebSocketManager.connect(userId)
        Log.d("MessagesFragment", "Setting up WebSocket message listener")
        MessageWebSocketManager.messageListener = { message ->
            Log.d("MessagesFragment", "New message received: $message")
            if ((message.sender_id == userId && message.receiver_id == receiverId) ||
                (message.sender_id == receiverId && message.receiver_id == userId)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isAdded && !isDetached) {
                        Log.d("MessagesFragment", "Processing message: $message")
                        // Remove temporary message if it matches content
                        lastTempMessage?.let { temp ->
                            if (temp.message == message.message && temp.sender_id == message.sender_id &&
                                temp.receiver_id == message.receiver_id) {
                                messagesList.remove(temp)
                                lastTempMessage = null
                                Log.d("MessagesFragment", "Removed temporary message: $temp")
                            }
                        }
                        // Add server message if not already present
                        if (!messagesList.any { it.id == message.id }) {
                            messagesList.add(message)
                            adapter.updateMessages(messagesList)
                            recyclerView.scrollToPosition(adapter.itemCount - 1)
                            Log.d("MessagesFragment", "Added server message: $message")
                        } else {
                            Log.d("MessagesFragment", "Duplicate message ignored: ${message.id}")
                        }
                    }
                }
            } else {
                Log.d("MessagesFragment", "Message ignored: not in conversation with receiverId=$receiverId")
            }
        }
    }

    private fun sendMessageViaWebSocket(text: String) {
        MessageWebSocketManager.sendMessage(text, userId, receiverId)
    }

    private fun sendMessageViaApi(text: String) {
        val messageRequest = MessageRequest(userId, receiverId, text)
        apiService.sendMessage(messageRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (isAdded && !isDetached) {
                    if (!response.isSuccessful) {
                        Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("MessagesFragment", "API send successful: ${response.body()}")
                    }
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                if (isAdded && !isDetached) {
                    Log.e("MessagesFragment", "Error sending message: ${t.message}")
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun addLocalMessage(text: String) {
        val tempMessage = Message(
            id = System.currentTimeMillis().toInt(), // Temporary ID
            sender_id = userId,
            receiver_id = receiverId,
            message = text,
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            status = "sent",
            is_read = 0
        )
        lastTempMessage = tempMessage // Store the temporary message
        messagesList.add(tempMessage)
        adapter.updateMessages(messagesList)
        toggleEmptyState()
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        Log.d("MessagesFragment", "Added local message: $tempMessage")
    }

    private fun fetchMessages() {
        apiService.getMessages(userId).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (isAdded && !isDetached) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.messages?.let { allMessages ->
                            messagesList.clear()
                            val conversationMessages = allMessages.filter {
                                (it.sender_id == userId && it.receiver_id == receiverId) ||
                                        (it.sender_id == receiverId && it.receiver_id == userId)
                            }
                            messagesList.addAll(conversationMessages.reversed())
                            adapter.updateMessages(messagesList)
                            toggleEmptyState()
                            recyclerView.post {
                                if (adapter.itemCount > 0) {
                                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                                }
                            }
                            Log.d("MessagesFragment", "Fetched messages: ${messagesList.size}")
                        }
                    } else {
                        toggleEmptyState()
                    }
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                if (isAdded && !isDetached) {
                    Log.e("MessagesFragment", "Error fetching messages: ${t.message}")
                    Toast.makeText(requireContext(), "Error loading messages", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getUserIdFromPreferences(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
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

    fun shouldReturnToProduct(): Boolean {
        val result = openedFromProductScreen && sourceProductId != null
        Log.d("MessagesFragment", "shouldReturnToProduct: $result (fromProductScreen=$openedFromProductScreen, productId=$sourceProductId)")
        return result
    }

    fun getSourceProductId(): String? {
        return sourceProductId
    }
}