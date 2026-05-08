package com.notes.core.model

/**
 * Sealed interface que representa os 3 estados possíveis de uma operação de dados:
 *
 * - [Loading]  operação em andamento (ex: buscando da API)
 * - [Success]  dados disponíveis (pode vir do cache ou da API)
 * - [Error]    falha (ex: sem internet), mas pode conter dados em cache
 *
 * Por que [Error] tem [cachedData]?
 * Para implementar "nunca mostrar tela em branco se tem cache".
 * Se a API falhou mas há dados no Room, passamos eles aqui.
 */
sealed interface AppResult<out T> {

    data object Loading : AppResult<Nothing>

    data class Success<T>(
        val data: T
    ) : AppResult<T>

    data class Error<T>(
        val exception: Throwable,
        val cachedData: T? = null  // dados do cache, se disponíveis
    ) : AppResult<T>
}

