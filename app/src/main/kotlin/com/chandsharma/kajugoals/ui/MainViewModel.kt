package com.chandsharma.kajugoals.ui

import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandsharma.kajugoals.data.sync.DriveAuthorizationOutcome
import com.chandsharma.kajugoals.data.sync.DisabledDriveSyncCoordinator
import com.chandsharma.kajugoals.data.sync.DriveSyncCoordinator
import com.chandsharma.kajugoals.data.sync.SyncUiState
import com.chandsharma.kajugoals.data.sync.userFacingSyncMessage
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GoalFilter {
    ACTIVE,
    PAUSED,
    COMPLETED,
    ALL,
}

enum class GoalSort {
    RECENT,
    NAME,
    PROGRESS,
}

data class MainUiState(
    val isLoading: Boolean = true,
    val allGoals: List<Goal> = emptyList(),
    val visibleGoals: List<Goal> = emptyList(),
    val recentProgress: List<ProgressRecord> = emptyList(),
    val filter: GoalFilter = GoalFilter.ACTIVE,
    val sort: GoalSort = GoalSort.RECENT,
    val sync: SyncUiState = SyncUiState(),
) {
    val activeGoals: List<Goal>
        get() = allGoals.filter { it.status == GoalStatus.ACTIVE }

    fun goal(goalId: String): Goal? = allGoals.firstOrNull { it.id == goalId }
}

sealed interface MainEvent {
    data class ProgressLogged(val entryId: String, val goalTitle: String) : MainEvent
    data class GoalSaved(val goalId: String, val isNew: Boolean) : MainEvent
    data class RequestDriveAuthorization(val pendingIntent: PendingIntent) : MainEvent
    data class Message(val text: String) : MainEvent
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val driveSyncCoordinator: DriveSyncCoordinator = DisabledDriveSyncCoordinator,
) : ViewModel() {
    private val filter = MutableStateFlow(GoalFilter.ACTIVE)
    private val sort = MutableStateFlow(GoalSort.RECENT)
    private val _events = MutableSharedFlow<MainEvent>(extraBufferCapacity = 8)

    val events = _events.asSharedFlow()

    val uiState: StateFlow<MainUiState> = combine(
        repository.observeGoals(),
        repository.observeRecentProgress(),
        filter,
        sort,
        driveSyncCoordinator.state,
    ) { goals, progress, selectedFilter, selectedSort, syncState ->
        val filtered = goals.filter { goal ->
            when (selectedFilter) {
                GoalFilter.ACTIVE -> goal.status == GoalStatus.ACTIVE
                GoalFilter.PAUSED -> goal.status == GoalStatus.PAUSED
                GoalFilter.COMPLETED -> goal.status == GoalStatus.COMPLETED
                GoalFilter.ALL -> goal.status != GoalStatus.ARCHIVED
            }
        }
        val sorted = when (selectedSort) {
            GoalSort.RECENT -> filtered.sortedByDescending { it.lastProgressAt ?: it.createdAt }
            GoalSort.NAME -> filtered.sortedBy { it.title.lowercase() }
            GoalSort.PROGRESS -> filtered.sortedByDescending(Goal::progressFraction)
        }
        MainUiState(
            isLoading = false,
            allGoals = goals,
            visibleGoals = sorted,
            recentProgress = progress,
            filter = selectedFilter,
            sort = selectedSort,
            sync = syncState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    init {
        driveSyncCoordinator.requestBackgroundSync()
    }

    fun setFilter(value: GoalFilter) {
        filter.value = value
    }

    fun setSort(value: GoalSort) {
        sort.value = value
    }

    fun saveGoal(goalId: String?, draft: GoalDraft) {
        viewModelScope.launch {
            runCatching {
                if (goalId == null) {
                    repository.createGoal(draft) to true
                } else {
                    repository.updateGoal(goalId, draft)
                    goalId to false
                }
            }.onSuccess { (savedId, isNew) ->
                _events.emit(MainEvent.GoalSaved(savedId, isNew))
            }.onFailure { throwable ->
                _events.emit(MainEvent.Message(throwable.message ?: "Could not save this goal"))
            }
        }
    }

    fun logProgress(goal: Goal, delta: Long, note: String?) {
        viewModelScope.launch {
            runCatching { repository.logProgress(goal.id, delta, note) }
                .onSuccess { entryId ->
                    _events.emit(MainEvent.ProgressLogged(entryId, goal.title))
                }
                .onFailure { throwable ->
                    _events.emit(MainEvent.Message(throwable.message ?: "Could not log progress"))
                }
        }
    }

    fun undoProgress(entryId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteProgress(entryId) }
                .onSuccess { _events.emit(MainEvent.Message("Progress entry removed")) }
                .onFailure { _events.emit(MainEvent.Message("Could not undo that entry")) }
        }
    }

    fun deleteProgress(entryId: String) {
        undoProgress(entryId)
    }

    fun setGoalStatus(goalId: String, status: GoalStatus) {
        viewModelScope.launch {
            runCatching { repository.setGoalStatus(goalId, status) }
                .onFailure { _events.emit(MainEvent.Message("Could not update this goal")) }
        }
    }

    fun connectOrSyncDrive() {
        viewModelScope.launch {
            if (driveSyncCoordinator.state.value.isConnected) {
                driveSyncCoordinator.markSyncing()
            } else {
                driveSyncCoordinator.markConnecting()
            }
            runCatching { driveSyncCoordinator.requestAccess() }
                .onSuccess(::handleAuthorizationOutcome)
                .onFailure { throwable ->
                    val message = throwable.userFacingSyncMessage()
                    driveSyncCoordinator.markError(message)
                    _events.emit(MainEvent.Message(message))
                }
        }
    }

    fun handleDriveAuthorizationResult(intent: Intent?) {
        viewModelScope.launch {
            runCatching { driveSyncCoordinator.resultFromIntent(intent) }
                .onSuccess(::handleAuthorizationOutcome)
                .onFailure {
                    val message = "Google Drive was not connected"
                    driveSyncCoordinator.markError(message)
                    _events.emit(MainEvent.Message(message))
                }
        }
    }

    fun disconnectDrive() {
        driveSyncCoordinator.disconnect()
        _events.tryEmit(MainEvent.Message("Drive sync is off. Your local and Drive data were kept."))
    }

    private fun handleAuthorizationOutcome(outcome: DriveAuthorizationOutcome) {
        when (outcome) {
            is DriveAuthorizationOutcome.NeedsUserConsent -> {
                viewModelScope.launch {
                    _events.emit(MainEvent.RequestDriveAuthorization(outcome.pendingIntent))
                }
            }
            is DriveAuthorizationOutcome.Ready -> syncWithToken(outcome.accessToken)
        }
    }

    private fun syncWithToken(accessToken: String) {
        viewModelScope.launch {
            driveSyncCoordinator.markConnected()
            driveSyncCoordinator.markSyncing()
            runCatching { driveSyncCoordinator.sync(accessToken) }
                .onSuccess {
                    driveSyncCoordinator.markSynced(System.currentTimeMillis())
                    _events.emit(MainEvent.Message("Google Drive is up to date"))
                }
                .onFailure { throwable ->
                    val message = throwable.userFacingSyncMessage()
                    driveSyncCoordinator.markError(message)
                    _events.emit(MainEvent.Message(message))
                }
        }
    }
}
