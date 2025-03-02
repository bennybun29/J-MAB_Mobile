package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CheckoutRequest
import com.example.j_mabmobile.model.CheckoutResponse
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    private lateinit var checkoutBtn: Button
    private var selectedPaymentMethod: String? = null
    private lateinit var orderPlacedCardView: View
    private lateinit var progressBar: ProgressBar
    private lateinit var doneBtn: Button
    private lateinit var overlayBackground: View


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
        orderPlacedCardView = findViewById(R.id.orderPlacedCardview)
        progressBar = findViewById(R.id.progressBar)
        doneBtn = findViewById(R.id.doneBtn)
        overlayBackground = findViewById(R.id.overlayBackground)

        progressBar.visibility = View.GONE
        orderPlacedCardView.visibility = View.GONE
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
            exitConfirmationCard.visibility = View.GONE
            overlayBackground.visibility = View.GONE

        }

        overlayBackground.setOnClickListener {
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

        checkoutBtn = findViewById(R.id.checkoutBtn)

        checkoutBtn.setOnClickListener {
            processCheckout()
        }

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

        doneBtn.setOnClickListener{
            doneBtn.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
        updateTotalPrice()

    }

    override fun onResume() {
        super.onResume()
        val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
        exitConfirmationCard.visibility = View.GONE
        overlayBackground.visibility = View.GONE
    }

    override fun onBackPressed() {
        val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
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

    private fun processCheckout() {
        val token = getToken()

        if (token == null) {
            android.util.Log.e("CHECKOUT_ERROR", "Token is null! Cannot proceed.")
            Toast.makeText(this, "Authentication error: No token found", Toast.LENGTH_SHORT).show()
            return
        }

        val cartIds = selectedCartItems.map { it.cart_id }

        android.util.Log.d("CHECKOUT_DEBUG", "Selected Cart Items: $selectedCartItems")
        android.util.Log.d("CHECKOUT_DEBUG", "Cart IDs: $cartIds")

        if (cartIds.isEmpty()) {
            android.util.Log.e("CHECKOUT_ERROR", "No cart items selected.")
            Toast.makeText(this, "No items selected for checkout", Toast.LENGTH_SHORT).show()
            return
        }

        selectedPaymentMethod = when {
            codCheckBox.isChecked -> "cod"
            gcashCheckBox.isChecked -> "gcash"
            else -> null
        }

        if (selectedPaymentMethod == null) {
            android.util.Log.e("CHECKOUT_ERROR", "No payment method selected.")
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progressBar if GCASH is selected
        if (selectedPaymentMethod == "gcash") {
            overlayBackground.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        }

        // Show orderPlacedCardView immediately if COD is selected
        if (selectedPaymentMethod == "cod") {
            overlayBackground.visibility = View.VISIBLE
            orderPlacedCardView.visibility = View.VISIBLE

        }

        val userId = getUserID()
        val request = CheckoutRequest(cartIds, selectedPaymentMethod!!, userId)
        android.util.Log.d("CHECKOUT_DEBUG", "Request Payload: $request")

        val apiService = RetrofitClient.getApiService(this)

        apiService.checkout(request).enqueue(object : Callback<CheckoutResponse> {
            override fun onResponse(call: Call<CheckoutResponse>, response: Response<CheckoutResponse>) {
                // Hide progressBar after response
                if (selectedPaymentMethod == "gcash") {
                    progressBar.visibility = View.GONE
                }

                if (response.isSuccessful) {
                    val checkoutResponse = response.body()
                    if (checkoutResponse != null && checkoutResponse.success) {
                        // Open GCASH payment link if available
                        checkoutResponse.payment_link?.let {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                            startActivity(intent)
                        } ?:  run {
                            if (selectedPaymentMethod == "cod") {
                                overlayBackground.visibility = View.VISIBLE
                                orderPlacedCardView.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        android.util.Log.e("CHECKOUT_ERROR", "Checkout failed: ${checkoutResponse?.message}")
                        Toast.makeText(this@CheckoutActivity, "Checkout failed: ${checkoutResponse?.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("CHECKOUT_ERROR", "Server error response: $errorBody")
                    Toast.makeText(this@CheckoutActivity, "Server error: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                // Hide progressBar if GCASH request fails
                if (selectedPaymentMethod == "gcash") {
                    progressBar.visibility = View.GONE
                }

                android.util.Log.e("CHECKOUT_ERROR", "Network error: ${t.message}")
                Toast.makeText(this@CheckoutActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }




    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        // Debugging: Print token before sending
        android.util.Log.d("CHECKOUT_DEBUG", "Retrieved Token: $token")

        return token?.let { "Bearer $it" }
    }

    private fun getUserID(): Int {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", 1)
    }



}