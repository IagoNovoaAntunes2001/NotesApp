package com.notes.home.di

import androidx.room.Room
import com.notes.home.data.local.NotesDatabase
import com.notes.home.data.repositories.TopicRepositoryImpl
import com.notes.home.domain.repositories.TopicRepository
import com.notes.home.domain.usecases.AddTopicUseCase
import com.notes.home.domain.usecases.DeleteTopicUseCase
import com.notes.home.domain.usecases.GetTopicByIdUseCase
import com.notes.home.domain.usecases.GetTopicsUseCase
import com.notes.home.presentation.HomeViewModel
import com.notes.home.presentation.detail.DetailViewModel
import com.notes.home.presentation.resources.HomeResources
import com.notes.home.presentation.resources.HomeResourcesImpl
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
    factory { GetTopicByIdUseCase(topicRepository = get()) }

    // Resources
    single<HomeResources> { HomeResourcesImpl(context = androidContext()) }

    viewModel {
        HomeViewModel(
            getTopicsUseCase = get(),
            addTopicUseCase = get(),
            deleteTopicUseCase = get(),
            savedStateHandle = get(),
            resources = get()
        )
    }

    viewModel {
        DetailViewModel(getTopicByIdUseCase = get())
    }
}
