package com.notes.core.network.api

import com.notes.core.network.dto.PostDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Contrato Retrofit da API JSONPlaceholder.
 * Base URL: https://jsonplaceholder.typicode.com/
 */
interface TopicsApi {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): PostDto
}
