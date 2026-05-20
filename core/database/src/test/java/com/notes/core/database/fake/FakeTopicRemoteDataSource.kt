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
 * [topics] é var para que cada teste possa configurar o payload da API,
 * incluindo [TopicDto.updatedAt] para testar o algoritmo Last Write Wins.
 */
class FakeTopicRemoteDataSource(
    var topics: List<TopicDto> = emptyList(),
    var shouldFail: Boolean = false
) : TopicRemoteDataSource {

    override suspend fun fetchTopics(): List<TopicDto> {
        if (shouldFail) throw IOException("Sem conexão com a internet")
        return topics
    }

    // Stubs: necessários pela interface
    override suspend fun createTopic(dto: TopicDto): TopicDto {
        if (shouldFail) throw IOException("Sem conexão com a internet")
        return dto
    }
    override suspend fun updateTopic(id: Int, dto: TopicDto): TopicDto {
        if (shouldFail) throw IOException("Sem conexão com a internet")
        return dto
    }
    override suspend fun deleteTopic(id: Int) = Unit
}
