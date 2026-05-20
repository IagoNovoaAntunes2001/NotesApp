package com.notes.core.database.repository

import com.notes.core.data.repository.TopicRepository
import com.notes.core.database.dao.TopicDao
import com.notes.core.database.mapper.toDomain
import com.notes.core.database.mapper.toEntity
import com.notes.core.model.SyncStatus
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
        // Novos tópicos criados localmente ficam PENDING até o próximo sync enviá-los ao servidor
        topicDao.insert(topic.copy(syncStatus = SyncStatus.PENDING).toEntity())

    override suspend fun deleteTopic(topic: Topic) =
        topicDao.delete(topic.toEntity())

    /**
     * Optimistic Update com Rollback:
     *
     * Passo 0 — Salva snapshot (estado anterior) para poder reverter.
     *
     * Passo 1 — Salva no Room IMEDIATAMENTE.
     *   O Flow do DAO emite na hora → UI já vê o dado atualizado.
     *   Isso é o "optimistic": agimos como se fosse funcionar.
     *
     * Passo 2 — Chama a API.
     *   Se funcionar: tudo certo, dado está consistente local + remoto.
     *   Se falhar: ROLLBACK — restaura o snapshot no Room → UI reverte
     *              automaticamente via Flow, e retorna Result.failure.
     *
     * Por que Room antes da API?
     *   UX instantânea: o usuário vê a mudança sem esperar latência de rede.
     *   Offline-first: se não há internet, o dado é revertido com feedback.
     */
    override suspend fun updateTopic(topic: Topic): Result<Unit> {
        // Passo 0: guarda snapshot antes de alterar
        val snapshot = topicDao.getById(topic.id)

        // Passo 1: stampa o timestamp E marca como PENDING (aguardando confirmação do servidor)
        val topicWithTimestamp = topic.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )

        // Passo 2: persiste localmente → UI atualiza via Flow (ícone ⏳ PENDING aparece)
        topicDao.insert(topicWithTimestamp.toEntity())

        // Passo 3: tenta sincronizar com a API
        return runCatching {
            remoteDataSource.updateTopic(topicWithTimestamp.id, topicWithTimestamp.toDto())
            // API confirmou → atualiza para SYNCED (ícone ✓ aparece)
            topicDao.insert(topicWithTimestamp.copy(syncStatus = SyncStatus.SYNCED).toEntity())
            Unit
        }.onFailure {
            // ROLLBACK: restaura estado anterior no Room → Flow emite → UI reverte
            if (snapshot != null) {
                topicDao.insert(snapshot)
            }
        }
    }

    /**
     * Last Write Wins (LWW):
     *
     * Para cada tópico que vem do servidor, comparamos o timestamp:
     *   server.updatedAt > local.updatedAt  →  SERVER VENCE  → salva no Room
     *   server.updatedAt ≤ local.updatedAt  →  LOCAL VENCE   → ignora o dado do servidor
     *   sem dado local (novo tópico)        →  SERVER VENCE  → salva no Room
     *
     * Por que LWW funciona para notas?
     * - Conflito real (dois devices editando ao mesmo tempo) é raro.
     * - A perda de dados em um conflito (a edição mais antiga é descartada) é aceitável.
     * - Apps como Google Keep, Notion e Bear usam LWW ou variações dele.
     *
     * Quando LWW NÃO é suficiente:
     * - Documentos colaborativos em tempo real → precisaria de CRDTs ou OT (Operational Transform)
     * - Ex: Google Docs usa OT para mesclar edições simultâneas sem perda
     */
    override suspend fun sync(): Result<Unit> = runCatching {
        val remoteDtos = remoteDataSource.fetchTopics()

        for (dto in remoteDtos) {
            val local = topicDao.getById(dto.id)

            when {
                // Novo do servidor → insere como SYNCED
                local == null -> {
                    topicDao.insert(dto.toDomain().copy(syncStatus = SyncStatus.SYNCED).toEntity())
                }
                // Server mais recente → server vence → SYNCED
                dto.updatedAt > local.updatedAt -> {
                    topicDao.insert(dto.toDomain().copy(syncStatus = SyncStatus.SYNCED).toEntity())
                }
                // Empate de timestamp → local mantido por convenção → CONFLICT
                dto.updatedAt == local.updatedAt -> {
                    topicDao.insert(local.copy(syncStatus = SyncStatus.CONFLICT.name))
                }
                // Local mais recente → local vence → mantém PENDING (edit offline protegida)
                else -> { /* não toca no dado local — mantém status atual */ }
            }
        }
    }

    /**
     * Push de pendentes ao servidor (usado pelo SyncWorker).
     *
     * Fluxo:
     *   1. Busca todos os tópicos com syncStatus = PENDING
     *   2. Para cada um, chama a API (update ou create)
     *   3. Se a API confirma → marca SYNCED no Room
     *   4. Se qualquer item falha → para e retorna failure
     *      (o item permanece PENDING → SyncWorker vai tentar de novo com backoff)
     */
    override suspend fun syncPending(): Result<Unit> = runCatching {
        val pendingTopics = topicDao.getPendingTopics()
        for (entity in pendingTopics) {
            val topic = entity.toDomain()
            remoteDataSource.updateTopic(topic.id, topic.toDto())
            topicDao.insert(entity.copy(syncStatus = SyncStatus.SYNCED.name))
        }
    }

    /**
     * Cleanup — remove do Room entradas que não são mais necessárias.
     *
     * O que é removido:
     *   - Tópicos com syncStatus = ERROR há mais de 7 dias
     *     → São falhas permanentes que o servidor rejeitou definitivamente.
     *       Manter esses registros para sempre polui o banco local.
     *
     * Por que 7 dias?
     *   - Dá tempo suficiente para o usuário perceber e tentar resolver.
     *   - Evita acúmulo infinito de lixo no Room.
     *
     * Chamado pelo CleanupWorker como 3ª fase da cadeia de sync.
     */
    override suspend fun cleanup(): Result<Unit> = runCatching {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        topicDao.deleteErrorTopicsOlderThan(sevenDaysAgo)
    }
}
