package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.SignUpRequest
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailTextField: EditText
    private lateinit var lastNameTextField: EditText
    private lateinit var firstNameTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var confirmPasswordTextField: EditText
    private lateinit var signUpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Check if token is already present and valid
        val token = getToken()
        if (token != null && isTokenValid()) {
            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        emailTextField = findViewById(R.id.emailAddress)
        lastNameTextField = findViewById(R.id.lastName)
        firstNameTextField = findViewById(R.id.firstName)
        passwordTextField = findViewById(R.id.userPassword)
        confirmPasswordTextField = findViewById(R.id.confirmPassword)
        signUpBtn = findViewById(R.id.signUpBtn)

        signUpBtn.isEnabled = false
        signUpBtn.setBackgroundColor(Color.LTGRAY)

        emailTextField.addTextChangedListener(SimpleTextWatcher { toggleSignUpButton() })
        firstNameTextField.addTextChangedListener(SimpleTextWatcher { toggleSignUpButton() })
        lastNameTextField.addTextChangedListener(SimpleTextWatcher { toggleSignUpButton() })
        passwordTextField.addTextChangedListener(SimpleTextWatcher { toggleSignUpButton() })
        confirmPasswordTextField.addTextChangedListener(SimpleTextWatcher { toggleSignUpButton() })

        val tvSignInHere = findViewById<TextView>(R.id.tvSignInHere)
        val text = "Already have an account? Sign in"
        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            }
        }

        val signInStart = text.indexOf("Sign in")
        val signInEnd = signInStart + "Sign in".length

        spannableString.setSpan(clickableSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.rgb(2, 37, 75)), signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(object : UnderlineSpan() {
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.isUnderlineText = false
            }
        }, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvSignInHere.text = spannableString
        tvSignInHere.movementMethod = LinkMovementMethod.getInstance()

        signUpBtn.setOnClickListener {
            val email = emailTextField.text.toString().trim()
            val firstName = firstNameTextField.text.toString().trim()
            val lastName = lastNameTextField.text.toString().trim()
            val password = passwordTextField.text.toString().trim()
            val confirmPassword = confirmPasswordTextField.text.toString().trim()

            // Validate fields on button click
            if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!password.any { it.isDigit() }) {
                Toast.makeText(this, "Password must contain at least one number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!password.any { it.isUpperCase() }) {
                Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If all validations pass, proceed with sign-up request
            val signUpRequest = SignUpRequest(firstName, lastName, email, password)
            signUpUser(signUpRequest)
        }
    }

    private fun signUpUser(signUpRequest: SignUpRequest) {
        val apiService = RetrofitClient.getRetrofitInstance(this)
        val call = apiService.create(ApiService::class.java).register(signUpRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        Toast.makeText(this@SignUpActivity, apiResponse.message, Toast.LENGTH_SHORT).show()
                        if (response.code() == 201) { // Successful registration
                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }
                    }
                } else {
                    try {
                        val errorResponse = response.errorBody()?.string()
                        val errorMessage = JSONObject(errorResponse).getString("message")
                        Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@SignUpActivity, "An unknown error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    private fun isTokenValid(): Boolean {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val expiryTime = sharedPreferences.getLong("token_expiry_time", 0)
        return System.currentTimeMillis() < expiryTime
    }

    private fun toggleSignUpButton() {
        val allFieldsFilled = emailTextField.text.isNotEmpty() &&
                firstNameTextField.text.isNotEmpty() &&
                lastNameTextField.text.isNotEmpty() &&
                passwordTextField.text.isNotEmpty() &&
                confirmPasswordTextField.text.isNotEmpty()

        if (allFieldsFilled) {
            signUpBtn.isEnabled = true
            signUpBtn.setBackgroundColor(Color.parseColor("#02254B"))
        } else {
            signUpBtn.isEnabled = false
            signUpBtn.setBackgroundColor(Color.LTGRAY)
        }
    }
}
