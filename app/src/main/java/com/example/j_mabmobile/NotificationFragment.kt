package com.example.j_mabmobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.NotificationWebSocketManager

class NotificationFragment : Fragment() {
    private lateinit var viewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).getNotificationViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewNotifications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(mutableListOf())
        recyclerView.adapter = adapter

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            adapter.updateData(notifications)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->

        }

        // ✅ Retrieve user ID from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            viewModel.fetchNotifications(userId)
            NotificationWebSocketManager.connect(userId) // ✅ Connect WebSocket with user ID
        } else {
            Log.e("UserID", "Failed to retrieve user ID")
        }

        // ✅ Listen for new WebSocket notifications and update UI
        NotificationWebSocketManager.notificationListener = { notification ->
            viewModel.addNotification(notification)
        }
    }

    override fun onResume() {
        super.onResume()
        // This ensures we only mark them as read when user actually sees them
        viewModel.markNotificationsAsRead()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Important: Deregister the local notification listener
        NotificationWebSocketManager.notificationListener = null

        // Re-establish the main activity's listener
        (activity as? MainActivity)?.setupWebSocketListener()

        // Let the main activity know we're closing
        (activity as? MainActivity)?.let {
            it.isNotificationFragmentOpen = false
        }
    }
}
