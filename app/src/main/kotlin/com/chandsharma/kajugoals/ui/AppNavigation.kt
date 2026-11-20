package com.chandsharma.kajugoals.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.ui.screens.GoalDetailScreen
import com.chandsharma.kajugoals.ui.screens.GoalFormScreen
import com.chandsharma.kajugoals.ui.screens.GoalsScreen
import com.chandsharma.kajugoals.ui.screens.ProgressScreen
import com.chandsharma.kajugoals.ui.screens.QuickLogSheet
import com.chandsharma.kajugoals.ui.screens.TodayScreen
import com.chandsharma.kajugoals.ui.screens.YouScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable

@Serializable
private data object TodayRoute : NavKey

@Serializable
private data object GoalsRoute : NavKey

@Serializable
private data object ProgressRoute : NavKey

@Serializable
private data object YouRoute : NavKey

@Serializable
private data class GoalDetailRoute(val goalId: String) : NavKey

@Serializable
private data class GoalFormRoute(val goalId: String? = null) : NavKey

private enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
) {
    TODAY("Today", Icons.Default.Home),
    GOALS("Goals", Icons.AutoMirrored.Filled.List),
    PROGRESS("Progress", Icons.Default.Favorite),
    YOU("You", Icons.Default.Person),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KajuGoalsApp(
    state: MainUiState,
    viewModel: MainViewModel,
    onExit: () -> Unit,
) {
    val todayStack = rememberNavBackStack(TodayRoute)
    val goalsStack = rememberNavBackStack(GoalsRoute)
    val progressStack = rememberNavBackStack(ProgressRoute)
    val youStack = rememberNavBackStack(YouRoute)
    var selectedDestinationName by rememberSaveable { mutableStateOf(TopLevelDestination.TODAY.name) }
    var quickLogGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val navigationSuiteState = rememberNavigationSuiteScaffoldState()
    val driveAuthorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleDriveAuthorizationResult(result.data)
        } else {
            viewModel.handleDriveAuthorizationResult(null)
        }
    }
    val selectedDestination = TopLevelDestination.valueOf(selectedDestinationName)
    val currentStack = when (selectedDestination) {
        TopLevelDestination.TODAY -> todayStack
        TopLevelDestination.GOALS -> goalsStack
        TopLevelDestination.PROGRESS -> progressStack
        TopLevelDestination.YOU -> youStack
    }

    fun stackFor(destination: TopLevelDestination) = when (destination) {
        TopLevelDestination.TODAY -> todayStack
        TopLevelDestination.GOALS -> goalsStack
        TopLevelDestination.PROGRESS -> progressStack
        TopLevelDestination.YOU -> youStack
    }

    fun selectDestination(destination: TopLevelDestination) {
        val stack = stackFor(destination)
        if (selectedDestination == destination) {
            while (stack.size > 1) stack.removeLastOrNull()
        }
        selectedDestinationName = destination.name
    }

    fun openGoal(goalId: String) {
        currentStack.add(GoalDetailRoute(goalId))
    }

    fun openGoalForm(goalId: String? = null) {
        currentStack.add(GoalFormRoute(goalId))
    }

    fun goBack() {
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (selectedDestination != TopLevelDestination.TODAY) {
            selectedDestinationName = TopLevelDestination.TODAY.name
        } else {
            onExit()
        }
    }

    LaunchedEffect(selectedDestination, currentStack.size) {
        if (currentStack.size == 1) {
            navigationSuiteState.show()
        } else {
            navigationSuiteState.hide()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MainEvent.GoalSaved -> {
                    if (event.isNew) {
                        // A successful save completes the form flow in the tab that launched it.
                        // Otherwise that completed form resurfaces when the user returns to the tab.
                        if (currentStack.lastOrNull() is GoalFormRoute) {
                            currentStack.removeLastOrNull()
                        }
                        selectedDestinationName = TopLevelDestination.GOALS.name
                        while (goalsStack.size > 1) goalsStack.removeLastOrNull()
                        goalsStack.add(GoalDetailRoute(event.goalId))
                    } else if (currentStack.size > 1) {
                        currentStack.removeLastOrNull()
                    }
                }
                is MainEvent.RequestDriveAuthorization -> {
                    driveAuthorizationLauncher.launch(
                        IntentSenderRequest.Builder(event.pendingIntent.intentSender).build(),
                    )
                }
                is MainEvent.ProgressLogged -> {
                    quickLogGoalId = null
                    val result = snackbarHostState.showSnackbar(
                        message = "Progress logged for ${event.goalTitle}",
                        actionLabel = "Undo",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoProgress(event.entryId)
                    }
                }
                is MainEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    NavigationSuiteScaffold(
        state = navigationSuiteState,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = destination == selectedDestination,
                    onClick = { selectDestination(destination) },
                    icon = {
                        Icon(destination.icon, contentDescription = null)
                    },
                    label = { Text(destination.label) },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Box(Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = currentStack,
                onBack = ::goBack,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    fadeIn(tween(FAST_NAV_ENTER_MILLIS)) togetherWith
                        fadeOut(tween(FAST_NAV_EXIT_MILLIS))
                },
                popTransitionSpec = {
                    fadeIn(tween(FAST_NAV_ENTER_MILLIS)) togetherWith
                        fadeOut(tween(FAST_NAV_EXIT_MILLIS))
                },
                predictivePopTransitionSpec = { _ ->
                    fadeIn(tween(FAST_NAV_ENTER_MILLIS)) togetherWith
                        fadeOut(tween(FAST_NAV_EXIT_MILLIS))
                },
                entryProvider = entryProvider {
                    entry<TodayRoute> {
                        TodayScreen(
                            state = state,
                            onCreateGoal = { openGoalForm() },
                            onOpenGoal = ::openGoal,
                            onEditGoal = ::openGoalForm,
                            onLogGoal = { quickLogGoalId = it.id },
                            onStatusChange = viewModel::setGoalStatus,
                        )
                    }
                    entry<GoalsRoute> {
                        GoalsScreen(
                            state = state,
                            onFilterChange = viewModel::setFilter,
                            onSortChange = viewModel::setSort,
                            onCreateGoal = { openGoalForm() },
                            onOpenGoal = ::openGoal,
                            onEditGoal = ::openGoalForm,
                            onLogGoal = { quickLogGoalId = it.id },
                            onStatusChange = viewModel::setGoalStatus,
                        )
                    }
                    entry<ProgressRoute> {
                        ProgressScreen(
                            state = state,
                            onCreateGoal = { openGoalForm() },
                            onOpenGoal = ::openGoal,
                            onDeleteProgress = viewModel::deleteProgress,
                        )
                    }
                    entry<YouRoute> {
                        YouScreen(
                            syncState = state.sync,
                            onConnectOrSync = viewModel::connectOrSyncDrive,
                            onDisconnect = viewModel::disconnectDrive,
                        )
                    }
                    entry<GoalDetailRoute> { route ->
                        val goal = state.goal(route.goalId)
                        GoalDetailScreen(
                            goal = goal,
                            history = state.recentProgress.filter { it.goalId == route.goalId },
                            onBack = ::goBack,
                            onEdit = { openGoalForm(route.goalId) },
                            onLog = { if (goal != null) quickLogGoalId = goal.id },
                            onDeleteProgress = viewModel::deleteProgress,
                        )
                    }
                    entry<GoalFormRoute> { route ->
                        GoalFormScreen(
                            goal = route.goalId?.let(state::goal),
                            isEditing = route.goalId != null,
                            onBack = ::goBack,
                            onSave = { viewModel.saveGoal(route.goalId, it) },
                        )
                    }
                },
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
            )
        }
    }

    val quickLogGoal: Goal? = quickLogGoalId?.let(state::goal)
    if (quickLogGoal != null) {
        ModalBottomSheet(onDismissRequest = { quickLogGoalId = null }) {
            QuickLogSheet(
                goal = quickLogGoal,
                onSave = { delta, note ->
                    viewModel.logProgress(quickLogGoal, delta, note)
                },
            )
        }
    }
}

private const val FAST_NAV_ENTER_MILLIS = 160
private const val FAST_NAV_EXIT_MILLIS = 90
