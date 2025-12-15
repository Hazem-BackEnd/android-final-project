package com.chat.app.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.chat.app.data.local.entities.UserEntity

class ContactsRepository(
    private val context: Context
) {

    fun getDeviceContacts(): List<UserEntity> {
        val contacts = mutableListOf<UserEntity>()
        
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: "Unknown"
                val phone = it.getString(numberIndex) ?: ""
                
                // Remove all non-digit characters for URL-safe ID
                val phoneId = phone.replace("[^0-9]".toRegex(), "")
                // Keep formatted phone for display (remove only spaces and dashes)
                val displayPhone = phone.replace("\\s".toRegex(), "").replace("-", "")
                
                if (phoneId.isNotEmpty()) {
                    contacts.add(
                        UserEntity(
                            uid = phoneId,  // Digits only - URL safe
                            fullName = name,
                            phoneNumber = displayPhone  // For display
                        )
                    )
                }
            }
        }
        return contacts.distinctBy { it.uid } // remove duplicates
    }
}