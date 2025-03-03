package com.example.j_mabmobile

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.j_mabmobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
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

        setInitialIconsState()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment !is HomeFragment) {
            openFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.home_btn
        } else {
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
        for (i in 0 until binding.bottomNavigation.menu.size()) {
            val menuItem = binding.bottomNavigation.menu.getItem(i)
            val icon = menuItem.icon
            if (menuItem.itemId == item.itemId) {
                scaleIcon(icon, 36)
            } else {
                scaleIcon(icon, 24)
            }
        }
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
}