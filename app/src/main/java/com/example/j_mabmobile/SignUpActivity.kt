package com.example.j_mabmobile

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
import com.example.j_mabmobile.SignInActivity
import com.example.j_mabmobile.api.ApiResponse
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.SignUpRequest
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailTextField : EditText
    private lateinit var lastNameTextField : EditText
    private lateinit var firstNameTextField : EditText
    private lateinit var passwordTextField : EditText
    private lateinit var signUpBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        emailTextField = findViewById(R.id.emailAddress)
        lastNameTextField = findViewById(R.id.lastName)
        firstNameTextField = findViewById(R.id.firstName)
        passwordTextField = findViewById(R.id.userPassword)
        signUpBtn = findViewById(R.id.signUpBtn)

        val tvSignInHere = findViewById<TextView>(R.id.tvSignInHere)
        val text = "Already have an account? Sign in"
        val spannableString = SpannableString(text)

        // Set the clickable span for "Sign in here"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle the click event, e.g., navigate to SignInActivity
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            }
        }

        val signInStart = text.indexOf("Sign in")
        val signInEnd = signInStart + "Sign in".length

        spannableString.setSpan(clickableSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(
            ForegroundColorSpan(Color.rgb(2,37,75)),
            signInStart,
            signInEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(object : UnderlineSpan() {
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.isUnderlineText = false // Disable underline
            }
        }, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvSignInHere.text = spannableString
        tvSignInHere.movementMethod = LinkMovementMethod.getInstance()

        // Sign Up Button Click Listener
        signUpBtn.setOnClickListener {
            val email = emailTextField.text.toString()
            val firstName = firstNameTextField.text.toString()
            val lastName = lastNameTextField.text.toString()
            val password = passwordTextField.text.toString()

            // Validation
            if (email.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && password.isNotEmpty()) {
                val signUpRequest = SignUpRequest(firstName, lastName, email, password)
                signUpUser(signUpRequest)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUpUser(signUpRequest: SignUpRequest) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val call = apiService.register(signUpRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    // Registration success, navigate to MainActivity
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        Toast.makeText(this@SignUpActivity, apiResponse.message, Toast.LENGTH_SHORT).show()
                        if (response.code() == 201) { // Successful registration
                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Handle unexpected successful response code if needed
                            Toast.makeText(this@SignUpActivity, "Unexpected success response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Show the error message returned by the backend
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
}
