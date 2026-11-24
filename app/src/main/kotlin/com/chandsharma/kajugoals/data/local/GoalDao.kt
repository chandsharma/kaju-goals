package com.chandsharma.kajugoals.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.chandsharma.kajugoals.domain.model.GoalStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query(
        """
        SELECT goals.*,
               COALESCE(SUM(progress_entries.delta), 0) AS logged_value,
               MAX(progress_entries.occurredAt) AS last_progress_at
        FROM goals
        LEFT JOIN progress_entries ON progress_entries.goalId = goals.id
             AND progress_entries.deletedAt IS NULL
             AND (
                 goals.recurrence = 'NONE'
                 OR (goals.recurrence = 'DAILY' AND progress_entries.occurredAt >= :dayStart)
                 OR (goals.recurrence = 'WEEKLY' AND progress_entries.occurredAt >= :weekStart)
                 OR (goals.recurrence = 'MONTHLY' AND progress_entries.occurredAt >= :monthStart)
             )
        WHERE goals.deletedAt IS NULL
        GROUP BY goals.id
        ORDER BY goals.isPinned DESC, goals.sortOrder ASC, goals.createdAt DESC
        """,
    )
    fun observeGoalSummaries(
        dayStart: Long,
        weekStart: Long,
        monthStart: Long,
    ): Flow<List<GoalSummaryRow>>

    @Query(
        """
        SELECT progress_entries.*,
               goals.title AS goal_title,
               goals.metricType AS metric_type,
               goals.unitLabel AS unit_label
        FROM progress_entries
        INNER JOIN goals ON goals.id = progress_entries.goalId
        WHERE progress_entries.deletedAt IS NULL
          AND goals.deletedAt IS NULL
        ORDER BY progress_entries.occurredAt DESC, progress_entries.createdAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecentProgress(limit: Int): Flow<List<ProgressRecordRow>>

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getGoal(goalId: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query(
        "UPDATE goals SET status = :status, updatedAt = :updatedAt, mutationId = :mutationId " +
            "WHERE id = :goalId",
    )
    suspend fun updateGoalStatus(
        goalId: String,
        status: GoalStatus,
        updatedAt: Long,
        mutationId: String,
    )

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProgress(entry: ProgressEntryEntity)

    @Query("SELECT * FROM progress_entries WHERE id = :entryId LIMIT 1")
    suspend fun getProgress(entryId: String): ProgressEntryEntity?

    @Update
    suspend fun updateProgress(entry: ProgressEntryEntity)

    @Query(
        "UPDATE progress_entries SET deletedAt = :deletedAt, updatedAt = :updatedAt, " +
            "mutationId = :mutationId WHERE id = :entryId",
    )
    suspend fun tombstoneProgress(
        entryId: String,
        deletedAt: Long,
        updatedAt: Long,
        mutationId: String,
    )

    @Query(
        "UPDATE goals SET isPinned = :isPinned, updatedAt = :updatedAt, mutationId = :mutationId " +
            "WHERE id = :goalId",
    )
    suspend fun updateGoalPinned(
        goalId: String,
        isPinned: Boolean,
        updatedAt: Long,
        mutationId: String,
    )

    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<GoalEntity>

    @Query("SELECT * FROM progress_entries")
    suspend fun getAllProgress(): List<ProgressEntryEntity>

    @Upsert
    suspend fun upsertGoals(goals: List<GoalEntity>)

    @Upsert
    suspend fun upsertProgress(entries: List<ProgressEntryEntity>)

    @Transaction
    suspend fun applyMergedSnapshot(
        goals: List<GoalEntity>,
        entries: List<ProgressEntryEntity>,
    ) {
        upsertGoals(goals)
        upsertProgress(entries)
    }
}
