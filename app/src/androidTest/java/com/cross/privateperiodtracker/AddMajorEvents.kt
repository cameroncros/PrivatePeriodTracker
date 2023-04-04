package com.cross.privateperiodtracker


import android.Manifest
import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.cross.privateperiodtracker.lib.listFiles
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class AddMajorEvents {
    private fun performActionCount(
        action: () -> Unit,
        maxRepeatTimes: Int = -1
    ) {
        var success = false
        var counter = if (maxRepeatTimes == -1) Int.MIN_VALUE else 0
        while (!success && counter < maxRepeatTimes) {
            success = try {
                counter++
                action()
                true
            } catch (e: NoMatchingViewException) {
                false
            }
        }
    }

    @Rule
    @JvmField
    var grantPermissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= 33) {
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(EntryActivity::class.java)

    @Before
    fun setup() {
        val files = InstrumentationRegistry.getInstrumentation().targetContext.filesDir
        listFiles(files).forEach { file -> file.delete() }
    }

    @Test
    fun addMajorEvents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(
                "android.permission.POST_NOTIFICATIONS"
            )
        }

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.realPassword),
                        isDisplayed()
                    )
                ).perform(replaceText("abc"), closeSoftKeyboard())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.duressPassword),
                        isDisplayed()
                    )
                ).perform(replaceText("123"), closeSoftKeyboard())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.save), withText("Save"),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.textPassword),
                        isDisplayed()
                    )
                ).perform(replaceText("abc"), closeSoftKeyboard())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.button4), withText("Login"),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.addEvent), withText(R.string.add_event),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.editTextTime),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(android.R.id.button1), withText("OK"),
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.saveEvent), withText("Save Event"),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.addEvent),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.editTextTime),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        performActionCount(
            action = {
                onView(
                    allOf(
                        withId(android.R.id.button1), withText("OK"),
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        for (event in listOf(
            resources.getString(R.string.tampon),
            resources.getString(R.string.painkiller),
            resources.getString(R.string.period_start),
            resources.getString(R.string.period_end),
            resources.getString(R.string.pregnancy_start),
            resources.getString(R.string.pregnancy_stop)
        )) {
            performActionCount(
                action = {
                    onView(
                        allOf(
                            withId(R.id.addEvent),
                            isDisplayed()
                        )
                    ).perform(click())
                },
                maxRepeatTimes = 20
            )

            performActionCount(
                action = {
                    onView(
                        allOf(
                            withText(event),
                            isDisplayed()
                        )
                    ).perform(click())
                },
                maxRepeatTimes = 20
            )

            performActionCount(
                action = {
                    onView(
                        allOf(
                            withId(R.id.saveEvent),
                            withText("Save Event"),
                            isDisplayed()
                        )
                    ).perform(click())
                },
                maxRepeatTimes = 20
            )
        }
    }
}
