package com.chandsharma.kajugoals.data.sync

import android.content.Intent
import com.chandsharma.kajugoals.data.repository.SyncScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface DriveSyncCoordinator {
    val state: StateFlow<SyncUiState>

    fun requestBackgroundSync()
    fun markConnecting()
    fun markSyncing()
    fun markConnected()
    fun disconnect()
    fun markSynced(atMillis: Long)
    fun markError(message: String)

    suspend fun requestAccess(): DriveAuthorizationOutcome
    fun resultFromIntent(intent: Intent?): DriveAuthorizationOutcome
    suspend fun sync(accessToken: String)
}

@Singleton
class DefaultDriveSyncCoordinator @Inject constructor(
    private val authorization: DriveAuthorization,
    private val engine: DriveSyncEngine,
    private val stateStore: SyncStateStore,
    private val scheduler: SyncScheduler,
) : DriveSyncCoordinator {
    override val state: StateFlow<SyncUiState> = stateStore.state

    override fun requestBackgroundSync() = scheduler.requestSync()
    override fun markConnecting() = stateStore.markConnecting()
    override fun markSyncing() = stateStore.markSyncing()
    override fun markConnected() = stateStore.markConnected()
    override fun disconnect() = stateStore.markDisconnected()
    override fun markSynced(atMillis: Long) = stateStore.markSynced(atMillis)
    override fun markError(message: String) = stateStore.markError(message)
    override suspend fun requestAccess() = authorization.requestAccess()
    override fun resultFromIntent(intent: Intent?) = authorization.resultFromIntent(intent)
    override suspend fun sync(accessToken: String) = engine.sync(accessToken)
}

object DisabledDriveSyncCoordinator : DriveSyncCoordinator {
    override val state: StateFlow<SyncUiState> = MutableStateFlow(SyncUiState())

    override fun requestBackgroundSync() = Unit
    override fun markConnecting() = Unit
    override fun markSyncing() = Unit
    override fun markConnected() = Unit
    override fun disconnect() = Unit
    override fun markSynced(atMillis: Long) = Unit
    override fun markError(message: String) = Unit
    override suspend fun requestAccess(): DriveAuthorizationOutcome =
        error("Google Drive sync is unavailable")
    override fun resultFromIntent(intent: Intent?): DriveAuthorizationOutcome =
        error("Google Drive sync is unavailable")
    override suspend fun sync(accessToken: String) = Unit
}
