package com.example.j_mabmobile

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AccountAndSecurityActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var editPersonalInfoButton: ImageButton
    lateinit var editSecurityButton: ImageButton
    lateinit var editContactInfoButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account_and_security)

        editPersonalInfoButton = findViewById(R.id.editPersonalInfoButton)
        editSecurityButton = findViewById(R.id.editSecurityButton)
        editContactInfoButton = findViewById(R.id.editContactInfoButton)

        backBtn = findViewById(R.id.backButton)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        editPersonalInfoButton.setOnClickListener {
            val dialog = EditPersonalInfoDialog()
            dialog.show(supportFragmentManager, "EditPersonalInfoDialog")
        }

        editSecurityButton.setOnClickListener {
            val dialog = EditSecurityDialog()
            dialog.show(supportFragmentManager, "EditSecurityDialog")
        }

        editContactInfoButton.setOnClickListener {
            val dialog = EditContactInfoDialog()
            dialog.show(supportFragmentManager, "EditContactInfoDialog")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
