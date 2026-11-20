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

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
