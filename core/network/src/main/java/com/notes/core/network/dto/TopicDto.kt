package com.notes.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) — representa o JSON que vem da API.
 *
 * Por que separar de TopicEntity?
 * - A API pode mudar o contrato sem quebrar o banco de dados e vice-versa.
 * - Nomes/tipos diferentes: API usa String UUID, banco usa Int autoIncrement.
 * - Mapeamento explícito evita acoplamento entre camadas.
 */
data class TopicDto(
    @SerializedName("id")          val id: Int,
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("is_pinned")   val isPinned: Boolean = false,
    /**
     * Timestamp do servidor em milissegundos.
     * JSONPlaceholder não retorna este campo — default 0L para a fake API.
     * Uma API real retornaria o timestamp da última edição no servidor.
     */
    @SerializedName("updated_at")  val updatedAt: Long = 0L
)
