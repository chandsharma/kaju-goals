package com.chandsharma.kajugoals.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.chandsharma.kajugoals.domain.model.GoalMetricType

data class GoalSummaryRow(
    @Embedded val goal: GoalEntity,
    @ColumnInfo(name = "logged_value") val loggedValue: Long,
    @ColumnInfo(name = "last_progress_at") val lastProgressAt: Long?,
)

data class ProgressRecordRow(
    @Embedded val entry: ProgressEntryEntity,
    @ColumnInfo(name = "goal_title") val goalTitle: String,
    @ColumnInfo(name = "metric_type") val metricType: GoalMetricType,
    @ColumnInfo(name = "unit_label") val unitLabel: String,
)
