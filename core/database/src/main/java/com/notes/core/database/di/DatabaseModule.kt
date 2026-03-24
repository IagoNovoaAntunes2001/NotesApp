package com.notes.core.database.di

import androidx.room.Room
import com.notes.core.data.repository.TopicRepository
import com.notes.core.database.NotesDatabase
import com.notes.core.database.repository.TopicRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            NotesDatabase::class.java,
            "notes.db"
        ).build()
    }

    single { get<NotesDatabase>().topicDao() }

    single<TopicRepository> { TopicRepositoryImpl(topicDao = get()) }
}
