package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class NewAddressActivity : AppCompatActivity()  {

    private lateinit var backBtn : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_address)

        backBtn = findViewById(R.id.backButton)

        backBtn.setOnClickListener{
            onBackPressed()
        }
    }


}