package com.notes.home.presentation.resources

import android.content.Context
import com.notes.home.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// @Inject constructor: o Hilt sabe como criar esta classe automaticamente.
// @ApplicationContext: qualificador do Hilt que fornece o Context da Application.
internal class HomeResourcesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HomeResources {
    override val topicAddedSuccess get() = context.getString(R.string.topic_added_success)
    override val topicDeletedSuccess get() = context.getString(R.string.topic_deleted_success)
    override val errorAddTopic get() = context.getString(R.string.error_add_topic)
    override val errorDeleteTopic get() = context.getString(R.string.error_delete_topic)
    override val offlineWithCache get() = context.getString(R.string.offline_with_cache)
    override val offlineNoCacheTitle get() = context.getString(R.string.offline_no_cache_title)
    override val offlineNoCacheMessage get() = context.getString(R.string.offline_no_cache_message)
    override val retryButton get() = context.getString(R.string.retry_button)
    override val syncing get() = context.getString(R.string.syncing)
    override val offlineBanner get() = context.getString(R.string.offline_banner)
}
