package com.chat.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for ContactsScreen
 */
data class ContactsUiState(
    val contacts: List<UserEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
) {
    // Helper properties for UI logic
    val shouldShowLoading: Boolean get() = isLoading && contacts.isEmpty()
    val shouldShowEmpty: Boolean get() = !isLoading && contacts.isEmpty() && errorMessage == null
    val shouldShowContacts: Boolean get() = !isLoading && contacts.isNotEmpty()
    
    // Filtered contacts based on search query
    val filteredContacts: List<UserEntity> get() = 
        if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.fullName.contains(searchQuery, ignoreCase = true) ||
                contact.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
}

class ContactsViewModel(private val contactsRepository: ContactsRepository): ViewModel() {
    
    // Private mutable state
    private val _uiState = MutableStateFlow(ContactsUiState())
    // Public read-only state
    val uiState = _uiState.asStateFlow()
    
    init {
        loadDeviceContacts()
    }
    
    /**
     * Load contacts from device using ContactsRepository
     */
    fun loadDeviceContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val deviceContacts = contactsRepository.getDeviceContacts()
                _uiState.value = _uiState.value.copy(
                    contacts = deviceContacts,
                    isLoading = false,
                    errorMessage = null
                )
                println("✅ Loaded ${deviceContacts.size} contacts from device")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load contacts"
                )
                println("❌ Error loading contacts: ${e.message}")
            }
        }
    }
    
    /**
     * Update search query and filter contacts
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    /**
     * Clear search query
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }
    
    /**
     * Refresh contacts from device
     */
    fun refreshContacts() {
        loadDeviceContacts()
    }
}