package com.example.j_mabmobile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
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
    private lateinit var emptyMessage: View
    private lateinit var dimView: View
    private lateinit var searchView: SearchView

    private var allProducts: List<Product> = listOf()
    private var filteredProducts: List<Product> = listOf()
    private var currentCategory: String = "All"
    private var userId: Int = -1
    private var currentBrand: String = ""
    private var productNames: List<String> = listOf()
    private lateinit var listPopupWindow: ListPopupWindow
    private var suggestionList: List<String> = listOf()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val spinner: Spinner = view.findViewById(R.id.spinner_options)
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", MODE_PRIVATE)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_filter,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                when (selectedItem) {
                    "Recently Added" -> filterProducts(currentCategory, currentBrand)
                    "Price: High to Low" -> sortProductsByPrice(descending = true)
                    "Price: Low to High" -> sortProductsByPrice(descending = false)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

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
        oilsButton = view.findViewById(R.id.oilsButton)
        batteryButton = view.findViewById(R.id.batteryButton)
        lubricantButton = view.findViewById(R.id.lubricantButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyMessage = view.findViewById(R.id.emptyMessage)
        searchView = view.findViewById(R.id.searchView)
        dimView = view.findViewById(R.id.dimView)

        searchView.setIconified(true)
        searchView.clearFocus()
        searchView.isFocusable = false

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

            Toast.makeText(requireContext(), "Opening cart...", Toast.LENGTH_SHORT).show()

            cartIcon.postDelayed({
                progressBar.visibility = View.GONE
                dimView.visibility = View.GONE
                val intent = Intent(activity, CartActivity::class.java)
                startActivity(intent)
            }, 300)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)


        fetchProducts()

        return view
    }

    private fun fetchProducts() {
        progressBar.visibility = View.VISIBLE
        emptyMessage.visibility = View.GONE

        val apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService::class.java)
        apiService.getProducts().enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val productResponse = response.body()!!
                    if (productResponse.success) {
                        allProducts = productResponse.products.reversed()
                        productNames = allProducts.map { it.name } // Store product names
                        setupSearchView() // Initialize suggestions
                        filterProducts("All", "")
                    } else {
                        Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Unexpected response format", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showErrorMessage("Error fetching products. Please try again.")
                Log.e("HomeFragment", "Error fetching products", t)
            }
        })
    }

    private fun setupSearchView() {
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
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suggestionList)

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
        progressBar.visibility = View.VISIBLE
        emptyMessage.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService::class.java)
        apiService.getProducts().enqueue(object : Callback<ProductResponse> { // Fetch all products first
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
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

                    updateRecyclerView()
                } else {
                    showErrorMessage("No products found for \"$query\" in $category category.")
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showErrorMessage("Error: ${t.message}")
            }
        })
    }


    private fun sortProductsByPrice(descending: Boolean) {
        filteredProducts = allProducts.filter {
            currentCategory == "All" || it.category.equals(currentCategory, ignoreCase = true)
        }.sortedWith(compareBy<Product> { it.price }.let { if (descending) it.reversed() else it })

        updateRecyclerView()
    }



    private fun updateRecyclerView() {
        if (filteredProducts.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyMessage.visibility = View.VISIBLE
            emptyMessage.findViewById<TextView>(R.id.emptyMessage).text = "No products found."
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyMessage.visibility = View.GONE
            recyclerView.adapter = RecyclerAdapter(filteredProducts, userId)
        }
    }

    private fun showErrorMessage(message: String) {
        recyclerView.visibility = View.GONE
        emptyMessage.visibility = View.VISIBLE
        emptyMessage.findViewById<TextView>(R.id.emptyMessage).text = message
    }

}
