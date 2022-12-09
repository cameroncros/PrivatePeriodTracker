package com.cross.privateperiodtracker.data

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class PeriodDataTest {
    @Test
    fun randLongTest() {
        for (i in 1..10000) {
            val r = randLong(50, 100);
            assertThat(r, greaterThanOrEqualTo(50));
            assertThat(r, lessThanOrEqualTo(100));
        }
    }

    @Test
    fun calcStatsTest() {
    }

    @Test
    fun getCurrentStateTest_Unknown() {
        val pd = PeriodData()
        assertEquals(CurrentState.Unknown, pd.getCurrentState())
    }

    @Test
    fun getCurrentStateTest_Period() {
        val pd = PeriodData()
        pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.PeriodStart))
        assertEquals(CurrentState.Period, pd.getCurrentState())
        pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.Painkiller))
        assertEquals(CurrentState.Period, pd.getCurrentState())
    }

    @Test
    fun getCurrentStateTest_Freedom() {
        val pd = PeriodData()
        pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.PeriodEnd))
        assertEquals(CurrentState.Freedom, pd.getCurrentState())
        pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.Painkiller))
        assertEquals(CurrentState.Freedom, pd.getCurrentState())
    }

    @Test
    fun getCurrentStateTest_Pregnant() {
        val pd = PeriodData()
        pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.PregnancyStart))
        assertEquals(CurrentState.Pregnant, pd.getCurrentState())
        pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.Painkiller))
        assertEquals(CurrentState.Pregnant, pd.getCurrentState())
    }

    @Test
    fun calcAveragePeriodCycleTest() {
        val pd: PeriodData = generateData()
        val pStats = pd.calcAveragePeriodCycle()
        assertThat(
            Duration.ofSeconds(pStats.mean.seconds),
            lessThanOrEqualTo(Duration.ofSeconds(MAX_PERIOD_CYCLE_S))
        )
        assertThat(
            Duration.ofSeconds(pStats.mean.seconds),
            greaterThanOrEqualTo(Duration.ofSeconds(MIN_PERIOD_CYCLE_S))
        )
    }

    @Test
    fun calcPeriodLengthStats() {
        val pd: PeriodData = generateData()
        val pStats = pd.calcAveragePeriodDuration()
        assertThat(pStats.mean.seconds, lessThanOrEqualTo(MAX_PERIOD_DURATION_S))
        assertThat(pStats.mean.seconds, greaterThanOrEqualTo(MIN_PERIOD_DURATION_S))
    }

    @Test
    fun calcNextPeriodDateTest_Unknown() {
        val pd= PeriodData()
        for (i in 0..3)
        {
            pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.PeriodEnd))
            assertNull(pd.calcNextPeriodDate())
        }
    }

    @Test
    fun calcNextPeriodDateTest_Valid() {
        val pd= PeriodData()
        pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.PeriodStart))
        for (i in 0..3)
        {
            pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.PeriodEnd))
            assertNotNull(pd.calcNextPeriodDate())
        }
    }

    @Test
    fun calcEndOfPeriodDate_Unknown() {
        val pd= PeriodData()
        for (i in 0..3)
        {
            pd.addEvent(PeriodEvent(LocalDateTime.now(), EventType.PeriodEnd))
            assertNull(pd.calcNextPeriodDate())
        }
    }


}