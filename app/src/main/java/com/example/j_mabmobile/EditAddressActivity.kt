package com.example.j_mabmobile

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.AddressRequest
import com.example.j_mabmobile.model.UserAddresses
import com.example.j_mabmobile.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditAddressActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    private lateinit var cityAutoCompleteTextView: AutoCompleteTextView
    private lateinit var barangayAutoCompleteTextView: AutoCompleteTextView
    private lateinit var homeAddressEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var addressId: Int = -1
    private var isFormChanged = false
    private val pangasinanCities = PangasinanLocations.pangasinanCities

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_address)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.j_mab_blue)

        sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)

        backBtn = findViewById(R.id.backButton)
        cityAutoCompleteTextView = findViewById(R.id.cityAutoCompleteTextView)
        barangayAutoCompleteTextView = findViewById(R.id.barangayAutoCompleteTextView)
        homeAddressEditText = findViewById(R.id.homeAddressEditText)
        saveButton = findViewById(R.id.saveButton)

        backBtn.setOnClickListener {
            if (isFormChanged) {
                showConfirmationDialog()
            } else {
                finish()
            }
        }

        // Retrieve address object and ID from intent
        val address = intent.getParcelableExtra<UserAddresses>("address")
        addressId = intent.getIntExtra("id", -1)

        Log.d("EditAddressActivity", "Received Address ID: $addressId")

        // Populate fields with existing data
        address?.let {
            cityAutoCompleteTextView.setText(it.city, false)

            // Update barangay dropdown based on the selected city
            updateBarangayDropdown(it.city)

            barangayAutoCompleteTextView.setText(it.barangay, false)
            homeAddressEditText.setText(it.home_address)
        }

        setupCityDropdown()
        setupListeners()

        saveButton.isEnabled = false
        saveButton.setBackgroundColor(Color.LTGRAY)
    }

    private fun setupCityDropdown() {
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_dropdown_item, pangasinanCities)
        cityAutoCompleteTextView.setAdapter(adapter)

        cityAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = adapter.getItem(position) ?: return@setOnItemClickListener
            updateBarangayDropdown(selectedCity)
            isFormChanged = true
            checkIfAllFieldsFilled()
        }
    }


    private fun updateBarangayDropdown(selectedCity: String) {
        // Use the barangay map from PangasinanLocations
        val barangays = PangasinanLocations.barangayMap[selectedCity] ?: emptyList()
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_dropdown_item, barangays)
        barangayAutoCompleteTextView.setAdapter(adapter)
        barangayAutoCompleteTextView.setText("", false) // Clear previous selection
        barangayAutoCompleteTextView.isEnabled = true
    }

    private fun setupListeners() {
        cityAutoCompleteTextView.addTextChangedListener(fieldWatcher)
        barangayAutoCompleteTextView.addTextChangedListener(fieldWatcher)
        homeAddressEditText.addTextChangedListener(fieldWatcher)

        saveButton.setOnClickListener {
            updateAddress()
        }
    }

    private val fieldWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            isFormChanged = true
            checkIfAllFieldsFilled()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun checkIfAllFieldsFilled() {
        val isCityFilled = cityAutoCompleteTextView.text.toString().isNotEmpty()
        val isBarangayFilled = barangayAutoCompleteTextView.text.toString().isNotEmpty()
        val isHomeAddressFilled = homeAddressEditText.text.toString().isNotEmpty()

        if (isCityFilled && isBarangayFilled && isHomeAddressFilled) {
            saveButton.isEnabled = true
            saveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.j_mab_blue))
        } else {
            saveButton.isEnabled = false
            saveButton.setBackgroundColor(Color.LTGRAY)
        }
    }

    private fun showConfirmationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_address_alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        // Get dialog buttons
        val btnCancel = dialogView.findViewById<Button>(R.id.btnNo)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnYes)

        // Dismiss dialog when "No" is clicked
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        // Proceed to cancel order when "Yes" is clicked
        btnConfirm.setOnClickListener {
            finish()
        }
    }

    private fun updateAddress() {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1 || addressId == -1) {
            Toast.makeText(this, "User ID or Address ID not found. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedAddress = UserAddresses(
            id = addressId,
            home_address = homeAddressEditText.text.toString(),
            barangay = barangayAutoCompleteTextView.text.toString(),
            city = cityAutoCompleteTextView.text.toString(),
            is_default = intent.getBooleanExtra("is_default", false)
        )

        val apiService = RetrofitClient.getApiService(this)
        val call = apiService.updateAddress(userId, addressId, AddressRequest(listOf(updatedAddress)))

        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditAddressActivity, "Address updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditAddressActivity, "Failed to update address", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditAddressActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
