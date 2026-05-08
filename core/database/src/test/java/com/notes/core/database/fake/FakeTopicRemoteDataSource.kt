package com.notes.core.database.fake

import com.notes.core.network.datasource.TopicRemoteDataSource
import com.notes.core.network.dto.TopicDto
import java.io.IOException

/**
 * Fake do TopicRemoteDataSource com 2 modos configuráveis:
 *
 * - [shouldFail] = false → simula API online, retorna [topics]
 * - [shouldFail] = true  → simula API offline, lança IOException
 *
 * Isso permite testar os 3 cenários sem precisar de internet real:
 *   Cenário 1: shouldFail=false, topics=<lista>   → API funciona
 *   Cenário 2: shouldFail=true, dao=<com dados>   → API falha, cache disponível
 *   Cenário 3: shouldFail=true, dao=<vazio>       → API falha, sem cache
 */
class FakeTopicRemoteDataSource(
    private val topics: List<TopicDto> = emptyList(),
    var shouldFail: Boolean = false
) : TopicRemoteDataSource {

    override suspend fun fetchTopics(): List<TopicDto> {
        if (shouldFail) throw IOException("Sem conexão com a internet")
        return topics
    }

    // Stubs: não usados nos testes de sync, mas necessários pela interface
    override suspend fun createTopic(dto: TopicDto): TopicDto = dto
    override suspend fun updateTopic(id: Int, dto: TopicDto): TopicDto = dto
    override suspend fun deleteTopic(id: Int) = Unit
}
