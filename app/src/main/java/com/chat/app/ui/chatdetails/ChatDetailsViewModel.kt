package com.chat.app.ui.chatdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.local.entities.ChatEntity
import com.chat.app.data.local.entities.MessageEntity
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.chat.app.data.remote.firebase.FirebaseChatService
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.repository.MessageRepository
import com.chat.app.data.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val authManager: FirebaseAuthManager,
    private val otherUserName: String,
    private val otherUserId: String
): ViewModel() {
    
    // Get current user ID from Firebase Auth
    private val currentUserId: String = authManager.currentUserId ?: ""
    
    // Generate chatId using FirebaseChatService (min/max logic)
    private val firebaseChatService = FirebaseChatService()
    private val chatId: String = firebaseChatService.getChatId(currentUserId, otherUserId)
    
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
        // Flow when user presses on contact:
        // Step 1: createOrUpdateChat(user:UserEntity)
        initializeChat()
        // Step 2: getMessages(chatId)
        loadMessages()
        // Step 3: startSync
        startMessageSyncing()
    }
    
    /**
     * Step 1: createOrUpdateChat(user:UserEntity)
     */
    private fun initializeChat() {
        viewModelScope.launch {
            try {
                // Create UserEntity from contact data
                val otherUser = UserEntity(
                    uid = otherUserId,
                    fullName = otherUserName,
                    phoneNumber = otherUserId
                )
                
                // Call createOrUpdateChat with UserEntity
                chatRepository.createOrUpdateChat(ChatEntity(
                    chatId = chatId,
                    otherUserId = otherUserId,
                    lastMessage = null,
                    timestamp = System.currentTimeMillis()
                ))
                
                // Save user locally
                userRepository.saveUserLocally(otherUser)
                
            } catch (e: Exception) {
                println("❌ Error initializing chat: ${e.message}")
            }
        }
    }
    
    /**
     * Step 2: getMessages(chatId)
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
                            messages = messageList.sortedBy { it.timestamp },
                            isLoading = false,
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load messages"
                )
            }
        }
    }
    /**
     * Step 3: startSync
     */
    private fun startMessageSyncing() {
        try {
            messageRepository.startSyncing(chatId)
        } catch (e: Exception) {
            println("❌ Error starting message sync: ${e.message}")
        }
    }
    
    /**
     * Send message when user clicks send button
     * Calls sendMessage(chatId, content) - message goes to Firebase
     * UI updates automatically via startSyncing listener
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Send message to Firebase via repository
                messageRepository.sendMessage(chatId, content.trim())
                
                // Clear input text
                _uiState.value = _uiState.value.copy(inputText = "")
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to send message: ${e.message}"
                )
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
     * Format timestamp for display
     */
    fun formatMessageTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }
}