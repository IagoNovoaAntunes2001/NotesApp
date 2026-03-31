package com.notes.core.database.di;

import android.content.Context;
import com.notes.core.database.NotesDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideNotesDatabaseFactory implements Factory<NotesDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideNotesDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NotesDatabase get() {
    return provideNotesDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideNotesDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideNotesDatabaseFactory(contextProvider);
  }

  public static NotesDatabase provideNotesDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideNotesDatabase(context));
  }
}
