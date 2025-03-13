package com.example.j_mabmobile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Message
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MessagesAdapter(private var messagesList: List<Message>, private val userId: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_ADMIN = 2
    private val VIEW_TYPE_TIMESTAMP = 3 // New view type for centered timestamps

    // Time gap in milliseconds (10 minutes = 600,000 ms)
    private val TIME_GAP_THRESHOLD = 10 * 60 * 1000

    // List to store our actual items (messages + timestamps)
    private val items = mutableListOf<Any>()

    // Track the position of the currently selected message
    private var selectedPosition = RecyclerView.NO_POSITION

    init {
        // Process the messages list to add timestamp separators
        processMessages()
    }

    // Method to update messages data
    fun updateMessages(newMessages: List<Message>) {
        this.messagesList = newMessages
        processMessages()
        notifyDataSetChanged()
    }

    private fun processMessages() {
        items.clear()

        if (messagesList.isEmpty()) return

        // Add the first message
        items.add(messagesList[0])

        // Parse the first message timestamp for comparison
        var lastTimestamp = parseTimestamp(messagesList[0].timestamp)

        // Start from the second message (if any)
        for (i in 1 until messagesList.size) {
            val currentMessage = messagesList[i]
            val currentTimestamp = parseTimestamp(currentMessage.timestamp)

            // If the time difference is greater than our threshold, add a timestamp separator
            if (currentTimestamp != null && lastTimestamp != null &&
                currentTimestamp.time - lastTimestamp.time > TIME_GAP_THRESHOLD) {
                // Add a timestamp separator using the current message's timestamp
                items.add(TimestampSeparator(currentMessage.timestamp))
            }

            // Add the current message
            items.add(currentMessage)
            lastTimestamp = currentTimestamp
        }
    }

    private fun parseTimestamp(timestamp: String): Date? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            inputFormat.parse(timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is TimestampSeparator -> VIEW_TYPE_TIMESTAMP
            is Message -> if ((item as Message).sender_id == userId) VIEW_TYPE_USER else VIEW_TYPE_ADMIN
            else -> throw IllegalArgumentException("Unknown item type at position $position: $item")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_ADMIN -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_admin, parent, false)
                AdminMessageViewHolder(view)
            }
            VIEW_TYPE_TIMESTAMP -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timestamp_divider, parent, false)
                TimestampViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is UserMessageViewHolder -> {
                if (item is Message) {
                    holder.bind(item, position == selectedPosition)
                }
            }
            is AdminMessageViewHolder -> {
                if (item is Message) {
                    holder.bind(item, position == selectedPosition)
                }
            }
            is TimestampViewHolder -> {
                if (item is TimestampSeparator) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val textTimestamp: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(message: Message, isSelected: Boolean) {
            textMessage.text = message.message
            textTimestamp.text = formatTimestamp(message.timestamp)

            // Set timestamp visibility based on selection
            textTimestamp.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Click listener code remains the same
            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                val newPosition = adapterPosition

                if (newPosition != RecyclerView.NO_POSITION) {
                    selectedPosition = if (selectedPosition == newPosition) RecyclerView.NO_POSITION else newPosition

                    if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelectedPosition)
                    }
                    if (selectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selectedPosition)
                    }
                }
            }
        }
    }

    inner class AdminMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val textTimestamp: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(message: Message, isSelected: Boolean) {
            textMessage.text = message.message
            textTimestamp.text = formatTimestamp(message.timestamp)

            // Set timestamp visibility based on selection
            textTimestamp.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Click listener code remains the same
            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                val newPosition = adapterPosition

                if (newPosition != RecyclerView.NO_POSITION) {
                    selectedPosition = if (selectedPosition == newPosition) RecyclerView.NO_POSITION else newPosition

                    if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelectedPosition)
                    }
                    if (selectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selectedPosition)
                    }
                }
            }
        }
    }

    // Also update the TimestampViewHolder to use the same method
    inner class TimestampViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTimestamp: TextView = itemView.findViewById(R.id.textTimestamp)

        fun bind(separator: TimestampSeparator) {
            textTimestamp.text = formatTimestamp(separator.timestamp)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Data class for timestamp separators
    data class TimestampSeparator(val timestamp: String)

    // This method should replace the current timestamp formatting in both ViewHolders

    private fun formatTimestamp(timestamp: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("MMMM dd, yyyy h:mm a", Locale.getDefault())
        val timeOnlyFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        return try {
            // Make sure parsing is done in the correct time zone
            inputFormat.timeZone = TimeZone.getDefault()
            fullDateFormat.timeZone = TimeZone.getDefault()
            timeOnlyFormat.timeZone = TimeZone.getDefault()

            val date: Date = inputFormat.parse(timestamp) ?: return timestamp

            // Debug the parsed date and current time
            Log.d("TimestampDebug", "Original timestamp: $timestamp")
            Log.d("TimestampDebug", "Parsed date: ${date}")
            Log.d("TimestampDebug", "Current time: ${Date()}")

            val currentDate = Calendar.getInstance()
            val messageDate = Calendar.getInstance().apply { time = date }

            val formattedTimestamp = if (isSameDay(currentDate, messageDate)) {
                timeOnlyFormat.format(date) // Show only time if it's today
            } else {
                fullDateFormat.format(date) // Show full date and time otherwise
            }

            Log.d("TimestampDebug", "Formatted timestamp: $formattedTimestamp")
            formattedTimestamp
        } catch (e: Exception) {
            Log.e("TimestampDebug", "Error formatting timestamp: ${e.message}", e)
            timestamp // Fallback to original timestamp
        }
    }
}