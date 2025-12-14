package com.chat.app.utils

import android.util.Patterns

object ValidationUtils {
    
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.Error("Username is required")
            username.length < 3 -> ValidationResult.Error("Username must be at least 3 characters")
            username.length > 20 -> ValidationResult.Error("Username must be less than 20 characters")
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> ValidationResult.Error("Username can only contain letters, numbers, and underscores")
            else -> ValidationResult.Success
        }
    }
    
    fun validatePhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult.Error("Phone number is required")
            phone.length < 10 -> ValidationResult.Error("Phone number must be at least 10 digits")
            phone.length > 15 -> ValidationResult.Error("Phone number must be less than 15 digits")
            !phone.matches(Regex("^[+]?[0-9\\-\\s()]+$")) -> ValidationResult.Error("Invalid phone number format")
            else -> ValidationResult.Success
        }
    }
    
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error("Invalid email format")
            email.length > 100 -> ValidationResult.Error("Email must be less than 100 characters")
            else -> ValidationResult.Success
        }
    }
    
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            password.length > 50 -> ValidationResult.Error("Password must be less than 50 characters")
            !password.any { it.isDigit() } -> ValidationResult.Error("Password must contain at least one number")
            !password.any { it.isLetter() } -> ValidationResult.Error("Password must contain at least one letter")
            else -> ValidationResult.Success
        }
    }
    
    fun validateAllFields(
        username: String,
        phone: String,
        email: String,
        password: String
    ): Map<String, ValidationResult> {
        return mapOf(
            "username" to validateUsername(username),
            "phone" to validatePhone(phone),
            "email" to validateEmail(email),
            "password" to validatePassword(password)
        )
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}