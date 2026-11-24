package com.chandsharma.kajugoals.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.GoalRecurrence
import com.chandsharma.kajugoals.domain.model.GoalStatus
import com.chandsharma.kajugoals.domain.logic.GoalPaceCalculator
import com.chandsharma.kajugoals.domain.logic.PaceStatus
import com.chandsharma.kajugoals.ui.format.formattedCurrent
import com.chandsharma.kajugoals.ui.format.formattedTarget
import com.chandsharma.kajugoals.ui.format.metricSymbol
import com.chandsharma.kajugoals.ui.theme.KajuTheme

private val LargeCardShape = RoundedCornerShape(32.dp)
private val SmallCardShape = RoundedCornerShape(24.dp)

@Composable
fun ConfirmProgressDeletionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove this progress entry?") },
        text = { Text("The goal total will be recalculated. This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun ProgressRing(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 14f,
) {
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f)
                stateDescription = label
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * clamped,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun GoalHeroCard(
    goal: Goal,
    onOpen: () -> Unit,
    onLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpen,
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, LargeCardShape, ambientColor = MaterialTheme.colorScheme.outlineVariant),
        shape = LargeCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "ONE MOVE THAT MATTERS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgressRing(
                    progress = goal.progressFraction,
                    label = "${goal.progressPercent}%",
                    modifier = Modifier.size(104.dp),
                    strokeWidth = 16f,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${goal.formattedCurrent()} of ${goal.formattedTarget()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    goalPaceLine(goal)?.let { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Button(
                onClick = onLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KajuTheme.extendedColors.graphite,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Log progress")
            }
        }
    }
}

@Composable
fun GoalListCard(
    goal: Goal,
    onOpen: () -> Unit,
    onLog: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (GoalStatus) -> Unit,
    modifier: Modifier = Modifier,
    onPinChange: (Boolean) -> Unit = {},
    onDuplicate: () -> Unit = {},
    selected: Boolean = false,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = SmallCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            CardDefaults.outlinedCardBorder()
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(metricColor(goal.metricType), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = metricSymbol(goal.metricType, goal.unitLabel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val paceLine = goalPaceLine(goal)
                if (goal.isPinned || goal.category.isNotBlank() || paceLine != null) {
                    Text(
                        text = listOfNotNull(
                            "PINNED".takeIf { goal.isPinned },
                            goal.category.takeIf(String::isNotBlank),
                            recurrenceLine(goal.recurrence),
                            paceLine,
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "${goal.formattedCurrent()} of ${goal.formattedTarget()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = { goal.progressFraction },
                    modifier = Modifier
                    .fillMaxWidth()
                        .height(6.dp),
                    strokeCap = StrokeCap.Round,
                )
            }
            FilledTonalIconButton(
                onClick = onLog,
                enabled = goal.status == GoalStatus.ACTIVE && !goal.isComplete,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = if (goal.isComplete) "Goal complete" else "Log progress",
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Goal actions")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(if (goal.isPinned) "Unpin" else "Pin to top") },
                        onClick = {
                            menuOpen = false
                            onPinChange(!goal.isPinned)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            menuOpen = false
                            onDuplicate()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        },
                    )
                    if (goal.status == GoalStatus.ARCHIVED) {
                        DropdownMenuItem(
                            text = { Text("Restore to active") },
                            onClick = {
                                menuOpen = false
                                onStatusChange(GoalStatus.ACTIVE)
                            },
                        )
                    } else {
                        val nextStatus = if (goal.status == GoalStatus.PAUSED) {
                            GoalStatus.ACTIVE
                        } else {
                            GoalStatus.PAUSED
                        }
                        DropdownMenuItem(
                            text = { Text(if (nextStatus == GoalStatus.ACTIVE) "Resume" else "Pause") },
                            onClick = {
                                menuOpen = false
                                onStatusChange(nextStatus)
                            },
                        )
                        if (!goal.isComplete) {
                            DropdownMenuItem(
                                text = { Text("Mark complete") },
                                onClick = {
                                    menuOpen = false
                                    onStatusChange(GoalStatus.COMPLETED)
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = {
                                menuOpen = false
                                onStatusChange(GoalStatus.ARCHIVED)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun metricColor(type: GoalMetricType) = when (type) {
    GoalMetricType.COUNT -> KajuTheme.extendedColors.glacier
    GoalMetricType.DURATION_MINUTES -> KajuTheme.extendedColors.mint
    GoalMetricType.PERCENTAGE -> KajuTheme.extendedColors.periwinkle
    GoalMetricType.CURRENCY -> KajuTheme.extendedColors.clay
    GoalMetricType.BINARY -> KajuTheme.extendedColors.mint
}

private fun goalPaceLine(goal: Goal): String? {
    if (goal.deadlineAt == null) return null
    val pace = GoalPaceCalculator.calculate(goal, System.currentTimeMillis())
    return when (pace.status) {
        PaceStatus.COMPLETE -> "COMPLETE"
        PaceStatus.ON_PACE -> "${pace.daysRemaining} DAYS · ON PACE"
        PaceStatus.BEHIND -> "${pace.daysRemaining} DAYS · NEEDS PACE"
        PaceStatus.OVERDUE -> "DEADLINE PASSED"
        PaceStatus.NO_DEADLINE -> null
    }
}

private fun recurrenceLine(recurrence: GoalRecurrence): String? = when (recurrence) {
    GoalRecurrence.NONE -> null
    GoalRecurrence.DAILY -> "DAILY"
    GoalRecurrence.WEEKLY -> "WEEKLY"
    GoalRecurrence.MONTHLY -> "MONTHLY"
}

@Composable
fun EmptyStateCard(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KajuTheme.extendedColors.glacier, LargeCardShape)
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(
            body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onAction, shape = RoundedCornerShape(16.dp)) {
            Text(actionLabel)
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color, SmallCardShape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.headlineMedium)
    }
}
