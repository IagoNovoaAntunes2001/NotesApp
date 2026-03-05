package com.notes.home.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notes.home.data.local.dao.TopicDao
import com.notes.home.data.local.entity.TopicEntity

@Database(
    entities = [TopicEntity::class],
    version = 1
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
}

