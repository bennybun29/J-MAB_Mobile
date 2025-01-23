package com.example.j_mabmobile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.j_mabmobile.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the first fragment (default)
        openFragment(HomeFragment())

        // Set the BottomNavigationView listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle other navigation actions if necessary
        return false
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
                .setPositiveButton("Yes") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }



    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
