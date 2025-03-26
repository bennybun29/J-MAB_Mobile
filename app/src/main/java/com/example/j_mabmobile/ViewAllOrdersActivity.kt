package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ViewAllOrdersActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    private lateinit var recyclerViewAllOrders: RecyclerView
    private lateinit var ordersViewModel: OrdersViewModel
    private lateinit var ordersAdapter: ViewAllOrdersAdapter
    private lateinit var emptyIcon: ImageView
    private lateinit var noOrdersYetTV: TextView
    private lateinit var continueShoppingBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_orders)

        // Initialize views
        backBtn = findViewById(R.id.backButton)
        recyclerViewAllOrders = findViewById(R.id.recyclerViewAllOrders)
        emptyIcon = findViewById(R.id.emptyIcon)
        noOrdersYetTV = findViewById(R.id.noOrdersYetTV)
        continueShoppingBtn = findViewById(R.id.continueShoppingBtn)

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

        // Observe allOrders LiveData
        ordersViewModel.allOrders.observe(this) { orders ->
            // Update the adapter with new data
            ordersAdapter.updateOrders(orders)

            if (orders.isEmpty()) {
                emptyIcon.visibility = View.VISIBLE
                noOrdersYetTV.visibility = View.VISIBLE
                continueShoppingBtn.visibility = View.VISIBLE
            } else {
                emptyIcon.visibility = View.GONE
                noOrdersYetTV.visibility = View.GONE
                continueShoppingBtn.visibility = View.GONE
            }
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
}
