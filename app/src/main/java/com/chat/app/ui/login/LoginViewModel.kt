package com.chat.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        _state.value = SignInState.Loading
        
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                if (result.isSuccess) {
                    // Fetch and save user data locally after successful login
                    val currentUserId = authRepository.getCurrentUserId()
                    if (currentUserId != null) {
                        try {
                            // Try to fetch user data from Firebase first
                            val firebaseUser = authRepository.getUserFromFirebase(currentUserId)
                            val userEntity = firebaseUser ?: UserEntity(
                                uid = currentUserId,
                                fullName = email.substringBefore("@"), // Fallback name
                                phoneNumber = "",
                                profilePictureUrl = null
                            )
                            userRepository.saveUserLocally(userEntity)
                        } catch (e: Exception) {
                            // Don't fail login if local save fails
                            println("Failed to save user locally: ${e.message}")
                        }
                    }
                    _state.value = SignInState.Success
                } else {
                    _state.value = SignInState.Error
                }
            } catch (e: Exception) {
                _state.value = SignInState.Error
            }
        }
    }
}

sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    object Error : SignInState()
}