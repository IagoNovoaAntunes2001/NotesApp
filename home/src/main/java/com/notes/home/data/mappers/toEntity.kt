package com.notes.home.data.mappers

import com.notes.home.data.remote.dto.TopicDto
import com.notes.home.domain.entities.Topic

fun TopicDto.toEntity() = Topic(
    id = id,
    name = name,
    isActive = isActive ?: false
)
