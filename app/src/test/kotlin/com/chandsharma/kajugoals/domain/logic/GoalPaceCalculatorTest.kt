package com.chandsharma.kajugoals.domain.logic

import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.GoalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalPaceCalculatorTest {
    @Test
    fun halfwayThroughAndHalfComplete_isOnPace() {
        val pace = GoalPaceCalculator.calculate(goal(current = 50), now = DAY * 5)

        assertEquals(PaceStatus.ON_PACE, pace.status)
        assertEquals(5, pace.daysRemaining)
        assertTrue(checkNotNull(pace.remainingPerWeek) in 69.9..70.1)
    }

    @Test
    fun halfwayThroughAndBarelyStarted_isBehind() {
        assertEquals(
            PaceStatus.BEHIND,
            GoalPaceCalculator.calculate(goal(current = 5), now = DAY * 5).status,
        )
    }

    private fun goal(current: Long) = Goal(
        id = "goal",
        title = "Test",
        metricType = GoalMetricType.COUNT,
        targetValue = 100,
        currentValue = current,
        unitLabel = "items",
        status = GoalStatus.ACTIVE,
        createdAt = 0,
        updatedAt = 0,
        lastProgressAt = null,
        deadlineAt = DAY * 10,
    )

    private companion object {
        const val DAY = 86_400_000L
    }
}
