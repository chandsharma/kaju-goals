package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    state: MainUiState,
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onDeleteProgress: (String) -> Unit,
    onEditProgress: (ProgressRecord) -> Unit,
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
                WeeklyReviewCard(state.recentProgress)
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
                            IconButton(onClick = { onEditProgress(record) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit progress entry")
                            }
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

@Composable
private fun WeeklyReviewCard(progress: List<ProgressRecord>) {
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val days = (6L downTo 0L).map(today::minusDays)
    val countsByDay = progress
        .filter { record ->
            Instant.ofEpochMilli(record.occurredAt).atZone(zone).toLocalDate() in days
        }
        .groupingBy { record -> Instant.ofEpochMilli(record.occurredAt).atZone(zone).toLocalDate() }
        .eachCount()
    val weekly = progress.filter { record ->
        Instant.ofEpochMilli(record.occurredAt).atZone(zone).toLocalDate() in days
    }
    val activeGoals = weekly.map(ProgressRecord::goalId).distinct().size
    val activeDays = countsByDay.count { it.value > 0 }
    val maxCount = (countsByDay.values.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KajuTheme.extendedColors.periwinkle, RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("This week", style = MaterialTheme.typography.titleLarge)
            Text(
                if (weekly.isEmpty()) {
                    "A fresh week. Your first check-in will appear here."
                } else {
                    "${weekly.size} check-ins · $activeGoals goals · $activeDays active days"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { day ->
                val count = countsByDay[day] ?: 0
                Column(
                    modifier = Modifier.semantics {
                        contentDescription = "$count progress check-ins on $day"
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(count.toString(), style = MaterialTheme.typography.labelSmall)
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(64.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                RoundedCornerShape(10.dp),
                            ),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .height(if (count == 0) 4.dp else (64f * count / maxCount).dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(10.dp),
                                ),
                        )
                    }
                    Text(
                        day.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
