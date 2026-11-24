package com.chandsharma.kajugoals.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KajuGoalsDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2_preservesDataAndInitializesSyncMetadata() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                """
                INSERT INTO goals (
                    id, title, metricType, targetValue, unitLabel, status,
                    createdAt, updatedAt, sortOrder
                ) VALUES ('goal-1', 'Read books', 'COUNT', 12, 'books', 'ACTIVE', 1, 2, 1)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO progress_entries (
                    id, goalId, delta, note, occurredAt, createdAt
                ) VALUES ('entry-1', 'goal-1', 1, NULL, 3, 4)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE,
            2,
            true,
            KajuGoalsDatabase.MIGRATION_1_2,
        ).use { database ->
            database.query(
                "SELECT mutationId, deletedAt FROM goals WHERE id = 'goal-1'",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals("goal-1", cursor.getString(0))
                assertEquals(true, cursor.isNull(1))
            }
            database.query(
                "SELECT updatedAt, mutationId, deletedAt FROM progress_entries WHERE id = 'entry-1'",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals(4L, cursor.getLong(0))
                assertEquals("entry-1", cursor.getString(1))
                assertEquals(true, cursor.isNull(2))
            }
        }
    }

    @Test
    fun migrate2To3_preservesDataAndInitializesPlanningFields() {
        helper.createDatabase(TEST_DATABASE_V3, 2).apply {
            execSQL(
                """
                INSERT INTO goals (
                    id, title, metricType, targetValue, unitLabel, status,
                    createdAt, updatedAt, sortOrder, mutationId, deletedAt
                ) VALUES ('goal-2', 'Save money', 'CURRENCY', 100000, 'INR', 'ACTIVE',
                    1, 2, 1, 'mutation-2', NULL)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE_V3,
            3,
            true,
            KajuGoalsDatabase.MIGRATION_2_3,
        ).use { database ->
            database.query(
                "SELECT deadlineAt, category, isPinned, reminderAt, recurrence, mutationId FROM goals WHERE id = 'goal-2'",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals(true, cursor.isNull(0))
                assertEquals("", cursor.getString(1))
                assertEquals(0, cursor.getInt(2))
                assertEquals(true, cursor.isNull(3))
                assertEquals("NONE", cursor.getString(4))
                assertEquals("mutation-2", cursor.getString(5))
            }
        }
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
        const val TEST_DATABASE_V3 = "migration-test-v3"
    }
}
