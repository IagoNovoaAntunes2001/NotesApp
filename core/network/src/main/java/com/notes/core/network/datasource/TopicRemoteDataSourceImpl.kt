package com.notes.core.network.datasource

import com.notes.core.network.api.TopicsApi
import com.notes.core.network.dto.TopicDto
import com.notes.core.network.mapper.toTopicDto
import javax.inject.Inject

/**
 * Implementação REAL — chama https://jsonplaceholder.typicode.com/posts
 * e converte PostDto → TopicDto para o resto do app não saber da diferença.
 */
class TopicRemoteDataSourceImpl @Inject constructor(
    private val api: TopicsApi
) : TopicRemoteDataSource {

    override suspend fun fetchTopics(): List<TopicDto> =
        api.getPosts().map { it.toTopicDto() }   // PostDto → TopicDto

    // JSONPlaceholder é read-only (POST/PUT/DELETE fazem fake no servidor)
    // mas deixamos implementado para quando migrar para API real
    override suspend fun createTopic(dto: TopicDto): TopicDto = dto
    override suspend fun updateTopic(id: Int, dto: TopicDto): TopicDto = dto
    override suspend fun deleteTopic(id: Int) = Unit
}
