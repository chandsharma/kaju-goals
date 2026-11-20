package com.chandsharma.kajugoals.data.sync

import com.chandsharma.kajugoals.data.local.GoalDao
import com.chandsharma.kajugoals.data.local.GoalEntity
import com.chandsharma.kajugoals.data.local.ProgressEntryEntity
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class SyncEnvelope(
    val schemaVersion: Int,
    val generatedAt: Long,
    val deviceId: String,
    val payload: SyncPayload,
    val checksum: String,
)

@Serializable
internal data class SyncPayload(
    val goals: List<SyncGoal>,
    val progressEntries: List<SyncProgressEntry>,
)

@Serializable
internal data class SyncGoal(
    val id: String,
    val title: String,
    val metricType: String,
    val targetValue: Long,
    val unitLabel: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Long,
    val mutationId: String,
    val deletedAt: Long? = null,
)

@Serializable
internal data class SyncProgressEntry(
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

class SyncDataException(message: String) : Exception(message)

@Singleton
class DriveSyncEngine @Inject constructor(
    private val goalDao: GoalDao,
    private val driveApi: DriveApi,
    private val syncStateStore: SyncStateStore,
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun sync(accessToken: String) = withContext(Dispatchers.IO) {
        repeat(MAX_CONFLICT_RETRIES) { attempt ->
            try {
                performSync(accessToken)
                return@withContext
            } catch (exception: DriveApiException) {
                if (exception.statusCode != HTTP_PRECONDITION_FAILED || attempt == MAX_CONFLICT_RETRIES - 1) {
                    throw exception
                }
            }
        }
    }

    private suspend fun performSync(accessToken: String) {
        val localPayload = readLocalPayload()
        val remoteFiles = driveApi.listSnapshots(accessToken)
            .map { driveApi.downloadSnapshot(accessToken, it) }
        val remotePayloads = remoteFiles.map(::decodeAndVerify)
        val merged = SyncMerger.merge(listOf(localPayload) + remotePayloads)

        // Applying the merge before any upload makes an empty fresh install restore cloud data first.
        goalDao.applyMergedSnapshot(
            goals = merged.goals.map { it.toEntity() },
            entries = merged.progressEntries.map { it.toEntity() },
        )

        if (merged.goals.isEmpty() && merged.progressEntries.isEmpty()) return

        val canonicalRemote = remoteFiles.firstOrNull()
        val canonicalPayload = remotePayloads.firstOrNull()
        if (canonicalPayload != null && payloadChecksum(canonicalPayload) == payloadChecksum(merged)) {
            return
        }

        val encoded = encodeEnvelope(merged)
        if (canonicalRemote == null) {
            driveApi.createSnapshot(accessToken, encoded)
        } else {
            driveApi.updateSnapshot(accessToken, canonicalRemote, encoded)
        }
    }

    private suspend fun readLocalPayload(): SyncPayload = SyncPayload(
        goals = goalDao.getAllGoals().map { it.toSync() }.sortedBy(SyncGoal::id),
        progressEntries = goalDao.getAllProgress()
            .map { it.toSync() }
            .sortedBy(SyncProgressEntry::id),
    )

    private fun decodeAndVerify(file: DriveSnapshotFile): SyncPayload {
        val envelope = runCatching { json.decodeFromString<SyncEnvelope>(file.body) }
            .getOrElse { throw SyncDataException("The Google Drive backup could not be read safely") }
        if (envelope.schemaVersion != SYNC_SCHEMA_VERSION) {
            throw SyncDataException("The Google Drive backup uses an unsupported format")
        }
        if (payloadChecksum(envelope.payload) != envelope.checksum) {
            throw SyncDataException("The Google Drive backup did not pass its integrity check")
        }
        return envelope.payload
    }

    private fun encodeEnvelope(payload: SyncPayload): String = json.encodeToString(
        SyncEnvelope(
            schemaVersion = SYNC_SCHEMA_VERSION,
            generatedAt = System.currentTimeMillis(),
            deviceId = syncStateStore.deviceId,
            payload = payload,
            checksum = payloadChecksum(payload),
        ),
    )

    private fun payloadChecksum(payload: SyncPayload): String {
        val bytes = json.encodeToString(payload).toByteArray()
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(separator = "") { "%02x".format(it) }
    }

    private fun GoalEntity.toSync() = SyncGoal(
        id = id,
        title = title,
        metricType = metricType.name,
        targetValue = targetValue,
        unitLabel = unitLabel,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        sortOrder = sortOrder,
        mutationId = mutationId,
        deletedAt = deletedAt,
    )

    private fun ProgressEntryEntity.toSync() = SyncProgressEntry(
        id = id,
        goalId = goalId,
        delta = delta,
        note = note,
        occurredAt = occurredAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mutationId = mutationId,
        deletedAt = deletedAt,
    )

    private fun SyncGoal.toEntity() = GoalEntity(
        id = id,
        title = title,
        metricType = com.chandsharma.kajugoals.domain.model.GoalMetricType.valueOf(metricType),
        targetValue = targetValue,
        unitLabel = unitLabel,
        status = com.chandsharma.kajugoals.domain.model.GoalStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt,
        sortOrder = sortOrder,
        mutationId = mutationId,
        deletedAt = deletedAt,
    )

    private fun SyncProgressEntry.toEntity() = ProgressEntryEntity(
        id = id,
        goalId = goalId,
        delta = delta,
        note = note,
        occurredAt = occurredAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mutationId = mutationId,
        deletedAt = deletedAt,
    )

    private companion object {
        const val SYNC_SCHEMA_VERSION = 1
        const val MAX_CONFLICT_RETRIES = 3
        const val HTTP_PRECONDITION_FAILED = 412
    }
}

internal object SyncMerger {
    fun merge(payloads: List<SyncPayload>): SyncPayload {
        val goals = payloads.flatMap(SyncPayload::goals)
            .groupBy(SyncGoal::id)
            .values
            .map(::newestGoal)
            .sortedBy(SyncGoal::id)
        val goalIds = goals.mapTo(mutableSetOf(), SyncGoal::id)
        val progress = payloads.flatMap(SyncPayload::progressEntries)
            .groupBy(SyncProgressEntry::id)
            .values
            .map(::newestProgress)
            .filter { it.goalId in goalIds }
            .sortedBy(SyncProgressEntry::id)
        return SyncPayload(goals, progress)
    }

    private fun newestGoal(versions: List<SyncGoal>): SyncGoal =
        versions.maxWith(compareBy<SyncGoal>({ it.updatedAt }, { it.mutationId }))

    private fun newestProgress(versions: List<SyncProgressEntry>): SyncProgressEntry =
        versions.maxWith(compareBy<SyncProgressEntry>({ it.updatedAt }, { it.mutationId }))
}
