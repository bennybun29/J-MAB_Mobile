package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.Address

class MyAddressesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var addressList: MutableList<Address>
    private lateinit var addAddressBtn: Button
    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the CartActivity
        setContentView(R.layout.activity_my_addresses)

        recyclerView = findViewById(R.id.recyclerViewAddress)
        backBtn = findViewById(R.id.backButton)
        addAddressBtn = findViewById(R.id.addAddressBtn)

        backBtn.setOnClickListener { onBackPressed() }

        addAddressBtn.setOnClickListener {
            val intent = Intent(this, NewAddressActivity::class.java)
            startActivity(intent)
        }

        // Sample address list
        addressList = mutableListOf(
            Address("Home", "123 Main Street", "San Roque", "Quezon City"),
            Address("Work", "456 Office Lane", "Business District", "Makati City"),
            Address("Parents' House", "789 Family Street", "Downtown", "Manila")
        )

        // Initialize adapter and set it to RecyclerView
        addressAdapter = AddressAdapter(addressList) { address ->
            Toast.makeText(this, "Edit: ${address.title}", Toast.LENGTH_SHORT).show()
            // You can navigate to edit screen here
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = addressAdapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}