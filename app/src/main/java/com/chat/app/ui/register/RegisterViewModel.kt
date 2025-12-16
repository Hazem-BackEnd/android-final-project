package com.chat.app.ui.register

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.StorageRepository
import com.chat.app.data.repository.UserRepository
import com.chat.app.utils.ValidationResult
import com.chat.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Nothing)
    val state = _state.asStateFlow()

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors = _validationErrors.asStateFlow()

    fun validateField(fieldName: String, value: String) {
        val result = when (fieldName) {
            "username" -> ValidationUtils.validateUsername(value)
            "phone" -> ValidationUtils.validatePhone(value)
            "email" -> ValidationUtils.validateEmail(value)
            "password" -> ValidationUtils.validatePassword(value)
            else -> ValidationResult.Success
        }

        val currentErrors = _validationErrors.value.toMutableMap()
        when (result) {
            is ValidationResult.Success -> currentErrors.remove(fieldName)
            is ValidationResult.Error -> currentErrors[fieldName] = result.message
        }
        _validationErrors.value = currentErrors
    }

    fun clearValidationError(fieldName: String) {
        val currentErrors = _validationErrors.value.toMutableMap()
        currentErrors.remove(fieldName)
        _validationErrors.value = currentErrors
    }

    fun signUp(fullName: String, phoneNumber: String, email: String, password: String, imageUri: Uri?) {
        // Validate all fields before proceeding
        val validationResults = ValidationUtils.validateAllFields(fullName, phoneNumber, email, password)
        val errors = mutableMapOf<String, String>()
        
        validationResults.forEach { (field, result) ->
            if (result is ValidationResult.Error) {
                errors[field] = result.message
            }
        }
        
        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            _state.value = SignUpState.ValidationError("Please fix the errors above")
            return
        }

        _state.value = SignUpState.Loading
        
        viewModelScope.launch {
            try {
                var profilePictureUrl: String? = null
                if (imageUri != null) {
                    profilePictureUrl = storageRepository.uploadProfileImage(imageUri, email)
                }

                val result = authRepository.register(fullName, phoneNumber, email, password, profilePictureUrl)
                
                if (result.isSuccess) {
                    // Fetch and save user data locally after successful registration
                    val currentUserId = authRepository.getCurrentUserId()
                    if (currentUserId != null) {
                        try {
                            // Try to fetch user data from Firebase first
                            val firebaseUser = authRepository.getUserFromFirebase(currentUserId)
                            val userEntity = firebaseUser ?: UserEntity(
                                uid = currentUserId,
                                fullName = fullName,
                                phoneNumber = phoneNumber,
                                profilePictureUrl = profilePictureUrl
                            )
                            userRepository.saveUserLocally(userEntity)
                        } catch (e: Exception) {
                            // Don't fail registration if local save fails
                            println("Failed to save user locally: ${e.message}")
                        }
                    }
                    _state.value = SignUpState.Success
                } else {
                    _state.value = SignUpState.Error("Registration failed. Please try again.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = SignUpState.Error("Registration failed. Please check your connection and try again.")
            }
        }
    }
}

sealed class SignUpState {
    object Nothing : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
    data class ValidationError(val message: String) : SignUpState()
}