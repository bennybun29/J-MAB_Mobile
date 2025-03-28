package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CheckoutRequest
import com.example.j_mabmobile.model.CheckoutResponse
import com.example.j_mabmobile.model.NotificationResponse
import com.example.j_mabmobile.model.SendNotifRequest
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
    private lateinit var progressBar: LottieAnimationView
    private lateinit var doneBtn: Button
    private lateinit var overlayBackground: View
    private lateinit var cartViewModel: CartViewModel
    private lateinit var userLocationTV: TextView
    private lateinit var addressViewModel: AddressViewModel
    private var defaultAddressId: Int? = null
    private var isFromBuyNow = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userNameTV: TextView
    private lateinit var userPhoneNumberTV: TextView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

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
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        isFromBuyNow = intent.getBooleanExtra("from_buy_now", false)
        sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        userLocationTV = findViewById(R.id.userLocationTV)
        userNameTV = findViewById(R.id.userNameTV)
        userPhoneNumberTV = findViewById(R.id.userPhoneNumberTV)

        val firstName = getUserFirstName()
        val lastName = getUserLastName()
        val phoneNumber = getUserPhoneNumber()

        userNameTV.text = "${firstName} ${lastName}"
        userPhoneNumberTV.text = phoneNumber ?: "No phone number"

        addressViewModel = ViewModelProvider(this)[AddressViewModel::class.java]

        addressViewModel.addresses.observe(this) { addresses ->
            val defaultAddress = addresses.find { it.is_default }
            if (defaultAddress != null) {
                val fullAddress = "${defaultAddress.home_address}, ${defaultAddress.barangay}, ${defaultAddress.city}, Pangasinan"
                val spannableString = SpannableString(fullAddress)

                val homeAddressStart = 0
                val homeAddressEnd = defaultAddress.home_address.length
                spannableString.setSpan(StyleSpan(Typeface.BOLD), homeAddressStart, homeAddressEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                val barangayStart = fullAddress.indexOf(defaultAddress.barangay)
                val barangayEnd = barangayStart + defaultAddress.barangay.length
                spannableString.setSpan(StyleSpan(Typeface.BOLD), barangayStart, barangayEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                val cityStart = fullAddress.indexOf(defaultAddress.city)
                val cityEnd = cityStart + defaultAddress.city.length
                spannableString.setSpan(StyleSpan(Typeface.BOLD), cityStart, cityEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                val pangasinanStart = fullAddress.indexOf("Pangasinan")
                val pangasinanEnd = pangasinanStart + "Pangasinan".length
                spannableString.setSpan(StyleSpan(Typeface.BOLD), pangasinanStart, pangasinanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                userLocationTV.text = spannableString

                defaultAddressId = defaultAddress.id
                findViewById<View>(R.id.noAddressErrorTV).visibility = View.GONE
                changeAddressBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.j_mab_blue))
            } else {
                userLocationTV.text = "No address added"
                defaultAddressId = null

                val noAddressErrorTV = findViewById<TextView>(R.id.noAddressErrorTV)
                noAddressErrorTV.visibility = View.VISIBLE }
        }

        addressViewModel.fetchAddresses()

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
            if (isFromBuyNow) {
                // Only delete the cart if coming from Buy Now
                Log.d("CheckoutActivity", "Deleting cart from Buy Now flow")
                deleteCart()
            }
            finish()
        }

        cancelExitBtn.setOnClickListener {
            exitConfirmationCard.visibility = View.GONE
            overlayBackground.visibility = View.GONE

        }

        overlayBackground.setOnClickListener {
            // Only allow dismissing the overlay if it's NOT from a COD payment
            if (selectedPaymentMethod != "cod") {
                val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
                exitConfirmationCard.visibility = View.GONE
                overlayBackground.visibility = View.GONE
            }
        }

        changeAddressBtn = findViewById(R.id.changeAddressBtn)

        changeAddressBtn.setOnClickListener({
            openChangeAddressScreen()
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

        doneBtn.setOnClickListener {
            val intent = Intent(this, MyPurchasesActivity::class.java)
            intent.putExtra("FROM_CHECKOUT", true)  // Add this flag
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        updatePaymentDetailsCard()
        updateTotalPrice()

    }

    override fun onResume() {
        super.onResume()
        val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
        exitConfirmationCard.visibility = View.GONE
        overlayBackground.visibility = View.GONE
        addressViewModel.fetchAddresses()

        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val gcashPaymentStarted = sharedPreferences.getBoolean("gcash_payment_started", false)

        if (gcashPaymentStarted) {
            sharedPreferences.edit().putBoolean("gcash_payment_started", false).apply()

            val intent = Intent(this, MyPurchasesActivity::class.java)
            intent.putExtra("FROM_CHECKOUT", true)  // Add this flag
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            val exitConfirmationCard = findViewById<CardView>(R.id.exitConfirmationCard)
            exitConfirmationCard.visibility = View.GONE
            overlayBackground.visibility = View.GONE
            addressViewModel.fetchAddresses()
        }

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

        totalPaymentTV.text = getBoldText("Total: \n", "₱${formattedPrice}")
    }

    private fun getBoldText(label: String, value: String?): SpannableString {
        val text = "$label$value"
        val spannable = SpannableString(text)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            label.length,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun processCheckout() {
        val token = getToken()
        val userId = getUserID()

        if (token == null || userId == -1) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show()
            return
        }

        val cartIds = selectedCartItems.map { it.cart_id }
        if (cartIds.isEmpty()) {
            Toast.makeText(this, "No items selected for checkout", Toast.LENGTH_SHORT).show()
            return
        }

        // Modify address check to show toast and highlight change address button
        if (defaultAddressId == null) {
            Toast.makeText(this, "Please add an address before checkout", Toast.LENGTH_SHORT).show()
            changeAddressBtn.startAnimation(getShakeAnimation())
            return
        }

        selectedPaymentMethod = when {
            codCheckBox.isChecked -> "cod"
            gcashCheckBox.isChecked -> "gcash"
            else -> null
        }

        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPaymentMethod == "gcash") {
            overlayBackground.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        }

        if (selectedPaymentMethod == "cod") {
            overlayBackground.visibility = View.VISIBLE
            orderPlacedCardView.visibility = View.VISIBLE
        }

        val request = CheckoutRequest(
            cart_ids = cartIds,
            payment_method = selectedPaymentMethod!!,
            address_id = defaultAddressId
        )

        val apiService = RetrofitClient.getApiService(this)
        apiService.checkout(userId, request).enqueue(object : Callback<CheckoutResponse> {
            override fun onResponse(call: Call<CheckoutResponse>, response: Response<CheckoutResponse>) {
                if (selectedPaymentMethod == "gcash") {
                    progressBar.visibility = View.GONE
                }

                if (response.isSuccessful) {
                    val checkoutResponse = response.body()
                    if (checkoutResponse != null && checkoutResponse.success) {

                        val firstName = getUserFirstName()
                        val lastName = getUserLastName()
                        // Send notification to admin
                        val productNames = selectedCartItems.map { it.product_name }.joinToString(", ")
                        val notificationRequest = SendNotifRequest(
                            user_id = 1, // Admin user ID
                            title = "New Order!",
                            message = "Order placed by User ID $userId and Username $firstName $lastName\nProducts: $productNames"
                        )

                        apiService.sendNotification(notificationRequest).enqueue(object : Callback<NotificationResponse> {
                            override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                                if (response.isSuccessful && response.body()?.success == true) {
                                    Log.d("CheckoutActivity", "Notification sent to admin successfully")
                                } else {
                                    Log.e("CheckoutActivity", "Failed to send notification to admin")
                                }
                            }

                            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                                Log.e("CheckoutActivity", "Error sending notification to admin", t)
                            }
                        })

                        // Existing payment method-specific logic
                        checkoutResponse.payment_link?.let {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                            startActivity(intent)

                            val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("gcash_payment_started", true).apply()
                        } ?: run {
                            if (selectedPaymentMethod == "cod") {
                                overlayBackground.visibility = View.VISIBLE
                                orderPlacedCardView.visibility = View.VISIBLE
                            }
                        }
                        cartViewModel.removeCheckedOutItems(selectedCartItems)
                    } else {
                        Toast.makeText(this@CheckoutActivity, "Checkout failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@CheckoutActivity, "Server error", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                if (selectedPaymentMethod == "gcash") {
                    progressBar.visibility = View.GONE
                }
                Toast.makeText(this@CheckoutActivity, "Network error", Toast.LENGTH_LONG).show()
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

    private fun openChangeAddressScreen() {
        val intent = Intent(this, MyAddressesActivity::class.java)
        startActivity(intent)
    }

    private fun deleteCart() {
        if (selectedCartItems.isNotEmpty()) {
            Log.d("CheckoutActivity", "Deleting cart items: ${selectedCartItems.map { it.cart_id }}")
            cartViewModel.removeCheckedOutItems(selectedCartItems)
        }
    }

    private fun updatePaymentDetailsCard() {
        val subtotalItemCount = selectedCartItems.size
        val subtotalAmount = selectedCartItems.sumOf { it.total_price }
        val shippingFee = 50.00 // Example flat rate shipping fee

        findViewById<TextView>(R.id.subtotalItemCountTV).text = "SUBTOTAL ($subtotalItemCount ITEM(S))"
        findViewById<TextView>(R.id.subtotalAmountTV).text = "SUBTOTAL: ₱${String.format("%.2f", subtotalAmount)}"

    }

    private fun getShakeAnimation(): Animation {
        return AnimationUtils.loadAnimation(this, R.anim.shake_animation)
    }

    private fun getUserFirstName(): String? {
        return sharedPreferences.getString("first_name", null)
    }

    private fun getUserLastName(): String? {
        return sharedPreferences.getString("last_name", null)
    }

    private fun getUserPhoneNumber(): String? {
        return sharedPreferences.getString("phone_number", null)
    }

}