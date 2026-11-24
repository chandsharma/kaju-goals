package com.chandsharma.kajugoals.data.portable

import android.content.Context
import android.net.Uri
import com.chandsharma.kajugoals.data.local.GoalDao
import com.chandsharma.kajugoals.data.local.GoalEntity
import com.chandsharma.kajugoals.data.local.ProgressEntryEntity
import com.chandsharma.kajugoals.data.repository.SyncScheduler
import com.chandsharma.kajugoals.data.repository.WidgetUpdater
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.model.GoalRecurrence
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class PortableEnvelope(
    val format: String = "kaju-goals",
    val version: Int = 1,
    val exportedAt: Long,
    val goals: List<PortableGoal>,
    val progress: List<PortableProgress>,
)

@Serializable
private data class PortableGoal(
    val id: String,
    val title: String,
    val metricType: String,
    val targetValue: Long,
    val unitLabel: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Long,
    val deadlineAt: Long? = null,
    val category: String = "",
    val isPinned: Boolean = false,
    val reminderAt: Long? = null,
    val recurrence: String = "NONE",
    val mutationId: String,
    val deletedAt: Long? = null,
)

@Serializable
private data class PortableProgress(
    val id: String,
    val goalId: String,
    val delta: Long,
    val note: String? = null,
    val occurredAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val mutationId: String,
    val deletedAt: Long? = null,
)

@Singleton
class PortableDataManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val goalDao: GoalDao,
    private val syncScheduler: SyncScheduler,
    private val widgetUpdater: WidgetUpdater,
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun exportJson(destination: Uri) = withContext(Dispatchers.IO) {
        val envelope = PortableEnvelope(
            exportedAt = System.currentTimeMillis(),
            goals = goalDao.getAllGoals().map { it.toPortable() },
            progress = goalDao.getAllProgress().map { it.toPortable() },
        )
        context.contentResolver.openOutputStream(destination, "wt")?.bufferedWriter()?.use { writer ->
            writer.write(json.encodeToString(envelope))
        } ?: error("The selected file could not be opened")
    }

    suspend fun exportCsv(destination: Uri) = withContext(Dispatchers.IO) {
        val goals = goalDao.getAllGoals()
        val progress = goalDao.getAllProgress()
        val goalById = goals.associateBy(GoalEntity::id)
        context.contentResolver.openOutputStream(destination, "wt")?.bufferedWriter()?.use { writer ->
            writer.appendLine("record_type,id,goal_id,title,metric,value,unit,status,occurred_at,note,deadline_at,category")
            goals.filter { it.deletedAt == null }.forEach { goal ->
                writer.appendLine(
                    csvRow(
                        "goal",
                        goal.id,
                        "",
                        goal.title,
                        goal.metricType.name,
                        goal.targetValue.toString(),
                        goal.unitLabel,
                        goal.status.name,
                        "",
                        "",
                        goal.deadlineAt?.toString().orEmpty(),
                        goal.category,
                    ),
                )
            }
            progress.filter { it.deletedAt == null }.forEach { entry ->
                val goal = goalById[entry.goalId]
                writer.appendLine(
                    csvRow(
                        "progress",
                        entry.id,
                        entry.goalId,
                        goal?.title.orEmpty(),
                        goal?.metricType?.name.orEmpty(),
                        entry.delta.toString(),
                        goal?.unitLabel.orEmpty(),
                        "",
                        entry.occurredAt.toString(),
                        entry.note.orEmpty(),
                        "",
                        goal?.category.orEmpty(),
                    ),
                )
            }
        } ?: error("The selected file could not be opened")
    }

    suspend fun importJson(source: Uri) = withContext(Dispatchers.IO) {
        val raw = context.contentResolver.openInputStream(source)?.bufferedReader()?.use { reader ->
            reader.readText().also { require(it.length <= MAX_IMPORT_CHARACTERS) { "Backup is too large" } }
        } ?: error("The selected file could not be opened")
        val imported = json.decodeFromString<PortableEnvelope>(raw)
        require(imported.format == "kaju-goals" && imported.version == 1) {
            "This is not a supported Kaju Goals backup"
        }
        val importedGoals = imported.goals.map { it.toEntity() }
        importedGoals.forEach { goal ->
            require(goal.id.isNotBlank() && goal.title.isNotBlank() && goal.targetValue > 0L) {
                "Backup contains an invalid goal"
            }
        }
        val mergedGoals = (goalDao.getAllGoals() + importedGoals)
            .groupBy(GoalEntity::id)
            .values
            .map { versions -> versions.maxWith(compareBy({ it.updatedAt }, { it.mutationId })) }
        val goalIds = mergedGoals.mapTo(mutableSetOf(), GoalEntity::id)
        val importedProgress = imported.progress.map { it.toEntity() }
        require(importedProgress.all { it.id.isNotBlank() && it.goalId in goalIds }) {
            "Backup contains progress for an unknown goal"
        }
        val mergedProgress = (goalDao.getAllProgress() + importedProgress)
            .groupBy(ProgressEntryEntity::id)
            .values
            .map { versions -> versions.maxWith(compareBy({ it.updatedAt }, { it.mutationId })) }
            .filter { it.goalId in goalIds }
        goalDao.applyMergedSnapshot(mergedGoals, mergedProgress)
        syncScheduler.requestSync()
        widgetUpdater.refresh()
    }

    private fun csvRow(vararg fields: String): String = fields.joinToString(",") { field ->
        "\"${field.replace("\"", "\"\"")}\""
    }

    private fun GoalEntity.toPortable() = PortableGoal(
        id, title, metricType.name, targetValue, unitLabel, status.name, createdAt, updatedAt,
        sortOrder, deadlineAt, category, isPinned, reminderAt, recurrence.name, mutationId, deletedAt,
    )

    private fun PortableGoal.toEntity() = GoalEntity(
        id = id,
        title = title,
        metricType = GoalMetricType.valueOf(metricType),
        targetValue = targetValue,
        unitLabel = unitLabel,
        status = GoalStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt,
        sortOrder = sortOrder,
        deadlineAt = deadlineAt,
        category = category,
        isPinned = isPinned,
        reminderAt = reminderAt,
        recurrence = GoalRecurrence.valueOf(recurrence),
        mutationId = mutationId,
        deletedAt = deletedAt,
    )

    private fun ProgressEntryEntity.toPortable() = PortableProgress(
        id, goalId, delta, note, occurredAt, createdAt, updatedAt, mutationId, deletedAt,
    )

    private fun PortableProgress.toEntity() = ProgressEntryEntity(
        id, goalId, delta, note, occurredAt, createdAt, updatedAt, mutationId, deletedAt,
    )

    private companion object {
        const val MAX_IMPORT_CHARACTERS = 20_000_000
    }
}
