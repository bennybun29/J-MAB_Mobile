package com.example.j_mabmobile

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.j_mabmobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)


        // Check if the token is valid and handle authentication
        if (!isTokenValid()) {
            // If the token is not valid or expired, navigate to the SignInActivity
            navigateToSignIn()
        } else {
            // Set the first fragment (default)
            openFragment(HomeFragment())
        }

        // Set the BottomNavigationView listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            updateBottomNavState(item)
            when (item.itemId) {
                R.id.home_btn -> {
                    openFragment(HomeFragment())
                    true
                }
                R.id.notification_btn -> {
                    openFragment(NotificationFragment())
                    true
                }
                R.id.message_btn -> {
                    openFragment(MessagesFragment())
                    true
                }
                R.id.account_btn -> {
                    openFragment(AccountFragment())
                    true
                }
                else -> false
            }
        }

        // Set the initial state for icons when the app starts
        setInitialIconsState()
    }

    override fun onBackPressed() {
        // Check if the current fragment is NOT HomeFragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment !is HomeFragment) {
            // Navigate to HomeFragment
            openFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.home_btn
        } else {
            // If already in HomeFragment, allow the default behavior (exit the app)
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want close the app?")
                .setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateBottomNavState(item: MenuItem) {
        // Loop through all items to reset size for unselected items
        for (i in 0 until binding.bottomNavigation.menu.size()) {
            val menuItem = binding.bottomNavigation.menu.getItem(i)
            val icon = menuItem.icon
            if (menuItem.itemId == item.itemId) {
                // For the selected item, increase the icon size
                scaleIcon(icon, 36) // Larger icon size
            } else {
                // For unselected items, decrease the icon size
                scaleIcon(icon, 24) // Smaller icon size
            }
        }
    }

    private fun scaleIcon(icon: Drawable?, size: Int) {
        // Set the icon size using bounds (scale the drawable)
        val width = size
        val height = size
        icon?.setBounds(0, 0, width, height)
    }

    private fun setInitialIconsState() {
        // Initialize all icons to be small on start
        for (i in 0 until binding.bottomNavigation.menu.size()) {
            val menuItem = binding.bottomNavigation.menu.getItem(i)
            val icon = menuItem.icon
            scaleIcon(icon, 24) // Smaller icon size
        }
    }

    // Check if the token is saved and valid
    private fun isTokenValid(): Boolean {
        val sharedPreferences = getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)
        val tokenExpiryTime = sharedPreferences.getLong("token_expiry_time", 0L)

        // Check if token exists and if it has expired
        if (token != null && tokenExpiryTime > System.currentTimeMillis()) {
            // Token is valid
            return true
        }
        // Token is either missing or expired
        return false
    }

    // Navigate to the SignInActivity if the token is invalid
    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish() // Close the MainActivity so the user cannot go back to it
    }
}
