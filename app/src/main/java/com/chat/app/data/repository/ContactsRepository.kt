package com.chat.app.data.repository

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.chat.app.data.local.entities.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactsRepository(
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getDeviceContacts(): List<UserEntity> {
        val contacts = mutableListOf<UserEntity>()
        val snapshot = firestore.collection("users").get().await()

        val registeredPhoneNumbers = snapshot.documents
            .mapNotNull { it.getString("phone_number") }
            .toSet()

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
                val rawPhone = it.getString(numberIndex) ?: ""
                val phoneFormated = formatPhoneNumber(rawPhone)

                if (phoneFormated.isNotEmpty() && registeredPhoneNumbers.contains(phoneFormated)) {

                    contacts.add(
                        UserEntity(
                            uid = phoneFormated,
                            fullName = name,
                            phoneNumber = phoneFormated
                        )
                    )
                }
            }
        }
        return contacts.distinctBy { it.phoneNumber }
    }

    fun formatPhoneNumber(phoneNumber:String): String{

        val digits = phoneNumber.filter { it.isDigit() }

        if (digits.length < 11) {
            return ""
        }

        val finalPhoneNumber = digits.takeLast(11)
        return finalPhoneNumber
    }
}