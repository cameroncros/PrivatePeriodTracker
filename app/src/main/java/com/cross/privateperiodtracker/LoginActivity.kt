package com.cross.privateperiodtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
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

            val data = Encryption(password, applicationContext).loadData()
            if (data == null)
            {
                passwordText.setText("");
                Toast.makeText(this, resources.getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }

            val k = Intent(this, HomeActivity::class.java)
            k.putExtra(passwordKey, password)
            k.putExtra(dataKey, data)
            startActivity(k);
        }
    }
}