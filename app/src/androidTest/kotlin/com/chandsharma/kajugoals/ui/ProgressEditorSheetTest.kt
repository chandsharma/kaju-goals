package com.chandsharma.kajugoals.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.ui.screens.ProgressEditorSheet
import com.chandsharma.kajugoals.ui.theme.KajuGoalsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProgressEditorSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun correction_canChangeAnEntryToSubtraction() {
        var savedDelta: Long? = null
        val record = ProgressRecord(
            id = "entry",
            goalId = "goal",
            goalTitle = "Read books",
            metricType = GoalMetricType.COUNT,
            unitLabel = "books",
            delta = 1,
            note = null,
            occurredAt = 1_700_000_000_000,
            createdAt = 1_700_000_000_000,
        )
        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                ProgressEditorSheet(record) { delta, _, _ -> savedDelta = delta }
            }
        }

        composeRule.onNodeWithText("Subtracted").performClick()
        composeRule.onNodeWithText("Amount").performTextReplacement("2")
        composeRule.onNodeWithText("Save correction").performClick()

        assertEquals(-2L, savedDelta)
    }
}
