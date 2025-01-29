package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.Product
import com.example.j_mabmobile.model.ProductResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var allBtn: Button
    private lateinit var tiresButton: Button
    private lateinit var oilsButton: Button
    private lateinit var batteryButton: Button
    private lateinit var lubricantButton: Button
    private lateinit var cartIcon: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyMessage: View // Placeholder for the empty message
    private var allProducts: List<Product> = listOf() // To store all products
    private var filteredProducts: List<Product> = listOf() // To store filtered products
    private lateinit var searchView: SearchView
    private var currentCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        cartIcon = view.findViewById(R.id.cart_icon)
        allBtn = view.findViewById(R.id.allBtn)
        tiresButton = view.findViewById(R.id.tiresButton)
        oilsButton = view.findViewById(R.id.oilsButton)
        batteryButton = view.findViewById(R.id.batteryButton)
        lubricantButton = view.findViewById(R.id.lubricantButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyMessage = view.findViewById(R.id.emptyMessage)
        searchView = view.findViewById(R.id.searchView)

        // Ensure the SearchView is collapsed initially but is clickable
        searchView.setIconified(true) // Collapse the SearchView initially
        searchView.clearFocus() // Remove any initial focus
        searchView.isFocusable = false

        // Set up the listener for the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotBlank()) {
                    searchProducts(query, currentCategory)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    filterProducts("All")
                }
                return true
            }
        })

        // When the SearchView gains focus, prevent iconification
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchView.isIconified = false // Prevent iconification when focused
            }
        }



// Setup buttons
        val buttons = listOf(allBtn, tiresButton, oilsButton, batteryButton, lubricantButton)
        for (button in buttons) {
            button.setOnClickListener {
                buttons.forEach { it.isSelected = false }
                it.isSelected = true

                // Apply filter based on selected button
                when (button) {
                    allBtn -> {
                        currentCategory = "All"
                        filterProducts("All")
                    }
                    tiresButton -> {
                        currentCategory = "Tires"
                        filterProducts("Tires")
                    }
                    oilsButton -> {
                        currentCategory = "Oils"
                        filterProducts("Oils")
                    }
                    batteryButton -> {
                        currentCategory = "Battery"
                        filterProducts("Battery")
                    }
                    lubricantButton -> {
                        currentCategory = "Lubricant"
                        filterProducts("Lubricant")
                    }
                }
            }
        }
        allBtn.isSelected = true // Set default category to "All"

// In the search query listener, modify to filter according to the active category:
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotBlank()) {
                    searchProducts(query, currentCategory) // Pass the active category
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    searchProducts(newText, currentCategory) // Pass the active category
                } else {
                    // If no search text, show products based on the current category
                    filterProducts(currentCategory)
                }
                return true
            }
        })


        // Set cart button click listener
        cartIcon.setOnClickListener {
            val intent = Intent(activity, CartActivity::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Fetch products
        fetchProducts()

        return view
    }



    private fun fetchProducts() {
        progressBar.visibility = View.VISIBLE
        emptyMessage.visibility = View.GONE // Hide the empty message initially

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.getProducts().enqueue(object : Callback<ProductResponse> {
            override fun onResponse(
                call: Call<ProductResponse>,
                response: Response<ProductResponse>
            ) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val productResponse = response.body()!!
                    if (productResponse.success) {
                        allProducts = productResponse.products
                        filterProducts("All")
                    } else {
                        Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Unexpected response format", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Error fetching products", t)
            }
        })
    }

    private fun filterProducts(category: String) {
        filteredProducts = if (category == "All") {
            allProducts // Show all products
        } else {
            // Filter products based on the category
            allProducts.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Check if there are products to display
        if (filteredProducts.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyMessage.visibility = View.VISIBLE // Show the empty message
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyMessage.visibility = View.GONE // Hide the empty message
            recyclerView.adapter = RecyclerAdapter(filteredProducts) // Update RecyclerView
        }
    }

    private fun searchProducts(query: String, category: String) {
        progressBar.visibility = View.VISIBLE
        emptyMessage.visibility = View.GONE

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.searchProducts(name = query).enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val productResponse = response.body()!!
                    // Filter products based on both search query and selected category
                    filteredProducts = if (category == "All") {
                        productResponse.products.filter { it.name.contains(query, ignoreCase = true) }
                    } else {
                        productResponse.products.filter {
                            it.category.equals(category, ignoreCase = true) && it.name.contains(query, ignoreCase = true)
                        }
                    }

                    if (filteredProducts.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyMessage.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyMessage.visibility = View.GONE
                        recyclerView.adapter = RecyclerAdapter(filteredProducts)
                    }
                } else {
                    Toast.makeText(requireContext(), "No products found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




}
