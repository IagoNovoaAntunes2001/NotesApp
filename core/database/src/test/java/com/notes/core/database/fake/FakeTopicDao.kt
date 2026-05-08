package com.notes.core.database.fake

import com.notes.core.database.dao.TopicDao
import com.notes.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fake manual do TopicDao para testes unitários.
 *
 * Por que Fake em vez de Mock (Mockk/Mockito)?
 * - Um Fake é uma implementação real simplificada — mais legível e fácil de entender
 * - O comportamento real do DAO (armazenar em memória, emitir via Flow) fica explícito
 * - Não precisa de anotações nem de bibliotecas externas para funcionar
 *
 * O MutableStateFlow simula o comportamento reativo do Room:
 * qualquer chamada a insert/delete/insertAll atualiza o Flow → os coletores recebem os dados novos.
 */
class FakeTopicDao : TopicDao {

    // Simula a tabela "topics" em memória
    private val _topics = MutableStateFlow<List<TopicEntity>>(emptyList())

    // Expõe o estado como Flow somente-leitura (igual ao Room)
    override fun getAllStream(): Flow<List<TopicEntity>> = _topics.asStateFlow()

    override suspend fun getAll(): List<TopicEntity> = _topics.value

    override suspend fun getById(id: Int): TopicEntity? =
        _topics.value.firstOrNull { it.id == id }

    override suspend fun insert(topic: TopicEntity) {
        _topics.update { current ->
            // Remove o existente com mesmo id e adiciona o novo (simula REPLACE)
            current.filterNot { it.id == topic.id } + topic
        }
    }

    override suspend fun insertAll(topics: List<TopicEntity>) {
        _topics.update { current ->
            val existingIds = topics.map { it.id }.toSet()
            current.filterNot { it.id in existingIds } + topics
        }
    }

    override suspend fun delete(topic: TopicEntity) {
        _topics.update { current -> current.filterNot { it.id == topic.id } }
    }
}

