package com.notes.core.database.repository

import com.notes.core.data.repository.TopicRepository
import com.notes.core.database.dao.TopicDao
import com.notes.core.database.mapper.toDomain
import com.notes.core.database.mapper.toEntity
import com.notes.core.model.Topic
import com.notes.core.network.datasource.TopicRemoteDataSource
import com.notes.core.network.mapper.toDomain
import com.notes.core.network.mapper.toDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TopicRepositoryImpl(
    private val topicDao: TopicDao,
    private val remoteDataSource: TopicRemoteDataSource
) : TopicRepository {

    // Reativo: UI observa este Flow — atualiza automaticamente quando Room muda
    override fun getTopicsStream(): Flow<List<Topic>> =
        topicDao.getAllStream().map { entities -> entities.map { it.toDomain() } }

    // Pontual
    override suspend fun getTopics(): List<Topic> =
        topicDao.getAll().map { it.toDomain() }

    override suspend fun getTopicById(id: Int): Topic? =
        topicDao.getById(id)?.toDomain()

    // Escrita
    override suspend fun insertTopic(topic: Topic) =
        topicDao.insert(topic.toEntity())

    override suspend fun deleteTopic(topic: Topic) =
        topicDao.delete(topic.toEntity())

    /**
     * Optimistic Update:
     *
     * Passo 1 — Salva no Room IMEDIATAMENTE.
     *   O Flow do DAO emite na hora → UI já vê o dado atualizado.
     *   Isso é o "optimistic": agimos como se fosse funcionar.
     *
     * Passo 2 — Chama a API.
     *   Se funcionar: tudo certo, dado está consistente local + remoto.
     *   Se falhar: Room já tem o novo dado, mas a API não.
     *              → amanhã (Terça-feira): rollback para o dado anterior.
     *
     * Por que Room antes da API?
     *   UX instantânea: o usuário vê a mudança sem esperar latência de rede.
     *   Offline-first: se não há internet, o dado fica salvo localmente.
     */
    override suspend fun updateTopic(topic: Topic): Result<Unit> = runCatching {
        // Passo 1: persiste localmente (REPLACE = upsert)
        topicDao.insert(topic.toEntity())

        // Passo 2: tenta sincronizar com a API
        remoteDataSource.updateTopic(topic.id, topic.toDto())
    }

    // Sync: padrão Network → DB → UI
    // 1) Busca da API (real ou fake)
    // 2) Salva no Room (upsert em batch)
    // 3) Flow emite automaticamente → UI atualiza sozinha
    override suspend fun sync(): Result<Unit> = runCatching {
        val remoteDtos = remoteDataSource.fetchTopics()
        val entities = remoteDtos.map { dto -> dto.toDomain().toEntity() }
        topicDao.insertAll(entities)
    }
}
