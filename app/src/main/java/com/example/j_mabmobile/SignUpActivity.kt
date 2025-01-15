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
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.j_mabmobile.SignInActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailTextField : EditText
    private lateinit var usernameTextField : EditText
    private lateinit var passwordTextField : EditText
    private lateinit var signUpBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

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

        signUpBtn.setOnClickListener {
            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}