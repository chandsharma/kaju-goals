package com.chandsharma.kajugoals.data.local

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class PeriodBoundariesTest {
    @Test
    fun boundariesUseLocalCalendarAndMondayWeek() {
        val zone = ZoneId.of("Asia/Kolkata")
        val now = LocalDateTime.of(2026, 11, 20, 15, 30).atZone(zone).toInstant().toEpochMilli()

        val result = periodBoundaries(now, zone)

        assertEquals(
            LocalDateTime.of(2026, 11, 20, 0, 0).atZone(zone).toInstant().toEpochMilli(),
            result.dayStart,
        )
        assertEquals(
            LocalDateTime.of(2026, 11, 16, 0, 0).atZone(zone).toInstant().toEpochMilli(),
            result.weekStart,
        )
        assertEquals(
            LocalDateTime.of(2026, 11, 1, 0, 0).atZone(zone).toInstant().toEpochMilli(),
            result.monthStart,
        )
    }
}
