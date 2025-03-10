package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyAddressesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var addAddressBtn: ImageButton
    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_addresses)

        // ✅ Initialize ViewModel
        addressViewModel = ViewModelProvider(this)[AddressViewModel::class.java]

        recyclerView = findViewById(R.id.recyclerViewAddress)
        backBtn = findViewById(R.id.backButton)
        addAddressBtn = findViewById(R.id.addAddressBtn)

        // ✅ Back button functionality
        backBtn.setOnClickListener { onBackPressed() }

        // ✅ Add new address button
        addAddressBtn.setOnClickListener {
            val intent = Intent(this, NewAddressActivity::class.java)
            startActivity(intent)
        }

        // ✅ Setup RecyclerView and Adapter
        addressAdapter = AddressAdapter(mutableListOf(),
            onEditClick = { address ->
                val intent = Intent(this, EditAddressActivity::class.java)
                intent.putExtra("addressId", address.id)
                startActivity(intent)
            },
            onAddressSelected = { selectedAddress ->
                // ✅ Automatically update the default address in API
                addressViewModel.updateDefaultAddress(selectedAddress)
                Toast.makeText(this, "Default address updated!", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = addressAdapter

        // ✅ Observe changes from ViewModel
        addressViewModel.addresses.observe(this) { updatedList ->
            addressAdapter.updateData(updatedList)
        }

        // ✅ Fetch the list of addresses when the screen opens
        addressViewModel.fetchAddresses()
    }

    override fun onResume() {
        super.onResume()
        // ✅ Always refresh the address list when returning from Edit/Add
        addressViewModel.fetchAddresses()
    }
}
