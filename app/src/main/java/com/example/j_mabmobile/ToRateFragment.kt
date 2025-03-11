package com.example.j_mabmobile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Order

class ToRateFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersList = mutableListOf<Order>()
    private lateinit var ordersViewModel: OrdersViewModel

    private lateinit var noOrdersTextView: TextView
    private lateinit var emptyIcon: ImageView
    private lateinit var continueShoppingBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_to_rate, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewToRate)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        noOrdersTextView = view.findViewById(R.id.noOrdersYetTV)
        emptyIcon = view.findViewById(R.id.empytIcon)
        continueShoppingBtn = view.findViewById(R.id.continueShoppingBtn)

        ordersAdapter = OrdersAdapter(ordersList)
        recyclerView.adapter = ordersAdapter

        continueShoppingBtn.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            activity?.finish()
        }

        ordersViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(OrdersViewModel::class.java)

        ordersViewModel.toRateOrders.observe(viewLifecycleOwner) { orders ->
            Log.d("DEBUG", "ToRateFragment - Orders Updated: ${orders.size}")

            ordersList.clear()
            ordersList.addAll(orders)
            ordersAdapter.updateOrders(orders)
            ordersAdapter.notifyDataSetChanged()

            if (ordersList.isEmpty()) {
                noOrdersTextView.visibility = View.VISIBLE
                emptyIcon.visibility = View.VISIBLE
                continueShoppingBtn.visibility = View.VISIBLE
            } else {
                noOrdersTextView.visibility = View.GONE
                emptyIcon.visibility = View.GONE
                continueShoppingBtn.visibility = View.GONE
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = requireActivity().getSharedPreferences("myAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            ordersViewModel.fetchOrders(userId, requireContext())
        } else {
            Toast.makeText(requireContext(), "User ID not found!", Toast.LENGTH_SHORT).show()
        }
    }
}