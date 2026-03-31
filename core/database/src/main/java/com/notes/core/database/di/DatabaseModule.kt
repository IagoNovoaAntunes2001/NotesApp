package com.notes.core.database.di

import android.content.Context
import androidx.room.Room
import com.notes.core.data.repository.TopicRepository
import com.notes.core.database.NotesDatabase
import com.notes.core.database.dao.TopicDao
import com.notes.core.database.repository.TopicRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// SingletonComponent = vive enquanto o app viver.
// @Singleton garante que só existe uma instância (igual ao single { } do Koin).
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNotesDatabase(@ApplicationContext context: Context): NotesDatabase =
        Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            "notes.db"
        ).build()

    // Sem @Singleton: o DAO é leve e pode ser recriado a partir do DB singleton.
    @Provides
    fun provideTopicDao(db: NotesDatabase): TopicDao = db.topicDao()

    @Provides
    @Singleton
    fun provideTopicRepository(dao: TopicDao): TopicRepository =
        TopicRepositoryImpl(topicDao = dao)
}
