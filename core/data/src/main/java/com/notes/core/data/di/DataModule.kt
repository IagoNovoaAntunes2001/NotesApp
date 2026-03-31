package com.notes.core.data.di

import com.notes.core.data.repository.TopicRepository
import com.notes.core.data.usecase.AddTopicUseCase
import com.notes.core.data.usecase.DeleteTopicUseCase
import com.notes.core.data.usecase.GetTopicByIdUseCase
import com.notes.core.data.usecase.GetTopicsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Usamos @Provides (e não @Binds) pois os UseCases têm parâmetro default
// (CoroutineDispatcher) que não é gerenciado pelo Hilt — passamos apenas o repo.
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideGetTopicsUseCase(repo: TopicRepository): GetTopicsUseCase =
        GetTopicsUseCase(topicRepository = repo)

    @Provides
    fun provideAddTopicUseCase(repo: TopicRepository): AddTopicUseCase =
        AddTopicUseCase(topicRepository = repo)

    @Provides
    fun provideDeleteTopicUseCase(repo: TopicRepository): DeleteTopicUseCase =
        DeleteTopicUseCase(topicRepository = repo)

    @Provides
    fun provideGetTopicByIdUseCase(repo: TopicRepository): GetTopicByIdUseCase =
        GetTopicByIdUseCase(topicRepository = repo)
}
