package com.notes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notes.core.database.dao.TopicDao
import com.notes.core.database.entity.TopicEntity

@Database(
    entities = [TopicEntity::class],
    version = 1
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
}
