package com.example.j_mabmobile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MyPurchasesActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var toPayBtn: Button
    lateinit var toShipBtn: Button
    lateinit var toReceiveBtn: Button
    lateinit var toRateBtn: Button
    lateinit var cancelledBtn: Button
    lateinit var buttonScrollView: HorizontalScrollView

    private var activeButton: Button? = null
    private var fromCheckout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_purchases)

        window.statusBarColor = resources.getColor(R.color.j_mab_blue, theme)
        window.navigationBarColor = resources.getColor(R.color.j_mab_blue, theme)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        backBtn = findViewById(R.id.backButton)
        toPayBtn = findViewById(R.id.toPayBtnFragment)
        toShipBtn = findViewById(R.id.toShipBtnFragment)
        toReceiveBtn = findViewById(R.id.toReceiveBtnFragment)
        toRateBtn = findViewById(R.id.toRateBtnFragment)
        cancelledBtn = findViewById(R.id.cancelledBtnFragment)
        buttonScrollView = findViewById(R.id.categoryHorizontalScrollView) // Add this line to get the ScrollView

        // Check if this activity was opened from CheckoutActivity
        fromCheckout = intent.getBooleanExtra("FROM_CHECKOUT", false)

        // Get the selected tab from Intent
        val activeTab = intent.getStringExtra("ACTIVE_TAB") ?: "TO_PAY"
        setInitialFragment(activeTab)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        toPayBtn.setOnClickListener {
            setActiveButton(toPayBtn)
            loadFragment(ToPayFragment())
        }

        toShipBtn.setOnClickListener {
            setActiveButton(toShipBtn)
            loadFragment(ToShipFragment())
        }

        toReceiveBtn.setOnClickListener {
            setActiveButton(toReceiveBtn)
            loadFragment(ToReceiveFragment())
        }

        toRateBtn.setOnClickListener {
            setActiveButton(toRateBtn)
            loadFragment(ToRateFragment())
        }

        cancelledBtn.setOnClickListener{
            setActiveButton(cancelledBtn)
            loadFragment(CancelledFragment())
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

    private fun setInitialFragment(activeTab: String) {
        val buttonToSelect = when (activeTab) {
            "TO_PAY" -> toPayBtn
            "TO_SHIP" -> toShipBtn
            "TO_RECEIVE" -> toReceiveBtn
            "TO_RATE" -> toRateBtn
            "CANCELLED" -> cancelledBtn
            else -> toPayBtn
        }

        buttonScrollView.post {
            val scrollX = when (activeTab) {
                "CANCELLED" -> buttonToSelect.right - buttonScrollView.width
                else -> buttonToSelect.left
            }

            buttonScrollView.smoothScrollTo(scrollX, 0)
        }

        setActiveButton(buttonToSelect)

        val fragment = when (activeTab) {
            "TO_PAY" -> ToPayFragment()
            "TO_SHIP" -> ToShipFragment()
            "TO_RECEIVE" -> ToReceiveFragment()
            "TO_RATE" -> ToRateFragment()
            "CANCELLED" -> CancelledFragment()
            else -> ToPayFragment()
        }

        loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setActiveButton(selectedButton: Button) {
        activeButton?.isSelected = false

        selectedButton.isSelected = true
        activeButton = selectedButton
    }
}