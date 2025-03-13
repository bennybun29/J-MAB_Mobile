package com.example.j_mabmobile

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.AddressRequest
import com.example.j_mabmobile.model.UserAddresses
import com.example.j_mabmobile.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout

class NewAddressActivity : AppCompatActivity() {

    private lateinit var cityTextInputLayout: TextInputLayout
    private lateinit var cityAutoCompleteTextView: AutoCompleteTextView
    private lateinit var barangayTextInputLayout: TextInputLayout
    private lateinit var barangayAutoCompleteTextView: AutoCompleteTextView
    private lateinit var homeAddressEditText: EditText
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var isFormChanged = false

    private val pangasinanCities = listOf(
        "Alaminos", "Binmaley", "Bugallon", "Calasiao", "Dagupan",
        "Lingayen", "Malasiqui", "Mangaldan", "San Carlos", "Urdaneta"
    )

    // Map of cities to their barangays
    private val barangayMap = mapOf(
        "Alaminos" to listOf("Alos", "Amandiego", "Amangbangan", "Balangobong", "Balayang"),
        "Binmaley" to listOf("Balogo", "Basing", "Buenlag", "Caloocan Norte", "Caloocan Sur"),
        "Bugallon" to listOf("Angarian", "Bacabac", "Bolaoen", "Cabayaoasan", "Gueset"),
        "Calasiao" to listOf("Ambonao", "Banaoang", "Bued", "Cabilocaan", "Dinalaoan"),
        "Dagupan" to listOf("Bacayao Norte", "Bacayao Sur", "Barangay I", "Barangay II", "Barangay III"),
        "Lingayen" to listOf("Aliwekwek", "Baay", "Balangobong", "Balococ", "Bantayan"),
        "Malasiqui" to listOf("Agdao", "Alacan", "Alitaya", "Amacalan", "Ampid"),
        "Mangaldan" to listOf("Alitaya", "Amansabina", "Anolid", "Banaoang", "Bantayan"),
        "San Carlos" to listOf("Abanon", "Agdao", "Anando", "Antipangol", "Aponit"),
        "Urdaneta" to listOf("Anonas", "Bactad East", "Bactad West", "Bayaoas", "Bolaoen")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_address)

        // Initialize SharedPreferences correctly
        sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)

        window.navigationBarColor = resources.getColor(R.color.j_mab_blue, theme)

        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        homeAddressEditText = findViewById(R.id.homeAddressEditText)
        barangayTextInputLayout = findViewById(R.id.barangayTextInputLayout)
        barangayAutoCompleteTextView = findViewById(R.id.barangayAutoCompleteTextView)
        cityTextInputLayout = findViewById(R.id.cityTextInputLayout)
        cityAutoCompleteTextView = findViewById(R.id.cityAutoCompleteTextView)

        setupCityDropdown()

        setupListeners()

        saveButton.isEnabled = false
        saveButton.setBackgroundColor(Color.LTGRAY)
    }

    private fun setupCityDropdown() {
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, pangasinanCities)
        cityAutoCompleteTextView.setAdapter(cityAdapter)
        barangayAutoCompleteTextView.isEnabled = false
    }


    private fun setupListeners() {
        // City selection listener
        cityAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedCity = pangasinanCities[position]

            // Get the barangays for the selected city
            val barangays = barangayMap[selectedCity] ?: listOf()

            // Update barangay dropdown
            val barangayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangays)
            barangayAutoCompleteTextView.setAdapter(barangayAdapter)

            // Clear and enable barangay field
            barangayAutoCompleteTextView.setText("")
            barangayAutoCompleteTextView.isEnabled = true

            isFormChanged = true
            checkIfAllFieldsFilled()
        }

        cityAutoCompleteTextView.addTextChangedListener(fieldWatcher)
        barangayAutoCompleteTextView.addTextChangedListener(fieldWatcher)
        homeAddressEditText.addTextChangedListener(fieldWatcher)

        // Back button click listener
        backButton.setOnClickListener {
            if (isFormChanged) {
                showConfirmationDialog()
            } else {
                finish()
            }
        }

        // Save button click listener
        saveButton.setOnClickListener {
            addNewAddress()
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
        AlertDialog.Builder(this)
            .setTitle("Unsaved Changes?")
            .setMessage("You haven’t saved your address yet. Do you want to leave without saving?")
            .setPositiveButton("Leave Without Saving") { _, _ -> finish() }
            .setNegativeButton("Stay and Save", null)
            .show()
    }


    private fun addNewAddress() {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User ID not found. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val newAddress = UserAddresses(
            id = 0, // API will generate an ID
            home_address = homeAddressEditText.text.toString(),
            barangay = barangayAutoCompleteTextView.text.toString(),
            city = cityAutoCompleteTextView.text.toString(),
            is_default = false
        )

        val apiService = RetrofitClient.getApiService(this)
        val call = apiService.newAddress(userId, AddressRequest(listOf(newAddress)))

        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@NewAddressActivity, "Address added successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Close activity after success
                } else {
                    Toast.makeText(this@NewAddressActivity, "Failed to add address", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@NewAddressActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        if (isFormChanged) {
            showConfirmationDialog()
        } else {
            super.onBackPressed()
        }
    }
}
