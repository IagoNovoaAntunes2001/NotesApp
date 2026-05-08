package com.notes.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa exatamente o JSON que vem de
 * https://jsonplaceholder.typicode.com/posts
 *
 * {
 *   "userId": 2,
 *   "id": 12,
 *   "title": "in quibusdam tempore odit est dolorem",
 *   "body": "itaque id aut magnam..."
 * }
 */
data class PostDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("id")     val id: Int,
    @SerializedName("title")  val title: String,
    @SerializedName("body")   val body: String
)

