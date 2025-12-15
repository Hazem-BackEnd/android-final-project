package com.chat.app.data.repository

import android.content.Context
import com.chat.app.data.local.AppDatabase
import com.chat.app.data.local.entities.ChatEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(context: Context) {
    private val chatDao = AppDatabase.getDatabase(context).chatDao()

    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()

    suspend fun createOrUpdateChat(chat: ChatEntity) {
        chatDao.insertChat(chat)
    }

    suspend fun getChat(chatId: String): ChatEntity? {
        return chatDao.getChatById(chatId)
    }
    
    // 🔥 NEW: User-specific methods
    fun getChatsForUser(userId: String): Flow<List<ChatEntity>> {
        return chatDao.getChatsForUser(userId)
    }
    
    fun searchChatsForUser(userId: String, searchQuery: String): Flow<List<ChatEntity>> {
        return chatDao.searchChatsForUser(userId, searchQuery)
    }
}