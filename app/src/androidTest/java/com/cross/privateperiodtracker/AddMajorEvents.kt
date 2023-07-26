package com.cross.privateperiodtracker


import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor
import com.cross.privateperiodtracker.lib.listFiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    val composeTestRule = createAndroidComposeRule<EntryActivity>()

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

        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        SettingsManager.setAutoEndPeriod(prefs, false)
        SettingsManager.setAutoEndPeriodDays(prefs, 0)

        val k = Intent(context, EntryActivity::class.java)
        k.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, k, null)

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        composeTestRule.onNodeWithTag("password").performClick().performTextInput("abc")
        composeTestRule.onNodeWithTag("duress").performClick().performTextInput("123")
        composeTestRule.onNodeWithTag("save")
            .assertTextContains(resources.getString(R.string.save))
            .performClick()
        composeTestRule.waitForIdle()

        val filesDir = InstrumentationRegistry.getInstrumentation().targetContext.filesDir
        val files = listFiles(filesDir).asSequence().toList()
        assert(files.size == 2)

        val dm = DataManager(
            InstrumentationRegistry.getInstrumentation().targetContext,
            Encryptor("abc")
        )
        dm.loadData()
        assertEquals(0, dm.data.events.size)

        composeTestRule.onNodeWithTag("password").performClick().performTextInput("abc")
        composeTestRule.onNodeWithTag("login")
            .assertTextContains(resources.getString(R.string.login))
            .performClick()
        composeTestRule.waitForIdle()

        for (event in listOf(
            resources.getString(R.string.tampon_start),
            resources.getString(R.string.tampon_stop),
            resources.getString(R.string.painkiller),
            resources.getString(R.string.period_start),
            resources.getString(R.string.period_end),
            resources.getString(R.string.pregnancy_start),
            resources.getString(R.string.pregnancy_stop)
        )) {
            composeTestRule.onNodeWithTag("addevent")
                .assertTextContains(resources.getString(R.string.add_event))
                .performScrollTo()
                .performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("eventtime").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("OK").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("notes")
                .performScrollTo()
                .performClick()
                .performTextInput("Event:\n\t${event}")
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(event).performScrollTo().performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("saveevent")
                .assertTextContains(resources.getString(R.string.save_event))
                .performScrollTo()
                .performClick()
            composeTestRule.waitForIdle()
        }

        dm.loadData()
        for (eventtype in EventType.values()) {
            var foundEvent = false
            for (event in dm.data.events) {
                if (event.type == eventtype) {
                    foundEvent = true
                    break
                }
                assertTrue(event.notes.isNotEmpty())
            }
            assertTrue("Found $eventtype", foundEvent)
        }

        assertEquals(EventType.values().size, dm.data.events.size)
    }
}
