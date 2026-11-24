package com.chandsharma.kajugoals.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.ui.screens.GoalFormScreen
import com.chandsharma.kajugoals.ui.theme.KajuGoalsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class GoalFormScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun quickStart_createsCompleteGoalWithoutTyping() {
        var saved: GoalDraft? = null
        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                GoalFormScreen(
                    goal = null,
                    isEditing = false,
                    onBack = {},
                    onSave = { saved = it },
                )
            }
        }

        composeRule.onNodeWithText("Read 12 books").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Create goal").performClick()

        assertNotNull(saved)
        assertEquals("Read 12 books", saved?.title)
        assertEquals(GoalMetricType.COUNT, saved?.metricType)
        assertEquals(12L, saved?.targetValue)
        assertEquals("books", saved?.unitLabel)
    }

    @Test
    fun percentage_explainsAutomaticFinishLine() {
        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                GoalFormScreen(
                    goal = null,
                    isEditing = false,
                    onBack = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("What do you want to achieve?")
            .performTextInput("Get healthier")
        composeRule.onNodeWithText("Percentage").performScrollTo().performClick()

        composeRule.onNodeWithText("The finish line is automatically 100%")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun rupeeQuickStart_usesIntegerMinorUnits() {
        var saved: GoalDraft? = null
        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                GoalFormScreen(
                    goal = null,
                    isEditing = false,
                    onBack = {},
                    onSave = { saved = it },
                )
            }
        }

        composeRule.onNodeWithText("Save ₹1,00,000").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Create goal").performClick()

        assertEquals(GoalMetricType.CURRENCY, saved?.metricType)
        assertEquals(10_000_000L, saved?.targetValue)
        assertEquals("INR", saved?.unitLabel)
    }

    @Test
    fun naturalTitle_infersMoneyAndIndianMultiplier() {
        var saved: GoalDraft? = null
        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                GoalFormScreen(
                    goal = null,
                    isEditing = false,
                    onBack = {},
                    onSave = { saved = it },
                )
            }
        }

        composeRule.onNodeWithText("What do you want to achieve?")
            .performTextInput("Save ₹1 lakh")
        composeRule.onNodeWithText("Understood").assertIsDisplayed()
        composeRule.onNodeWithText("Create goal").performClick()

        assertEquals(GoalMetricType.CURRENCY, saved?.metricType)
        assertEquals(10_000_000L, saved?.targetValue)
        assertEquals("INR", saved?.unitLabel)
    }

    @Test
    fun manualMeasurementChoice_survivesLaterTitleEdits() {
        var saved: GoalDraft? = null
        composeRule.setContent {
            KajuGoalsTheme(darkTheme = false) {
                GoalFormScreen(
                    goal = null,
                    isEditing = false,
                    onBack = {},
                    onSave = { saved = it },
                )
            }
        }

        val title = composeRule.onNodeWithText("What do you want to achieve?")
        title.performTextInput("Save ₹1 lakh")
        composeRule.onNodeWithText("Edit").performClick()
        composeRule.onNodeWithText("Count").performScrollTo().performClick()
        title.performTextReplacement("Save ₹2 lakhs")
        composeRule.onNodeWithText("Create goal").performClick()

        assertEquals(GoalMetricType.COUNT, saved?.metricType)
        assertEquals(10L, saved?.targetValue)
        assertEquals("items", saved?.unitLabel)
    }
}
