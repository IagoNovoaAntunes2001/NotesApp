package com.notes.core.network.di;

import com.notes.core.network.api.TopicsApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideTopicsApiFactory implements Factory<TopicsApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideTopicsApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public TopicsApi get() {
    return provideTopicsApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideTopicsApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideTopicsApiFactory(retrofitProvider);
  }

  public static TopicsApi provideTopicsApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTopicsApi(retrofit));
  }
}
