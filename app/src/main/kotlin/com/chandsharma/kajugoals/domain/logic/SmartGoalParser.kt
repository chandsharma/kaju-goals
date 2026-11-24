package com.chandsharma.kajugoals.domain.logic

import com.chandsharma.kajugoals.domain.model.GoalMetricType
import java.math.BigDecimal
import java.math.RoundingMode

data class GoalSuggestion(
    val metricType: GoalMetricType,
    val targetValue: Long,
    val unitLabel: String,
)

/**
 * Small, deterministic and fully on-device interpreter for common goal sentences.
 * It deliberately returns no suggestion when the text is ambiguous.
 */
object SmartGoalParser {
    private val numberPattern = Regex(
        "(\\d[\\d,]*(?:\\.\\d+)?)\\s*(crores?|cr|lakhs?|lacs?|thousands?|k|millions?|m)?\\b",
        RegexOption.IGNORE_CASE,
    )
    private val durationPattern = Regex(
        "(\\d[\\d,]*(?:\\.\\d+)?)\\s*(minutes?|mins?|min|hours?|hrs?|hr)\\b",
        RegexOption.IGNORE_CASE,
    )
    private val countPattern = Regex(
        "(\\d[\\d,]*)\\s*(books?|steps?|pages?|sessions?|times?|workouts?|runs?|chapters?|" +
            "glasses?|kilomet(?:er|re)s?|kms?|miles?|kilograms?|kgs?|kg|reps?|items?|lessons?|" +
            "courses?|projects?|meditations?)\\b",
        RegexOption.IGNORE_CASE,
    )

    fun infer(title: String): GoalSuggestion? {
        val text = title.trim()
        if (text.isBlank()) return null

        inferCurrency(text)?.let { return it }
        inferDuration(text)?.let { return it }

        if (Regex("%|\\b(percent|percentage)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            return GoalSuggestion(GoalMetricType.PERCENTAGE, 100L, "%")
        }

        countPattern.find(text)?.let { match ->
            val target = match.groupValues[1].replace(",", "").toLongOrNull()
                ?.takeIf { it > 0L }
                ?: return@let
            val unit = canonicalCountUnit(match.groupValues[2])
            return GoalSuggestion(GoalMetricType.COUNT, target, unit)
        }

        val genericNumber = numberPattern.find(text)
        if (genericNumber != null && genericNumber.groupValues[2].isBlank()) {
            val target = genericNumber.groupValues[1].replace(",", "").toLongOrNull()
            if (target != null && target > 0L) {
                return GoalSuggestion(GoalMetricType.COUNT, target, "items")
            }
        }

        if (Regex("\\b(finish|complete|launch|ship|submit|publish)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text)
        ) {
            return GoalSuggestion(GoalMetricType.BINARY, 1L, "done")
        }

        return null
    }

    private fun inferDuration(text: String): GoalSuggestion? {
        val match = durationPattern.find(text) ?: return null
        val amount = parseDecimal(match.groupValues[1]) ?: return null
        val rawUnit = match.groupValues[2].lowercase()
        val isHours = rawUnit.startsWith("h")
        val minutes = amount
            .multiply(if (isHours) SIXTY else BigDecimal.ONE)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
            .takeIf { it > 0L }
            ?: return null
        return GoalSuggestion(
            metricType = GoalMetricType.DURATION_MINUTES,
            targetValue = minutes,
            unitLabel = if (isHours) "hours" else "minutes",
        )
    }

    private fun inferCurrency(text: String): GoalSuggestion? {
        val currency = when {
            '₹' in text || Regex("\\b(rs\\.?|inr|rupees?)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text) -> "INR"
            '$' in text || Regex("\\b(usd|dollars?)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text) -> "USD"
            '€' in text || Regex("\\b(eur|euros?)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text) -> "EUR"
            '£' in text || Regex("\\b(gbp|pounds?)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text) -> "GBP"
            Regex("\\b(save|saving|savings|earn|budget|invest)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text) -> "INR"
            else -> return null
        }
        val match = numberPattern.find(text) ?: return null
        val amount = parseDecimal(match.groupValues[1]) ?: return null
        val multiplier = when (match.groupValues[2].lowercase()) {
            "crore", "crores", "cr" -> BigDecimal("10000000")
            "lakh", "lakhs", "lac", "lacs" -> BigDecimal("100000")
            "thousand", "thousands", "k" -> BigDecimal("1000")
            "million", "millions", "m" -> BigDecimal("1000000")
            else -> BigDecimal.ONE
        }
        val minorUnits = amount
            .multiply(multiplier)
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
            .takeIf { it > 0L }
            ?: return null
        return GoalSuggestion(GoalMetricType.CURRENCY, minorUnits, currency)
    }

    private fun parseDecimal(value: String): BigDecimal? = runCatching {
        BigDecimal(value.replace(",", ""))
    }.getOrNull()

    private fun canonicalCountUnit(raw: String): String = when (raw.lowercase()) {
        "book" -> "books"
        "step" -> "steps"
        "page" -> "pages"
        "session" -> "sessions"
        "time" -> "times"
        "workout" -> "workouts"
        "run" -> "runs"
        "chapter" -> "chapters"
        "glass" -> "glasses"
        "kilometer", "kilometers", "kilometre", "kilometres", "km", "kms" -> "km"
        "mile" -> "miles"
        "kilogram", "kilograms", "kg", "kgs" -> "kg"
        "rep" -> "reps"
        "item" -> "items"
        "lesson" -> "lessons"
        "course" -> "courses"
        "project" -> "projects"
        "meditation" -> "meditations"
        else -> raw.lowercase()
    }

    private val SIXTY = BigDecimal("60")
}
