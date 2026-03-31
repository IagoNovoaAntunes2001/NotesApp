package com.notes.core.data.di;

import com.notes.core.data.repository.TopicRepository;
import com.notes.core.data.usecase.GetTopicByIdUseCase;
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
public final class DataModule_ProvideGetTopicByIdUseCaseFactory implements Factory<GetTopicByIdUseCase> {
  private final Provider<TopicRepository> repoProvider;

  public DataModule_ProvideGetTopicByIdUseCaseFactory(Provider<TopicRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public GetTopicByIdUseCase get() {
    return provideGetTopicByIdUseCase(repoProvider.get());
  }

  public static DataModule_ProvideGetTopicByIdUseCaseFactory create(
      Provider<TopicRepository> repoProvider) {
    return new DataModule_ProvideGetTopicByIdUseCaseFactory(repoProvider);
  }

  public static GetTopicByIdUseCase provideGetTopicByIdUseCase(TopicRepository repo) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideGetTopicByIdUseCase(repo));
  }
}
