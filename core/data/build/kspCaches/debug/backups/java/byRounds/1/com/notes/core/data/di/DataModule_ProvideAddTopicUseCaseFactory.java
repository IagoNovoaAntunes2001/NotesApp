package com.notes.core.data.di;

import com.notes.core.data.repository.TopicRepository;
import com.notes.core.data.usecase.AddTopicUseCase;
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
public final class DataModule_ProvideAddTopicUseCaseFactory implements Factory<AddTopicUseCase> {
  private final Provider<TopicRepository> repoProvider;

  public DataModule_ProvideAddTopicUseCaseFactory(Provider<TopicRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public AddTopicUseCase get() {
    return provideAddTopicUseCase(repoProvider.get());
  }

  public static DataModule_ProvideAddTopicUseCaseFactory create(
      Provider<TopicRepository> repoProvider) {
    return new DataModule_ProvideAddTopicUseCaseFactory(repoProvider);
  }

  public static AddTopicUseCase provideAddTopicUseCase(TopicRepository repo) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideAddTopicUseCase(repo));
  }
}
