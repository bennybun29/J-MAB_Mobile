package com.example.j_mabmobile

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var tvGlobeNumber: TextView
    lateinit var tvSmartNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        backBtn = findViewById(R.id.backBtn)
        tvGlobeNumber = findViewById(R.id.tvGlobeNumber)
        tvSmartNumber = findViewById(R.id.tvSmartNumber)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        tvGlobeNumber.setOnClickListener { showOptionsDialog("09777693620") }
        tvSmartNumber.setOnClickListener { showOptionsDialog("09462896767") }

        underlineTextView(tvGlobeNumber, "GLOBE: 0977 769 3620", "0977 769 3620")
        underlineTextView(tvSmartNumber, "SMART: 0946 289 6767", "0946 289 6767")
    }

    private fun showOptionsDialog(phoneNumber: String) {
        val options = arrayOf("Call", "Text")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an action")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> callNumber(phoneNumber)
                1 -> textNumber(phoneNumber)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun callNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun textNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("sms:$phoneNumber")
        startActivity(intent)
    }

    private fun underlineTextView(textView: TextView, fullText: String, phoneNumber: String) {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(phoneNumber)

        if (startIndex != -1) {
            spannable.setSpan(UnderlineSpan(), startIndex, startIndex + phoneNumber.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannable
        textView.setTextColor(Color.BLUE)
    }

}
