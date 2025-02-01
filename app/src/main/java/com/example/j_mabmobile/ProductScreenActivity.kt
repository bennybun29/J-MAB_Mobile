package com.example.j_mabmobile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartRequest
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductScreenActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_screen)

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService::class.java)

        val productImage: ImageView = findViewById(R.id.item_image)
        val productName: TextView = findViewById(R.id.item_text)
        val productDescription: TextView = findViewById(R.id.item_description)
        val productStock: TextView = findViewById(R.id.stock_text)
        val productBrand: TextView = findViewById(R.id.brand_text)
        val productVariation: TextView = findViewById(R.id.variationTV)
        val productPrice: TextView = findViewById(R.id.item_price)
        val backBtn: ImageButton = findViewById(R.id.backBtn)
        val addToCartBtn: LinearLayout = findViewById(R.id.addToCartBtn)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        // Get data from intent
        val product_id = intent.getIntExtra("product_id", 0)
        val imageUrl = intent.getStringExtra("product_image_url")
        val name = intent.getStringExtra("product_name")
        val description = intent.getStringExtra("product_description")
        val stock = intent.getIntExtra("product_stock", 0)
        val brand = intent.getStringExtra("product_brand")
        val category = intent.getStringExtra("product_category")
        val price = intent.getDoubleExtra("product_price", 0.0)
        val token = intent.getStringExtra("jwt_token")
        val userId = intent.getIntExtra("user_id", 0)

        productName.text = name
        productDescription.text = description
        productStock.text = "Stock Available: $stock"
        productBrand.text = "Brand: $brand"
        productPrice.text = "₱ $price"

        Picasso.get().load(imageUrl).into(productImage)

        if (category == "Tires") {
            productVariation.visibility = View.VISIBLE
            productVariation.text = "Size: " + intent.getStringExtra("size")
        } else if (category == "Batteries") {
            productVariation.visibility = View.VISIBLE
            productVariation.text = "Voltage: " + intent.getStringExtra("voltage")
        }

        addToCartBtn.setOnClickListener {
            token?.let {
                addToCart(userId, product_id, 1, it) // Assuming quantity = 1 for simplicity
            }
        }




        Log.d("ProductScreenActivity", "User ID: $userId")
        Log.d("ProductScreenActivity", "Token: $token")
        Log.d("ProductScreenActivity", "Product ID: $product_id")
    }

    private fun addToCart(userId: Int, productId: Int, quantity: Int, token: String) {
        val cartRequest = CartRequest(userId, productId, quantity)

        // Make the network call using Retrofit
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.addToCart("Bearer $token", cartRequest)

                if (response.isSuccessful) {
                    val cartResponse = response.body()
                    if (cartResponse?.success == true) {
                        Toast.makeText(this@ProductScreenActivity, "Product added to cart!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = cartResponse?.errors?.get(0) ?: "Unknown error"
                        Toast.makeText(this@ProductScreenActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProductScreenActivity, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductScreenActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


