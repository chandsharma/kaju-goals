package com.chandsharma.kajugoals.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chandsharma.kajugoals.domain.model.GoalMetricType
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
    val mutationId: String = id,
    val deletedAt: Long? = null,
)
