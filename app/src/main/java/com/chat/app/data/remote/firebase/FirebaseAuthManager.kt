package com.chat.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, pass: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun register(email: String, pass: String): FirebaseUser? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            result.user
        } catch (e: Exception) {
            throw e
        }
    }

    fun logout() {
        auth.signOut()
    }
}