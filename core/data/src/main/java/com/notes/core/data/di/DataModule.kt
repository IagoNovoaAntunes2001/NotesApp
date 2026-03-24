package com.notes.core.data.di

import com.notes.core.data.usecase.AddTopicUseCase
import com.notes.core.data.usecase.DeleteTopicUseCase
import com.notes.core.data.usecase.GetTopicByIdUseCase
import com.notes.core.data.usecase.GetTopicsUseCase
import org.koin.dsl.module

val dataModule = module {
    factory { GetTopicsUseCase(topicRepository = get()) }
    factory { AddTopicUseCase(topicRepository = get()) }
    factory { DeleteTopicUseCase(topicRepository = get()) }
    factory { GetTopicByIdUseCase(topicRepository = get()) }
}

