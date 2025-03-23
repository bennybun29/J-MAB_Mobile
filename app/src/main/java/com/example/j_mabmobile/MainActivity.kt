package com.example.j_mabmobile

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.Manifest
import android.content.pm.ServiceInfo
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.j_mabmobile.api.NotificationWebSocketManager
import com.example.j_mabmobile.api.WebSocketService
import com.example.j_mabmobile.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_NOTIFICATIONS = 101
    }

    private lateinit var binding: ActivityMainBinding
    private var pendingProductName: String? = null
    private var pendingFromProductScreen: Boolean = false
    private var pendingProductId: String? = null
    private lateinit var notificationViewModel: NotificationViewModel
    var isNotificationFragmentOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.getBooleanExtra("navigate_to_home", false)) {
            openFragment(HomeFragment())
        }

        // Check and request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_NOTIFICATIONS
                )
            }
        }

        // Start service
        val serviceIntent = Intent(this, WebSocketService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= 35) { // Android 15
                serviceIntent.putExtra("foregroundServiceType", ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            }
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        NotificationWebSocketManager.notificationListener = { notification ->
            // Use ViewModel to handle the notification
            notificationViewModel.addNotification(notification)
        }

        setupWebSocketListener()

        notificationViewModel.hasUnreadNotifications.observe(this) { hasUnread ->
            updateNotificationIcon(hasUnread)
        }

        val sharedPreferences = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            notificationViewModel.fetchNotifications(userId)
            // Connect to WebSocket for real-time updates
            NotificationWebSocketManager.connect(userId)
        }

        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        window.navigationBarColor = resources.getColor(R.color.j_mab_blue, theme)

        // Check if the token is valid and handle authentication
        if (!isTokenValid()) {
            // If the token is not valid or expired, navigate to the SignInActivity
            navigateToSignIn()
        } else {
            // Set the first fragment (default)
            navigateToHome()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Reset all icons to white first
            for (i in 0 until binding.bottomNavigation.menu.size()) {
                val menuItem = binding.bottomNavigation.menu.getItem(i)
                menuItem.icon?.setTint(resources.getColor(R.color.white, theme))
            }

            // Set the selected item's icon to dark blue
            item.icon?.setTint(resources.getColor(R.color.j_mab_blue, theme))

            when (item.itemId) {
                R.id.home_btn -> {
                    openFragment(HomeFragment())
                    true
                }
                R.id.notification_btn -> {
                    if (!isNotificationFragmentOpen) {
                        openFragment(NotificationFragment())
                        isNotificationFragmentOpen = true
                        notificationViewModel.markNotificationsAsRead()
                    }
                    true
                }
                R.id.message_btn -> {
                    // Create a fragment with the pending parameters if they exist
                    val messagesFragment = MessagesFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("FROM_PRODUCT_SCREEN", pendingFromProductScreen)
                            if (pendingProductName != null) {
                                putString("PRODUCT_NAME", pendingProductName)
                            }
                            if (pendingProductId != null) {
                                putString("PRODUCT_ID", pendingProductId)
                            }
                        }
                    }

                    // Reset pending parameters
                    pendingProductName = null
                    pendingFromProductScreen = false
                    isNotificationFragmentOpen = false

                    // Open the fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, messagesFragment, "MESSAGES_FRAGMENT")
                        .commit()

                    true
                }
                R.id.account_btn -> {
                    openFragment(AccountFragment())
                    true
                }
                else -> false
            }
        }

        if (intent.hasExtra("OPEN_FRAGMENT")) {
            when (intent.getStringExtra("OPEN_FRAGMENT")) {
                "NOTIFICATION_FRAGMENT" -> {
                    openFragment(NotificationFragment())
                    binding.bottomNavigation.selectedItemId = R.id.notification_btn
                    isNotificationFragmentOpen = true
                }
                "MESSAGE" -> {
                    // Store the parameters instead of creating the fragment here
                    pendingFromProductScreen = intent.getBooleanExtra("FROM_PRODUCT_SCREEN", false)
                    pendingProductName = intent.getStringExtra("PRODUCT_NAME")
                    pendingProductId = intent.getStringExtra("PRODUCT_ID")

                    Log.d("MainActivity", "Setting pending parameters: fromProductScreen=$pendingFromProductScreen, productName=$pendingProductName")

                    // Just set selected item, the listener will handle fragment creation
                    binding.bottomNavigation.selectedItemId = R.id.message_btn
                }
                // Other cases...
            }
        }

        setInitialIconsState()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can show notifications
            } else {
                // Permission denied, inform the user
                Toast.makeText(
                    this,
                    "Notification permission is required to receive alerts",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.hasExtra("OPEN_FRAGMENT")) {
            when (intent.getStringExtra("OPEN_FRAGMENT")) {
                "NOTIFICATION_FRAGMENT" -> {
                    // Open the NotificationFragment
                    openFragment(NotificationFragment())
                    binding.bottomNavigation.selectedItemId = R.id.notification_btn
                    isNotificationFragmentOpen = true
                }
                "MESSAGE" -> {
                    // Handle the message fragment case as before
                    val fragment = MessagesFragment()
                    val bundle = Bundle()

                    val fromOrderInfo = intent.getBooleanExtra("FROM_ORDER_INFO", false)
                    val fromProductScreen = intent.getBooleanExtra("FROM_PRODUCT_SCREEN", false)
                    val productName = intent.getStringExtra("PRODUCT_NAME")

                    bundle.putBoolean("FROM_ORDER_INFO", fromOrderInfo)
                    bundle.putBoolean("FROM_PRODUCT_SCREEN", fromProductScreen)
                    if (productName != null) {
                        bundle.putString("PRODUCT_NAME", productName)
                    }

                    fragment.arguments = bundle
                    loadFragment(fragment)
                    binding.bottomNavigation.selectedItemId = R.id.message_btn
                }
            }
        }
    }
    
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        Log.d("MainActivity", "onBackPressed: currentFragment=${currentFragment?.javaClass?.simpleName}")

        if (currentFragment is MessagesFragment && currentFragment.shouldReturnToProduct()) {
            // Return to the specific product screen
            val productId = currentFragment.getSourceProductId()
            navigateToProductScreen(productId)
        }
        else if (currentFragment !is HomeFragment) {
            openFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.home_btn
        }
        else {
            showCustomLogoutDialog()
        }
    }

    fun setupWebSocketListener() {
        NotificationWebSocketManager.notificationListener = { notification ->
            // Always update the notifications in the view model
            notificationViewModel.addNotification(notification)

            // If we're not currently in the notification fragment, make sure the icon updates
            if (!isNotificationFragmentOpen && notification.is_read == 0) {
                // Force update the UI on the main thread
                runOnUiThread {
                    updateNotificationIcon(true)
                }
            }
        }
    }

    private fun navigateToProductScreen(productId: String?) {
        if (productId != null) {
            val intent = Intent(this, ProductScreenActivity::class.java) // Or whatever your product activity is
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }

    private fun openFragment(fragment: Fragment) {
        // If we're leaving the notification fragment
        if (isNotificationFragmentOpen && fragment !is NotificationFragment) {
            // Reset the notification fragment state
            isNotificationFragmentOpen = false

            // Re-setup the WebSocket listener to ensure it works correctly
            setupWebSocketListener()

            // Force a check for unread notifications
            notificationViewModel.forceUpdateNotificationState()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun scaleIcon(icon: Drawable?, size: Int) {
        val width = size
        val height = size
        icon?.setBounds(0, 0, width, height)
    }

    private fun setInitialIconsState() {
        for (i in 0 until binding.bottomNavigation.menu.size()) {
            val menuItem = binding.bottomNavigation.menu.getItem(i)
            val icon = menuItem.icon
            scaleIcon(icon, 24)
        }
    }

    private fun isTokenValid(): Boolean {
        val sharedPreferences = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)
        val tokenExpiryTime = sharedPreferences.getLong("token_expiry_time", 0L)

        if (token != null && tokenExpiryTime > System.currentTimeMillis()) {
            return true
        }
        return false
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToHome() {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE) // ✅ Clears back stack
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        binding.bottomNavigation.selectedItemId = R.id.home_btn
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentTag = when (fragment) {
            is MessagesFragment -> "MESSAGES_FRAGMENT"
            is HomeFragment -> "HOME_FRAGMENT"
            is NotificationFragment -> "NOTIFICATION_FRAGMENT"
            is AccountFragment -> "ACCOUNT_FRAGMENT"
            else -> null
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, fragmentTag)
            .commit()
    }

    private fun updateNotificationIcon(hasUnread: Boolean) {
        val bottomNavigationView = binding.bottomNavigation
        val menuItemId = R.id.notification_btn

        val badgeDrawable = bottomNavigationView.getOrCreateBadge(menuItemId)

        if (hasUnread) {
            badgeDrawable.isVisible = true
            badgeDrawable.backgroundColor = ContextCompat.getColor(bottomNavigationView.context, R.color.red)
            badgeDrawable.badgeTextColor = ContextCompat.getColor(bottomNavigationView.context, R.color.white)

            badgeDrawable.clearNumber()
            badgeDrawable.verticalOffset = 10
            badgeDrawable.horizontalOffset = 10
        } else {
            badgeDrawable.isVisible = false
        }
    }

    fun getNotificationViewModel(): NotificationViewModel {
        return notificationViewModel
    }

    private fun showCustomLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_close_app_dialog, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Make background transparent

        // Get buttons from custom layout
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            dialog.dismiss()
            super.onBackPressed() // Close app
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}