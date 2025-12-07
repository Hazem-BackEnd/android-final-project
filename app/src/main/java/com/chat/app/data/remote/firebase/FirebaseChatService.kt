package com.chat.app.data.remote.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    //we want both users have the same chat id format
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

            val chatUpdate = mapOf(
                "lastMessage" to message.content,
                "timestamp" to message.timestamp
            )
            firestore.collection("chats").document(chatId).set(chatUpdate)
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

            trySend(messages)//send the data to Flow
        }

        awaitClose { listener.remove() }
    }
}
