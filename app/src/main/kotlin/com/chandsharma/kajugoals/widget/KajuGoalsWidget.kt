package com.chandsharma.kajugoals.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.chandsharma.kajugoals.MainActivity
import com.chandsharma.kajugoals.data.local.KajuGoalsDatabase
import com.chandsharma.kajugoals.data.local.periodBoundaries
import com.chandsharma.kajugoals.data.repository.WidgetUpdater
import com.chandsharma.kajugoals.domain.model.GoalStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

private data class WidgetGoal(
    val title: String,
    val current: Long,
    val target: Long,
)

class KajuGoalsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val goal = loadGoal(context)
        provideContent { WidgetContent(goal) }
    }

    private suspend fun loadGoal(context: Context): WidgetGoal? {
        val database = Room.databaseBuilder(
            context,
            KajuGoalsDatabase::class.java,
            "kaju-goals.db",
        ).addMigrations(
            KajuGoalsDatabase.MIGRATION_1_2,
            KajuGoalsDatabase.MIGRATION_2_3,
        ).build()
        return try {
            val boundaries = periodBoundaries()
            database.goalDao().observeGoalSummaries(
                boundaries.dayStart,
                boundaries.weekStart,
                boundaries.monthStart,
            ).first()
                .firstOrNull { row ->
                    row.goal.status == GoalStatus.ACTIVE && row.loggedValue < row.goal.targetValue
                }
                ?.let { row ->
                    WidgetGoal(row.goal.title, row.loggedValue, row.goal.targetValue)
                }
        } finally {
            database.close()
        }
    }
}

@Composable
private fun WidgetContent(goal: WidgetGoal?) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFF2F5F6)))
            .clickable(actionStartActivity<MainActivity>())
            .padding(18.dp),
        verticalAlignment = androidx.glance.layout.Alignment.Vertical.CenterVertically,
        horizontalAlignment = androidx.glance.layout.Alignment.Horizontal.Start,
    ) {
        Text(
            "KAJU GOALS",
            style = TextStyle(
                color = ColorProvider(Color(0xFF5D666B)),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        if (goal == null) {
            Text(
                "Create a goal",
                modifier = GlanceModifier.padding(top = 8.dp),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            )
            Text("Tap to begin", modifier = GlanceModifier.padding(top = 4.dp))
        } else {
            Text(
                goal.title,
                modifier = GlanceModifier.padding(top = 8.dp),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                maxLines = 2,
            )
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(top = 10.dp),
                horizontalAlignment = androidx.glance.layout.Alignment.Horizontal.Start,
                verticalAlignment = androidx.glance.layout.Alignment.Vertical.CenterVertically,
            ) {
                val percent = if (goal.target <= 0L) 0 else {
                    (goal.current.toDouble() / goal.target * 100).roundToInt().coerceIn(0, 100)
                }
                Text(
                    "$percent%",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF087A6B)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    "  ${goal.current} / ${goal.target}",
                    style = TextStyle(color = ColorProvider(Color(0xFF5D666B)), fontSize = 14.sp),
                )
            }
        }
    }
}

class KajuGoalsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KajuGoalsWidget()
}

@Singleton
class GlanceWidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : WidgetUpdater {
    override suspend fun refresh() {
        KajuGoalsWidget().updateAll(context)
    }
}
