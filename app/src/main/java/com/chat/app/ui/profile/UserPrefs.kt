package com.chat.app.ui.profile

import android.content.Context
import android.net.Uri
import kotlin.let

object UserPrefs {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PHONE = "phone"
    private const val KEY_EMAIL = "email"
    private const val KEY_PROFILE_URI = "profile_uri"

    fun saveUser(context: Context, username: String, phone: String, email: String, profileUri: Uri?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(KEY_USERNAME, username)
            putString(KEY_PHONE, phone)
            putString(KEY_EMAIL, email)
            putString(KEY_PROFILE_URI, profileUri?.toString())
            apply()
        }
    }

    fun loadUser(context: Context): UserData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val profileUri = prefs.getString(KEY_PROFILE_URI, null)?.let { Uri.parse(it) }
        return UserData(username, phone, email, profileUri)
    }
}
