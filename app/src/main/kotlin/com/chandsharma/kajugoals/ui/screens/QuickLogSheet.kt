package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.ui.format.editableMetricValue
import com.chandsharma.kajugoals.ui.format.formatMetricValue
import com.chandsharma.kajugoals.ui.format.formattedCurrent
import com.chandsharma.kajugoals.ui.format.formattedTarget
import com.chandsharma.kajugoals.ui.format.metricSymbol
import com.chandsharma.kajugoals.ui.format.parseMetricValue
import com.chandsharma.kajugoals.ui.theme.KajuTheme
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogSheet(
    goal: Goal,
    onSave: (delta: Long, note: String?, occurredAt: Long?) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val increments = quickIncrements(goal.metricType)
    val step = increments.firstOrNull() ?: 1L
    var amount by rememberSaveable(goal.id) { mutableLongStateOf(step) }
    var showNote by rememberSaveable(goal.id) { mutableStateOf(false) }
    var note by rememberSaveable(goal.id) { mutableStateOf("") }
    var modeName by rememberSaveable(goal.id) { mutableStateOf(LogMode.ADD.name) }
    var totalText by rememberSaveable(goal.id) {
        mutableStateOf(editableMetricValue(goal.metricType, goal.currentValue, goal.unitLabel))
    }
    var occurredAt by rememberSaveable(goal.id) { mutableStateOf<Long?>(null) }
    var showDatePicker by rememberSaveable(goal.id) { mutableStateOf(false) }
    val isBinary = goal.metricType == GoalMetricType.BINARY
    val mode = LogMode.valueOf(modeName)
    val parsedTotal = parseMetricValue(goal.metricType, totalText, goal.unitLabel)
    val calculatedDelta: Long? = when {
        isBinary -> (goal.targetValue - goal.currentValue).coerceAtLeast(1L)
        mode == LogMode.ADD -> amount
        mode == LogMode.SUBTRACT -> -amount
        else -> parsedTotal?.minus(goal.currentValue)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(KajuTheme.extendedColors.glacier, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    metricSymbol(goal.metricType, goal.unitLabel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(Modifier.weight(1f)) {
                Text("Log progress", style = MaterialTheme.typography.headlineMedium)
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            "${goal.formattedCurrent()} of ${goal.formattedTarget()}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!isBinary) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LogMode.entries.forEach { option ->
                    FilterChip(
                        selected = mode == option,
                        onClick = { modeName = option.name },
                        label = { Text(option.label) },
                    )
                }
            }

            if (mode == LogMode.SET_TOTAL) {
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { value ->
                        totalText = value.filter { it.isDigit() || it == '.' }.take(16)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New total") },
                    supportingText = {
                        Text("Kaju records the correction, so your history stays traceable.")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (
                            goal.metricType == GoalMetricType.CURRENCY ||
                            goal.metricType == GoalMetricType.DURATION_MINUTES
                        ) {
                            KeyboardType.Decimal
                        } else {
                            KeyboardType.Number
                        },
                    ),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(26.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = { amount = (amount - step).coerceAtLeast(step) },
                        enabled = amount > step,
                        modifier = Modifier.size(56.dp),
                    ) {
                        Text("−", style = MaterialTheme.typography.headlineMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (mode == LogMode.ADD) "Add" else "Subtract",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            formatMetricValue(goal.metricType, amount, goal.unitLabel),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { amount = Math.addExact(amount, step) },
                        modifier = Modifier.size(56.dp),
                    ) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    increments.forEach { increment ->
                        SuggestionChip(
                            onClick = { amount = increment },
                            label = {
                                Text(formatMetricValue(goal.metricType, increment, goal.unitLabel))
                            },
                        )
                    }
                }
            }
        } else {
            Text(
                "One tap will mark this goal complete.",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KajuTheme.extendedColors.mint, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onSave(checkNotNull(calculatedDelta), note.ifBlank { null }, occurredAt)
            },
            enabled = calculatedDelta != null && calculatedDelta != 0L,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                when {
                    isBinary -> "Mark complete"
                    mode == LogMode.SET_TOTAL -> "Set total"
                    mode == LogMode.ADD -> "Log +${formatMetricValue(goal.metricType, amount, goal.unitLabel)}"
                    else -> "Log −${formatMetricValue(goal.metricType, amount, goal.unitLabel)}"
                },
            )
        }

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(occurredAt?.let { "On ${formatProgressDay(it)}" } ?: "Logged today · change date")
        }

        if (showNote) {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                minLines = 2,
                maxLines = 4,
            )
        } else {
            TextButton(onClick = { showNote = true }) {
                Text("+ Add a note")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = occurredAt ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        occurredAt = datePickerState.selectedDateMillis?.plus(HALF_DAY_MILLIS)
                        showDatePicker = false
                    },
                ) { Text("Use date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private enum class LogMode(val label: String) {
    ADD("Add"),
    SUBTRACT("Subtract"),
    SET_TOTAL("Set total"),
}

private fun quickIncrements(type: GoalMetricType): List<Long> = when (type) {
    GoalMetricType.COUNT -> listOf(1L, 5L, 10L)
    GoalMetricType.DURATION_MINUTES -> listOf(15L, 30L, 60L)
    GoalMetricType.PERCENTAGE -> listOf(1L, 5L, 10L)
    GoalMetricType.CURRENCY -> listOf(10_000L, 50_000L, 100_000L)
    GoalMetricType.BINARY -> emptyList()
}

internal fun formatProgressDay(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneOffset.UTC)
        .format(Instant.ofEpochMilli(epochMillis))

internal const val HALF_DAY_MILLIS = 43_200_000L
