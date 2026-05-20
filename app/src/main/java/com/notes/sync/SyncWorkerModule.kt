package com.notes.sync

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @HiltWorker em SyncWorker já registra o Worker automaticamente na HiltWorkerFactory.
// Este módulo também provê o WorkManager como singleton injetável em qualquer lugar
// (inclusive HomeViewModel), apontando para a mesma instância do Application.
@Module
@InstallIn(SingletonComponent::class)
object SyncWorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

