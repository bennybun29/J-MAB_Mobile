package com.example.j_mabmobile

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.NotificationWebSocketManager
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.CartResponse
import com.example.j_mabmobile.model.Product
import com.example.j_mabmobile.model.ProductResponse
import com.example.j_mabmobile.viewmodels.CartViewModel
import com.google.android.material.textfield.TextInputLayout
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var allBtn: ImageButton
    private lateinit var tiresButton: ImageButton
    private lateinit var oilsButton: ImageButton
    private lateinit var batteryButton: ImageButton
    private lateinit var lubricantButton: ImageButton
    private lateinit var cartIcon: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var progressBar: LottieAnimationView
    private lateinit var emptyMessage: View
    private lateinit var dimView: View
    private lateinit var searchView: SearchView
    private lateinit var dotsIndicator: WormDotsIndicator
    private lateinit var viewPager: ViewPager2
    private lateinit var bannerAdapter: BannerAdapter
    private val cartViewModel: CartViewModel by viewModels()
    private var currentPage = 0
    // New filter components
    private lateinit var filterTextInputLayout: TextInputLayout
    private lateinit var filterAutoCompleteTextView: AutoCompleteTextView


    private var allProducts: List<Product> = listOf()
    private var filteredProducts: List<Product> = listOf()
    private var currentCategory: String = "All"
    private var userId: Int = -1
    private var currentBrand: String = ""
    private var productNames: List<String> = listOf()
    private lateinit var listPopupWindow: ListPopupWindow
    private var suggestionList: List<String> = listOf()
    private lateinit var cartBadge: TextView
    private lateinit var userNameHomeTV: TextView
    private val CART_REQUEST_CODE = 1001




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Reference to the new filter components
        filterTextInputLayout = view.findViewById(R.id.selectFilterTextInputLayout)
        filterAutoCompleteTextView = view.findViewById(R.id.filterAutoCompleteTextview)

        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", MODE_PRIVATE)

        val imageUrls = listOf(
            R.drawable.battery_banner,
            R.drawable.tire_banner,
            R.drawable.oil_lubricant_banner
        )

        bannerAdapter = BannerAdapter(imageUrls)
        viewPager = view.findViewById(R.id.carouselBanner)
        viewPager.adapter = bannerAdapter
        dotsIndicator = view.findViewById(R.id.dotsIndicator)
        dotsIndicator.attachTo(viewPager)
        nestedScrollView = view.findViewById(R.id.nestedScrollView)
        recyclerView = view.findViewById(R.id.recyclerView)

        lifecycleScope.launch {
            while (isActive) {
                delay(10000)
                if (isAdded) {
                    currentPage = (currentPage + 1) % bannerAdapter.itemCount
                    viewPager.setCurrentItem(currentPage, true)
                }
            }
        }

        // Set up filter dropdown
        val filterOptions = resources.getStringArray(R.array.spinner_filter)
        val filterAdapter = ArrayAdapter(
            requireContext(),
            R.layout.custom_spinner_dropdown_item,
            filterOptions
        )

        filterAutoCompleteTextView.setAdapter(filterAdapter)
        filterAutoCompleteTextView.setText(filterOptions[0], false) // Set default selection

        filterAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            when (selectedItem) {
                "Recently Added" -> filterProducts(currentCategory, currentBrand)
                "Price: High to Low" -> sortProductsByPrice(descending = true)
                "Price: Low to High" -> sortProductsByPrice(descending = false)
            }
        }

        userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            Log.d("UserID", "Retrieved User ID: $userId")
        } else {
            Log.d("UserID", "No User ID found")
        }

        cartIcon = view.findViewById(R.id.cart_icon)
        allBtn = view.findViewById(R.id.allBtn)
        tiresButton = view.findViewById(R.id.tiresButton)
        oilsButton = view.findViewById(R.id.oilsBtn)
        batteryButton = view.findViewById(R.id.batteryButton)
        lubricantButton = view.findViewById(R.id.lubricantButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyMessage = view.findViewById(R.id.emptyMessage)
        searchView = view.findViewById(R.id.imageView3)
        dimView = view.findViewById(R.id.dimView)
        cartBadge = view.findViewById(R.id.cart_badge)
        userNameHomeTV = view.findViewById(R.id.userNameHomeTV)

        searchView.setIconified(true)
        searchView.clearFocus()
        searchView.isFocusable = false

        val firstName = sharedPreferences.getString("first_name", "") ?: ""
        val lastName = sharedPreferences.getString("last_name", "") ?: ""

        userNameHomeTV.text = "$firstName $lastName"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchProducts(query, currentCategory, currentBrand)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    filterProducts(currentCategory, currentBrand)
                } else {
                    searchProducts(newText, currentCategory, currentBrand)
                }
                return true
            }
        })

        val buttons = listOf(allBtn, tiresButton, oilsButton, batteryButton, lubricantButton)
        for (button in buttons) {
            button.setOnClickListener {
                buttons.forEach { it.isSelected = false }
                it.isSelected = true
                currentCategory = when (button) {
                    allBtn -> "All"
                    tiresButton -> "Tires"
                    oilsButton -> "Oils"
                    batteryButton -> "Batteries"
                    lubricantButton -> "Lubricants"
                    else -> "All"
                }
                filterProducts(currentCategory, currentBrand)
            }
        }
        allBtn.isSelected = true

        cartIcon.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            dimView.visibility = View.VISIBLE

            cartIcon.postDelayed({
                progressBar.visibility = View.GONE
                dimView.visibility = View.GONE
                val intent = Intent(activity, CartActivity::class.java)
                startActivityForResult(intent, CART_REQUEST_CODE)
            }, 400)
        }

        userId = sharedPreferences.getInt("user_id", -1)

        cartViewModel.cartItemCount.observe(viewLifecycleOwner) { count ->
            updateCartBadge(count)
        }

        if (userId != -1) {
            Log.d("UserID", "Retrieved User ID: $userId")
            fetchCartItems() // Fetch cart items for the logged-in user
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)


        lifecycleScope.launch {
            delay(800)
            fetchProducts()
        }

        cartViewModel.fetchCartItems(userId, requireContext())


        return view
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.fetchCartItems(userId, requireContext())
        NotificationWebSocketManager.reconnectIfNeeded()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CART_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            cartViewModel.fetchCartItems(userId, requireContext()) // Refresh cart count
        }
    }


    private fun fetchProducts() {
        if (!isAdded || isDetached) return // Check if fragment is attached

        progressBar.visibility = View.VISIBLE
        emptyMessage.visibility = View.GONE  // Hide error message while loading
        recyclerView.visibility = View.GONE  // Hide recycler view while loading

        val apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService::class.java)
        apiService.getProducts().enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (!isAdded || isDetached) return // Check if fragment is attached

                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val productResponse = response.body()!!
                    if (productResponse.success && productResponse.products.isNotEmpty()) {
                        allProducts = productResponse.products.reversed()
                        productNames = allProducts.map { it.name }
                        setupSearchView()
                        filterProducts("All", "")  // This will call updateRecyclerView()
                    } else {
                        // Only now show the error message if there are genuinely no products
                        showErrorMessage("No products found.")
                    }
                } else {
                    showErrorMessage("Unexpected response format")
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                if (!isAdded || isDetached) return // Check if fragment is attached

                progressBar.visibility = View.GONE
                showErrorMessage("Error fetching products. Please try again.")
            }
        })
    }



    private fun setupSearchView() {
        if (!isAdded || isDetached) return // Check if fragment is attached

        listPopupWindow = ListPopupWindow(requireContext())

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                listPopupWindow.dismiss()
                if (!query.isNullOrBlank()) {
                    searchProducts(query, currentCategory, currentBrand)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    filteredProducts = allProducts
                    updateRecyclerView()
                    listPopupWindow.dismiss()
                    return true
                }

                suggestionList = productNames.filter { it.contains(newText, ignoreCase = true) }

                if (suggestionList.isNotEmpty()) {
                    showSuggestions()
                } else {
                    listPopupWindow.dismiss()
                }
                return true
            }
        })
    }


    private fun showSuggestions() {
        // Create a custom adapter with custom layout
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.custom_spinner_item,  // Create this custom layout
            suggestionList
        )

        listPopupWindow.setAdapter(adapter)
        listPopupWindow.anchorView = searchView
        listPopupWindow.width = searchView.width
        listPopupWindow.height = ListPopupWindow.WRAP_CONTENT
        listPopupWindow.isModal = false

        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = suggestionList[position]
            searchView.setQuery(selectedItem, false)
            listPopupWindow.dismiss()
            searchView.requestFocus()
        }

        listPopupWindow.show()
    }





    private fun filterProducts(category: String, brand: String) {
        filteredProducts = allProducts.filter {
            (category == "All" || it.category.equals(category, ignoreCase = true)) &&
                    (brand.isEmpty() || it.brand?.equals(brand, ignoreCase = true) == true)
        }

        updateRecyclerView()
    }

    private fun searchProducts(query: String, category: String, brand: String) {
        if (!isAdded || isDetached) return // Check if fragment is attached

        progressBar.visibility = View.VISIBLE
        emptyMessage.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService::class.java)
        apiService.getProducts().enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (!isAdded || isDetached) return // Check if fragment is attached

                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val allProducts = response.body()!!.products

                    // Now filter locally based on multiple fields
                    filteredProducts = allProducts.filter {
                        // Ensure product matches query AND belongs to the selected category
                        (category == "All" || it.category.equals(category, ignoreCase = true)) &&
                                (it.name.contains(query, ignoreCase = true) ||
                                        it.brand?.contains(query, ignoreCase = true) == true)
                    }

                    if (filteredProducts.isEmpty()) {
                        // Only show empty message if a search was performed and no results were found
                        showErrorMessage("No products found for \"$query\" in $category category.")
                    } else {
                        updateRecyclerView()
                    }
                } else {
                    showErrorMessage("No products found for \"$query\" in $category category.")
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                if (!isAdded || isDetached) return // Check if fragment is attached

                progressBar.visibility = View.GONE
                showErrorMessage("Error: ${t.message}")
            }
        })
    }



    private fun sortProductsByPrice(descending: Boolean) {
        filteredProducts = allProducts.filter {
            currentCategory == "All" || it.category.equals(currentCategory, ignoreCase = true)
        }.sortedWith(compareBy<Product> {
            it.variants.firstOrNull()?.price?.toDoubleOrNull() ?: 0.0
        }.let { if (descending) it.reversed() else it })

        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        progressBar.visibility = View.GONE

        if (filteredProducts.isEmpty() && allProducts.isNotEmpty()) {
            recyclerView.visibility = View.GONE
            emptyMessage.visibility = View.VISIBLE
        } else if (filteredProducts.isEmpty() && allProducts.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyMessage.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyMessage.visibility = View.GONE

            val availableProducts = filteredProducts.filter {
                it.variants.any { variant -> variant.stock > 0 }
            }
            recyclerView.adapter = RecyclerAdapter(availableProducts, userId)
        }
    }




    private fun showErrorMessage(message: String) {
        recyclerView.visibility = View.GONE
        emptyMessage.visibility = View.VISIBLE
        emptyMessage.findViewById<TextView>(R.id.emptyMessage).text = message
    }

    private fun updateCartBadge(count: Int) {
        if (count > 0) {
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = if (count > 10) "10+" else count.toString()
        } else {
            cartBadge.visibility = View.GONE
        }
    }

    private fun fetchCartItems() {
        if (userId == -1) return

        val apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService::class.java)

        apiService.getCartItemsCall(userId).enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val cartResponse = response.body()!!
                    if (cartResponse.success) {
                        val cartCount = cartResponse.cart.size
                        updateCartBadge(cartCount)
                    } else {
                        updateCartBadge(0)
                    }
                } else {
                    Log.e("Cart Fetch", "Failed to fetch cart items")
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                Log.e("Cart Fetch", "Error: ${t.message}")
            }
        })
    }
}