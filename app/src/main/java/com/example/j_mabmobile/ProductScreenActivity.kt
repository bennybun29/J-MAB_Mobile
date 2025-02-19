package com.example.j_mabmobile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
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
    private lateinit var minusBtn: TextView
    private lateinit var plusBtn: TextView
    private lateinit var quantityText: TextView
    private var quantity = 1

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
        val backBtn: ImageButton = findViewById(R.id.backBtn)
        val addToCartBtn: LinearLayout = findViewById(R.id.addToCartBtn)
        val cartBtn: ImageButton = findViewById(R.id.cartBtn)
        val priceTextView: TextView = findViewById(R.id.priceTextView)
        val buyNowBtn: Button = findViewById(R.id.buyNowBtn)

        minusBtn = findViewById(R.id.minusBtn)
        plusBtn = findViewById(R.id.plusBtn)
        quantityText = findViewById(R.id.quantityText)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        cartBtn.setOnClickListener({
            onPause()
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        })

        buyNowBtn.setOnClickListener({
            onPause()
            val intent = Intent(this, CheckoutActivity::class.java)
            startActivity(intent)
        })

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
        priceTextView.text = "PHP: $price"

        Picasso.get().load(imageUrl).into(productImage)

        if (category == "Tires") {
            productVariation.visibility = View.VISIBLE
            productVariation.text = "Size: " + intent.getStringExtra("size")
        } else if (category == "Batteries") {
            productVariation.visibility = View.VISIBLE
            productVariation.text = "Voltage: " + intent.getStringExtra("voltage")
        }

        plusBtn.setOnClickListener {
            if (quantity < stock) {
                quantity++
                quantityText.text = quantity.toString()
            } else {
                Toast.makeText(this, "Max stock reached", Toast.LENGTH_SHORT).show()
            }
        }

        minusBtn.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityText.text = quantity.toString()
            }
        }


        addToCartBtn.setOnClickListener {
            token?.let {
                addToCart(userId, product_id, quantity, it)
            }

            animateCartEffect()
        }




        Log.d("ProductScreenActivity", "User ID: $userId")
        Log.d("ProductScreenActivity", "Token: $token")
        Log.d("ProductScreenActivity", "Product ID: $product_id")
    }

    private fun animateCartEffect() {
        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        val cartIcon = findViewById<ImageButton>(R.id.cartBtn)
        val addToCartIcon = findViewById<LinearLayout>(R.id.addToCartBtn)
            .findViewById<ImageView>(R.id.shopping_cart_image) // Get the ImageView inside LinearLayout


        val floatingCart = ImageView(this).apply {
            setImageResource(R.drawable.shopping_cart_colored)
            layoutParams = FrameLayout.LayoutParams(100, 100)
        }

        rootLayout.addView(floatingCart)

        // Get start and end positions
        val startLoc = IntArray(2)
        addToCartIcon.getLocationInWindow(startLoc)

        val endLoc = IntArray(2)
        cartIcon.getLocationInWindow(endLoc)

        val startX = startLoc[0].toFloat()
        val startY = startLoc[1].toFloat()
        val endX = endLoc[0].toFloat()
        val endY = endLoc[1].toFloat()

        floatingCart.x = startX
        floatingCart.y = startY

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                floatingCart.x = startX + fraction * (endX - startX)
                floatingCart.y = startY + fraction * (endY - startY)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootLayout.removeView(floatingCart)
                }
            })
        }

        // Apply scale animation
        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.cart_animation)
        cartIcon.startAnimation(scaleAnim)

        animator.start()
    }

    private fun addToCart(userId: Int, productId: Int, quantity: Int, token: String) {
        val cartRequest = CartRequest(userId, productId, quantity)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.addToCart("Bearer $token", cartRequest)

                if (response.isSuccessful) {
                    val cartResponse = response.body()
                    if (cartResponse?.success == true) {
                        Toast.makeText(this@ProductScreenActivity, "Added $quantity to cart!", Toast.LENGTH_SHORT).show()
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


