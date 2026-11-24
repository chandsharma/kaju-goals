package com.chandsharma.kajugoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.ProgressRecord
import com.chandsharma.kajugoals.ui.format.editableMetricValue
import com.chandsharma.kajugoals.ui.format.parseMetricValue
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressEditorSheet(
    record: ProgressRecord,
    onSave: (delta: Long, note: String?, occurredAt: Long) -> Unit,
) {
    var isSubtracting by rememberSaveable(record.id) { mutableStateOf(record.delta < 0L) }
    var valueText by rememberSaveable(record.id) {
        mutableStateOf(
            editableMetricValue(record.metricType, record.delta.absoluteValue, record.unitLabel),
        )
    }
    var note by rememberSaveable(record.id) { mutableStateOf(record.note.orEmpty()) }
    var occurredAt by rememberSaveable(record.id) { mutableLongStateOf(record.occurredAt) }
    var showDatePicker by rememberSaveable(record.id) { mutableStateOf(false) }
    val parsedAmount = parseMetricValue(record.metricType, valueText, record.unitLabel)
        ?.takeIf { it > 0L }
    val signedDelta = parsedAmount?.let { if (isSubtracting) -it else it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Edit progress", style = MaterialTheme.typography.headlineMedium)
            Text(
                record.goalTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (record.metricType != GoalMetricType.BINARY) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isSubtracting,
                    onClick = { isSubtracting = false },
                    label = { Text("Added") },
                )
                FilterChip(
                    selected = isSubtracting,
                    onClick = { isSubtracting = true },
                    label = { Text("Subtracted") },
                )
            }
        }

        OutlinedTextField(
            value = valueText,
            onValueChange = { value ->
                valueText = value.filter { it.isDigit() || it == '.' }.take(16)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (
                    record.metricType == GoalMetricType.CURRENCY ||
                    record.metricType == GoalMetricType.DURATION_MINUTES
                ) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Number
                },
            ),
        )

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Date · ${formatProgressDay(occurredAt)}")
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
                onSave(checkNotNull(signedDelta), note.ifBlank { null }, occurredAt)
            },
            enabled = signedDelta != null && signedDelta != 0L,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("Save correction")
        }
    }

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = occurredAt,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        occurredAt = datePickerState.selectedDateMillis?.plus(HALF_DAY_MILLIS)
                            ?: occurredAt
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
