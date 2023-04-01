package com.cross.privateperiodtracker

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import androidx.preference.PreferenceManager
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.ServiceTestRule
import androidx.test.uiautomator.UiDevice
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneOffset


@LargeTest
@RunWith(JUnitParamsRunner::class)
class StickyServiceTest {
    @Rule
    @JvmField
    var grantPermissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= 33) {
            GrantPermissionRule.grant(POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @JvmField
    @Rule
    val mServiceRule: ServiceTestRule = ServiceTestRule()

    @Test
    @Parameters(
        value = [
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7"
        ]
    )
    fun stickyServiceNotificationTest(day: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(
                "android.permission.POST_NOTIFICATIONS"
            )
        }
        val context = getInstrumentation().targetContext.applicationContext
        NotificationReceiver.setupNotificationChannels(context)

        // Enable notification
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
        assertFalse(findNotification(context, manager))
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        // Configure notifications
        val prefKey = day.toString() + "days"
        val dateKey = day.toString() + "date"
        val notificationTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + 30
        prefs.edit().putBoolean(prefKey, true).putLong(dateKey, notificationTime).apply()

        // Start sticky service
        startForegroundService(context, Intent(context, StickyService::class.java))

        val expectedTitle = if (day == 0) {
            context.getString(R.string.period_coming_today)
        } else {
            val format = context.getString(R.string.period_coming_in_d_days)
            String.format(format, day)
        }

        // Notification shouldn't come early.
        Thread.sleep(5000)
        assertFalse(findNotification(context, manager))

        // Turn off phone, notification should still come
        val device: UiDevice = UiDevice.getInstance(getInstrumentation())
        device.pressHome()
        //device.pressKeyCode(KeyEvent.KEYCODE_POWER)

        // Notification should come in the next 2 minutes at most.
        for (i in 0..120) {
            if (findNotification(context, manager, expectedTitle)) {
                //device.pressKeyCode(KeyEvent.KEYCODE_POWER)
                return
            }
            // Wait for notification
            Thread.sleep(1000)
        }

        assertFalse("Failed to find notification", true)
        //device.pressKeyCode(KeyEvent.KEYCODE_POWER)
    }

    private fun findNotification(
        context: Context,
        manager: NotificationManager,
        expectedTitle: String = ""
    ): Boolean {
        val notifications = manager.activeNotifications
        for (notification in notifications) {
            val title = notification.notification.extras.getString("android.title")
            val text = notification.notification.extras.getString("android.text")
            if (title != null &&
                title == expectedTitle &&
                text != null && text == context.getString(R.string.click_to_open_app)
            ) {
                return true
            }
        }
        return false
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
