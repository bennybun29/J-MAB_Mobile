package com.example.j_mabmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Notification

class NotificationAdapter(private val notifications: MutableList<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.notificationTitle)
        val message: TextView = itemView.findViewById(R.id.notificationMessage)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteNotifButton)
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

        holder.deleteButton.setOnClickListener {
            notifications.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }
}
