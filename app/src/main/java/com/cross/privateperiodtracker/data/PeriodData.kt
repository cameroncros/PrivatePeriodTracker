package com.cross.privateperiodtracker.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.time.Duration
import java.time.LocalDateTime
import java.util.Random
import kotlin.math.abs

enum class CurrentState {
    Freedom, Period, Pregnant, Unknown
}

enum class EventType {
    PeriodStart, PeriodEnd, PregnancyStart, PregnancyEnd, TamponStart, TamponEnd, Painkiller,
}

@JsonClass(generateAdapter = true)
data class PeriodEvent(val time: LocalDateTime, val type: EventType) {
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

data class PeriodStats(val mean: Duration, val variance: Duration)

@JsonClass(generateAdapter = true)
class PeriodData {
    var events: ArrayList<PeriodEvent> = ArrayList<PeriodEvent>()

    fun sort() {
        events.sortedWith(compareBy { it.time })
    }

    fun addEvent(event: PeriodEvent) {
        events.add(event)
        sort()
    }

    private fun calcStats(minutes: ArrayList<Long>): PeriodStats {
        // Exclude outliers
        Stats.removeOutliers(minutes);

        // Calculate the mean
        val mean = Stats.mean(minutes);
        val meanDuration = Duration.ofMinutes(mean.toLong());

        // Calculate the variance
        val variance = Stats.variance(minutes);
        val varDuration = Duration.ofMinutes(variance.toLong());

        return PeriodStats(meanDuration, varDuration);
    }

    fun getCurrentState(): CurrentState {
        var lastIndex = events.size - 1;
        while (lastIndex >= 0) {
            val lastEvent = events[lastIndex]
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
        return CurrentState.Unknown;
    }

    fun calcAveragePeriodCycle(): PeriodStats {
        // Calculate the number of periods, and their lengths.
        val periods: ArrayList<Long> = ArrayList();
        var startTime: LocalDateTime? = null;
        for (event in events) {
            when (event.type) {
                EventType.PeriodStart -> {
                    if (startTime != null) {
                        val periodDuration = Duration.between(startTime, event.time).toMinutes()
                        periods.add(periodDuration)
                    }
                    startTime = event.time;
                }

                EventType.PregnancyStart -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null;
                }

                EventType.PregnancyEnd -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null;
                }

                else -> {
                    // ignore other events, not relevant.
                }
            }


        }

        // Calc stats
        return calcStats(periods);
    }

    fun calcAveragePeriodDuration(): PeriodStats {
        // Calculate the duration of the period.
        val periods: ArrayList<Long> = ArrayList();
        var startTime: LocalDateTime? = null;
        for (event in events) {
            when (event.type) {
                EventType.PeriodStart -> {
                    startTime = event.time;
                }

                EventType.PeriodEnd -> {
                    if (startTime != null) {
                        val periodDuration = Duration.between(startTime, event.time).toMinutes()
                        periods.add(periodDuration)
                    }
                }

                EventType.PregnancyStart -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null;
                }

                EventType.PregnancyEnd -> {
                    // Pregnancy will screw up calculations :/
                    startTime = null;
                }

                else -> {
                    // ignore other events, not relevant.
                }
            }
        }

        // Calc stats
        return calcStats(periods);
    }

    fun calcNextPeriodDate(): LocalDateTime? {
        val ps = calcAveragePeriodCycle()
        var lastIndex = events.size - 1;
        while (lastIndex >= 0) {
            val lastEvent = events[lastIndex];
            if (lastEvent.type == EventType.PeriodStart) {
                return lastEvent.time + ps.mean;
            }
            lastIndex -= 1
        }
        return null
    }

    fun calcEndOfPeriodDate(): LocalDateTime? {
        val ps = calcAveragePeriodDuration()
        var lastIndex = events.size - 1;
        while (lastIndex >= 0) {
            val lastEvent = events[lastIndex];
            if (lastEvent.type == EventType.PeriodStart) {
                return lastEvent.time + ps.mean;
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
        for (event in events)
        {
            if (!other.events.contains(event))
            {
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
}

fun randLong(low: Long, high: Long): Long {
    return abs(Random().nextLong()) % (high - low) + low
}

const val FORGETFULNESS: Double = 0.1  // Forget to log start/stop 10% of the time
fun didForget(): Boolean {
    return randLong(0, 100) < FORGETFULNESS * 100
}

const val MAX_GENERATED_DAYS_S: Long = 7 * 365 * 24 * 60 * 60;
const val MIN_GENERATED_DAYS_S: Long = 3 * 365 * 24 * 60 * 60;

const val MIN_PERIOD_CYCLE_S: Long = 24 * 60 * 60;
const val MAX_PERIOD_CYCLE_S: Long = 32 * 60 * 60;
const val MIN_PERIOD_DURATION_S: Long = 2 * 60 * 60;
const val MAX_PERIOD_DURATION_S: Long = 5 * 60 * 60;

fun generateData(): PeriodData {
    val pd = PeriodData();
    var startDate =
        LocalDateTime.now().minusSeconds(randLong(MIN_GENERATED_DAYS_S, MAX_GENERATED_DAYS_S));
// Future: Generate tampon events, generate pregnancy events??
    while (startDate < LocalDateTime.now()) {
        val periodDurationLength = randLong(MIN_PERIOD_DURATION_S, MAX_PERIOD_DURATION_S)
        val periodCycleLength = randLong(MIN_PERIOD_CYCLE_S, MAX_PERIOD_CYCLE_S)

        if (!didForget()) {
            pd.addEvent(PeriodEvent(time = startDate, type = EventType.PeriodStart));
        }
        startDate = startDate.plusSeconds(periodDurationLength);
        if (startDate > LocalDateTime.now()) {
            break;
        }
        if (!didForget()) {
            pd.addEvent(PeriodEvent(time = startDate, type = EventType.PeriodEnd));
        }
        startDate = startDate.plusSeconds(periodCycleLength - periodDurationLength)
        if (startDate > LocalDateTime.now()) {
            break;
        }
    }
    return pd;
}