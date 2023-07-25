package com.cross.privateperiodtracker.data

import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.time.Duration
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
class PeriodData(
    var events: MutableList<PeriodEvent> = mutableListOf()
) : Serializable {
    @Transient
    private var _calculator: PeriodCalculator? = null

    fun calculator(): PeriodCalculator {
        if (_calculator == null) {
            _calculator = PeriodCalculator(events)
        }
        return _calculator!!
    }

    fun addEvent(event: PeriodEvent) {
        events.add(event)
        _calculator = PeriodCalculator(events)
    }

    fun removeEvent(event: PeriodEvent) {
        events.remove(event)
        _calculator = PeriodCalculator(events)
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
    val data = mutableListOf<PeriodEvent>()
    var startDate =
        LocalDateTime.now().minusSeconds(randLong(MIN_GENERATED_DAYS_S, MAX_GENERATED_DAYS_S))
    // Future: Generate tampon events, generate pregnancy events??
    while (startDate < LocalDateTime.now()) {
        val periodDurationLength = randLong(MIN_PERIOD_DURATION_S, MAX_PERIOD_DURATION_S)
        val periodCycleLength = randLong(MIN_PERIOD_CYCLE_S, MAX_PERIOD_CYCLE_S)

        if (!didForget()) {
            data.add(PeriodEvent(time = startDate, type = EventType.PeriodStart))
        }
        startDate = startDate.plusSeconds(periodDurationLength)
        if (startDate > LocalDateTime.now()) {
            break
        }
        if (!didForget()) {
            data.add(PeriodEvent(time = startDate, type = EventType.PeriodEnd))
        }
        startDate = startDate.plusSeconds(periodCycleLength - periodDurationLength)
        if (startDate > LocalDateTime.now()) {
            break
        }
    }
    return PeriodData(events = data)
}