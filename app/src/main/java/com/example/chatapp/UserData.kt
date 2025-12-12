package com.example.chatapp

import android.net.Uri

data class UserData(
    var username: String = "",
    var phone: String = "",
    var email: String = "",
    var profileUri: Uri? = null
)

object CurrentUser {
    val user = UserData()
}