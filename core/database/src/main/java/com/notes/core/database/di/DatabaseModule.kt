package com.notes.core.database.di

import android.content.Context
import androidx.room.Room
import com.notes.core.data.repository.TopicRepository
import com.notes.core.database.NotesDatabase
import com.notes.core.database.dao.TopicDao
import com.notes.core.database.repository.TopicRepositoryImpl
import com.notes.core.network.datasource.TopicRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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

    @Provides
    fun provideTopicDao(db: NotesDatabase): TopicDao = db.topicDao()

    @Provides
    @Singleton
    fun provideTopicRepository(
        dao: TopicDao,
        remoteDataSource: TopicRemoteDataSource  // ← Hilt injeta via NetworkModule
    ): TopicRepository = TopicRepositoryImpl(
        topicDao = dao,
        remoteDataSource = remoteDataSource
    )
}
