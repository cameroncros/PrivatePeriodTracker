package com.cross.privateperiodtracker

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.content.ContextCompat.startForegroundService
import androidx.preference.PreferenceManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

const val DAYKEY: String = "days"

const val DISABLE_ME: String = "DISABLE_ME"
const val PERIOD_DUE: String = "PeriodDue"
const val NEXT_PERIOD_DUE: String = "cross.privateperiodtracker.NEXT_PERIOD_DUE"

class NotificationReceiver : BroadcastReceiver() {

    private fun sendNotification(context: Context, days: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val startAppIntent = Intent(context, EntryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            startAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title: String = if (days < 0) {
            context.getString(R.string.period_coming_soon)
        } else if (days == 0) {
            context.getString(R.string.period_coming_today)
        } else {
            val format = context.getString(R.string.period_coming_in_N_days)
            String.format(format, days)
        }

        val builder = Notification.Builder(context, PERIOD_DUE)
            .setSmallIcon(R.drawable.icon_notification)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.click_to_open_app))
            .setContentIntent(pendingIntent)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(2, builder.build())
        }
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                startForegroundService(context, Intent(context, StickyService::class.java))
            }

            NEXT_PERIOD_DUE -> {
                val day = intent.getIntExtra(DAYKEY, -1)
                sendNotification(context, day)
            }
        }
    }

    companion object {

        fun requestSendNotificationsPermission(activity: Activity) {
            // Request notification permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        0
                    )
                }
            }
        }

        fun setupNotificationChannels(context: Context) {
            // Foreground service notification
            run {
                val name = DISABLE_ME
                val descriptionText = DISABLE_ME
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(DISABLE_ME, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    getSystemService(
                        context,
                        NotificationManager::class.java
                    ) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            // Period Due in X days notification
            run {
                val name = PERIOD_DUE
                val descriptionText = PERIOD_DUE
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(PERIOD_DUE, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    getSystemService(
                        context,
                        NotificationManager::class.java
                    ) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun configNotifications(context: Context) {
            val manager = getSystemService(context, AlarmManager::class.java)!!
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            registerReceiver(
                context,
                NotificationReceiver(),
                IntentFilter(NEXT_PERIOD_DUE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            val notifications = SettingsManager.checkNotificationEnabled(prefs)

            // Enable notifications
            for (day in 0..7) {
                // Setup notification intent
                val intent = Intent("cross.privateperiodtracker.NEXT_PERIOD_DUE")
                intent.putExtra(DAYKEY, day)
                val pintent = PendingIntent.getBroadcast(
                    context,
                    day,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                // Cancel existing notification
                manager.cancel(pintent)

                // Check if notification should exist
                if (!notifications[0]) {
                    continue
                }

                // Check when the next notification should occur
                val dateKey = day.toString() + "date"
                val alarmEpoch = prefs.getLong(dateKey, 0L)
                val alarmTime = LocalDateTime.ofEpochSecond(
                    alarmEpoch,
                    0,
                    ZoneOffset.UTC
                )
                if (alarmTime < LocalDateTime.now()) {
                    continue
                }

                // Schedule notification
                val nextPeriod = Duration.between(LocalDateTime.now(), alarmTime)
                if (BuildConfig.DEBUG) {
                    Toast.makeText(
                        context,
                        "Sending notification in: $nextPeriod",
                        Toast.LENGTH_LONG
                    ).show()
                }
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + nextPeriod.toMillis(),
                    pintent
                )
            }
        }
    }
}