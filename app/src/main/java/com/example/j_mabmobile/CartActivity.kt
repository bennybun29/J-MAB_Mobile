package com.example.j_mabmobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CartResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var totalPriceTV: TextView
    private lateinit var selectAllChkBox: CheckBox
    private lateinit var emptyIcon: ImageView
    private lateinit var noOrdersYetTV: TextView

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
        emptyIcon = findViewById(R.id.empytIcon)
        noOrdersYetTV = findViewById(R.id.noOrdersYetTV)


        backBtn.setOnClickListener {
            onBackPressed()
        }


        cartAdapter = CartAdapter(cartItems, totalPriceTV, selectAllChkBox,) {
            cartItem -> deleteCartItem(cartItem.cart_id)
        }
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
                        if (cartResponse?.success == true && cartResponse.cart.isNotEmpty()) {
                            cartItems.clear()
                            cartItems.addAll(cartResponse.cart)
                            cartAdapter.notifyDataSetChanged()
                            emptyIcon.visibility = View.GONE
                            noOrdersYetTV.visibility = View.GONE
                            recyclerViewCart.visibility = View.VISIBLE
                        } else {
                            // Handle empty cart scenario without an error message
                            emptyIcon.visibility = View.VISIBLE
                            noOrdersYetTV.visibility = View.VISIBLE
                            recyclerViewCart.visibility = View.GONE
                        }
                    } else if (response.code() == 404) {
                        // If the cart is empty (404), just show the empty state UI and don't show a toast
                        emptyIcon.visibility = View.VISIBLE
                        noOrdersYetTV.visibility = View.VISIBLE
                        recyclerViewCart.visibility = View.GONE
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

    private fun deleteCartItem(cartId: Int) {
        if (authToken == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Use Call<ApiResponse> instead of Response<ApiResponse>
                val response = RetrofitClient.getApiService(this@CartActivity)
                    .deleteCartItem(authToken!!, cartId.toString())

                response.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Remove the item from the local list and notify the adapter
                            val itemToRemove = cartItems.find { it.cart_id == cartId }
                            if (itemToRemove != null) {
                                cartItems.remove(itemToRemove)
                                cartAdapter.notifyDataSetChanged()
                                Toast.makeText(this@CartActivity, "Item removed from cart", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@CartActivity, "Failed to remove item", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        // Log the error message in Logcat
                        Log.e("DeleteCartItemError", "Error: ${t.message}", t)
                        Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                // Log the error message in Logcat
                Log.e("DeleteCartItemError", "Error: ${e.message}", e)
                Toast.makeText(this@CartActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
