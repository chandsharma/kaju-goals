package com.chandsharma.kajugoals.domain.repository

import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>

    fun observeRecentProgress(limit: Int = 100): Flow<List<ProgressRecord>>

    suspend fun createGoal(draft: GoalDraft): String

    suspend fun updateGoal(goalId: String, draft: GoalDraft)

    suspend fun logProgress(goalId: String, delta: Long, note: String? = null): String

    suspend fun deleteProgress(entryId: String)

    suspend fun setGoalStatus(goalId: String, status: GoalStatus)
}
