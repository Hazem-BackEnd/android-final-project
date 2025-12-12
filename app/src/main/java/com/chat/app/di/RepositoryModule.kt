package com.chat.app.di

import android.content.Context
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.StorageRepository
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
}