package com.chandsharma.kajugoals.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalTest {
    @Test
    fun progress_isCalculatedFromIntegerValuesAndClampedForDisplay() {
        val goal = goal(currentValue = 125, targetValue = 100)

        assertEquals(1f, goal.progressFraction)
        assertEquals(100, goal.progressPercent)
        assertTrue(goal.isComplete)
    }

    @Test
    fun zeroTarget_hasSafeZeroProgress() {
        val goal = goal(currentValue = 5, targetValue = 0)

        assertEquals(0f, goal.progressFraction)
        assertEquals(0, goal.progressPercent)
    }

    private fun goal(currentValue: Long, targetValue: Long) = Goal(
        id = "goal-1",
        title = "Read deeply",
        metricType = GoalMetricType.COUNT,
        targetValue = targetValue,
        currentValue = currentValue,
        unitLabel = "pages",
        status = GoalStatus.ACTIVE,
        createdAt = 1,
        updatedAt = 1,
        lastProgressAt = null,
    )
}
