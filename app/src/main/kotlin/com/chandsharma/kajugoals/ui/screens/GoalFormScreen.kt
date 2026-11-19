package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalMetricType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormScreen(
    goal: Goal?,
    isEditing: Boolean,
    onBack: () -> Unit,
    onSave: (GoalDraft) -> Unit,
) {
    var title by rememberSaveable(goal?.id) { mutableStateOf(goal?.title.orEmpty()) }
    var metricName by rememberSaveable(goal?.id) {
        mutableStateOf((goal?.metricType ?: GoalMetricType.COUNT).name)
    }
    var target by rememberSaveable(goal?.id) {
        mutableStateOf(goal?.targetValue?.toString() ?: "10")
    }
    var unit by rememberSaveable(goal?.id) { mutableStateOf(goal?.unitLabel.orEmpty()) }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    val metric = GoalMetricType.valueOf(metricName)
    val parsedTarget = target.toLongOrNull()
    val titleError = attemptedSave && title.isBlank()
    val targetError = attemptedSave && (parsedTarget == null || parsedTarget <= 0)
    val canSave = title.isNotBlank() && parsedTarget != null && parsedTarget > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit goal" else "New goal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "What do you want to make true?",
                style = MaterialTheme.typography.headlineLarge,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Goal name") },
                supportingText = if (titleError) {
                    { Text("Enter a name") }
                } else {
                    { Text("Keep it specific and recognizable") }
                },
                isError = titleError,
                singleLine = true,
            )
            Text("How will you measure it?", style = MaterialTheme.typography.titleLarge)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalMetricType.entries.chunked(2).forEach { rowMetrics ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowMetrics.forEach { option ->
                            FilterChip(
                                selected = metric == option,
                                onClick = {
                                    metricName = option.name
                                    when (option) {
                                        GoalMetricType.PERCENTAGE -> target = "100"
                                        GoalMetricType.BINARY -> target = "1"
                                        else -> if (target == "1" || target == "100") target = "10"
                                    }
                                    unit = when (option) {
                                        GoalMetricType.DURATION_MINUTES -> "minutes"
                                        GoalMetricType.PERCENTAGE -> "%"
                                        GoalMetricType.BINARY -> "done"
                                        GoalMetricType.COUNT -> ""
                                    }
                                },
                                label = {
                                    Text(
                                        when (option) {
                                            GoalMetricType.COUNT -> "Count"
                                            GoalMetricType.DURATION_MINUTES -> "Time"
                                            GoalMetricType.PERCENTAGE -> "Percentage"
                                            GoalMetricType.BINARY -> "Done / not done"
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = target,
                onValueChange = { target = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Target") },
                supportingText = if (targetError) {
                    { Text("Enter a target greater than zero") }
                } else {
                    null
                },
                isError = targetError,
                enabled = metric != GoalMetricType.PERCENTAGE && metric != GoalMetricType.BINARY,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            if (metric == GoalMetricType.COUNT) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Unit (optional)") },
                    supportingText = { Text("Examples: pages, sessions, kilometres") },
                    singleLine = true,
                )
            }
            Button(
                onClick = {
                    attemptedSave = true
                    if (canSave) {
                        onSave(
                            GoalDraft(
                                title = title,
                                metricType = metric,
                                targetValue = checkNotNull(parsedTarget),
                                unitLabel = when (metric) {
                                    GoalMetricType.DURATION_MINUTES -> "minutes"
                                    GoalMetricType.PERCENTAGE -> "%"
                                    GoalMetricType.BINARY -> "done"
                                    GoalMetricType.COUNT -> unit
                                },
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isEditing) "Save changes" else "Create goal")
            }
        }
    }
}
