package com.notes.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notes.core.database.entity.TopicEntity

@Dao
interface TopicDao {

    @Query("SELECT * FROM topics")
    suspend fun getAll(): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity)

    @Delete
    suspend fun delete(topic: TopicEntity)
}
