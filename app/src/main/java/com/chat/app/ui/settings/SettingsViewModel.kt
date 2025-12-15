package com.chat.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.remote.api.QuotesApiService
import com.chat.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for SettingsScreen
 */
data class SettingsUiState(
    val isLoggingOut: Boolean = false,
    val logoutSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    // 🔥 Quote state
    val quote: String = "",
    val quoteAuthor: String = "",
    val isLoadingQuote: Boolean = false
) {
    val shouldShowLoading: Boolean get() = isLoggingOut
    val shouldShowError: Boolean get() = errorMessage != null
    val hasQuote: Boolean get() = quote.isNotEmpty()
}

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(SettingsUiState())
    // Public read-only state
    val uiState = _uiState.asStateFlow()
    
    // 🔥 Quotes API Service (Retrofit)
    private val quotesApiService = QuotesApiService()

    /**
     * Check if user is currently logged in
     */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    /**
     * Logout the current user
     */
    fun logout() {
        if (!isUserLoggedIn()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No user is currently logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoggingOut = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // Call AuthRepository logout method
                authRepository.logout()
                
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = true,
                    errorMessage = null
                )
                
                println("✅ User logged out successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = false,
                    errorMessage = e.message ?: "Logout failed"
                )
                println("❌ Logout error: ${e.message}")
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Reset logout success state
     */
    fun resetLogoutState() {
        _uiState.value = _uiState.value.copy(logoutSuccess = false)
    }

    /**
     * Toggle dark theme setting
     */
    fun toggleDarkTheme() {
        _uiState.value = _uiState.value.copy(
            isDarkTheme = !_uiState.value.isDarkTheme
        )
        // TODO: Implement actual theme switching logic
        println("🌙 Dark theme toggled: ${_uiState.value.isDarkTheme}")
    }

    /**
     * Toggle notifications setting
     */
    fun toggleNotifications() {
        _uiState.value = _uiState.value.copy(
            notificationsEnabled = !_uiState.value.notificationsEnabled
        )
        // TODO: Implement actual notification settings logic
        println("🔔 Notifications toggled: ${_uiState.value.notificationsEnabled}")
    }
    
    /**
     * 🔥 Fetch a random quote from ZenQuotes API using Retrofit
     */
    fun fetchRandomQuote() {
        _uiState.value = _uiState.value.copy(isLoadingQuote = true)
        
        viewModelScope.launch {
            val result = quotesApiService.getRandomQuote()
            
            result.onSuccess { quote ->
                _uiState.value = _uiState.value.copy(
                    quote = quote.q,
                    quoteAuthor = quote.a,
                    isLoadingQuote = false
                )
                println("✅ Quote fetched: \"${quote.q}\" - ${quote.a}")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    quote = "Failed to load quote",
                    quoteAuthor = "",
                    isLoadingQuote = false
                )
                println("❌ Quote fetch error: ${error.message}")
            }
        }
    }
}