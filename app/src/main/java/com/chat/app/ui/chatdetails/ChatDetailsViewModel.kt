package com.chat.app.ui.chatdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.local.entities.ChatEntity
import com.chat.app.data.local.entities.MessageEntity
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.repository.MessageRepository
import com.chat.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

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

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    
    // Private mutable state
    private val _uiState = MutableStateFlow(ChatDetailsUiState())
    // Public read-only state
    val uiState = _uiState.asStateFlow()
    
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private var currentUserId: String = ""
    
    /**
     * Initialize chat with parameters - call this from the UI
     */
    fun initializeChat(chatId: String, otherUserName: String, otherUserId: String) {
        currentUserId = authRepository.getCurrentUserId() ?: "current_user"
        
        _uiState.value = _uiState.value.copy(
            chatId = chatId,
            otherUserName = otherUserName
        )
        
        // 🔥 Step 1: Save the other user locally and create/update the chat
        initializeChatData(chatId, otherUserName, otherUserId)
        // 🔥 Step 2: Load messages from local DB
        loadMessages(chatId)
        // 🔥 Step 3: Start syncing with Firebase (CRITICAL)
        startMessageSyncing(chatId)
    }
    
    /**
     * 🔥 Initialize chat: Save user locally and create/update chat entity
     * This follows the "Starting a Chat" logic from the documentation
     */
    private fun initializeChatData(chatId: String, otherUserName: String, otherUserId: String) {
        viewModelScope.launch {
            try {
                // Step A.1: Save the other user locally
                val otherUser = UserEntity(
                    uid = otherUserId,
                    fullName = otherUserName,
                    phoneNumber = otherUserId  // Using uid as phone number
                )
                userRepository.saveUserLocally(otherUser)
                println("✅ Saved user locally: $otherUserName ($otherUserId)")
                
                // Step A.2: Create or update the chat
                val chat = ChatEntity(
                    chatId = chatId,
                    otherUserId = otherUserId,
                    lastMessage = null,
                    timestamp = System.currentTimeMillis()
                )
                chatRepository.createOrUpdateChat(chat)
                println("✅ Created/updated chat: $chatId")
                
            } catch (e: Exception) {
                println("❌ Error initializing chat: ${e.message}")
            }
        }
    }
    
    /**
     * Load messages for this chat from repository
     */
    private fun loadMessages(chatId: String) {
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
    private fun startMessageSyncing(chatId: String) {
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
        
        val chatId = _uiState.value.chatId
        if (chatId.isEmpty()) {
            println("❌ Cannot send message: chatId is empty")
            return
        }
        
        viewModelScope.launch {
            try {
                // Send message via repository (follows Single Source of Truth pattern)
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
        val chatId = _uiState.value.chatId
        if (chatId.isEmpty()) return
        
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