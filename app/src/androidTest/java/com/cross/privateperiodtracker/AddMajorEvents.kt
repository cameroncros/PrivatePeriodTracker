package com.cross.privateperiodtracker


import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.cross.privateperiodtracker.lib.listFiles
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AddMajorEvents {
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
        ActivityScenario.launch(EntryActivity::class.java)

        val appCompatEditText = onView(
            allOf(
                withId(R.id.realPassword),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("abc"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.duressPassword),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("123"), closeSoftKeyboard())

        val materialButton = onView(
            allOf(
                withId(R.id.save), withText("Save"),
                isDisplayed()
            )
        )
        materialButton.perform(click())

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.textPassword),
                isDisplayed()
            )
        )
        appCompatEditText3.perform(replaceText("abc"), closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.button4), withText("Login"),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        val materialButton3 = onView(
            allOf(
                withId(R.id.addEvent), withText(R.string.add_event),
                isDisplayed()
            )
        )
        materialButton3.perform(click())

        val materialTextView = onView(
            allOf(
                withId(R.id.editTextTime),
                isDisplayed()
            )
        )
        materialTextView.perform(click())

        val materialButton4 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
            )
        )
        materialButton4.perform(scrollTo(), click())

        val materialButton5 = onView(
            allOf(
                withId(R.id.saveEvent), withText("Save Event"),
                isDisplayed()
            )
        )
        materialButton5.perform(click())

        val materialButton6 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                isDisplayed()
            )
        )
        materialButton6.perform(click())

        val materialTextView2 = onView(
            allOf(
                withId(R.id.editTextTime),
                isDisplayed()
            )
        )
        materialTextView2.perform(click())

        val materialButton7 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
            )
        )
        materialButton7.perform(scrollTo(), click())

        val materialRadioButton = onView(
            allOf(
                withId(R.id.radioPeriodStop), withText("Period End"),
            )
        )
        materialRadioButton.perform(scrollTo(), click())

        val materialButton8 = onView(
            allOf(
                withId(R.id.saveEvent), withText("Save Event"),
                isDisplayed()
            )
        )
        materialButton8.perform(click())

        val materialButton9 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                isDisplayed()
            )
        )
        materialButton9.perform(click())

        val materialRadioButton2 = onView(
            allOf(
                withId(R.id.radioPainkiller), withText("Painkiller"),
            )
        )
        materialRadioButton2.perform(scrollTo(), click())

        val materialButton10 = onView(
            allOf(
                withId(R.id.saveEvent), withText("Save Event"),
                isDisplayed()
            )
        )
        materialButton10.perform(click())

        val materialButton11 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                isDisplayed()
            )
        )
        materialButton11.perform(click())

        val materialRadioButton3 = onView(
            allOf(
                withId(R.id.radioTampon), withText("Tampon"),
            )
        )
        materialRadioButton3.perform(scrollTo(), click())

        val materialButton12 = onView(
            allOf(
                withId(R.id.saveEvent), withText("Save Event"),
                isDisplayed()
            )
        )
        materialButton12.perform(click())

        val materialButton13 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                isDisplayed()
            )
        )
        materialButton13.perform(click())
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
