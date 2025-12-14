package com.chat.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.repository.ContactsRepository
import com.chat.app.data.repository.UserRepository
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.local.entities.ChatEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val deviceContacts = contactsRepository.getDeviceContacts()
                val contacts = deviceContacts.map { userEntity ->
                    Contact(
                        id = userEntity.uid,
                        name = userEntity.fullName,
                        phone = userEntity.phoneNumber,
                        isOnline = false // TODO: Implement online status check
                    )
                }.sortedBy { it.name }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contacts = contacts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load contacts: ${e.message}"
                )
            }
        }
    }

    fun refreshContacts() {
        loadContacts()
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun searchContacts(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadContacts()
                return@launch
            }

            val allContacts = _uiState.value.contacts
            val filteredContacts = allContacts.filter { contact ->
                contact.name.contains(query, ignoreCase = true) ||
                contact.phone.contains(query)
            }

            _uiState.value = _uiState.value.copy(
                contacts = filteredContacts,
                searchQuery = query
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
        loadContacts()
    }

    fun onContactClick(contact: Contact, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Save the user locally
                val userEntity = UserEntity(
                    uid = contact.id,
                    fullName = contact.name,
                    phoneNumber = contact.phone,
                    profilePictureUrl = null
                )
                userRepository.saveUserLocally(userEntity)

                // 2. Create or update the chat
                val chatId = generateChatId(contact.id)
                val existingChat = chatRepository.getChat(chatId)
                
                if (existingChat == null) {
                    // Create new chat
                    val newChat = ChatEntity(
                        chatId = chatId,
                        otherUserId = contact.id,
                        lastMessage = null,
                        timestamp = System.currentTimeMillis()
                    )
                    chatRepository.createOrUpdateChat(newChat)
                } else {
                    // Update existing chat (refresh timestamp)
                    val updatedChat = existingChat.copy(
                        timestamp = System.currentTimeMillis()
                    )
                    chatRepository.createOrUpdateChat(updatedChat)
                }
                
                // Call the callback with the chatId
                onChatCreated(chatId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to start chat: ${e.message}"
                )
            }
        }
    }

    private fun generateChatId(contactId: String): String {
        // Generate a consistent chat ID based on contact ID
        return "chat_$contactId"
    }
}

data class ContactsUiState(
    val isLoading: Boolean = false,
    val contacts: List<Contact> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val permissionGranted: Boolean = false
)