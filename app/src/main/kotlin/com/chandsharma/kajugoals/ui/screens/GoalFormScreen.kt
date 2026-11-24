package com.chandsharma.kajugoals.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.logic.GoalSuggestion
import com.chandsharma.kajugoals.domain.logic.SmartGoalParser
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.domain.model.GoalRecurrence
import com.chandsharma.kajugoals.ui.format.currencySymbol
import com.chandsharma.kajugoals.ui.format.formatMetricValue
import com.chandsharma.kajugoals.ui.theme.KajuTheme
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private data class MetricChoice(
    val type: GoalMetricType,
    val symbol: String,
    val title: String,
    val example: String,
    val color: Color,
)

private data class QuickStart(
    val symbol: String,
    val title: String,
    val metric: GoalMetricType,
    val target: String,
    val unit: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormScreen(
    goal: Goal?,
    isEditing: Boolean,
    onBack: () -> Unit,
    onSave: (GoalDraft) -> Unit,
) {
    val colors = KajuTheme.extendedColors
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    val metricChoices = listOf(
        MetricChoice(GoalMetricType.COUNT, "123", "Count", "books, steps, sessions", colors.glacier),
        MetricChoice(GoalMetricType.DURATION_MINUTES, "◷", "Time", "minutes or hours", colors.mint),
        MetricChoice(GoalMetricType.PERCENTAGE, "%", "Percentage", "from 0 to 100", colors.periwinkle),
        MetricChoice(GoalMetricType.CURRENCY, "₹", "Money", "save or earn", colors.clay),
        MetricChoice(GoalMetricType.BINARY, "✓", "Done", "one clear finish line", colors.glacier),
    )
    val quickStarts = listOf(
        QuickStart("12", "Read 12 books", GoalMetricType.COUNT, "12", "books"),
        QuickStart("₹", "Save ₹1,00,000", GoalMetricType.CURRENCY, "100000", "INR"),
        QuickStart("10k", "Walk 10,000 steps", GoalMetricType.COUNT, "10000", "steps"),
        QuickStart("✓", "Finish a project", GoalMetricType.BINARY, "1", "done"),
    )

    var title by rememberSaveable(goal?.id) { mutableStateOf(goal?.title.orEmpty()) }
    var metricName by rememberSaveable(goal?.id) {
        mutableStateOf((goal?.metricType ?: GoalMetricType.COUNT).name)
    }
    var unit by rememberSaveable(goal?.id) { mutableStateOf(initialUnit(goal)) }
    var target by rememberSaveable(goal?.id) {
        mutableStateOf(goal?.let(::editableTarget).orEmpty().ifBlank { "10" })
    }
    var customUnit by rememberSaveable(goal?.id) { mutableStateOf(initialCustomUnit(goal)) }
    var deadlineAt by rememberSaveable(goal?.id) { mutableStateOf(goal?.deadlineAt) }
    var reminderAt by rememberSaveable(goal?.id) { mutableStateOf(goal?.reminderAt) }
    var recurrenceName by rememberSaveable(goal?.id) {
        mutableStateOf((goal?.recurrence ?: GoalRecurrence.NONE).name)
    }
    var category by rememberSaveable(goal?.id) { mutableStateOf(goal?.category.orEmpty()) }
    var manualOverride by rememberSaveable(goal?.id) { mutableStateOf(isEditing) }
    var showMeasurementEditor by rememberSaveable(goal?.id) { mutableStateOf(isEditing) }
    var showDetails by rememberSaveable(goal?.id) {
        mutableStateOf(
            isEditing && (
                goal?.deadlineAt != null ||
                    goal?.category?.isNotBlank() == true ||
                    goal?.recurrence != GoalRecurrence.NONE
                ),
        )
    }
    var showDatePicker by rememberSaveable(goal?.id) { mutableStateOf(false) }
    var attemptedSave by rememberSaveable(goal?.id) { mutableStateOf(false) }
    val metric = GoalMetricType.valueOf(metricName)
    val parsedTarget = parseTarget(metric, target, unit)
    val titleError = attemptedSave && title.isBlank()
    val targetError = attemptedSave && parsedTarget == null
    val finalUnit = when {
        metric == GoalMetricType.COUNT && unit == CUSTOM_UNIT -> customUnit.trim().ifBlank { "items" }
        else -> unit
    }
    val canSave = title.isNotBlank() && parsedTarget != null
    val suggestion = if (isEditing) null else SmartGoalParser.infer(title)
    val suggestionApplied = suggestion != null && suggestion.metricType == metric &&
        suggestion.targetValue == parsedTarget && suggestion.unitLabel == finalUnit
    val needsMeasurementEditor = showMeasurementEditor ||
        (title.isNotBlank() && suggestion == null)

    fun applySuggestion(value: GoalSuggestion) {
        metricName = value.metricType.name
        unit = if (
            value.metricType == GoalMetricType.COUNT && value.unitLabel !in standardCountUnits
        ) {
            customUnit = value.unitLabel
            CUSTOM_UNIT
        } else {
            value.unitLabel
        }
        target = editableTarget(value)
        manualOverride = false
        showMeasurementEditor = false
    }

    fun chooseMetric(newMetric: GoalMetricType) {
        metricName = newMetric.name
        unit = defaultUnit(newMetric)
        target = defaultTarget(newMetric)
        manualOverride = true
        showMeasurementEditor = true
    }

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
        bottomBar = {
            Surface(
                modifier = Modifier.imePadding(),
                shadowElevation = 10.dp,
            ) {
                Button(
                    onClick = {
                        attemptedSave = true
                        if (canSave) {
                            onSave(
                                GoalDraft(
                                    title = title.trim(),
                                    metricType = metric,
                                    targetValue = checkNotNull(parsedTarget),
                                    unitLabel = finalUnit,
                                    deadlineAt = deadlineAt,
                                    category = category.trim(),
                                    isPinned = goal?.isPinned == true,
                                    reminderAt = reminderAt,
                                    recurrence = GoalRecurrence.valueOf(recurrenceName),
                                ),
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(if (isEditing) "Save changes" else "Create goal")
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    if (isEditing) "Keep the finish line clear" else "What are you working toward?",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    if (isEditing) {
                        "Change anything without losing your progress history."
                    } else {
                        "Type it naturally. Kaju fills in the measuring details for you."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Quick starts",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(quickStarts) { preset ->
                            QuickStartCard(
                                preset = preset,
                                onClick = {
                                    title = preset.title
                                    metricName = preset.metric.name
                                    target = preset.target
                                    unit = preset.unit
                                    customUnit = ""
                                    manualOverride = false
                                    showMeasurementEditor = false
                                },
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { value ->
                        title = value
                        if (!manualOverride) {
                            SmartGoalParser.infer(value)?.let(::applySuggestion)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("What do you want to achieve?") },
                    placeholder = { Text("Example: Read 12 books") },
                    supportingText = if (titleError) {
                        { Text("Give this goal a name") }
                    } else {
                        null
                    },
                    isError = titleError,
                    singleLine = true,
                )

                if (suggestion != null) {
                    SuggestionCard(
                        suggestion = suggestion,
                        applied = suggestionApplied,
                        onApply = { applySuggestion(suggestion) },
                        onEdit = {
                            manualOverride = true
                            showMeasurementEditor = true
                        },
                    )
                }

                if (needsMeasurementEditor) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            if (suggestion == null && title.isNotBlank()) {
                                "How should this be measured?"
                            } else {
                                "Measure with"
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            metricChoices.forEach { choice ->
                                FilterChip(
                                    selected = choice.type == metric,
                                    onClick = { chooseMetric(choice.type) },
                                    label = { Text(choice.title) },
                                    leadingIcon = { Text(choice.symbol) },
                                )
                            }
                        }
                    }

                    GoalTargetEditor(
                        metric = metric,
                        target = target,
                        onTargetChange = { value ->
                            manualOverride = true
                            target = if (
                                metric == GoalMetricType.CURRENCY ||
                                (metric == GoalMetricType.DURATION_MINUTES && unit == "hours")
                            ) {
                                value.filter { it.isDigit() || it == '.' }.take(16)
                            } else {
                                value.filter(Char::isDigit).take(12)
                            }
                        },
                        unit = unit,
                        onUnitChange = { selected ->
                            manualOverride = true
                            val wasHours = unit == "hours"
                            unit = selected
                            if (metric == GoalMetricType.DURATION_MINUTES) {
                                target = convertTimeTarget(target, wasHours, selected == "hours")
                            }
                        },
                        customUnit = customUnit,
                        onCustomUnitChange = {
                            manualOverride = true
                            customUnit = it
                        },
                        isError = targetError,
                    )
                }

                if (title.isNotBlank()) {
                    OutlinedButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (showDetails) "Hide options" else "More options · deadline, repeat, category")
                    }
                }

                if (showDetails) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Optional details", style = MaterialTheme.typography.titleMedium)
                        Text("Repeat", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(GoalRecurrence.entries) { recurrence ->
                                FilterChip(
                                    selected = recurrenceName == recurrence.name,
                                    onClick = { recurrenceName = recurrence.name },
                                    label = { Text(recurrenceLabel(recurrence)) },
                                )
                            }
                        }
                        if (recurrenceName != GoalRecurrence.NONE.name) {
                            Text(
                                "Progress resets visually each period; the full history is never deleted and missed periods do not break a streak.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(deadlineAt?.let(::formatDeadline) ?: "Choose deadline")
                            }
                            if (deadlineAt != null) {
                                TextButton(onClick = {
                                    deadlineAt = null
                                    reminderAt = null
                                }) { Text("Clear") }
                            }
                        }
                        Text("Category", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categoryChoices) { choice ->
                                FilterChip(
                                    selected = category == choice,
                                    onClick = { category = if (category == choice) "" else choice },
                                    label = { Text(choice) },
                                )
                            }
                        }
                        deadlineAt?.let { deadline ->
                            Text("Reminder", style = MaterialTheme.typography.labelLarge)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = reminderAt == null,
                                        onClick = { reminderAt = null },
                                        label = { Text("Off") },
                                    )
                                }
                                items(reminderChoices) { choice ->
                                    val trigger = reminderTime(deadline, choice.daysBefore)
                                    FilterChip(
                                        selected = reminderAt == trigger,
                                        onClick = {
                                            reminderAt = trigger
                                            if (
                                                Build.VERSION.SDK_INT >= 33 &&
                                                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                                                PackageManager.PERMISSION_GRANTED
                                            ) {
                                                notificationPermissionLauncher.launch(
                                                    Manifest.permission.POST_NOTIFICATIONS,
                                                )
                                            }
                                        },
                                        label = { Text(choice.label) },
                                    )
                                }
                            }
                            Text(
                                "Reminders are best-effort and may arrive a little late when Android is conserving battery.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = deadlineAt,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deadlineAt = datePickerState.selectedDateMillis?.plus(DAY_END_OFFSET)
                        reminderAt = null
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text("Set deadline") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: GoalSuggestion,
    applied: Boolean,
    onApply: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = KajuTheme.extendedColors.mint,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                metricChoicesLabel(suggestion.metricType),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    if (applied) "Understood" else "New suggestion",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${metricTitle(suggestion.metricType)} · " +
                        formatMetricValue(
                            suggestion.metricType,
                            suggestion.targetValue,
                            suggestion.unitLabel,
                        ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextButton(onClick = if (applied) onEdit else onApply) {
                Text(if (applied) "Edit" else "Apply")
            }
        }
    }
}

@Composable
private fun QuickStartCard(
    preset: QuickStart,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(164.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(KajuTheme.extendedColors.glacier, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(preset.symbol, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                preset.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Use template",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MetricChoiceCard(
    choice: MetricChoice,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(112.dp)
            .clickable(role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) choice.color else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(choice.symbol, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(choice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    choice.example,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun GoalTargetEditor(
    metric: GoalMetricType,
    target: String,
    onTargetChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    customUnit: String,
    onCustomUnitChange: (String) -> Unit,
    isError: Boolean,
) {
    when (metric) {
        GoalMetricType.PERCENTAGE -> AutomaticTargetCard("%", "The finish line is automatically 100%")
        GoalMetricType.BINARY -> AutomaticTargetCard("✓", "Tap once to mark this goal complete")
        else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Finish line", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = target,
                onValueChange = onTargetChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        when (metric) {
                            GoalMetricType.CURRENCY -> "Amount"
                            GoalMetricType.DURATION_MINUTES -> "How much time?"
                            else -> "Target"
                        },
                    )
                },
                prefix = if (metric == GoalMetricType.CURRENCY) {
                    { Text(currencySymbol(unit)) }
                } else {
                    null
                },
                suffix = when (metric) {
                    GoalMetricType.DURATION_MINUTES -> ({ Text(if (unit == "hours") "hours" else "minutes") })
                    GoalMetricType.COUNT -> ({ Text(if (unit == CUSTOM_UNIT) customUnit else unit) })
                    else -> null
                },
                supportingText = if (isError) {
                    { Text("Enter a target greater than zero") }
                } else {
                    null
                },
                isError = isError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (metric == GoalMetricType.CURRENCY) {
                        KeyboardType.Decimal
                    } else {
                        KeyboardType.Number
                    },
                ),
            )
            val choices = when (metric) {
                GoalMetricType.COUNT -> countUnits
                GoalMetricType.DURATION_MINUTES -> listOf("minutes", "hours")
                GoalMetricType.CURRENCY -> listOf("INR", "USD", "EUR", "GBP")
                GoalMetricType.PERCENTAGE,
                GoalMetricType.BINARY,
                -> emptyList()
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(choices) { choice ->
                    AssistChip(
                        onClick = { onUnitChange(choice) },
                        label = {
                            Text(
                                when {
                                    choice == CUSTOM_UNIT -> "Custom"
                                    metric == GoalMetricType.CURRENCY -> currencySymbol(choice)
                                    else -> choice
                                },
                            )
                        },
                        leadingIcon = if (unit == choice) ({ Text("✓") }) else null,
                    )
                }
            }
            if (metric == GoalMetricType.COUNT && unit == CUSTOM_UNIT) {
                OutlinedTextField(
                    value = customUnit,
                    onValueChange = onCustomUnitChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Custom unit") },
                    placeholder = { Text("kilometres, glasses, chapters…") },
                    singleLine = true,
                )
            }
        }
    }
}

@Composable
private fun AutomaticTargetCard(symbol: String, message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KajuTheme.extendedColors.mint, RoundedCornerShape(22.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(symbol, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

private fun parseTarget(metric: GoalMetricType, value: String, unit: String): Long? = when (metric) {
    GoalMetricType.PERCENTAGE -> 100L
    GoalMetricType.BINARY -> 1L
    GoalMetricType.CURRENCY -> runCatching {
        BigDecimal(value)
            .setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2)
            .longValueExact()
            .takeIf { it > 0L }
    }.getOrNull()
    GoalMetricType.DURATION_MINUTES -> runCatching {
        BigDecimal(value)
            .multiply(if (unit == "hours") BigDecimal("60") else BigDecimal.ONE)
            .setScale(0, RoundingMode.UNNECESSARY)
            .longValueExact()
            .takeIf { it > 0L }
    }.getOrNull()
    GoalMetricType.COUNT -> value.toLongOrNull()?.takeIf { it > 0L }
}

private fun editableTarget(goal: Goal): String = when (goal.metricType) {
    GoalMetricType.CURRENCY -> BigDecimal(goal.targetValue).movePointLeft(2).stripTrailingZeros().toPlainString()
    GoalMetricType.DURATION_MINUTES -> if (goal.unitLabel == "hours") {
        BigDecimal(goal.targetValue)
            .divide(BigDecimal("60"), 4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    } else {
        goal.targetValue.toString()
    }
    else -> goal.targetValue.toString()
}

private fun editableTarget(suggestion: GoalSuggestion): String = when (suggestion.metricType) {
    GoalMetricType.CURRENCY -> BigDecimal(suggestion.targetValue)
        .movePointLeft(2)
        .stripTrailingZeros()
        .toPlainString()
    GoalMetricType.DURATION_MINUTES -> if (suggestion.unitLabel == "hours") {
        BigDecimal(suggestion.targetValue)
            .divide(BigDecimal("60"), 4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    } else {
        suggestion.targetValue.toString()
    }
    else -> suggestion.targetValue.toString()
}

private fun defaultTarget(metric: GoalMetricType): String = when (metric) {
    GoalMetricType.COUNT -> "10"
    GoalMetricType.DURATION_MINUTES -> "30"
    GoalMetricType.PERCENTAGE -> "100"
    GoalMetricType.CURRENCY -> "10000"
    GoalMetricType.BINARY -> "1"
}

private fun defaultUnit(metric: GoalMetricType): String = when (metric) {
    GoalMetricType.COUNT -> "items"
    GoalMetricType.DURATION_MINUTES -> "minutes"
    GoalMetricType.PERCENTAGE -> "%"
    GoalMetricType.CURRENCY -> "INR"
    GoalMetricType.BINARY -> "done"
}

private fun convertTimeTarget(value: String, wasHours: Boolean, isHours: Boolean): String {
    val amount = runCatching { BigDecimal(value) }.getOrNull() ?: return value
    return when {
        wasHours && !isHours -> amount.multiply(BigDecimal("60")).stripTrailingZeros().toPlainString()
        !wasHours && isHours -> amount.divide(BigDecimal("60"), 2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        else -> value
    }
}

private fun metricTitle(metric: GoalMetricType): String = when (metric) {
    GoalMetricType.COUNT -> "Count"
    GoalMetricType.DURATION_MINUTES -> "Time"
    GoalMetricType.PERCENTAGE -> "Percentage"
    GoalMetricType.CURRENCY -> "Money"
    GoalMetricType.BINARY -> "Done"
}

private fun metricChoicesLabel(metric: GoalMetricType): String = when (metric) {
    GoalMetricType.COUNT -> "123"
    GoalMetricType.DURATION_MINUTES -> "◷"
    GoalMetricType.PERCENTAGE -> "%"
    GoalMetricType.CURRENCY -> "₹"
    GoalMetricType.BINARY -> "✓"
}

private fun recurrenceLabel(recurrence: GoalRecurrence): String = when (recurrence) {
    GoalRecurrence.NONE -> "One time"
    GoalRecurrence.DAILY -> "Daily"
    GoalRecurrence.WEEKLY -> "Weekly"
    GoalRecurrence.MONTHLY -> "Monthly"
}

private fun formatDeadline(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneOffset.UTC)
        .format(Instant.ofEpochMilli(epochMillis))

private const val CUSTOM_UNIT = "__custom__"
private val countUnits = listOf(
    "items",
    "times",
    "steps",
    "books",
    "pages",
    "sessions",
    "km",
    "kg",
    CUSTOM_UNIT,
)
private val standardCountUnits = countUnits - CUSTOM_UNIT
private val categoryChoices = listOf("Health", "Learning", "Money", "Work", "Personal")
private data class ReminderChoice(val label: String, val daysBefore: Long)
private val reminderChoices = listOf(
    ReminderChoice("Due morning", 0),
    ReminderChoice("1 day before", 1),
    ReminderChoice("1 week before", 7),
)

private fun reminderTime(deadlineAt: Long, daysBefore: Long): Long {
    val dueDate = Instant.ofEpochMilli(deadlineAt).atZone(ZoneOffset.UTC).toLocalDate()
    return dueDate.minusDays(daysBefore)
        .atTime(9, 0)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

private const val DAY_END_OFFSET = 86_399_999L

private fun initialUnit(goal: Goal?): String = when {
    goal == null -> defaultUnit(GoalMetricType.COUNT)
    goal.metricType != GoalMetricType.COUNT -> goal.unitLabel
    goal.unitLabel in standardCountUnits -> goal.unitLabel
    else -> CUSTOM_UNIT
}

private fun initialCustomUnit(goal: Goal?): String = goal
    ?.takeIf { it.metricType == GoalMetricType.COUNT && it.unitLabel !in standardCountUnits }
    ?.unitLabel
    .orEmpty()
