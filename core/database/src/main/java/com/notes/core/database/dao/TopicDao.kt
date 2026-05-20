package com.notes.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notes.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    // Reativo — UI observa este (sem suspend, retorna Flow)
    @Query("SELECT * FROM topics")
    fun getAllStream(): Flow<List<TopicEntity>>

    // Pontual — para sync e queries únicas
    @Query("SELECT * FROM topics")
    suspend fun getAll(): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity)

    // Batch upsert — usado pelo SyncWorker
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(topics: List<TopicEntity>)

    // Busca apenas registros que precisam ser enviados ao servidor
    @Query("SELECT * FROM topics WHERE syncStatus = 'PENDING'")
    suspend fun getPendingTopics(): List<TopicEntity>

    @Delete
    suspend fun delete(topic: TopicEntity)

    /**
     * Remove tópicos com syncStatus = ERROR cujo updatedAt é anterior ao threshold.
     * Usado pelo CleanupWorker como fase final da cadeia de sync.
     * Threshold = System.currentTimeMillis() - 7 dias (em ms)
     */
    @Query("DELETE FROM topics WHERE syncStatus = 'ERROR' AND updatedAt < :thresholdMs")
    suspend fun deleteErrorTopicsOlderThan(thresholdMs: Long)
}
