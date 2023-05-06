package com.cross.privateperiodtracker


import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import java.lang.Thread.sleep

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

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        composeTestRule.onNodeWithTag("password").performClick().performTextInput("abc")
        composeTestRule.onNodeWithTag("duress").performClick().performTextInput("123")
        composeTestRule.onNodeWithTag("save")
            .assertTextContains(resources.getString(R.string.save))
            .performClick()

        composeTestRule.onNodeWithTag("password").performClick().performTextInput("abc")
        composeTestRule.onNodeWithTag("login")
            .assertTextContains(resources.getString(R.string.login))
            .performClick()

        sleep(5000)

        composeTestRule.onNodeWithTag("addevent")
            .assertTextContains(resources.getString(R.string.add_event))
            .performClick()

        composeTestRule.onNodeWithTag("eventtime")
            .performClick()

        composeTestRule.onNodeWithText("OK").performClick()

        composeTestRule.onNodeWithTag("saveevent")
            .assertTextContains(resources.getString(R.string.save_event))
            .performClick()

        composeTestRule.onNodeWithTag("addevent")
            .assertTextContains(resources.getString(R.string.add_event))
            .performClick()

        composeTestRule.onNodeWithTag("eventtime").performClick()

        composeTestRule.onNodeWithText("OK").performClick()

        composeTestRule.onNodeWithTag("saveevent")
            .assertTextContains(resources.getString(R.string.save_event))
            .performClick()
        sleep(1000)

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
                .performClick()

            composeTestRule.onNodeWithTag("eventtime").performClick()

            composeTestRule.onNodeWithText("OK").performClick()

            composeTestRule.onNodeWithText(event).performClick()

            composeTestRule.onNodeWithTag("saveevent")
                .assertTextContains(resources.getString(R.string.save_event))
                .performClick()
            sleep(1000)
        }

        sleep(10000)

        val filesDir = InstrumentationRegistry.getInstrumentation().targetContext.filesDir
        val files = listFiles(filesDir).asSequence().toList()
        assert(files.size == 2)

        val dm = DataManager(
            InstrumentationRegistry.getInstrumentation().targetContext,
            Encryptor("abc")
        )
        dm.loadData()

        for (eventtype in EventType.values()) {
            var foundEvent = false
            for (event in dm.data.events) {
                if (event.type == eventtype) {
                    foundEvent = true
                    break
                }
            }
            assertTrue("Found $eventtype", foundEvent)
        }

        assertEquals(9, dm.data.events.size)
    }
}
