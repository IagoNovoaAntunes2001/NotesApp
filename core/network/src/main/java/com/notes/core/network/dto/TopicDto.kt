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
    @SerializedName("is_pinned")   val isPinned: Boolean = false
)
