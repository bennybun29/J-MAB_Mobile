package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ResetPasswordRequest
import com.example.j_mabmobile.model.ResetPasswordResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var userPasswordTIL: TextInputLayout
    private lateinit var confirmUserPasswordTIL: TextInputLayout
    private lateinit var userPassword: TextInputEditText
    private lateinit var confirmPassword: TextInputEditText
    private lateinit var resetPasswordBtn: Button
    private lateinit var progressBar: LottieAnimationView
    private lateinit var overlayBackground: View
    private lateinit var backBtn: ImageButton
    private lateinit var apiService: ApiService

    private var email: String = ""
    private var resetCode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Initialize API service
        apiService = RetrofitClient.getApiService(this)

        // Get data from intent
        email = intent.getStringExtra("email") ?: ""
        resetCode = intent.getIntExtra("reset_code", 0)

        // Validate data from intent
        if (email.isEmpty() || resetCode == 0) {
            Toast.makeText(this, "Required information missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        userPasswordTIL = findViewById(R.id.userPasswordTIL)
        confirmUserPasswordTIL = findViewById(R.id.confirmUserPasswordTIL)
        userPassword = findViewById(R.id.userPassword)
        confirmPassword = findViewById(R.id.confirmPassword)
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn)
        progressBar = findViewById(R.id.progressBar)
        overlayBackground = findViewById(R.id.overlayBackground)
        backBtn = findViewById(R.id.backBtn)
    }

    private fun setupListeners() {
        // Back button listener
        backBtn.setOnClickListener {
            onBackPressed()
        }

        // Reset password button listener
        resetPasswordBtn.setOnClickListener {
            if (validatePasswords()) {
                resetPassword()
            }
        }

        // Clear errors when typing
        userPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userPasswordTIL.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirmUserPasswordTIL.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePasswords(): Boolean {
        val password = userPassword.text.toString().trim()
        val confirmPass = confirmPassword.text.toString().trim()

        // Validate password is not empty
        if (password.isEmpty()) {
            userPasswordTIL.error = "Password cannot be empty"
            return false
        }

        // Validate password length
        if (password.length < 8) {
            userPasswordTIL.error = "Password should be at least 8 characters"
            return false
        }

        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$"
        if (!password.matches(passwordRegex.toRegex())) {
            userPasswordTIL.error = "Password must contain at least one digit, one lowercase letter, and one uppercase letter"
            return false
        }

        // Validate passwords match
        if (password != confirmPass) {
            confirmUserPasswordTIL.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun resetPassword() {
        val newPassword = userPassword.text.toString().trim()

        // Show loading state
        showLoading(true)

        // Create request object
        val resetPasswordRequest = ResetPasswordRequest(email, resetCode, newPassword)

        // Call API to reset password
        apiService.resetPassword(resetPasswordRequest).enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                showLoading(false)

                if (response.isSuccessful && response.body()?.success == true) {
                    // Password reset was successful
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Password reset successful. Please log in with your new password.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigate to login screen
                    navigateToLogin()
                } else {
                    // Handle error
                    val errors = response.body()?.errors
                    val errorMessage = if (!errors.isNullOrEmpty()) {
                        errors.joinToString("\n")
                    } else {
                        "Failed to reset password. Please try again."
                    }

                    Toast.makeText(this@ResetPasswordActivity, errorMessage, Toast.LENGTH_LONG).show()

                    // Log the error
                    Log.e("RESET_PASSWORD", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                showLoading(false)

                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

                // Log the error
                Log.e("RESET_PASSWORD", "Network failure: ${t.message}", t)
            }
        })
    }

    private fun navigateToLogin() {
        // Navigate to login screen and clear the back stack
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            overlayBackground.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            resetPasswordBtn.isEnabled = false
        } else {
            overlayBackground.visibility = View.GONE
            progressBar.visibility = View.GONE
            resetPasswordBtn.isEnabled = true
        }
    }
}