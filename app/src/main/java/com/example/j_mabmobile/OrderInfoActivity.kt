package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity.CENTER
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.CancelOrderRequest
import com.example.j_mabmobile.model.PostRatingResponse
import com.example.j_mabmobile.model.RatingRequest
import com.example.j_mabmobile.model.RatingResponse
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderInfoActivity : AppCompatActivity() {

    private lateinit var productNameTV: TextView
    private lateinit var productBrandTV: TextView
    private lateinit var quantityTV: TextView
    private lateinit var totalPriceTV: TextView
    private lateinit var paymentMethodTV: TextView
    private lateinit var paymentStatusTV: TextView
    private lateinit var orderStatusTV: TextView
    private lateinit var productImageIV: ImageView
    private lateinit var address: TextView
    private lateinit var referenceID: TextView
    private lateinit var requestTimeTV: TextView
    private lateinit var backBtn: ImageButton
    private lateinit var cancelOrderBtn: Button
    private lateinit var confirmOrderBtn: Button
    private lateinit var chatSeller: Button
    private lateinit var contactSeller: Button
    private lateinit var etaTV: TextView
    private lateinit var stars: Array<ImageView>
    private var selectedRating = 0f
    private lateinit var topCardView: CardView
    private lateinit var item_size: TextView
    private lateinit var topCardViewDelivered: CardView
    private lateinit var dateDeliveredTV: TextView
    private lateinit var orderInfoTitle: TextView
    private lateinit var supportCenterCV: CardView
    private lateinit var topCardViewOutForDelivery: CardView
    private lateinit var userNameTV: TextView
    private lateinit var userPhoneNumberTV: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_info)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

        val ratingLayout = findViewById<View>(R.id.ratingLayout)
        stars = arrayOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )
        val submitButton = findViewById<Button>(R.id.submitRatingButton)

        // Initialize UI components
        productNameTV = findViewById(R.id.item_text)
        productBrandTV = findViewById(R.id.item_brand)
        quantityTV = findViewById(R.id.item_quantity)
        totalPriceTV = findViewById(R.id.productPriceTV)
        paymentMethodTV = findViewById(R.id.paymentMethodTV)
        paymentStatusTV = findViewById(R.id.paymentStatusTV)
        orderStatusTV = findViewById(R.id.orderStatusTV)
        productImageIV = findViewById(R.id.item_image)
        address = findViewById(R.id.userAddress)
        requestTimeTV = findViewById(R.id.requestTime)
        referenceID = findViewById(R.id.referenceID)
        backBtn = findViewById(R.id.backBtn)
        cancelOrderBtn = findViewById(R.id.cancelOrderButton)
        confirmOrderBtn = findViewById(R.id.confirmOderButton)
        chatSeller = findViewById(R.id.chatSeller)
        contactSeller = findViewById(R.id.contactSeller)
        etaTV = findViewById(R.id.etaTV)
        dateDeliveredTV = findViewById(R.id.dateDeliveredTV)
        topCardView = findViewById(R.id.topCardView)
        topCardViewDelivered = findViewById(R.id.topCardViewDelivered)
        topCardViewOutForDelivery = findViewById(R.id.topCardViewOutForDelivery)
        item_size = findViewById(R.id.item_size)
        orderInfoTitle = findViewById(R.id.orderInfoTitle)
        supportCenterCV  = findViewById(R.id.supportCenter)
        userNameTV = findViewById(R.id.userNameTV)
        userPhoneNumberTV = findViewById(R.id.userPhoneNumberTV)
        sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)

        // Retrieve data from intent
        val orderID = intent.getIntExtra("ORDER_ID", 0)
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "N/A"
        val productBrand = intent.getStringExtra("PRODUCT_BRAND") ?: "N/A"
        val quantity = intent.getIntExtra("QUANTITY", 0)
        val totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)
        val rawPaymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: "N/A"
        val paymentMethod = if (rawPaymentMethod.equals("cod", ignoreCase = true)) "Cash On Delivery" else rawPaymentMethod
        val paymentStatus = intent.getStringExtra("PAYMENT_STATUS") ?: "N/A"
        val orderStatus = intent.getStringExtra("ORDER_STATUS") ?: "N/A"
        val productImage = intent.getStringExtra("PRODUCT_IMAGE") ?: ""
        val homeAddress = intent.getStringExtra("HOME_ADDRESS") ?: "N/A"
        val baranggay = intent.getStringExtra("BARANGAY") ?: "N/A"
        val city = intent.getStringExtra("CITY") ?: "N/A"
        val reference = intent.getStringExtra("REFERENCE") ?: "N/A"
        val requestTimeRaw = intent.getStringExtra("REQUEST_TIME") ?: "N/A"
        val size = intent.getStringExtra("SIZE")?: "N/A"
        val formattedRequestTime = formatRequestTime(requestTimeRaw)
        requestTimeTV.text = boldText(formattedRequestTime)
        val etaTime = calculateEta(requestTimeRaw)
        setBoldText(etaTV, etaTime)
        val firstName = getUserFirstName()
        val lastName = getUserLastName()
        val phoneNumber = getUserPhoneNumber()

        userNameTV.text = "${firstName} ${lastName}"
        userPhoneNumberTV.text = phoneNumber ?: "No phone number"

        val titleText = when (orderStatus.lowercase()) {
            "pending" -> "To Pay"
            "processing" -> "To Ship"
            "out for delivery" -> "To Receive"
            "delivered" -> "To Rate"
            "cancelled" -> "Cancelled Orders"
            "failed" -> "Failed Orders"
            else -> "Order Details"
        }


        orderInfoTitle.text = boldText(titleText)

        productNameTV.text = boldText(productName)
        productBrandTV.text = boldText("Brand: ${productBrand}")
        quantityTV.text = boldText("Quantity: ${quantity.toString()}")
        item_size.text = boldText("Size: ${size}")
        val formattedPrice = NumberFormat.getNumberInstance().format(totalPrice)
        totalPriceTV.text = boldText("₱$formattedPrice")
        paymentMethodTV.text = boldText(paymentMethod)
        paymentStatusTV.text = boldText(paymentStatus)
        orderStatusTV.text = boldText(orderStatus)
        referenceID.text = boldTextOnly(reference)  // Keep original formatting (bold only, no capitalization)
        requestTimeTV.text = boldText(formattedRequestTime)

        val formattedAddress = listOf(homeAddress, baranggay, city, "Pangasinan")
            .joinToString(", ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }
        address.text = boldText(formattedAddress)


        topCardView.visibility = View.VISIBLE
        topCardViewDelivered.visibility = View.GONE

        if (orderStatus.equals("cancelled", ignoreCase = true)) {
            // Only for cancelled orders, hide the top card view
            topCardView.visibility = View.GONE
            cancelOrderBtn.visibility = View.GONE
            confirmOrderBtn.visibility = View.GONE
        } else if (orderStatus.equals("cancelled", ignoreCase = true) && paymentStatus.equals("failed", ignoreCase = true)) {
            cancelOrderBtn.visibility = View.GONE
            confirmOrderBtn.visibility = View.GONE
        } else if (orderStatus.equals("processing", ignoreCase = true)) {
            cancelOrderBtn.visibility = View.GONE
            confirmOrderBtn.visibility = View.GONE
        } else if (orderStatus.equals("delivered", ignoreCase = true) && paymentStatus.equals("paid", ignoreCase = true)) {
            // Show delivered card view instead of the ETA card
            topCardViewDelivered.visibility = View.VISIBLE
            topCardView.visibility = View.GONE

            // Set the delivered date (you can use the formatted request time or calculate delivered date)
            val deliveredDate = calculateFutureDate(requestTimeRaw, 5)
            setBoldText(dateDeliveredTV, deliveredDate)

            // The rest of your delivered status handling
            ratingLayout.visibility = View.VISIBLE
            cancelOrderBtn.visibility = View.GONE
            confirmOrderBtn.visibility = View.GONE
            supportCenterCV.visibility = View.GONE
        } else if (orderStatus.equals("out for delivery", ignoreCase = true)) {
            // Show the regular ETA card for orders in transit
            topCardView.visibility = View.GONE
            topCardViewDelivered.visibility = View.GONE
            topCardViewOutForDelivery.visibility = View.VISIBLE
            cancelOrderBtn.visibility = View.GONE
            confirmOrderBtn.visibility = View.GONE
        } else {
            // For other states, show the regular ETA card
            topCardView.visibility = View.GONE
            topCardViewDelivered.visibility = View.GONE
        }

        // Set click listeners for each star
        for (i in stars.indices) {
            stars[i].setOnClickListener {
                updateStars(i + 1f)
            }
        }

        // Check if the variant has already been rated
        checkRatingStatus()

        submitButton.setOnClickListener {
            if (selectedRating > 0) {
                val apiService = RetrofitClient.getApiService(this)

                val variantId = intent.getIntExtra("VARIANT_ID", 0) // Retrieve order ID from intent
                Log.d("DEBUG", "Received Variant ID: $variantId")
                val ratingRequest = RatingRequest(variantId, selectedRating)

                apiService.postRating(ratingRequest).enqueue(object : Callback<PostRatingResponse> {
                    override fun onResponse(call: Call<PostRatingResponse>, response: Response<PostRatingResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@OrderInfoActivity, "Rating submitted successfully!", Toast.LENGTH_SHORT).show()
                            disableRating()
                        } else {
                            Toast.makeText(this@OrderInfoActivity, "Failed to submit rating", Toast.LENGTH_SHORT).show()
                            Log.e("RATING_ERROR", "Response: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<PostRatingResponse>, t: Throwable) {
                        Toast.makeText(this@OrderInfoActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("RATING_ERROR", "API call failed", t)
                    }
                })
            } else {
                Toast.makeText(this, "Please select a rating!", Toast.LENGTH_SHORT).show()
            }
        }


        backBtn.setOnClickListener {
            onBackPressed()
        }

        contactSeller.setOnClickListener{
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        chatSeller.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "MESSAGE")
            intent.putExtra("FROM_ORDER_INFO", true) // Extra flag
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        confirmOrderBtn.setOnClickListener{
            onBackPressed()
        }

        Picasso.get()
            .load(productImage)
            .placeholder(R.drawable.jmab_logo)
            .error(R.drawable.jmab_logo)
            .into(productImageIV)

        cancelOrderBtn.setOnClickListener {
            showCancelOrderDialog(orderID)
        }
    }

    private fun showCancelOrderDialog(orderId: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_cancel_order_alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        // Get dialog buttons
        val btnCancel = dialogView.findViewById<Button>(R.id.btnNo)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnYes)

        // Dismiss dialog when "No" is clicked
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        // Proceed to cancel order when "Yes" is clicked
        btnConfirm.setOnClickListener {
            alertDialog.dismiss()
            cancelOrder(orderId)
        }
    }


    private fun cancelOrder(orderId: Int) {
        val apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService::class.java)
        val request = CancelOrderRequest(status = "cancelled")

        apiService.cancelOrders(orderId, request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DEBUG", "Order cancelled successfully: ${response.body()?.message}")
                    Toast.makeText(this@OrderInfoActivity, "Order cancelled successfully", Toast.LENGTH_SHORT).show()

                    // Update UI to reflect cancellation
                    orderStatusTV.text = "Cancelled"
                    setBoldText(orderStatusTV, "Cancelled")

                    // Hide cancel & confirm buttons
                    cancelOrderBtn.visibility = View.GONE
                    confirmOrderBtn.visibility = View.GONE
                } else {
                    Log.e("DEBUG", "Failed to cancel order: ${response.errorBody()?.string()}")
                    Toast.makeText(this@OrderInfoActivity, "Failed to cancel order", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("DEBUG", "API call failed", t)
                Toast.makeText(this@OrderInfoActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // For request time formatting only (no days added)
    private fun formatRequestTime(rawTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(rawTime)
            date?.let { outputFormat.format(it) } ?: rawTime
        } catch (e: Exception) {
            rawTime
        }
    }

    private fun calculateEta(rawTime: String): String {
        return calculateFutureDate(rawTime, 5)
    }

    private fun calculateFutureDate(rawTime: String, daysToAdd: Int): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            val calendar = Calendar.getInstance()
            val date = inputFormat.parse(rawTime)

            if (date != null) {
                calendar.time = date
                calendar.add(Calendar.DAY_OF_MONTH, daysToAdd) // Add specified days
                outputFormat.format(calendar.time)
            } else {
                rawTime
            }
        } catch (e: Exception) {
            rawTime
        }
    }




    private fun setBoldText(textView: TextView, value: String) {
        val spannableString = SpannableString(value)
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            value.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString
    }

    private fun updateStars(rating: Float) {
        selectedRating = rating
        for (i in stars.indices) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.filled_star)
            } else {
                stars[i].setImageResource(R.drawable.unfilled_star)
            }
        }
    }

    private fun boldText(value: String): SpannableString {
        val capitalizedValue = value.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val spannable = SpannableString(capitalizedValue)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, capitalizedValue.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun boldTextOnly(value: String): SpannableString {
        val spannable = SpannableString(value)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun checkRatingStatus() {
        val userId = getUserID()
        val variantId = intent.getIntExtra("VARIANT_ID", 0)

        val apiService = RetrofitClient.getApiService(this)

        apiService.hasRated(userId).enqueue(object : Callback<RatingResponse> {
            override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                if (response.isSuccessful) {
                    val ratedItems = response.body()?.items ?: emptyList()

                    // Find the specific rated item for this variant
                    val ratedItem = ratedItems.find {
                        it.variant_id == variantId && it.rating_status == "Rated"
                    }

                    if (ratedItem != null) {
                        // If item is rated and has a rating between 1-5
                        ratedItem.rating?.let { rating ->
                            if (rating in 1..5) {
                                // Disable further rating
                                disableRating()

                                // Set the existing rating
                                updateStars(rating.toFloat())
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                Log.e("RatingCheck", "Failed to check rating status", t)
            }
        })
    }

    private fun disableRating() {
        // Disable star clicks
        for (star in stars) {
            star.isClickable = false
            star.isEnabled = false
        }

        // Hide submit button
        findViewById<Button>(R.id.submitRatingButton).visibility = View.GONE

        // Optionally, show a message that item has been rated
        val ratingLayout = findViewById<View>(R.id.ratingLayout)
        val alreadyRatedTextView = TextView(this)
        alreadyRatedTextView.text = "This item has already been rated"
        alreadyRatedTextView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        alreadyRatedTextView.gravity = Gravity.CENTER

        // Replace or add the TextView to the rating layout
        (ratingLayout as ViewGroup).addView(alreadyRatedTextView)
    }

    private fun getUserID(): Int {
        return sharedPreferences.getInt("user_id", 1)
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
