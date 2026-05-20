package com.notes.core.network.mapper

import com.notes.core.model.Topic
import com.notes.core.network.dto.PostDto
import com.notes.core.network.dto.TopicDto

/**
 * Mapper: PostDto (JSONPlaceholder) → TopicDto → Topic (domain)
 *
 * JSONPlaceholder /posts:         Nosso modelo:
 *   userId  (Int)    → ignorado
 *   id      (Int)    → id
 *   title   (String) → title
 *   body    (String) → description
 */
fun PostDto.toTopicDto(): TopicDto = TopicDto(
    id = id,
    title = title,
    description = body   // body da API vira description no nosso domínio
)

// TopicDto ↔ Domain
fun TopicDto.toDomain(): Topic = Topic(
    id = id,
    title = title,
    description = description,
    updatedAt = updatedAt
)

fun Topic.toDto(): TopicDto = TopicDto(
    id = id,
    title = title,
    description = description,
    updatedAt = updatedAt
)
