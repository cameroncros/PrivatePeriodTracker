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
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodEvent
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor
import com.cross.privateperiodtracker.lib.listFiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class AddEvents {
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
    fun autoEndPeriod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(
                "android.permission.POST_NOTIFICATIONS"
            )
        }

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val days = Random.nextLong(30)

        SettingsManager.setAutoEndPeriod(prefs, true)
        SettingsManager.setAutoEndPeriodDays(prefs, days)

        val dm = DataManager(context = context, encryptor = Encryptor("abc"))

        val k = Intent(context, HomeActivity::class.java)
        k.putExtra(dataKey, dm)
        k.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, k, null)

        composeTestRule.onNodeWithTag("addevent")
            .assertTextContains(resources.getString(R.string.add_event))
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(resources.getString(R.string.period_start)).performClick()

        composeTestRule.onNodeWithTag("saveevent")
            .assertTextContains(resources.getString(R.string.save_event))
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        val filesDir = InstrumentationRegistry.getInstrumentation().targetContext.filesDir
        val files = listFiles(filesDir).asSequence().toList()
        assert(files.size == 1)

        dm.loadData()

        assertEquals(2, dm.data.events.size)

        var startEvent: PeriodEvent? = null
        var endEvent: PeriodEvent? = null
        for (event in dm.data.events) {
            if (event.type == EventType.PeriodStart) {
                startEvent = event
            }
            if (event.type == EventType.PeriodEnd) {
                endEvent = event
            }
        }
        assertNotNull("Found PeriodStart", startEvent)
        assertNotNull("Found PeriodStart", endEvent)

        assertEquals(days, ChronoUnit.DAYS.between(startEvent!!.time, endEvent!!.time))
    }
}
