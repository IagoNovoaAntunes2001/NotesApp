package com.notes.detail.presentation;

import com.notes.core.data.usecase.GetTopicByIdUseCase;
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
public final class DetailViewModel_Factory implements Factory<DetailViewModel> {
  private final Provider<GetTopicByIdUseCase> getTopicByIdUseCaseProvider;

  public DetailViewModel_Factory(Provider<GetTopicByIdUseCase> getTopicByIdUseCaseProvider) {
    this.getTopicByIdUseCaseProvider = getTopicByIdUseCaseProvider;
  }

  @Override
  public DetailViewModel get() {
    return newInstance(getTopicByIdUseCaseProvider.get());
  }

  public static DetailViewModel_Factory create(
      Provider<GetTopicByIdUseCase> getTopicByIdUseCaseProvider) {
    return new DetailViewModel_Factory(getTopicByIdUseCaseProvider);
  }

  public static DetailViewModel newInstance(GetTopicByIdUseCase getTopicByIdUseCase) {
    return new DetailViewModel(getTopicByIdUseCase);
  }
}
