package com.chat.app.data.repository

import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val authManager = FirebaseAuthManager()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean = authManager.isUserLoggedIn()

    suspend fun login(email: String, pass: String): Result<Boolean> {
        return try {
            authManager.login(email, pass)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(fullName: String, phoneNumber: String, email: String, pass: String, profilePictureUrl:String?): Result<Boolean> {
        return try {
            val user = authManager.register(email, pass)
            
            user?.let {
                val userMap = hashMapOf(
                    "uid" to it.uid,
                    "full_name" to fullName,
                    "phone_number" to phoneNumber,
                    "email" to email,
                    "profile_picture_url" to profilePictureUrl
                )
                firestore.collection("users").document(it.uid).set(userMap).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() = authManager.logout()
}