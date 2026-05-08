package com.notes.core.data.repository

import com.notes.core.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    // Reativo — UI sempre observa daqui
    fun getTopicsStream(): Flow<List<Topic>>

    // Pontual — para queries únicas
    suspend fun getTopics(): List<Topic>
    suspend fun getTopicById(id: Int): Topic?

    // Escrita
    suspend fun insertTopic(topic: Topic)
    suspend fun deleteTopic(topic: Topic)

    /**
     * Atualiza um tópico existente.
     * Implementa o padrão Optimistic Update:
     *   1. Salva no Room imediatamente (persiste localmente)
     *   2. Envia para a API em seguida (pode falhar → rollback amanhã)
     *
     * Retorna Result.success se ambos os passos funcionaram.
     * Retorna Result.failure se a API falhou (Room já tem o dado novo).
     */
    suspend fun updateTopic(topic: Topic): Result<Unit>

    // Sync com API (SSOT)
    suspend fun sync(): Result<Unit>
}
