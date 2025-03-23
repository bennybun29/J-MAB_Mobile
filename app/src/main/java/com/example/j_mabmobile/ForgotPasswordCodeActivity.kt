package com.example.j_mabmobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class ForgotPasswordCodeActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    private lateinit var digitBox1: EditText
    private lateinit var digitBox2: EditText
    private lateinit var digitBox3: EditText
    private lateinit var digitBox4: EditText
    private lateinit var digitBox5: EditText
    private lateinit var digitBox6: EditText
    private lateinit var verifyButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_code)

        // Initialize views
        backBtn = findViewById(R.id.backBtn)
        digitBox1 = findViewById(R.id.digitBox1)
        digitBox2 = findViewById(R.id.digitBox2)
        digitBox3 = findViewById(R.id.digitBox3)
        digitBox4 = findViewById(R.id.digitBox4)
        digitBox5 = findViewById(R.id.digitBox5)
        digitBox6 = findViewById(R.id.digitBox6)
        verifyButton = findViewById(R.id.signUpBtn)

        // Set up back button
        backBtn.setOnClickListener {
            onBackPressed()
        }

        // Set up verify button
        verifyButton.setOnClickListener {
            validateAndSubmitCode()
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
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Not needed
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

    private fun validateAndSubmitCode() {
        val digitBoxes = listOf(digitBox1, digitBox2, digitBox3, digitBox4, digitBox5, digitBox6)

        // Check if all boxes are filled
        if (digitBoxes.any { it.text.isEmpty() }) {
            Toast.makeText(this, "Please enter the complete verification code", Toast.LENGTH_SHORT).show()
            return
        }

        // Combine all digits into a single code
        val verificationCode = digitBoxes.joinToString("") { it.text.toString() }

        // TODO: Add your verification logic here
        Toast.makeText(this, "Verifying code: $verificationCode", Toast.LENGTH_SHORT).show()

        // Example: Navigate to next screen after verification
        // val intent = Intent(this, ResetPasswordActivity::class.java)
        // intent.putExtra("verification_code", verificationCode)
        // startActivity(intent)
    }
}