package com.chat.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.chat.app.data.remote.supabase.SupabaseConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val supabase by lazy {
        try {
            
            createSupabaseClient(
                supabaseUrl = SupabaseConfig.SUPABASE_URL,
                supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
            ) {
                install(Storage)
                httpEngine = Android.create()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private val storage by lazy {
        supabase.storage 
    }

    suspend fun uploadProfileImage(imageUri: Uri, userEmail: String): String? {
        return withContext(Dispatchers.IO) {
            try {

                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    return@withContext null
                }
                

                val safeEmail = userEmail.replace("@", "_").replace(".", "_")
                val timestamp = System.currentTimeMillis()
                val fileName = "profile_${safeEmail}_${timestamp}.jpg"
                
                inputStream.use { stream ->
                    val bytes = stream.readBytes()

                    
                    if (bytes.isEmpty()) {
                        return@withContext null
                    }

                    

                    storage.from(SupabaseConfig.PROFILE_IMAGES_BUCKET).upload(
                        path = fileName,
                        data = bytes,
                        upsert = true
                    )
                    


                    val publicUrl = storage.from(SupabaseConfig.PROFILE_IMAGES_BUCKET).publicUrl(fileName)
                    
                    return@withContext publicUrl
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    suspend fun deleteProfileImage(fileName: String): Boolean {
        return try {
            storage.from(SupabaseConfig.PROFILE_IMAGES_BUCKET).delete(fileName)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun testConnection(): Boolean {
        return try {
            val buckets = storage.retrieveBuckets()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}