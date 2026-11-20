package com.chandsharma.kajugoals.ui.screens

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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chandsharma.kajugoals.domain.model.Goal
import com.chandsharma.kajugoals.domain.model.GoalDraft
import com.chandsharma.kajugoals.domain.model.GoalMetricType
import com.chandsharma.kajugoals.ui.format.currencySymbol
import com.chandsharma.kajugoals.ui.theme.KajuTheme
import java.math.BigDecimal
import java.math.RoundingMode

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
    val metricChoices = listOf(
        MetricChoice(GoalMetricType.COUNT, "123", "Count", "books, steps, sessions", colors.glacier),
        MetricChoice(GoalMetricType.DURATION_MINUTES, "◷", "Time", "minutes or hours", colors.mint),
        MetricChoice(GoalMetricType.PERCENTAGE, "%", "Percentage", "from 0 to 100", colors.periwinkle),
        MetricChoice(GoalMetricType.CURRENCY, "₹", "Money", "save or earn", colors.clay),
        MetricChoice(GoalMetricType.BINARY, "✓", "Done", "one clear finish line", colors.glacier),
    )
    val quickStarts = listOf(
        QuickStart("📚", "Read 12 books", GoalMetricType.COUNT, "12", "books"),
        QuickStart("₹", "Save ₹1,00,000", GoalMetricType.CURRENCY, "100000", "INR"),
        QuickStart("👟", "Walk 10,000 steps", GoalMetricType.COUNT, "10000", "steps"),
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

    fun chooseMetric(newMetric: GoalMetricType) {
        if (newMetric == metric) return
        metricName = newMetric.name
        unit = defaultUnit(newMetric)
        target = defaultTarget(newMetric)
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
            Surface(shadowElevation = 10.dp) {
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
                    if (isEditing) "Keep it clear and easy to log" else "Create a goal in seconds",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    "Only the name and finish line matter. Everything else is optional.",
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
                    onValueChange = { title = it },
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

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Track progress as", style = MaterialTheme.typography.titleLarge)
                    metricChoices.chunked(2).forEach { choices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            choices.forEach { choice ->
                                MetricChoiceCard(
                                    choice = choice,
                                    selected = choice.type == metric,
                                    onClick = { chooseMetric(choice.type) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (choices.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                GoalTargetEditor(
                    metric = metric,
                    target = target,
                    onTargetChange = { value ->
                        target = if (metric == GoalMetricType.CURRENCY) {
                            value.filter { it.isDigit() || it == '.' }.take(16)
                        } else {
                            value.filter(Char::isDigit).take(12)
                        }
                    },
                    unit = unit,
                    onUnitChange = { selected ->
                        val wasHours = unit == "hours"
                        unit = selected
                        if (metric == GoalMetricType.DURATION_MINUTES) {
                            target = convertTimeTarget(target, wasHours, selected == "hours")
                        }
                    },
                    customUnit = customUnit,
                    onCustomUnitChange = { customUnit = it },
                    isError = targetError,
                )
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
    GoalMetricType.DURATION_MINUTES -> value.toLongOrNull()
        ?.takeIf { it > 0L }
        ?.let { if (unit == "hours") Math.multiplyExact(it, 60L) else it }
    GoalMetricType.COUNT -> value.toLongOrNull()?.takeIf { it > 0L }
}

private fun editableTarget(goal: Goal): String = when (goal.metricType) {
    GoalMetricType.CURRENCY -> BigDecimal(goal.targetValue).movePointLeft(2).stripTrailingZeros().toPlainString()
    GoalMetricType.DURATION_MINUTES -> if (goal.unitLabel == "hours") {
        (goal.targetValue / 60L).toString()
    } else {
        goal.targetValue.toString()
    }
    else -> goal.targetValue.toString()
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
    val amount = value.toLongOrNull() ?: return value
    return when {
        wasHours && !isHours -> Math.multiplyExact(amount, 60L).toString()
        !wasHours && isHours -> maxOf(1L, amount / 60L).toString()
        else -> value
    }
}

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
