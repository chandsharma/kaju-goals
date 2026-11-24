package com.chandsharma.kajugoals.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.GoalStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalDaoTest {
    private lateinit var database: KajuGoalsDatabase
    private lateinit var dao: GoalDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KajuGoalsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.goalDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun goalSummary_aggregatesProgressEntries() = runTest {
        dao.insertGoal(
            GoalEntity(
                id = "goal-1",
                title = "Run 100 kilometres",
                metricType = GoalMetricType.COUNT,
                targetValue = 100,
                unitLabel = "km",
                status = GoalStatus.ACTIVE,
                createdAt = 1,
                updatedAt = 1,
                sortOrder = 1,
            ),
        )
        dao.insertProgress(
            ProgressEntryEntity(
                id = "entry-1",
                goalId = "goal-1",
                delta = 7,
                note = null,
                occurredAt = 2,
                createdAt = 2,
            ),
        )
        dao.insertProgress(
            ProgressEntryEntity(
                id = "entry-2",
                goalId = "goal-1",
                delta = -2,
                note = "Correction",
                occurredAt = 3,
                createdAt = 3,
            ),
        )

        val summary = dao.observeGoalSummaries(0, 0, 0).first().single()

        assertEquals(5, summary.loggedValue)
        assertEquals(3L, summary.lastProgressAt)
    }

    @Test
    fun editedProgress_updatesValueDateAndMutationMetadata() = runTest {
        dao.insertGoal(
            GoalEntity(
                id = "goal-2",
                title = "Read books",
                metricType = GoalMetricType.COUNT,
                targetValue = 12,
                unitLabel = "books",
                status = GoalStatus.ACTIVE,
                createdAt = 1,
                updatedAt = 1,
                sortOrder = 1,
            ),
        )
        val original = ProgressEntryEntity(
            id = "entry-edit",
            goalId = "goal-2",
            delta = 1,
            note = null,
            occurredAt = 2,
            createdAt = 2,
        )
        dao.insertProgress(original)

        dao.updateProgress(
            original.copy(
                delta = 3,
                note = "Actually three",
                occurredAt = 10,
                updatedAt = 11,
                mutationId = "edited",
            ),
        )

        val updated = checkNotNull(dao.getProgress("entry-edit"))
        assertEquals(3L, updated.delta)
        assertEquals("Actually three", updated.note)
        assertEquals(10L, updated.occurredAt)
        assertEquals(11L, updated.updatedAt)
        assertEquals("edited", updated.mutationId)
    }
}
