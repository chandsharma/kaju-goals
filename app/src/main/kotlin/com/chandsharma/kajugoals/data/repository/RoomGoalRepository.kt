package com.chandsharma.kajugoals.data.repository

import com.chandsharma.kajugoals.data.local.GoalDao
import com.chandsharma.kajugoals.data.local.GoalEntity
import com.chandsharma.kajugoals.data.local.ProgressEntryEntity
import com.chandsharma.kajugoals.data.local.periodBoundaries
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class RoomGoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
    private val syncScheduler: SyncScheduler,
    private val reminderScheduler: GoalReminderScheduler,
    private val widgetUpdater: WidgetUpdater,
) : GoalRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeGoals(): Flow<List<Goal>> = periodBoundaryFlow()
        .flatMapLatest { boundaries ->
            goalDao.observeGoalSummaries(
                dayStart = boundaries.dayStart,
                weekStart = boundaries.weekStart,
                monthStart = boundaries.monthStart,
            )
        }.map { rows ->
            rows.map { row ->
                val inferredStatus = if (
                    row.goal.recurrence == com.chandsharma.kajugoals.domain.model.GoalRecurrence.NONE &&
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
                    deadlineAt = row.goal.deadlineAt,
                    category = row.goal.category,
                    isPinned = row.goal.isPinned,
                    reminderAt = row.goal.reminderAt,
                    recurrence = row.goal.recurrence,
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
                    updatedAt = row.entry.updatedAt,
                )
            }
        }

    override suspend fun createGoal(draft: GoalDraft): String {
        require(draft.title.isNotBlank()) { "Goal title is required" }
        require(draft.targetValue > 0L) { "Goal target must be greater than zero" }
        val now = timeProvider.nowMillis()
        val id = idGenerator.newId()
        val mutationId = idGenerator.newId()
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
                deadlineAt = draft.deadlineAt,
                category = draft.category.trim(),
                isPinned = draft.isPinned,
                reminderAt = draft.reminderAt,
                recurrence = draft.recurrence,
                mutationId = mutationId,
            ),
        )
        scheduleReminder(id, draft)
        syncScheduler.requestSync()
        widgetUpdater.refresh()
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
                deadlineAt = draft.deadlineAt,
                category = draft.category.trim(),
                isPinned = draft.isPinned,
                reminderAt = draft.reminderAt,
                recurrence = draft.recurrence,
                updatedAt = timeProvider.nowMillis(),
                mutationId = idGenerator.newId(),
            ),
        )
        scheduleReminder(goalId, draft)
        syncScheduler.requestSync()
        widgetUpdater.refresh()
    }

    override suspend fun logProgress(
        goalId: String,
        delta: Long,
        note: String?,
        occurredAt: Long?,
    ): String {
        require(delta != 0L) { "Progress change cannot be zero" }
        checkNotNull(goalDao.getGoal(goalId)) { "Goal no longer exists" }
        val now = timeProvider.nowMillis()
        val id = idGenerator.newId()
        goalDao.insertProgress(
            ProgressEntryEntity(
                id = id,
                goalId = goalId,
                delta = delta,
                note = note?.trim()?.takeIf(String::isNotBlank),
                occurredAt = occurredAt ?: now,
                createdAt = now,
                updatedAt = now,
                mutationId = idGenerator.newId(),
            ),
        )
        syncScheduler.requestSync()
        widgetUpdater.refresh()
        return id
    }

    override suspend fun updateProgress(
        entryId: String,
        delta: Long,
        note: String?,
        occurredAt: Long,
    ) {
        require(delta != 0L) { "Progress change cannot be zero" }
        val existing = checkNotNull(goalDao.getProgress(entryId)) { "Progress entry no longer exists" }
        goalDao.updateProgress(
            existing.copy(
                delta = delta,
                note = note?.trim()?.takeIf(String::isNotBlank),
                occurredAt = occurredAt,
                updatedAt = timeProvider.nowMillis(),
                mutationId = idGenerator.newId(),
                deletedAt = null,
            ),
        )
        syncScheduler.requestSync()
        widgetUpdater.refresh()
    }

    override suspend fun deleteProgress(entryId: String) {
        val now = timeProvider.nowMillis()
        goalDao.tombstoneProgress(
            entryId = entryId,
            deletedAt = now,
            updatedAt = now,
            mutationId = idGenerator.newId(),
        )
        syncScheduler.requestSync()
        widgetUpdater.refresh()
    }

    override suspend fun setGoalStatus(goalId: String, status: GoalStatus) {
        goalDao.updateGoalStatus(
            goalId = goalId,
            status = status,
            updatedAt = timeProvider.nowMillis(),
            mutationId = idGenerator.newId(),
        )
        if (status != GoalStatus.ACTIVE) reminderScheduler.cancel(goalId)
        syncScheduler.requestSync()
        widgetUpdater.refresh()
    }

    override suspend fun setGoalPinned(goalId: String, isPinned: Boolean) {
        goalDao.updateGoalPinned(
            goalId = goalId,
            isPinned = isPinned,
            updatedAt = timeProvider.nowMillis(),
            mutationId = idGenerator.newId(),
        )
        syncScheduler.requestSync()
        widgetUpdater.refresh()
    }

    private fun scheduleReminder(goalId: String, draft: GoalDraft) {
        val reminderAt = draft.reminderAt
        if (reminderAt == null) {
            reminderScheduler.cancel(goalId)
        } else {
            reminderScheduler.schedule(goalId, draft.title.trim(), reminderAt)
        }
    }

    private fun periodBoundaryFlow() = flow {
        while (true) {
            val boundaries = periodBoundaries(timeProvider.nowMillis())
            emit(boundaries)
            delay((boundaries.nextDayStart - timeProvider.nowMillis()).coerceAtLeast(1_000L))
        }
    }
}

fun interface TimeProvider {
    fun nowMillis(): Long
}

fun interface IdGenerator {
    fun newId(): String
}

fun interface SyncScheduler {
    fun requestSync()
}

interface GoalReminderScheduler {
    fun schedule(goalId: String, goalTitle: String, triggerAt: Long)

    fun cancel(goalId: String)
}

fun interface WidgetUpdater {
    suspend fun refresh()
}
