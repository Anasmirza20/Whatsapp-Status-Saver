package com.whatsappstatus.di

import android.content.ContentResolver
import android.content.Context
import androidx.work.WorkManager
import com.whatsappstatus.util.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {


    @Provides
    @Singleton
    fun getSharedPrefInstance(@ApplicationContext context: Context) = SharedPref(context)

    @Provides
    @Singleton
    fun getContext(@ApplicationContext context: Context) = context


    @Provides
    @Singleton
    fun getContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

}
