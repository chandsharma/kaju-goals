package com.chandsharma.kajugoals.data.sync

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SyncPhase {
    DISCONNECTED,
    IDLE,
    CONNECTING,
    SYNCING,
    NEEDS_AUTHORIZATION,
    ERROR,
}

data class SyncUiState(
    val phase: SyncPhase = SyncPhase.DISCONNECTED,
    val isConnected: Boolean = false,
    val lastSyncedAt: Long? = null,
    val message: String? = null,
)

@Singleton
class SyncStateStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences("drive_sync", Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(readState())

    val state: StateFlow<SyncUiState> = mutableState.asStateFlow()

    val deviceId: String
        get() = preferences.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            preferences.edit { putString(KEY_DEVICE_ID, it) }
        }

    fun isConnected(): Boolean = preferences.getBoolean(KEY_CONNECTED, false)

    fun markConnecting() = update(SyncPhase.CONNECTING, null)

    fun markConnected() {
        preferences.edit { putBoolean(KEY_CONNECTED, true) }
        update(SyncPhase.IDLE, null)
    }

    fun markDisconnected() {
        preferences.edit {
            putBoolean(KEY_CONNECTED, false)
            remove(KEY_LAST_ERROR)
        }
        mutableState.value = SyncUiState()
    }

    fun markSyncing() = update(SyncPhase.SYNCING, null)

    fun markAuthorizationNeeded(message: String = "Reconnect Google Drive to continue syncing") {
        update(SyncPhase.NEEDS_AUTHORIZATION, message)
    }

    fun markSynced(atMillis: Long) {
        preferences.edit {
            putBoolean(KEY_CONNECTED, true)
            putLong(KEY_LAST_SYNCED_AT, atMillis)
            remove(KEY_LAST_ERROR)
        }
        mutableState.value = SyncUiState(
            phase = SyncPhase.IDLE,
            isConnected = true,
            lastSyncedAt = atMillis,
        )
    }

    fun markError(message: String) {
        preferences.edit { putString(KEY_LAST_ERROR, message) }
        update(SyncPhase.ERROR, message)
    }

    private fun update(phase: SyncPhase, message: String?) {
        mutableState.value = SyncUiState(
            phase = phase,
            isConnected = isConnected(),
            lastSyncedAt = preferences.getLong(KEY_LAST_SYNCED_AT, 0L).takeIf { it > 0L },
            message = message,
        )
    }

    private fun readState(): SyncUiState {
        val connected = preferences.getBoolean(KEY_CONNECTED, false)
        val error = preferences.getString(KEY_LAST_ERROR, null)
        return SyncUiState(
            phase = when {
                !connected -> SyncPhase.DISCONNECTED
                error != null -> SyncPhase.ERROR
                else -> SyncPhase.IDLE
            },
            isConnected = connected,
            lastSyncedAt = preferences.getLong(KEY_LAST_SYNCED_AT, 0L).takeIf { it > 0L },
            message = error,
        )
    }

    private companion object {
        const val KEY_CONNECTED = "connected"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_LAST_SYNCED_AT = "last_synced_at"
        const val KEY_LAST_ERROR = "last_error"
    }
}
