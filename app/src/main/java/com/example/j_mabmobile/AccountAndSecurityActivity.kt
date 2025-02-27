package com.example.j_mabmobile

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

        loadUserInfo()

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

    private fun loadUserInfo() {
        firstNameTV.text = getBoldText("First Name: ", sharedPreferences.getString("first_name", "N/A"))
        lastNameTV.text = getBoldText("Last Name: ", sharedPreferences.getString("last_name", "N/A"))
        userGenderTV.text = getBoldText("Gender: ", sharedPreferences.getString("gender", "N/A"))
        userBirthdayTV.text = getBoldText("Birthday: ", sharedPreferences.getString("birthday", "N/A"))
        userEmailTV.text = getBoldText("Email: ", sharedPreferences.getString("user_email", "N/A"))
        userPhoneNumberTV.text = getBoldText("Phone Number: ", sharedPreferences.getString("phone_number", "N/A"))
    }

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

    private fun saveUserInfo(firstName: String, lastName: String, email: String, phoneNumber: String) {
        val editor = sharedPreferences.edit()
        editor.putString("first_name", firstName)
        editor.putString("last_name", lastName)
        editor.putString("user_email", email)
        editor.putString("phone_number", phoneNumber)
        editor.apply()
    }


    fun updateUserInfoUI() {
        loadUserInfo()
    }
}
