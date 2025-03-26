package com.example.j_mabmobile

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var tvGlobeNumber: TextView
    lateinit var tvSmartNumber: TextView
    lateinit var googleMapsButton: ImageButton
    lateinit var facebookBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        backBtn = findViewById(R.id.backBtn)
        tvGlobeNumber = findViewById(R.id.tvGlobeNumber)
        tvSmartNumber = findViewById(R.id.tvSmartNumber)
        googleMapsButton = findViewById(R.id.mapsBtn)
        facebookBtn = findViewById(R.id.facebookBtn)


        backBtn.setOnClickListener {
            onBackPressed()
        }

        tvGlobeNumber.setOnClickListener { showOptionsDialog("09777693620") }
        tvSmartNumber.setOnClickListener { showOptionsDialog("09462896767") }

        underlineTextView(tvGlobeNumber, "GLOBE: 0977 769 3620", "0977 769 3620")
        underlineTextView(tvSmartNumber, "SMART: 0946 289 6767", "0946 289 6767")


        googleMapsButton.setOnClickListener {
            openGoogleMaps()
        }

        facebookBtn.setOnClickListener{
            openFacebookPage()
        }

    }

    private fun openGoogleMaps() {
        val mapUrl = "https://www.google.com/maps/place/87+Tebeng+Rd,+Dagupan,+Pangasinan/@16.0356193,120.3567036,696m/data=!3m2!1e3!4b1!4m5!3m4!1s0x339142a6bd9e0ac9:0x917be9d95001da77!8m2!3d16.0356142!4d120.3592785?entry=ttu"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
            startActivity(webIntent)
        }

    }

    private fun openFacebookPage() {
        val facebookPageID = "jmab.trd"
        val facebookUrl = "https://www.facebook.com/$facebookPageID"
        val facebookAppUri = Uri.parse("fb://facewebmodal/f?href=$facebookUrl")

        val intent = Intent(Intent.ACTION_VIEW, facebookAppUri)
        intent.setPackage("com.facebook.katana")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
            startActivity(webIntent)
        }
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
