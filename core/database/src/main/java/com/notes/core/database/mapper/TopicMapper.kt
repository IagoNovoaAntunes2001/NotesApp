package com.notes.core.database.mapper

import com.notes.core.database.entity.TopicEntity
import com.notes.core.model.Topic

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
