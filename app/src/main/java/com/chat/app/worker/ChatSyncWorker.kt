package com.chat.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chat.app.data.local.AppDatabase
import com.chat.app.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * WorkManager Worker that syncs chats from Firebase to local Room database.
 * Runs periodically in the background even when app is closed.
 */
class ChatSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "ChatSyncWorker"
        const val WORK_NAME = "chat_sync_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "🔄 Starting chat sync...")

        return try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            
            if (currentUserId == null) {
                Log.d(TAG, "⚠️ No user logged in, skipping sync")
                return Result.success()
            }

            // Get repository and trigger sync
            val chatRepository = ChatRepository(applicationContext)
            chatRepository.startSyncingAllChats(currentUserId)

            Log.d(TAG, "✅ Chat sync completed successfully")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Chat sync failed: ${e.message}")
            
            // Retry if failed (max 3 times by default)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
