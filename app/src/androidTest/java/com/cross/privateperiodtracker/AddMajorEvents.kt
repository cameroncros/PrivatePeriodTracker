package com.cross.privateperiodtracker


import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.cross.privateperiodtracker.lib.listFiles
import com.cross.privateperiodtracker.utils.Utils.Companion.performActionCount
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class AddMajorEvents {
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

    @get:Rule
    val composeTestRule = createAndroidComposeRule<CreatePasswordActivity>()

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
        composeTestRule.onNodeWithTag("password").performClick().performTextInput("abc")
        composeTestRule.onNodeWithTag("duress").performClick().performTextInput("123")
        composeTestRule.onNodeWithText(resources.getString(R.string.save)).performClick()
        
        composeTestRule.onNodeWithTag("password").performClick().performTextInput("abc")
        composeTestRule.onNodeWithText(resources.getString(R.string.login)).performClick()

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
