package com.cross.privateperiodtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cross.privateperiodtracker.lib.listFiles

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!listFiles(applicationContext).hasNext()) {
            val k = Intent(this, CreatePasswordActivity::class.java)
            startActivity(k)
        }
    }

    override fun onStart()
    {
        super.onStart()
        if (listFiles(applicationContext).hasNext()) {
            val k = Intent(this, LoginActivity::class.java)
            startActivity(k)
            finish()
        }
    }

    override fun onResume()
    {
        super.onResume()
        if (listFiles(applicationContext).hasNext()) {
            val k = Intent(this, LoginActivity::class.java)
            startActivity(k)
            finish()
        }
    }
}