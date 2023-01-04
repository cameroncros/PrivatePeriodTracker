package com.cross.privateperiodtracker.data

import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.Random
import kotlin.math.abs

enum class CurrentState {
    Freedom, Period, Pregnant, Unknown
}

enum class EventType {
    PeriodStart, PeriodEnd, PregnancyStart, PregnancyEnd, TamponStart, TamponEnd, Painkiller,
}

@JsonClass(generateAdapter = true)
data class PeriodEvent(val time: LocalDateTime, val type: EventType) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PeriodEvent

        if (time != other.time) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "PeriodEvent(time=$time, type=$type)"
    }
}

data class PeriodStats(val mean: Duration, val sd: Duration)
data class MonthYear(val month: Month, val year: Int)

@JsonClass(generateAdapter = true)
class PeriodData : Serializable {
    var events: ArrayList<PeriodEvent> = ArrayList()

    @Transient
    var index: HashMap<MonthYear, ArrayList<PeriodEvent>> = HashMap()

    private fun addIndex(event: PeriodEvent) {
        val date = event.time.toLocalDate()
        val key = MonthYear(date.month, date.year)
        if (!index.containsKey(key)) {
            index[key] = ArrayList<PeriodEvent>()
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

    fun addEvent(event: PeriodEvent) {
        events.add(event)
        addIndex(event)
        sort()
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
        val monthData = getMonthEvents(date)

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PeriodData

        if (events.size != other.events.size) return false
        for (event in events) {
            if (!other.events.contains(event)) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        return events.hashCode()
    }

    override fun toString(): String {
        sort()
        return "PeriodData(events=$events)"
    }

    fun delete(event: PeriodEvent) {
        events.remove(event)
        rebuildIndex()
    }
}

fun randLong(low: Long, high: Long): Long {
    return abs(Random().nextLong()) % (high - low) + low
}

const val FORGETFULNESS: Double = 0.1  // Forget to log start/stop 10% of the time
fun didForget(): Boolean {
    return randLong(0, 100) < FORGETFULNESS * 100
}

const val S_IN_DAY: Long = 24 * 60 * 60
const val MAX_GENERATED_DAYS_S: Long = 7 * 365 * S_IN_DAY
const val MIN_GENERATED_DAYS_S: Long = 3 * 365 * S_IN_DAY

const val MIN_PERIOD_CYCLE_S: Long = 24 * S_IN_DAY
const val MAX_PERIOD_CYCLE_S: Long = 32 * S_IN_DAY
const val MIN_PERIOD_DURATION_S: Long = 2 * S_IN_DAY
const val MAX_PERIOD_DURATION_S: Long = 5 * S_IN_DAY

fun generateData(): PeriodData {
    val pd = PeriodData()
    var startDate =
        LocalDateTime.now().minusSeconds(randLong(MIN_GENERATED_DAYS_S, MAX_GENERATED_DAYS_S))
    // Future: Generate tampon events, generate pregnancy events??
    while (startDate < LocalDateTime.now()) {
        val periodDurationLength = randLong(MIN_PERIOD_DURATION_S, MAX_PERIOD_DURATION_S)
        val periodCycleLength = randLong(MIN_PERIOD_CYCLE_S, MAX_PERIOD_CYCLE_S)

        if (!didForget()) {
            pd.addEvent(PeriodEvent(time = startDate, type = EventType.PeriodStart))
        }
        startDate = startDate.plusSeconds(periodDurationLength)
        if (startDate > LocalDateTime.now()) {
            break
        }
        if (!didForget()) {
            pd.addEvent(PeriodEvent(time = startDate, type = EventType.PeriodEnd))
        }
        startDate = startDate.plusSeconds(periodCycleLength - periodDurationLength)
        if (startDate > LocalDateTime.now()) {
            break
        }
    }
    return pd
}