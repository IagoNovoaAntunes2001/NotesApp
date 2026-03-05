package com.notes.home.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TopicDto(
    @SerializedName("topic_id") val id: String,
    @SerializedName("topic_name") val name: String,
    @SerializedName("is_active") val isActive: Boolean?
)
