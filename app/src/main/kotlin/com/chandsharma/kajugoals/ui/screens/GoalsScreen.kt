package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.ui.GoalFilter
import com.chandsharma.kajugoals.ui.GoalSort
import com.chandsharma.kajugoals.ui.MainUiState
import com.chandsharma.kajugoals.ui.components.EmptyStateCard
import com.chandsharma.kajugoals.ui.components.GoalListCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    state: MainUiState,
    onFilterChange: (GoalFilter) -> Unit,
    onSortChange: (GoalSort) -> Unit,
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onEditGoal: (String) -> Unit,
    onLogGoal: (Goal) -> Unit,
    onStatusChange: (String, GoalStatus) -> Unit,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals", style = MaterialTheme.typography.headlineLarge) },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Sort goals")
                        }
                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false },
                        ) {
                            GoalSort.entries.forEach { sort ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (sort) {
                                                GoalSort.RECENT -> "Recently updated"
                                                GoalSort.NAME -> "Name"
                                                GoalSort.PROGRESS -> "Progress"
                                            },
                                        )
                                    },
                                    onClick = {
                                        sortMenuOpen = false
                                        onSortChange(sort)
                                    },
                                    leadingIcon = if (state.sort == sort) {
                                        { Text("✓") }
                                    } else {
                                        null
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGoal) {
                Icon(Icons.Default.Add, contentDescription = "Create goal")
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 12.dp),
                ) {
                    items(GoalFilter.entries) { filter ->
                        FilterChip(
                            selected = state.filter == filter,
                            onClick = { onFilterChange(filter) },
                            label = {
                                Text(filter.name.lowercase().replaceFirstChar(Char::uppercase))
                            },
                        )
                    }
                }
            }
            if (state.visibleGoals.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = if (state.allGoals.isEmpty()) "Your goals, clearly listed" else "Nothing in this view",
                        body = if (state.allGoals.isEmpty()) {
                            "Create a goal and it will stay easy to scan, update, pause, and complete."
                        } else {
                            "Choose another filter or create a new goal."
                        },
                        actionLabel = "Create a goal",
                        onAction = onCreateGoal,
                    )
                }
            } else {
                items(
                    items = state.visibleGoals,
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
                item { Spacer(Modifier.padding(1.dp)) }
            }
        }
    }
}
