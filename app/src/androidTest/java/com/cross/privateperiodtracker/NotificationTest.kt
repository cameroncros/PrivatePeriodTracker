package com.cross.privateperiodtracker

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.UiDevice
import com.cross.privateperiodtracker.lib.listFiles
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.LocalDateTime


@LargeTest
@RunWith(JUnitParamsRunner::class)
class NotificationTest {
    @Rule
    @JvmField
    var grantPermissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= 33) {
            grant(POST_NOTIFICATIONS)
        } else {
            grant()
        }

    @Before
    fun setup() {
        val files = getInstrumentation().targetContext.filesDir
        listFiles(files).forEach { file -> file.delete() }
    }

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
    fun notificationTest(day: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            grant(
                "android.permission.POST_NOTIFICATIONS"
            )
        }
        val context = getInstrumentation().targetContext.applicationContext
        NotificationReceiver.setupNotificationChannels(context)

        // Enable notification
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
        assertFalse(findNotification(context, manager))

        // Build data, such that the notification should be sent in the next minute.
        val dm = generateNotification(context, Duration.ofSeconds(30), day)
        val k = Intent(context, HomeActivity::class.java)
        k.putExtra(dataKey, dm)
        k.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, k, null)

        assertEquals(28, dm.data.calcAveragePeriodCycle().mean.toDays())
        assertEquals(0, dm.data.calcAveragePeriodCycle().sd.toDays())

        assertThat(
            dm.data.calcNextPeriodDate(),
            greaterThan(LocalDateTime.now() + Duration.ofSeconds(5))
        )

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
        device.pressKeyCode(KeyEvent.KEYCODE_POWER)

        // Notification should come in the next 2 minutes at most.
        for (i in 0..120) {
            if (findNotification(context, manager, expectedTitle)) {
                device.pressKeyCode(KeyEvent.KEYCODE_POWER)
                return
            }
            // Wait for notification
            Thread.sleep(1000)
        }

        assertFalse("Failed to find notification", true)
        device.pressKeyCode(KeyEvent.KEYCODE_POWER)
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
