package com.chat.app.data.remote.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class NetworkMessage(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0
)

class FirebaseChatService {
    private val firestore = FirebaseFirestore.getInstance()

    fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    suspend fun sendMessage(chatId: String, message: NetworkMessage) {
        try {
            firestore.collection("chats").document(chatId)
                .collection("messages")
                .document(message.id)
                .set(message)
                .await()


            val participants = chatId.split("_")

            val chatUpdate = mapOf(
                "lastMessage" to message.content,
                "timestamp" to message.timestamp,
                "participants" to participants
            )

            // Use MERGE to avoid overwriting other fields
            firestore.collection("chats").document(chatId)
                .set(chatUpdate, SetOptions.merge())
                .await()

        } catch (e: Exception) {
            Log.e("FirebaseChat", "Error sending message", e)
        }
    }

    fun listenToMessages(chatId: String): Flow<List<NetworkMessage>> = callbackFlow {
        val collection = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(NetworkMessage::class.java)
            } ?: emptyList()

            trySend(messages)
        }

        awaitClose { listener.remove() }
    }
    fun listenToUserChats(userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val query = firestore.collection("chats")
            .whereArrayContains("participants", userId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val chatDocs = snapshot?.documents?.map { doc ->
                val data = doc.data ?: emptyMap()
                data + ("chatId" to doc.id)
            } ?: emptyList()

            trySend(chatDocs)
        }
        awaitClose { listener.remove() }
    }

    suspend fun fetchUserName(userId: String): String {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("fullName") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
