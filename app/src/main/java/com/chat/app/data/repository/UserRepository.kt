package com.chat.app.data.repository

import android.content.Context
import com.chat.app.data.local.AppDatabase
import com.chat.app.data.local.entities.UserEntity

class UserRepository(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()

    suspend fun saveUserLocally(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getUser(uid: String): UserEntity? {
        return userDao.getUser(uid)
    }
}