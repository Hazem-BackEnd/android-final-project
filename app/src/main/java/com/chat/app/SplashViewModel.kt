package com.chat.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf

class SplashViewModel : ViewModel() {

    var isLoading = mutableStateOf(true)

    init {
        viewModelScope.launch {
            delay(2000)
            isLoading.value = false
        }
    }
}