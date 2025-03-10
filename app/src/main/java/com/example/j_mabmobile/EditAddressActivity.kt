package com.example.j_mabmobile

import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.model.UserAddresses

class EditAddressActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the CartActivity
        setContentView(R.layout.activity_edit_address)

        backBtn = findViewById(R.id.backButton)

        backBtn.setOnClickListener{
            onBackPressed()
        }
    }
}