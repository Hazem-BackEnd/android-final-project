package com.chat.app.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.chat.app.data.local.entities.UserEntity

class ContactsRepository(private val context: Context) {

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
                val name = it.getString(nameIndex)
                var phone = it.getString(numberIndex)
                
                phone = phone.replace("\\s".toRegex(), "").replace("-", "")


                contacts.add(
                    UserEntity(
                        uid = phone,
                        fullName = name,
                        phoneNumber = phone

                    )
                )
            }
        }
        return contacts.distinctBy { it.uid } // remove duplicates
    }
}