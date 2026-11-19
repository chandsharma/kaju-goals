package com.chandsharma.kajugoals.ui.format

import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Goal.formattedCurrent(): String = formatMetricValue(metricType, currentValue, unitLabel)

fun Goal.formattedTarget(): String = formatMetricValue(metricType, targetValue, unitLabel)

fun ProgressRecord.formattedDelta(): String =
    "+${formatMetricValue(metricType, delta, unitLabel)}"

fun formatMetricValue(type: GoalMetricType, value: Long, unitLabel: String): String =
    when (type) {
        GoalMetricType.COUNT -> if (unitLabel.isBlank()) "$value" else "$value $unitLabel"
        GoalMetricType.DURATION_MINUTES -> {
            val hours = value / 60
            val minutes = value % 60
            when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }
        }
        GoalMetricType.PERCENTAGE -> "$value%"
        GoalMetricType.BINARY -> if (value >= 1) "Done" else "Not done"
    }

fun formatTimestamp(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
