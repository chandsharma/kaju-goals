package com.chandsharma.kajugoals.data.repository

import com.chandsharma.kajugoals.data.local.GoalDao
import com.chandsharma.kajugoals.data.local.GoalEntity
import com.chandsharma.kajugoals.data.local.ProgressEntryEntity
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomGoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) : GoalRepository {
    override fun observeGoals(): Flow<List<Goal>> =
        goalDao.observeGoalSummaries().map { rows ->
            rows.map { row ->
                val inferredStatus = if (
                    row.goal.status == GoalStatus.ACTIVE && row.loggedValue >= row.goal.targetValue
                ) {
                    GoalStatus.COMPLETED
                } else {
                    row.goal.status
                }
                Goal(
                    id = row.goal.id,
                    title = row.goal.title,
                    metricType = row.goal.metricType,
                    targetValue = row.goal.targetValue,
                    currentValue = row.loggedValue,
                    unitLabel = row.goal.unitLabel,
                    status = inferredStatus,
                    createdAt = row.goal.createdAt,
                    updatedAt = row.goal.updatedAt,
                    lastProgressAt = row.lastProgressAt,
                )
            }
        }

    override fun observeRecentProgress(limit: Int): Flow<List<ProgressRecord>> =
        goalDao.observeRecentProgress(limit).map { rows ->
            rows.map { row ->
                ProgressRecord(
                    id = row.entry.id,
                    goalId = row.entry.goalId,
                    goalTitle = row.goalTitle,
                    metricType = row.metricType,
                    unitLabel = row.unitLabel,
                    delta = row.entry.delta,
                    note = row.entry.note,
                    occurredAt = row.entry.occurredAt,
                    createdAt = row.entry.createdAt,
                )
            }
        }

    override suspend fun createGoal(draft: GoalDraft): String {
        require(draft.title.isNotBlank()) { "Goal title is required" }
        require(draft.targetValue > 0L) { "Goal target must be greater than zero" }
        val now = timeProvider.nowMillis()
        val id = idGenerator.newId()
        goalDao.insertGoal(
            GoalEntity(
                id = id,
                title = draft.title.trim(),
                metricType = draft.metricType,
                targetValue = draft.targetValue,
                unitLabel = draft.unitLabel.trim(),
                status = GoalStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
                sortOrder = now,
            ),
        )
        return id
    }

    override suspend fun updateGoal(goalId: String, draft: GoalDraft) {
        require(draft.title.isNotBlank()) { "Goal title is required" }
        require(draft.targetValue > 0L) { "Goal target must be greater than zero" }
        val existing = checkNotNull(goalDao.getGoal(goalId)) { "Goal no longer exists" }
        goalDao.updateGoal(
            existing.copy(
                title = draft.title.trim(),
                metricType = draft.metricType,
                targetValue = draft.targetValue,
                unitLabel = draft.unitLabel.trim(),
                updatedAt = timeProvider.nowMillis(),
            ),
        )
    }

    override suspend fun logProgress(goalId: String, delta: Long, note: String?): String {
        require(delta > 0L) { "Progress must be greater than zero" }
        checkNotNull(goalDao.getGoal(goalId)) { "Goal no longer exists" }
        val now = timeProvider.nowMillis()
        val id = idGenerator.newId()
        goalDao.insertProgress(
            ProgressEntryEntity(
                id = id,
                goalId = goalId,
                delta = delta,
                note = note?.trim()?.takeIf(String::isNotBlank),
                occurredAt = now,
                createdAt = now,
            ),
        )
        return id
    }

    override suspend fun deleteProgress(entryId: String) {
        goalDao.deleteProgress(entryId)
    }

    override suspend fun setGoalStatus(goalId: String, status: GoalStatus) {
        goalDao.updateGoalStatus(goalId, status, timeProvider.nowMillis())
    }
}

fun interface TimeProvider {
    fun nowMillis(): Long
}

fun interface IdGenerator {
    fun newId(): String
}
