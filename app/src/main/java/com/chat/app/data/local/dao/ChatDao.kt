package com.chat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chat.app.data.local.entities.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?
    
    // 🔥 NEW: User-specific chat queries
    @Query("SELECT * FROM chats WHERE chatId LIKE '%' || :userId || '%' ORDER BY timestamp DESC")
    fun getChatsForUser(userId: String): Flow<List<ChatEntity>>
    
    @Query("SELECT * FROM chats WHERE (chatId LIKE '%' || :userId || '%') AND (otherUserId LIKE '%' || :searchQuery || '%' OR lastMessage LIKE '%' || :searchQuery || '%') ORDER BY timestamp DESC")
    fun searchChatsForUser(userId: String, searchQuery: String): Flow<List<ChatEntity>>
}