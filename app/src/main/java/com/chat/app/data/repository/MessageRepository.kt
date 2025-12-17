package com.chat.app.data.repository

import android.content.Context
import com.chat.app.data.local.AppDatabase
import com.chat.app.data.local.entities.ChatEntity // Import this
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
    private val db = AppDatabase.getDatabase(context)
    private val messageDao = db.messageDao()
    private val chatDao = db.chatDao()
    private val userDao = db.userDao()


    private val firebaseService = FirebaseChatService()
    private val authManager = FirebaseAuthManager()

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

        updateLocalChat(chatId, content, timestamp)
    }

    fun startSyncing(chatId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            firebaseService.listenToMessages(chatId).collect { networkMessages ->
                if (networkMessages.isNotEmpty()) {
                    // 1. Insert Messages
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

                    val lastMsg = networkMessages.maxByOrNull { it.timestamp }
                    if (lastMsg != null) {
                        updateLocalChat(chatId, lastMsg.content, lastMsg.timestamp)
                    }
                }
            }
        }
    }


    private suspend fun updateLocalChat(chatId: String, lastMessage: String, timestamp: Long) {
        val ids = chatId.split("_")
        if (ids.size == 2) {
            val otherUserId = if (ids[0] == currentUserId) ids[1] else ids[0]

            val user = userDao.getUser(otherUserId)
            val realName = user?.fullName ?: "Unknown"

            val chatEntity = ChatEntity(
                chatId = chatId,
                otherUserId = otherUserId,
                otherUserName = realName,
                lastMessage = lastMessage,
                timestamp = timestamp
            )
            chatDao.insertChat(chatEntity)
        }
    }

}