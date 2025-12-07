package com.chat.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chats",
        indices = [Index(value = ["timestamp"])]
)

data class ChatEntity(
    @PrimaryKey
    val chatId: String,
    val otherUserId: String,
    val lastMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()

)