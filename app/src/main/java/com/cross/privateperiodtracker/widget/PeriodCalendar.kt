package com.cross.privateperiodtracker.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cross.privateperiodtracker.R
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodCalculator
import com.cross.privateperiodtracker.data.generateData
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme
import com.cross.privateperiodtracker.theme.Typography
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PeriodCalendar(
    calculator: PeriodCalculator,
    initialSelectedDay: LocalDate,
    daySelectedFn: (day: LocalDate) -> Unit,
) {
    var selectedDay by remember { mutableStateOf(initialSelectedDay) }
    val currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val startMonth by remember { mutableStateOf(currentMonth.minusMonths(100)) } // Adjust as needed
    val endMonth by remember { mutableStateOf(currentMonth.plusMonths(100)) } // Adjust as needed
    val firstDayOfWeek by remember { mutableStateOf(firstDayOfWeekFromLocale()) } // Available from the library

    val coroutineScope = rememberCoroutineScope()
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    HorizontalCalendar(
        state = state,
        dayContent = {
            Day(
                day = it,
                calculator = calculator,
                clickFn = { cd: CalendarDay ->
                    selectedDay = cd.date
                    daySelectedFn(cd.date)
                },
                selectedDay = selectedDay
            )
        },
        monthHeader = {
            MonthHeader(it,
                selectedDay = selectedDay,
                scrollFn = { my: YearMonth ->
                    coroutineScope.launch {
                        state.animateScrollToMonth(my)
                    }
                })
        },
        monthContainer = { _, container ->
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            Box(
                modifier = Modifier
                    .width(screenWidth)
                    .padding(8.dp)
            ) {
                container()
            }
        },
        calendarScrollPaged = true,
        userScrollEnabled = true
    )
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

@Composable
fun MonthHeader(it: CalendarMonth, selectedDay: LocalDate, scrollFn: (md: YearMonth) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        IconButton(onClick = { scrollFn(it.yearMonth.minusMonths(1)) }) {
            Icon(Icons.Filled.ChevronLeft, "previous month")
        }
        val heading = if (selectedDay.yearMonth == it.yearMonth) {
            selectedDay.toString()
        } else {
            it.yearMonth.toString()
        }
        Text(heading)
        IconButton(onClick = { scrollFn(it.yearMonth.plusMonths(1)) }) {
            Icon(Icons.Filled.ChevronRight, "next month")
        }
    }
    DaysOfWeekTitle(daysOfWeek = daysOfWeek())
}

@Composable
fun Day(
    day: CalendarDay,
    selectedDay: LocalDate,
    calculator: PeriodCalculator,
    clickFn: (cd: CalendarDay) -> Unit
) {
    val events = calculator.getDayEvents(day.date)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = { clickFn(day) })
    )
    {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight(1f)
        )
        {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(1f)
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = Typography.bodySmall
                )
                if (events.size >= 1) {
                    val event = events[0]
                    when (event.type) {
                        EventType.PeriodStart -> Image(
                            painterResource(id = R.drawable.icon_period_start),
                            "period start"
                        )

                        EventType.PeriodEnd -> Image(
                            painterResource(id = R.drawable.icon_period_stop),
                            "period stop"
                        )

                        EventType.PregnancyStart -> Image(
                            painterResource(id = R.drawable.icon_pregnancy_start),
                            "pregnancy start"
                        )

                        EventType.PregnancyEnd -> Image(
                            painterResource(id = R.drawable.icon_pregnancy_stop),
                            "pregnancy stop"
                        )

                        EventType.TamponStart -> Image(
                            painterResource(id = R.drawable.icon_tampon_start),
                            "tampon start"
                        )

                        EventType.TamponEnd -> Image(
                            painterResource(id = R.drawable.icon_tampon_stop),
                            "tampon start"
                        )

                        EventType.Painkiller -> Image(
                            painterResource(id = R.drawable.icon_painkiller),
                            "painkiller"
                        )
                    }
                }
            }
            if (day.date == selectedDay) {
                Row(
                    modifier = Modifier
                        .padding(0.dp)
                        .background(color = Color.Red)
                        .defaultMinSize(minHeight = 4.dp)
                        .fillMaxWidth(1f)
                ) {}
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 64, heightDp = 64)
@Composable
fun DayPreview() {
    val periodData = generateData()
    var date = LocalDate.now()
    PrivatePeriodTrackerTheme {
        Day(
            day = CalendarDay(date = date, position = DayPosition.MonthDate),
            selectedDay = date,
            calculator = periodData.calculator(),
            clickFn = { cd: CalendarDay ->
                date = cd.date
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PeriodCalendarPreview() {
    val periodData = generateData()
    PrivatePeriodTrackerTheme {
        PeriodCalendar(
            calculator = periodData.calculator(),
            initialSelectedDay = LocalDate.now(),
            daySelectedFn = { _: LocalDate -> },
        )
    }
}
