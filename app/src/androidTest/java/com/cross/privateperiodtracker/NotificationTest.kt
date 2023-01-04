package com.cross.privateperiodtracker


import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodEvent
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor
import com.cross.privateperiodtracker.lib.listFiles
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
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
    @JvmField
    @Rule
    val mActivityRule = ActivityTestRule(
        EntryActivity::class.java,
        true,
        false
    )

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
            GrantPermissionRule.grant(
                "android.permission.POST_NOTIFICATIONS"
            )
        }

        // Enable notification
        val context = getInstrumentation().targetContext.applicationContext
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putBoolean("enabled", true)
            .putBoolean(day.toString() + "days", true).apply()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
        assertFalse(findNotification(context, manager))

        // Build data, such that the notification should be sent in the next minute.
        val fm = DataManager(
            getInstrumentation().targetContext,
            Encryptor("abc")
        )
        val periodCycle = Duration.ofDays(28)
        val periodLength = Duration.ofDays(1)
        val notificationPeriod = Duration.ofDays(day.toLong())

        val lastEndDate =
            LocalDateTime.now() + Duration.ofSeconds(10) + notificationPeriod - periodCycle + periodLength
        var date = lastEndDate
        fm.data.addEvent(PeriodEvent(date, EventType.PeriodEnd))
        date -= periodLength
        fm.data.addEvent(PeriodEvent(date, EventType.PeriodStart))
        repeat(10) {
            date -= periodCycle - periodLength
            fm.data.addEvent(PeriodEvent(date, EventType.PeriodEnd))
            date -= periodLength
            fm.data.addEvent(PeriodEvent(date, EventType.PeriodStart))
        }

        assertEquals(28, fm.data.calcAveragePeriodCycle().mean.toDays())
        assertEquals(0, fm.data.calcAveragePeriodCycle().sd.toDays())

        assertThat(
            fm.data.calcNextPeriodDate(),
            greaterThan(LocalDateTime.now() + notificationPeriod)
        )
        fm.saveData()

        // Launch app, and login
        mActivityRule.launchActivity(Intent())
        ActivityScenario.launch(EntryActivity::class.java)

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(700)

        val appCompatEditText4 = onView(
            allOf(
                withId(R.id.textPassword),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatEditText4.perform(replaceText("abc"), closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.button4), withText("Login"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())
        assertFalse(findNotification(context, manager))

        val expectedTitle = if (day == 0) {
            context.getString(R.string.period_coming_today)
        } else {
            val format = context.getString(R.string.period_coming_in_d_days)
            String.format(format, day)
        }

        // Notification shouldnt come early.
        Thread.sleep(5000)
        assertFalse(findNotification(context, manager))

        // Notification should come in the next 2 minutes at most.
        for (i in 0..120) {
            if (findNotification(context, manager, expectedTitle)) {
                return
            }
            // Wait for notification
            Thread.sleep(1000)
        }

        assertFalse("Failed to find notification", true)
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
