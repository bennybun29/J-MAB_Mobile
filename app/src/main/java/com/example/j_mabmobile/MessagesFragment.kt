package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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
import com.example.j_mabmobile.model.AdminResponse
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.Message
import com.example.j_mabmobile.model.MessageRequest
import com.example.j_mabmobile.model.MessageResponse
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.delay
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
    private var selectedAdminId: Int = 1
    private val messagesList = mutableListOf<Message>()
    private lateinit var noMessageIcon: ImageView
    private lateinit var startConvoTV: TextView
    private var lastTempMessage: Message? = null
    private var openedFromOrderInfo = false
    private var openedFromProductScreen = false
    private var sourceProductId: String? = null
    private var adminList = mutableListOf<Admin>()
    private lateinit var adminAutoCompleteTextView: AutoCompleteTextView
    private lateinit var selectAdminTextInputLayout: TextInputLayout

    // Updated Admin data class with last name
    data class Admin(val id: Int, val firstName: String, val lastName: String) {
        // Helper function to get full name
        fun getFullName(): String = "$firstName $lastName"
    }

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
        adminAutoCompleteTextView = view.findViewById(R.id.adminAutoCompleteTextview)
        selectAdminTextInputLayout = view.findViewById(R.id.selectAdminTextInputLayou)
        val btnSend = view.findViewById<ImageView>(R.id.btn_send)

        setupAdminDropdown()

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        adapter = MessagesAdapter(messagesList, userId)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null

        // Fetch admins first
        fetchAdmins()

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

    private fun setupAdminDropdown() {
        // This will be populated once we fetch admins from the server
        val dropdownAdapter = ArrayAdapter(
            requireContext(),
            R.layout.custom_spinner_dropdown_item,
            mutableListOf<String>()
        )

        adminAutoCompleteTextView.setAdapter(dropdownAdapter)

        adminAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            if (adminList.isNotEmpty() && position < adminList.size) {
                val previousAdminId = selectedAdminId
                selectedAdminId = adminList[position].id

                // Only reload messages if admin ID changed
                if (previousAdminId != selectedAdminId) {
                    messagesList.clear()
                    adapter.updateMessages(messagesList)
                    fetchConversation()
                    setupWebSocketConnection()
                }
            }
        }
    }

    private fun fetchAdmins() {
        apiService.getAdmins().enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                // Check if the fragment is added and not detached before processing the response
                if (!isAdded || isDetached) {
                    return
                }

                // Safely use requireContext() only if the fragment is attached
                val context = context ?: return

                if (response.isSuccessful && response.body()?.success == true) {
                    adminList.clear()
                    response.body()?.admins?.forEach {
                        adminList.add(Admin(it.id, it.first_name, it.last_name))
                    }

                    // Update dropdown with admin full names
                    val adminNames = adminList.map { it.getFullName() }
                    val dropdownAdapter = ArrayAdapter(
                        context,
                        R.layout.custom_spinner_dropdown_item,
                        adminNames
                    )
                    adminAutoCompleteTextView.setAdapter(dropdownAdapter)

                    // Select the first admin or a previously selected one
                    val savedAdminId = getSavedAdminId()
                    val indexToSelect = adminList.indexOfFirst { it.id == savedAdminId }
                    if (indexToSelect >= 0) {
                        adminAutoCompleteTextView.setText(adminList[indexToSelect].getFullName(), false)
                        selectedAdminId = savedAdminId
                    } else if (adminList.isNotEmpty()) {
                        adminAutoCompleteTextView.setText(adminList[0].getFullName(), false)
                        selectedAdminId = adminList[0].id
                    }

                    // Now fetch messages for the selected admin
                    fetchConversation()
                } else {
                    Toast.makeText(context, "Failed to load admins", Toast.LENGTH_SHORT).show()
                    // Fallback to default admin ID
                    selectedAdminId = 1
                    fetchConversation()
                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {
                // Check if the fragment is added and not detached before processing the failure
                if (!isAdded || isDetached) {
                    return
                }

                val context = context ?: return

                Log.e("MessagesFragment", "Error fetching admins: ${t.message}")
                Toast.makeText(context, "Error loading admins", Toast.LENGTH_SHORT).show()
                // Fallback to default admin ID
                selectedAdminId = 1
                fetchConversation()
            }
        })
    }

    // Get saved admin ID from preferences
    private fun getSavedAdminId(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("selected_admin_id", 1) // Default to 1 if not found
    }

    // Save selected admin ID to preferences
    private fun saveSelectedAdminId(adminId: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("selected_admin_id", adminId).apply()
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
        // Save the currently selected admin ID when pausing
        saveSelectedAdminId(selectedAdminId)
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageWebSocketManager.messageListener = null
    }

    private fun setupWebSocketConnection() {
        // Make sure you're connected
        MessageWebSocketManager.connect(userId)

        // Set up the message listener
        MessageWebSocketManager.messageListener = { message ->
            // Filter messages for this conversation only
            if ((message.sender_id == userId && message.receiver_id == selectedAdminId) ||
                (message.sender_id == selectedAdminId && message.receiver_id == userId)) {

                // Make sure we're in a valid state to update UI
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isAdded && !isDetached) {
                        // Check if this is a response to our temporary message
                        lastTempMessage?.let { temp ->
                            if (temp.message == message.message && temp.sender_id == message.sender_id &&
                                temp.receiver_id == message.receiver_id) {
                                // Remove temporary message
                                messagesList.remove(temp)
                                lastTempMessage = null
                            }
                        }

                        // Add the server message if it's not a duplicate
                        if (!messagesList.any { it.id == message.id }) {
                            messagesList.add(message)
                            adapter.updateMessages(messagesList)
                            recyclerView.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    private fun sendMessageViaApi(text: String) {
        val messageRequest = MessageRequest(userId, selectedAdminId, text)
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
            receiver_id = selectedAdminId,
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

    private fun fetchConversation() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            apiService.getConversation(selectedAdminId, userId).enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (isAdded && !isDetached) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            response.body()?.messages?.let { messages ->
                                messagesList.clear()
                                messagesList.addAll(messages.reversed())
                                adapter.updateMessages(messagesList)
                                toggleEmptyState()
                                recyclerView.post {
                                    if (adapter.itemCount > 0) {
                                        recyclerView.scrollToPosition(adapter.itemCount - 1)
                                    }
                                }
                                Log.d("MessagesFragment", "Fetched conversation: ${messagesList.size} messages with admin $selectedAdminId")
                            }
                        } else {
                            toggleEmptyState()
                            Log.e("MessagesFragment", "Error fetching conversation: ${response.code()}")
                        }
                    }
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    if (isAdded && !isDetached) {
                        Log.e("MessagesFragment", "Error fetching conversation: ${t.message}")
                        Toast.makeText(requireContext(), "Error loading messages", Toast.LENGTH_SHORT).show()
                        toggleEmptyState()
                    }
                }
            })
        }
    }

    // Keep the original fetchMessages as a backup/alternative
    private fun fetchMessages() {
        apiService.getMessages(userId).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (isAdded && !isDetached) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.messages?.let { allMessages ->
                            messagesList.clear()
                            val conversationMessages = allMessages.filter {
                                (it.sender_id == userId && it.receiver_id == selectedAdminId) ||
                                        (it.sender_id == selectedAdminId && it.receiver_id == userId)
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