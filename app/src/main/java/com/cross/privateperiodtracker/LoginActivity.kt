package com.cross.privateperiodtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.lib.Encryption

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val saveButton: Button = findViewById(R.id.button4);
        saveButton.setOnClickListener {
            val passwordText: EditText = findViewById(R.id.textPassword);
            val password = passwordText.text.toString()

            val k = Intent(this, LoginActivity::class.java)
            startActivity(k);
            finish();
        }
    }
}