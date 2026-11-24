package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.domain.logic.GoalPaceCalculator
import com.chandsharma.kajugoals.domain.logic.PaceStatus
import com.chandsharma.kajugoals.ui.components.EmptyStateCard
import com.chandsharma.kajugoals.ui.components.ConfirmProgressDeletionDialog
import com.chandsharma.kajugoals.ui.components.ProgressRing
import com.chandsharma.kajugoals.ui.format.formattedCurrent
import com.chandsharma.kajugoals.ui.format.formattedDelta
import com.chandsharma.kajugoals.ui.format.formattedTarget
import com.chandsharma.kajugoals.ui.format.formatMetricValue
import com.chandsharma.kajugoals.ui.format.formatTimestamp
import com.chandsharma.kajugoals.ui.theme.KajuTheme
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goal: Goal?,
    history: List<ProgressRecord>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onLog: () -> Unit,
    onDeleteProgress: (String) -> Unit,
    onEditProgress: (ProgressRecord) -> Unit,
) {
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
                title = { Text("Goal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (goal != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit goal")
                        }
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        if (goal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("This goal is no longer available")
            }
        } else {
            val pace = GoalPaceCalculator.calculate(goal, System.currentTimeMillis())
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    end = 20.dp,
                    bottom = innerPadding.calculateBottomPadding() + 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(goal.title, style = MaterialTheme.typography.headlineLarge)
                }
                if (goal.deadlineAt != null || goal.category.isNotBlank()) {
                    item {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KajuTheme.extendedColors.glacier, RoundedCornerShape(24.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(paceTitle(pace.status), style = MaterialTheme.typography.titleMedium)
                            goal.deadlineAt?.let { deadline ->
                                Text(
                                    pace.daysRemaining?.let { "$it days left · due ${formatProgressDay(deadline)}" }
                                        ?: "Due ${formatProgressDay(deadline)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            pace.remainingPerWeek?.takeIf { it > 0.0 }?.let { weekly ->
                                Text(
                                    "About ${formatMetricValue(goal.metricType, ceil(weekly).toLong(), goal.unitLabel)} per week from here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (goal.category.isNotBlank()) {
                                Text(
                                    goal.category,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                if (goal.metricType != com.chandsharma.kajugoals.domain.model.GoalMetricType.BINARY) {
                    item { GoalMilestones(goal.progressPercent) }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(22.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ProgressRing(
                                    progress = goal.progressFraction,
                                    label = "${goal.progressPercent}%",
                                    modifier = Modifier.size(128.dp),
                                    strokeWidth = 18f,
                                )
                                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                                    Text("Progress", style = MaterialTheme.typography.labelLarge)
                                    Text(goal.formattedCurrent(), style = MaterialTheme.typography.headlineMedium)
                                    Text(
                                        "Target ${goal.formattedTarget()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Button(
                                onClick = onLog,
                                enabled = !goal.isComplete,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Text(if (goal.isComplete) "Goal complete" else "Log progress")
                            }
                        }
                    }
                }
                item {
                    Text("History", style = MaterialTheme.typography.titleLarge)
                }
                if (history.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No progress logged yet",
                            body = "Your first update will appear here with its exact time and value.",
                            actionLabel = "Log progress",
                            onAction = onLog,
                        )
                    }
                } else {
                    items(history, key = ProgressRecord::id) { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                                Text(record.formattedDelta(), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    formatTimestamp(record.occurredAt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                record.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            }
                            IconButton(onClick = { onEditProgress(record) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit progress entry")
                            }
                            IconButton(onClick = { pendingDeleteId = record.id }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete progress entry")
                            }
                        }
                    }
                }
                item {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KajuTheme.extendedColors.mint, RoundedCornerShape(24.dp))
                            .padding(18.dp),
                    ) {
                        Text("Every entry stays traceable", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Remove a mistaken entry here, or use Undo immediately after logging.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

private fun paceTitle(status: PaceStatus): String = when (status) {
    PaceStatus.NO_DEADLINE -> "No deadline"
    PaceStatus.COMPLETE -> "Finish line reached"
    PaceStatus.ON_PACE -> "On pace"
    PaceStatus.BEHIND -> "A little more pace needed"
    PaceStatus.OVERDUE -> "Deadline passed"
}

@Composable
private fun GoalMilestones(progressPercent: Int) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Milestones", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf(25, 50, 75, 100).forEach { milestone ->
                val reached = progressPercent >= milestone
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (reached) KajuTheme.extendedColors.mint else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                androidx.compose.foundation.shape.CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (reached) "✓" else "$milestone", style = MaterialTheme.typography.labelLarge)
                    }
                    Text("$milestone%", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
