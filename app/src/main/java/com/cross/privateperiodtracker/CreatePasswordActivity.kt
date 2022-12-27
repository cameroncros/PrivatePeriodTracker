package com.cross.privateperiodtracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.generateData
import com.cross.privateperiodtracker.lib.Encryption


class CreatePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_password)

        val saveButton: Button = findViewById(R.id.save)
        saveButton.setOnClickListener {
            val passwordText: EditText = findViewById(R.id.realPassword)
            val password = passwordText.text.toString()
            val canaryText: EditText = findViewById(R.id.duressPassword)
            val canary = canaryText.text.toString()

            Encryption(
                password,
                this@CreatePasswordActivity.applicationContext
            ).saveData()
            if (canary.isNotEmpty()) {
                val encryption = Encryption(
                    canary,
                    this@CreatePasswordActivity.applicationContext
                )
                encryption.data = generateData()
                encryption.saveData()
            }
            finish()
        }
    }
}