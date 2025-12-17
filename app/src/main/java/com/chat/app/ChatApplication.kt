package com.chat.app

import android.app.Application
import com.chat.app.worker.WorkManagerHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChatApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Schedule periodic chat sync with WorkManager
        WorkManagerHelper.scheduleChatSync(this)
    }
}