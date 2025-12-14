package com.chat.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.repository.ContactsRepository
import com.chat.app.data.local.entities.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository
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
}

data class ContactsUiState(
    val isLoading: Boolean = false,
    val contacts: List<Contact> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val permissionGranted: Boolean = false
)