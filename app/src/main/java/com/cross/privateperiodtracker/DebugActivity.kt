package com.cross.privateperiodtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodEvent
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme
import com.cross.privateperiodtracker.theme.Typography
import java.time.Duration
import java.time.LocalDateTime

class DebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrivatePeriodTrackerTheme {
                // A surface container using the 'background' color from the theme
                ToolList(
                )
            }
        }
    }
}

@Composable
fun ToolList() {
    Column {
        NotificationCard("Generate a notification that will go off in 3s", Duration.ofSeconds(3))
        NotificationCard("Generate a notification that will go off in 60s", Duration.ofSeconds(60))
        NotificationCard(
            "Generate a notification that will go off in 3 minutes",
            Duration.ofMinutes(3)
        )
        NotificationCard("Generate a notification that will go off in 1 hour", Duration.ofHours(1))
        NotificationCard("Generate a notification that will go off in 1 day", Duration.ofDays(1))
        NotificationCard("Generate a notification that will go off in 7 days", Duration.ofDays(7))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(s: String, d: Duration) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
        onClick = {
            val dm = generateNotification(context, d)
            val k = Intent(context, HomeActivity::class.java)
            k.putExtra(dataKey, dm)
            startActivity(context, k, null)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = s,
                style = Typography.bodyLarge
            )
        }
    }
}

fun generateNotification(
    context: Context,
    timeout: Duration = Duration.ofSeconds(60),
    notificationDay: Int = 0
): DataManager {
    // Enable notification
    val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    sharedPreferences.edit().putBoolean("enabled", true)
        .putBoolean(notificationDay.toString() + "days", true).apply()

    // Cook data to ensure notification in the next $timeout seconds
    val fm = DataManager(
        context,
        Encryptor("abc")
    )
    val periodCycle = Duration.ofDays(28)
    val periodLength = Duration.ofDays(1)
    val notificationPeriod = Duration.ofDays(notificationDay.toLong())

    val lastEndDate =
        LocalDateTime.now() + timeout + notificationPeriod - periodCycle + periodLength
    var date = lastEndDate
    fm.data.addEvent(PeriodEvent(date, EventType.PeriodEnd))
    date -= periodLength
    fm.data.addEvent(PeriodEvent(date, EventType.PeriodStart))
    repeat(10) {
        date -= periodCycle - periodLength
        fm.data.addEvent(PeriodEvent(date, EventType.PeriodEnd))
        date -= periodLength
        fm.data.addEvent(PeriodEvent(date, EventType.PeriodStart))
    }
    fm.saveData()
    return fm
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrivatePeriodTrackerTheme {
        ToolList()
    }
}