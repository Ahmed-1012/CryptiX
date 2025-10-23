package com.ah.cryptix

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MatrixInverseActivity : AppCompatActivity() {

    private lateinit var spinnerMatrixSize: Spinner
    private lateinit var grid2x2: LinearLayout
    private lateinit var grid3x3: LinearLayout
    private lateinit var btnCalculateInverse: Button
    private lateinit var tvMatrixWarning: TextView
    private lateinit var tvResultTitle: TextView
    private lateinit var result2x2: LinearLayout
    private lateinit var result3x3: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matrix_inverse)

        initializeViews()
        setupMatrixSizeSpinner()
        setupCalculateButton()
    }

    private fun initializeViews() {
        spinnerMatrixSize = findViewById(R.id.spinnerMatrixSize)
        grid2x2 = findViewById(R.id.grid2x2)
        grid3x3 = findViewById(R.id.grid3x3)
        btnCalculateInverse = findViewById(R.id.btnCalculateInverse)
        tvMatrixWarning = findViewById(R.id.tvMatrixWarning)
        tvResultTitle = findViewById(R.id.tvResultTitle)
        result2x2 = findViewById(R.id.result2x2)
        result3x3 = findViewById(R.id.result3x3)
    }

    private fun setupMatrixSizeSpinner() {
        val matrixSizes = arrayOf("2x2 Matrix", "3x3 Matrix")

        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, matrixSizes)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinnerMatrixSize.adapter = adapter

        spinnerMatrixSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Hide all results and warnings when switching
                resetResults()

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

    private fun setupCalculateButton() {
        btnCalculateInverse.setOnClickListener {
            resetResults()

            try {
                when (spinnerMatrixSize.selectedItemPosition) {
                    0 -> calculate2x2Inverse()
                    1 -> calculate3x3Inverse()
                    else -> throw IllegalArgumentException("Invalid matrix size")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    private fun calculate2x2Inverse() {
        val k11 = findViewById<EditText>(R.id.et2x2_11).text.toString().toInt()
        val k12 = findViewById<EditText>(R.id.et2x2_12).text.toString().toInt()
        val k21 = findViewById<EditText>(R.id.et2x2_21).text.toString().toInt()
        val k22 = findViewById<EditText>(R.id.et2x2_22).text.toString().toInt()

        val keyMatrix = arrayOf(
            intArrayOf(k11, k12),
            intArrayOf(k21, k22)
        )

        try {
            val inverseMatrix = calculate2x2InverseMatrix(keyMatrix)
            display2x2Result(inverseMatrix)
        } catch (e: Exception) {
            showError("Matrix is not invertible! Determinant is zero.")
        }
    }

    private fun calculate3x3Inverse() {
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

        try {
            val inverseMatrix = calculate3x3InverseMatrix(keyMatrix)
            display3x3Result(inverseMatrix)
        } catch (e: Exception) {
            showError("Matrix is not invertible! Determinant is zero.")
        }
    }

    private fun calculate2x2InverseMatrix(matrix: Array<IntArray>): Array<IntArray> {
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

    private fun calculate3x3InverseMatrix(matrix: Array<IntArray>): Array<IntArray> {
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

    private fun display2x2Result(matrix: Array<IntArray>) {
        tvResultTitle.visibility = TextView.VISIBLE
        result2x2.visibility = LinearLayout.VISIBLE

        findViewById<TextView>(R.id.tv2x2_11).text = matrix[0][0].toString()
        findViewById<TextView>(R.id.tv2x2_12).text = matrix[0][1].toString()
        findViewById<TextView>(R.id.tv2x2_21).text = matrix[1][0].toString()
        findViewById<TextView>(R.id.tv2x2_22).text = matrix[1][1].toString()
    }

    private fun display3x3Result(matrix: Array<IntArray>) {
        tvResultTitle.visibility = TextView.VISIBLE
        result3x3.visibility = LinearLayout.VISIBLE

        findViewById<TextView>(R.id.tv3x3_11).text = matrix[0][0].toString()
        findViewById<TextView>(R.id.tv3x3_12).text = matrix[0][1].toString()
        findViewById<TextView>(R.id.tv3x3_13).text = matrix[0][2].toString()
        findViewById<TextView>(R.id.tv3x3_21).text = matrix[1][0].toString()
        findViewById<TextView>(R.id.tv3x3_22).text = matrix[1][1].toString()
        findViewById<TextView>(R.id.tv3x3_23).text = matrix[1][2].toString()
        findViewById<TextView>(R.id.tv3x3_31).text = matrix[2][0].toString()
        findViewById<TextView>(R.id.tv3x3_32).text = matrix[2][1].toString()
        findViewById<TextView>(R.id.tv3x3_33).text = matrix[2][2].toString()
    }

    private fun showError(message: String) {
        tvMatrixWarning.text = "⚠️ $message"
        tvMatrixWarning.visibility = TextView.VISIBLE
    }

    private fun resetResults() {
        tvMatrixWarning.visibility = TextView.GONE
        tvResultTitle.visibility = TextView.GONE
        result2x2.visibility = LinearLayout.GONE
        result3x3.visibility = LinearLayout.GONE
    }
}