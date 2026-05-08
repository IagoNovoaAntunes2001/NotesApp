package com.notes.core.network.di;

import com.notes.core.network.api.TopicsApi;
import com.notes.core.network.datasource.TopicRemoteDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_ProvideTopicRemoteDataSourceFactory implements Factory<TopicRemoteDataSource> {
  private final Provider<TopicsApi> apiProvider;

  public NetworkModule_ProvideTopicRemoteDataSourceFactory(Provider<TopicsApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public TopicRemoteDataSource get() {
    return provideTopicRemoteDataSource(apiProvider.get());
  }

  public static NetworkModule_ProvideTopicRemoteDataSourceFactory create(
      Provider<TopicsApi> apiProvider) {
    return new NetworkModule_ProvideTopicRemoteDataSourceFactory(apiProvider);
  }

  public static TopicRemoteDataSource provideTopicRemoteDataSource(TopicsApi api) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTopicRemoteDataSource(api));
  }
}
