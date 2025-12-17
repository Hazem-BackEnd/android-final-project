package com.chat.app.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Helper object to schedule and manage WorkManager tasks.
 */
object WorkManagerHelper {

    private const val TAG = "WorkManagerHelper"

    /**
     * Schedule periodic chat sync every 15 minutes.
     * Only runs when internet is available.
     */
    fun scheduleChatSync(context: Context) {
        Log.d(TAG, "📅 Scheduling periodic chat sync...")

        // Constraints: Only run when connected to internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create periodic work request (minimum 15 minutes)
        val syncRequest = PeriodicWorkRequestBuilder<ChatSyncWorker>(
            1, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(ChatSyncWorker.TAG)
            .build()

        // Enqueue unique periodic work (won't duplicate if already scheduled)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ChatSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            syncRequest
        )

        Log.d(TAG, "✅ Chat sync scheduled successfully")
    }

    /**
     * Cancel all scheduled chat sync work.
     */
    fun cancelChatSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ChatSyncWorker.WORK_NAME)
        Log.d(TAG, "🛑 Chat sync cancelled")
    }

    /**
     * Trigger immediate one-time sync (useful for manual refresh).
     */
    fun triggerImmediateSync(context: Context) {
        Log.d(TAG, "⚡ Triggering immediate sync...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateRequest = androidx.work.OneTimeWorkRequestBuilder<ChatSyncWorker>()
            .setConstraints(constraints)
            .addTag("immediate_sync")
            .build()

        WorkManager.getInstance(context).enqueue(immediateRequest)
    }
}
