# LEARNING LOG — StaffNotes

---

## Mês 1 | Semana 1 | Segunda-feira

- UI Layer = ViewModel + UI State + UI Elements
  - ViewModel é responsável por fornecer os dados para UI(Screen), 
  - sendo um stateHolder e expondo um stateFlow/LiveData com um Ui State
- ViewModel expõe StateFlow, UI coleta com collectAsStateWithLifecycle
- UI State deve ser imutável (data class)
- UI State deve conter apenas os dados necessários para a UI, 
  - não deve conter lógica de negócios ou dados desnecessários

## Mês 1 | Semana 1 | Terça-feira

- Domain Layer = Use-Cases + Entity + Repository Interface
    - Use-Cases são responsáveis por executar a lógica de negócios, 
    - Entity é a representação dos dados do domínio, 
    - Repository Interface é a abstração para acessar os dados
    - ViewModel(Ui-Layer) -> Use-Cases(Domain-Layer) -> Repository Interface(Domain-Layer)
        - Repository Interface é implementada na Data Layer

## Mês 1 | Semana 1 | Quarta-feira

- Data Layer = Repository Implementation + Data SOurce
  - Repository implementation é responsável por implementar a lógica de acesso aos dados e decidir a fonte deles
  - Data Source é responsável por acessar os dados, seja de uma API, banco de dados local, etc

## Mês 1 | Semana 1 | Quinta-feira

- A UI deve ser completamente desacoplada das regras e mudanças, delegando para a viewModel
  - UDF (Unidirectional Data Flow) é um padrão de arquitetura onde os dados fluem em uma única direção, 
    - UI -> ViewModel -> Ui State -> UI
  - Isso garante que a UI seja reativa e responda às mudanças de estado

## Mês 1 | Semana 1 | Sexta-feira

- Clean Architecture é uma abordagem de design de software que promove a separação de responsabilidades e a independência entre as camadas do aplicativo
  - As camadas são: UI Layer, Domain Layer e Data Layer
  - Cada camada tem suas responsabilidades e dependências bem definidas, 
    - UI Layer depende do Domain Layer, 
    - Domain Layer depende do Data Layer, lembrando que este, não conhece o data layer, ele olha apenas o contrato
    - Data Layer é independente das outras camadas
    - Segue o UDF (Unidirectional Data Flow) para garantir que os dados fluam em uma única direção, 
      - UI -> ViewModel -> Ui State -> UI

## Mês 1 | Semana 2 | Segunda-feira

- MVI Archutecture (Model-View-Intent) é um padrão de arquitetura que promove a separação de responsabilidades e a reatividade da UI
  - Model representa o estado da UI, 
  - View é responsável por exibir os dados e interagir com o usuário, 
  - Intent representa as ações do usuário e as intenções de mudança de estado
  - O fluxo de dados é unidirecional: 
    - View -> Intent -> Model -> View
  - Isso garante que a UI seja reativa e responda às mudanças de estado, 
    - além de facilitar a manutenção e a testabilidade do código
    - a grande diferença entre MVI e MVVM é que no MVI, a UI é completamente reativa e depende do estado, mandando intenções para a viewModel,
    - enquanto no MVVM, a UI pode ter lógica de negócios e não depende completamente do estado, podendo chamar métodos da viewModel diretamente

## Mês 1 | Semana 2 | Terça-feira

- Side-Effect é uma ação que ocorre como resultado de uma mudança de estado, 
  - pode ser uma navegação, exibição de um toast, etc
  - deve ser tratado de forma separada do estado da UI, 
    - para evitar que a UI fique acoplada a lógica de negócios e para garantir que a UI seja reativa e responda às mudanças de estado
  - pode ser tratado com um canal ou um flow separado do stateFlow do Ui State, 
    - para garantir que a UI possa coletar os side-effects de forma reativa e independente do estado da UI
  - Fluxo correto MVI + Side-Effects:
    - Intent → ViewModel → (State + Side-Effect)
    - State (Flow): dados persistentes que a UI observa sempre
    - Side-Effect (Channel): ações únicas que a UI consome e descarta

## Mês 1 | Semana 2 | Quarta-feira

- A viewModel vive no ViewModelStore, que é retido pelo sistema durante config changes (rotação, mudança de tema, etc)
  - ela **não morre com a Activity** em config changes — é exatamente para isso que ela existe
  - ela morre quando a Activity **realmente termina** (usuário pressiona back, `finish()` é chamado) ou com process death
- Process death ocorre quando o sistema mata o processo do app para liberar recursos (Low Memory Kill)
  - nesse caso a viewModel é destruída e perde todo o seu estado em memória
- Para sobreviver ao process death, usamos o SavedStateHandle
  - ele é um mapa chave-valor que serializa os dados num **Bundle**
  - esse Bundle é mantido pelo **processo do sistema** (ActivityManagerService) na RAM do sistema, **não em disco**
  - por isso tem limitações: aceita apenas tipos primitivos, Strings e Parcelables, com limite de ~500KB
  - quando o usuário retorna ao app após um process death, o sistema restaura o Bundle e o SavedStateHandle é recriado com os dados anteriores
  - diferença importante: SavedStateHandle ≠ persistência em disco (Room, DataStore) — se o dispositivo reiniciar sem o app ter sido backgrounded antes, os dados são perdidos

## Mês 1 | Semana 2 | Quarta-feira/Sexta-feira

- Usamos o MVI com StateFlow para o estado da UI e Channel para os side-effects, garantindo um fluxo unidirecional de dados e uma UI reativa
- A viewModel é responsável por gerenciar o estado da UI e os side-effects, garantindo que a UI seja desacoplada da lógica de negócios e responda às mudanças de estado de forma
- Os side-effects são tratados de forma separada do estado da UI, garantindo que a UI seja reativa e responda às mudanças de estado, além de facilitar a manutenção e a testabilidade do código
- O SavedStateHandle é usado para salvar o estado da viewModel em caso de morte do processo, garantindo que os dados sejam restaurados corretamente quando o aplicativo for reaberto
- Os dados do savedStateHandle são restaurados automaticamente quando a viewModel é recriada após a morte do processo, garantindo que a UI seja restaurada ao estado anterior sem necessidade de lógica adicional para lidar com isso

## Mês 1 | Semana 3 | Seg, Ter, Qua-feiras

- A modularização é importante para a escalabilidade e manutenção do código, permitindo que diferentes partes do aplicativo sejam desenvolvidas e testadas de forma independente
- A modularização pode ser feita por feature, camada ou responsabilidade, dependendo das necessidades do projeto
- Estrutura adotada no projeto: by feature + by layer nos módulos core
  - `:core:model` — entidade pura (Topic), zero dependências, puro Kotlin
  - `:core:data` — contrato (TopicRepository interface) + UseCases — só conhece o modelo
  - `:core:database` — implementação Room (Entity, DAO, Database, Mapper, RepositoryImpl) — conhece model + data
  - `:home` — feature: lista de tópicos (MVI completo) — só conhece core:model + core:data
  - `:detail` — feature: detalhe do tópico (MVI completo) — só conhece core:model + core:data
  - `:app` — orquestrador: monta a navegação e inicializa o DI

- Regra fundamental: **feature nunca depende de outra feature** — quem orquestra é o `:app`

- Cada feature expõe um `navGraph` extension function para o `NavGraphBuilder`
  - o `:app` monta o NavHost chamando os navGraphs de cada feature
  - isso garante que cada feature seja completamente independente e não conheça as rotas das outras
  - as rotas e constantes de argumentos ficam dentro do próprio módulo feature

- O `:app` conhece `:core:database` para dar vida às instâncias no DI (Koin startKoin)
  - `databaseModule` → instancia o Room e registra o `TopicRepositoryImpl` como `TopicRepository`
  - `dataModule` → registra os UseCases que dependem do contrato `TopicRepository`
  - as features nunca veem o Room — elas pedem `TopicRepository` ao Koin e o `:app` garante que a implementação está registrada
  - isso é a inversão de dependência (SOLID — DIP) aplicada no Gradle

- Diferença entre `implementation` e `api` no Gradle:
  - `implementation` → dependência interna, não vaza para quem depende do módulo (padrão)
  - `api` → dependência pública, quem depende do módulo também a recebe transitivamente

## Mês 1 | Semana 3 | Quinta-feira

- O hilt é um framework de injeção de dependências para Android que facilita a gestão de dependências e a construção de objetos complexos, promovendo a modularização e a testabilidade do código
- Ele é baseado no Dagger, mas com uma configuração mais simples e integrada ao ciclo de vida do Android, permitindo a injeção automática de dependências em Activities, Fragments, ViewModels, etc
- O hilt usa anotações para definir os componentes e as dependências, como `@HiltAndroidApp`, `@AndroidEntryPoint`, `@Inject`, `@Module`, `@Provides`, etc
- Ele também suporta escopos para controlar o ciclo de vida das dependências, como `@Singleton`, `@ActivityScoped`, `@FragmentScoped`, etc
- O hilt facilita a modularização do código, permitindo que as dependências sejam definidas em módulos separados e injetadas onde necessário, promovendo a separação de responsabilidades e a testabilidade do código
- Em comparação ao Koin, o hilt é mais robusto e escalável para projetos maiores, mas tem uma curva de aprendizado mais acentuada devido à sua complexidade e ao uso de anotações, enquanto o Koin é mais simples e fácil de configurar, mas pode não ser tão eficiente em projetos grandes com muitas dependências.

## Mês 1 | Semana 3 | Sexta-feira

### modules-graph-assert

- Plugin Gradle que valida regras arquiteturais do grafo de módulos em tempo de build
- Permite definir regras como "o módulo X não pode depender do módulo Y" ou "o módulo Z só pode ser dependido por módulos A e B"
- Ajuda a manter a modularização e a independência entre os módulos, evitando acoplamento indesejado e garantindo que as dependências sejam controladas e explícitas
- Pode ser configurado para falhar o build se as regras forem violadas, garantindo que a arquitetura seja mantida ao longo do desenvolvimento e evitando que dependências indesejadas sejam introduzidas acidental
- Exemplo de configuração:

moduleGraphAssert {
    // restricted: pares de módulos que NÃO podem ter dependência entre si.
    // Formato 2.9.0: ":from -X> :to" (o -X> indica dependência PROIBIDA)
    restricted = arrayOf(
        ":home -X> :detail",
        ":detail -X> :home"
    )
}

## Mês 1 | Semana 4 | Segunda-feira

- Convention plugins são usados para centralizar e padronizar a configuração do Gradle em um projeto multi-módulo, evitando repetição de código e garantindo consistência entre os módulos
- Eles são criados como módulos Gradle separados (geralmente em `buildSrc`) e aplicados nos módulos do projeto para compartilhar configurações comuns, como dependências, plugins, versões, etc
- Exemplo de uso: criar um convention plugin para configurar o Koin em todos os módulos que precisam de injeção de dependências, evitando a repetição da configuração do Koin em cada módulo e garantindo que todos usem a mesma versão e configuração
- Isso promove a modularização e a manutenção do código, permitindo que as mudanças na configuração sejam feitas em um único lugar e propagadas para todos os módulos que usam o convention plugin, além de facilitar a adição de novas dependências ou plugins no futuro, mantendo a consistência e a organização do projeto.

## Mês 1 | Semana 4 | Terça-feira

### Version Catalog (libs.versions.toml)

- Version Catalog é a fonte única de verdade para versões e dependências em projetos Gradle multi-módulo
- Definido em `gradle/libs.versions.toml`, organizado em três seções: `[versions]`, `[libraries]`, `[plugins]`
- Gera accessors type-safe: `libs.compose.bom`, `libs.room.runtime`, `libs.plugins.hilt.android`, etc.
- Vantagens: autocomplete na IDE, renaming centralizado, sem inconsistência de versões entre módulos
- Convention plugins do included build (`build-logic`) são registrados **sem `version`** no catálogo — o Gradle os resolve pelo classpath do included build, não de um repositório remoto
  - Exemplo: `notes-android-library = { id = "notes.android.library" }` → accessor `libs.plugins.notes.android.library`
  - O alias com hífens (`notes-android-library`) gera o accessor com pontos (`notes.android.library`)

## Mês 1 | Semana 4 | Quarta-feira

Otimizações ativadas em `gradle.properties`:
- O 1º build com `configuration-cache` é **mais lento** — ele precisa serializar o task graph para disco
- O **ganho real** aparece a partir do 2º build: `Reusing configuration cache.` → fase de configuration é ignorada
- `parallel=true` escala com o número de módulos: quanto mais módulos desacoplados, maior o ganho
- `build-cache` é poderoso em CI: módulos não alterados são restaurados do cache sem recompilar
- Aumentar `Xmx` (2048m → 4096m) reduz GC pressure em projetos maiores, mas não ajuda no 1º build frio

**Observação importante — quando cada otimização ajuda:**
- `--no-configuration-cache` + código inalterado → **parallel** ajuda mais
- Mudança em 1 arquivo de 1 módulo → **build-cache** faz os outros módulos serem `FROM-CACHE`
- Build repetido sem mudanças → **configuration-cache** faz o build terminar em ~5sco

## Mês 1 | Semana 4 | Quinta-feira

**Por que essas tasks são lentas?**
- `mergeDebugResources` — mescla todos os recursos XML/drawables de todos os módulos num único diretório. Escala com o número de módulos e o volume de recursos.
- `mergeDebugJavaResource` — consolida todos os `.jar` de recursos Java (META-INF, etc.) de todas as dependências transitivas. Quanto mais libs externas, mais lento.
- `mergeExtDexDebug` — converte as classes das dependências externas (Compose, Hilt, Room…) para DEX. É o passo mais pesado num build frio — por isso o build-cache é tão valioso aqui.

- KAPT rodaria um compilador extra antes de processar as anotações, enquanto KSP lê o código diretamente.
- KSP é mais rápido porque é otimizado para o processamento de anotações em Kotlin, enquanto o KAPT é um wrapper que usa o compilador Java, introduzindo overhead adicional.

## Mês 1 | Semana 4 | Sexta-feira

### Architecture Learning Journey — Now in Android

O documento do NiA descreve como o app oficial do Google foi arquitetado e por quê. Os pontos centrais:

**1. Camadas bem definidas com dependências unidirecionais**
- UI Layer → Domain Layer → Data Layer
- Nunca na direção contrária — Data Layer não conhece a UI
- Domain Layer é **opcional** — só existe quando há lógica que múltiplas features precisam compartilhar

**2. Representação do estado da UI**
- `UiState` é uma `data class` imutável — snapshot da UI em um dado instante
- Exposta via `StateFlow` do ViewModel — reativa, lifecycle-aware via `collectAsStateWithLifecycle`
- Nunca expor `MutableStateFlow` publicamente — a UI nunca escreve no estado diretamente

**3. Unidirectional Data Flow (UDF)**
- Eventos sobem: `UI → Intent/Event → ViewModel`
- Estado desce: `ViewModel → StateFlow → UI`
- Side-effects são tratados separadamente via `Channel` (exactly-once)

**4. Multi-module por feature**
- Cada feature é um módulo independente: compila isolado, testa isolado, escala isolado
- `:app` é o único que conhece todas as features (para montar o NavHost e o DI)
- `:core:*` são os módulos de infraestrutura compartilhada, nunca conhecem as features

**5. Convention Plugins**
- Cada módulo novo = 3 linhas no `build.gradle.kts` graças aos convention plugins
- Elimina a "config tax": `compileSdk`, `minSdk`, `jvmTarget`, deps do Hilt — tudo centralizado

**6. Build como parte da arquitetura**
- Configuration cache + build cache + parallelism não são "otimizações extras" — são requisitos da arquitetura multi-module
- O grafo de módulos desacoplado é o que torna o parallel build eficiente

---

### Revisão do StaffNotes — Estado atual

Estrutura de módulos:
```
:app                ← @HiltAndroidApp + NavHost + orquestrador
:home               ← feature lista (MVI: Intent, State, SideEffect, ViewModel, Screen)
:detail             ← feature detalhe (MVI completo)
:design-system      ← componentes compartilhados
:core:model         ← Topic (data class pura, zero deps)
:core:data          ← TopicRepository (interface) + 4 UseCases
:core:database      ← Room (Entity, DAO, Database, Mapper, RepositoryImpl)
build-logic/        ← 5 Convention Plugins
gradle/libs.versions.toml ← Version Catalog (versão única source of truth)
```

O que funciona end-to-end:
- UI (Compose) → Intent → ViewModel → UseCase → Repository → Room → Flow → UI
- Navegação: Home → Detail → Back (multi-module, sem acoplamento entre features)
- DI: Hilt instalado em todos os módulos, cada um com seu @Module
- SavedStateHandle: searchQuery sobrevive ao process death
- Side-effects via Channel: NavigateToDetail, ShowSnackbar
- Módulo grafo validado: `:home -X> :detail` e `:detail -X> :home` proibidos em build time

Decisões que funcionaram bem:
- `Channel` para side-effects → zero navegação duplicada
- `reduce { copy(...) }` → mudanças de estado são funções puras fáceis de debugar
- Convention Plugins → adicionar novo módulo leva < 5 minutos de config
- `modules-graph-assert` → arquitetura enforçada pelo próprio build, não pela boa vontade

Dívidas técnicas identificadas para o Mês 2:
- `getTopics()` é `suspend fun` que retorna `List` — deveria ser `Flow<List<Topic>>` para reatividade real
- Sem tratamento de erro tipado ainda (plain `Exception`) — Mês 2 introduzirá `Result<T>`
- Sem testes unitários nos UseCases e ViewModel — Mês 2 começa a cobrir isso

ADR escrita em: `docs/adr/001-arquitetura-staffnotes.md`

---

### RESUMO MÊS 1

**O que foi construído:**
- App Android moderno do zero com arquitetura multi-camada (UI → Domain → Data)
- 2 features completas com MVI: Home (lista) e Detail
- Room como source of truth com Entity, DAO, Database e Mapper
- Hilt multi-module com Convention Plugins para DI sem cerimônia
- Navegação multi-module: cada feature expõe seu navGraph, `:app` orquestra
- SavedStateHandle para sobreviver ao process death
- Side-effects via Channel (exactly-once semantics)
- build-logic com 5 Convention Plugins eliminando duplicação Gradle
- Version Catalog (libs.versions.toml) como fonte única de versões
- Build otimizado: configuration-cache + parallel + build-cache (38s → 5s)
- modules-graph-assert: grafo de módulos validado em build time
- 1 ADR completa documentando todas as decisões arquiteturais

**Conceitos dominados:**
- UDF: eventos sobem, estado desce
- MVI vs MVVM: a diferença real é o contrato tipado de Intents
- StateFlow vs Channel: estado persistente vs evento único
- Repository pattern com inversão de dependência no Gradle
- Process death vs config change: quando cada um ocorre e como tratar
- Multi-module: vertical slicing (por feature) > horizontal slicing (por layer)
- Convention Plugins: como e por que criar, onde o included build se encaixa
- Build optimizations: qual otimização ajuda em qual cenário
- KSP vs KAPT: KSP lê AST diretamente (2x mais rápido), KAPT é wrapper Java

---

## Mês 2 | Semana 1 | Segunda-feira

- Padrão **offline-first**: o banco de dados local (Room) é a única fonte de verdade — a UI nunca lê diretamente da API
  - Fluxo: `Network → DB → UI`
  - A API é usada apenas para **sincronizar** o banco, nunca para alimentar a UI diretamente
  - Se a API falhar, o usuário ainda vê os dados do cache — nunca uma tela em branco se há cache

- Criado módulo `:core:network` com:
  - `TopicRemoteDataSource` (interface) — o Repository conhece apenas o contrato
  - `TopicRemoteDataSourceImpl` (Retrofit) — implementação real
  - `FakeTopicRemoteDataSource` — implementação fake para testes e desenvolvimento offline
  - `TopicDto` / `PostDto` — objetos de transferência, separados da Entity do Room

## Mês 2 | Semana 1 | Terça-feira

- **Repository como orquestrador** entre local e remoto:
  - `getTopicsStream()` → sempre lê do Room (Flow reativo)
  - `sync()` → chama API, salva no Room com `insertAll` (upsert)
  - A UI **nunca chama sync diretamente** — ela só coleta o Flow

- Por que separar `getTopicsStream()` de `sync()`?
  - São responsabilidades diferentes: **ler** vs **sincronizar**
  - O WorkManager pode chamar `sync()` sem a UI estar aberta
  - A UI pode estar aberta sem precisar disparar um sync (ex: dados frescos)

- Refatorado `TopicRepositoryImpl` para o padrão SSOT (Single Source of Truth):
  ```kotlin
  override fun getTopicsStream(): Flow<List<Topic>> =
      topicDao.getAllStream().map { entities -> entities.map { it.toDomain() } }

  override suspend fun sync(): Result<Unit> = runCatching {
      val remoteDtos = remoteDataSource.fetchTopics()
      topicDao.insertAll(remoteDtos.map { it.toDomain().toEntity() })
  }
  ```

## Mês 2 | Semana 1 | Quarta-feira (WorkManager)

- **WorkManager** para sync periódico em background:
  - `PeriodicWorkRequest` de 1 hora com `NetworkType.CONNECTED` como constraint
  - Sobrevive a: app fechado, reboot do dispositivo, process death
  - `PeriodicWork` **não acumula** execuções perdidas (Doze Mode): ao voltar, executa 1 vez e reagenda
  - HiltWorker (`@HiltWorker` + `@AssistedInject`) para injeção de dependências no Worker

- Integração com Hilt: `HiltWorkerFactory` configurado no `NotesApplication`
  - `WorkerFactory` padrão é substituído pelo do Hilt via `Configuration.Provider`

- API conectada: `https://jsonplaceholder.typicode.com/posts`
  - DTOs mapeados para o modelo de `Topic` via `TopicDtoMapper`
  - Campos mapeados: `id → id`, `title → title`, `body → description`

## Mês 2 | Semana 1 | Quinta/Sexta-feira

### Como o Flow emite automaticamente quando há dados novos no Room

- O Room usa um **InvalidationTracker** interno — quando há INSERT/UPDATE/DELETE em uma tabela, todos os Flows que observam aquela tabela são notificados e re-emitem automaticamente
- O `sync()` é apenas o **gatilho** que escreve no Room — o Flow reage sozinho
- Fluxo completo:
  ```
  sync() → dao.insertAll() → Room detecta mudança → Flow emite → ViewModel → UI atualiza
  ```
- A UI nunca "puxa" dados — ela **reage** ao que o Room notifica (padrão push, não pull)

---

## Mês 2 | Semana 2 | Segunda e Terça-feira

- **`sealed interface AppResult<T>`** criado em `:core:model`:
  - `Loading` — operação em andamento
  - `Success<T>(data: T)` — dados disponíveis
  - `Error<T>(exception, cachedData?)` — falha, mas com possibilidade de carregar cache

- Por que não usar o `Result<T>` do Kotlin stdlib?
  - O `Result<T>` nativo não suporta o estado `Loading` nem `cachedData`
  - Para UI, precisamos modelar os 3 estados — o `AppResult` é mais expressivo

- **`GetTopicsStreamUseCase`** atualizado para retornar `Flow<AppResult<List<Topic>>>`:
  - `.onStart { emit(AppResult.Loading) }` — emite Loading antes do primeiro dado
  - `.map { AppResult.Success(it) }` — envolve os dados do Room em Success
  - `.catch { emit(AppResult.Error(it)) }` — captura erros do Room (raro)

## Mês 2 | Semana 2 | Quarta e Quinta-feira

### Error Handling com 3 cenários (offline-first na prática)

- **Cenário 1 — Online + cache:**
  - `sync()` sucede → Room atualiza → Flow emite → UI exibe `LinearProgressIndicator` durante sync
  - Após o sync, `isRefreshing = false` e a lista atualiza automaticamente

- **Cenário 2 — Offline + cache:**
  - `sync()` falha + `topics.isNotEmpty()` → `isOffline = true`
  - UI exibe banner de aviso: "Você está offline — dados podem estar desatualizados"
  - Usuário ainda vê os dados — **nunca tela em branco se há cache**

- **Cenário 3 — Offline + sem cache:**
  - `sync()` falha + `topics.isEmpty()` → `syncFailed = true`
  - UI exibe tela de erro completa com ícone + mensagem + botão "Tentar novamente"
  - Botão dispara `HomeIntent.Refresh` → tenta sync novamente

- Lógica no ViewModel que decide o cenário:
  ```kotlin
  syncResult.fold(
      onSuccess = { reduce { copy(isRefreshing = false) } },
      onFailure = { exception ->
          val hasCachedData = _uiState.value.topics.isNotEmpty()
          reduce {
              copy(
                  isOffline = true,
                  syncFailed = !hasCachedData,
                  errorMessage = if (hasCachedData) null else exception.message
              )
          }
      }
  )
  ```

## Mês 2 | Semana 2 | Sexta-feira (Testes)

### Testes unitários do TopicRepositoryImpl

- **Estratégia: Fakes manuais** (sem Mockk/Mockito):
  - `FakeTopicDao` — implementa `TopicDao` com `MutableStateFlow` em memória (simula o Room reativo)
  - `FakeTopicRemoteDataSource` — implementa `TopicRemoteDataSource` com flag `shouldFail` controlável

- Por que Fakes em vez de Mocks?
  - Fakes são implementações reais simples — o comportamento fica explícito e legível
  - Mocks (`every { ... } returns ...`) podem passar em testes errados por má configuração
  - O `FakeTopicDao` com `MutableStateFlow` **realmente emite** quando dados mudam, testando o Flow real

- **Turbine** para testar Flows:
  - `.test { awaitItem() }` — aguarda a próxima emissão e cancela o Flow automaticamente
  - Evita race conditions e código verboso do `collect {}`

- Testes criados (6 cenários):
  1. Cenário 1: API funciona → sync sucede → stream emite dados novos
  2. Cenário 1: stream vazio antes do sync → preenchido após sync
  3. Cenário 2: API falha + cache → sync falha mas stream emite cache
  4. Cenário 2: falha no sync não apaga dados existentes no DAO
  5. Cenário 3: API falha + DAO vazio → sync falha e stream vazio
  6. Cenário 3: exceção correta é propagada no `Result.failure`
  7. Bônus: sync faz upsert (não duplica tópicos)

- `runTest` do `kotlinx-coroutines-test`:
  - Coroutine scope especial para testes com **tempo virtual**
  - Substitui `runBlocking` — mais rápido, controla delays sem esperar tempo real

### RESUMO DA SEMANA 2 — Mês 2

- ✅ Network → DB → UI implementado (padrão offline-first SSOT)
- ✅ `AppResult<T>` modelando os 3 estados: Loading, Success, Error com cachedData
- ✅ Error handling com 3 cenários distintos na UI (online, offline+cache, offline+vazio)
- ✅ Testes unitários do Repository com Fakes manuais + Turbine
- ✅ Princípio central consolidado: **nunca mostrar tela em branco se há cache**

---

## Mês 2 | Semana 3 | Segunda-feira

### Optimistic Updates

- **O que é?** Atualizar a UI *antes* de confirmar com o servidor — assumimos que vai funcionar.
  - O nome vem de "otimismo": agimos como se a operação fosse ter sucesso.
  - Apps como Gmail, Google Docs, Notion, Twitter (curtir/descurtir) todos fazem isso.

- **Por que fazer?**
  - Latência de rede: 200ms–2s de espera antes de mostrar a mudança é UX ruim.
  - Com optimistic update, a resposta parece **instantânea** — sem spinner bloqueante.

- **O risco:** a API pode rejeitar a operação (ex: conflito, sem permissão, dado inválido).
  - Solução: **rollback** — restaurar o estado anterior se a API falhar (Terça-feira).

### Implementação no projeto

- **`TopicRepository`** — adicionado `updateTopic(topic: Topic): Result<Unit>`
- **`TopicRepositoryImpl`** — implementa o padrão em 2 passos:
  ```kotlin
  override suspend fun updateTopic(topic: Topic): Result<Unit> = runCatching {
      // Passo 1: Room primeiro (persiste localmente, Flow emite → UI atualiza)
      topicDao.insert(topic.toEntity())
      // Passo 2: API depois (pode falhar — rollback amanhã)
      remoteDataSource.updateTopic(topic.id, topic.toDto())
  }
  ```
- **`UpdateTopicUseCase`** — novo use case, delega ao repository no `Dispatchers.IO`
- **`DetailViewModel`** — o coração do optimistic update:
  ```kotlin
  // Passo 1: UI atualiza AGORA (antes de Room ou API)
  reduce {
      copy(isEditing = false, isSaving = true, topic = updatedTopic)
  }
  // Passo 2: I/O em background — UI já está atualizada
  viewModelScope.launch {
      updateTopicUseCase(updatedTopic).fold(
          onSuccess = { reduce { copy(isSaving = false) } },
          onFailure = { reduce { copy(isSaving = false) }
              emitSideEffect(ShowSnackbar("Salvo localmente. Sincronização pendente."))
          }
      )
  }
  ```
- **`DetailScreen`** — 2 modos:
  - **Visualização**: `Text` com título + descrição + botão Edit (✏️) na TopAppBar
  - **Edição**: `OutlinedTextField` + botões Cancelar (✕) e Salvar (✓) na TopAppBar
  - `LinearProgressIndicator` sutil enquanto `isSaving = true` (não bloqueia)

### Diferença entre Pessimistic vs Optimistic

| | Pessimistic | Optimistic |
|---|---|---|
| Quando UI atualiza | Após API confirmar | Imediatamente |
| UX durante request | Spinner bloqueante | Nada (imperceptível) |
| Se API falhar | Nada mudou | Precisa de rollback |
| Usado quando | Operações críticas (pagamento) | CRUD comum |

---

## Mês 3 | Semana 2 | Terça-feira

### Conflict Resolution + Rollback (Optimistic Update)

**Referência:** https://developer.android.com/topic/architecture/data-layer/offline-first#conflict-resolution

- **Conflito de dados** ocorre quando o dado local e o remoto divergem.
  - Ex: usu��rio edita offline → API retorna um valor diferente → quem ganha?
  - Estratégias comuns: **Last Write Wins**, **Server Wins**, **Client Wins**, **Merge**.

- **No nosso app:** adotamos **Server Wins** — se a API rejeitar, fazemos rollback local.
  - Garante consistência: o servidor é a fonte de verdade final.

- **O que é Rollback?**
  - Antes de persistir o dado novo, guardamos um **snapshot** do estado anterior.
  - Se a API falhar, restauramos o snapshot no Room.
  - O Flow do DAO emite automaticamente → UI reverte sem código adicional na UI.

---

## Mês 3 | Semana 2 | Quarta-feira

### Last Write Wins (LWW) — Estratégia de Conflict Resolution

**Referência:** "last write wins vs merge conflict resolution"

#### O que é conflito de dados?
Ocorre quando local e servidor têm versões diferentes do mesmo dado — quem vence?

Ex: usuário edita offline às 10h → servidor foi atualizado às 12h → qual versão prevalece?

#### Estratégias de resolução

| Estratégia | Como funciona | Risco |
|---|---|---|
| **Last Write Wins (LWW)** | Quem tem timestamp mais recente vence | Pode descartar edição mais antiga |
| **Server Wins** | Servidor sempre vence, ignora local | Perde edições offline |
| **Client Wins** | Local sempre vence | Perde atualizações do servidor |
| **Merge** | Tenta fundir as duas versões | Complexo, pode gerar conflitos de merge |
| **CRDTs** | Estruturas de dados que se mergeiam sem conflito | Muito complexo, para apps colaborativos |

- Para um app de notas pessoal: LWW é o sweet spot entre simplicidade e correção

---

## Mês 3 | Semana 2 | Quinta-feira

### SyncStatus — Visual Feedback de Sincronização

**Referência:** "Google Keep architecture sync"

#### Por que visual feedback de sync é crucial?
- O usuário precisa saber se o dado está **seguro na nuvem** ou só existe localmente.
- Sem feedback, um PENDING parece igual a um SYNCED → falsa sensação de segurança.
- Apps como Google Keep, Notion, Obsidian mostram indicadores de sync explicitamente.

#### Reflexão
- Visual feedback de sync não é cosmético — é parte da **data layer contract com o usuário**.
- O usuário toma decisões com base no que vê: "este dado está seguro?" → SYNCED = sim.
- Implementar cedo (antes de ter usuários) é infinitamente mais fácil do que retrofitar depois.

---

## Mês 3 | Semana 2 | Sexta-feira — RESUMO DA SEMANA

### Reflexão geral

A semana 3 foi sobre **o que acontece quando a realidade bate de frente com o otimismo**:
- Segunda: agimos como se fosse funcionar (optimistic)
- Terça: planejamos o que fazer quando falha (rollback)
- Quarta: decidimos quem tem razão quando há discordância (LWW)
- Quinta: mostramos isso tudo visualmente ao usuário (SyncStatus)
- Sexta: provamos que funciona (testes)

### Os 4 conceitos da semana

**1. Optimistic Update**
- UI atualiza *antes* de confirmar com o servidor
- Fluxo: UI atualiza → Room salva (PENDING) → API em background
- Risco: API pode rejeitar → precisa de rollback

**2. Rollback**
- Guardamos snapshot *antes* de alterar
- Se API falha: `topicDao.insert(snapshot)` restaura o estado anterior
- Por que `insert` e não `delete`? O dado existia antes — delete apagaria tudo
- O Flow do Room reverte a UI automaticamente (sem código extra na UI)

**3. Last Write Wins (LWW)**
- `server.updatedAt > local.updatedAt` → server vence → SYNCED
- `server.updatedAt < local.updatedAt` → local vence → PENDING (edição offline protegida)
- `server.updatedAt == local.updatedAt` → empate → local mantido → CONFLICT
- `local == null` → tópico novo do servidor → SYNCED
- Alternativas mais complexas: OT (Google Docs), CRDTs (Notion) — overkill para notas pessoais

**4. SyncStatus**
- `SYNCED` ✓ → confirmado pelo servidor
- `PENDING` ⏳ → criado/editado localmente, aguardando envio
- `CONFLICT` ⚡ → empate de timestamp detectado
- `ERROR` ✗ → reservado para falhas persistentes
- Armazenado como `String` no Room (enums não são suportados nativamente)
- Atualiza na UI via Flow — ícone muda sozinho quando o status muda no banco

### Testes escritos — 7 cenários

| # | Cenário | O que garante |
|---|---|---|
| 1 | Optimistic update — API aceita | Dado permanece atualizado no Room |
| 2 | **Optimistic update — API falha** | **Snapshot restaurado — dado original volta** |
| 3 | Rollback — stream reverte | Flow emite dado antigo automaticamente |
| 4 | LWW — server mais recente | Server sobrescreve local |
| 5 | **LWW — local mais recente** | **Edição offline protegida** |
| 6 | LWW — timestamps iguais | Local mantido (operador `>`, não `>=`) |
| 7 | LWW — tópico novo do servidor | Inserido sem conflito |

### ✅ Checklist da Semana 3
- ✅ Optimistic updates + rollback implementados e testados
- ✅ Last Write Wins com `updatedAt` + 4 cenários de teste
- ✅ `SyncStatus` enum com visual feedback na lista
- ✅ Migrations 1→2 (`updatedAt`) e 2→3 (`syncStatus`)
- ✅ 7 testes de repository cobrindo todos os cenários de sync


---

## Mês 3 | Semana 4 | Segunda-feira

### WorkManager — Teoria

**Quando usar WorkManager?**
- Tarefas que precisam ser **garantidas**, mesmo se o app fechar ou o dispositivo reiniciar
- Não usar para tarefas imediatas ou que o usuário espera ver instantaneamente (use coroutines ou Services)
- Exemplos: sync periódico, upload de foto, backup, limpeza de cache

**Constraints — pré-condições para executar:**
- `setRequiredNetworkType(CONNECTED)` → só roda com internet
- `setRequiresBatteryNotLow(true)` → não roda se bateria está fraca
- `setRequiresCharging(true)` → só roda no carregador (para tarefas pesadas)
- `setRequiresDeviceIdle(true)` → só roda quando o aparelho está ocioso (API 23+)

**Retry Policy — o que acontece ao falhar:**
- `BackoffPolicy.EXPONENTIAL` → 10s → 20s → 40s → 80s → ... (dobra a cada tentativa)
- `BackoffPolicy.LINEAR` → 10s → 20s → 30s → 40s → ... (incremento fixo)
- O Worker retorna `Result.retry()` para acionar o backoff
- `Result.success()` → concluído, não reagenda
- `Result.failure()` → falhou definitivamente, não tenta mais

**WorkManager sobrevive a:**
- App fechado pelo usuário ✅
- Processo morto pelo sistema (low memory) ✅
- Reinicialização do dispositivo ✅ (reagenda automaticamente)
- Doze Mode ✅ (aguarda a próxima janela de manutenção)

### Prática implementada

**`SyncWorker` — duas fases de sync:**
```
doWork() {
  Fase 1 — PUSH: syncPendingTopicsUseCase()
    → busca topics com syncStatus = PENDING
    → envia ao servidor via API
    → marca como SYNCED no Room se confirmado
    → falhou? → Result.retry() → backoff exponencial

  Fase 2 — PULL: syncTopicsUseCase()
    → busca todos os dados do servidor
    → aplica LWW (Last Write Wins) no Room
    → falhou? → Result.retry() → backoff exponencial

  → Result.success() se tudo ok
}
```

**`setProgress(workDataOf(KEY_PROGRESS to 0/50/100))`**
- Permite que a UI observe o progresso do Worker em tempo real
- 0% = início, 50% = push concluído, 100% = sync completo

**Constraints configuradas:**
```kotlin
Constraints.Builder()
  .setRequiredNetworkType(NetworkType.CONNECTED)
  .setRequiresBatteryNotLow(true)
  .build()
```

**Backoff exponencial configurado:**
```kotlin
setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
// 10s → 20s → 40s → 80s → 160s → ...
```

**`ExistingPeriodicWorkPolicy.KEEP`** — se já existe um trabalho agendado com o mesmo nome, mantém o atual (não recria), evitando duplicação.

### Reflexão

- **WorkManager = tarefas garantidas** — o sistema se compromete a executar mesmo após reboot
- **Exponential Backoff** protege o servidor de avalanches de requisições em caso de falha em massa
- **Dois Constraints** garantem UX educada: não desperdiça dados do usuário (rede) nem bateria
- **PUSH antes do PULL** é a ordem correta: garantir que edições locais chegam ao servidor antes de sobrescrever com dados do servidor
- **`setProgress`** é útil para Workers longos — a UI pode mostrar uma barra de progresso observando `WorkInfo`

---

## Mês 3 | Semana 4 | Terça-feira

### WorkManager Chaining

**Conceito:** encadear Workers em sequência garantida — cada Worker só executa se o anterior retornou `Result.success()`.

```
SyncUpWorker ──success──► SyncDownWorker ──success──► CleanupWorker
      │                          │
   retry/fail               retry/fail
      │                          │
   ⛔ cadeia para            ⛔ cadeia para
```

**API:**
```kotlin
WorkManager.getInstance(this)
    .beginUniqueWork("sync_chain", ExistingWorkPolicy.KEEP, syncUp)
    .then(syncDown)
    .then(cleanup)
    .enqueue()
```

**Passagem de dados entre Workers:**
- `outputData` no Worker que termina → `inputData` no Worker seguinte
- Ex: `SyncUpWorker` passa `KEY_UP_DONE = true` → `SyncDownWorker` lê via `inputData.getBoolean(...)`

**`UniqueWork` — garante que nunca existam 2 cadeias simultâneas:**

| Policy | Comportamento |
|---|---|
| `KEEP` | Já existe? Mantém a atual, não duplica |
| `REPLACE` | Cancela a existente e começa do zero |
| `APPEND` | Aguarda a existente terminar, depois executa |

**Implementado no projeto — 3 Workers especializados:**
- `SyncUpWorker` — PUSH: envia tópicos `PENDING` ao servidor (fase 1)
- `SyncDownWorker` — PULL: busca dados novos do servidor, aplica LWW (fase 2)
- `CleanupWorker` — CLEANUP: remove tópicos `ERROR` há mais de 7 dias do Room (fase 3)

**Importante:** `PeriodicWork` **não suporta chaining** — chaining só funciona com `OneTimeWork`.

**Observação na UI:** `getWorkInfosForUniqueWorkFlow("sync_chain")` retorna um Flow que emite sempre que o estado ou progresso mudam — sem polling. Na cadeia, o Worker com `state == RUNNING` é o ativo e reporta seu progresso individual.

---

## Mês 3 | Semana 4 | Quarta-feira

### Expedited Work + PeriodicWorkRequest + ForegroundLifecycleObserver

#### 1. Expedited Work — `UrgentSyncWorker`

Worker de **alta prioridade**: roda o mais rápido possível, ignorando o job scheduler normal.

```kotlin
OneTimeWorkRequestBuilder<UrgentSyncWorker>()
    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    .build()
```

**Regras obrigatórias:**
- **Deve** implementar `getForegroundInfo()` — o sistema pode precisar rodar como `ForegroundService`, e para isso exige uma `Notification`
- Tem limite de ~10 minutos de execução
- **Não suporta chaining**

**`OutOfQuotaPolicy`** — o que acontece se o app esgotou a cota de expedited:

| Policy | Comportamento |
|---|---|
| `RUN_AS_NON_EXPEDITED_WORK_REQUEST` | Degrada para worker normal (não falha) ✅ |
| `DROP_WORK_REQUEST` | Cancela silenciosamente |

#### 2. PeriodicWorkRequest — a cada 15 minutos

```kotlin
PeriodicWorkRequestBuilder<SyncUpWorker>(15, TimeUnit.MINUTES).build()
```

- **15 min é o mínimo** imposto pelo Android para economizar bateria
- Mesmo que você peça 5min, o WorkManager usa 15min

#### 3. ForegroundSyncObserver — ProcessLifecycleOwner

```kotlin
ProcessLifecycleOwner.get().lifecycle.addObserver(ForegroundSyncObserver(workManager))
```

**`ProcessLifecycleOwner` vs `Activity Lifecycle`:**
- `Activity Lifecycle` dispara ao navegar entre telas (onStop/onStart a cada troca de tela) — ruim para sync
- `ProcessLifecycleOwner` representa **todo o app**: só dispara `onStart` quando o app volta do background — perfeito para sync ao reabrir

```
Usuário navega Home → Detail:
  Activity.onStop/onStart ← dispara
  ProcessLifecycle.onStop/onStart ← NÃO dispara ✅

Usuário pressiona Home (sai do app) e volta:
  ProcessLifecycle.onStop ← dispara
  ProcessLifecycle.onStart ← dispara → UrgentSyncWorker enfileirado ✅
```

#### Visão geral — quando cada sync roda

```
App abre (onCreate)            → scheduleSyncChain()    → cadeia completa (Up→Down→Cleanup)
App volta ao foreground        → ForegroundSyncObserver → UrgentSyncWorker (expedited, imediato)
A cada 15 minutos              → PeriodicWorkRequest    → SyncUpWorker (push de pendentes)
```

---

## Mês 3 | Semana 4 | Quinta-feira

### WorkManager — Observing Work

**Conceito:** observar o `WorkInfo` no ViewModel via Flow e traduzir os estados do WorkManager em feedback visual para o usuário.

#### WorkInfo.State — todos os estados possíveis

| Estado | Significado |
|---|---|
| `ENQUEUED` | Agendado, aguardando constraints (rede, bateria) |
| `BLOCKED` | Na cadeia, aguardando o worker anterior terminar |
| `RUNNING` | Executando agora — pode reportar progresso via `setProgress()` |
| `SUCCEEDED` | Terminou com sucesso |
| `FAILED` | Esgotou todas as tentativas de retry |
| `CANCELLED` | Cancelado manualmente |

#### API de observação

```kotlin
// Flow — emite sempre que estado ou progresso mudam (sem polling)
workManager
    .getWorkInfosForUniqueWorkFlow("sync_chain")
    .collect { workInfoList: List<WorkInfo> ->
        val running = workInfoList.firstOrNull { it.state == WorkInfo.State.RUNNING }
        val progress = running?.progress?.getInt("sync_progress", 0) ?: 0
    }
```

#### SyncState — sealed interface que mapeia WorkInfo.State → UX

```kotlin
sealed interface SyncState {
    data object Idle      : SyncState   // nenhum worker ativo
    data object Enqueued  : SyncState   // aguardando constraints
    data class  Running(val progress: Int) : SyncState  // executando, 0..100%
    data object Succeeded : SyncState   // cadeia completa ✓
    data object Failed    : SyncState   // falhou após retries ✗
}
```

**Por que criar SyncState em vez de usar WorkInfo.State direto na UI?**
- `WorkInfo.State` é um detalhe do WorkManager — a UI não deveria conhecê-lo diretamente
- `SyncState` é uma abstração de UX — desacopla a UI da biblioteca
- Facilita testes (mockar `SyncState` é trivial)
- Cadeia tem múltiplos workers: precisamos de lógica para combinar estados em um único estado de UX

#### Mapeamento implementado no ViewModel

```
workInfoList.any { RUNNING }   → SyncState.Running(progress)
workInfoList.any { ENQUEUED ou BLOCKED } → SyncState.Enqueued
workInfoList.any { FAILED }    → SyncState.Failed
workInfoList.all { SUCCEEDED } → SyncState.Succeeded
else                           → SyncState.Idle
```

#### SyncStatusBar — feedback visual na HomeScreen

| SyncState | UX |
|---|---|
| `Idle` | Invisível (sem ruído) |
| `Enqueued` | ⏳ "Aguardando sync..." (cinza) |
| `Running(33)` | Barra de progresso determinada + "Sincronizando... 33%" |
| `Succeeded` | ✓ "Sincronizado ✓" (verde) |
| `Failed` | ✗ "Erro ao sincronizar ✗" (vermelho) |

`AnimatedVisibility` com `fadeIn/fadeOut` garante transições suaves entre estados.

#### setProgress() — progresso granular na cadeia

```
SyncUpWorker:   0% → 33%   (PUSH)
SyncDownWorker: 33% → 66%  (PULL)
CleanupWorker:  66% → 100% (CLEANUP)
```
O ViewModel pega o progresso do worker `RUNNING` no momento — a barra avança de 0 a 100% ao longo de toda a cadeia.

---

## Mês 3 | Semana 4 | Sexta-feira — RESUMO MÊS 2

### Revisão da arquitetura de sync completa

Três estratégias complementares, cada uma resolvendo um cenário diferente:

| Estratégia | Tipo | Worker(s) | Quando roda | O que faz |
|---|---|---|---|---|
| `sync_chain` | UniqueWork (OneTime) | Up→Down→Cleanup | App abre | PUSH + PULL + limpeza (cadeia completa) |
| `urgent_sync` | UniqueWork (Expedited) | UrgentSyncWorker | App volta ao foreground | PUSH + PULL imediatos, alta prioridade |
| `periodic_sync` | PeriodicWork | SyncUpWorker | A cada 15 min | Só PUSH (PeriodicWork não suporta chaining) |

**Por que 3 e não 1?**
- `PeriodicWork` não encadeia → não substitui a cadeia
- `Expedited` não é periódico → não substitui o timer
- Cada um cobre uma lacuna que os outros não cobrem

---

### RESUMO COMPLETO MÊS 2

#### Semana 1 — Offline-First Foundation

- **Padrão SSOT:** Room é a fonte de verdade, API é apenas sync
- **Fluxo:** `API → Room → UI` — UI nunca lê da API diretamente
- **`getTopicsStream()`** retorna `Flow` do Room — reativo, UI nunca "puxa"
- **`sync()`** separado do read — pode ser chamado pelo WorkManager sem UI aberta
- **`:core:network`** criado com Retrofit, DTOs separados das Entities do Room
- **Módulo de rede** conectado à API real: `jsonplaceholder.typicode.com/posts`

#### Semana 2 — Error Handling + Testes

- **`AppResult<T>`** sealed interface: `Loading`, `Success(data)`, `Error(exception, cachedData?)`
  - Por que não `Result<T>` do stdlib? Não tem `Loading` nem `cachedData`
- **3 cenários offline modelados explicitamente no UiState:**
  - Online + cache → `isRefreshing`, lista atualiza
  - Offline + cache → `isOffline = true`, banner de aviso
  - Offline + sem cache → `syncFailed = true`, tela de erro com retry
- **Testes com Fakes manuais + Turbine:** 6 cenários cobrindo todo o Repository
- **Princípio central consolidado: **nunca mostrar tela em branco se há cache**

#### Semana 3 — Conflict Resolution + Optimistic Updates

- **Optimistic Update:** UI atualiza *antes* da API confirmar → UX instantânea
  - Room primeiro → API depois → rollback se falhar
- **Rollback:** guarda snapshot *antes* de alterar → restaura se API rejeitar
  - Por que `insert` no rollback e não `delete`? O dado existia antes
  - O Flow do Room reverte a UI automaticamente (sem código extra na UI)
- **Last Write Wins (LWW):**
  - `server.updatedAt > local.updatedAt` → server vence → SYNCED
  - `server.updatedAt < local.updatedAt` → local vence → PENDING (edição offline protegida)
  - Empate → CONFLICT; novo do server → SYNCED
  - Alternativas rejeitadas: Server Wins (perde edições offline), CRDTs (overkill)
- **`SyncStatus`** por registro: SYNCED ✓ / PENDING ⏳ / CONFLICT ⚡ / ERROR ✗
- **Migrations Room:** `updatedAt` (v1→v2), `syncStatus` (v2→v3)

#### Semana 4 — WorkManager

- **WorkManager Chaining:** `beginUniqueWork().then().then().enqueue()`
  - Cada Worker só roda se o anterior retornou `Result.success()`
  - Passagem de dados: `outputData` → `inputData` entre Workers
  - `ExistingWorkPolicy`: KEEP, REPLACE, APPEND, APPEND_OR_REPLACE
- **Expedited Work:** alta prioridade, roda em segundos
  - Obrigatório implementar `getForegroundInfo()` com Notification
  - `OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST` → degrada sem falhar
- **PeriodicWork:** mínimo 15 minutos, não suporta chaining
- **`ProcessLifecycleOwner`:** observer do processo todo (não de Activity)
  - `onStart` → app voltou do background → `UrgentSyncWorker` expedited
  - Não dispara ao navegar entre telas (diferente de Activity.onStart)
- **Observing Work:** `getWorkInfosForUniqueWorkFlow()` → Flow de WorkInfo
  - `WorkInfo.State` → `SyncState` (sealed interface de UX)
  - `SyncStatusBar` na HomeScreen: ⏳ Aguardando / 🔄 Sincronizando 33% / ✓ Sincronizado / ✗ Erro
  - `setProgress()` para progresso granular por fase: 0→33→66→100%

---

### ADR escrita

`docs/adr/002-estrategia-sincronizacao.md` — documenta:
- Decisão pelo padrão offline-first com SSOT
- As 3 estratégias de sync e por que cada uma existe
- Escolha do LWW vs CRDTs/OT (e por que LWW é suficiente para notas pessoais)
- Optimistic Updates com rollback vs Pessimistic
- SyncStatus por registro e feedback visual ao usuário
- Consequências, trade-offs e possíveis evoluções futuras

---

### ✅ Checklist Mês 2

- ✅ Room avançado: migrations, FTS, relations, TypeConverters
- ✅ Offline-first: SSOT, Network→DB→UI, stale-while-revalidate
- ✅ Conflict resolution: LWW com `updatedAt`
- ✅ Optimistic updates + rollback automático via Room Flow
- ✅ WorkManager: chaining, expedited, periodic, `setProgress`
- ✅ Observing Work: `WorkInfo.State` → `SyncState` → UI
- ✅ `ForegroundSyncObserver` com `ProcessLifecycleOwner`
- ✅ 1 ADR de sync completa com diagrama de fluxo

---

## Mês 3 | Semana 1 | Segunda-feira

### Mobile System Design Framework

**Framework geral: Requirements → Constraints → HLD → Deep Dives**

#### 1. Functional Requirements
O que o app *faz*. Listar features principais e fora de escopo (non-goals).

Para o StaffNotes:
- Criar, editar, deletar nota
- Buscar notas (full-text)
- Sincronizar com servidor
- Funcionar offline

#### 2. Non-Functional Requirements (Constraints)
Como o app *se comporta*. São os critérios de qualidade:
- **Offline-first:** funciona sem internet, sync quando conectar
- **Performance:** lista fluida com 100K notas (Paging + FTS)
- **Confiabilidade:** nenhuma nota perdida (LWW + SyncStatus)
- **Bateria/rede:** WorkManager com constraints, backoff exponencial

> ⚠️ Em Mobile System Design, sempre **começar pelos constraints** — eles guiam todas as decisões de arquitetura.

#### 3. High-Level Design (HLD)
Diagrama de componentes e fluxo de dados:

```
UI (Compose)
   └── ViewModel (MVI)
         └── UseCase
               └── Repository
                     ├── LocalDataSource (Room)
                     └── RemoteDataSource (Retrofit/Ktor)
WorkManager (sync periódico + expedited)
```

#### 4. Deep Dives
Entrar em detalhes de cada componente crítico:
- Como funciona o sync? (LWW, PENDING→SYNCED, rollback)
- Como paginar 100K notas? (Paging 3 + Room PagingSource)
- Como garantir busca rápida? (FTS4/FTS5)
- Como lidar com conflitos? (timestamp updatedAt)

**Resumo:**
- Framework: Requirements → Constraints → HLD → Deep Dive
- Mobile: sempre começar por constraints (offline? bateria? volume de dados?)
- HLD deve caber num quadro branco — simples e claro

---

## Mês 3 | Semana 1 | Terça-feira

### Camada de Rede — Networking Layer

**Componentes implementados no `:core:network`:**

#### 1. Timeouts — por que são obrigatórios
- **connectTimeout (10s):** tempo máximo para estabelecer a conexão TCP com o servidor. Se ultrapassar → `ConnectTimeoutException`
- **readTimeout (30s):** tempo máximo para receber o corpo da resposta. Uploads grandes precisam de valor maior.
- **writeTimeout (30s):** tempo máximo para enviar o corpo da requisição (ex: upload de imagem).
- Sem timeouts: requisição pode ficar pendurada *para sempre* → ANR / UX travada.

#### 2. AuthInterceptor — autenticação transparente
- Adiciona o header `Authorization: Bearer <token>` em **todas** as requisições automaticamente.
- O `tokenProvider` é um lambda `() -> String` — permite injetar qualquer fonte do token (DataStore, EncryptedSharedPrefs).
- Se token estiver vazio (ex: usuário não logado): não adiciona o header, sem erro.
- **Por que Interceptor e não parâmetro em cada @GET?** — DRY. Um lugar só cuida de auth.

#### 3. RetryInterceptor — resiliência automática
- Faz retry em:
  - `IOException` (sem internet, DNS, conexão recusada) → pode ser transiente
  - Resposta `5xx` (erro do servidor) → pode ser overload momentâneo
- **NÃO** faz retry em `4xx`:
  - `401` Unauthorized → token inválido, retry vai falhar de novo
  - `404` Not Found → o recurso não existe, retry é inútil
- Máximo de 3 tentativas (além da original = 4 tentativas no total).
- Diferença vs WorkManager BackoffPolicy: este interceptor é para falhas *dentro de uma requisição*. O WorkManager é para falhas do Worker inteiro.

#### 4. Cursor Pagination — por que e como

```
Offset:  GET /posts?page=2&per_page=10
Cursor:  GET /posts?_start=10&_limit=10
```

**Problema do offset:** se um item for inserido na página 1 enquanto o usuário lê a página 2, os itens se deslocam → o usuário vê um item duplicado ou pula um.

**Cursor:** usa a posição (`id` ou `timestamp`) do último item recebido como âncora — não se importa com inserções anteriores.

```kotlin
// Primeira página
fetchTopicsPaged(cursor = 0, limit = 20)
// → nextCursor = 20

// Segunda página (usa o nextCursor da resposta anterior)
fetchTopicsPaged(cursor = 20, limit = 20)
// → nextCursor = 40
```

**`PagedResult<T>`:**
```kotlin
data class PagedResult<T>(
    val data: List<T>,
    val nextCursor: Int?,  // null = última página
    val total: Int
)
val hasMore: Boolean get() = nextCursor != null
```

**Quando usar:**
- Cursor → feeds, listas dinâmicas (redes sociais, restaurantes, notícias)
- Offset → relatórios, tabelas com dados estáticos onde o usuário pode pular para "página 5"

---

## Mês 3 | Semana 1 | Quarta-feira

### Camada de Persistência — Room vs DataStore vs SharedPreferences

**Comparativo completo:**

| Opção | Estrutura | API | Thread-safe | Quando usar |
|---|---|---|---|---|
| **Room** | SQL (tabelas + relações) | DAO + Flow | ✅ (IO thread) | Dados estruturados, queries complexas, listas |
| **DataStore Preferences** | Chave-valor tipado | Flow + suspend | ✅ (assíncrono) | Configurações simples, flags, last sync |
| **DataStore Proto** | Schema Protobuf | Flow + suspend | ✅ | Dados tipados com schema versionado |
| **SharedPreferences** | Chave-valor XML | Síncrono | ❌ (ANR risk) | Legado — nunca em código novo |

**Regra de ouro:**
- Tem relações, queries, filtros? → **Room**
- Precisa salvar "última sync" ou "tema escolhido"? → **DataStore Preferences**
- Código legado que não posso reescrever agora? → **SharedPreferences** (apenas manter)

#### DataStore na prática — `UserPreferencesDataSource`

```kotlin
// Leitura: Flow reativo — emite sempre que uma preferência muda
val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
    UserPreferences(
        lastSyncAt  = prefs[Keys.LAST_SYNC_AT] ?: 0L,
        isDarkTheme = prefs[Keys.IS_DARK_THEME],
        fontSize    = FontSize.entries[prefs[Keys.FONT_SIZE] ?: 1]
    )
}

// Escrita: suspend — nunca bloqueia a Main thread
suspend fun updateLastSyncAt(timestampMs: Long) {
    dataStore.edit { prefs ->
        prefs[Keys.LAST_SYNC_AT] = timestampMs  // ← transação atômica
    }
}
```

**Por que `edit { }` é atômico?**
- SharedPreferences: `put` + `commit` são duas operações separadas → crash pode deixar estado inconsistente
- DataStore: `edit { }` é uma transação — ou tudo salva ou nada muda

**Onde fica o arquivo?**
```
data/data/<package>/files/datastore/user_preferences.preferences_pb
```
- Formato binário (`.pb`) — mais eficiente que XML do SharedPreferences

**Schema design para performance no Room:**
- Sempre indexar colunas usadas em `WHERE` e `ORDER BY`:
  ```sql
  CREATE INDEX idx_topics_sync_status ON topics(sync_status)
  CREATE INDEX idx_topics_updated_at ON topics(updated_at)
  ```
- Nunca fazer `SELECT *` em tabelas grandes — selecione só as colunas que a UI precisa
- Use `@DatabaseView` para queries complexas que são lidas com frequência (pré-computado pelo Room)

---

## Mês 3 | Semana 1 | Quinta-feira

### System Design Interview — App de Delivery

**Exercício completo documentado em:** `docs/system-design/delivery-app.md`

#### Framework aplicado

**1. Functional Requirements**
- Ver restaurantes próximos (paginado por localização)
- Ver cardápio
- Montar carrinho + checkout
- Rastrear pedido em tempo real
- Histórico de pedidos

**2. Non-Functional (Constraints) — os que guiaram as decisões:**
- Offline parcial → cache de cardápios no Room
- 100K restaurantes → cursor pagination + busca por raio geográfico
- Status do pedido real-time → polling 30s (MVP) vs WebSocket (evolução)
- Carrinho não pode ser perdido → Room como SSOT do carrinho

**3. HLD — diagrama de componentes:**
```
UI → ViewModel → UseCase → Repository → Room + Retrofit
                                         ↑
                              WorkManager (OrderStatusWorker a cada 30s)
```

**4. Deep Dives:**
- Cursor pagination para lista de restaurantes (dados dinâmicos que mudam)
- Carrinho offline-first no Room
- `priceAtOrder` — snapshot imutável do preço (nunca JOIN com preço atual)
- Polling vs WebSocket: polling é suficiente para MVP

**Decisão de destaque — Snapshot de preço:**
```kotlin
data class OrderItem(
    val orderId: String,
    val menuItemId: String,
    val quantity: Int,
    val priceAtOrder: Double  // ← snapshot — nunca mudar depois do pedido confirmado
)
```
O preço pago é imutável. Se calcularmos o total com `MenuItem.price` atual, o histórico ficará errado quando o restaurante mudar o preço.

**Onde travei:**
- Múltiplos restaurantes no mesmo carrinho → validar no UseCase
- Deep links para compartilhar restaurante → próxima semana
- Push via FCM para status do pedido → FCM Data Message → Room → Flow → UI

```
**Reflexão:**
- Cursor > offset para dados dinâmicos
- Offline é um constraint, não um feature — planejar desde o início
- Snapshot de dados financeiros é um padrão crítico (preço, taxa de câmbio, etc.)
