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
import android.util.Log
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
import com.example.j_mabmobile.model.LogInRequest
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var signInBtn: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        signInBtn = findViewById(R.id.signInBtn)
        emailEditText = findViewById(R.id.emailAddress)
        passwordEditText = findViewById(R.id.userPassword)

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

        signInBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val logInRequest = LogInRequest(email, password)
                loginUser(logInRequest)
            } else {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show()
            }
        }
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
                ds.isUnderlineText = false
            }
        }, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvSignUpHere.text = spannableString
        tvSignUpHere.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun loginUser(logInRequest: LogInRequest) {
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

                    if (token != null) {
                        saveToken(token, expiresIn)
                    }

                    if (firstName != null && lastName != null) {
                        saveUserName(firstName, lastName)
                    }

                    if (userId != null) {
                        saveUserId(userId)
                    }

                    Toast.makeText(this@SignInActivity, apiResponse?.message ?: "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finishAffinity()
                } else {
                    try {
                        val errorResponse = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorResponse)
                        val errorMessage = jsonObject.optString("message", "Invalid email or password.")  // Fix: Read `message`

                        Toast.makeText(this@SignInActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@SignInActivity, "An unknown error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@SignInActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveToken(token: String, expiresIn: Long) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val expiryTime = System.currentTimeMillis() + expiresIn * 1000
        editor.putString("jwt_token", token)
        editor.putLong("token_expiry_time", expiryTime)
        editor.apply()

        Log.d("Token", "Token saved with expiry time: $expiryTime")
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

}

