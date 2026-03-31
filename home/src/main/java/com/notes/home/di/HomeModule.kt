package com.notes.home.di

import com.notes.home.presentation.resources.HomeResources
import com.notes.home.presentation.resources.HomeResourcesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @Module: declara um módulo Hilt com provedores de dependências.
// @InstallIn(SingletonComponent::class): as dependências aqui vivem enquanto
//   o app viver (mesmo escopo de Application).
// @Binds: diz ao Hilt "quando alguém pedir HomeResources, dê HomeResourcesImpl".
//   É mais eficiente que @Provides pois não gera código extra — só mapeia interfaces.
@Module
@InstallIn(SingletonComponent::class)
internal abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeResources(impl: HomeResourcesImpl): HomeResources
}
