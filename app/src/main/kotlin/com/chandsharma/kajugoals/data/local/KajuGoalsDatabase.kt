package com.chandsharma.kajugoals.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GoalEntity::class, ProgressEntryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class KajuGoalsDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
}
