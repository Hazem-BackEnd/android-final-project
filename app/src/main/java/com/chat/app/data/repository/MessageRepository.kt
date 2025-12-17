package com.chat.app.data.repository

import android.content.Context
import com.chat.app.data.local.AppDatabase
import com.chat.app.data.local.entities.MessageEntity
import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.chat.app.data.remote.firebase.FirebaseChatService
import com.chat.app.data.remote.firebase.NetworkMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class MessageRepository(context: Context) {
    private val messageDao = AppDatabase.getDatabase(context).messageDao()
    private val firebaseService = FirebaseChatService()
    private val authManager = FirebaseAuthManager()
    
    // Get currentUserId dynamically to ensure it's always fresh
    private val currentUserId: String get() = authManager.currentUserId ?: ""

    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun sendMessage(chatId: String, content: String) {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val netMessage = NetworkMessage(
            id = messageId,
            senderId = currentUserId,
            content = content,
            timestamp = timestamp
        )

        firebaseService.sendMessage(chatId, netMessage)
    }


    fun startSyncing(chatId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            firebaseService.listenToMessages(chatId).collect { networkMessages ->
                val entities = networkMessages.map { netMsg ->
                    MessageEntity(
                        messageId = netMsg.id,
                        chatId = chatId,
                        senderId = netMsg.senderId,
                        content = netMsg.content,
                        timestamp = netMsg.timestamp,
                        isFromMe = (netMsg.senderId == currentUserId)
                    )
                }
                entities.forEach { messageDao.insertMessage(it) }
            }
        }
    }
}