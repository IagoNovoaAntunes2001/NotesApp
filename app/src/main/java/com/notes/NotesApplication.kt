package com.notes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp dispara a geração de código do Hilt e inicializa
// o grafo de dependências global (SingletonComponent) automaticamente.
// Não precisamos mais listar módulos manualmente — o Hilt os descobre
// via @InstallIn em cada @Module espalhado pelos módulos do projeto.
@HiltAndroidApp
class NotesApplication : Application()
