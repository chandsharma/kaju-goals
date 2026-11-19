package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.ui.MainUiState
import com.chandsharma.kajugoals.ui.components.EmptyStateCard
import com.chandsharma.kajugoals.ui.components.GoalHeroCard
import com.chandsharma.kajugoals.ui.components.GoalListCard
import com.chandsharma.kajugoals.ui.components.MetricTile
import com.chandsharma.kajugoals.ui.theme.KajuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: MainUiState,
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onEditGoal: (String) -> Unit,
    onLogGoal: (Goal) -> Unit,
    onStatusChange: (String, GoalStatus) -> Unit,
) {
    val hero = state.activeGoals.firstOrNull()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text("KAJU GOALS", style = MaterialTheme.typography.labelLarge)
                        Text("Good morning", style = MaterialTheme.typography.headlineLarge)
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 18.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (hero == null) {
                item {
                    EmptyStateCard(
                        title = "Make progress visible",
                        body = "Create your first goal, choose how it is measured, and log the first move in seconds.",
                        actionLabel = "Create a goal",
                        onAction = onCreateGoal,
                    )
                }
            } else {
                item {
                    GoalHeroCard(
                        goal = hero,
                        onOpen = { onOpenGoal(hero.id) },
                        onLog = { onLogGoal(hero) },
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MetricTile(
                            label = "Active goals",
                            value = state.activeGoals.size.toString(),
                            color = KajuTheme.extendedColors.mint,
                            modifier = Modifier.weight(1f),
                        )
                        MetricTile(
                            label = "Progress logs",
                            value = state.recentProgress.size.toString(),
                            color = KajuTheme.extendedColors.periwinkle,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (state.activeGoals.size > 1) {
                    item {
                        Text("Up next", style = MaterialTheme.typography.titleLarge)
                    }
                    items(
                        items = state.activeGoals.drop(1).take(3),
                        key = Goal::id,
                    ) { goal ->
                        GoalListCard(
                            goal = goal,
                            onOpen = { onOpenGoal(goal.id) },
                            onLog = { onLogGoal(goal) },
                            onEdit = { onEditGoal(goal.id) },
                            onStatusChange = { onStatusChange(goal.id, it) },
                        )
                    }
                }
            }
        }
    }
}
