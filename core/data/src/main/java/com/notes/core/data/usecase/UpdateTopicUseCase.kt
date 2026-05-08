package com.notes.core.data.usecase

import com.notes.core.data.repository.TopicRepository
import com.notes.core.model.Topic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Executa o padrão Optimistic Update para edição de um tópico.
 *
 * O fluxo completo (orquestrado pelo Repository):
 *   1. Salva no Room imediatamente → Flow emite → UI atualiza sozinha
 *   2. Envia para API em background
 *   3. Retorna Result<Unit>:
 *      - success → tudo sincronizado
 *      - failure → dado está no Room mas API rejeitou (rollback = Terça-feira)
 *
 * Por que [ioDispatcher]?
 *   Room e Retrofit são operações de I/O — precisam de Dispatchers.IO.
 *   Passamos o dispatcher como parâmetro para facilitar testes (injetamos TestDispatcher).
 */
class UpdateTopicUseCase(
    private val topicRepository: TopicRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(topic: Topic): Result<Unit> = withContext(ioDispatcher) {
        topicRepository.updateTopic(topic)
    }
}

