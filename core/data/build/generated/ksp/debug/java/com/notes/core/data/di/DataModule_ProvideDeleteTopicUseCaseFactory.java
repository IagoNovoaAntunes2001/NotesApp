package com.notes.core.data.di;

import com.notes.core.data.repository.TopicRepository;
import com.notes.core.data.usecase.DeleteTopicUseCase;
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
public final class DataModule_ProvideDeleteTopicUseCaseFactory implements Factory<DeleteTopicUseCase> {
  private final Provider<TopicRepository> repoProvider;

  public DataModule_ProvideDeleteTopicUseCaseFactory(Provider<TopicRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public DeleteTopicUseCase get() {
    return provideDeleteTopicUseCase(repoProvider.get());
  }

  public static DataModule_ProvideDeleteTopicUseCaseFactory create(
      Provider<TopicRepository> repoProvider) {
    return new DataModule_ProvideDeleteTopicUseCaseFactory(repoProvider);
  }

  public static DeleteTopicUseCase provideDeleteTopicUseCase(TopicRepository repo) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideDeleteTopicUseCase(repo));
  }
}
