package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Order


class ViewAllOrdersActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    private lateinit var recyclerViewAllOrders: RecyclerView
    private lateinit var ordersViewModel: OrdersViewModel
    private lateinit var ordersAdapter: ViewAllOrdersAdapter
    private lateinit var emptyIcon: ImageView
    private lateinit var noOrdersYetTV: TextView
    private lateinit var continueShoppingBtn: Button
    private lateinit var filterOrderTextview: AutoCompleteTextView

    // List of possible order statuses
    private val orderStatuses = listOf(
        "All Orders",
        "Pending",
        "Processing",
        "Out for Delivery",
        "Delivered",
        "Failed Delivery",
        "Cancelled"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_orders)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

        // Initialize views
        backBtn = findViewById(R.id.backButton)
        recyclerViewAllOrders = findViewById(R.id.recyclerViewAllOrders)
        emptyIcon = findViewById(R.id.emptyIcon)
        noOrdersYetTV = findViewById(R.id.noOrdersYetTV)
        continueShoppingBtn = findViewById(R.id.continueShoppingBtn)
        filterOrderTextview = findViewById(R.id.filterOrderTextview)

        // Set up RecyclerView
        recyclerViewAllOrders.layoutManager = LinearLayoutManager(this)
        ordersAdapter = ViewAllOrdersAdapter(mutableListOf())
        recyclerViewAllOrders.adapter = ordersAdapter

        // Set up ViewModel
        ordersViewModel = ViewModelProvider(this)[OrdersViewModel::class.java]

        // Get userId from SharedPreferences or intent
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        // Fetch orders data
        ordersViewModel.fetchOrders(userId, this)

        // Set up filter dropdown
        val adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_dropdown_item,
            orderStatuses
        )
        filterOrderTextview.setAdapter(adapter)
        filterOrderTextview.setText("All Orders", false)

        // Observe allOrders LiveData
        ordersViewModel.allOrders.observe(this) { orders ->
            // Update the adapter with filtered data based on selected status
            updateOrdersList(orders)
        }

        // Set up filter text click listener
        filterOrderTextview.setOnItemClickListener { _, _, position, _ ->
            val selectedStatus = orderStatuses[position]
            ordersViewModel.allOrders.value?.let { updateOrdersList(it, selectedStatus) }
        }

        continueShoppingBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }

        // Set up back button click listener
        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun updateOrdersList(orders: List<Order>, status: String = "All Orders") {
        // First filter by status, then reverse the order
        val filteredOrders = when (status) {
            "All Orders" -> orders.reversed()
            else -> orders.filter { it.status.equals(status, ignoreCase = true) }.reversed()
        }

        ordersAdapter.updateOrders(filteredOrders)

        if (filteredOrders.isEmpty()) {
            emptyIcon.visibility = View.VISIBLE
            noOrdersYetTV.visibility = View.VISIBLE
            continueShoppingBtn.visibility = View.VISIBLE
            noOrdersYetTV.text = "No $status Orders"
        } else {
            emptyIcon.visibility = View.GONE
            noOrdersYetTV.visibility = View.GONE
            continueShoppingBtn.visibility = View.GONE
        }
    }
}