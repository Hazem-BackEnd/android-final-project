package com.chat.app.ui.profile

import android.net.Uri
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
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    fun loadCurrentUserProfile() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId != null) {
            loadUserProfile(currentUserId)
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No user is currently logged in"
            )
        }
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val userEntity = userRepository.getUser(uid)
                if (userEntity != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        username = userEntity.fullName,
                        phone = userEntity.phoneNumber,
                        email = "userEntity.email",
                        profileImageUrl = userEntity.profilePictureUrl,
                        uid = userEntity.uid
                    )
                } else {
                    // User not found in database, show empty state
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        username = "",
                        phone = "",
                        email = "",
                        uid = uid
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateProfileImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    fun toggleEditMode() {
        _isEditing.value = !_isEditing.value
        
        if (!_isEditing.value) {
            // Save changes when exiting edit mode
            saveProfile()
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            
            try {
                val currentState = _uiState.value
                val currentUserId = authRepository.getCurrentUserId()
                
                if (currentUserId == null) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "No user is currently logged in"
                    )
                    return@launch
                }
                
                // Save to database using UserRepository
                val userEntity = UserEntity(
                    uid = currentUserId,
                    fullName = currentState.username,
                    phoneNumber = currentState.phone,
//                    email = "currentState.email",
                    profilePictureUrl = currentState.profileImageUrl
                )
                
                userRepository.saveUserLocally(userEntity)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Profile updated successfully"
                )
                
                // Clear success message after a delay
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(successMessage = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save profile: ${e.message}"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    // Load from SharedPreferences as fallback
    fun loadFromSharedPreferences(userData: UserData) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            username = if (currentState.username.isEmpty()) userData.username else currentState.username,
            phone = if (currentState.phone.isEmpty()) userData.phone else currentState.phone,
            email = userData.email, // Always use email from SharedPreferences
            profileImageUri = userData.profileUri
        )
    }

    // Save to SharedPreferences
    fun saveToSharedPreferences(): UserData {
        val currentState = _uiState.value
        return UserData(
            username = currentState.username,
            phone = currentState.phone,
            email = currentState.email,
            profileUri = currentState.profileImageUri
        )
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val username: String = "",
    val phone: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val profileImageUri: Uri? = null,
    val uid: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)