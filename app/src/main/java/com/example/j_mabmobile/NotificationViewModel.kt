package com.example.j_mabmobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.j_mabmobile.api.NotificationWebSocketManager
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.DeleteNotificationResponse
import com.example.j_mabmobile.model.Notification
import com.example.j_mabmobile.model.NotificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications

    private val _hasUnreadNotifications = MutableLiveData<Boolean>()
    val hasUnreadNotifications: LiveData<Boolean> get() = _hasUnreadNotifications

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val apiService = RetrofitClient.getApiService(application.applicationContext)
    private val currentNotifications = mutableListOf<Notification>()

    // Flag to track if all notifications have been manually marked as read
    private var allNotificationsRead = false

    fun fetchNotifications(userId: Int) {
        viewModelScope.launch {
            try {
                apiService.getNotifications(userId).enqueue(object : Callback<NotificationResponse> {
                    override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            currentNotifications.clear()
                            currentNotifications.addAll(response.body()?.notifications ?: emptyList())
                            _notifications.postValue(currentNotifications)

                            // Reset the read flag when fetching new notifications
                            allNotificationsRead = false
                            checkUnreadNotifications()
                        } else {
                            _errorMessage.postValue("Failed to fetch notifications")
                        }
                    }

                    override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                        _errorMessage.postValue(t.message ?: "An error occurred")
                    }
                })
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "An error occurred")
            }
        }
    }

    fun addNotification(notification: Notification) {
        // Run on main thread to avoid concurrency issues
        viewModelScope.launch(Dispatchers.Main) {
            // Check if notification already exists
            val existingIndex = currentNotifications.indexOfFirst { it.id == notification.id }

            if (existingIndex >= 0) {
                // Update existing notification
                currentNotifications[existingIndex] = notification
            } else {
                // Add new notification at the top
                currentNotifications.add(0, notification)

                // Reset the read flag when new notification arrives
                allNotificationsRead = false

                // Immediately update the unread status for new notifications
                if (notification.is_read == 0) {
                    _hasUnreadNotifications.value = true
                }
            }

            _notifications.value = ArrayList(currentNotifications)
        }
    }

    private fun checkUnreadNotifications() {
        // If all notifications have been manually marked as read, don't change the status
        if (allNotificationsRead) return

        val hasUnread = currentNotifications.any { it.is_read == 0 }
        _hasUnreadNotifications.postValue(hasUnread)
    }

    fun markNotificationsAsRead() {
        val unreadNotifications = currentNotifications.filter { it.is_read == 0 }

        if (unreadNotifications.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    for (notification in unreadNotifications) {
                        val response = apiService.markNotificationAsRead(notification.id)
                        if (response.isSuccessful && response.body()?.success == true) {
                            notification.is_read = 1  // Update local state
                        }
                    }

                    // Update LiveData after marking all as read
                    _notifications.value = ArrayList(currentNotifications)
                    allNotificationsRead = true
                    _hasUnreadNotifications.value = false
                } catch (e: Exception) {
                    _errorMessage.postValue(e.message ?: "Failed to mark notifications as read")
                }
            }
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            try {
                apiService.deleteNotification(notificationId).enqueue(object : Callback<DeleteNotificationResponse> {
                    override fun onResponse(call: Call<DeleteNotificationResponse>, response: Response<DeleteNotificationResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Remove notification from local list
                            currentNotifications.removeAll { it.id == notificationId }
                            _notifications.postValue(ArrayList(currentNotifications))

                            // Update unread notification status
                            checkUnreadNotifications()
                        } else {
                            _errorMessage.postValue("Failed to delete notification")
                        }
                    }

                    override fun onFailure(call: Call<DeleteNotificationResponse>, t: Throwable) {
                        _errorMessage.postValue(t.message ?: "An error occurred while deleting notification")
                    }
                })
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "An error occurred")
            }
        }
    }


    // Force update the notification icon state
    fun forceUpdateNotificationState() {
        // Reset the read flag
        allNotificationsRead = false
        checkUnreadNotifications()
    }
}