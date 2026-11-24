package com.chandsharma.kajugoals.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.GoalRecurrence
import com.chandsharma.kajugoals.domain.model.GoalStatus

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val metricType: GoalMetricType,
    val targetValue: Long,
    val unitLabel: String,
    val status: GoalStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Long,
    val deadlineAt: Long? = null,
    val category: String = "",
    val isPinned: Boolean = false,
    val reminderAt: Long? = null,
    val recurrence: GoalRecurrence = GoalRecurrence.NONE,
    val mutationId: String = id,
    val deletedAt: Long? = null,
)
