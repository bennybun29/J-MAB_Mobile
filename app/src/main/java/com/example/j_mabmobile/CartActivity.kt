package com.example.j_mabmobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CartResponse
import com.example.j_mabmobile.model.UpdateCartRequest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.ContextCompat


class CartActivity : AppCompatActivity() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var totalPriceTV: TextView
    private lateinit var selectAllChkBox: CheckBox
    private lateinit var emptyIcon: ImageView
    private lateinit var noOrdersYetTV: TextView
    private lateinit var deleteTV: TextView
    private lateinit var checkoutBtn: Button
    private lateinit var horizontal_linear_layout: LinearLayout
    private lateinit var cartViewModel: CartViewModel


    private val cartItems = mutableListOf<CartItem>()

    private var userId: Int = -1
    private var authToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        window.statusBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }


        backBtn = findViewById(R.id.backBtn)
        recyclerViewCart = findViewById(R.id.recyclerViewCart)
        totalPriceTV = findViewById(R.id.totalPriceTV)
        selectAllChkBox = findViewById(R.id.selectAllChkBox)
        emptyIcon = findViewById(R.id.empytIcon)
        noOrdersYetTV = findViewById(R.id.noOrdersYetTV)
        deleteTV = findViewById(R.id.deleteTV)
        checkoutBtn = findViewById(R.id.checkoutBtn)
        horizontal_linear_layout = findViewById(R.id.horizontal_linear_layout)
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

        cartViewModel.cartItems.observe(this) { cartList ->
            cartItems.clear()
            cartItems.addAll(cartList)
            cartAdapter.notifyDataSetChanged()
            updateEmptyCartUI(cartList.isEmpty())
        }



        backBtn.setOnClickListener {
            onBackPressed()
        }


        cartAdapter = CartAdapter(
            cartItems,
            totalPriceTV,
            selectAllChkBox,
            { cartItem -> deleteCartItem(cartItem.cart_id) },
            { cartId, quantity -> updateCartItem(cartId, quantity) },
            { isEnabled -> updateCheckoutButton(isEnabled) }
        )

        recyclerViewCart.layoutManager = LinearLayoutManager(this)
        recyclerViewCart.adapter = cartAdapter

        getUserDataFromPreferences()

        selectAllChkBox.setOnCheckedChangeListener { _, isChecked ->
            cartAdapter.selectAllItems(isChecked)
        }

        if (userId != -1 && authToken != null) {
            cartViewModel.fetchCartItems(userId, this)
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        deleteTV.setOnClickListener {
            val selectedCartIds = cartAdapter.getSelectedCartIds()

            if (selectedCartIds.isNotEmpty()) {
                val cartIdsString = selectedCartIds
                showConfirmationDialog(cartIdsString)
            } else {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
            }
        }

        checkoutBtn.setOnClickListener {
            val selectedItems = cartAdapter.getSelectedItems()

            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "No items selected for checkout", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putParcelableArrayListExtra("selected_items", ArrayList(selectedItems))
            startActivity(intent)
        }

        updateCheckoutButton(false)
    }

    override fun onBackPressed() {
        val result = Intent()
        result.putExtra("cartUpdated", true)
        setResult(Activity.RESULT_OK, result)
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.fetchCartItems(userId, this) // Refresh cart badge when returning
    }

    private fun showConfirmationDialog(cartIds: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_remove_from_cart_dialog, null)
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
            deleteMultipleItems(cartIds)
            alertDialog.dismiss()
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

        RetrofitClient.getApiService(this).getCartItemsCall(userId)
            .enqueue(object : Callback<CartResponse> {
                override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val cartResponse = response.body()
                        if (cartResponse?.success == true) {
                            cartItems.clear()
                            cartItems.addAll(cartResponse.cart)
                            cartAdapter.notifyDataSetChanged()
                            updateEmptyCartUI(cartItems.isEmpty())

                        } else {
                            updateEmptyCartUI(true)
                        }
                    }
                }

                override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Error: ${t.message}")
                    Toast.makeText(applicationContext, "API request failed", Toast.LENGTH_SHORT).show()
                    updateEmptyCartUI(true)
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
                    .deleteCartItem(cartId.toString())

                response.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            val itemToRemove = cartItems.find { it.cart_id == cartId }
                            if (itemToRemove != null) {
                                cartItems.remove(itemToRemove)
                                cartAdapter.notifyDataSetChanged()
                                cartAdapter.updateTotalPrice()

                                if (cartItems.isEmpty()) {
                                    updateEmptyCartUI(true)
                                }

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

    private fun updateCartItem(cartId: Int, quantity: Int) {
        if (authToken == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UpdateCartRequest(quantity)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@CartActivity)
                    .updateCartItem(cartId, updateRequest)

                response.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Quantity updated successfully
                            Log.d("CartActivity", "Quantity Updated")
                        } else {
                            Toast.makeText(this@CartActivity, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("UpdateCartItemError", "Error: ${t.message}", t)
                        Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                Log.e("UpdateCartItemError", "Error: ${e.message}", e)
                Toast.makeText(this@CartActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyCartUI(isEmpty: Boolean) {
        if (isEmpty) {
            emptyIcon.visibility = View.VISIBLE
            noOrdersYetTV.visibility = View.VISIBLE
            deleteTV.visibility = View.GONE
            horizontal_linear_layout.visibility = View.GONE
        } else {
            emptyIcon.visibility = View.GONE
            noOrdersYetTV.visibility = View.GONE
            recyclerViewCart.visibility = View.VISIBLE
            selectAllChkBox.visibility = View.VISIBLE
            deleteTV.visibility = View.VISIBLE
            checkoutBtn.visibility = View.VISIBLE
            deleteTV.visibility = View.VISIBLE
            horizontal_linear_layout.visibility = View.VISIBLE
        }
    }


    private fun deleteMultipleItems(cartIds: String) {
        if (authToken == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@CartActivity)
                    .deleteCartItem(cartIds)

                response.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            cartItems.removeAll { item -> cartIds.split(",").contains(item.cart_id.toString()) }
                            cartAdapter.notifyDataSetChanged()
                            cartAdapter.selectedItems.removeAll { item -> cartIds.split(",").contains(item.cart_id.toString()) }
                            cartAdapter.updateTotalPrice()
                            cartAdapter.updateTotalPrice()

                            if (cartItems.isEmpty()) {
                                updateEmptyCartUI(true)
                            }


                            Toast.makeText(this@CartActivity, "Items removed from cart", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CartActivity, "Failed to remove items", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("DeleteCartItemsError", "Error: ${t.message}")
                        Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                Log.e("DeleteCartItemsError", "Error: ${e.message}", e)
                Toast.makeText(this@CartActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateCheckoutButton(isEnabled: Boolean) {
        Log.d("CartActivity", "updateCheckoutButton called with isEnabled = $isEnabled")
        checkoutBtn.isEnabled = isEnabled
        checkoutBtn.setBackgroundColor(if (isEnabled) getColor(R.color.j_mab_blue) else getColor(R.color.LTGRAY))
    }

}
