package com.example.j_mabmobile

import ImageCarouselAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartItem
import com.example.j_mabmobile.model.CartRequest
import com.example.j_mabmobile.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
    private lateinit var cartBadge: TextView
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>
    private lateinit var imageCarousel: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_screen)

        window.statusBarColor = resources.getColor(R.color.j_mab_blue, theme)
        window.navigationBarColor = resources.getColor(R.color.j_mab_blue, theme)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

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
        val goToCartButton: Button = findViewById(R.id.goToCartButton)
        val imageCountTextView: TextView = findViewById(R.id.image_count)
        imageCarousel = findViewById(R.id.image_carousel)
        val helpBtn: ImageButton = findViewById(R.id.helpBtn)
        val fromRecommended = intent.getBooleanExtra("from_recommended", false)

        overlayBackground = findViewById(R.id.overlayBackground)
        addToCartOverlay = findViewById(R.id.addToCartOverlay)
        recommendedProductsRecycler = findViewById(R.id.recommendedProductsRecycler)
        recommendedProductsRecycler.layoutManager = GridLayoutManager(this, 2)
        cartBadge = findViewById(R.id.cartBadge)
        userId = intent.getIntExtra("user_id", 0)
        shimmerLayout = findViewById(R.id.shimmerLayout)

        val cardView = findViewById<CardView>(R.id.cardView)
        bottomSheetBehavior = BottomSheetBehavior.from(cardView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val rootView = findViewById<View>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // Now set up the BottomSheet after we know the insets
            imageCountTextView.post {
                // Calculate where the bottom of the imageCountTextView is relative to screen top
                val imageCountLocation = IntArray(2)
                imageCountTextView.getLocationOnScreen(imageCountLocation)
                val imageCountBottom = imageCountLocation[1] + imageCountTextView.height

                // Get total screen height, accounting for system insets
                val screenHeight = resources.displayMetrics.heightPixels

                // Set the bottom sheet's peek height to start exactly at the bottom of the imageCountTextView
                // Subtract the navigation bar height for gesture navigation devices
                bottomSheetBehavior.peekHeight = screenHeight - imageCountBottom - systemInsets.bottom

                // Make sure the card is tall enough to fill the rest of the screen when dragged up
                cardView.layoutParams.height = screenHeight - systemInsets.bottom
                cardView.requestLayout()

                // Calculate top buttons offset for expanded state
                val backLocation = IntArray(2)
                backBtn.getLocationOnScreen(backLocation)

                // Use the back button's position as a reference for where to stop expanding
                val topOffset = backLocation[1] + backBtn.height + dp(20) // Add some padding

                // Set expanded offset through reflection (optional if needed)
                try {
                    val behaviorClass = BottomSheetBehavior::class.java
                    val field = behaviorClass.getDeclaredField("maxHeight")
                    field.isAccessible = true
                    field.set(bottomSheetBehavior, screenHeight - topOffset - systemInsets.bottom)
                } catch (e: Exception) {
                    Log.e("BottomSheet", "Could not set maxHeight: ${e.message}")
                }
            }

            insets
        }

// Handle BottomSheet state changes
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // No state change handling needed
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Ensure slideOffset is within [0,1] range
                val offset = slideOffset.coerceIn(0f, 1f)

                // Apply opacity effect smoothly as sheet slides up
                imageCarousel.alpha = 1 - offset
            }
        })

        cartViewModel.cartItemCount.observe(this) { count ->
            updateCartBadge(count)
        }

        cartViewModel.fetchCartItems(userId, this)

        minusBtn = findViewById(R.id.minusBtn)
        plusBtn = findViewById(R.id.plusBtn)
        quantityText = findViewById(R.id.quantityText)

        backBtn.setOnClickListener {
            if (fromRecommended) {
                finish()
            } else {
                onBackPressed()
            }
        }

        cartBtn.setOnClickListener {
            onPause()
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        helpBtn.setOnClickListener {
            onPause()
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        val product_id = intent.getIntExtra("product_id", 0)
        val imageUrl = intent.getStringExtra("product_image_url")
        val name = intent.getStringExtra("product_name")
        val description = intent.getStringExtra("product_description")
        val stock = intent.getIntExtra("product_stock", 0)
        val brand = intent.getStringExtra("product_brand")
        val category = intent.getStringExtra("product_category")
        val price = intent.getDoubleExtra("product_price", 0.0)
        val token = intent.getStringExtra("jwt_token")

        Log.d("DEBUG", "Received token: $token")

        buyNowBtn.setOnClickListener {
            token?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // First, check if the product already exists in the cart
                        val cartResponse = apiService.getCartItemsSuspend(userId)

                        if (cartResponse.isSuccessful) {
                            val cartItems = cartResponse.body()?.cart ?: emptyList()
                            val existingCartItem = cartItems.find { it.product_id == product_id }

                            if (existingCartItem != null) {
                                // Show custom dialog if product already exists in the cart
                                val dialogView = layoutInflater.inflate(R.layout.custom_cart_dialog, null)
                                val dialogBuilder = AlertDialog.Builder(this@ProductScreenActivity)
                                    .setView(dialogView)

                                val alertDialog = dialogBuilder.create()
                                alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                                alertDialog.show()

                                dialogView.findViewById<Button>(R.id.btnYes).setOnClickListener {
                                    val intent = Intent(this@ProductScreenActivity, CartActivity::class.java)
                                    startActivity(intent)
                                    alertDialog.dismiss()
                                }

                                dialogView.findViewById<Button>(R.id.btnNo).setOnClickListener {
                                    alertDialog.dismiss()
                                }

                                alertDialog.show()
                                return@launch
                            }
                        }

                        // If not in cart, proceed to buy now
                        val cartRequest = CartRequest(userId, product_id, quantity)
                        val response = apiService.addToCart(cartRequest)

                        if (response.isSuccessful && response.body()?.success == true) {
                            val cartItemResponse = apiService.getCartItemsSuspend(userId)

                            if (cartItemResponse.isSuccessful) {
                                val cartItems = cartItemResponse.body()?.cart ?: emptyList()
                                val cartItem = cartItems.find { it.product_id == product_id }

                                if (cartItem != null) {
                                    val intent = Intent(this@ProductScreenActivity, CheckoutActivity::class.java)
                                    intent.putParcelableArrayListExtra("selected_items", arrayListOf(cartItem))
                                    intent.putExtra("from_buy_now", true)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this@ProductScreenActivity, "Added to cart but couldn't retrieve cart information", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@ProductScreenActivity, "Added to cart but couldn't retrieve cart details", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val errorMessage = response.body()?.errors?.get(0) ?: "Unknown error"
                            Toast.makeText(this@ProductScreenActivity, "Failed to add to cart: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ProductScreenActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        productName.text = name
        productDescription.text = description
        productStock.text = "Stock Available: $stock"
        productBrand.text = "Brand: $brand"
        priceTextView.text = "₱ ${formatPrice(price)}"

        if (category == "Tires") {
            productVariation.visibility = View.VISIBLE
            productVariation.text = "Size: " + intent.getStringExtra("size")
        } else if (category == "Batteries") {
            productVariation.visibility = View.VISIBLE
            productVariation.text = "Voltage: " + intent.getStringExtra("voltage")
        }

        var hasShownMaxStockToast = false

        plusBtn.setOnClickListener {
            if (quantity < stock) {
                quantity++
                quantityText.text = quantity.toString()
                hasShownMaxStockToast = false
            } else {
                if (!hasShownMaxStockToast) {
                    Toast.makeText(this, "Max stock reached", Toast.LENGTH_SHORT).show()
                    hasShownMaxStockToast = true
                }
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

        overlayBackground.setOnClickListener {
            hideAddToCartOverlay()
        }

        goToCartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val imageUrls = listOf(
            imageUrl ?: "",
            imageUrl ?: "",
            imageUrl ?: ""
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

    override fun onResume() {
        super.onResume()
        cartViewModel.fetchCartItems(userId, this) // Refresh cart badge when returning
    }

    private fun updateCartBadge(count: Int) {
        if (count > 0) {
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = if (count > 10) "10+" else count.toString()
        } else {
            cartBadge.visibility = View.GONE
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun addToCart(userId: Int, productId: Int, quantity: Int, token: String) {
        Log.d("DEBUG", "Token being sent: $token")
        Log.d("DEBUG", "User ID: $userId, Product ID: $productId, Quantity: $quantity")

        val cartRequest = CartRequest(userId, productId, quantity)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch the current cart items first
                val cartResponse = apiService.getCartItemsSuspend(userId)
                val productStock = intent.getIntExtra("product_stock", 0)
                if (cartResponse.isSuccessful) {
                    val cartItems = cartResponse.body()?.cart ?: emptyList()
                    val existingCartItem = cartItems.find { it.product_id == productId }
                    val existingQuantity = existingCartItem?.quantity ?: 0

                    val totalQuantity = existingQuantity + quantity

                    if (totalQuantity > productStock) {
                        Toast.makeText(
                            this@ProductScreenActivity,
                            "Exceeds stock! Max: $productStock, In cart: $existingQuantity",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                }

                // Now proceed with adding to cart
                val response = apiService.addToCart(cartRequest)
                if (response.isSuccessful) {
                    val cartResponse = response.body()
                    if (cartResponse?.success == true) {
                        showAddToCartOverlay()
                        fetchRecommendedProducts(userId)
                        cartViewModel.fetchCartItems(userId, this@ProductScreenActivity)
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
            delay(5000)

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

                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE
                        recommendedProductsRecycler.visibility = View.VISIBLE

                        recommendedProductsRecycler.alpha = 0f
                        recommendedProductsRecycler.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
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