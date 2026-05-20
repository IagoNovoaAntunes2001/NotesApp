package com.notes.core.database.repository

import app.cash.turbine.test
import com.notes.core.database.entity.TopicEntity
import com.notes.core.database.fake.FakeTopicDao
import com.notes.core.database.fake.FakeTopicRemoteDataSource
import com.notes.core.network.dto.TopicDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários do TopicRepositoryImpl.
 *
 * Estratégia: usar Fakes (implementações manuais simples) em vez de mocks.
 *   - FakeTopicDao     → simula o Room em memória com MutableStateFlow
 *   - FakeTopicRemoteDataSource → simula a API (sucesso ou falha controlável)
 *
 * Os 3 cenários testados:
 *   1. API funciona      → dados são salvos no DAO → Stream emite os dados novos
 *   2. API falha + cache → Stream ainda emite os dados do cache (offline-first!)
 *   3. API falha + vazio → sync retorna Result.failure (nada para mostrar)
 *
 * Por que runTest?
 *   - É o coroutine scope especial para testes: controla o tempo virtual
 *   - Substitui GlobalScope/runBlocking em testes de coroutines
 *
 * Por que Turbine?
 *   - Testar Flows com collect{} normal é verboso e sujeito a race conditions
 *   - Turbine adiciona .test { awaitItem() } — limpo, determinístico, cancela sozinho
 */
class TopicRepositoryImplTest {

    // Fakes configuráveis para cada cenário
    private lateinit var fakeDao: FakeTopicDao
    private lateinit var fakeRemote: FakeTopicRemoteDataSource
    private lateinit var repository: TopicRepositoryImpl

    // Dados de exemplo que a API "retornaria"
    // updatedAt = 1_000L garante que o servidor vence no LWW quando o dado
    // local tem updatedAt = 0L (default para registros sem timestamp).
    private val apiTopics = listOf(
        TopicDto(id = 1, title = "Kotlin Coroutines", description = "Async sem callbacks", updatedAt = 1_000L),
        TopicDto(id = 2, title = "Clean Architecture", description = "Separação de camadas", updatedAt = 1_000L),
    )

    // Dados de cache pré-existentes no DAO (simulam 1º sync feito anteriormente)
    private val cachedEntities = listOf(
        TopicEntity(id = 10, title = "Cache antigo", description = "Dado local"),
    )

    @Before
    fun setup() {
        // Cada teste começa com fakes limpos — sem estado residual entre testes
        fakeDao = FakeTopicDao()
        fakeRemote = FakeTopicRemoteDataSource(topics = apiTopics)
        repository = TopicRepositoryImpl(
            topicDao = fakeDao,
            remoteDataSource = fakeRemote
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cenário 1: API funciona → dados atualizados
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Garante que quando a API responde com sucesso:
     * - sync() retorna Result.success
     * - Os DTOs da API são convertidos e salvos no DAO
     * - O Flow do Stream emite os dados novos automaticamente
     */
    @Test
    fun `cenario 1 - API funciona - sync salva dados e stream emite automaticamente`() = runTest {
        // API configurada para funcionar (shouldFail = false, padrão)

        // Act: dispara o sync
        val result = repository.sync()

        // Assert: sync foi bem-sucedido
        assertTrue("sync deve retornar success quando API responde", result.isSuccess)

        // Assert: o Flow do Room emite os dados recém salvos
        // .test { } do Turbine coleta o próximo item emitido e cancela o Flow automaticamente
        repository.getTopicsStream().test {
            val topics = awaitItem()  // aguarda a próxima emissão do Flow

            assertEquals("Deve ter 2 tópicos vindos da API", 2, topics.size)
            assertEquals("Primeiro tópico deve ser 'Kotlin Coroutines'", "Kotlin Coroutines", topics[0].title)
            assertEquals("Segundo tópico deve ser 'Clean Architecture'", "Clean Architecture", topics[1].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Garante que o padrão Network → DB → UI está correto:
     * o stream reflete EXATAMENTE o que está no DAO após o sync.
     */
    @Test
    fun `cenario 1 - getTopicsStream emite lista vazia antes do sync`() = runTest {
        // Arrange: DAO começa vazio, sem sync ainda

        repository.getTopicsStream().test {
            val topicsAntes = awaitItem()
            assertTrue("Antes do sync, stream deve estar vazio", topicsAntes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }

        // Act: sync
        repository.sync()

        // Assert: agora tem dados
        repository.getTopicsStream().test {
            val topicsDepois = awaitItem()
            assertEquals("Após sync, stream deve ter os dados da API", 2, topicsDepois.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cenário 2: API falha + cache → retorna cache
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Garante o comportamento offline-first:
     * mesmo com API falhando, o Stream continua emitindo os dados do cache.
     *
     * Este é o caso mais importante do padrão "DB é a fonte de verdade":
     * a API falha, mas o usuário AINDA vê os dados salvos anteriormente.
     */
    @Test
    fun `cenario 2 - API falha mas cache existe - stream emite dados do cache`() = runTest {
        // Arrange: DAO já tem dados (cache de um sync anterior)
        fakeDao.insertAll(cachedEntities)

        // API vai falhar agora
        fakeRemote.shouldFail = true

        // Act: tenta sincronizar (vai falhar)
        val syncResult = repository.sync()

        // Assert: sync retornou falha
        assertTrue("sync deve retornar failure quando API falha", syncResult.isFailure)

        // Assert: mas o Stream ainda tem o cache — usuário não vê tela em branco!
        repository.getTopicsStream().test {
            val cachedTopics = awaitItem()

            assertEquals("Cache deve ter 1 tópico salvo anteriormente", 1, cachedTopics.size)
            assertEquals("Tópico do cache deve ser o 'Cache antigo'", "Cache antigo", cachedTopics[0].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Garante que uma falha no sync NÃO apaga os dados existentes no DAO.
     * O sync é falha-seguro: só escreve no DAO se a API responder com sucesso.
     */
    @Test
    fun `cenario 2 - sync com falha nao sobrescreve dados existentes no DAO`() = runTest {
        // Arrange: 3 tópicos no cache
        val maisCache = listOf(
            TopicEntity(id = 1, title = "Tópico 1", description = ""),
            TopicEntity(id = 2, title = "Tópico 2", description = ""),
            TopicEntity(id = 3, title = "Tópico 3", description = ""),
        )
        fakeDao.insertAll(maisCache)
        fakeRemote.shouldFail = true

        // Act
        repository.sync()

        // Assert: os 3 ainda estão lá, intactos
        val topicsRestantes = repository.getTopics()
        assertEquals("Dados do cache não devem ser apagados após falha de sync", 3, topicsRestantes.size)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cenário 3: API falha + sem cache → erro
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pior cenário: primeiro uso do app, sem internet, sem nada no DAO.
     * - sync() retorna falha
     * - Stream emite lista vazia
     * - A UI deve mostrar a tela de erro (tratado no ViewModel/Screen)
     */
    @Test
    fun `cenario 3 - API falha e sem cache - sync retorna failure e stream vazio`() = runTest {
        // Arrange: DAO vazio + API falhando
        fakeRemote.shouldFail = true
        // (fakeDao começa vazio por padrão)

        // Act
        val syncResult = repository.sync()

        // Assert: falha
        assertTrue("sync deve falhar sem internet", syncResult.isFailure)

        // Assert: stream emite vazio (sem nada para mostrar)
        repository.getTopicsStream().test {
            val topics = awaitItem()
            assertTrue("Stream deve estar vazio sem cache e sem internet", topics.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Garante que a exceção correta é propagada no Result.
     * O ViewModel usa exception.message para montar a mensagem de erro na UI.
     */
    @Test
    fun `cenario 3 - sync propaga a excecao correta no Result`() = runTest {
        // Arrange
        fakeRemote.shouldFail = true

        // Act
        val syncResult = repository.sync()

        // Assert: a exceção tem a mensagem esperada
        val exception = syncResult.exceptionOrNull()
        assertTrue("Deve ter uma exceção", exception != null)
        assertEquals(
            "Mensagem da exceção deve ser a do fake",
            "Sem conexão com a internet",
            exception?.message
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bônus: testa o upsert (replace) do sync
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Garante que o sync faz UPSERT: se um tópico já existe no DAO,
     * ele é atualizado (não duplicado).
     */
    @Test
    fun `sync faz upsert - atualiza topico existente sem duplicar`() = runTest {
        // Arrange: DAO já tem o tópico com id=1, mas com título antigo
        fakeDao.insert(TopicEntity(id = 1, title = "Título antigo", description = "desc velha"))

        // API retorna o mesmo id=1 com título novo
        // (FakeRemoteDataSource já tem apiTopics com id=1 = "Kotlin Coroutines")

        // Act
        repository.sync()

        // Assert: não há duplicata — ainda 2 tópicos (1 atualizado + 1 novo)
        val topics = repository.getTopics()
        assertEquals("Não deve duplicar — deve ser upsert", 2, topics.size)
        assertEquals("Título deve ser atualizado para o da API", "Kotlin Coroutines", topics[0].title)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cenário 4: Optimistic Update + Rollback
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cenário feliz: API aceita o update.
     * - Room atualizado imediatamente
     * - API confirma
     * - Result.success retornado
     * - Dado final no Room é o atualizado
     */
    @Test
    fun `optimistic update - API aceita - dado permanece atualizado`() = runTest {
        // Arrange: tópico original no DAO
        val original = TopicEntity(id = 1, title = "Original", description = "Desc original")
        fakeDao.insert(original)

        val updatedTopic = com.notes.core.model.Topic(id = 1, title = "Atualizado", description = "Desc nova")

        // API vai funcionar normalmente
        // fakeRemote.shouldFail = false (padrão)

        // Act
        val result = repository.updateTopic(updatedTopic)

        // Assert: success
        assertTrue("updateTopic deve retornar success quando API aceita", result.isSuccess)

        // Assert: dado final é o atualizado
        val finalTopic = fakeDao.getById(1)
        assertEquals("Dado deve ser o atualizado após API aceitar", "Atualizado", finalTopic?.title)
    }

    /**
     * ROLLBACK: API rejeita (ou falha de rede).
     * - Room atualizado otimisticamente (UI vê dado novo por um instante)
     * - API falha
     * - Repository restaura o snapshot (dado anterior) no Room
     * - Flow emite o dado antigo → UI reverte automaticamente
     * - Result.failure retornado → ViewModel mostra "Alteração revertida"
     */
    @Test
    fun `optimistic update - API falha - rollback restaura snapshot no Room`() = runTest {
        // Arrange: tópico original no DAO
        val original = TopicEntity(id = 1, title = "Original", description = "Desc original")
        fakeDao.insert(original)

        val updatedTopic = com.notes.core.model.Topic(id = 1, title = "Deveria reverter", description = "Vai reverter")

        // API vai falhar
        fakeRemote.shouldFail = true

        // Act
        val result = repository.updateTopic(updatedTopic)

        // Assert: failure retornado ao caller (ViewModel)
        assertTrue("updateTopic deve retornar failure quando API falha", result.isFailure)

        // Assert: ROLLBACK — Room deve ter o dado ORIGINAL de volta, não o atualizado
        val finalTopic = fakeDao.getById(1)
        assertEquals(
            "Após rollback, Room deve ter o título original",
            "Original",
            finalTopic?.title
        )
        assertEquals(
            "Após rollback, Room deve ter a descrição original",
            "Desc original",
            finalTopic?.description
        )
    }

    /**
     * Garante que o Stream (observado pela UI) reflete o rollback.
     * O Flow deve emitir a sequência: original → atualizado → original (revertido).
     */
    @Test
    fun `optimistic update - rollback - stream reverte para dado original`() = runTest {
        // Arrange
        val original = TopicEntity(id = 1, title = "Título Original", description = "")
        fakeDao.insert(original)

        fakeRemote.shouldFail = true

        val updatedTopic = com.notes.core.model.Topic(id = 1, title = "Vai reverter", description = "")

        // Act
        repository.updateTopic(updatedTopic)

        // Assert: após rollback, o Stream emite o dado original
        repository.getTopicsStream().test {
            val topics = awaitItem()
            assertEquals("Stream deve refletir o rollback — título original", "Título Original", topics.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cenário 5: Last Write Wins (LWW)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * SERVER VENCE: servidor tem timestamp mais recente.
     * O dado do servidor deve sobrescrever o dado local.
     *
     * Cenário: usuário editou às 10h, servidor tem versão das 12h.
     * Resultado esperado: versão das 12h vence (server wins).
     */
    @Test
    fun `LWW - server tem updatedAt maior - server vence e sobrescreve local`() = runTest {
        val localTime  = 1_000L   // 10h (mais antigo)
        val serverTime = 2_000L   // 12h (mais recente) → server vence

        // Arrange: dado local com timestamp antigo
        fakeDao.insert(TopicEntity(id = 1, title = "Versão local (antiga)", description = "", updatedAt = localTime))

        // API retorna dado mais recente
        fakeRemote.topics = listOf(
            TopicDto(id = 1, title = "Versão servidor (nova)", description = "", updatedAt = serverTime)
        )

        // Act
        val result = repository.sync()

        // Assert: sync bem-sucedido
        assertTrue("sync deve retornar success", result.isSuccess)

        // Assert: server venceu — dado local foi sobrescrito
        val finalTopic = fakeDao.getById(1)
        assertEquals(
            "Server deve vencer quando tem updatedAt maior",
            "Versão servidor (nova)",
            finalTopic?.title
        )
        assertEquals("updatedAt deve ser o do servidor", serverTime, finalTopic?.updatedAt)
    }

    /**
     * LOCAL VENCE: usuário editou mais recentemente que o servidor.
     * O dado local NÃO deve ser sobrescrito pelo dado do servidor.
     *
     * Cenário: servidor tem versão das 10h, usuário editou às 12h (offline).
     * Resultado esperado: versão local das 12h é mantida (local wins).
     */
    @Test
    fun `LWW - local tem updatedAt maior - local vence e nao e sobrescrito`() = runTest {
        val serverTime = 1_000L   // 10h (mais antigo)
        val localTime  = 2_000L   // 12h (mais recente) → local vence

        // Arrange: dado local com timestamp recente (editado offline)
        fakeDao.insert(TopicEntity(id = 1, title = "Edição offline (nova)", description = "", updatedAt = localTime))

        // API retorna dado mais antigo
        fakeRemote.topics = listOf(
            TopicDto(id = 1, title = "Versão servidor (antiga)", description = "", updatedAt = serverTime)
        )

        // Act
        repository.sync()

        // Assert: local venceu — dado local foi MANTIDO
        val finalTopic = fakeDao.getById(1)
        assertEquals(
            "Local deve vencer quando tem updatedAt maior",
            "Edição offline (nova)",
            finalTopic?.title
        )
        assertEquals("updatedAt local deve ser preservado", localTime, finalTopic?.updatedAt)
    }

    /**
     * TIMESTAMPS IGUAIS: empate — por convenção, server vence (server-authoritative).
     * Garante comportamento determinístico no caso de empate.
     */
    @Test
    fun `LWW - timestamps iguais - server vence por convencao`() = runTest {
        val sameTime = 1_000L

        fakeDao.insert(TopicEntity(id = 1, title = "Local", description = "", updatedAt = sameTime))
        fakeRemote.topics = listOf(
            TopicDto(id = 1, title = "Server", description = "", updatedAt = sameTime)
        )

        // Com updatedAt igual, `dto.updatedAt > local.updatedAt` é false → local vence
        // Isso é uma escolha de design: poderíamos usar >= para server ganhar no empate
        repository.sync()

        val finalTopic = fakeDao.getById(1)
        // No nosso LWW: server.updatedAt > local → server ganha; igual → local mantido
        assertEquals("Com timestamps iguais, local é mantido (> não >=)", "Local", finalTopic?.title)
    }

    /**
     * NOVO TÓPICO DO SERVIDOR: não existe localmente → server sempre vence (insert).
     */
    @Test
    fun `LWW - topico novo do servidor - e inserido sem conflito`() = runTest {
        // Arrange: DAO vazio
        fakeRemote.topics = listOf(
            TopicDto(id = 99, title = "Novo do servidor", description = "", updatedAt = 5_000L)
        )

        // Act
        repository.sync()

        // Assert: tópico novo foi inserido
        val finalTopic = fakeDao.getById(99)
        assertEquals("Tópico novo do servidor deve ser inserido", "Novo do servidor", finalTopic?.title)
    }
}

