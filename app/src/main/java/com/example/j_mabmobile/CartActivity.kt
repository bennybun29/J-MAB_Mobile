package com.example.j_mabmobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CartResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var totalPriceTV: TextView
    private lateinit var selectAllChkBox: CheckBox
    private val cartItems = mutableListOf<CartItem>()

    private var userId: Int = -1
    private var authToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        backBtn = findViewById(R.id.backBtn)
        recyclerViewCart = findViewById(R.id.recyclerViewCart)
        totalPriceTV = findViewById(R.id.totalPriceTV)
        selectAllChkBox = findViewById(R.id.selectAllChkBox)

        backBtn.setOnClickListener {
            onBackPressed()
        }


        cartAdapter = CartAdapter(cartItems, totalPriceTV, selectAllChkBox)
        recyclerViewCart.layoutManager = LinearLayoutManager(this)
        recyclerViewCart.adapter = cartAdapter

        getUserDataFromPreferences()

        selectAllChkBox.setOnCheckedChangeListener { _, isChecked ->
            cartAdapter.selectAllItems(isChecked)
        }

        if (userId != -1 && authToken != null) {
            fetchCartItems()
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserDataFromPreferences() {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)
        authToken = sharedPreferences.getString("jwt_token", null)

        if (authToken != null) {
            authToken = "Bearer $authToken"
        }
    }

    private fun fetchCartItems() {
        if (authToken == null || userId == -1) {
            Toast.makeText(this, "Invalid user credentials", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.getApiService(this).getCartItems(authToken!!, userId)
            .enqueue(object : Callback<CartResponse> {
                override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val cartResponse = response.body()
                        if (cartResponse?.success == true) {
                            cartItems.clear()
                            cartItems.addAll(cartResponse.cart)
                            cartAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(applicationContext, "Cart is empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Error: ${t.message}")
                    Toast.makeText(applicationContext, "API request failed", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleSelectAll(isChecked: Boolean) {
        if (isChecked) {
            // Calculate total price when all items are selected
            val totalPrice = cartItems.sumOf { it.total_price }
            totalPriceTV.text = "Total: ₱ ${"%.2f".format(totalPrice)}"
        } else {
            totalPriceTV.text = "Total: ₱ 0.00"
        }
    }
}
