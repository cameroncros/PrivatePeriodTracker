package com.cross.privateperiodtracker.data

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class PeriodCalculator(private val events: MutableList<PeriodEvent>) {
    private var index: HashMap<MonthYear, ArrayList<PeriodEvent>> = HashMap()

    private fun addIndex(event: PeriodEvent) {
        val date = event.time.toLocalDate()
        val key = MonthYear(date.month, date.year)
        if (!index.containsKey(key)) {
            index[key] = ArrayList()
        }
        index[key]?.add(event)
    }

    private fun rebuildIndex() {
        index = HashMap()

        for (event in events) {
            addIndex(event)
        }
    }

    fun getMonthEvents(date: LocalDate): ArrayList<PeriodEvent> {
        val key = MonthYear(date.month, date.year)
        @Suppress("SENSELESS_COMPARISON")
        if (index == null || index.size == 0) {
            rebuildIndex()
        }
        return index.getOrDefault(key, ArrayList())
    }

    fun getDayEvents(date: LocalDate): ArrayList<PeriodEvent> {
        val dayEvents = ArrayList<PeriodEvent>()
        val monthEvents = getMonthEvents(date)
        for (event in monthEvents) {
            if (event.time.toLocalDate() == date) {
                dayEvents.add(event)
            }
        }
        return dayEvents
    }

    fun sort() {
        events.sortBy { it.time }
    }


    private fun calcStats(minutes: ArrayList<Long>): PeriodStats {
        // Exclude outliers
        Stats.removeOutliers(minutes)

        // Calculate the mean
        val mean = Stats.mean(minutes)
        val meanDuration = Duration.ofMinutes(mean.toLong())

        // Calculate the variance
        val sd = Stats.sd(minutes)
        val sdDuration = Duration.ofMinutes(sd.toLong())

        return PeriodStats(meanDuration, sdDuration)
    }

    fun getState(date_in: LocalDate = LocalDate.now()): CurrentState {
        var date = LocalDate.now()
        if (date != null) {
            date = date_in
        }
        var monthData = getMonthEvents(date)
        repeat(3)
        {
            if (monthData.size == 0) {
                date = date.minusMonths(1)
                monthData = getMonthEvents(date)
            }
        }
        var lastIndex = monthData.size - 1
        while (lastIndex >= 0) {
            val lastEvent = monthData[lastIndex]
            when (lastEvent.type) {
                EventType.PeriodStart -> {
                    return CurrentState.Period
                }

                EventType.PeriodEnd -> {
                    return CurrentState.Freedom
                }

                EventType.PregnancyStart -> {
                    return CurrentState.Pregnant
                }

                else -> {
                    lastIndex -= 1
                }
            }
        }
        return CurrentState.Unknown
    }

    fun calcAveragePeriodCycle(): PeriodStats {
        // Calculate the number of periods, and their lengths.
        sort()
        val periods: ArrayList<Long> = ArrayList()
        var startTime: LocalDateTime? = null
        val eventsIterator = events.iterator()
        while (eventsIterator.hasNext()) {
            val event = eventsIterator.next()
            when (event.type) {
                EventType.PeriodStart -> {
                    if (startTime != null) {
                        val periodDuration = Duration.between(startTime, event.time).toMinutes()
                        periods.add(periodDuration)
                    }
                    startTime = event.time
                }

                EventType.PregnancyStart -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null
                }

                EventType.PregnancyEnd -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null
                }

                else -> {
                    // ignore other events, not relevant.
                }
            }


        }

        // Calc stats
        return calcStats(periods)
    }

    fun calcAveragePeriodDuration(): PeriodStats {
        // Calculate the duration of the period.
        val periods: ArrayList<Long> = ArrayList()
        var startTime: LocalDateTime? = null
        for (event in events) {
            when (event.type) {
                EventType.PeriodStart -> {
                    startTime = event.time
                }

                EventType.PeriodEnd -> {
                    if (startTime != null) {
                        val periodDuration = Duration.between(startTime, event.time).toMinutes()
                        periods.add(periodDuration)
                    }
                }

                EventType.PregnancyStart -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null
                }

                EventType.PregnancyEnd -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null
                }

                else -> {
                    // ignore other events, not relevant.
                }
            }
        }

        // Calc stats
        return calcStats(periods)
    }

    fun calcNextPeriodDate(): LocalDateTime? {
        val ps = calcAveragePeriodCycle()
        if (ps.mean <= Duration.ZERO) {
            return null
        }
        var lastIndex = events.size - 1
        while (lastIndex >= 0) {
            val lastEvent = events[lastIndex]
            if (lastEvent.type == EventType.PeriodStart) {
                var nextStart = lastEvent.time + ps.mean
                while (nextStart < LocalDateTime.now()) {
                    nextStart += ps.mean
                }
                return nextStart
            }
            lastIndex -= 1
        }
        return null
    }

    fun calcEndOfPeriodDate(): LocalDateTime? {
        val ps = calcAveragePeriodDuration()
        if (ps.mean <= Duration.ZERO) {
            return null
        }
        var lastIndex = events.size - 1
        while (lastIndex >= 0) {
            val lastEvent = events[lastIndex]
            if (lastEvent.type == EventType.PeriodStart) {
                return lastEvent.time + ps.mean
            }
            lastIndex -= 1
        }
        return null
    }

    fun getPregnancyStart(): LocalDateTime? {
        var lastIndex = events.size - 1
        while (lastIndex >= 0) {
            val lastEvent = events[lastIndex]
            if (lastEvent.type == EventType.PregnancyStart) {
                return lastEvent.time
            }
            lastIndex -= 1
        }
        return null
    }

}