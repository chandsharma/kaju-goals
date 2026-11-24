package com.chandsharma.kajugoals.domain.logic

import com.chandsharma.kajugoals.domain.model.Goal
import kotlin.math.ceil

enum class PaceStatus {
    NO_DEADLINE,
    COMPLETE,
    ON_PACE,
    BEHIND,
    OVERDUE,
}

data class GoalPace(
    val status: PaceStatus,
    val daysRemaining: Int? = null,
    val remainingPerWeek: Double? = null,
)

object GoalPaceCalculator {
    private const val DAY_MILLIS = 86_400_000.0

    fun calculate(goal: Goal, now: Long): GoalPace {
        if (goal.isComplete) return GoalPace(PaceStatus.COMPLETE, 0, 0.0)
        val deadline = goal.deadlineAt ?: return GoalPace(PaceStatus.NO_DEADLINE)
        if (deadline <= now) return GoalPace(PaceStatus.OVERDUE, 0, null)

        val daysRemaining = ceil((deadline - now) / DAY_MILLIS).toInt().coerceAtLeast(1)
        val remaining = (goal.targetValue - goal.currentValue).coerceAtLeast(0L).toDouble()
        val remainingPerWeek = remaining / daysRemaining * 7.0
        val totalWindow = (deadline - goal.createdAt).coerceAtLeast(1L).toDouble()
        val elapsed = (now - goal.createdAt).coerceAtLeast(0L).toDouble()
        val expectedProgress = (elapsed / totalWindow).coerceIn(0.0, 1.0)
        val actualProgress = if (goal.targetValue <= 0L) {
            0.0
        } else {
            goal.currentValue.toDouble() / goal.targetValue
        }
        val status = if (actualProgress + 0.05 >= expectedProgress) {
            PaceStatus.ON_PACE
        } else {
            PaceStatus.BEHIND
        }
        return GoalPace(status, daysRemaining, remainingPerWeek)
    }
}
