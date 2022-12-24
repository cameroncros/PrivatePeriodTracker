package com.cross.privateperiodtracker

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cross.privateperiodtracker.data.CurrentState
import com.cross.privateperiodtracker.data.EventType
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


const val dataKey: String = "data"
const val eventKey: String = "event"

class HomeActivity : AppCompatActivity() {
    private lateinit var encryption: Encryption
    private lateinit var status: TextView
    private lateinit var stats: TextView
    private lateinit var calendarView : com.kizitonwose.calendar.view.CalendarView
    private lateinit var eventList : RecyclerView
    private var selectedDay : LocalDate? = null;

    fun update() {
        val periodData = encryption.data
        status.text = updateStatus()
        stats.text = updateStats()

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            val icon = view.findViewById<ImageView>(R.id.calendarDayIcon)
            // Will be set when this container is bound
            lateinit var day: LocalDate

            init {
                view.setOnClickListener {
                    updateEventList(day)
                    // Use the CalendarDay associated with this container.
                }
            }
        }

        calendarView.dayBinder =
            object : MonthDayBinder<DayViewContainer> {
                // Called only when a new container is needed.
                override fun create(view: View) = DayViewContainer(view)

                // Called every time we need to reuse a container.
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    // Set the calendar day for this container.
                    container.day = data.date
                    container.textView.text = data.date.dayOfMonth.toString()
                    val events = periodData.getDayEvents(data.date)
                    if (events.size > 0) {
                        container.icon.visibility = View.VISIBLE
                        when (events[0].type) {
                            EventType.PeriodStart -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.icon_period_start))
                            }
                            EventType.PeriodEnd  -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.icon_period_end))
                            }
                            EventType.PregnancyStart -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.icon_pregnancy_start))
                            }
                            EventType.PregnancyEnd -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.icon_pregnancy_end))
                            }
                            EventType.TamponStart -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.icon_tampon_start))
                            }
                            EventType.TamponEnd -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.icon_tampon_end))
                            }
                            EventType.Painkiller -> {
                                container.icon.setImageDrawable(resources.getDrawable(R.drawable.icon_painkiller))
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

    fun updateEventList(day: LocalDate) {
        selectedDay = day
        (eventList.adapter as EventListAdapter).updateData(encryption.data.getDayEvents(day))
        eventList.invalidate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        encryption =
            intent.getSerializableExtra(dataKey) as Encryption;

        status = findViewById(R.id.currentStatus)
        stats = findViewById(R.id.currentStats)
        calendarView = findViewById(R.id.calendarView)
        eventList = findViewById(R.id.eventList)
        eventList.layoutManager = LinearLayoutManager(this);
        eventList.adapter = EventListAdapter(::deleteEventCallback)

        val intentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val event: PeriodEvent =
                        result.data?.getSerializableExtra(eventKey) as PeriodEvent
                    encryption.data.addEvent(event)
                    encryption.saveData()
                    update()
                }
            }
        val addEventButton = findViewById<Button>(R.id.addEvent);
        addEventButton.setOnClickListener {
            val k = Intent(this, AddEventActivity::class.java)
            intentLauncher.launch(k);
        }

        update()
        updateNotifications()
    }

    private fun deleteEventCallback(event:PeriodEvent)
    {
        encryption.data.delete(event)
        encryption.saveData()

        selectedDay?.let { updateEventList(it) }
        update()
        updateNotifications()
    }

    private fun updateNotifications()
    {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0)
        }

        val nextPeriodDate = encryption.data.calcNextPeriodDate() ?: return
        val dayBefore = nextPeriodDate.minusDays(1)
        val nextPeriod = Duration.between(LocalDateTime.now(), dayBefore)

        this.registerReceiver(AlarmReceiver(), IntentFilter("cross.privateperiodtracker.NEXT_PERIOD_DUE"))

        val pintent = PendingIntent.getBroadcast(this, 0, Intent("cross.privateperiodtracker.NEXT_PERIOD_DUE"), FLAG_IMMUTABLE)
        val manager = this.getSystemService(ALARM_SERVICE) as AlarmManager

        manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + nextPeriod.toMillis(),
            pintent)
    }

    private fun updateStats(): String {
        val cycleStats = encryption.data.calcAveragePeriodCycle()
        val durationStats = encryption.data.calcAveragePeriodDuration()
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

    private fun updateStatus(): String {
        when (encryption.data.getState()) {
            CurrentState.Period -> {
                val endDate = encryption.data.calcEndOfPeriodDate() ?: return resources.getString(R.string.need_more_data)
                val delta = Duration.between(LocalDateTime.now(), endDate)
                val sb = StringBuilder()
                sb.append(resources.getString(R.string.period_will_end_in_))
                sb.append(delta.toDays())
                sb.append(resources.getString(R.string._days))
                return sb.toString()
            }

            CurrentState.Freedom -> {
                val endDate = encryption.data.calcNextPeriodDate() ?: return resources.getString(R.string.need_more_data)
                val delta = Duration.between(LocalDateTime.now(), endDate)
                val sb = StringBuilder()
                sb.append(resources.getString(R.string.next_period_in_))
                sb.append(delta.toDays())
                sb.append(resources.getString(R.string._days))
                return sb.toString()
            }

            CurrentState.Pregnant -> {
                val startDate = encryption.data.getPregnancyStart() ?: return resources.getString(R.string.need_more_data)
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