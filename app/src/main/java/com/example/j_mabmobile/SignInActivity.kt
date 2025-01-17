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
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SignInActivity : AppCompatActivity() {

    private lateinit var signInBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        signInBtn = findViewById(R.id.signInBtn)

        val tvSignInHere = findViewById<TextView>(R.id.tvSignInHere)
        val text = "Don't have an account? Sign up"
        val spannableString = SpannableString(text)

        // Set the clickable span for "Sign up"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to SignUpActivity
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

        tvSignInHere.text = spannableString
        tvSignInHere.movementMethod = LinkMovementMethod.getInstance()

        signInBtn.setOnClickListener {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
