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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
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
import com.example.j_mabmobile.model.AverageRatingResponse
import com.example.j_mabmobile.model.ProductResponse
import com.example.j_mabmobile.model.RatingByIDResponse
import com.example.j_mabmobile.model.RatingResponse
import com.example.j_mabmobile.model.Variant
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicInteger

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
    private lateinit var imageCountTextView: TextView
    private lateinit var backButton: TextView
    private var selectedVariantId: Int = 0
    private lateinit var ratingText: TextView
    private var productId: Int = 0
    private var variantId: Int = 0
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_screen)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService::class.java)

        val productName: TextView = findViewById(R.id.item_text)
        val productDescription: TextView = findViewById(R.id.item_description)
        val productStock: TextView = findViewById(R.id.stock_text)
        val productBrand: TextView = findViewById(R.id.brand_text)
        val backBtn: ImageButton = findViewById(R.id.backBtn)
        val addToCartBtn: LinearLayout = findViewById(R.id.addToCartBtn)
        val cartBtn: ImageButton = findViewById(R.id.cartBtn)
        val priceTextView: TextView = findViewById(R.id.priceTextView)
        ratingText = findViewById(R.id.rating_text)
        val goToCartButton: Button = findViewById(R.id.goToCartButton)
        val helpBtn: ImageButton = findViewById(R.id.helpBtn)
        val fromRecommended = intent.getBooleanExtra("from_recommended", false)
        val cardView = findViewById<CardView>(R.id.cardView)
        val fromCart = intent.getBooleanExtra("from_cart", false)
        backButton = findViewById(R.id.backButton)
        imageCountTextView= findViewById(R.id.image_count)
        imageCarousel = findViewById(R.id.image_carousel)
        overlayBackground = findViewById(R.id.overlayBackground)
        addToCartOverlay = findViewById(R.id.addToCartOverlay)
        recommendedProductsRecycler = findViewById(R.id.recommendedProductsRecycler)
        recommendedProductsRecycler.layoutManager = GridLayoutManager(this, 2)
        cartBadge = findViewById(R.id.cartBadge)
        userId = intent.getIntExtra("user_id", 0)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        bottomSheetBehavior = BottomSheetBehavior.from(cardView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Get the product ID from intent
        productId = intent.getIntExtra("product_id", 0)
        token = intent.getStringExtra("jwt_token")

        setupBottomSheetBehavior()

        cartViewModel.cartItemCount.observe(this) { count ->
            updateCartBadge(count)
        }

        cartViewModel.fetchCartItems(userId, this)

        minusBtn = findViewById(R.id.minusBtn)
        plusBtn = findViewById(R.id.plusBtn)
        quantityText = findViewById(R.id.quantityText)

        if (fromCart) {
            // Product details should come from the cart item, ensure they are set properly
            productId = intent.getIntExtra("product_id", 0)
            selectedVariantId = intent.getIntExtra("variant_id", 0)
        } else {
            // Coming from HomeFragment, use the home product details
            productId = intent.getIntExtra("product_id", 0)
            selectedVariantId = intent.getIntExtra("variant_id", 0)
        }

        backBtn.setOnClickListener {
            if (fromRecommended) {
                finish()
            } else {
                onBackPressed()
            }
        }

        cartBtn.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }


        helpBtn.setOnClickListener {
            onPause()
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        // Fetch product details using the API instead of intent
        fetchProductDetails(productId)

        // Initialize the buy now button click handler
        initializeBuyNowButton()

        // Placeholder text while loading
        productName.text = "Loading..."
        productDescription.text = "Loading..."
        productStock.text = "Stock: ..."
        productBrand.text = "Brand: ..."
        priceTextView.text = "₱ ..."
        ratingText.text = "⭐ Loading ratings..."

        // Initialize quantity buttons
        var hasShownMaxStockToast = false

        plusBtn.setOnClickListener {
            val currentStock = productStock.text.toString().replace("Stock: ", "").toIntOrNull() ?: 0

            if (quantity < currentStock) {
                quantity++
                quantityText.text = quantity.toString()
                hasShownMaxStockToast = false
            } else {
                if (!hasShownMaxStockToast) {
                    Toast.makeText(this, "Max stock reached ($currentStock available)", Toast.LENGTH_SHORT).show()
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
                addToCart(userId, selectedVariantId, quantity, it)
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

        Log.d("ProductScreenActivity", "User ID: $userId")
        Log.d("ProductScreenActivity", "Token: $token")
        Log.d("ProductScreenActivity", "Product ID: $productId")
    }

    private fun fetchProductDetails(productId: Int) {
        if (productId <= 0) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show loading state
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        apiService.getProductById(productId).enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val products = response.body()?.products
                    if (products != null && products.isNotEmpty()) {
                        val product = products[0]
                        displayProductDetails(product)
                    } else {
                        Toast.makeText(this@ProductScreenActivity, "Product not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@ProductScreenActivity, "Failed to load product details", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                Toast.makeText(this@ProductScreenActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("ProductScreenActivity", "API call failed", t)
                finish()
            }
        })
    }

    private fun displayProductDetails(product: Product) {
        val productName: TextView = findViewById(R.id.item_text)
        val productDescription: TextView = findViewById(R.id.item_description)
        val productStock: TextView = findViewById(R.id.stock_text)
        val productBrand: TextView = findViewById(R.id.brand_text)
        val productVariation: TextView = findViewById(R.id.variationTV)
        val priceTextView: TextView = findViewById(R.id.priceTextView)
        val chooseVariationAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.chooseVariationAutoCompleteTextView)
        val chooseVariationTextInputLayout = findViewById<TextInputLayout>(R.id.chooseVariationTextInputLayout)
        val circularButton: ImageButton = findViewById(R.id.chatSellerForThisProduct)

        // Set basic product details
        productName.text = product.name
        productDescription.text = product.description
        productBrand.text = "Brand: ${product.brand}"

        // Handle variants
        val variants = product.variants
        val hasVariants = variants.isNotEmpty()

        if (hasVariants) {
            // Filter out variants with zero stock
            val availableVariants = variants.filter { it.stock > 0 }

            if (availableVariants.isEmpty()) {
                // No variants with stock available
                productVariation.visibility = View.VISIBLE
                productVariation.text = "No variants available"
                chooseVariationAutoCompleteTextView.visibility = View.GONE
                chooseVariationTextInputLayout.visibility = View.GONE
                return
            }

            // First get all variant names for the dropdown
            val variantNames = availableVariants.map { it.size ?: "Default" }

            // Create the mapping for variant details
            val variantMap = HashMap<String, Triple<Int, Double, Int>>()
            availableVariants.forEach { variant ->
                val variantName = variant.size ?: "Default"
                val price = variant.price.toDoubleOrNull() ?: 0.0
                variantMap[variantName] = Triple(variant.variant_id, price, variant.stock)
            }

            // Find the highest rated variant
            findHighestRatedVariant(availableVariants) { highestRatedVariant ->
                // This code runs after we've determined the highest rated variant

                // Set up dropdown adapter
                val adapter = ArrayAdapter(this, R.layout.custom_spinner_dropdown_item, variantNames)
                chooseVariationAutoCompleteTextView.setAdapter(adapter)
                chooseVariationAutoCompleteTextView.visibility = View.VISIBLE
                chooseVariationTextInputLayout.visibility = View.VISIBLE

                // Determine default variant
                val defaultVariant = highestRatedVariant ?: availableVariants.first()
                val defaultVariantName = defaultVariant.size ?: "Default"

                // Set default variant in dropdown
                chooseVariationAutoCompleteTextView.setText(defaultVariantName, false)

                // Update selectedVariantId and UI with default variant details
                selectedVariantId = defaultVariant.variant_id
                val defaultPrice = defaultVariant.price.toDoubleOrNull() ?: 0.0
                val defaultStock = defaultVariant.stock

                // Update UI
                priceTextView.text = "₱${formatPrice(defaultPrice)}"
                productStock.text = "Stock: $defaultStock"

                // Fetch and display ratings for the selected variant
                fetchAndDisplayRatings(selectedVariantId)

                Log.d("ProductScreenActivity", "Default Variant: $defaultVariantName, ID: $selectedVariantId, Price: $defaultPrice, Stock: $defaultStock")

                // Handle dropdown selection when user changes it
                chooseVariationAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                    val selectedVariantName = variantNames[position]
                    val variantDetails = variantMap[selectedVariantName]

                    if (variantDetails != null) {
                        selectedVariantId = variantDetails.first // Get variant_id
                        val newPrice = variantDetails.second // Get price
                        val newStock = variantDetails.third // Get stock
                        quantity = 1
                        quantityText.text = quantity.toString()

                        // Update UI
                        priceTextView.text = "₱${formatPrice(newPrice)}"
                        productStock.text = "Stock: $newStock"

                        // Update ratings for the newly selected variant
                        fetchAndDisplayRatings(selectedVariantId)

                        Log.d("ProductScreenActivity", "Selected Variant: $selectedVariantName, ID: $selectedVariantId, Price: $newPrice, Stock: $newStock")
                    }
                }
            }
        } else {
            // Product has no variants, use the main product details
            productVariation.visibility = View.VISIBLE
            chooseVariationAutoCompleteTextView.visibility = View.GONE
            chooseVariationTextInputLayout.visibility = View.GONE

            // If there are no variants, we should still set a default variant ID
            selectedVariantId = if (variants.isNotEmpty()) variants[0].variant_id else 0

            // Set price and stock from the first/default variant or from product itself
            val price = if (variants.isNotEmpty()) variants[0].price.toDoubleOrNull() ?: 0.0 else 0.0
            val stock = if (variants.isNotEmpty()) variants[0].stock else 0

            priceTextView.text = "₱${formatPrice(price)}"
            productStock.text = "Stock: $stock"

            // Fetch ratings for the default variant
            fetchAndDisplayRatings(selectedVariantId)
        }

        // Set up image carousel
        val imageUrls = listOf(product.image_url, product.image_url, product.image_url)
        val adapter = ImageCarouselAdapter(imageUrls)
        imageCarousel.adapter = adapter

        imageCountTextView.text = "1/${imageUrls.size}"

        imageCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                imageCountTextView.text = "${position + 1}/${imageUrls.size}"
            }
        })

        // Set up chat seller button
        circularButton.setOnClickListener {
            val productNameToSend = product.name
            val productIdToSend = selectedVariantId

            Log.d("ProductScreen", "Sending product name to MessagesFragment: $productNameToSend")

            val intent = Intent(this@ProductScreenActivity, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "MESSAGE")
            intent.putExtra("FROM_PRODUCT_SCREEN", true)
            intent.putExtra("PRODUCT_NAME", productNameToSend)
            intent.putExtra("PRODUCT_ID", productIdToSend)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun findHighestRatedVariant(variants: List<Variant>, callback: (Variant?) -> Unit) {
        if (variants.isEmpty()) {
            callback(null)
            return
        }

        val variantRatings = mutableMapOf<Int, Float>()
        val countDownLatch = AtomicInteger(variants.size)

        for (variant in variants) {
            apiService.getAverageRating(variant.variant_id).enqueue(object : Callback<AverageRatingResponse> {
                override fun onResponse(call: Call<AverageRatingResponse>, response: Response<AverageRatingResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val avgRating = response.body()?.average_rating ?: 0f
                        if (avgRating > 0) {
                            synchronized(variantRatings) {
                                variantRatings[variant.variant_id] = avgRating
                            }
                        }
                    }

                    if (countDownLatch.decrementAndGet() == 0) {
                        // All API calls complete
                        processHighestRatedVariant(variants, variantRatings, callback)
                    }
                }

                override fun onFailure(call: Call<AverageRatingResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failed to fetch rating for variant ${variant.variant_id}: ${t.message}")

                    if (countDownLatch.decrementAndGet() == 0) {
                        // All API calls complete (including failures)
                        processHighestRatedVariant(variants, variantRatings, callback)
                    }
                }
            })
        }
    }

    private fun processHighestRatedVariant(
        variants: List<Variant>,
        variantRatings: Map<Int, Float>,
        callback: (Variant?) -> Unit
    ) {
        if (variantRatings.isEmpty()) {
            // No ratings found, use first variant
            callback(variants.firstOrNull())
            return
        }

        // Find the variant ID with highest rating
        val highestRatedVariantId = variantRatings.maxByOrNull { it.value }?.key

        // Find the corresponding variant object
        val highestRatedVariant = if (highestRatedVariantId != null) {
            variants.find { it.variant_id == highestRatedVariantId } ?: variants.firstOrNull()
        } else {
            variants.firstOrNull()
        }

        // Return the highest rated variant through callback
        callback(highestRatedVariant)
    }

    private fun initializeBuyNowButton() {
        val buyNowBtn: Button = findViewById(R.id.buyNowBtn)

        buyNowBtn.setOnClickListener {
            token?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // First, check if the product already exists in the cart
                        val cartResponse = apiService.getCartItemsSuspend(userId)

                        if (cartResponse.isSuccessful) {
                            val cartItems = cartResponse.body()?.cart ?: emptyList()
                            val existingCartItem = cartItems.find { it.variant_id == selectedVariantId }

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
                        val cartRequest = CartRequest(userId, selectedVariantId, quantity)
                        val response = apiService.addToCart(cartRequest)

                        if (response.isSuccessful && response.body()?.success == true) {
                            val cartItemResponse = apiService.getCartItemsSuspend(userId)

                            if (cartItemResponse.isSuccessful) {
                                val cartItems = cartItemResponse.body()?.cart ?: emptyList()
                                val cartItem = cartItems.find { it.variant_id == selectedVariantId }

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
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.fetchCartItems(userId, this) // Refresh cart badge when returning
    }

    private fun setupBottomSheetBehavior() {
        val cardView = findViewById<CardView>(R.id.cardView)
        bottomSheetBehavior = BottomSheetBehavior.from(cardView)

        // Initially set to hidden/collapsed
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Wait for layout to be ready
        imageCarousel.post {
            // Get the position of the carousel
            val carouselPosition = IntArray(2)
            imageCarousel.getLocationOnScreen(carouselPosition)

            // Calculate the bottom position of the carousel plus some padding (e.g., 16dp)
            val carouselBottom = carouselPosition[1] + imageCarousel.height
            val padding = resources.getDimensionPixelSize(R.dimen.card_overlap) // Or any value you want

            // Set peek height to show the card just below the carousel
            val screenHeight = resources.displayMetrics.heightPixels
            val peekHeight = screenHeight - carouselBottom - padding

            // Set the calculated height
            bottomSheetBehavior.peekHeight = peekHeight
        }

        // Handle BottomSheet state changes
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // No state change handling needed
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val offset = slideOffset.coerceIn(0f, 1f)
                imageCarousel.alpha = 1 - offset
            }
        })
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

        val cartRequest = CartRequest(userId, selectedVariantId, quantity)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch the current cart items first
                val cartResponse = apiService.getCartItemsSuspend(userId)

                // Get current stock from product stock text view
                val currentStockText = findViewById<TextView>(R.id.stock_text).text.toString()
                val productStock = currentStockText.replace("Stock: ", "").toIntOrNull() ?: 0

                if (cartResponse.isSuccessful) {
                    val cartItems = cartResponse.body()?.cart ?: emptyList()
                    val existingCartItem = cartItems.find { it.variant_id == selectedVariantId }
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

    private fun fetchAndDisplayRatings(variantId: Int) {
        if (variantId <= 0) {
            ratingText.text = "⭐ No ratings yet"
            return
        }

        // Call the API to get ratings for the specific variant
        apiService.getRatingByVariantId(variantId).enqueue(object : Callback<RatingResponse> {
            override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val ratingResponse = response.body()
                    if (ratingResponse != null && ratingResponse.ratings.isNotEmpty()) {
                        // Calculate average rating if there are multiple ratings
                        val averageRating = ratingResponse.ratings.map { it.rating }.average().toFloat()

                        // Update UI with the fetched rating
                        runOnUiThread {
                            if (averageRating == 0f) {
                                ratingText.text = "⭐ No ratings yet"
                            } else {
                                ratingText.text = "⭐ ${String.format("%.1f", averageRating)}"
                            }
                        }

                        // Log the rating for debugging
                        Log.d("ProductScreenActivity", "Ratings fetched: ${ratingResponse.ratings.size} ratings, average: $averageRating for variant $variantId")
                    } else {
                        ratingText.text = "⭐ No ratings yet"
                    }
                } else {
                    // Handle error
                    ratingText.text = "⭐ No ratings yet"
                    Log.e("ProductScreenActivity", "Error fetching ratings: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                // Handle failure
                ratingText.text = "⭐ No ratings yet"
                Log.e("ProductScreenActivity", "Failed to fetch ratings: ${t.message}")
            }
        })
    }
}