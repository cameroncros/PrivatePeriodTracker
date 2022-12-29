package com.cross.privateperiodtracker


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
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
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

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.POST_NOTIFICATIONS"
        )

    @Test
    fun addMajorEvents() {
        ActivityScenario.launch(EntryActivity::class.java)
        val appCompatEditText = onView(
            allOf(
                withId(R.id.realPassword),
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
        appCompatEditText.perform(replaceText("abc"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.duressPassword),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("123"), closeSoftKeyboard())

        val materialButton = onView(
            allOf(
                withId(R.id.save), withText("Save"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    5
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())

        val appCompatEditText3 = onView(
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
        appCompatEditText3.perform(replaceText("abc"), closeSoftKeyboard())

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

        val materialButton3 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton3.perform(click())

        val materialTextView = onView(
            allOf(
                withId(R.id.editTextTime),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialTextView.perform(click())

        val materialButton4 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    3
                )
            )
        )
        materialButton4.perform(scrollTo(), click())

        val materialButton5 = onView(
            allOf(
                withId(R.id.addEvent), withText("Save Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton5.perform(click())

        val materialButton6 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton6.perform(click())

        val materialTextView2 = onView(
            allOf(
                withId(R.id.editTextTime),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialTextView2.perform(click())

        val materialButton7 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    3
                )
            )
        )
        materialButton7.perform(scrollTo(), click())

        val materialRadioButton = onView(
            allOf(
                withId(R.id.radioPeriodStop), withText("Period End"),
                childAtPosition(
                    allOf(
                        withId(R.id.radioGroup),
                        childAtPosition(
                            withClassName(`is`("android.widget.HorizontalScrollView")),
                            0
                        )
                    ),
                    1
                )
            )
        )
        materialRadioButton.perform(scrollTo(), click())

        val materialButton8 = onView(
            allOf(
                withId(R.id.addEvent), withText("Save Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton8.perform(click())

        val materialButton9 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton9.perform(click())

        val materialRadioButton2 = onView(
            allOf(
                withId(R.id.radioPainkiller), withText("Painkiller"),
                childAtPosition(
                    allOf(
                        withId(R.id.radioGroup),
                        childAtPosition(
                            withClassName(`is`("android.widget.HorizontalScrollView")),
                            0
                        )
                    ),
                    2
                )
            )
        )
        materialRadioButton2.perform(scrollTo(), click())

        val materialButton10 = onView(
            allOf(
                withId(R.id.addEvent), withText("Save Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton10.perform(click())

        val constraintLayout = onView(
            allOf(
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        3
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        constraintLayout.perform(click())

        val materialButton11 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton11.perform(click())

        val materialRadioButton3 = onView(
            allOf(
                withId(R.id.radioTampon), withText("Tampon"),
                childAtPosition(
                    allOf(
                        withId(R.id.radioGroup),
                        childAtPosition(
                            withClassName(`is`("android.widget.HorizontalScrollView")),
                            0
                        )
                    ),
                    3
                )
            )
        )
        materialRadioButton3.perform(scrollTo(), click())

        val materialButton12 = onView(
            allOf(
                withId(R.id.addEvent), withText("Save Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton12.perform(click())

        val materialButton13 = onView(
            allOf(
                withId(R.id.addEvent), withText("Add Event"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
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
