package com.notes.core.network.api

import com.notes.core.network.dto.PostDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato Retrofit da API JSONPlaceholder.
 * Base URL: https://jsonplaceholder.typicode.com/
 */
interface TopicsApi {

    /** Retorna TODOS os posts (sem paginação — usado no sync completo) */
    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    /**
     * Retorna posts com cursor pagination.
     *
     * JSONPlaceholder suporta _start (offset do cursor) e _limit:
     *   GET /posts?_start=0&_limit=10   → posts 1..10
     *   GET /posts?_start=10&_limit=10  → posts 11..20
     *
     * O cabeçalho X-Total-Count retorna o total disponível.
     * Em uma API real, o servidor retornaria um campo "nextCursor" no body.
     *
     * @param start posição inicial (cursor = id do último item recebido)
     * @param limit quantidade de itens por página (default: 20)
     */
    @GET("posts")
    suspend fun getPostsPaged(
        @Query("_start") start: Int = 0,
        @Query("_limit") limit: Int = DEFAULT_PAGE_SIZE
    ): List<PostDto>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): PostDto

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
