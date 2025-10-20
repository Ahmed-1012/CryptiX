package com.ah.cryptix

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat

class EncryptActivity : AppCompatActivity() {

    private lateinit var etPlainText: EditText
    private lateinit var spinnerMatrixSize: Spinner
    private lateinit var grid2x2: LinearLayout
    private lateinit var grid3x3: LinearLayout
    private lateinit var btnEncryptNow: Button
    private lateinit var tvEncryptedResult: TextView
    private lateinit var btnCopyEncrypted: Button
    private lateinit var tvMatrixWarning: TextView

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
        tvMatrixWarning = findViewById(R.id.tvMatrixWarning)
    }

    private fun setupMatrixSizeSpinner() {
        val matrixSizes = arrayOf("2x2 Matrix", "3x3 Matrix")

        // Use default adapter but customize it
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, matrixSizes) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(this@EncryptActivity, android.R.color.white))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(this@EncryptActivity, android.R.color.white))
                textView.setBackgroundColor(ContextCompat.getColor(this@EncryptActivity, android.R.color.black))
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMatrixSize.adapter = adapter

        spinnerMatrixSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Hide warning when switching matrix sizes
                tvMatrixWarning.visibility = TextView.GONE

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
                // First check if matrix is invertible
                val isInvertible = when (spinnerMatrixSize.selectedItemPosition) {
                    0 -> is2x2MatrixInvertible()
                    1 -> is3x3MatrixInvertible()
                    else -> false
                }

                // Show warning if matrix is not invertible
                if (!isInvertible) {
                    tvMatrixWarning.text = "⚠️ Warning: This matrix is not invertible! Decryption will not be possible."
                    tvMatrixWarning.visibility = TextView.VISIBLE
                } else {
                    tvMatrixWarning.visibility = TextView.GONE
                }

                // Proceed with encryption anyway
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

    private fun is2x2MatrixInvertible(): Boolean {
        try {
            val k11 = findViewById<EditText>(R.id.et2x2_11).text.toString().toInt()
            val k12 = findViewById<EditText>(R.id.et2x2_12).text.toString().toInt()
            val k21 = findViewById<EditText>(R.id.et2x2_21).text.toString().toInt()
            val k22 = findViewById<EditText>(R.id.et2x2_22).text.toString().toInt()

            val keyMatrix = arrayOf(
                intArrayOf(k11, k12),
                intArrayOf(k21, k22)
            )

            // Try to calculate inverse - if it fails, matrix is not invertible
            calculate2x2Inverse(keyMatrix)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun is3x3MatrixInvertible(): Boolean {
        try {
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

            // Try to calculate inverse - if it fails, matrix is not invertible
            calculate3x3Inverse(keyMatrix)
            return true
        } catch (e: Exception) {
            return false
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

            // CORRECTED: Row vector × Key matrix multiplication
            // For each column in the key matrix
            for (col in 0 until n) {
                var sum = 0
                // Multiply row vector elements with key matrix column elements
                for (row in 0 until n) {
                    sum += vector[row] * keyMatrix[row][col]
                }
                encryptedVector[col] = sum % 26
                if (encryptedVector[col] < 0) encryptedVector[col] += 26
            }

            // Convert back to letters
            for (value in encryptedVector) {
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
        val clip = ClipData.newPlainText("Encrypted Text", text)
        clipboard.setPrimaryClip(clip)
    }
}