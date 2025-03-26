package com.example.j_mabmobile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2

class MyPurchasesActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var toPayBtn: Button
    lateinit var toShipBtn: Button
    lateinit var toReceiveBtn: Button
    lateinit var toRateBtn: Button
    lateinit var cancelledBtn: Button
    lateinit var viewPager: ViewPager2
    lateinit var buttonScrollView: HorizontalScrollView

    private var fromCheckout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_purchases)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

        backBtn = findViewById(R.id.backButton)
        toPayBtn = findViewById(R.id.toPayBtnFragment)
        toShipBtn = findViewById(R.id.toShipBtnFragment)
        toReceiveBtn = findViewById(R.id.toReceiveBtnFragment)
        toRateBtn = findViewById(R.id.toRateBtnFragment)
        cancelledBtn = findViewById(R.id.cancelledBtnFragment)
        viewPager = findViewById(R.id.viewPager)
        buttonScrollView = findViewById(R.id.categoryHorizontalScrollView)

        val ordersViewModel = ViewModelProvider(this).get(OrdersViewModel::class.java)

        val toPayBadge = findViewById<TextView>(R.id.toPayBadge)
        val toShipBadge = findViewById<TextView>(R.id.toShipBadge)
        val toReceiveBadge = findViewById<TextView>(R.id.toReceiveBadge)
        val toRateBadge = findViewById<TextView>(R.id.toRateBadge)

        ordersViewModel.toPayCount.observe(this) { count ->
            updateBadge(toPayBadge, count)
        }

        ordersViewModel.toShipCount.observe(this) { count ->
            updateBadge(toShipBadge, count)
        }

        ordersViewModel.toReceiveCount.observe(this) { count ->
            updateBadge(toReceiveBadge, count)
        }

        ordersViewModel.toRateCount.observe(this) { count ->
            updateBadge(toRateBadge, count)
        }

        // Set up ViewPager with adapter
        val adapter = PurchasesPagerAdapter(this)
        viewPager.adapter = adapter

        // Check if this activity was opened from CheckoutActivity
        fromCheckout = intent.getBooleanExtra("FROM_CHECKOUT", false)

        // Get the selected tab from Intent
        val activeTab = intent.getStringExtra("ACTIVE_TAB") ?: "TO_PAY"
        val initialPosition = getPositionFromTab(activeTab)

        // Set up button click listeners
        toPayBtn.setOnClickListener {
            viewPager.currentItem = 0
            scrollToSelectedButton(toPayBtn)
        }

        toShipBtn.setOnClickListener {
            viewPager.currentItem = 1
            scrollToSelectedButton(toShipBtn)
        }

        toReceiveBtn.setOnClickListener {
            viewPager.currentItem = 2
            scrollToSelectedButton(toReceiveBtn)
        }

        toRateBtn.setOnClickListener {
            viewPager.currentItem = 3
            scrollToSelectedButton(toRateBtn)
        }

        cancelledBtn.setOnClickListener {
            viewPager.currentItem = 4
            scrollToSelectedButton(cancelledBtn)
        }

        // Set initial page
        viewPager.currentItem = initialPosition
        updateButtonSelection(initialPosition)

        // Handle initial button scrolling with a delay to ensure layout is ready
        buttonScrollView.post {
            // First scroll immediately to position the view somewhat correctly
            scrollToButtonByPosition(initialPosition)

            // Then do a more precise scroll after a slight delay
            Handler(Looper.getMainLooper()).postDelayed({
                scrollToButtonByPosition(initialPosition)
            }, 200)
        }

        // Register page change callback
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtonSelection(position)
                scrollToButtonByPosition(position)
            }
        })

        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (fromCheckout) {
            // Navigate to MainActivity with the HomeFragment selected
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "HOME")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun getPositionFromTab(activeTab: String): Int {
        return when (activeTab) {
            "TO_PAY" -> 0
            "TO_SHIP" -> 1
            "TO_RECEIVE" -> 2
            "TO_RATE" -> 3
            "CANCELLED" -> 4
            else -> 0
        }
    }

    private fun updateButtonSelection(position: Int) {
        val buttons = listOf(toPayBtn, toShipBtn, toReceiveBtn, toRateBtn, cancelledBtn)
        buttons.forEachIndexed { index, button ->
            button.isSelected = index == position
        }
    }

    private fun scrollToButtonByPosition(position: Int) {
        val button = when (position) {
            0 -> toPayBtn
            1 -> toShipBtn
            2 -> toReceiveBtn
            3 -> toRateBtn
            4 -> cancelledBtn
            else -> toPayBtn
        }

        scrollToSelectedButton(button)
    }

    private fun scrollToSelectedButton(selectedButton: Button) {
        buttonScrollView.post {
            // Get scroll view width
            val scrollViewWidth = buttonScrollView.width

            // Get linear layout that contains the buttons
            val linearLayout = buttonScrollView.getChildAt(0) as LinearLayout

            // Special handling for the last button (Cancelled)
            if (selectedButton == cancelledBtn) {
                // Calculate the maximum scrollable distance
                val maxScrollX = linearLayout.width - scrollViewWidth

                // Scroll to show the Cancelled button
                if (maxScrollX > 0) {
                    buttonScrollView.smoothScrollTo(maxScrollX, 0)
                }
            } else {
                // For other buttons, center them or align left as appropriate
                val buttonLeft = selectedButton.left
                val buttonWidth = selectedButton.width

                // Calculate position to center the button
                val scrollTo = buttonLeft - (scrollViewWidth - buttonWidth) / 2
                val finalScrollX = maxOf(0, scrollTo)

                buttonScrollView.smoothScrollTo(finalScrollX, 0)
            }
        }
    }

    private fun updateBadge(badge: TextView, count: Int) {
        if (count > 0) {
            badge.visibility = View.VISIBLE
            badge.text = if (count > 99) "99+" else count.toString()
        } else {
            badge.visibility = View.GONE
        }
    }
}