package com.chandsharma.kajugoals.ui.format

import com.chandsharma.kajugoals.domain.model.GoalMetricType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalFormattersTest {
    @Test
    fun count_usesReadableSingularAndPluralUnits() {
        assertEquals("1 book", formatMetricValue(GoalMetricType.COUNT, 1, "books"))
        assertEquals("12 books", formatMetricValue(GoalMetricType.COUNT, 12, "books"))
        assertEquals("1 kg", formatMetricValue(GoalMetricType.COUNT, 1, "kg"))
    }

    @Test
    fun inr_usesIndianGroupingWithoutFloatingPoint() {
        val formatted = formatMetricValue(
            GoalMetricType.CURRENCY,
            value = 10_000_000L,
            unitLabel = "INR",
        )

        assertTrue(formatted.contains("₹"))
        assertTrue(formatted.contains("1,00,000"))
        assertFalse(formatted.contains(".00"))
    }
}
