package com.notes.core.model

data class Topic(
    val id: Int = 0,
    val title: String,
    val description: String,
    /**
     * Timestamp Unix em milissegundos da última edição.
     * Usado pelo algoritmo Last Write Wins (LWW) para decidir
     * quem vence em caso de conflito: server vs local.
     */
    val updatedAt: Long = 0L,
    /**
     * Estado de sincronização com o servidor.
     * Usado para mostrar ícone de status na UI (✓ SYNCED, ⏳ PENDING, ⚡ CONFLICT, ✗ ERROR).
     */
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
