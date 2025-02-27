package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.CartItem
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var changeAddressBtn: Button
    lateinit var codCheckBox: CheckBox
    lateinit var gcashCheckBox: CheckBox
    private lateinit var recyclerViewCheckout: RecyclerView
    private lateinit var checkoutAdapter: CheckoutAdapter
    private val selectedCartItems = mutableListOf<CartItem>()
    private lateinit var totalPaymentTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
        val confirmExitBtn = findViewById<Button>(R.id.confirmExitBtn)
        val cancelExitBtn = findViewById<Button>(R.id.cancelExitBtn)
        val firstProductImage = findViewById<ImageView>(R.id.firstProductImage)
        recyclerViewCheckout = findViewById(R.id.checkoutRecyclerView)
        recyclerViewCheckout.layoutManager = LinearLayoutManager(this)
        totalPaymentTV = findViewById(R.id.totalPaymentTV)

        val items: ArrayList<CartItem>? = intent.getParcelableArrayListExtra("selected_items")

        if (!items.isNullOrEmpty()) {
            selectedCartItems.addAll(items)

            val firstItem = selectedCartItems[0]

            firstProductImage.visibility = View.VISIBLE
            Picasso.get()
                .load(firstItem.product_image)
                .placeholder(R.drawable.jmab_fab)
                .error(R.drawable.jmab_fab)
                .into(firstProductImage)
        } else {
            firstProductImage.visibility = View.GONE
        }

        checkoutAdapter = CheckoutAdapter(selectedCartItems)
        recyclerViewCheckout.adapter = checkoutAdapter

        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener({
            onBackPressed()
        })

        confirmExitBtn.setOnClickListener {
            finish()
        }

        cancelExitBtn.setOnClickListener {
            val overlayBackground = findViewById<View>(R.id.overlayBackground)
            exitConfirmationCard.visibility = View.GONE
            overlayBackground.visibility = View.GONE

        }

        changeAddressBtn = findViewById(R.id.changeAddressBtn)

        changeAddressBtn.setOnClickListener({
            onPause()
            val intent = Intent(this, MyAddressesActivity::class.java)
            startActivity(intent)
        })

        codCheckBox = findViewById(R.id.codChkBox)
        gcashCheckBox = findViewById(R.id.gcashChkBox)

        val checkBoxes = listOf(codCheckBox, gcashCheckBox)

        for (checkBox in checkBoxes) {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxes.forEach { otherCheckBox ->
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.isChecked = false
                        }
                    }
                }
            }
        }
        updateTotalPrice()

    }

    override fun onResume() {
        super.onResume()
        val overlayBackground = findViewById<View>(R.id.overlayBackground)
        val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
        exitConfirmationCard.visibility = View.GONE
        overlayBackground.visibility = View.GONE
    }

    override fun onBackPressed() {
        val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
        val overlayBackground = findViewById<View>(R.id.overlayBackground)
        exitConfirmationCard.visibility = View.VISIBLE
        overlayBackground.visibility = View.VISIBLE
    }


    private fun updateTotalPrice() {
        val totalPrice = selectedCartItems.sumOf { it.total_price }

        val formattedPrice = if (totalPrice > 100) {
            NumberFormat.getNumberInstance(Locale.US).format(totalPrice)
        } else {
            "%.2f".format(totalPrice)
        }

        totalPaymentTV.text = "Total: \n₱${formattedPrice}"
    }

}