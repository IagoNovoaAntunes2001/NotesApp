package com.notes.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    /**
     * Timestamp Unix em milissegundos — controla quem vence no LWW.
     * Default 0L: registros antigos (antes da migration) perdem
     * para qualquer server timestamp, forçando uma atualização no 1º sync.
     */
    val updatedAt: Long = 0L,
    /**
     * Estado de sincronização armazenado como String (nome do enum).
     * Room não suporta enums nativamente — armazenamos o name() e convertemos no mapper.
     * Default "SYNCED" para registros existentes (considerados já sincronizados).
     */
    val syncStatus: String = "SYNCED"
)
