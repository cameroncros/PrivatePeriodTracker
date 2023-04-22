package com.cross.privateperiodtracker


import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.cross.privateperiodtracker.lib.listFiles
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class CanaryTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<CreatePasswordActivity>()

    @Before
    fun setup() {
        val files = InstrumentationRegistry.getInstrumentation().targetContext.filesDir
        listFiles(files).forEach { file -> file.delete() }
    }

    @Test
    fun canaryTest() {
        ActivityScenario.launch(EntryActivity::class.java)
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        composeTestRule.onNodeWithTag("password").performClick().performTextInput("abc")
        composeTestRule.onNodeWithTag("duress").performClick().performTextInput("123")
        composeTestRule.onNodeWithText(resources.getString(R.string.save)).performClick()

        composeTestRule.onNodeWithTag("password").performClick().performTextInput("123")
        composeTestRule.onNodeWithText(resources.getString(R.string.login)).performClick()

        //TODO: Validation?
    }
}
