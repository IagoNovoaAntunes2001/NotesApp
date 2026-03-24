package com.notes

import android.app.Application
import com.notes.core.data.di.dataModule
import com.notes.core.database.di.databaseModule
import com.notes.detail.di.detailModule
import com.notes.home.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NotesApplication)
            modules(
                databaseModule,
                dataModule,
                homeModule,
                detailModule
            )
        }
    }
}
