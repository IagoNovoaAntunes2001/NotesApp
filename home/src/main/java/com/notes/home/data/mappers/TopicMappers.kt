package com.notes.home.data.mappers

import com.notes.home.data.local.entity.TopicEntity
import com.notes.home.domain.entities.Topic

fun TopicEntity.toDomain() = Topic(
    id = id,
    title = title,
    description = description
)

fun Topic.toEntity() = TopicEntity(
    id = id,
    title = title,
    description = description
)

