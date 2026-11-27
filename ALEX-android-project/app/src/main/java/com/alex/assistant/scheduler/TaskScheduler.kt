package com.alex.assistant.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class DailyBriefingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // Trigger proactive briefing notification when user enables briefings
        return Result.success()
    }
}

class TaskScheduler(private val context: Context) {
    fun scheduleDailyBriefing(enabled: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork("alex_daily_briefing")
            return
        }
        val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(24, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork(
            "alex_daily_briefing",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
