package com.cross.privateperiodtracker.lib

import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.PeriodEvent
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDateTime


class PeriodDataTest {
    @Test
    fun serialisePeriodEvent_Compat() {
        val now = LocalDateTime.now()
        val json = "{\"time\":\"$now\", \"type\": \"PeriodEnd\"}"

        val moshi: Moshi = Moshi.Builder().add(LocalDateTimeMoshiAdapter()).build()
        val jsonAdapter: JsonAdapter<PeriodEvent> = moshi.adapter(PeriodEvent::class.java)

        val periodEvent = jsonAdapter.fromJson(json)
        assertNotNull(periodEvent)
        assertEquals(now, periodEvent?.time)
        assertEquals(EventType.PeriodEnd, periodEvent?.type)
        assertEquals("", periodEvent?.notes)
    }

    @Test
    fun serialisePeriodEvent_Full() {
        val now = LocalDateTime.now()
        val inputEvent = PeriodEvent(now, EventType.PeriodEnd, "Notes")

        val moshi: Moshi = Moshi.Builder().add(LocalDateTimeMoshiAdapter()).build()
        val jsonAdapter: JsonAdapter<PeriodEvent> = moshi.adapter(PeriodEvent::class.java)

        val json = jsonAdapter.toJson(inputEvent)

        val periodEvent = jsonAdapter.fromJson(json)
        assertNotNull(periodEvent)
        assertEquals(now, periodEvent?.time)
        assertEquals(EventType.PeriodEnd, periodEvent?.type)
        assertEquals("Notes", periodEvent?.notes)
    }
}