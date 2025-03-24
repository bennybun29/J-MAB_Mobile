package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ForgotPasswordEmailResponse
import com.example.j_mabmobile.model.VerifyCodeRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordCodeActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    private lateinit var digitBox1: EditText
    private lateinit var digitBox2: EditText
    private lateinit var digitBox3: EditText
    private lateinit var digitBox4: EditText
    private lateinit var digitBox5: EditText
    private lateinit var digitBox6: EditText
    private lateinit var verifyButton: Button
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var overlayBackground: View
    private lateinit var tvResendCode: TextView
    private lateinit var apiService: ApiService

    // Get email from intent
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_code)

        // Initialize API service
        apiService = RetrofitClient.getApiService(this)

        // Get email from previous activity
        email = intent.getStringExtra("email") ?: ""
        if (email.isEmpty()) {
            Toast.makeText(this, "Email not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        backBtn = findViewById(R.id.backBtn)
        digitBox1 = findViewById(R.id.digitBox1)
        digitBox2 = findViewById(R.id.digitBox2)
        digitBox3 = findViewById(R.id.digitBox3)
        digitBox4 = findViewById(R.id.digitBox4)
        digitBox5 = findViewById(R.id.digitBox5)
        digitBox6 = findViewById(R.id.digitBox6)
        verifyButton = findViewById(R.id.signUpBtn)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        overlayBackground = findViewById(R.id.overlayBackground)
        tvResendCode = findViewById(R.id.tvResendCode)

        // Set up back button
        backBtn.setOnClickListener {
            onBackPressed()
        }

        // Set up verify button
        verifyButton.setOnClickListener {
            validateAndSubmitCode()
        }

        // Set up resend code
        tvResendCode.setOnClickListener {
            // Implementation for resending code would go here
            Toast.makeText(this, "Resending code to $email", Toast.LENGTH_SHORT).show()
            // You would typically call your API to resend the verification code here
        }

        // Set up focus change and auto-advance between digit boxes
        setupVerificationCodeBoxes()
    }

    private fun setupVerificationCodeBoxes() {
        // List of all digit boxes for easier iteration
        val digitBoxes = listOf(digitBox1, digitBox2, digitBox3, digitBox4, digitBox5, digitBox6)

        // Set focus to first box when activity starts
        digitBox1.requestFocus()

        // Set up TextWatchers for each box
        for (i in digitBoxes.indices) {
            val currentBox = digitBoxes[i]

            currentBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Not needed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // If text is entered (count > 0) and there's a next box, move focus to it
                    if (count > 0 && i < digitBoxes.size - 1) {
                        digitBoxes[i + 1].requestFocus()
                    } else if (count > 0 && i == digitBoxes.size - 1) {
                        // Auto-submit when all digits are entered
                        hideKeyboard()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Check if all boxes are filled to enable the verify button
                    val allFilled = digitBoxes.all { it.text.isNotEmpty() }
                    verifyButton.isEnabled = allFilled
                }
            })

            // Handle backspace key for empty boxes
            currentBox.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    currentBox.text.isEmpty() &&
                    i > 0) {
                    // If backspace is pressed on an empty box, move to previous box and clear it
                    digitBoxes[i - 1].apply {
                        requestFocus()
                        text.clear()
                    }
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
        }
    }

    private fun validateAndSubmitCode() {
        val digitBoxes = listOf(digitBox1, digitBox2, digitBox3, digitBox4, digitBox5, digitBox6)

        // Check if all boxes are filled
        if (digitBoxes.any { it.text.isEmpty() }) {
            Toast.makeText(this, "Please enter the complete verification code", Toast.LENGTH_SHORT).show()
            return
        }

        // Combine all digits into a single code
        val verificationCode = digitBoxes.joinToString("") { it.text.toString() }

        // Convert to integer for API request
        val resetCode = verificationCode.toIntOrNull()
        if (resetCode == null) {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        showLoading(true)

        // Create request object
        val verifyCodeRequest = VerifyCodeRequest(email, resetCode)

        // Call API to verify code
        apiService.verifyCode(verifyCodeRequest).enqueue(object : Callback<ForgotPasswordEmailResponse> {
            override fun onResponse(call: Call<ForgotPasswordEmailResponse>, response: Response<ForgotPasswordEmailResponse>) {
                showLoading(false)

                if (response.isSuccessful && response.body()?.success == true) {
                    // Verification successful, navigate to Reset Password screen
                    Toast.makeText(
                        this@ForgotPasswordCodeActivity,
                        "Code verified successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToResetPassword(resetCode)
                } else {
                    // Handle error
                    val errorMessage = response.body()?.message ?: "Verification failed. Please try again."
                    Toast.makeText(this@ForgotPasswordCodeActivity, errorMessage, Toast.LENGTH_SHORT).show()

                    // Log error details
                    Log.e("CODE_VERIFICATION", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ForgotPasswordEmailResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(
                    this@ForgotPasswordCodeActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

                // Log error details
                Log.e("CODE_VERIFICATION", "Network failure: ${t.message}", t)
            }
        })
    }

    private fun navigateToResetPassword(resetCode: Int) {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("reset_code", resetCode)
        startActivity(intent)
        finish() // Close this activity to prevent going back
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            overlayBackground.visibility = View.VISIBLE
            loadingAnimation.visibility = View.VISIBLE
            verifyButton.isEnabled = false
        } else {
            overlayBackground.visibility = View.GONE
            loadingAnimation.visibility = View.GONE
            verifyButton.isEnabled = true
        }
    }
}