package com.ah.cryptix

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEncrypt = findViewById<Button>(R.id.btnEncrypt)
        val btnDecrypt = findViewById<Button>(R.id.btnDecrypt)
        val btnInfo = findViewById<Button>(R.id.btnInfo)

        btnEncrypt.setOnClickListener {
            val intent = Intent(this, EncryptActivity::class.java)
            startActivity(intent)
        }

        btnDecrypt.setOnClickListener {
            val intent = Intent(this, DecryptActivity::class.java)
            startActivity(intent)
        }
        btnInfo.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }
    }
}