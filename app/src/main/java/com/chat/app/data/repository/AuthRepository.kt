package com.chat.app.data.repository

import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val authManager = FirebaseAuthManager()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean = authManager.isUserLoggedIn()

    fun getCurrentUserId(): String? = authManager.currentUserId

    suspend fun login(email: String, pass: String): Result<Boolean> {
        return try {
            authManager.login(email, pass)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(fullName: String, phoneNumber: String, email: String, pass: String, profilePictureUrl: String?): Result<Boolean> {
        return try {
            val digits = phoneNumber.filter { it.isDigit() }

            if (digits.length < 11) {
                return Result.failure(Exception("Phone number is too short"))
            }

            val finalPhoneNumber = digits.takeLast(11)


            val snapshot = firestore.collection("users")
                .whereEqualTo("phone_number", finalPhoneNumber)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                return Result.failure(Exception("Phone number already exists"))
            }


            val user = authManager.register(email, pass)

            user?.let {
                val userMap = hashMapOf(
                    "uid" to it.uid,
                    "full_name" to fullName,
                    "phone_number" to finalPhoneNumber,
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
    suspend fun getUserFromFirebase(uid: String): UserEntity? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                val data = document.data
                UserEntity(
                    uid = data?.get("uid") as? String ?: uid,
                    fullName = data?.get("full_name") as? String ?: "",
                    phoneNumber = data?.get("phone_number") as? String ?: "",
                    profilePictureUrl = data?.get("profile_picture_url") as? String
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun logout() = authManager.logout()
}