package com.chandsharma.kajugoals.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_entries",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("goalId"),
        Index("occurredAt"),
    ],
)
data class ProgressEntryEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val delta: Long,
    val note: String?,
    val occurredAt: Long,
    val createdAt: Long,
)
