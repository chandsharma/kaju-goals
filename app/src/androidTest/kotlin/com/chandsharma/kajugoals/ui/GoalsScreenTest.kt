package com.chandsharma.kajugoals.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chandsharma.kajugoals.ui.screens.GoalsScreen
import com.chandsharma.kajugoals.ui.theme.KajuGoalsTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GoalsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_exposesClearCreateAction() {
        var clicked = false
        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                GoalsScreen(
                    state = MainUiState(isLoading = false),
                    onFilterChange = {},
                    onSortChange = {},
                    onSearchQueryChange = {},
                    onCreateGoal = { clicked = true },
                    onOpenGoal = {},
                    onEditGoal = {},
                    onLogGoal = {},
                    onStatusChange = { _, _ -> },
                    onPinChange = { _, _ -> },
                    onDuplicate = {},
                    onArchiveGoals = {},
                )
            }
        }

        composeRule.onNodeWithText("Your goals, clearly listed").assertIsDisplayed()
        composeRule.onNodeWithText("Create a goal").performClick()

        assertTrue(clicked)
    }
}
