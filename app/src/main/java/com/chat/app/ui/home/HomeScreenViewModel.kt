package com.chat.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.local.entities.ChatEntity
import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val chats: List<ChatEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val errorMessage: String? = null
) {
    // Helper properties for UI logic
    val shouldShowLoading: Boolean get() = isLoading && chats.isEmpty()
    val shouldShowEmpty: Boolean get() = !isLoading && chats.isEmpty() && errorMessage == null
    val shouldShowChats: Boolean get() = !isLoading && chats.isNotEmpty()
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
): ViewModel(){
    
    // 🔥 Get real user ID from Firebase Auth
    private val authManager = FirebaseAuthManager()
    private val currentUserId: String = authManager.currentUserId ?: "current_user"
    
    // Private mutable state
    private val _uiState = MutableStateFlow(HomeUiState())
    // Public read-only state
    val uiState = _uiState.asStateFlow()
    
    // All chats from database
    private val _allChats = MutableStateFlow<List<ChatEntity>>(emptyList())
    
    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 🔥 UPDATED: Use user-specific chat query instead of all chats
                chatRepository.getChatsForUser(currentUserId)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                    .collect { chatList ->
                        _allChats.value = chatList
                        updateFilteredChats()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                        println("📱 Loaded ${chatList.size} chats for user: $currentUserId")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
                println("❌ Error loading chats: ${e.message}")
            }
        }
    }
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateFilteredChats()
    }

    fun toggleSearch() {
        val newSearching = !_uiState.value.isSearching
        _uiState.value = _uiState.value.copy(
            isSearching = newSearching,
            searchQuery = if (!newSearching) "" else _uiState.value.searchQuery
        )
        updateFilteredChats()
    }

    private fun updateFilteredChats() {
        val currentState = _uiState.value
        
        if (currentState.searchQuery.isEmpty()) {
            // No search query - use all chats
            _uiState.value = currentState.copy(chats = _allChats.value)
        } else {
            // 🔥 UPDATED: Use database search for better performance
            viewModelScope.launch {
                try {
                    chatRepository.searchChatsForUser(currentUserId, currentState.searchQuery)
                        .collect { searchResults ->
                            _uiState.value = _uiState.value.copy(chats = searchResults)
                            println("🔍 Search results for '${currentState.searchQuery}': ${searchResults.size} chats")
                        }
                } catch (e: Exception) {
                    // Fallback to in-memory search if database search fails
                    val filteredChats = _allChats.value.filter { chat ->
                        chat.otherUserId.contains(currentState.searchQuery, ignoreCase = true) ||
                        (chat.lastMessage?.contains(currentState.searchQuery, ignoreCase = true) == true)
                    }
                    _uiState.value = currentState.copy(chats = filteredChats)
                    println("⚠️ Database search failed, using in-memory search: ${e.message}")
                }
            }
        }
    }
    fun onChatClicked(chatId: String) {
        println("🔥 Chat clicked: $chatId")
        // Navigation will be handled in the UI layer
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to logout: ${e.message}"
                )
            }
        }
    }
    /**
     * 🔥 UPDATED: Add sample data with proper chat IDs for current user
     */
    fun addSampleData() {
        viewModelScope.launch {
            try {
                val sampleChats = listOf(
                    ChatEntity(
                        chatId = "${currentUserId}_ahmed_ali", // 🔥 Include current user in chat ID
                        otherUserId = "Ahmed Ali",
                        lastMessage = "Hey, how are you doing? Let's catch up soon!",
                        timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
                    ),
                    ChatEntity(
                        chatId = "${currentUserId}_mariam_hassan",
                        otherUserId = "Mariam Hassan", 
                        lastMessage = "Let's meet tomorrow at 5 PM for coffee",
                        timestamp = System.currentTimeMillis() - 7200000 // 2 hours ago
                    ),
                    ChatEntity(
                        chatId = "${currentUserId}_omar_khaled",
                        otherUserId = "Omar Khaled",
                        lastMessage = "Thanks for your help with the project!",
                        timestamp = System.currentTimeMillis() - 86400000 // 1 day ago
                    ),
                    ChatEntity(
                        chatId = "${currentUserId}_sara_mohamed",
                        otherUserId = "Sara Mohamed",
                        lastMessage = "See you soon at the meeting 👋",
                        timestamp = System.currentTimeMillis() - 172800000 // 2 days ago
                    ),
                    ChatEntity(
                        chatId = "${currentUserId}_hassan_ahmed",
                        otherUserId = "Hassan Ahmed",
                        lastMessage = "Call me when you're free to discuss the plan",
                        timestamp = System.currentTimeMillis() - 259200000 // 3 days ago
                    )
                )
                
                sampleChats.forEach { chat ->
                    chatRepository.createOrUpdateChat(chat)
                }
                
                println("✅ Sample chats added for user: $currentUserId")
                println("🔍 Try searching for: 'Ahmed', 'meeting', 'help', 'coffee'")
            } catch (e: Exception) {
                println("❌ Error adding sample chats: ${e.message}")
            }
        }
    }
}