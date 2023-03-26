package com.cross.privateperiodtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cross.privateperiodtracker.lib.listFiles

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!listFiles(applicationContext.filesDir).hasNext()) {
            val k = Intent(this, CreatePasswordActivity::class.java)
            startActivity(k)
        }
    }

    override fun onStart() {
        startForegroundService(Intent(this, StickyService::class.java))

        super.onStart()
        if (listFiles(applicationContext.filesDir).hasNext()) {
            val k = Intent(this, LoginActivity::class.java)
            startActivity(k)
            finish()
        }
    }

    override fun onResume() {
        startForegroundService(Intent(this, StickyService::class.java))

        super.onResume()
        if (listFiles(applicationContext.filesDir).hasNext()) {
            val k = Intent(this, LoginActivity::class.java)
            startActivity(k)
            finish()
        }
    }
}