package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Notification
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(
    private val notifications: MutableList<Notification>,
    private val onDeleteNotification: (Int) -> Unit // Callback for deletion
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.notificationTitle)
        val message: TextView = itemView.findViewById(R.id.notificationMessage)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteNotifButton)
        val notificationDateTV: TextView = itemView.findViewById(R.id.notificationDateTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.notificationDateTV.text = formatDate(notification.created_at)

        holder.deleteButton.setOnClickListener {
            onDeleteNotification(notification.id)
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    // Date formatting method
    private fun formatDate(timestamp: String): String {
        return try {
            // Parse the original timestamp
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)

            // Format to a more readable format
            val outputFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (e: Exception) {
            // Fallback to original timestamp if parsing fails
            timestamp
        }
    }
}