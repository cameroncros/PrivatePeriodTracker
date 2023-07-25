package com.cross.privateperiodtracker


import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @get:Rule
    var composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun checkPrivacyPolicy() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        Intents.init()
        val expectedIntent: Matcher<Intent> =
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(resources.getString(R.string.priv_policy_url))
            )
        Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        composeTestRule.onNodeWithText(resources.getString(R.string.privacy_policy)).performClick()

        Intents.intended(expectedIntent)
        Intents.release()
    }
}
