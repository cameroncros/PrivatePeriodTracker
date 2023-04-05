package com.cross.privateperiodtracker


import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.cross.privateperiodtracker.utils.Utils
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class CreatePasswordTest {
    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(CreatePasswordActivity::class.java)

    @Test
    fun checkPrivacyPolicy() {

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        Intents.init()
        val expectedIntent: Matcher<Intent> =
            allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(resources.getString(R.string.priv_policy_url))
            )
        Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        Utils.performActionCount(
            action = {
                onView(
                    allOf(
                        withId(R.id.privacyPolicyButton),
                        isDisplayed()
                    )
                ).perform(click())
            },
            maxRepeatTimes = 20
        )

        Intents.intended(expectedIntent)
        Intents.release()
    }

}
