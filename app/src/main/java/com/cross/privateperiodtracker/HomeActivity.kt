package com.cross.privateperiodtracker

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.cross.privateperiodtracker.data.CurrentState
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodCalculator
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.PeriodEvent
import com.cross.privateperiodtracker.data.generateData
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme
import com.cross.privateperiodtracker.theme.Typography
import com.cross.privateperiodtracker.widget.PeriodCalendar
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


const val dataKey: String = "data"
const val eventKey: String = "event"

class HomeActivity : ComponentActivity() {

    private lateinit var dataManager: DataManager
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        dataManager = intent.getSerializableExtra(dataKey) as DataManager

        startForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val events = result.data?.extras!!.get(eventKey) as Array<*>
                for (event in events) {
                    dataManager.data.addEvent(event as PeriodEvent)
                }
                dataManager.saveData()

                updateNotifications()

                setContent {
                    PrivatePeriodTrackerTheme {
                        Home(
                            periodData = dataManager.data,
                            addFn = {
                                val i = Intent(this, AddEventActivity::class.java)
                                i.putExtra(dataKey, dataManager)
                                startForResult.launch(i)
                            }
                        )
                    }
                }
            }
        }

        setContent {
            PrivatePeriodTrackerTheme {
                Home(
                    periodData = dataManager.data,
                    addFn = {
                        val i = Intent(this, AddEventActivity::class.java)
                        i.putExtra(dataKey, dataManager)
                        startForResult.launch(i)
                    })
            }
        }

        updateNotifications()
    }

    private fun updateNotifications() {
        val prefs = getDefaultSharedPreferences(applicationContext)

        NotificationReceiver.requestSendNotificationsPermission(this)

        val nextPeriodDate = dataManager.data.calculator().calcNextPeriodDate() ?: return

        // Save next times to shared preferences.
        val spedit: SharedPreferences.Editor = prefs.edit()
        for (day in 0..7) {
            val alarmTime = nextPeriodDate.minusDays(day.toLong())
            if (alarmTime < LocalDateTime.now()) {
                continue
            }

            val dateKey = day.toString() + "date"

            spedit.putLong(dateKey, alarmTime.toEpochSecond(ZoneOffset.UTC))
        }
        spedit.apply()

        NotificationReceiver.configNotifications(applicationContext)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(periodData: PeriodData, addFn: () -> Unit) {
    val context = LocalContext.current
    val initialSelectedDay by remember { mutableStateOf(LocalDate.now()) }
    var calculator = periodData.calculator()
    var dayEvents = calculator.getDayEvents(initialSelectedDay)


    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    )
    {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            CurrentStatus(calculator)
            IconButton(
                onClick = {
                    val k = Intent(context, SettingsActivity::class.java)
                    startActivity(context, k, null)
                },
                modifier = Modifier.testTag("settings")
            ) {
                Icon(Icons.Filled.Settings, "settings")
            }
        }
        CycleStats(calculator)
        Text(
            style = Typography.bodySmall,
            text = stringResource(id = R.string.this_is_not_medical_advice_if_you_have_concerns_see_a_medical_professional),
            modifier = Modifier.padding(8.dp)
        )

        PeriodCalendar(
            padding = 16.dp,
            calculator = calculator,
            initialSelectedDay = initialSelectedDay
        ) { day: LocalDate ->
            dayEvents = calculator.getDayEvents(day)
        }

        Row(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 1f, fill = false)
        )
        {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                for (it in dayEvents) {
                    Event(event = it,
                        delFn = { ev ->
                            periodData.removeEvent(ev)
                            calculator = periodData.calculator()
                            dayEvents.remove(ev)
                        })
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(16.dp)
        )
        {
            Button(
                onClick = addFn,
                modifier = Modifier.testTag("addevent")
            ) {
                Icon(Icons.Filled.Add, "add")
                Text(stringResource(id = R.string.add_event))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val periodData = generateData()
    PrivatePeriodTrackerTheme {
        Home(
            periodData = periodData,
            addFn = {})
    }
}

@Composable
fun CycleStats(calculator: PeriodCalculator) {
    val cycle = calculator.calcAveragePeriodCycle()
    val duration = calculator.calcAveragePeriodCycle()
    Text(
        style = Typography.bodySmall,
        text = stringResource(
            id = R.string.your_cycle_is_x_days_and_your_average_period_is_y_days,
            cycle.mean.toDays(),
            duration.mean.toDays()
        ),
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun CurrentStatus(calculator: PeriodCalculator) {
    val status: String = when (calculator.getState()) {
        CurrentState.Period -> {
            var endDate = calculator.calcEndOfPeriodDate()
            if (endDate == null) {
                stringResource(R.string.need_more_data)
            } else {
                val cycle = calculator.calcAveragePeriodCycle()
                while (endDate < LocalDateTime.now()) {
                    endDate += cycle.mean
                }
                val delta = Duration.between(LocalDateTime.now(), endDate)
                stringResource(R.string.period_ending_in_N_days, delta.toDays())
            }
        }

        CurrentState.Freedom -> {
            val endDate = calculator.calcNextPeriodDate()
            if (endDate == null) {
                stringResource(R.string.need_more_data)
            } else {
                val delta = Duration.between(LocalDateTime.now(), endDate)
                stringResource(R.string.period_coming_in_N_days, delta.toDays())
            }
        }

        CurrentState.Pregnant -> {
            val startDate = calculator.getPregnancyStart()
            if (startDate == null) {
                stringResource(R.string.need_more_data)
            } else {
                val delta = Duration.between(startDate, LocalDateTime.now())
                stringResource(R.string.you_have_been_pregnant_for_N_days, delta.toDays())
            }
        }

        CurrentState.Unknown -> {
            stringResource(R.string.unknown_state)
        }
    }
    Text(text = status, modifier = Modifier.padding(8.dp))
}

@Composable
fun Event(event: PeriodEvent, delFn: (event: PeriodEvent) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(1f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            Row {
                var event_label = ""
                when (event.type) {
                    EventType.PeriodStart -> {
                        Image(
                            painterResource(id = R.drawable.icon_period_start),
                            "period start"
                        )
                        event_label = stringResource(id = R.string.period_start)
                    }

                    EventType.PeriodEnd -> {
                        Image(
                            painterResource(id = R.drawable.icon_period_stop),
                            "period stop"
                        )
                        event_label = stringResource(id = R.string.period_end)
                    }

                    EventType.PregnancyStart -> {
                        Image(
                            painterResource(id = R.drawable.icon_pregnancy_start),
                            "pregnancy start"
                        )
                        event_label = stringResource(id = R.string.pregnancy_start)
                    }

                    EventType.PregnancyEnd -> {
                        Image(
                            painterResource(id = R.drawable.icon_pregnancy_stop),
                            "pregnancy stop"
                        )
                        event_label = stringResource(id = R.string.pregnancy_stop)
                    }

                    EventType.TamponStart -> {
                        Image(
                            painterResource(id = R.drawable.icon_tampon_start),
                            "tampon start"
                        )
                        event_label = stringResource(id = R.string.tampon_start)
                    }

                    EventType.TamponEnd -> {
                        Image(
                            painterResource(id = R.drawable.icon_tampon_stop),
                            "tampon stop"
                        )
                        event_label = stringResource(id = R.string.tampon_stop)
                    }

                    EventType.Painkiller -> {
                        Image(
                            painterResource(id = R.drawable.icon_painkiller),
                            "painkiller"
                        )
                        event_label = stringResource(id = R.string.painkiller)
                    }
                }
                event_label += " - " + event.time.format(DateTimeFormatter.ofPattern("hh:mm a"))
                Text(event_label)
            }
            IconButton(onClick = { delFn(event) }) {
                Icon(Icons.Filled.Delete, "delete")
            }
        }
        if (event.notes.isNotEmpty()) {
            Row {
                OutlinedTextField(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(1.0f),
                    value = event.notes,
                    readOnly = true,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.notes)) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventPreview() {
    val event = PeriodEvent(LocalDateTime.now(), EventType.PeriodStart, "Heavy\n\tWoo! not pregs")
    PrivatePeriodTrackerTheme {
        Event(event = event, delFn = {})
    }
}
