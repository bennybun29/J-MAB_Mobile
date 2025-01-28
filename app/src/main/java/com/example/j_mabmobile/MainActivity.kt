package com.example.j_mabmobile

import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
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

        // Set the first fragment (default)
        openFragment(HomeFragment())

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
}
