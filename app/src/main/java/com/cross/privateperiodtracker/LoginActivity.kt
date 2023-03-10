package com.cross.privateperiodtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val saveButton: Button = findViewById(R.id.button4)
        saveButton.setOnClickListener {
            val passwordText: EditText = findViewById(R.id.textPassword)
            val password = passwordText.text.toString()
            passwordText.setText("")

            val encryptor = Encryptor(password)

            val dm = DataManager(applicationContext, encryptor)
            if (dm.loadData() == null) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.wrong_password),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val k = Intent(this, HomeActivity::class.java)
            k.putExtra(dataKey, dm)
            startActivity(k)
        }
    }
}