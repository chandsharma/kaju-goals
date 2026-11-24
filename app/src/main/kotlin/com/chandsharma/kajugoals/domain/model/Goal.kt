package com.chandsharma.kajugoals.domain.model

import kotlin.math.roundToInt

enum class GoalMetricType {
    COUNT,
    DURATION_MINUTES,
    PERCENTAGE,
    CURRENCY,
    BINARY,
}

enum class GoalStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    ARCHIVED,
}

enum class GoalRecurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
}

data class Goal(
    val id: String,
    val title: String,
    val metricType: GoalMetricType,
    val targetValue: Long,
    val currentValue: Long,
    val unitLabel: String,
    val status: GoalStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val lastProgressAt: Long?,
    val deadlineAt: Long? = null,
    val category: String = "",
    val isPinned: Boolean = false,
    val reminderAt: Long? = null,
    val recurrence: GoalRecurrence = GoalRecurrence.NONE,
) {
    val progressFraction: Float
        get() = if (targetValue <= 0L) 0f else (currentValue.toDouble() / targetValue).toFloat().coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progressFraction * 100).roundToInt()

    val isComplete: Boolean
        get() = status == GoalStatus.COMPLETED || currentValue >= targetValue
}

data class ProgressRecord(
    val id: String,
    val goalId: String,
    val goalTitle: String,
    val metricType: GoalMetricType,
    val unitLabel: String,
    val delta: Long,
    val note: String?,
    val occurredAt: Long,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
)

data class GoalDraft(
    val title: String,
    val metricType: GoalMetricType,
    val targetValue: Long,
    val unitLabel: String,
    val deadlineAt: Long? = null,
    val category: String = "",
    val isPinned: Boolean = false,
    val reminderAt: Long? = null,
    val recurrence: GoalRecurrence = GoalRecurrence.NONE,
)
