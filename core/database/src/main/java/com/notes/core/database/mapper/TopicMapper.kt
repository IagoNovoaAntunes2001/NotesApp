package com.notes.core.database.mapper

import com.notes.core.database.entity.TopicEntity
import com.notes.core.model.SyncStatus
import com.notes.core.model.Topic

fun TopicEntity.toDomain() = Topic(
    id = id,
    title = title,
    description = description,
    updatedAt = updatedAt,
    // Converte String → SyncStatus com fallback seguro: String corrompida → PENDING
    syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.PENDING)
)

fun Topic.toEntity() = TopicEntity(
    id = id,
    title = title,
    description = description,
    updatedAt = updatedAt,
    // Converte SyncStatus → String (nome do enum)
    syncStatus = syncStatus.name
)
