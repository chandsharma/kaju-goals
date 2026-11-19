package com.chandsharma.kajugoals.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
        WHERE goals.status != 'ARCHIVED'
        GROUP BY goals.id
        ORDER BY goals.sortOrder ASC, goals.createdAt DESC
        """,
    )
    fun observeGoalSummaries(): Flow<List<GoalSummaryRow>>

    @Query(
        """
        SELECT progress_entries.*,
               goals.title AS goal_title,
               goals.metricType AS metric_type,
               goals.unitLabel AS unit_label
        FROM progress_entries
        INNER JOIN goals ON goals.id = progress_entries.goalId
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

    @Query("UPDATE goals SET status = :status, updatedAt = :updatedAt WHERE id = :goalId")
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProgress(entry: ProgressEntryEntity)

    @Query("DELETE FROM progress_entries WHERE id = :entryId")
    suspend fun deleteProgress(entryId: String)
}
