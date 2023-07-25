package com.cross.privateperiodtracker


import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test


class CreatePasswordTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<CreatePasswordActivity>()

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

        composeTestRule.onNodeWithText(resources.getString(R.string.privacy_policy)).performClick()

        Intents.intended(expectedIntent)
        Intents.release()
    }
}
