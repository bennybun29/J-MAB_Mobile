package com.example.j_mabmobile

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.User
import com.example.j_mabmobile.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountAndSecurityActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var editPersonalInfoButton: ImageButton
    lateinit var editSecurityButton: ImageButton
    lateinit var editContactInfoButton: ImageButton
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var firstNameTV: TextView
    private lateinit var lastNameTV: TextView
    private lateinit var userGenderTV: TextView
    private lateinit var userBirthdayTV: TextView
    private lateinit var userEmailTV: TextView
    private lateinit var userPhoneNumberTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_and_security)

        /*
        window.statusBarColor = resources.getColor(R.color.j_mab_blue, theme)
        window.navigationBarColor = resources.getColor(R.color.j_mab_blue, theme)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
         */

        editPersonalInfoButton = findViewById(R.id.editPersonalInfoButton)
        editSecurityButton = findViewById(R.id.editSecurityButton)
        editContactInfoButton = findViewById(R.id.editContactInfoButton)

        firstNameTV = findViewById(R.id.firstNameTV)
        lastNameTV = findViewById(R.id.lastNameTV)
        userGenderTV = findViewById(R.id.userGenderTV)
        userBirthdayTV = findViewById(R.id.userBirthdayTV)
        userEmailTV = findViewById(R.id.userEmailTV)
        userPhoneNumberTV = findViewById(R.id.userPhoneNumberTV)

        backBtn = findViewById(R.id.backButton)

        sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)

        // Load user info from SharedPreferences first
        loadUserInfo()

        // Fetch user profile from API
        fetchUserProfile()

        // Handle click events
        backBtn.setOnClickListener {
            onBackPressed()
        }

        editPersonalInfoButton.setOnClickListener {
            val dialog = EditPersonalInfoDialog()
            dialog.show(supportFragmentManager, "EditPersonalInfoDialog")
        }

        editSecurityButton.setOnClickListener {
            val dialog = EditSecurityDialog()
            dialog.show(supportFragmentManager, "EditSecurityDialog")
        }

        editContactInfoButton.setOnClickListener {
            val dialog = EditContactInfoDialog()
            dialog.show(supportFragmentManager, "EditContactInfoDialog")
        }
    }

    /**
     * Fetch user profile from API
     */
    private fun fetchUserProfile() {
        val userId = sharedPreferences.getInt("user_id", -1) // Get user ID from shared preferences
        if (userId == -1) return

        val apiService = RetrofitClient.getApiService(this)
        apiService.getUserProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val userProfile = response.body()!!.user
                    if (userProfile != null) {
                        // Update UI with user's gender and birthday
                        userGenderTV.text = getBoldText("Gender: ", userProfile.gender)
                        userBirthdayTV.text = getBoldText("Birthday: ", userProfile.birthday)
                    }
                } else {
                    Log.e("API_ERROR", "Failed to fetch user profile: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("API_ERROR", "Network error: ${t.message}")
            }
        })
    }

    /**
     * Load user information from SharedPreferences
     */
    private fun loadUserInfo() {
        firstNameTV.text = getBoldText("First Name: ", sharedPreferences.getString("first_name", "N/A"))
        lastNameTV.text = getBoldText("Last Name: ", sharedPreferences.getString("last_name", "N/A"))
        userGenderTV.text = getBoldText("Gender: ", sharedPreferences.getString("gender", "N/A"))
        userBirthdayTV.text = getBoldText("Birthday: ", sharedPreferences.getString("birthday", "N/A"))
        userEmailTV.text = getBoldText("Email: ", sharedPreferences.getString("user_email", "N/A"))
        userPhoneNumberTV.text = getBoldText("Phone Number: ", sharedPreferences.getString("phone_number", "N/A"))
    }

    /**
     * Save user information to SharedPreferences
     */
    private fun saveUserInfo(
        firstName: String, lastName: String, email: String,
        phoneNumber: String, gender: String, birthday: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("first_name", firstName)
        editor.putString("last_name", lastName)
        editor.putString("user_email", email)
        editor.putString("phone_number", phoneNumber)
        editor.putString("gender", gender)
        editor.putString("birthday", birthday)
        editor.apply()
    }

    /**
     * Apply bold text to labels
     */
    private fun getBoldText(label: String, value: String?): SpannableString {
        val text = "$label$value"
        val spannable = SpannableString(text)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            label.length,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    /**
     * Called to refresh user data after updating
     */
    fun updateUserInfoUI() {
        loadUserInfo()
    }
}
