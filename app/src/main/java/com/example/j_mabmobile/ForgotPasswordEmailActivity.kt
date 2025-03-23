package com.example.j_mabmobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ForgotPasswordEmailRequest
import com.example.j_mabmobile.model.ForgotPasswordEmailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordEmailActivity : AppCompatActivity() {

    private lateinit var sendEmailButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var backBtn: ImageButton
    private lateinit var progressBar: LottieAnimationView
    private lateinit var overlayBackground: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_email)

        backBtn = findViewById(R.id.backBtn)
        emailEditText = findViewById(R.id.emailAddress)
        sendEmailButton = findViewById(R.id.send_email_button)
        progressBar = findViewById(R.id.progressBar)
        overlayBackground = findViewById(R.id.overlayBackground)

        sendEmailButton.setBackgroundColor(Color.LTGRAY)
        sendEmailButton.isEnabled = false

        backBtn.setOnClickListener {
            onBackPressed()
        }

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateEmail()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        sendEmailButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendForgotPasswordEmail(email)
            } else {
                val emailErrorText: TextView = findViewById(R.id.emailErrorText)
                emailErrorText.text = "Invalid email"
                sendEmailButton.setBackgroundColor(Color.LTGRAY)
            }
        }
    }


    private fun sendForgotPasswordEmail(email: String) {
        showLoading()

        val apiService = RetrofitClient.getApiService(this)
        val request = ForgotPasswordEmailRequest(email)

        apiService.forgotPassword(request).enqueue(object : Callback<ForgotPasswordEmailResponse> {
            override fun onResponse(
                call: Call<ForgotPasswordEmailResponse>,
                response: Response<ForgotPasswordEmailResponse>
            ) {
                hideLoading()
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@ForgotPasswordEmailActivity, response.body()?.message ?: "Success", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@ForgotPasswordEmailActivity, ForgotPasswordCodeActivity::class.java)
                    intent.putExtra("email", email) // Pass email to next activity
                    startActivity(intent)
                } else {
                    Toast.makeText(this@ForgotPasswordEmailActivity, response.body()?.message ?: "Failed to send email", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ForgotPasswordEmailResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ForgotPasswordEmailActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun validateEmail() {
        val email = emailEditText.text.toString().trim()
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        val emailErrorText: TextView = findViewById(R.id.emailErrorText)

        if (!isValid) {
            emailErrorText.text = "Invalid email" // Set error text
            sendEmailButton.setBackgroundColor(Color.LTGRAY)
            sendEmailButton.isEnabled = false
        } else {
            emailErrorText.text = "" // Clear error message
            sendEmailButton.setBackgroundColor(ContextCompat.getColor(this, R.color.j_mab_blue)) // Replace with your original button color
            sendEmailButton.isEnabled = true
        }
    }



    private fun showLoading() {
        overlayBackground.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        progressBar.playAnimation()
    }

    private fun hideLoading() {
        overlayBackground.visibility = View.GONE
        progressBar.visibility = View.GONE
        progressBar.pauseAnimation()
    }

}