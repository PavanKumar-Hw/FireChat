package com.example.firechat.chats.di

import com.example.firechat.chats.data.repository.ChatsRepository
import com.example.firechat.chats.data.repository.ChatsRepositoryImpl
import com.example.firechat.chats.domain.ChatsUseCase
import com.example.firechat.chats.domain.ChatsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class ChatsRemoteModule {

    @Binds
    abstract fun bindsRemoteSource(
        remoteDataSourceImpl: ChatsRepositoryImpl
    ): ChatsRepository


    @Binds
    abstract fun bindsChatUseCase(
        mActiveUsersUseCase: ChatsUseCaseImpl
    ): ChatsUseCase

}