package com.chandsharma.kajugoals.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncMergerTest {
    @Test
    fun emptyLocalSnapshot_restoresCloudRecords() {
        val cloud = SyncPayload(
            goals = listOf(goal(title = "Cloud goal", updatedAt = 10, mutationId = "cloud")),
            progressEntries = listOf(progress(updatedAt = 11, mutationId = "cloud-progress")),
        )

        val merged = SyncMerger.merge(listOf(emptyPayload(), cloud))

        assertEquals("Cloud goal", merged.goals.single().title)
        assertEquals(5L, merged.progressEntries.single().delta)
    }

    @Test
    fun newerLocalEdit_winsWithoutDroppingCloudProgress() {
        val cloud = SyncPayload(
            goals = listOf(goal(title = "Old title", updatedAt = 10, mutationId = "cloud")),
            progressEntries = listOf(progress(updatedAt = 11, mutationId = "cloud-progress")),
        )
        val local = SyncPayload(
            goals = listOf(goal(title = "New title", updatedAt = 20, mutationId = "local")),
            progressEntries = emptyList(),
        )

        val merged = SyncMerger.merge(listOf(local, cloud))

        assertEquals("New title", merged.goals.single().title)
        assertEquals(1, merged.progressEntries.size)
    }

    @Test
    fun equalTimestamp_usesMutationIdAsDeterministicTieBreaker() {
        val earlier = goal(title = "Earlier", updatedAt = 10, mutationId = "a")
        val later = goal(title = "Later", updatedAt = 10, mutationId = "z")

        val merged = SyncMerger.merge(
            listOf(
                SyncPayload(listOf(later), emptyList()),
                SyncPayload(listOf(earlier), emptyList()),
            ),
        )

        assertEquals("Later", merged.goals.single().title)
    }

    @Test
    fun progressTombstone_isNotResurrectedByOlderSnapshot() {
        val live = progress(updatedAt = 10, mutationId = "live")
        val tombstone = live.copy(updatedAt = 20, mutationId = "deleted", deletedAt = 20)

        val merged = SyncMerger.merge(
            listOf(
                SyncPayload(listOf(goal()), listOf(live)),
                SyncPayload(listOf(goal()), listOf(tombstone)),
            ),
        )

        assertEquals(20L, merged.progressEntries.single().deletedAt)
    }

    @Test
    fun orphanProgress_isRejectedFromMergedSnapshot() {
        val merged = SyncMerger.merge(
            listOf(SyncPayload(emptyList(), listOf(progress()))),
        )

        assertNull(merged.progressEntries.singleOrNull())
    }

    private fun emptyPayload() = SyncPayload(emptyList(), emptyList())

    private fun goal(
        title: String = "Goal",
        updatedAt: Long = 1,
        mutationId: String = "goal-mutation",
    ) = SyncGoal(
        id = "goal-1",
        title = title,
        metricType = "COUNT",
        targetValue = 10,
        unitLabel = "items",
        status = "ACTIVE",
        createdAt = 1,
        updatedAt = updatedAt,
        sortOrder = 1,
        mutationId = mutationId,
    )

    private fun progress(
        updatedAt: Long = 2,
        mutationId: String = "progress-mutation",
    ) = SyncProgressEntry(
        id = "progress-1",
        goalId = "goal-1",
        delta = 5,
        occurredAt = 2,
        createdAt = 2,
        updatedAt = updatedAt,
        mutationId = mutationId,
    )
}
