package com.notes.core.network.datasource

import com.notes.core.network.dto.TopicDto
import com.notes.core.network.pagination.PagedResult

/**
 * Contrato do RemoteDataSource — a interface que o Repository conhece.
 *
 * O Repository depende desta INTERFACE (não da implementação Retrofit).
 * Isso permite trocar a implementação (Retrofit → Ktor → Fake) sem
 * tocar no Repository — Dependency Inversion Principle (SOLID).
 */
interface TopicRemoteDataSource {
    /** Busca todos os tópicos (sync completo — sem paginação) */
    suspend fun fetchTopics(): List<TopicDto>

    /**
     * Busca tópicos com cursor pagination.
     *
     * @param cursor posição de início (use o id do último item recebido)
     * @param limit itens por página
     */
    suspend fun fetchTopicsPaged(
        cursor: Int = 0,
        limit: Int = 20
    ): PagedResult<TopicDto>

    suspend fun createTopic(dto: TopicDto): TopicDto
    suspend fun updateTopic(id: Int, dto: TopicDto): TopicDto
    suspend fun deleteTopic(id: Int)
}
