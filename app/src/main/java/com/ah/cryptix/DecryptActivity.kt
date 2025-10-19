package com.ah.cryptix

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class DecryptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt)

        val etEncryptedText = findViewById<EditText>(R.id.etEncryptedText)
        val etDecryptKey = findViewById<EditText>(R.id.etDecryptKey)
        val btnDecryptNow = findViewById<Button>(R.id.btnDecryptNow)
        val tvDecryptedResult = findViewById<TextView>(R.id.tvDecryptedResult)

        btnDecryptNow.setOnClickListener {
            val encryptedText = etEncryptedText.text.toString()
            val key = etDecryptKey.text.toString()

            if (encryptedText.isEmpty() || key.isEmpty()) {
                Toast.makeText(this, "Please enter both text and key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // XOR decryption (same as encryption)
            val decryptedText = decryptText(encryptedText, key)
            tvDecryptedResult.text = "Decrypted: $decryptedText"
            tvDecryptedResult.visibility = TextView.VISIBLE
        }
    }

    private fun decryptText(text: String, key: String): String {
        // XOR encryption and decryption are the same operation
        return encryptText(text, key)
    }

    private fun encryptText(text: String, key: String): String {
        val result = StringBuilder()
        for (i in text.indices) {
            val textChar = text[i].code
            val keyChar = key[i % key.length].code
            val decryptedChar = (textChar xor keyChar).toChar()
            result.append(decryptedChar)
        }
        return result.toString()
    }
}