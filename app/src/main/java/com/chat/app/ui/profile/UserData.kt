package com.chat.app.ui.profile

import android.content.Context
import android.net.Uri

data class UserData(
    var username: String = "",
    var phone: String = "",
    var email: String = "",
    var profileUri: Uri? = null
)
object CurrentUser {
    var user: UserData = UserData()

    fun load(context: Context) {
        user = UserPrefs.loadUser(context)
    }

    fun save(context: Context) {
        UserPrefs.saveUser(context, user.username, user.phone, user.email, user.profileUri)
    }
}
