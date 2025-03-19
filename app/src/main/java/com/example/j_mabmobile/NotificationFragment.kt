package com.example.j_mabmobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        val emptyIcon = view.findViewById<ImageView>(R.id.emptyIcon)
        val noOrdersYetTV = view.findViewById<TextView>(R.id.noOrdersYetTV)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewNotifications)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotificationAdapter(mutableListOf()) { notificationId ->
            viewModel.deleteNotification(notificationId) // Pass function reference
        }

        recyclerView.adapter = adapter

        // ✅ Observe notifications from ViewModel
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            adapter.updateData(notifications)

            if (notifications.isEmpty()) {
                emptyIcon.visibility = View.VISIBLE
                noOrdersYetTV.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyIcon.visibility = View.GONE
                noOrdersYetTV.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        // ✅ Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Log.e("NotificationError", error)
        }

        // ✅ Retrieve user ID from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            viewModel.fetchNotifications(userId)
            NotificationWebSocketManager.connect(userId)
        } else {
            Log.e("UserID", "Failed to retrieve user ID")
        }

        // ✅ Set WebSocket notification listener
        NotificationWebSocketManager.notificationListener = { notification ->
            activity?.runOnUiThread {
                viewModel.addNotification(notification)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.markNotificationsAsRead() // Mark notifications as read when user sees them
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // ✅ Remove observers (no duplicates)
        viewModel.notifications.removeObservers(viewLifecycleOwner)
        viewModel.errorMessage.removeObservers(viewLifecycleOwner)

        // ✅ Clean up WebSocket listener
        NotificationWebSocketManager.notificationListener = null

        // ✅ Re-establish MainActivity WebSocket listener
        (activity as? MainActivity)?.setupWebSocketListener()

        (activity as? MainActivity)?.isNotificationFragmentOpen = false
    }
}
