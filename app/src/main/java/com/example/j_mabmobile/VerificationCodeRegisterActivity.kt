package com.example.j_mabmobile

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.LogInRequest
import com.example.j_mabmobile.model.VerificationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificationCodeRegisterActivity : AppCompatActivity() {

    private lateinit var digitBox1: EditText
    private lateinit var digitBox2: EditText
    private lateinit var digitBox3: EditText
    private lateinit var digitBox4: EditText
    private lateinit var digitBox5: EditText
    private lateinit var digitBox6: EditText
    private lateinit var verifyButton: Button
    private lateinit var tvResendCode: TextView
    private lateinit var tvSignInHere: TextView
    private lateinit var userPassword: String
    private lateinit var verificationSuccessfulProgressBar: LottieAnimationView
    private lateinit var overlayBackground: View

    // You should store the email from the previous screen
    private lateinit var userEmail: String

    // Retrofit service
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_code_register)

        userEmail = intent.getStringExtra("email") ?: ""
        userPassword = intent.getStringExtra("password") ?: ""

        apiService = RetrofitClient.getApiService(this)

        initViews()

        setSpannableText()

        setupDigitBoxes()

        setupClickListeners()
    }

    private fun initViews() {
        digitBox1 = findViewById(R.id.digitBox1)
        digitBox2 = findViewById(R.id.digitBox2)
        digitBox3 = findViewById(R.id.digitBox3)
        digitBox4 = findViewById(R.id.digitBox4)
        digitBox5 = findViewById(R.id.digitBox5)
        digitBox6 = findViewById(R.id.digitBox6)
        verifyButton = findViewById(R.id.signUpBtn)
        tvResendCode = findViewById(R.id.tvResendCode)
        tvSignInHere = findViewById(R.id.tvSignInHere)
        verificationSuccessfulProgressBar = findViewById(R.id.verificationSuccessfulProgressBar)
        overlayBackground = findViewById(R.id.overlayBackground)

        // Disable the verify button initially
        verifyButton.isEnabled = false
        verifyButton.setBackgroundColor(Color.LTGRAY)

        verificationSuccessfulProgressBar.visibility = View.GONE
        overlayBackground.visibility = View.GONE
    }

    private fun setupDigitBoxes() {
        // List of all digit boxes for easier iteration
        val digitBoxes = listOf(digitBox1, digitBox2, digitBox3, digitBox4, digitBox5, digitBox6)

        // Add text change listeners for moving forward
        digitBox1.addTextChangedListener(createTextWatcher(digitBox2))
        digitBox2.addTextChangedListener(createTextWatcher(digitBox3))
        digitBox3.addTextChangedListener(createTextWatcher(digitBox4))
        digitBox4.addTextChangedListener(createTextWatcher(digitBox5))
        digitBox5.addTextChangedListener(createTextWatcher(digitBox6))
        digitBox6.addTextChangedListener(createCompletionWatcher())

        // Add key listeners for backspace handling
        setupBackspaceHandling(digitBoxes)

        // Set initial focus
        digitBox1.requestFocus()
    }

    private fun setupBackspaceHandling(digitBoxes: List<EditText>) {
        // Loop through all boxes except the first one
        for (i in 1 until digitBoxes.size) {
            val currentBox = digitBoxes[i]
            val previousBox = digitBoxes[i - 1]

            // Add key listener to detect backspace press
            currentBox.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    currentBox.text.isEmpty()) {
                    // If backspace is pressed on an empty box, move to previous box
                    previousBox.apply {
                        requestFocus()
                        text.clear()
                    }
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    private fun setupClickListeners() {
        // Handle verify button click
        verifyButton.setOnClickListener {
            val code = getVerificationCode()
            if (code.isNotEmpty()) {
                verifyEmail(code.toInt())
            }
        }

        // Handle resend code click
        tvResendCode.setOnClickListener {
            // You would need to implement the resend logic here
            // For now, just clear the fields
            clearDigitBoxes()
            Toast.makeText(this, "Verification code resent", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper method to create text watchers for auto-focus
    private fun createTextWatcher(nextField: EditText): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    nextField.requestFocus()
                }
                // Check if verification is complete after each change
                checkVerificationComplete()
            }
        }
    }

    // Helper method to check if verification is complete
    private fun createCompletionWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkVerificationComplete()
            }
        }
    }

    // Check if all boxes are filled and enable the verify button
    private fun checkVerificationComplete() {
        val allDigitsFilled = digitBox1.text.isNotEmpty() &&
                digitBox2.text.isNotEmpty() &&
                digitBox3.text.isNotEmpty() &&
                digitBox4.text.isNotEmpty() &&
                digitBox5.text.isNotEmpty() &&
                digitBox6.text.isNotEmpty()

        // Enable/disable verify button based on completion
        verifyButton.isEnabled = allDigitsFilled

        // Change button appearance when enabled
        if (allDigitsFilled) {
            verifyButton.setBackgroundColor(ContextCompat.getColor(this, R.color.j_mab_blue))
        } else {
            verifyButton.setBackgroundColor(Color.LTGRAY)
        }
    }


    // Method to collect the complete verification code
    private fun getVerificationCode(): String {
        return digitBox1.text.toString() +
                digitBox2.text.toString() +
                digitBox3.text.toString() +
                digitBox4.text.toString() +
                digitBox5.text.toString() +
                digitBox6.text.toString()
    }

    // Method to clear all digit boxes
    private fun clearDigitBoxes() {
        digitBox1.text.clear()
        digitBox2.text.clear()
        digitBox3.text.clear()
        digitBox4.text.clear()
        digitBox5.text.clear()
        digitBox6.text.clear()

        // Set focus to the first box
        digitBox1.requestFocus()
    }

    // Method to call the API for email verification
    private fun verifyEmail(code: Int) {
        // Show loading indicator
        showLoading(true)

        // Create request object
        val verificationRequest = VerificationRequest(userEmail, code)

        // Launch API call in coroutine
        lifecycleScope.launch {
            try {
                Log.d("VERIFICATION", "Attempting to verify email: $userEmail with code: $code")
                val response = apiService.verifyEmail(verificationRequest)

                // Handle the response on the main thread
                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val verificationResponse = response.body()
                        Log.d("VERIFICATION", "Success response: ${verificationResponse?.message}")
                        if (verificationResponse?.success == true) {
                            performAutoLogin()
                        } else {
                            // Verification failed with API error message
                            Toast.makeText(this@VerificationCodeRegisterActivity,
                                verificationResponse?.message ?: "Verification failed",
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Handle HTTP error
                        val errorBody = response.errorBody()?.string()
                        Log.e("VERIFICATION", "Error response: $errorBody")
                        Toast.makeText(this@VerificationCodeRegisterActivity,
                            "Verification failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle network or other exceptions
                Log.e("VERIFICATION", "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@VerificationCodeRegisterActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performAutoLogin() {
        showLoading(true)

        val logInRequest = LogInRequest(userEmail, userPassword)
        val apiService = RetrofitClient.getRetrofitInstance(this)
        val call = apiService.create(ApiService::class.java).login(logInRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val token = apiResponse?.token
                    val firstName = apiResponse?.user?.first_name
                    val lastName = apiResponse?.user?.last_name
                    val userId = apiResponse?.user?.id
                    val expiresIn = apiResponse?.expiresIn ?: 3600L
                    val email = apiResponse?.user?.email
                    val phone_number = apiResponse?.user?.phone_number
                    val birthday = apiResponse?.user?.birthday
                    val gender = apiResponse?.user?.gender

                    // Save all user data as in SignInActivity
                    if (token != null) {
                        saveToken(token, expiresIn)
                    }

                    if (firstName != null && lastName != null) {
                        saveUserName(firstName, lastName)
                    }

                    if (userId != null) {
                        saveUserId(userId)
                    }

                    if (email != null) {
                        saveEmail(email)
                    }

                    if (phone_number != null) {
                        savePhoneNumber(phone_number)
                    }

                    if (gender != null) {
                        saveGender(gender)
                    }

                    if (birthday != null) {
                        saveBirthday(birthday)
                    }

                    Toast.makeText(
                        this@VerificationCodeRegisterActivity,
                        "Registration successful! You are now logged in.",
                        Toast.LENGTH_SHORT
                    ).show()

                    showSuccessAnimation()

                    // Navigate to MainActivity directly
                    val intent = Intent(this@VerificationCodeRegisterActivity, MainActivity::class.java)
                    intent.putExtra("navigate_to_home", true)
                    startActivity(intent)
                    finishAffinity() // Close all previous activities
                } else {
                    try {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            val jsonObject = JSONObject(it)
                            val errorMessage = jsonObject.optString("message", "Auto-login failed. Please sign in manually.")
                            Toast.makeText(this@VerificationCodeRegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            // Fall back to manual sign in
                            navigateToSignIn()
                        } ?: run {
                            Toast.makeText(this@VerificationCodeRegisterActivity, "Auto-login failed. Please sign in manually.", Toast.LENGTH_SHORT).show()
                            navigateToSignIn()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@VerificationCodeRegisterActivity, "Error during auto-login", Toast.LENGTH_SHORT).show()
                        Log.e("AutoLoginError", "Error parsing JSON", e)
                        navigateToSignIn()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                showLoading(false)
                Log.e("AutoLoginError", "Network error: ${t.message}", t)
                Toast.makeText(this@VerificationCodeRegisterActivity, "Auto-login failed. Please sign in manually.", Toast.LENGTH_SHORT).show()
                navigateToSignIn()
            }
        })
    }

    private fun showSuccessAnimation() {
        // Show overlay and animation
        overlayBackground.visibility = View.VISIBLE
        verificationSuccessfulProgressBar.visibility = View.VISIBLE

        // Make sure animation only plays once
        verificationSuccessfulProgressBar.repeatCount = 0

        // Disable all interactive elements during animation
        disableInteractions()

        // Add animation listener
        verificationSuccessfulProgressBar.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                Handler(Looper.getMainLooper()).postDelayed({
                    completeRegistrationAndNavigate()
                }, 2000)
            }

            override fun onAnimationCancel(animation: Animator) {
                // Just in case animation is canceled, still navigate with delay
                Handler(Looper.getMainLooper()).postDelayed({
                    completeRegistrationAndNavigate()
                }, 2000)
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })

        // Start the animation
        verificationSuccessfulProgressBar.playAnimation()
    }

    private fun disableInteractions() {
        // Disable all input fields
        digitBox1.isEnabled = false
        digitBox2.isEnabled = false
        digitBox3.isEnabled = false
        digitBox4.isEnabled = false
        digitBox5.isEnabled = false
        digitBox6.isEnabled = false

        // Disable buttons and clickable text views
        verifyButton.isEnabled = false
        tvResendCode.isEnabled = false
        tvSignInHere.isEnabled = false
    }

    // Add methods for saving user data (copied from SignInActivity)
    private fun saveToken(token: String, expiresIn: Long) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Force expiration to 7 days (604800 seconds) if API sends a lower value
        val correctedExpiry = if (expiresIn < 604800) 604800 else expiresIn

        val expiryTime = System.currentTimeMillis() + correctedExpiry * 1000
        editor.putString("jwt_token", token)
        editor.putLong("token_expiry_time", expiryTime)
        editor.apply()

        Log.d("Token", "Token saved with corrected expiry time: $expiryTime (expires in: $correctedExpiry seconds)")
    }

    private fun saveUserName(firstName: String, lastName: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("first_name", firstName)
        editor.putString("last_name", lastName)
        editor.apply()
    }

    private fun saveUserId(userId: Int) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("user_id", userId)
        editor.apply()
    }

    private fun savePhoneNumber(phoneNumber: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("phone_number", phoneNumber)
        editor.apply()
    }

    private fun saveGender(gender: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("gender", gender)
        editor.apply()
    }

    private fun saveBirthday(birthday: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("birthday", birthday)
        editor.apply()
    }

    private fun saveEmail(userEmail: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_email", userEmail)
        editor.apply()
    }

    // Fallback method to navigate to sign in if auto-login fails
    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Method to show/hide loading indicator
    private fun showLoading(isLoading: Boolean) {
        // Example: Show/hide a ProgressBar
        // findViewById<ProgressBar>(R.id.progressBar).visibility = if (isLoading) View.VISIBLE else View.GONE

        // Disable/enable UI elements during loading
        verifyButton.isEnabled = !isLoading
        tvResendCode.isEnabled = !isLoading

        // Optionally change button text to indicate loading
        verifyButton.text = if (isLoading) "Verifying..." else "Verify"
    }

    private fun setSpannableText() {
        val fullText = "Already have an account? Sign in"
        val spannableString = SpannableString(fullText)
        val signInText = "Sign in"
        val startIndex = fullText.indexOf(signInText)

        // Make "Sign in" underlined and clickable
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to login screen
                val intent = Intent(this@VerificationCodeRegisterActivity, SignInActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // Underline the text
                ds.color = ContextCompat.getColor(this@VerificationCodeRegisterActivity, R.color.j_mab_blue) // Change color if needed
            }
        }

        // Apply the span
        spannableString.setSpan(clickableSpan, startIndex, startIndex + signInText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the spannable text to TextView
        tvSignInHere.text = spannableString
        tvSignInHere.movementMethod = LinkMovementMethod.getInstance() // Make it clickable
        tvSignInHere.highlightColor = Color.TRANSPARENT // Remove background color on click
    }

    private fun completeRegistrationAndNavigate() {
        Toast.makeText(
            this@VerificationCodeRegisterActivity,
            "Registration successful! You are now logged in.",
            Toast.LENGTH_SHORT
        ).show()

        // Navigate to MainActivity
        val intent = Intent(this@VerificationCodeRegisterActivity, MainActivity::class.java)
        intent.putExtra("navigate_to_home", true)
        startActivity(intent)
        finishAffinity() // Close all previous activities
    }
}