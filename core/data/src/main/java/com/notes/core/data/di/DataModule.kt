package com.notes.core.data.di

import com.notes.core.data.repository.TopicRepository
import com.notes.core.data.usecase.AddTopicUseCase
import com.notes.core.data.usecase.DeleteTopicUseCase
import com.notes.core.data.usecase.GetTopicByIdUseCase
import com.notes.core.data.usecase.GetTopicsStreamUseCase
import com.notes.core.data.usecase.GetTopicsUseCase
import com.notes.core.data.usecase.SyncTopicsUseCase
import com.notes.core.data.usecase.SyncPendingTopicsUseCase
import com.notes.core.data.usecase.UpdateTopicUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideGetTopicsUseCase(repo: TopicRepository): GetTopicsUseCase =
        GetTopicsUseCase(topicRepository = repo)

    @Provides
    fun provideGetTopicsStreamUseCase(repo: TopicRepository): GetTopicsStreamUseCase =
        GetTopicsStreamUseCase(topicRepository = repo)

    @Provides
    fun provideAddTopicUseCase(repo: TopicRepository): AddTopicUseCase =
        AddTopicUseCase(topicRepository = repo)

    @Provides
    fun provideDeleteTopicUseCase(repo: TopicRepository): DeleteTopicUseCase =
        DeleteTopicUseCase(topicRepository = repo)

    @Provides
    fun provideGetTopicByIdUseCase(repo: TopicRepository): GetTopicByIdUseCase =
        GetTopicByIdUseCase(topicRepository = repo)

    @Provides
    fun provideSyncTopicsUseCase(repo: TopicRepository): SyncTopicsUseCase =
        SyncTopicsUseCase(topicRepository = repo)

    @Provides
    fun provideSyncPendingTopicsUseCase(repo: TopicRepository): SyncPendingTopicsUseCase =
        SyncPendingTopicsUseCase(topicRepository = repo)

    @Provides
    fun provideUpdateTopicUseCase(repo: TopicRepository): UpdateTopicUseCase =
        UpdateTopicUseCase(topicRepository = repo)
}
