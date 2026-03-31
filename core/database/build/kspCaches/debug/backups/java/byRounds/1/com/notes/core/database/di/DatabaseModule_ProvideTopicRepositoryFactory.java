package com.notes.core.database.di;

import com.notes.core.data.repository.TopicRepository;
import com.notes.core.database.dao.TopicDao;
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
public final class DatabaseModule_ProvideTopicRepositoryFactory implements Factory<TopicRepository> {
  private final Provider<TopicDao> daoProvider;

  public DatabaseModule_ProvideTopicRepositoryFactory(Provider<TopicDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public TopicRepository get() {
    return provideTopicRepository(daoProvider.get());
  }

  public static DatabaseModule_ProvideTopicRepositoryFactory create(
      Provider<TopicDao> daoProvider) {
    return new DatabaseModule_ProvideTopicRepositoryFactory(daoProvider);
  }

  public static TopicRepository provideTopicRepository(TopicDao dao) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTopicRepository(dao));
  }
}
