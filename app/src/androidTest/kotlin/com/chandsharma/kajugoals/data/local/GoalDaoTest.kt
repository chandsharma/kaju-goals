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

        val summary = dao.observeGoalSummaries().first().single()

        assertEquals(7, summary.loggedValue)
        assertEquals(2L, summary.lastProgressAt)
    }
}
