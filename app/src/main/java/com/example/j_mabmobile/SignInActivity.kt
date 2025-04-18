package com.example.j_mabmobile

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.SignUpActivity
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.LogInRequest
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var signInBtn: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var forgotPassword: TextView
    private lateinit var emailTIL: TextInputLayout
    private lateinit var passwordTIL: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        signInBtn = findViewById(R.id.signInBtn)
        emailEditText = findViewById(R.id.emailAddress)
        passwordEditText = findViewById(R.id.userPassword)
        forgotPassword = findViewById(R.id.forgotPassword)
        emailTIL = findViewById(R.id.emailTIL)
        passwordTIL = findViewById(R.id.passwordTIL)

        signInBtn.isEnabled = false
        signInBtn.setBackgroundColor(Color.LTGRAY)

        emailEditText.addTextChangedListener(SimpleTextWatcher { checkFields() })
        passwordEditText.addTextChangedListener(SimpleTextWatcher { checkFields() })


        if (isTokenValid()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupSignUpLink()
        setupForgotPasswordLink()

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                emailTIL.error = null // Clear error on text change
                emailTIL.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
                checkFields() // Update button state
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordTIL.error = null // Clear error on text change
                passwordTIL.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
                checkFields() // Update button state
            }
        })


        signInBtn.setOnClickListener {
            if (validateInputs()) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val logInRequest = LogInRequest(email, password)
                loginUser(logInRequest)
            }
        }
    }

    private fun setupForgotPasswordLink() {
        val text = "Forgot Password?"
        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignInActivity, ForgotPasswordEmailActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.rgb(2, 37, 75) // Set the color
                ds.isUnderlineText = true // Add underline
            }
        }

        spannableString.setSpan(clickableSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        forgotPassword.text = spannableString
        forgotPassword.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupSignUpLink() {
        val tvSignUpHere = findViewById<TextView>(R.id.tvSignInHere)
        val text = "Don't have an account? Sign up"
        val spannableString = SpannableString(text)

        // Set the clickable span for "Sign up"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }
        }

        val signInStart = text.indexOf("Sign up")
        val signInEnd = signInStart + "Sign up".length

        spannableString.setSpan(clickableSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(
            ForegroundColorSpan(Color.rgb(2, 37, 75)),
            signInStart,
            signInEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(object : UnderlineSpan() {
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.isUnderlineText = true
            }
        }, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvSignUpHere.text = spannableString
        tvSignUpHere.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun loginUser(logInRequest: LogInRequest) {
        // Find the overlay and loading animation views
        val overlayBG: View = findViewById(R.id.overlayBG)
        val loadingAnimation: LottieAnimationView = findViewById(R.id.loadingAnimation)

        // Show overlay and loading animation
        overlayBG.visibility = View.VISIBLE
        loadingAnimation.visibility = View.VISIBLE

        val apiService = RetrofitClient.getRetrofitInstance(this)
        val call = apiService.create(ApiService::class.java).login(logInRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
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

                    // Wait for animation to complete before navigating
                    loadingAnimation.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {}

                        override fun onAnimationEnd(animator: Animator) {
                            // Hide overlay and animation
                            overlayBG.visibility = View.GONE
                            loadingAnimation.visibility = View.GONE

                            // Show success toast
                            Toast.makeText(
                                this@SignInActivity,
                                apiResponse?.message ?: "Login successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to MainActivity
                            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                            finishAffinity()
                        }

                        override fun onAnimationCancel(animator: Animator) {}

                        override fun onAnimationRepeat(animator: Animator) {}
                    })
                } else {
                    // Hide overlay and animation in case of error
                    overlayBG.visibility = View.GONE
                    loadingAnimation.visibility = View.GONE

                    try {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            val jsonObject = JSONObject(it)
                            val errorMessage = jsonObject.optString("message", "Invalid email or password.")
                            Toast.makeText(this@SignInActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        } ?: run {
                            Toast.makeText(this@SignInActivity, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SignInActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                        Log.e("SignInError", "Error parsing JSON", e)
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Hide overlay and animation in case of network failure
                overlayBG.visibility = View.GONE
                loadingAnimation.visibility = View.GONE

                Log.e("LoginError", "Network error: ${t.message}", t)
                Toast.makeText(this@SignInActivity, "Invalid email or password.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveToken(token: String, expiresIn: Long) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 🔥 Force expiration to 7 days (604800 seconds) if API sends a lower value
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

        Log.d("UserID", "User ID saved: $userId")
    }

    private fun savePhoneNumber(phoneNumber: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("phone_number", phoneNumber)
        editor.apply()

        Log.d("Phone Number", "Phone Number saved: $phoneNumber")
    }

    private fun saveGender(gender: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("gender", gender)
        editor.apply()

        Log.d("User Gender", "User Gender saved: $gender")
    }

    private fun saveBirthday(birthday: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("birthday", birthday)
        editor.apply()

        Log.d("User Birthday", "User Birthday saved: $birthday")
    }

    private fun saveEmail(userEmail:String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_email", userEmail)
        editor.apply()

        Log.d("Email", "Email saved: $userEmail")
    }


    private fun isTokenValid(): Boolean {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)
        val expiryTime = sharedPreferences.getLong("token_expiry_time", 0L)

        if (token != null && System.currentTimeMillis() < expiryTime) {
            return true
        }

        return false
    }

    private fun checkFields() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        val allFieldsFilled = email.isNotEmpty() && password.isNotEmpty()

        if (allFieldsFilled) {
            signInBtn.isEnabled = true
            signInBtn.setBackgroundColor(Color.parseColor("#02254B")) // Original color
        } else {
            signInBtn.isEnabled = false
            signInBtn.setBackgroundColor(Color.LTGRAY) // Greyed out when not clickable
        }
    }

    private fun validateInputs(): Boolean {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            emailTIL.error = "Email cannot be empty"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailTIL.error = "Please enter a valid email address"
            isValid = false
        } else {
            emailTIL.error = null
        }

        // Validate password
        if (password.isEmpty()) {
            passwordTIL.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 8) {
            passwordTIL.error = "Password should be at least 8 characters"
            isValid = false
        } else {
            passwordTIL.error = null
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

}

