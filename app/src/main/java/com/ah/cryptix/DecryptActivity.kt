package com.ah.cryptix

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlin.math.roundToInt

class DecryptActivity : AppCompatActivity() {

    private lateinit var etEncryptedText: EditText
    private lateinit var spinnerMatrixSize: Spinner
    private lateinit var grid2x2: LinearLayout
    private lateinit var grid3x3: LinearLayout
    private lateinit var btnDecryptNow: Button
    private lateinit var tvDecryptedResult: TextView
    private lateinit var btnCopyDecrypted: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt)

        initializeViews()
        setupMatrixSizeSpinner()
        setupDecryptButton()
        setupCopyButton()
    }

    private fun initializeViews() {
        etEncryptedText = findViewById(R.id.etEncryptedText)
        spinnerMatrixSize = findViewById(R.id.spinnerMatrixSize)
        grid2x2 = findViewById(R.id.grid2x2)
        grid3x3 = findViewById(R.id.grid3x3)
        btnDecryptNow = findViewById(R.id.btnDecryptNow)
        tvDecryptedResult = findViewById(R.id.tvDecryptedResult)
        btnCopyDecrypted = findViewById(R.id.btnCopyDecrypted)
    }

    private fun setupMatrixSizeSpinner() {
        val matrixSizes = arrayOf("2x2 Matrix", "3x3 Matrix")

        // Use custom adapter with white text
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, matrixSizes)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)

        spinnerMatrixSize.adapter = adapter

        spinnerMatrixSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // Set text color for selected item
                (view as? TextView)?.setTextColor(resources.getColor(android.R.color.white, null))

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

    private fun setupDecryptButton() {
        btnDecryptNow.setOnClickListener {
            val encryptedText = etEncryptedText.text.toString().trim()

            if (encryptedText.isEmpty()) {
                Toast.makeText(this, "Please enter text to decrypt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val decryptedText = when (spinnerMatrixSize.selectedItemPosition) {
                    0 -> decryptWith2x2Matrix(encryptedText)
                    1 -> decryptWith3x3Matrix(encryptedText)
                    else -> throw IllegalArgumentException("Invalid matrix size")
                }

                tvDecryptedResult.text = "Decrypted: $decryptedText"
                tvDecryptedResult.visibility = TextView.VISIBLE
                btnCopyDecrypted.visibility = Button.VISIBLE

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupCopyButton() {
        btnCopyDecrypted.setOnClickListener {
            val decryptedText = tvDecryptedResult.text.toString().replace("Decrypted: ", "")
            copyToClipboard(decryptedText)
            Toast.makeText(this, "Decrypted text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decryptWith2x2Matrix(text: String): String {
        // Get matrix values from input fields
        val k11 = findViewById<EditText>(R.id.et2x2_11).text.toString().toInt()
        val k12 = findViewById<EditText>(R.id.et2x2_12).text.toString().toInt()
        val k21 = findViewById<EditText>(R.id.et2x2_21).text.toString().toInt()
        val k22 = findViewById<EditText>(R.id.et2x2_22).text.toString().toInt()

        val keyMatrix = arrayOf(
            intArrayOf(k11, k12),
            intArrayOf(k21, k22)
        )

        return hillCipherDecrypt(text, keyMatrix)
    }

    private fun decryptWith3x3Matrix(text: String): String {
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

        return hillCipherDecrypt(text, keyMatrix)
    }

    private fun hillCipherDecrypt(cipherText: String, keyMatrix: Array<IntArray>): String {
        val n = keyMatrix.size
        val text = cipherText.uppercase().replace("[^A-Z]".toRegex(), "")

        // Calculate inverse matrix
        val inverseMatrix = when (n) {
            2 -> calculate2x2Inverse(keyMatrix)
            3 -> calculate3x3Inverse(keyMatrix)
            else -> throw IllegalArgumentException("Unsupported matrix size")
        }

        val result = StringBuilder()

        for (i in text.indices step n) {
            val block = text.substring(i, i + n)
            val vector = IntArray(n) { j -> block[j] - 'A' }
            val decryptedVector = IntArray(n)

            // CORRECTED: Row vector × Inverse matrix multiplication
            for (col in 0 until n) {
                var sum = 0
                for (row in 0 until n) {
                    sum += vector[row] * inverseMatrix[row][col]
                }
                decryptedVector[col] = sum % 26
                if (decryptedVector[col] < 0) decryptedVector[col] += 26
            }

            // Convert back to letters
            for (value in decryptedVector) {
                result.append((value + 'A'.code).toChar())
            }
        }

        return result.toString()
    }

    private fun calculate2x2Inverse(matrix: Array<IntArray>): Array<IntArray> {
        val a = matrix[0][0]
        val b = matrix[0][1]
        val c = matrix[1][0]
        val d = matrix[1][1] 

        // Calculate determinant
        val det = (a * d - b * c) % 26
        if (det == 0) throw IllegalArgumentException("Matrix is not invertible")

        // Find modular inverse of determinant
        val detInverse = modInverse(det, 26)

        // Calculate inverse matrix
        return arrayOf(
            intArrayOf((d * detInverse) % 26, (-b * detInverse) % 26),
            intArrayOf((-c * detInverse) % 26, (a * detInverse) % 26)
        ).map { row -> row.map { (it + 26) % 26 }.toIntArray() }.toTypedArray()
    }

    private fun calculate3x3Inverse(matrix: Array<IntArray>): Array<IntArray> {
        val a = matrix[0][0]
        val b = matrix[0][1]
        val c = matrix[0][2]
        val d = matrix[1][0]
        val e = matrix[1][1]
        val f = matrix[1][2]
        val g = matrix[2][0]
        val h = matrix[2][1]
        val i = matrix[2][2]

        // Calculate determinant
        val det = (a*(e*i - f*h) - b*(d*i - f*g) + c*(d*h - e*g)) % 26
        if (det == 0) throw IllegalArgumentException("Matrix is not invertible")

        // Find modular inverse of determinant
        val detInverse = modInverse(det, 26)

        // Calculate adjugate matrix
        val adj = arrayOf(
            intArrayOf((e*i - f*h), -(b*i - c*h), (b*f - c*e)),
            intArrayOf(-(d*i - f*g), (a*i - c*g), -(a*f - c*d)),
            intArrayOf((d*h - e*g), -(a*h - b*g), (a*e - b*d))
        )

        // Calculate inverse: (adj * detInverse) mod 26
        return Array(3) { row ->
            IntArray(3) { col ->
                var value = adj[row][col] * detInverse
                value %= 26
                if (value < 0) value += 26
                value
            }
        }
    }

    private fun modInverse(a: Int, m: Int): Int {
        var a = a % m
        // Ensure a is positive
        if (a < 0) {
            a += m
        }
        for (x in 1 until m) {
            if ((a * x) % m == 1) {
                return x
            }
        }
        throw IllegalArgumentException("Modular inverse doesn't exist")
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Decrypted Text", text)
        clipboard.setPrimaryClip(clip)
    }
}