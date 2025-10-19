package com.ah.cryptix

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class EncryptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt)

        val etPlainText = findViewById<EditText>(R.id.etPlainText)
        val etEncryptKey = findViewById<EditText>(R.id.etEncryptKey)
        val btnEncryptNow = findViewById<Button>(R.id.btnEncryptNow)
        val tvEncryptedResult = findViewById<TextView>(R.id.tvEncryptedResult)

        btnEncryptNow.setOnClickListener {
            val plainText = etPlainText.text.toString()
            val key = etEncryptKey.text.toString()

            if (plainText.isEmpty() || key.isEmpty()) {
                Toast.makeText(this, "Please enter both text and key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple XOR encryption (for demonstration)
            val encryptedText = encryptText(plainText, key)
            tvEncryptedResult.text = "Encrypted: $encryptedText"
            tvEncryptedResult.visibility = TextView.VISIBLE
        }
    }

    private fun encryptText(text: String, key: String): String {
        val result = StringBuilder()
        for (i in text.indices) {
            val textChar = text[i].code
            val keyChar = key[i % key.length].code
            val encryptedChar = (textChar xor keyChar).toChar()
            result.append(encryptedChar)
        }
        return result.toString()
    }
}