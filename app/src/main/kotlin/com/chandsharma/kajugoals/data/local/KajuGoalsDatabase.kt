package com.chandsharma.kajugoals.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GoalEntity::class, ProgressEntryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class KajuGoalsDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN mutationId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE goals ADD COLUMN deletedAt INTEGER")
                db.execSQL("UPDATE goals SET mutationId = id")

                db.execSQL(
                    "ALTER TABLE progress_entries ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE progress_entries ADD COLUMN mutationId TEXT NOT NULL DEFAULT ''",
                )
                db.execSQL("ALTER TABLE progress_entries ADD COLUMN deletedAt INTEGER")
                db.execSQL("UPDATE progress_entries SET updatedAt = createdAt, mutationId = id")
            }
        }
    }
}
