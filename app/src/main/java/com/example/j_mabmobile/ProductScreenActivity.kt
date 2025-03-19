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
import com.example.j_mabmobile.model.ProductResponse
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        val ratingText: TextView = findViewById(R.id.rating_text)
        val chooseVariationAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.chooseVariationAutoCompleteTextView)
        val chooseVariationTextInputLayout = findViewById<TextInputLayout>(R.id.chooseVariationTextInputLayout)
        val goToCartButton: Button = findViewById(R.id.goToCartButton)
        val circularButton: ImageButton = findViewById(R.id.chatSellerForThisProduct)
        val helpBtn: ImageButton = findViewById(R.id.helpBtn)
        val fromRecommended = intent.getBooleanExtra("from_recommended", false)
        val cardView = findViewById<CardView>(R.id.cardView)
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

        setupBottomSheetBehavior()

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

        val productId = intent.getIntExtra("product_id", 0)
        val variant_id = intent.getIntExtra("variant_id", 0)
        selectedVariantId = variant_id
        val imageUrl = intent.getStringExtra("product_image_url")
        val name = intent.getStringExtra("product_name")
        val description = intent.getStringExtra("product_description")
        val stock = intent.getIntExtra("product_stock", 0)
        val brand = intent.getStringExtra("product_brand")
        val category = intent.getStringExtra("product_category")
        val price = intent.getDoubleExtra("product_price", 0.0)
        val token = intent.getStringExtra("jwt_token")
        val ratings = intent.getFloatExtra("product_rating", 0f)
        val hasVariants = intent.getBooleanExtra("has_variants", false)
        val productVariants = intent.getStringArrayListExtra("product_variants") ?: arrayListOf()
        val size = intent.getStringExtra("size")
        val variantMap = intent.getSerializableExtra("variant_map") as? HashMap<String, Triple<Int, Double, Int>> ?: hashMapOf()

        if (hasVariants && productVariants.isNotEmpty()) {
            // Filter out variants with zero stock
            val availableVariants = productVariants.filter { variantName ->
                val variantDetails = variantMap[variantName]
                variantDetails?.third ?: 0 > 0
            }

            if (availableVariants.isNotEmpty()) {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, availableVariants)
                chooseVariationAutoCompleteTextView.setAdapter(adapter)
                chooseVariationAutoCompleteTextView.visibility = View.VISIBLE
                chooseVariationTextInputLayout.visibility = View.VISIBLE

                // Set the first available variant as default
                val defaultVariantName = availableVariants[0]
                chooseVariationAutoCompleteTextView.setText(defaultVariantName, false)

                // Update selectedVariantId and UI with default variant details
                val defaultVariantDetails = variantMap[defaultVariantName]
                if (defaultVariantDetails != null) {
                    selectedVariantId = defaultVariantDetails.first // Get variant_id
                    val newPrice = defaultVariantDetails.second // Get price
                    val newStock = defaultVariantDetails.third // Get stock

                    // Update UI
                    priceTextView.text = "₱${String.format("%.2f", newPrice)}"
                    productStock.text = "Stock: $newStock"

                    Log.d("ProductScreenActivity", "Default Variant: $defaultVariantName, ID: $selectedVariantId, Price: $newPrice, Stock: $newStock")
                }

                // Handle dropdown selection when user changes it
                chooseVariationAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                    val selectedVariantName = availableVariants[position]
                    val variantDetails = variantMap[selectedVariantName]

                    if (variantDetails != null) {
                        selectedVariantId = variantDetails.first // Get variant_id
                        val newPrice = variantDetails.second // Get price
                        val newStock = variantDetails.third // Get stock
                        quantity = 1
                        quantityText.text = quantity.toString()

                        // Update UI
                        priceTextView.text = "₱${String.format("%.2f", newPrice)}"
                        productStock.text = "Stock: $newStock"

                        Log.d("ProductScreenActivity", "Selected Variant: $selectedVariantName, ID: $selectedVariantId, Price: $newPrice, Stock: $newStock")
                    }
                }
            } else {
                // No variants with stock available
                productVariation.visibility = View.VISIBLE
                productVariation.text = "No variants available"
                chooseVariationAutoCompleteTextView.visibility = View.GONE
                chooseVariationTextInputLayout.visibility = View.GONE
            }
        } else {
            productVariation.visibility = View.VISIBLE
            chooseVariationAutoCompleteTextView.visibility = View.GONE
            chooseVariationTextInputLayout.visibility = View.GONE
        }

        circularButton.setOnClickListener {
            val productNameToSend = name ?: "Unknown Product"  // Fallback if name is null
            val productIdToSend = variant_id ?: ""
            Log.d("ProductScreen", "Sending product name to MessagesFragment: $productNameToSend")

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "MESSAGE")
            intent.putExtra("FROM_PRODUCT_SCREEN", true)
            intent.putExtra("PRODUCT_NAME", productNameToSend)
            intent.putExtra("PRODUCT_ID", productIdToSend)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

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
                        val cartRequest = CartRequest(userId, selectedVariantId, quantity)  // Use selectedVariantId
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


        productName.text = name
        productDescription.text = description
        productStock.text = "Stock Available: $stock"
        productBrand.text = "Brand: $brand"
        priceTextView.text = "₱ ${formatPrice(price)}"
        ratingText.text = if (ratings == 0.0f) "⭐ No ratings yet" else "⭐ $ratings"

        var hasShownMaxStockToast = false

        plusBtn.setOnClickListener {
            val currentStock = productStock.text.toString().replace("Stock: ", "").toIntOrNull() ?: stock

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
                addToCart(userId, selectedVariantId, quantity, it)  // Use selectedVariantId instead of variant_id
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
        Log.d("ProductScreenActivity", "Product ID: $variant_id")
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.fetchCartItems(userId, this) // Refresh cart badge when returning
    }

    private fun setupBottomSheetBehavior() {
        val cardView = findViewById<CardView>(R.id.cardView)
        bottomSheetBehavior = BottomSheetBehavior.from(cardView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // Calculate peek height and max height dynamically
            imageCountTextView.post {
                val imageCountLocation = IntArray(2)
                imageCountTextView.getLocationOnScreen(imageCountLocation)
                val imageCountBottom = imageCountLocation[1] + imageCountTextView.height

                val screenHeight = resources.displayMetrics.heightPixels
                bottomSheetBehavior.peekHeight = screenHeight - imageCountBottom - systemInsets.bottom

                cardView.layoutParams.height = screenHeight - systemInsets.bottom
                cardView.requestLayout()

                val backLocation = IntArray(2)
                backButton.getLocationOnScreen(backLocation)
                val topOffset = backLocation[1] + backButton.height + dp(20)

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

        val cartRequest = CartRequest(userId, selectedVariantId, quantity)  // Use selectedVariantId

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch the current cart items first
                val cartResponse = apiService.getCartItemsSuspend(userId)
                val productStock = intent.getIntExtra("product_stock", 0)
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
}