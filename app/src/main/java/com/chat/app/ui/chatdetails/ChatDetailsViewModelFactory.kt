package com.chat.app.ui.chatdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.repository.MessageRepository
import com.chat.app.data.repository.UserRepository

class ChatDetailsViewModelFactory(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatId: String,
    private val otherUserName: String,
    private val otherUserId: String,
    private val currentUserId: String = "current_user"
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatDetailsViewModel::class.java)) {
            return ChatDetailsViewModel(
                chatRepository = chatRepository,
                messageRepository = messageRepository,
                userRepository = userRepository,
                chatId = chatId,
                otherUserName = otherUserName,
                otherUserId = otherUserId,
                currentUserId = currentUserId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}