package com.chat.app.ui.chatdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.local.entities.MessageEntity
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * UI State for ChatDetailsScreen
 */
data class ChatDetailsUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val chatId: String = "",
    val otherUserName: String = "",
    val inputText: String = ""
) {
    // Helper properties for UI logic
    val shouldShowLoading: Boolean get() = isLoading && messages.isEmpty()
    val shouldShowEmpty: Boolean get() = !isLoading && messages.isEmpty() && errorMessage == null
    val shouldShowMessages: Boolean get() = !isLoading && messages.isNotEmpty()
}

class ChatDetailsViewModel(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val chatId: String,
    private val otherUserName: String,
    private val currentUserId: String = "current_user"
): ViewModel() {
    
    // Private mutable state
    private val _uiState = MutableStateFlow(
        ChatDetailsUiState(
            chatId = chatId,
            otherUserName = otherUserName
        )
    )
    // Public read-only state
    val uiState = _uiState.asStateFlow()
    
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    init {
        loadMessages()
        startMessageSyncing()
    }
    
    /**
     * Load messages for this chat from repository
     */
    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                messageRepository.getMessages(chatId)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                    .collect { messageList ->
                        _uiState.value = _uiState.value.copy(
                            messages = messageList.sortedBy { it.timestamp }, // Sort by timestamp
                            isLoading = false,
                            errorMessage = null
                        )
                        println("📱 Loaded ${messageList.size} messages for chat: $chatId")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load messages"
                )
                println("❌ Error loading messages: ${e.message}")
            }
        }
    }
    /**
     * Start syncing messages with Firebase
     */
    private fun startMessageSyncing() {
        try {
            messageRepository.startSyncing(chatId)
            println("🔄 Started message syncing for chat: $chatId")
        } catch (e: Exception) {
            println("❌ Error starting message sync: ${e.message}")
        }
    }
    
    /**
     * Send a new message
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Send message via repository
                messageRepository.sendMessage(chatId, content.trim())
                
                // Clear input text
                _uiState.value = _uiState.value.copy(inputText = "")
                
                println("📤 Message sent: $content")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to send message: ${e.message}"
                )
                println("❌ Error sending message: ${e.message}")
            }
        }
    }
    
    /**
     * Update input text
     */
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Add sample messages for testing
     */
    fun addSampleMessages() {
        viewModelScope.launch {
            try {
                val sampleMessages = listOf(
                    "Hello! How are you doing?",
                    "I'm good, thanks for asking!",
                    "What are you up to today?",
                    "Just working on some projects. You?",
                    "Same here! Let's catch up soon."
                )
                
                sampleMessages.forEachIndexed { index, content ->
                    kotlinx.coroutines.delay(100) // Small delay between messages
                    messageRepository.sendMessage(chatId, content)
                }
                
                println("✅ Sample messages added for chat: $chatId")
            } catch (e: Exception) {
                println("❌ Error adding sample messages: ${e.message}")
            }
        }
    }
    
    /**
     * Format timestamp for display
     */
    fun formatMessageTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }
}