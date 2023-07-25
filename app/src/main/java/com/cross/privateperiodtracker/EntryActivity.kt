package com.cross.privateperiodtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cross.privateperiodtracker.lib.listFiles
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme

class EntryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrivatePeriodTrackerTheme {
            }
        }

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