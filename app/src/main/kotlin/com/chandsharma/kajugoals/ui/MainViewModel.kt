package com.chandsharma.kajugoals.ui

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandsharma.kajugoals.data.sync.DriveAuthorizationOutcome
import com.chandsharma.kajugoals.data.sync.DisabledDriveSyncCoordinator
import com.chandsharma.kajugoals.data.sync.DriveSyncCoordinator
import com.chandsharma.kajugoals.data.sync.SyncUiState
import com.chandsharma.kajugoals.data.sync.userFacingSyncMessage
import com.chandsharma.kajugoals.data.portable.PortableDataManager
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
    ARCHIVED,
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
    val searchQuery: String = "",
    val sync: SyncUiState = SyncUiState(),
) {
    val activeGoals: List<Goal>
        get() = allGoals.filter { it.status == GoalStatus.ACTIVE }

    fun goal(goalId: String): Goal? = allGoals.firstOrNull { it.id == goalId }
}

sealed interface MainEvent {
    data class ProgressLogged(
        val entryId: String,
        val goalTitle: String,
        val completed: Boolean,
    ) : MainEvent
    data class GoalSaved(val goalId: String, val isNew: Boolean) : MainEvent
    data class RequestDriveAuthorization(val pendingIntent: PendingIntent) : MainEvent
    data class Message(val text: String) : MainEvent
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val driveSyncCoordinator: DriveSyncCoordinator = DisabledDriveSyncCoordinator,
    private val portableDataManager: PortableDataManager? = null,
) : ViewModel() {
    private val filter = MutableStateFlow(GoalFilter.ACTIVE)
    private val sort = MutableStateFlow(GoalSort.RECENT)
    private val searchQuery = MutableStateFlow("")
    private val _events = MutableSharedFlow<MainEvent>(extraBufferCapacity = 8)

    val events = _events.asSharedFlow()

    private val listPreferences = combine(filter, sort, searchQuery, ::GoalListPreferences)

    val uiState: StateFlow<MainUiState> = combine(
        repository.observeGoals(),
        repository.observeRecentProgress(),
        listPreferences,
        driveSyncCoordinator.state,
    ) { goals, progress, preferences, syncState ->
        val filtered = goals.filter { goal ->
            val matchesFilter = when (preferences.filter) {
                GoalFilter.ACTIVE -> goal.status == GoalStatus.ACTIVE
                GoalFilter.PAUSED -> goal.status == GoalStatus.PAUSED
                GoalFilter.COMPLETED -> goal.status == GoalStatus.COMPLETED
                GoalFilter.ARCHIVED -> goal.status == GoalStatus.ARCHIVED
                GoalFilter.ALL -> goal.status != GoalStatus.ARCHIVED
            }
            val query = preferences.searchQuery.trim()
            matchesFilter && (
                query.isBlank() ||
                    goal.title.contains(query, ignoreCase = true) ||
                    goal.category.contains(query, ignoreCase = true)
                )
        }
        val sortedWithinPins = when (preferences.sort) {
            GoalSort.RECENT -> filtered.sortedByDescending { it.lastProgressAt ?: it.createdAt }
            GoalSort.NAME -> filtered.sortedBy { it.title.lowercase() }
            GoalSort.PROGRESS -> filtered.sortedByDescending(Goal::progressFraction)
        }
        val sorted = sortedWithinPins.sortedByDescending(Goal::isPinned)
        MainUiState(
            isLoading = false,
            allGoals = goals,
            visibleGoals = sorted,
            recentProgress = progress,
            filter = preferences.filter,
            sort = preferences.sort,
            searchQuery = preferences.searchQuery,
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

    fun setSearchQuery(value: String) {
        searchQuery.value = value
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

    fun logProgress(goal: Goal, delta: Long, note: String?, occurredAt: Long? = null) {
        viewModelScope.launch {
            runCatching { repository.logProgress(goal.id, delta, note, occurredAt) }
                .onSuccess { entryId ->
                    _events.emit(
                        MainEvent.ProgressLogged(
                            entryId = entryId,
                            goalTitle = goal.title,
                            completed = goal.currentValue + delta >= goal.targetValue,
                        ),
                    )
                }
                .onFailure { throwable ->
                    _events.emit(MainEvent.Message(throwable.message ?: "Could not log progress"))
                }
        }
    }

    fun updateProgress(entry: ProgressRecord, delta: Long, note: String?, occurredAt: Long) {
        viewModelScope.launch {
            runCatching { repository.updateProgress(entry.id, delta, note, occurredAt) }
                .onSuccess { _events.emit(MainEvent.Message("Progress updated")) }
                .onFailure { throwable ->
                    _events.emit(MainEvent.Message(throwable.message ?: "Could not update progress"))
                }
        }
    }

    fun setGoalTotal(goal: Goal, total: Long, note: String?, occurredAt: Long? = null) {
        val delta = total - goal.currentValue
        if (delta == 0L) {
            _events.tryEmit(MainEvent.Message("That total is already current"))
            return
        }
        logProgress(goal, delta, note ?: "Set total", occurredAt)
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

    fun setGoalPinned(goalId: String, isPinned: Boolean) {
        viewModelScope.launch {
            runCatching { repository.setGoalPinned(goalId, isPinned) }
                .onFailure { _events.emit(MainEvent.Message("Could not update this goal")) }
        }
    }

    fun duplicateGoal(goal: Goal) {
        saveGoal(
            goalId = null,
            draft = GoalDraft(
                title = "${goal.title} copy",
                metricType = goal.metricType,
                targetValue = goal.targetValue,
                unitLabel = goal.unitLabel,
                deadlineAt = goal.deadlineAt,
                category = goal.category,
                recurrence = goal.recurrence,
            ),
        )
    }

    fun archiveGoals(goalIds: Set<String>) {
        goalIds.forEach { setGoalStatus(it, GoalStatus.ARCHIVED) }
    }

    fun exportJson(destination: Uri) {
        viewModelScope.launch {
            val manager = portableDataManager
            if (manager == null) {
                _events.emit(MainEvent.Message("Portable backup is unavailable"))
                return@launch
            }
            runCatching { manager.exportJson(destination) }
                .onSuccess { _events.emit(MainEvent.Message("JSON backup saved")) }
                .onFailure { _events.emit(MainEvent.Message(it.message ?: "Could not save backup")) }
        }
    }

    fun exportCsv(destination: Uri) {
        viewModelScope.launch {
            val manager = portableDataManager
            if (manager == null) {
                _events.emit(MainEvent.Message("CSV export is unavailable"))
                return@launch
            }
            runCatching { manager.exportCsv(destination) }
                .onSuccess { _events.emit(MainEvent.Message("CSV export saved")) }
                .onFailure { _events.emit(MainEvent.Message(it.message ?: "Could not save export")) }
        }
    }

    fun importJson(source: Uri) {
        viewModelScope.launch {
            val manager = portableDataManager
            if (manager == null) {
                _events.emit(MainEvent.Message("Portable restore is unavailable"))
                return@launch
            }
            runCatching { manager.importJson(source) }
                .onSuccess { _events.emit(MainEvent.Message("Backup merged safely")) }
                .onFailure { _events.emit(MainEvent.Message(it.message ?: "Could not restore backup")) }
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

private data class GoalListPreferences(
    val filter: GoalFilter,
    val sort: GoalSort,
    val searchQuery: String,
)
