package com.chat.app.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.chat.app.data.local.AppDatabase
import com.chat.app.data.local.entities.ChatEntity
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.remote.firebase.FirebaseChatService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val context: Context
) {
    private val db = AppDatabase.getDatabase(context)
    private val chatDao = db.chatDao()
    private val userDao = db.userDao()
    private val firebaseChatService = FirebaseChatService()
    private val firestore = FirebaseFirestore.getInstance()

    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()

    suspend fun createOrUpdateChat(chat: ChatEntity) {
        chatDao.insertChat(chat)
    }

    suspend fun getChat(chatId: String): ChatEntity? {
        return chatDao.getChatById(chatId)
    }

    fun getChatsForUser(userId: String): Flow<List<ChatEntity>> {
        return chatDao.getChatsForUser(userId)
    }

    fun searchChatsForUser(userId: String, searchQuery: String): Flow<List<ChatEntity>> {
        return chatDao.searchChatsForUser(userId, searchQuery)
    }


    fun startSyncingAllChats(currentUserId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            firebaseChatService.listenToUserChats(currentUserId).collect { firebaseChats ->

                for (chatData in firebaseChats) {
                    val chatId = chatData["chatId"] as? String ?: continue
                    val lastMessage = chatData["lastMessage"] as? String
                    val timestamp = (chatData["timestamp"] as? Long) ?: System.currentTimeMillis()

                    val ids = chatId.split("_")
                    if (ids.size == 2) {
                        val otherUserId = if (ids[0] == currentUserId) ids[1] else ids[0]

                        val resolvedUser = resolveUser(otherUserId)

                        val chatEntity = ChatEntity(
                            chatId = chatId,
                            otherUserId = otherUserId,
                            otherUserName = resolvedUser.fullName,
                            lastMessage = lastMessage,
                            timestamp = timestamp
                        )
                        chatDao.insertChat(chatEntity)
                    }
                }
            }
        }
    }


    private suspend fun resolveUser(userId: String): UserEntity {
        val localUser = userDao.getUser(userId)
        if (localUser != null) return localUser

        var phone = ""
        var remoteName = "Unknown"

        try {
            val doc = firestore.collection("users").document(userId).get().await()
            phone = doc.getString("phone_number") ?: ""
            remoteName = doc.getString("fullName") ?: "Unknown"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var finalName = remoteName
        if (phone.isNotEmpty()) {
            val contactName = getContactNameFromPhone(phone)
            if (contactName != null) {
                finalName = contactName
            }
        }

        val newUser = UserEntity(
            uid = userId,
            fullName = finalName,
            phoneNumber = phone,
            profilePictureUrl = null
        )
        userDao.insertUser(newUser)

        return newUser
    }


    private fun getContactNameFromPhone(phoneNumber: String): String? {
        if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val targetNumber = phoneNumber.filter { it.isDigit() }.takeLast(9)

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )

        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val rawNum = it.getString(numIdx) ?: continue
                val cleanNum = rawNum.filter { char -> char.isDigit() }

                if (cleanNum.contains(targetNumber)) {
                    return it.getString(nameIdx)
                }
            }
        }
        return null
    }
}