package com.cross.privateperiodtracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cross.privateperiodtracker.data.generateData
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor


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

            val encryptor = Encryptor(password)
            DataManager(
                this@CreatePasswordActivity.applicationContext,
                encryptor
            ).saveData()

            if (canary.isNotEmpty()) {
                val canaryEncryptor = Encryptor(canary)
                val dataManager = DataManager(
                    this@CreatePasswordActivity.applicationContext,
                    canaryEncryptor
                )
                dataManager.data = generateData()
                dataManager.saveData()
            }
            finish()
        }

        val privPolicy: TextView = findViewById(R.id.privacyPolicyButton)
        privPolicy.setOnClickListener {
            val uri = Uri.parse(getString(R.string.priv_policy_url))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}