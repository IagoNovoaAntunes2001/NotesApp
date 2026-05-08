package com.notes.core.network.datasource;

import com.notes.core.network.api.TopicsApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class TopicRemoteDataSourceImpl_Factory implements Factory<TopicRemoteDataSourceImpl> {
  private final Provider<TopicsApi> apiProvider;

  public TopicRemoteDataSourceImpl_Factory(Provider<TopicsApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public TopicRemoteDataSourceImpl get() {
    return newInstance(apiProvider.get());
  }

  public static TopicRemoteDataSourceImpl_Factory create(Provider<TopicsApi> apiProvider) {
    return new TopicRemoteDataSourceImpl_Factory(apiProvider);
  }

  public static TopicRemoteDataSourceImpl newInstance(TopicsApi api) {
    return new TopicRemoteDataSourceImpl(api);
  }
}
