package com.chat.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users",
        indices = [Index(value = ["phoneNumber"], unique = true),
                   Index(value = ["fullName"])]
)
data class UserEntity(
    @PrimaryKey val uid: String,
    val phoneNumber: String,
    val fullName: String,
    val profilePictureUrl: String? = null
)