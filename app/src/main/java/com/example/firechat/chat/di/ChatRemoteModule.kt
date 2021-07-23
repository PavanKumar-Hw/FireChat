package com.example.firechat.chat.di

import com.example.firechat.chat.data.repository.ChatRepository
import com.example.firechat.chat.data.repository.ChatRepositoryImpl
import com.example.firechat.chat.domain.ChatUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class ChatRemoteModule {

    @Binds
    abstract fun bindsRemoteSource(
        remoteDataSourceImpl: ChatRepositoryImpl
    ): ChatRepository


    @Binds
    abstract fun bindsArticlesUseCase(
        mActiveUsersUseCase: ChatUseCase
    ): ChatUseCase

}