package com.chandsharma.kajugoals.ui

import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.domain.repository.GoalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun changingFilter_exposesOnlyMatchingGoals() = runTest {
        val repository = FakeGoalRepository(
            listOf(
                goal("active", GoalStatus.ACTIVE),
                goal("paused", GoalStatus.PAUSED),
            ),
        )
        val viewModel = MainViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.setFilter(GoalFilter.PAUSED)
        advanceUntilIdle()

        assertEquals(listOf("paused"), viewModel.uiState.value.visibleGoals.map(Goal::id))
    }

    private fun goal(id: String, status: GoalStatus) = Goal(
        id = id,
        title = id,
        metricType = GoalMetricType.COUNT,
        targetValue = 10,
        currentValue = 0,
        unitLabel = "sessions",
        status = status,
        createdAt = 1,
        updatedAt = 1,
        lastProgressAt = null,
    )
}

private class FakeGoalRepository(initialGoals: List<Goal>) : GoalRepository {
    private val goals = MutableStateFlow(initialGoals)
    private val progress = MutableStateFlow<List<ProgressRecord>>(emptyList())

    override fun observeGoals(): Flow<List<Goal>> = goals

    override fun observeRecentProgress(limit: Int): Flow<List<ProgressRecord>> = progress

    override suspend fun createGoal(draft: GoalDraft): String = "created"

    override suspend fun updateGoal(goalId: String, draft: GoalDraft) = Unit

    override suspend fun logProgress(goalId: String, delta: Long, note: String?): String = "entry"

    override suspend fun deleteProgress(entryId: String) = Unit

    override suspend fun setGoalStatus(goalId: String, status: GoalStatus) = Unit
}
