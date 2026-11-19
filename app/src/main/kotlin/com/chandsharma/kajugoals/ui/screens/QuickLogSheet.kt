package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.ui.format.formattedCurrent
import com.chandsharma.kajugoals.ui.format.formattedTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogSheet(
    goal: Goal,
    onSave: (delta: Long, note: String?) -> Unit,
) {
    val defaults = when (goal.metricType) {
        GoalMetricType.COUNT -> listOf(1L, 5L, 10L)
        GoalMetricType.DURATION_MINUTES -> listOf(15L, 30L, 60L)
        GoalMetricType.PERCENTAGE -> listOf(1L, 5L, 10L)
        GoalMetricType.BINARY -> emptyList()
    }
    var value by rememberSaveable(goal.id) { mutableStateOf(defaults.firstOrNull()?.toString() ?: "1") }
    var note by rememberSaveable(goal.id) { mutableStateOf("") }
    var attemptedSave by rememberSaveable(goal.id) { mutableStateOf(false) }
    val parsed = value.toLongOrNull()
    val invalid = attemptedSave && (parsed == null || parsed <= 0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Log progress", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text(goal.title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        Text(
            "${goal.formattedCurrent()} of ${goal.formattedTarget()}",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (goal.metricType != GoalMetricType.BINARY) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                defaults.forEach { increment ->
                    SuggestionChip(
                        onClick = { value = increment.toString() },
                        label = { Text("+$increment") },
                    )
                }
            }
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Progress to add") },
                supportingText = if (invalid) {
                    { Text("Enter a value greater than zero") }
                } else {
                    null
                },
                isError = invalid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note (optional)") },
            minLines = 2,
            maxLines = 4,
        )
        Button(
            onClick = {
                attemptedSave = true
                val delta = if (goal.metricType == GoalMetricType.BINARY) {
                    (goal.targetValue - goal.currentValue).coerceAtLeast(1L)
                } else {
                    parsed
                }
                if (delta != null && delta > 0) onSave(delta, note.ifBlank { null })
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (goal.metricType == GoalMetricType.BINARY) "Mark complete" else "Save progress")
        }
    }
}
