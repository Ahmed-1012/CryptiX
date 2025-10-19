package com.ah.cryptix

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlin.math.roundToInt

class EncryptActivity : AppCompatActivity() {

    private lateinit var etPlainText: EditText
    private lateinit var spinnerMatrixSize: Spinner
    private lateinit var grid2x2: LinearLayout
    private lateinit var grid3x3: LinearLayout
    private lateinit var btnEncryptNow: Button
    private lateinit var tvEncryptedResult: TextView
    private lateinit var btnCopyEncrypted: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt)

        initializeViews()
        setupMatrixSizeSpinner()
        setupEncryptButton()
        setupCopyButton()
    }

    private fun initializeViews() {
        etPlainText = findViewById(R.id.etPlainText)
        spinnerMatrixSize = findViewById(R.id.spinnerMatrixSize)
        grid2x2 = findViewById(R.id.grid2x2)
        grid3x3 = findViewById(R.id.grid3x3)
        btnEncryptNow = findViewById(R.id.btnEncryptNow)
        tvEncryptedResult = findViewById(R.id.tvEncryptedResult)
        btnCopyEncrypted = findViewById(R.id.btnCopyEncrypted)
    }

    private fun setupMatrixSizeSpinner() {
        val matrixSizes = arrayOf("2x2 Matrix", "3x3 Matrix")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, matrixSizes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMatrixSize.adapter = adapter

        spinnerMatrixSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        grid2x2.visibility = LinearLayout.VISIBLE
                        grid3x3.visibility = LinearLayout.GONE
                    }
                    1 -> {
                        grid2x2.visibility = LinearLayout.GONE
                        grid3x3.visibility = LinearLayout.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupEncryptButton() {
        btnEncryptNow.setOnClickListener {
            val plainText = etPlainText.text.toString().trim()

            if (plainText.isEmpty()) {
                Toast.makeText(this, "Please enter text to encrypt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val encryptedText = when (spinnerMatrixSize.selectedItemPosition) {
                    0 -> encryptWith2x2Matrix(plainText)
                    1 -> encryptWith3x3Matrix(plainText)
                    else -> throw IllegalArgumentException("Invalid matrix size")
                }

                tvEncryptedResult.text = "Encrypted: $encryptedText"
                tvEncryptedResult.visibility = TextView.VISIBLE
                btnCopyEncrypted.visibility = Button.VISIBLE

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupCopyButton() {
        btnCopyEncrypted.setOnClickListener {
            val encryptedText = tvEncryptedResult.text.toString().replace("Encrypted: ", "")
            copyToClipboard(encryptedText)
            Toast.makeText(this, "Encrypted text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encryptWith2x2Matrix(text: String): String {
        // Get matrix values from input fields
        val k11 = findViewById<EditText>(R.id.et2x2_11).text.toString().toInt()
        val k12 = findViewById<EditText>(R.id.et2x2_12).text.toString().toInt()
        val k21 = findViewById<EditText>(R.id.et2x2_21).text.toString().toInt()
        val k22 = findViewById<EditText>(R.id.et2x2_22).text.toString().toInt()

        val keyMatrix = arrayOf(
            intArrayOf(k11, k12),
            intArrayOf(k21, k22)
        )

        return hillCipherEncrypt(text, keyMatrix)
    }

    private fun encryptWith3x3Matrix(text: String): String {
        // Get matrix values from input fields
        val k11 = findViewById<EditText>(R.id.et3x3_11).text.toString().toInt()
        val k12 = findViewById<EditText>(R.id.et3x3_12).text.toString().toInt()
        val k13 = findViewById<EditText>(R.id.et3x3_13).text.toString().toInt()
        val k21 = findViewById<EditText>(R.id.et3x3_21).text.toString().toInt()
        val k22 = findViewById<EditText>(R.id.et3x3_22).text.toString().toInt()
        val k23 = findViewById<EditText>(R.id.et3x3_23).text.toString().toInt()
        val k31 = findViewById<EditText>(R.id.et3x3_31).text.toString().toInt()
        val k32 = findViewById<EditText>(R.id.et3x3_32).text.toString().toInt()
        val k33 = findViewById<EditText>(R.id.et3x3_33).text.toString().toInt()

        val keyMatrix = arrayOf(
            intArrayOf(k11, k12, k13),
            intArrayOf(k21, k22, k23),
            intArrayOf(k31, k32, k33)
        )

        return hillCipherEncrypt(text, keyMatrix)
    }

    private fun hillCipherEncrypt(plainText: String, keyMatrix: Array<IntArray>): String {
        val n = keyMatrix.size
        var text = plainText.uppercase().replace("[^A-Z]".toRegex(), "")

        // Pad text if necessary
        while (text.length % n != 0) {
            text += 'X'
        }

        val result = StringBuilder()

        for (i in text.indices step n) {
            val block = text.substring(i, i + n)
            val vector = IntArray(n) { j -> block[j] - 'A' }
            val encryptedVector = IntArray(n)

            // Matrix multiplication: C = (P * K) mod 26
            for (row in 0 until n) {
                var sum = 0
                for (col in 0 until n) {
                    sum += vector[col] * keyMatrix[row][col]
                }
                encryptedVector[row] = sum % 26
                if (encryptedVector[row] < 0) encryptedVector[row] += 26
            }

            // Convert back to letters
            for (value in encryptedVector) {
                result.append((value + 'A'.code).toChar())
            }
        }

        return result.toString()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Encrypted Text", text)
        clipboard.setPrimaryClip(clip)
    }
}