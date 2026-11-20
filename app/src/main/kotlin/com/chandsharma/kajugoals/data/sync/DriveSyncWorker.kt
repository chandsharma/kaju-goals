package com.chandsharma.kajugoals.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.chandsharma.kajugoals.data.repository.SyncScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun authorization(): DriveAuthorization
    fun syncEngine(): DriveSyncEngine
    fun stateStore(): SyncStateStore
}

class DriveSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java,
        )
        val state = entryPoint.stateStore()
        if (!state.isConnected()) return Result.success()

        state.markSyncing()
        return try {
            when (val authorization = entryPoint.authorization().requestAccess()) {
                is DriveAuthorizationOutcome.NeedsUserConsent -> {
                    state.markAuthorizationNeeded()
                    Result.success()
                }
                is DriveAuthorizationOutcome.Ready -> {
                    entryPoint.syncEngine().sync(authorization.accessToken)
                    state.markSynced(System.currentTimeMillis())
                    Result.success()
                }
            }
        } catch (exception: Exception) {
            state.markError(exception.userFacingSyncMessage())
            when {
                exception is IOException -> Result.retry()
                exception is DriveApiException && exception.statusCode >= 500 -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}

@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    @ApplicationContext context: Context,
    private val stateStore: SyncStateStore,
) : SyncScheduler {
    private val workManager = WorkManager.getInstance(context)

    override fun requestSync() {
        if (!stateStore.isConnected()) return
        val request = OneTimeWorkRequestBuilder<DriveSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "kaju-drive-sync"
    }
}

fun Throwable.userFacingSyncMessage(): String = when (this) {
    is SyncDataException -> message ?: "The Google Drive backup could not be read safely"
    is DriveApiException -> when (statusCode) {
        401, 403 -> "Google Drive access needs attention"
        else -> "Google Drive sync could not finish"
    }
    is IOException -> "Sync will retry when the connection is stable"
    else -> "Google Drive sync could not finish"
}
