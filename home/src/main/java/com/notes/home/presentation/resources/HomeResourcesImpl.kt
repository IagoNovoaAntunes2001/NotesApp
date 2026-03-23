package com.notes.home.presentation.resources

import android.content.Context
import com.notes.home.R

class HomeResourcesImpl(private val context: Context) : HomeResources {
    override val topicAddedSuccess: String
        get() = context.getString(R.string.topic_added_success)

    override val topicDeletedSuccess: String
        get() = context.getString(R.string.topic_deleted_success)

    override val errorAddTopic: String
        get() = context.getString(R.string.error_add_topic)

    override val errorDeleteTopic: String
        get() = context.getString(R.string.error_delete_topic)
}
