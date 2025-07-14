package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.viewmodels.AddressViewModel

class MyAddressesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var addAddressBtn: ImageButton
    private lateinit var backBtn: ImageButton
    private lateinit var noAddressIcon: ImageView
    private lateinit var noNewAddressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_addresses)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

        // ✅ Initialize ViewModel
        addressViewModel = ViewModelProvider(this)[AddressViewModel::class.java]

        recyclerView = findViewById(R.id.recyclerViewAddress)
        backBtn = findViewById(R.id.backButton)
        addAddressBtn = findViewById(R.id.addAddressBtn)
        noAddressIcon = findViewById(R.id.noAddressIcon)
        noNewAddressText = findViewById(R.id.noNewAddressText)

        // ✅ Back button functionality
        backBtn.setOnClickListener { onBackPressed() }

        // ✅ Add new address button
        addAddressBtn.setOnClickListener {
            val intent = Intent(this, NewAddressActivity::class.java)
            startActivity(intent)
        }

// ✅ Setup RecyclerView and Adapter
        addressAdapter = AddressAdapter(
            mutableListOf(),
            onEditClick = { address ->
                val intent = Intent(this, EditAddressActivity::class.java)
                intent.putExtra("addressId", address.id)
                startActivity(intent)
            },
            onAddressSelected = { selectedAddress ->
                // ✅ Automatically update the default address in API
                addressViewModel.updateDefaultAddress(selectedAddress)
                Toast.makeText(this, "Default address updated!", Toast.LENGTH_SHORT).show()
            },
            addressViewModel = addressViewModel // Add this parameter
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = addressAdapter

        // ✅ Observe changes from ViewModel
        addressViewModel.addresses.observe(this) { updatedList ->
            addressAdapter.updateData(updatedList)
            toggleEmptyState(updatedList.isEmpty())
        }

        // ✅ Fetch the list of addresses when the screen opens
        addressViewModel.fetchAddresses()
    }

    override fun onResume() {
        super.onResume()
        // ✅ Always refresh the address list when returning from Edit/Add
        addressViewModel.fetchAddresses()
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            noAddressIcon.visibility = View.VISIBLE
            noNewAddressText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noAddressIcon.visibility = View.GONE
            noNewAddressText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
