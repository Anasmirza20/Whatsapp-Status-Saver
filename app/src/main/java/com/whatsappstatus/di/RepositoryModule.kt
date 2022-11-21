package com.whatsappstatus.di

import com.whatsappstatus.repositories.ReelsRepository
import com.whatsappstatus.repositories.ReelsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReelsRepository(
        reelsRepositoryImpl: ReelsRepositoryImpl
    ): ReelsRepository


}