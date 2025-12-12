package com.chat.app.ui.register

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Nothing)
    val state = _state.asStateFlow()

    fun signUp(fullName: String, phoneNumber: String, email: String, password: String, imageUri: Uri?) {
        _state.value = SignUpState.Loading
        
        viewModelScope.launch {
            try {

                var profilePictureUrl: String? = null
                if (imageUri != null) {
                    profilePictureUrl = storageRepository.uploadProfileImage(imageUri, email)

                }

                val result = authRepository.register(fullName, phoneNumber, email, password, profilePictureUrl)
                
                if (result.isSuccess) {
                    _state.value = SignUpState.Success
                } else {
                    _state.value = SignUpState.Error
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = SignUpState.Error
            }
        }
    }
}

sealed class SignUpState {
    object Nothing : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    object Error : SignUpState()
}