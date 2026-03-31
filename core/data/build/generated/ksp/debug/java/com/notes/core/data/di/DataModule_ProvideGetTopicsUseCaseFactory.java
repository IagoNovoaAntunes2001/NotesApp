package com.notes.core.data.di;

import com.notes.core.data.repository.TopicRepository;
import com.notes.core.data.usecase.GetTopicsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_ProvideGetTopicsUseCaseFactory implements Factory<GetTopicsUseCase> {
  private final Provider<TopicRepository> repoProvider;

  public DataModule_ProvideGetTopicsUseCaseFactory(Provider<TopicRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public GetTopicsUseCase get() {
    return provideGetTopicsUseCase(repoProvider.get());
  }

  public static DataModule_ProvideGetTopicsUseCaseFactory create(
      Provider<TopicRepository> repoProvider) {
    return new DataModule_ProvideGetTopicsUseCaseFactory(repoProvider);
  }

  public static GetTopicsUseCase provideGetTopicsUseCase(TopicRepository repo) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideGetTopicsUseCase(repo));
  }
}
