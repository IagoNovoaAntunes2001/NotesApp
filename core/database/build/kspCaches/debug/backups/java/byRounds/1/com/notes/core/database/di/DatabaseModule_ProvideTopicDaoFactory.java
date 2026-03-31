package com.notes.core.database.di;

import com.notes.core.database.NotesDatabase;
import com.notes.core.database.dao.TopicDao;
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
public final class DatabaseModule_ProvideTopicDaoFactory implements Factory<TopicDao> {
  private final Provider<NotesDatabase> dbProvider;

  public DatabaseModule_ProvideTopicDaoFactory(Provider<NotesDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TopicDao get() {
    return provideTopicDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideTopicDaoFactory create(Provider<NotesDatabase> dbProvider) {
    return new DatabaseModule_ProvideTopicDaoFactory(dbProvider);
  }

  public static TopicDao provideTopicDao(NotesDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTopicDao(db));
  }
}
