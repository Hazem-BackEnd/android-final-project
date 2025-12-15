package com.chat.app.data.repository

import com.chat.app.data.local.dao.UserDao
import com.chat.app.data.local.entities.UserEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun saveUserLocally(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getUser(uid: String): UserEntity? {
        return userDao.getUser(uid)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.insertUser(user) // Using REPLACE strategy
    }
}