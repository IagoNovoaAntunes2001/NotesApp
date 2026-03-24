package com.notes.detail.di

import com.notes.detail.presentation.DetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detailModule = module {
    viewModel { DetailViewModel(getTopicByIdUseCase = get()) }
}
