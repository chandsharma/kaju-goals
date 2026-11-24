package com.chandsharma.kajugoals.domain.logic

import com.chandsharma.kajugoals.domain.model.GoalMetricType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmartGoalParserTest {
    @Test
    fun readBooks_infersCountAndUnit() {
        assertEquals(
            GoalSuggestion(GoalMetricType.COUNT, 12L, "books"),
            SmartGoalParser.infer("Read 12 books"),
        )
    }

    @Test
    fun indianMoney_infersMinorUnitsAndCurrency() {
        assertEquals(
            GoalSuggestion(GoalMetricType.CURRENCY, 10_000_000L, "INR"),
            SmartGoalParser.infer("Save ₹1 lakh"),
        )
        assertEquals(
            GoalSuggestion(GoalMetricType.CURRENCY, 25_000_000L, "INR"),
            SmartGoalParser.infer("Invest 2.5 lakhs"),
        )
    }

    @Test
    fun exerciseHours_infersStoredMinutesAndEditableHours() {
        assertEquals(
            GoalSuggestion(GoalMetricType.DURATION_MINUTES, 1_800L, "hours"),
            SmartGoalParser.infer("Exercise 30 hours"),
        )
        assertEquals(
            GoalSuggestion(GoalMetricType.DURATION_MINUTES, 90L, "hours"),
            SmartGoalParser.infer("Exercise 1.5 hours"),
        )
    }

    @Test
    fun ambiguousText_doesNotPretendToKnow() {
        assertNull(SmartGoalParser.infer("Feel healthier"))
    }
}
