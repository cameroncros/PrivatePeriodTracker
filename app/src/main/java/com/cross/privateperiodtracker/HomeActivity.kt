package com.cross.privateperiodtracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.cross.privateperiodtracker.data.CurrentState
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.PeriodEvent
import com.cross.privateperiodtracker.lib.Encryption
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.Duration
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

const val dataKey: String = "data"
const val eventKey: String = "event"

class HomeActivity : AppCompatActivity() {
    private lateinit var encryption: Encryption
    private lateinit var periodData: PeriodData
    private lateinit var status: TextView
    private lateinit var stats: TextView
    private lateinit var calendarView : com.kizitonwose.calendar.view.CalendarView

    fun update(periodData: PeriodData) {
        status.text = updateStatus(periodData)
        stats.text = updateStats(periodData)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            val icon = view.findViewById<ImageView>(R.id.calendarDayIcon)
        }

        calendarView.dayBinder =
            object : MonthDayBinder<DayViewContainer> {
                // Called only when a new container is needed.
                override fun create(view: View) = DayViewContainer(view)

                // Called every time we need to reuse a container.
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.textView.text = data.date.dayOfMonth.toString()
                    val events = periodData.getDayEvents(data.date)
                    if (events.size > 0) {
                        container.icon.visibility = View.VISIBLE
                        when (events[0].type) {
                            EventType.PeriodStart -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.baseline_bloodtype_24))
                            }

                            EventType.PeriodEnd -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.baseline_check_circle_24))
                            }

                            else -> {
                                container.icon.visibility = View.INVISIBLE
                            }
                        }
                    } else {
                        container.icon.visibility = View.INVISIBLE
                    }
                }
            }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            // Alternatively, you can add an ID to the container layout and use findViewById()
            val titles = view as ViewGroup
        }

        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val daysOfWeek = daysOfWeek(firstDayOfWeek)
        calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.titles.findViewById<TextView>(R.id.currentMonthText).text =
                        data.yearMonth.format(
                            DateTimeFormatter.ofPattern("yyyy MMM")
                        )
                    container.titles.findViewById<ImageButton>(R.id.prevMonthButton)
                        .setOnClickListener {
                            val prevMonth = data.yearMonth.minusMonths(1)
                            calendarView.smoothScrollToMonth(prevMonth)
                        }
                    container.titles.findViewById<ImageButton>(R.id.nextMonthButton)
                        .setOnClickListener {
                            val nextMonth = data.yearMonth.plusMonths(1)
                            calendarView.smoothScrollToMonth(nextMonth)
                        }

                    if (container.titles.tag == null) {
                        container.titles.tag = data.yearMonth
                        container.titles.findViewById<LinearLayout>(R.id.dayHeader).children.map { it as TextView }
                            .forEachIndexed { index, textView ->
                                val dayOfWeek = daysOfWeek[index]
                                val title =
                                    dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                textView.text = title
                            }
                    }
                }
            }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(30)
        val endMonth = currentMonth.plusYears(30)
        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        encryption =
            intent.getSerializableExtra(dataKey) as Encryption;
        val periodData = encryption.data!!;

        status = findViewById(R.id.currentStatus)
        stats = findViewById(R.id.currentStats)
        calendarView = findViewById(R.id.calendarView);

        val intentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val event: PeriodEvent =
                        result.data?.getSerializableExtra(eventKey) as PeriodEvent
                    periodData.addEvent(event)
                    encryption.saveData(periodData)
                    update(periodData)
                }
            }
        val addEventButton = findViewById<Button>(R.id.addEvent);
        addEventButton.setOnClickListener {
            val k = Intent(this, AddEventActivity::class.java)
            intentLauncher.launch(k);
        }

        update(periodData)
    }

    private fun updateStats(periodData: PeriodData): String {
        val cycleStats = periodData.calcAveragePeriodCycle()
        val durationStats = periodData.calcAveragePeriodDuration()
        val sb = StringBuilder()
        sb.append(resources.getString(R.string.your_cycle_is_))
        sb.append(cycleStats.mean.toDays())
        if (cycleStats.sd.toDays() > 1) {
            sb.append("(±")
            sb.append(cycleStats.sd.toDays())
            sb.append(")")
        }
        sb.append(resources.getString(R.string._days_and_your_average_period_is_))
        sb.append(durationStats.mean.toDays())
        if (durationStats.sd.toDays() > 1) {
            sb.append("(±")
            sb.append(durationStats.sd.toDays())
            sb.append(")")
        }
        sb.append(resources.getString(R.string._days))
        return sb.toString()
    }

    private fun updateStatus(periodData: PeriodData): String {
        when (periodData.getState()) {
            CurrentState.Period -> {
                val endDate = periodData.calcEndOfPeriodDate()
                val delta = Duration.between(LocalDateTime.now(), endDate)
                val sb = StringBuilder()
                sb.append(resources.getString(R.string.period_will_end_in_))
                sb.append(delta.toDays())
                sb.append(resources.getString(R.string._days))
                return sb.toString()
            }

            CurrentState.Freedom -> {
                val endDate = periodData.calcNextPeriodDate()
                val delta = Duration.between(LocalDateTime.now(), endDate)
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