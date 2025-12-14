package com.chat.app.di

import android.content.Context
import com.chat.app.data.local.dao.ChatDao
import com.chat.app.data.local.dao.UserDao
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.repository.ContactsRepository
import com.chat.app.data.repository.StorageRepository
import com.chat.app.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideStorageRepository(@ApplicationContext context: Context): StorageRepository {
        return StorageRepository(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideContactsRepository(@ApplicationContext context: Context): ContactsRepository {
        return ContactsRepository(context)
    }

    @Provides
    @Singleton
    fun provideChatRepository(chatDao: ChatDao): ChatRepository {
        return ChatRepository(chatDao)
    }
}