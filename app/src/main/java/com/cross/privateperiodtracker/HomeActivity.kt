package com.cross.privateperiodtracker

import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cross.privateperiodtracker.data.CurrentState
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodData
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

const val passwordKey: String = "password"
const val dataKey: String = "data"

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val periodData: PeriodData =
            intent.getSerializableExtra(dataKey) as PeriodData;
        val password = intent.getStringExtra(passwordKey);

        val status = findViewById<TextView>(R.id.currentStatus)
        status.text = updateStatus(periodData)

        val stats = findViewById<TextView>(R.id.currentStats)
        stats.text = updateStats(periodData)
    }

    private fun updateStats(periodData: PeriodData): String {
        val cycleStats = periodData.calcAveragePeriodCycle()
        val durationStats = periodData.calcAveragePeriodDuration()
        val sb = StringBuilder()
        sb.append(resources.getString(R.string.your_cycle_is_))
        sb.append(cycleStats.mean.toDays())
        if (cycleStats.variance.toDays() > 1) {
            sb.append("(±")
            sb.append(cycleStats.variance.toDays())
            sb.append(")")
        }
        sb.append(resources.getString(R.string._days_and_your_average_period_is_))
        sb.append(durationStats.mean.toDays())
        if (durationStats.variance.toDays() > 1) {
            sb.append("(±")
            sb.append(durationStats.variance.toDays())
            sb.append(")")
        }
        sb.append(resources.getString(R.string._days))
        return sb.toString()
    }

    private fun updateStatus(periodData: PeriodData): String {
        when (periodData.getCurrentState()) {
            CurrentState.Period -> {
                val endDate = periodData.calcEndOfPeriodDate()
                val delta = Duration.between(endDate, LocalDateTime.now())
                val sb = StringBuilder()
                sb.append(resources.getString(R.string.period_will_end_in_))
                sb.append(delta.toDays())
                sb.append(resources.getString(R.string._days))
                return sb.toString()
            }

            CurrentState.Freedom -> {
                val endDate = periodData.calcNextPeriodDate()
                val delta = Duration.between(endDate, LocalDateTime.now())
                val sb = StringBuilder()
                sb.append(resources.getString(R.string.next_period_in_))
                sb.append(delta.toDays())
                sb.append(resources.getString(R.string._days))
                return sb.toString()
            }

            CurrentState.Pregnant -> {
                val startDate = periodData.getPregnancyStart()
                val delta = Duration.between(startDate, LocalDateTime.now())
                val sb = StringBuilder()
                sb.append(resources.getString(R.string.you_have_been_pregnant_for_))
                sb.append(delta.toDays())
                sb.append(resources.getString(R.string._days_congrats))
                return sb.toString()
            }

            CurrentState.Unknown -> {
                return resources.getString(R.string.unknown_state)
            }
        }
    }
}