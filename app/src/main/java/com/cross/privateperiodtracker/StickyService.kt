package com.cross.privateperiodtracker

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class StickyService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationReceiver.setupNotificationChannels(applicationContext)

        // Create notification
        val notificationIntent = Intent(this, EntryActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, DISABLE_ME)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()

        // Register as foreground
        startForeground(1, notification)

        NotificationReceiver.configNotifications(applicationContext)

        return START_STICKY
    }
}