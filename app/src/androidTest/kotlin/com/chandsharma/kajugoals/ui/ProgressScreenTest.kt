package com.chandsharma.kajugoals.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.ui.screens.ProgressScreen
import com.chandsharma.kajugoals.ui.theme.KajuGoalsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProgressScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun deletingHistory_requiresExplicitConfirmation() {
        var deletedId: String? = null
        val record = ProgressRecord(
            id = "entry-1",
            goalId = "goal-1",
            goalTitle = "Read 24 books",
            metricType = GoalMetricType.COUNT,
            unitLabel = "books",
            delta = 5,
            note = null,
            occurredAt = 1,
            createdAt = 1,
        )

        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                ProgressScreen(
                    state = MainUiState(isLoading = false, recentProgress = listOf(record)),
                    onCreateGoal = {},
                    onOpenGoal = {},
                    onDeleteProgress = { deletedId = it },
                    onEditProgress = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Delete progress entry").performClick()
        composeRule.onNodeWithText("Remove this progress entry?").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Remove this progress entry?").assertIsNotDisplayed()
        assertEquals(null, deletedId)

        composeRule.onNodeWithContentDescription("Delete progress entry").performClick()
        composeRule.onNodeWithText("Remove").performClick()
        assertEquals("entry-1", deletedId)
    }
}
