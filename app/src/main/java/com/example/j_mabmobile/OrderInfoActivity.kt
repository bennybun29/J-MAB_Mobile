package com.example.j_mabmobile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.CancelOrderRequest
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_info)

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

        // Retrieve data from intent
        val orderID = intent.getIntExtra("ORDER_ID", 0)
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "N/A"
        val productBrand = intent.getStringExtra("PRODUCT_BRAND") ?: "N/A"
        val quantity = intent.getIntExtra("QUANTITY", 0)
        val totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)
        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: "N/A"
        val paymentStatus = intent.getStringExtra("PAYMENT_STATUS") ?: "N/A"
        val orderStatus = intent.getStringExtra("ORDER_STATUS") ?: "N/A"
        val productImage = intent.getStringExtra("PRODUCT_IMAGE") ?: ""
        val homeAddress = intent.getStringExtra("HOME_ADDRESS") ?: "N/A"
        val baranggay = intent.getStringExtra("BARANGAY") ?: "N/A"
        val city = intent.getStringExtra("CITY") ?: "N/A"
        val reference = intent.getStringExtra("REFERENCE") ?: "N/A"
        val requestTimeRaw = intent.getStringExtra("REQUEST_TIME") ?: "N/A"

        // Format requestTime
        val formattedRequestTime = formatRequestTime(requestTimeRaw)

        // Set values to UI components
        productNameTV.text = productName
        productBrandTV.text = "Brand: $productBrand"
        quantityTV.text = "Quantity: $quantity"
        totalPriceTV.text = "Total Price: ₱$totalPrice"
        address.text = "$homeAddress, $baranggay, $city, Pangasinan"

        // Set bold text for selected fields
        setBoldText(referenceID, reference)
        setBoldText(requestTimeTV, formattedRequestTime)
        setBoldText(paymentMethodTV, paymentMethod)
        setBoldText(paymentStatusTV, paymentStatus)
        setBoldText(orderStatusTV, orderStatus)

        // Hide buttons if order is cancelled or payment failed
        if (orderStatus.equals("cancelled", ignoreCase = true) ||
            (orderStatus.equals("cancelled", ignoreCase = true) && paymentStatus.equals("failed", ignoreCase = true))) {
            cancelOrderBtn.visibility = View.GONE
            confirmOrderBtn.visibility = View.GONE
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
            alertDialog.dismiss() // Close the dialog
            cancelOrder(orderId)  // Call cancel order function
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

    private fun formatRequestTime(rawTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(rawTime)
            outputFormat.format(date ?: rawTime)
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
}
