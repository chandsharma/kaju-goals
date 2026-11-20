package com.chandsharma.kajugoals.ui.format

import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import java.math.BigDecimal
import java.math.BigInteger
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

fun Goal.formattedCurrent(): String = formatMetricValue(metricType, currentValue, unitLabel)

fun Goal.formattedTarget(): String = formatMetricValue(metricType, targetValue, unitLabel)

fun ProgressRecord.formattedDelta(): String =
    "+${formatMetricValue(metricType, delta, unitLabel)}"

fun formatMetricValue(type: GoalMetricType, value: Long, unitLabel: String): String =
    when (type) {
        GoalMetricType.COUNT -> if (unitLabel.isBlank()) {
            "$value"
        } else {
            "$value ${countUnitFor(value, unitLabel)}"
        }
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
        GoalMetricType.CURRENCY -> formatCurrency(value, unitLabel)
        GoalMetricType.BINARY -> if (value >= 1) "Done" else "Not done"
    }

fun metricSymbol(type: GoalMetricType, unitLabel: String): String = when (type) {
    GoalMetricType.COUNT -> "123"
    GoalMetricType.DURATION_MINUTES -> "◷"
    GoalMetricType.PERCENTAGE -> "%"
    GoalMetricType.CURRENCY -> currencySymbol(unitLabel)
    GoalMetricType.BINARY -> "✓"
}

fun currencySymbol(code: String): String = when (code.uppercase(Locale.ROOT)) {
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    else -> "₹"
}

private fun formatCurrency(minorUnits: Long, code: String): String {
    val normalizedCode = code.uppercase(Locale.ROOT).takeIf { it in supportedCurrencies } ?: "INR"
    if (normalizedCode == "INR") return formatIndianRupees(minorUnits)
    val locale = when (normalizedCode) {
        "USD" -> Locale.US
        "EUR" -> Locale.GERMANY
        "GBP" -> Locale.UK
        else -> Locale.US
    }
    val amount = BigDecimal.valueOf(minorUnits, 2)
    return NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(normalizedCode)
        minimumFractionDigits = if (minorUnits % 100L == 0L) 0 else 2
        maximumFractionDigits = 2
    }.format(amount)
}

private fun formatIndianRupees(minorUnits: Long): String {
    val absoluteMinor = BigInteger.valueOf(minorUnits).abs()
    val whole = absoluteMinor.divide(BigInteger.valueOf(100L)).toString()
    val fraction = absoluteMinor.remainder(BigInteger.valueOf(100L)).toInt()
    val sign = if (minorUnits < 0L) "−" else ""
    val decimal = if (fraction == 0) "" else ".${fraction.toString().padStart(2, '0')}"
    return "$sign₹${whole.withIndianGrouping()}$decimal"
}

private fun String.withIndianGrouping(): String {
    if (length <= 3) return this
    val finalThree = takeLast(3)
    val leading = dropLast(3)
    val groupedLeading = leading.reversed().chunked(2).joinToString(",").reversed()
    return "$groupedLeading,$finalThree"
}

private fun countUnitFor(value: Long, unit: String): String = when {
    value != 1L -> unit
    unit in invariantCountUnits -> unit
    unit.endsWith("ies", ignoreCase = true) -> unit.dropLast(3) + "y"
    unit.endsWith("s", ignoreCase = true) -> unit.dropLast(1)
    else -> unit
}

private val supportedCurrencies = setOf("INR", "USD", "EUR", "GBP")
private val invariantCountUnits = setOf("kg", "km")

fun formatTimestamp(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
