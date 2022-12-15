package com.cross.privateperiodtracker

import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.cross.privateperiodtracker.data.CurrentState
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodData
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

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            val icon = view.findViewById<ImageView>(R.id.calendarDayIcon)
        }

        val calendarView = findViewById<com.kizitonwose.calendar.view.CalendarView>(R.id.calendarView);
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                val events = periodData.getDayEvents(data.date)
                if (events != null)
                {
                    container.icon.visibility = View.VISIBLE
                    when (events[0].type) {
                        EventType.PeriodStart -> {
                            container.icon.setImageDrawable(resources.getDrawable(R.drawable.baseline_arrow_back_ios_24))
                        }
                        EventType.PeriodEnd -> {
                            container.icon.setImageDrawable(resources.getDrawable(R.drawable.baseline_arrow_forward_ios_24))

                        }

                        else -> {
                            container.icon.setImageDrawable(resources.getDrawable(R.drawable.baseline_bloodtype_24))
                        }
                    }
                }
                else
                {
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
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                container.titles.findViewById<TextView>(R.id.currentMonthText).text = data.yearMonth.format(
                    DateTimeFormatter.ofPattern("yyyy MMM")
                )
                container.titles.findViewById<ImageButton>(R.id.prevMonthButton).setOnClickListener {
                    val prevMonth = data.yearMonth.minusMonths(1)
                    calendarView.smoothScrollToMonth(prevMonth)
                }
                container.titles.findViewById<ImageButton>(R.id.nextMonthButton).setOnClickListener {
                    val nextMonth = data.yearMonth.plusMonths(1)
                    calendarView.smoothScrollToMonth(nextMonth)
                }
                // Remember that the header is reused so this will be called for each month.
                // However, the first day of the week will not change so no need to bind
                // the same view every time it is reused.
                if (container.titles.tag == null) {
                    container.titles.tag = data.yearMonth
                    container.titles.findViewById<LinearLayout>(R.id.dayHeader).children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            textView.text = title
                            // In the code above, we use the same `daysOfWeek` list
                            // that was created when we set up the calendar.
                            // However, we can also get the `daysOfWeek` list from the month data:
                            // val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
                            // Alternatively, you can get the value for this specific index:
                            // val dayOfWeek = data.weekDays.first()[index].date.dayOfWeek
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