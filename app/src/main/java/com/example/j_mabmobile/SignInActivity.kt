package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.j_mabmobile.api.ApiResponse
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
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

        signInBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validate user input
            if (email.isNotEmpty() && password.isNotEmpty()) {
                val logInRequest = LogInRequest(email, password)
                loginUser(logInRequest)
            } else {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(logInRequest: LogInRequest) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val call = apiService.login(logInRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    // Login success, proceed to MainActivity
                    val apiResponse = response.body()
                    Toast.makeText(this@SignInActivity, apiResponse?.message ?: "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Finish the current activity to avoid returning to it on back press
                } else {
                    // Login failed
                    try {
                        val errorResponse = response.errorBody()?.string()
                        val errorMessage = JSONObject(errorResponse).getString("message")
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
}
