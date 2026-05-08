package com.notes.core.network.datasource

import com.notes.core.network.dto.TopicDto

/**
 * Contrato do RemoteDataSource — a interface que o Repository conhece.
 *
 * O Repository depende desta INTERFACE (não da implementação Retrofit).
 * Isso permite trocar a implementação (Retrofit → Ktor → Fake) sem
 * tocar no Repository — Dependency Inversion Principle (SOLID).
 */
interface TopicRemoteDataSource {
    suspend fun fetchTopics(): List<TopicDto>
    suspend fun createTopic(dto: TopicDto): TopicDto
    suspend fun updateTopic(id: Int, dto: TopicDto): TopicDto
    suspend fun deleteTopic(id: Int)
}
