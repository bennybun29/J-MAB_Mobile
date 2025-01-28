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
        emptyMessage = view.findViewById(R.id.emptyMessage) // Reference the empty message view

        // Setup buttons
        val buttons = listOf(allBtn, tiresButton, oilsButton, batteryButton, lubricantButton)
        for (button in buttons) {
            button.setOnClickListener {
                buttons.forEach { it.isSelected = false }
                it.isSelected = true

                // Apply filter based on selected button
                when (button) {
                    allBtn -> filterProducts("All")
                    tiresButton -> filterProducts("Tires")
                    oilsButton -> filterProducts("Oils")
                    batteryButton -> filterProducts("Battery")
                    lubricantButton -> filterProducts("Lubricant")
                }
            }
        }
        allBtn.isSelected = true

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
                        allProducts = productResponse.products // Store all products
                        filterProducts("All") // Show all products by default
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
}
