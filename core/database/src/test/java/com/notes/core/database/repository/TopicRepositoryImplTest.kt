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
    private val apiTopics = listOf(
        TopicDto(id = 1, title = "Kotlin Coroutines", description = "Async sem callbacks"),
        TopicDto(id = 2, title = "Clean Architecture", description = "Separação de camadas"),
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
}

