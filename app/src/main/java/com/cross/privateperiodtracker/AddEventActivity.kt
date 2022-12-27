package com.cross.privateperiodtracker

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.TemporalField

class AddEventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_event)

        val now = LocalDateTime.now()

        val time = findViewById<TextView>(R.id.editTextTime)
        fun updateTime(hour: Int, minute: Int) {
            time.text = LocalTime.of(hour, minute).toString()
        }

        fun parseTime(): LocalTime {
            return LocalTime.parse(time.text)
        }
        updateTime(now.hour, now.minute)
        time.setOnClickListener {
            run {
                val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    updateTime(hourOfDay, minute)
                }
                TimePickerDialog(
                    time.context,
                    listener,
                    now.hour,
                    now.minute,
                    false
                ).show()
            }
        }

        val calendar = findViewById<CalendarView>(R.id.calendarView2)
        calendar.date = now.toEpochSecond(ZoneOffset.UTC) * 1000
        calendar.setOnDateChangeListener { calView: CalendarView, year: Int, month: Int, dayOfMonth: Int ->
            val localdate: LocalDateTime = LocalDateTime.of(year, month+1, dayOfMonth, 0, 0)
            calView.setDate(localdate.toEpochSecond(ZoneOffset.UTC) * 1000, false, false)
        }

        val radiobuttons = findViewById<RadioGroup>(R.id.radioGroup)
        radiobuttons.check(R.id.radioPeriodStart)

        val save = findViewById<Button>(R.id.addEvent)
        save.setOnClickListener {
            run {
                val date = LocalDateTime.ofEpochSecond(calendar.date / 1000, 0, ZoneOffset.UTC)
                    .toLocalDate()

                val datetime = date.atTime(parseTime())

                val event = when (radiobuttons.checkedRadioButtonId) {
                    R.id.radioPeriodStart -> PeriodEvent(datetime, EventType.PeriodStart)
                    R.id.radioPeriodStop -> PeriodEvent(datetime, EventType.PeriodEnd)
                    R.id.radioPainkiller -> PeriodEvent(datetime, EventType.Painkiller)
                    R.id.radioTampon -> PeriodEvent(datetime, EventType.TamponStart)
                    R.id.radioPregnantStart -> PeriodEvent(datetime, EventType.PregnancyStart)
                    R.id.radioPregnantStop -> PeriodEvent(datetime, EventType.PregnancyEnd)
                    else -> {
                        null
                    }
                }

                val data = Intent()
                data.putExtra(eventKey, event)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }

    }
}