package com.chandsharma.kajugoals.data.reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.chandsharma.kajugoals.MainActivity
import com.chandsharma.kajugoals.data.repository.GoalReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class GoalReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : Worker(appContext, workerParameters) {
    override fun doWork(): Result {
        val goalId = inputData.getString(KEY_GOAL_ID) ?: return Result.failure()
        val goalTitle = inputData.getString(KEY_GOAL_TITLE) ?: return Result.failure()
        createChannel()
        val openApp = PendingIntent.getActivity(
            applicationContext,
            goalId.hashCode(),
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val publicNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Kaju Goals")
            .setContentText("A goal is ready for your attention")
            .build()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Goal reminder")
            .setContentText(goalTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(goalTitle))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .build()
        val canPost = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (canPost) {
            try {
                NotificationManagerCompat.from(applicationContext)
                    .notify(goalId.hashCode(), notification)
            } catch (_: SecurityException) {
                // Permission may be revoked between the explicit check and posting.
            }
        }
        return Result.success()
    }

    private fun createChannel() {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Goal reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Optional reminders chosen for individual goals"
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            },
        )
    }

    companion object {
        const val KEY_GOAL_ID = "goal_id"
        const val KEY_GOAL_TITLE = "goal_title"
        const val CHANNEL_ID = "goal_reminders"
    }
}

@Singleton
class WorkManagerGoalReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : GoalReminderScheduler {
    override fun schedule(goalId: String, goalTitle: String, triggerAt: Long) {
        val workManager = WorkManager.getInstance(context)
        val delay = triggerAt - System.currentTimeMillis()
        if (delay <= 0L) {
            workManager.cancelUniqueWork(workName(goalId))
            return
        }
        val input = Data.Builder()
            .putString(GoalReminderWorker.KEY_GOAL_ID, goalId)
            .putString(GoalReminderWorker.KEY_GOAL_TITLE, goalTitle)
            .build()
        val request = OneTimeWorkRequestBuilder<GoalReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .addTag(REMINDER_TAG)
            .build()
        workManager.enqueueUniqueWork(
            workName(goalId),
            androidx.work.ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override fun cancel(goalId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(goalId))
    }

    private fun workName(goalId: String) = "goal-reminder-$goalId"

    private companion object {
        const val REMINDER_TAG = "goal-reminder"
    }
}
