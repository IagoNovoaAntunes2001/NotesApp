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
     * Atualiza um tópico existente com Optimistic Update + Rollback:
     *   1. Salva snapshot do estado atual
     *   2. Salva no Room imediatamente → UI atualiza via Flow
     *   3. Envia para a API
     *      - Sucesso �� retorna Result.success, dado consistente
     *      - Falha   → ROLLBACK: restaura snapshot no Room → UI reverte via Flow
     *                  retorna Result.failure
     */
    suspend fun updateTopic(topic: Topic): Result<Unit>

    // Sync com API (SSOT)
    suspend fun sync(): Result<Unit>

    /**
     * Push de itens PENDING ao servidor.
     * Chamado pelo SyncWorker para garantir que edições offline são enviadas.
     * Cada item enviado com sucesso tem seu syncStatus atualizado para SYNCED.
     */
    suspend fun syncPending(): Result<Unit>

    /**
     * Cleanup — remove do Room entradas que não são mais necessárias:
     *   - Tópicos com syncStatus = ERROR há mais de 7 dias (falha permanente)
     *   - Cache antigo além do limite configurado
     * Chamado pelo CleanupWorker como fase final da cadeia de sync.
     */
    suspend fun cleanup(): Result<Unit>
}
