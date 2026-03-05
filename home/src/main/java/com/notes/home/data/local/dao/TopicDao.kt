package com.notes.home.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notes.home.data.local.entity.TopicEntity

@Dao
interface TopicDao {

    @Query("SELECT * FROM topics")
    suspend fun getAll(): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity)

    @Delete
    suspend fun delete(topic: TopicEntity)
}
