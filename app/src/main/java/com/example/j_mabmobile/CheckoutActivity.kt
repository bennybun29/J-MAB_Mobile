package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class CheckoutActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var changeAddressBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener({
            onBackPressed()
        })

        changeAddressBtn = findViewById(R.id.changeAddressBtn)

        changeAddressBtn.setOnClickListener({
            onPause()
            val intent = Intent(this, MyAddressesActivity::class.java)
            startActivity(intent)
        })

    }
}