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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.ui.MainUiState
import com.chandsharma.kajugoals.ui.components.EmptyStateCard
import com.chandsharma.kajugoals.ui.components.ConfirmProgressDeletionDialog
import com.chandsharma.kajugoals.ui.components.MetricTile
import com.chandsharma.kajugoals.ui.format.formattedDelta
import com.chandsharma.kajugoals.ui.format.formatTimestamp
import com.chandsharma.kajugoals.ui.theme.KajuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    state: MainUiState,
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onDeleteProgress: (String) -> Unit,
) {
    val completed = state.allGoals.count { it.status == GoalStatus.COMPLETED }
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }

    pendingDeleteId?.let { entryId ->
        ConfirmProgressDeletionDialog(
            onDismiss = { pendingDeleteId = null },
            onConfirm = {
                pendingDeleteId = null
                onDeleteProgress(entryId)
            },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress", style = MaterialTheme.typography.headlineLarge) },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricTile(
                        label = "Logs",
                        value = state.recentProgress.size.toString(),
                        color = KajuTheme.extendedColors.glacier,
                        modifier = Modifier.weight(1f),
                    )
                    MetricTile(
                        label = "Complete",
                        value = completed.toString(),
                        color = KajuTheme.extendedColors.mint,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                Text("History", style = MaterialTheme.typography.titleLarge)
            }
            if (state.recentProgress.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Progress will collect here",
                        body = "Each log becomes an exact, reviewable part of your history—not just a chart point.",
                        actionLabel = "Create a goal",
                        onAction = onCreateGoal,
                    )
                }
            } else {
                items(
                    items = state.recentProgress,
                    key = ProgressRecord::id,
                ) { record ->
                    Card(
                        onClick = { onOpenGoal(record.goalId) },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                                Text(record.goalTitle, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    formatTimestamp(record.occurredAt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                record.note?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Text(record.formattedDelta(), style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { pendingDeleteId = record.id }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete progress entry")
                            }
                        }
                    }
                }
            }
        }
    }
}
