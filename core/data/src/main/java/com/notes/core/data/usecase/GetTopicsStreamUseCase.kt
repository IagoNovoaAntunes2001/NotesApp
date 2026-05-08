package com.notes.core.data.usecase

import com.notes.core.data.repository.TopicRepository
import com.notes.core.model.AppResult
import com.notes.core.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Retorna um Flow que emite AppResult representando os 3 cenários:
 *
 * 1. Loading  → enquanto ainda não chegou nenhuma lista do Room
 * 2. Success  → Room emitiu dados (pode ser lista vazia ou com itens)
 * 3. Error    → Room lançou exceção (raro, mas tratado)
 *
 * Nota: este UseCase só observa o Room (Source of Truth).
 * O sync com a API é responsabilidade de [SyncTopicsUseCase].
 * O ViewModel combina ambos para montar o UiState correto.
 */
class GetTopicsStreamUseCase(
    private val topicRepository: TopicRepository
) {
    operator fun invoke(): Flow<AppResult<List<Topic>>> =
        topicRepository.getTopicsStream()
            .map<List<Topic>, AppResult<List<Topic>>> { topics ->
                AppResult.Success(topics)
            }
            .onStart { emit(AppResult.Loading) }
            .catch { exception ->
                emit(AppResult.Error(exception = exception, cachedData = emptyList()))
            }
}
