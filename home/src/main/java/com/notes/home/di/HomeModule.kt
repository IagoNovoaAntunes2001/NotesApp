package com.notes.home.di

import androidx.room.Room
import com.notes.home.data.local.NotesDatabase
import com.notes.home.data.repositories.TopicRepositoryImpl
import com.notes.home.domain.repositories.TopicRepository
import com.notes.home.domain.usecases.AddTopicUseCase
import com.notes.home.domain.usecases.DeleteTopicUseCase
import com.notes.home.domain.usecases.GetTopicsUseCase
import com.notes.home.presentation.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            NotesDatabase::class.java,
            "notes.db"
        ).build()
    }

    // DAO
    single { get<NotesDatabase>().topicDao() }

    // Repository
    single<TopicRepository> { TopicRepositoryImpl(topicDao = get()) }
    factory { GetTopicsUseCase(topicRepository = get()) }
    factory { AddTopicUseCase(topicRepository = get()) }
    factory { DeleteTopicUseCase(topicRepository = get()) }

    viewModel {
        HomeViewModel(
            getTopicsUseCase = get(),
            addTopicUseCase = get(),
            deleteTopicUseCase = get()
        )
    }
}
