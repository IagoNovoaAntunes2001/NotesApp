package com.notes.home.di

import com.notes.home.presentation.HomeViewModel
import com.notes.home.presentation.resources.HomeResources
import com.notes.home.presentation.resources.HomeResourcesImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    single<HomeResources> { HomeResourcesImpl(context = androidContext()) }

    viewModel {
        HomeViewModel(
            getTopicsUseCase = get(),
            addTopicUseCase = get(),
            deleteTopicUseCase = get(),
            savedStateHandle = get(),
            resources = get()
        )
    }
}
