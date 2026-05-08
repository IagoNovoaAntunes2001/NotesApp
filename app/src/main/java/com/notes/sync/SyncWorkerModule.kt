package com.notes.sync

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// @HiltWorker em SyncWorker já registra o Worker automaticamente na HiltWorkerFactory.
// Este módulo garante que o Hilt processa o pacote sync durante a geração de código.
@Module
@InstallIn(SingletonComponent::class)
object SyncWorkerModule

