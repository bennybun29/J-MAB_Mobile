package com.example.j_mabmobile

import ImageCarouselAdapter
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CartRequest
import com.example.j_mabmobile.model.Product
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.logging.Handler

class ProductScreenActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var minusBtn: TextView
    private lateinit var plusBtn: TextView
    private lateinit var quantityText: TextView
    private var quantity = 1
    private lateinit var overlayBackground: View
    private lateinit var addToCartOverlay: CardView
    private lateinit var recommendedProductsRecycler: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private var recommendedProducts = listOf<Product>()
    private var userId: Int = 0
    private lateinit var shimmerLayout: com.facebook.shimmer.ShimmerFrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_screen)

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService::class.java)

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
        val backButton: TextView = findViewById(R.id.backButton)
        val goToCartButton: Button= findViewById(R.id.goToCartButton)
        val imageCountTextView: TextView = findViewById(R.id.image_count)
        val imageCarousel: ViewPager2 = findViewById(R.id.image_carousel)
        overlayBackground = findViewById(R.id.overlayBackground)
        addToCartOverlay = findViewById(R.id.addToCartOverlay)
        recommendedProductsRecycler = findViewById(R.id.recommendedProductsRecycler)
        recommendedProductsRecycler.layoutManager = GridLayoutManager(this,2)
        userId = intent.getIntExtra("user_id", 0)
        shimmerLayout = findViewById(R.id.shimmerLayout)


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

        Log.d("DEBUG", "Received token: $token")

        buyNowBtn.setOnClickListener {
            onPause()

            val selectedItem = CartItem(
                cart_id = 0,  // Set to 0 since it's not from the cart
                user_id = userId,
                product_id = product_id,
                product_name = name ?: "Unknown",
                product_image = imageUrl ?: "",
                product_price = price,
                product_brand = brand ?: "Unknown",
                product_description = description ?: "No description",
                product_stock = stock,
                quantity = quantity  // Get the current selected quantity
            )

            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putParcelableArrayListExtra("selected_items", arrayListOf(selectedItem))
            startActivity(intent)
        }


        productName.text = name
        productDescription.text = description
        productStock.text = "Stock Available: $stock"
        productBrand.text = "Brand: $brand"
        priceTextView.text = "PHP: ${formatPrice(price)}"

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
        }

        backButton.setOnClickListener {
            hideAddToCartOverlay()
        }

        overlayBackground.setOnClickListener{
            hideAddToCartOverlay()
        }

        goToCartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val imageUrls = listOf(
            imageUrl ?: "",
            imageUrl ?: "",
            imageUrl ?:""
        )

        val adapter = ImageCarouselAdapter(imageUrls)
        imageCarousel.adapter = adapter

        imageCountTextView.text = "1/${imageUrls.size}"

        imageCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                imageCountTextView.text = "${position + 1}/${imageUrls.size}"
            }
        })


        Log.d("ProductScreenActivity", "User ID: $userId")
        Log.d("ProductScreenActivity", "Token: $token")
        Log.d("ProductScreenActivity", "Product ID: $product_id")
    }

    /*
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

        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.cart_animation)
        cartIcon.startAnimation(scaleAnim)

        animator.start()
    }*/

    private fun addToCart(userId: Int, productId: Int, quantity: Int, token: String) {
        Log.d("DEBUG", "Token being sent: $token")
        Log.d("DEBUG", "User ID: $userId, Product ID: $productId, Quantity: $quantity")

        val cartRequest = CartRequest(userId, productId, quantity)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.addToCart("Bearer $token", cartRequest)

                if (response.isSuccessful) {
                    val cartResponse = response.body()
                    if (cartResponse?.success == true) {
                        showAddToCartOverlay()
                        fetchRecommendedProducts(userId)
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


    private fun showAddToCartOverlay() {
        overlayBackground.visibility = View.VISIBLE
        addToCartOverlay.visibility = View.VISIBLE

        recommendedProductsRecycler.visibility = View.GONE

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        GlobalScope.launch(Dispatchers.Main) {
            delay(2000)

            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE

            fetchRecommendedProducts(userId)
        }
    }




    private fun hideAddToCartOverlay() {
        overlayBackground.visibility = View.GONE
        addToCartOverlay.visibility = View.GONE
    }

    private fun fetchRecommendedProducts(userId: Int) {
        Log.d("API_DEBUG", "Fetching recommended products for userId: $userId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getProducts().execute()
                Log.d("API_DEBUG", "Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val allProducts = response.body()!!.products
                    Log.d("API_DEBUG", "Products fetched: ${allProducts.size}")

                    val recommended = allProducts.shuffled().take(10)

                    launch(Dispatchers.Main) {
                        recommendedProducts = recommended
                        recyclerAdapter = RecyclerAdapter(recommendedProducts, userId)
                        recommendedProductsRecycler.adapter = recyclerAdapter

                        recommendedProductsRecycler.postDelayed({
                            recommendedProductsRecycler.alpha = 0f
                            recommendedProductsRecycler.visibility = View.VISIBLE
                            recommendedProductsRecycler.animate()
                                .alpha(1f)
                                .setDuration(500)
                                .setInterpolator(DecelerateInterpolator())
                                .start()
                        }, 1000)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_DEBUG", "Failed to fetch recommended products. Error: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Error fetching recommended products", e)
            }
        }
    }

    private fun formatPrice(price: Double): String {
        return if (price > 100) {
            NumberFormat.getNumberInstance(Locale.US).format(price)
        } else {
            "%.2f".format(price)
        }
    }


}


