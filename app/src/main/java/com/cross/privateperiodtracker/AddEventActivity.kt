package com.cross.privateperiodtracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cross.privateperiodtracker.data.CurrentState
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.PeriodEvent
import com.cross.privateperiodtracker.data.generateData
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme
import com.cross.privateperiodtracker.theme.Typography
import com.cross.privateperiodtracker.widget.PeriodCalendar
import com.cross.privateperiodtracker.widget.TimePickerDialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AddEventActivity : ComponentActivity() {
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        dataManager = intent.getSerializableExtra(dataKey) as DataManager

        setContent {
            PrivatePeriodTrackerTheme {
                AddEvent(
                    periodData = dataManager.data,
                    addFn = { event: PeriodEvent ->
                        val data = Intent()
                        data.putExtra(eventKey, event)
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEvent(
    periodData: PeriodData,
    addFn: (event: PeriodEvent) -> Unit
) {
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    val time by remember { mutableStateOf(LocalDateTime.now()) }
    var selectedEvent by remember {
        mutableStateOf(
            when (periodData.calculator().getState(selectedDay)) {
                CurrentState.Freedom -> R.string.period_start
                CurrentState.Period -> R.string.period_end
                CurrentState.Pregnant -> R.string.pregnancy_stop
                CurrentState.Unknown -> R.string.period_start
            }
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }
    val allEventTypes = listOf(
        R.string.period_start,
        R.string.period_end,
        R.string.painkiller,
        R.string.pregnancy_start,
        R.string.pregnancy_stop,
        R.string.tampon_start,
        R.string.tampon_stop
    )
    Column {
        Row {
            PeriodCalendar(
                calculator = periodData.calculator(),
                selectedDay = selectedDay,
                daySelectedFn = { day: LocalDate -> selectedDay = day })
        }
        Row(
            Modifier
                .fillMaxWidth(1f)
                .clickable { showTimePicker = true }
                .testTag("eventtime"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.AccessTime,
                "",
                modifier = Modifier.clickable { showTimePicker = true })
            Text(
                style = Typography.displayLarge,
                text = time.format(DateTimeFormatter.ofPattern("hh:mm a")),
            )
            Icon(
                Icons.Filled.Edit,
                "edit time",
                modifier = Modifier.clickable { showTimePicker = true })
            if (showTimePicker) {
                TimePickerDialog(
                    onConfirm = { nt: LocalTime ->
                        time.withHour(nt.hour).withMinute(nt.minute)
                        showTimePicker = false
                    },
                    onCancel = { showTimePicker = false },
                )
            }
        }
        FlowRow(Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.Center) {
            allEventTypes.forEach { id: Int ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { selectedEvent = id }) {
                    RadioButton(selected = selectedEvent == id, onClick = { selectedEvent = id })
                    Text(
                        text = stringResource(id = id),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(1f), horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    val eventType = when (selectedEvent) {
                        R.string.period_start -> EventType.PeriodStart
                        R.string.period_end -> EventType.PeriodEnd
                        R.string.painkiller -> EventType.Painkiller
                        R.string.pregnancy_start -> EventType.PregnancyStart
                        R.string.pregnancy_stop -> EventType.PregnancyEnd
                        R.string.tampon_start -> EventType.TamponStart
                        R.string.tampon_stop -> EventType.TamponEnd
                        else -> EventType.PeriodStart
                    }
                    val eventTime = selectedDay.atTime(time.toLocalTime())
                    val event = PeriodEvent(eventTime, eventType)
                    addFn(event)
                },
                modifier = Modifier.testTag("saveevent")
            ) {
                Image(Icons.Filled.Save, "save")
                Text(stringResource(id = R.string.save_event))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventPreview() {
    PrivatePeriodTrackerTheme {
        AddEvent(
            periodData = generateData(),
            addFn = { _: PeriodEvent -> })
    }
}
