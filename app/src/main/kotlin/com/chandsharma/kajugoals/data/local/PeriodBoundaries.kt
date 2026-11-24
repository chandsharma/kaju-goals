package com.chandsharma.kajugoals.data.local

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class PeriodBoundaries(
    val dayStart: Long,
    val weekStart: Long,
    val monthStart: Long,
    val nextDayStart: Long,
)

fun periodBoundaries(
    now: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): PeriodBoundaries {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    fun java.time.LocalDate.startMillis() = atStartOfDay(zone).toInstant().toEpochMilli()
    return PeriodBoundaries(
        dayStart = today.startMillis(),
        weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).startMillis(),
        monthStart = today.withDayOfMonth(1).startMillis(),
        nextDayStart = today.plusDays(1).startMillis(),
    )
}
