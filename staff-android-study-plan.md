# 📋 Plano de Estudos — Staff Android Engineer

## 🗓️ Duração: 12 meses | Seg a Sex | 1h por dia
## 📊 Distribuição: 70% Android + IA | 20% KMP | 10% Back-end/AWS

## Progresso Geral:

- [X] Q1 — Fundações (Meses 1-3)
- [ ] Q2 — Performance, Qualidade + IA (Meses 4-6)
- [ ] Q3 — Expansão: KMP + Back-end/AWS (Meses 7-9)
- [ ] Q4 — Impacto & Consolidação (Meses 10-12)

---

# 📅 Q1 — FUNDAÇÕES (Meses 1-3) | 100% Android

---

## 📅 Mês 1 — Arquitetura Android Moderna

### Semana 1: App Architecture Guide + Setup

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Leia "Guide to app architecture — UI Layer"
    - Link: https://developer.android.com/topic/architecture/ui-layer
    - Foco: UI State, StateHolder (ViewModel), UI Events
  - [X] 💻 **Prática (30min):**
    - Crie o projeto "StaffNotes" no Android Studio
    - Configure: minSdk 26, targetSdk 35, Kotlin 2.x, Compose BOM
    - Crie módulo :app com tela "Hello StaffNotes"
    - Crie NoteListViewModel com StateFlow\<NoteListUiState\>
  - [X] ✍️ **Reflexão (10min):** Escreva no LEARNING_LOG.md:
    - UI Layer = ViewModel + UI State + UI Elements
    - ViewModel expõe StateFlow, UI coleta com collectAsState
    - UI State deve ser imutável (data class)

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia "Guide to app architecture — Domain Layer"
    - Link: https://developer.android.com/topic/architecture/domain-layer
    - Foco: UseCases, quando usar, quando NÃO usar
  - [X] 💻 **Prática (30min):**
    - Crie módulo :core:model
    - Crie `data class Note(id, title, content, createdAt, updatedAt)`
    - Crie módulo :core:data com `interface NoteRepository`
    - Crie `GetNotesUseCase` no feature module
    - Conecte: ViewModel → UseCase → Repository (fake por enquanto)
  - [X] ✍️ **Reflexão (10min):**
    - Domain Layer é opcional — só usar quando tem lógica combinada
    - UseCase = 1 responsabilidade, operator fun invoke()
    - Não criar UseCase se ele só repassa pro Repository

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Guide to app architecture — Data Layer"
    - Link: https://developer.android.com/topic/architecture/data-layer
    - Foco: Repository pattern, DataSource, Single Source of Truth
  - [X] 💻 **Prática (30min):**
    - Crie módulo :core:database
    - Configure Room: NoteEntity, NoteDao, AppDatabase
    - Crie NoteLocalDataSource (wrapper do DAO)
    - Implemente NoteRepositoryImpl no :core:data
    - Conecte tudo: UI → ViewModel → UseCase → Repository → Room
  - [X] ✍️ **Reflexão (10min):**
    - Data Layer = Repository + DataSources
    - Repository decide de ONDE vêm os dados
    - Single Source of Truth = Room (DB é a verdade)

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "State holders and UI State"
    - Link: https://developer.android.com/topic/architecture/ui-layer/stateholders
    - Foco: UDF (Unidirectional Data Flow), state hoisting
  - [X] 💻 **Prática (30min):**
    - Refatore NoteListViewModel para UDF:
      - sealed interface NoteListEvent { AddNote, DeleteNote, Search }
      - fun onEvent(event: NoteListEvent)
    - Crie NoteListScreen em Compose coletando state
    - Teste: adicionar nota e ver na lista
  - [X] ✍️ **Reflexão (10min):**
    - UDF: Events sobem (UI → VM), State desce (VM → UI)
    - ViewModel é o "processador" de eventos
    - UI NUNCA muda estado diretamente

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia anotações + Leia "Architecture recommendations"
    - Link: https://developer.android.com/topic/architecture/recommendations
  - [X] 💻 **Prática (30min):**
    - Revise código da semana, limpe e organize
    - Rode o app: lista de notas funcionando com Room
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Projeto criado com 3 camadas (UI, Domain, Data)
    - ✅ Room como source of truth
    - ✅ UDF implementado

- [X] ✅ **Semana 1 concluída**

---

### Semana 2: MVI na Prática

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Leia "MVI Architecture" — Hannes Dorfmann
    - Link: https://hannesdorfmann.com/android/model-view-intent/
    - Foco: Intent (evento), Model (estado), como difere de MVVM
  - [X] 💻 **Prática (30min):**
    - Crie sealed interfaces robustas:
      - `NoteListIntent { LoadNotes, SearchNotes(query), DeleteNote(id) }`
      - `NoteListState(notes, isLoading, error, searchQuery)`
    - Implemente processamento de Intents no ViewModel
  - [X] ✍️ **Reflexão (10min):**
    - MVI = MVVM com eventos tipados (sealed class)
    - Intent → Reducer → novo State (puro)
    - Side effects tratados separadamente

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia "Side Effects in Jetpack Compose"
    - Link: https://developer.android.com/develop/ui/compose/side-effects
    - Foco: LaunchedEffect, SideEffect, rememberCoroutineScope
  - [X] 💻 **Prática (30min):**
    - Crie sealed interface de Side Effects:
      - `NoteListSideEffect { NavigateToDetail(id), ShowSnackbar(msg) }`
    - Use Channel\<SideEffect\> no ViewModel
    - Colete com LaunchedEffect na UI
  - [X] ✍️ **Reflexão (10min):**
    - Side effects = coisas que acontecem UMA vez
    - Channel garante: emitiu → consumiu → acabou
    - NÃO colocar side effects no State

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia "ViewModel Overview"
    - Link: https://developer.android.com/topic/libraries/architecture/viewmodel
    - Foco: lifecycle, SavedStateHandle
  - [X] 💻 **Prática (30min):**
    - Adicione SavedStateHandle ao ViewModel
    - Salve searchQuery (sobrevive process death)
    - Teste: busque → rotacione → query mantida?
    - Teste: force process death → query mantida?
  - [X] ✍️ **Reflexão (10min):**
    - SavedStateHandle sobrevive process death
    - StateFlow NÃO sobrevive
    - Dados do usuário → SavedStateHandle

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia Orbit MVI ou Circuit (escolha um):
    - Orbit: https://github.com/orbit-mvi/orbit-mvi
    - Circuit (Slack): https://slackhq.github.io/circuit/
  - [X] 💻 **Prática (30min):**
    - Experimente a lib escolhida
    - Compare: código com lib vs sem lib
  - [X] ✍️ **Reflexão (10min):**
    - Minha decisão: [manual vs lib] porque [motivo]
    - Trade-off: dependência vs produtividade

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Managing State with Compose"
    - Link: https://developer.android.com/develop/ui/compose/state
  - [X] 💻 **Prática (30min):**
    - Refatore NoteListScreen: state hoisting completo
    - Crie Previews com fake state
    - Crie NoteDetailScreen com mesmo padrão MVI
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ MVI com Intent, State, SideEffect
    - ✅ SavedStateHandle para process death
    - ✅ 2 telas com MVI: List + Detail

- [X] ✅ **Semana 2 concluída**

---

### Semana 3: Multi-Module Architecture

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Leia "Guide to Android app modularization"
    - Link: https://developer.android.com/topic/modularization
    - Foco: por que modularizar, tipos de módulos
  - [X] 💻 **Prática (30min):**
    - Clone Now in Android: `git clone https://github.com/android/nowinandroid.git`
    - Explore: :core:*, :feature:*, build-logic/
    - Abra build.gradle.kts de 3 módulos diferentes
  - [X] ✍️ **Reflexão (10min):**
    - :feature depende de :core, nunca de outro :feature
    - :core:model não depende de nada (puro)
    - Dependências apontam "para dentro"

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia "Modularization — Common patterns"
    - Link: https://developer.android.com/topic/modularization/patterns
    - Foco: by feature vs by layer, API modules
  - [X] 💻 **Prática (30min):**
    - Crie módulo :feature:notes-list (mova código da lista)
    - Configure build.gradle.kts com dependências corretas
    - Garanta que compila
  - [X] ✍️ **Reflexão (10min):**
    - :app agora só faz navigation + DI
    - :feature:notes-list compila independente

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Estude navegação no Now in Android:
    - Link: https://github.com/android/nowinandroid/tree/main/app/src/main/kotlin/com/google/samples/apps/nowinandroid/navigation
  - [X] 💻 **Prática (30min):**
    - Crie :feature:note-detail (mova tela de detalhe)
    - Cada feature expõe `fun navGraph(NavGraphBuilder)`
    - :app monta NavHost chamando cada feature
    - Teste: lista → detalhe → voltar
  - [X] ✍️ **Reflexão (10min):**
    - Cada feature expõe rota sem conhecer as outras
    - :app é o orquestrador de navegação

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Hilt in multi-module"
    - Link: https://developer.android.com/training/dependency-injection/hilt-multi-module
  - [X] 💻 **Prática (30min):**
    - Configure Hilt nos módulos:
      - :core:data → @Module @Binds NoteRepository
      - :core:database → @Module @Provides AppDatabase
      - :feature:notes-list → @HiltViewModel
    - Rode: lista de notas via Room + Hilt DI
  - [X] ✍️ **Reflexão (10min):**
    - Hilt multi-module: cada módulo declara @Module
    - :app instala tudo via @HiltAndroidApp

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Leia modules-graph-assert README
    - Link: https://github.com/jraska/modules-graph-assert
  - [X] 💻 **Prática (30min):**
    - Adicione plugin, gere grafo de módulos
    - Configure regra: :feature não depende de :feature
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ App modularizado: :app, :core:*, :feature:*
    - ✅ Navegação multi-module
    - ✅ Hilt cross-module
    - ✅ Module graph validado

- [X] ✅ **Semana 3 concluída**

---

### Semana 4: Gradle Avançado + Build Optimization

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Estude build-logic/ do Now in Android
    - Link: https://github.com/android/nowinandroid/tree/main/build-logic
    - Foco: Convention Plugins
  - [X] 💻 **Prática (30min):**
    - Crie build-logic/ no StaffNotes
    - Crie AndroidLibraryConventionPlugin
    - Aplique nos :core:* modules
  - [X] ✍️ **Reflexão (10min):**
    - Convention Plugin = eliminar duplicação nos build.gradle
    - 1 linha aplica toda a config padrão

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia "Migrate to Version Catalogs"
    - Link: https://developer.android.com/build/migrate-to-catalogs
  - [X] 💻 **Prática (30min):**
    - Organize gradle/libs.versions.toml completo
    - Migre TODAS as dependências para o catalog
  - [X] ✍️ **Reflexão (10min):**
    - Version Catalog = single source of truth para versões
    - Autocomplete: libs.androidx.room.runtime

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Optimize your build speed"
    - Link: https://developer.android.com/build/optimize-your-build
  - [X] 💻 **Prática (30min):**
    - Rode `./gradlew assembleDebug --scan`
    - Analise build scan
    - Ative: configuration-cache, parallel, caching
  - [X] ✍️ **Reflexão (10min):**
    - Build time ANTES: 38s (clean build, sem otimizações)
    - Build time DEPOIS: 5s (2º build — configuration cache reused + build cache hit)

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Use the Build Analyzer"
    - Link: https://developer.android.com/build/build-analyzer
  - [X] 💻 **Prática (30min):**
    - Build Analyzer: top 3 tasks mais lentas
    - KSP ao invés de KAPT?
    - Remova dependências desnecessárias
  - [X] ✍️ **Reflexão (10min):**
    - KSP é ~2x mais rápido que KAPT — projeto já usa KSP (Room + Hilt), zero KAPT ✅
    - Tasks mais lentas: mergeDebugResources (2.3s) · mergeDebugJavaResource (2.2s) · mergeExtDexDebug (1.1s)

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Architecture Learning Journey" (NiA)
    - Link: https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
  - [X] 💻 **Prática (30min):**
    - Revise StaffNotes completo
    - Escreva ADR: "Arquitetura do StaffNotes"
  - [X] ✍️ **Reflexão (10min):** RESUMO MÊS 1
    - ✅ Projeto modularizado com MVI + UDF
    - ✅ Room, Hilt, Navigation multi-module
    - ✅ Convention Plugins + Version Catalog
    - ✅ Build otimizado com métricas
    - ✅ 1 ADR escrita

- [X] ✅ **Semana 4 concluída**
- [X] ✅ **Mês 1 concluído**

---

## 📅 Mês 2 — Offline-First & Sincronização

### Semana 1: Room Avançado

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Leia "Room — Migrating database versions"
    - Link: https://developer.android.com/training/data-storage/room/migrating-db-versions
    - Foco: auto migrations vs manual
  - [X] 💻 **Prática (30min):**
    - Adicione campo `isPinned: Boolean` ao NoteEntity
    - Crie Migration manual (versão 1 → 2)
    - Crie teste de migration com MigrationTestHelper
  - [X] ✍️ **Reflexão (10min):**
    - Auto migration: Room gera se mudança é simples
    - Manual: necessário se renomeia/deleta column
    - SEMPRE testar migrations

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia "Room — Define relationships"
    - Link: https://developer.android.com/training/data-storage/room/relationships
    - Foco: @Embedded, @Relation, 1:N, N:N
  - [X] 💻 **Prática (30min):**
    - Crie TagEntity (id, name, color)
    - Crie NoteTagCrossRef (N:N relationship)
    - Query: notas com suas tags
  - [X] ✍️ **Reflexão (10min):**
    - @Relation = Room faz 2 queries internamente
    - Cuidado com N+1 em listas grandes

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Room — Full Text Search"
    - Link: https://developer.android.com/reference/androidx/room/Fts4
    - Complementar: https://www.sqlite.org/fts5.html
  - [X] 💻 **Prática (30min):**
    - Crie módulo :feature:search
    - Crie FTS table para notas
    - Implemente busca full-text por título e conteúdo
  - [X] ✍️ **Reflexão (10min):**
    - FTS = indexa palavras para busca rápida
    - Performance muito melhor que LIKE '%query%'

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Room — Database Views"
    - Link: https://developer.android.com/training/data-storage/room/creating-views
  - [X] 💻 **Prática (30min):**
    - Crie DatabaseView: NoteWithTagCount
    - Crie TypeConverter para Date/Instant
    - Refatore lista para usar a View
  - [X] ✍️ **Reflexão (10min):**
    - DatabaseView = query como "tabela virtual"
    - TypeConverter: converte tipos que Room não conhece

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Leia "7 Pro Tips for Room"
    - Link: https://medium.com/androiddevelopers/7-pro-tips-for-room-fbadea4bfbd1
  - [X] 💻 **Prática (30min):**
    - Refatore DAOs, organize queries por feature
    - Adicione @Transaction onde necessário
    - Rode testes de Room
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Migrations com testes
    - ✅ Relationships N:N
    - ✅ FTS search funcional
    - ✅ Database Views e TypeConverters

- [X] ✅ **Semana 1 concluída**

---

### Semana 2: Single Source of Truth

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Leia "Build an offline-first app"
    - Link: https://developer.android.com/topic/architecture/data-layer/offline-first
    - Foco: padrão Network → DB → UI
  - [X] 💻 **Prática (30min):**
    - Crie módulo :core:network
    - Crie fake API (mock JSON ou json-server)
    - Crie NoteRemoteDataSource com Ktor ou Retrofit
  - [X] ✍️ **Reflexão (10min):**
    - Offline-first: DB é a verdade, API é sync
    - UI sempre lê do DB (Flow)

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia Data Layer docs (Repository)
    - Link: https://developer.android.com/topic/architecture/data-layer
    - Foco: como Repository orquestra local + remote
  - [X] 💻 **Prática (30min):**
    - Refatore NoteRepositoryImpl:
      - `getNotesStream()` → retorna Flow do Room
      - `sync()` → chama API, salva no Room
    - UI coleta do Flow (auto-update quando Room muda)
  - [X] ✍️ **Reflexão (10min):**
    - Repository expõe Flows do DB (reativo)
    - Sync é separado do read

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia sobre "stale-while-revalidate"
    - Link: https://developer.android.com/topic/architecture/data-layer/offline-first#synchronization
  - [X] 💻 **Prática (30min):**
    - Implemente: mostra cache + atualiza em background
    - Loading state: `UiState(notes, isRefreshing, lastSync)`
    - Mostre "Última sync: 5min atrás"
  - [X] ✍️ **Reflexão (10min):**
    - stale-while-revalidate: dados rápidos, refresh em background
    - UX: nunca bloquear UI esperando API

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Handle errors in data layer"
    - Link: https://developer.android.com/topic/architecture/data-layer#error-handling
  - [X] 💻 **Prática (30min):**
    - Crie sealed interface Result\<T\> { Success, Error, Loading }
    - Implemente 3 cenários:
      - Online+cache → atualiza
      - Offline+cache → mostra cache + aviso
      - Offline+sem cache → tela de erro
  - [X] ✍️ **Reflexão (10min):**
    - Nunca mostrar tela em branco se tem cache
    - Cada cenário tem UX diferente

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia anotações + revise fluxo completo
  - [X] 💻 **Prática (30min):**
    - Escreva testes para NoteRepositoryImpl:
      - API funciona → dados atualizados
      - API falha → retorna cache
      - Sem cache sem internet → erro
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Network → DB → UI implementado
    - ✅ Stale-while-revalidate funcional
    - ✅ Error handling com 3 cenários
    - ✅ Testes de Repository

- [X] ✅ **Semana 2 concluída**

---

### Semana 3: Conflict Resolution & Optimistic Updates

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "optimistic updates mobile pattern"
    - Conceito: atualizar UI antes da confirmação do servidor
  - [X] 💻 **Prática (30min):**
    - Implemente: ao editar nota, UI atualiza imediatamente
    - Salva no Room → mostra na UI → envia pra API em background
  - [X] ✍️ **Reflexão (10min):**
    - Optimistic update = UX instantânea
    - Risco: API pode rejeitar

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia sobre conflict resolution
    - Link: https://developer.android.com/topic/architecture/data-layer/offline-first#conflict-resolution
  - [X] 💻 **Prática (30min):**
    - Implemente rollback se API retorna erro:
      - Salva estado anterior antes do update
      - Se falha: restaura no Room
      - Snackbar: "Erro ao salvar. Alteração revertida."
  - [X] ✍️ **Reflexão (10min):**
    - Rollback = guardar snapshot antes da mudança
    - UX: Snackbar com "Tentar novamente"

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "last write wins vs merge conflict resolution"
    - Conceitos: LWW, CRDTs, merge strategies
  - [X] 💻 **Prática (30min):**
    - Adicione campo `updatedAt` em Note
    - Implemente Last Write Wins com timestamp
    - Se server.updatedAt > local → server vence
  - [X] ✍️ **Reflexão (10min):**
    - LWW é simples mas pode perder dados
    - Para notas: LWW é suficiente

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "Google Keep architecture sync"
    - Compare abordagens de sync de apps conhecidos
  - [X] 💻 **Prática (30min):**
    - Crie SyncStatus enum: SYNCED, PENDING, CONFLICT, ERROR
    - Mostre ícone de status em cada nota na lista
    - Notas pendentes com ícone de "sync pendente"
  - [X] ✍️ **Reflexão (10min):**
    - Visual feedback de sync é crucial
    - Usuário precisa saber se dado está salvo na nuvem

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia anotações da semana
  - [X] 💻 **Prática (30min):**
    - Escreva testes:
      - Optimistic update + rollback
      - LWW com timestamps diferentes
      - SyncStatus transitions
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Optimistic updates + rollback
    - ✅ Last Write Wins com timestamps
    - ✅ SyncStatus visual na UI
    - ✅ Testes de sync

- [X] ✅ **Semana 3 concluída**

---

### Semana 4: WorkManager Avançado

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Leia "WorkManager Overview"
    - Link: https://developer.android.com/topic/libraries/architecture/workmanager
    - Foco: quando usar, constraints, retry policy
  - [X] 💻 **Prática (30min):**
    - Crie SyncWorker: sincroniza notas pendentes
    - Configure constraints: requiresNetwork, battery ok
    - Configure retry: BackoffPolicy.EXPONENTIAL
  - [X] ✍️ **Reflexão (10min):**
    - WorkManager = tarefas garantidas (sobrevive reboot)
    - Exponential backoff: 10s, 20s, 40s, 80s...

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia "WorkManager — Chaining"
    - Link: https://developer.android.com/topic/libraries/architecture/workmanager/how-to/chain-work
  - [X] 💻 **Prática (30min):**
    - Crie cadeia: SyncUpWorker → SyncDownWorker → CleanupWorker
    - Configure UniqueWork (evita duplicação)
  - [X] ✍️ **Reflexão (10min):**
    - Chaining: WorkA → WorkB → WorkC
    - UniqueWork: evita 2 syncs simultâneos

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia "WorkManager — Expedited Work"
    - Link: https://developer.android.com/topic/libraries/architecture/workmanager/how-to/define-work#expedited
  - [X] 💻 **Prática (30min):**
    - Implemente expedited work para sync urgente
    - Implemente PeriodicWorkRequest (a cada 15min)
    - Configure: foreground trigger com Lifecycle observer
  - [X] ✍️ **Reflexão (10min):**
    - Expedited: alta prioridade, roda imediatamente
    - Periodic: mínimo 15min

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "WorkManager — Observing Work"
    - Link: https://developer.android.com/topic/libraries/architecture/workmanager/how-to/observe-work
  - [X] 💻 **Prática (30min):**
    - Observe WorkInfo no ViewModel
    - Mostre na UI: "Sincronizando...", "Sincronizado ✓", "Erro ✗"
    - Adicione progress reporting
  - [X] ✍️ **Reflexão (10min):**
    - WorkInfo.State: ENQUEUED, RUNNING, SUCCEEDED, FAILED
    - setProgress() para progresso granular

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia anotações do mês
  - [X] 💻 **Prática (30min):**
    - Revise arquitetura de sync completa
    - Escreva ADR: "Estratégia de Sincronização do StaffNotes"
  - [X] ✍️ **Reflexão (10min):** RESUMO MÊS 2
    - ✅ Room avançado: migrations, FTS, relations
    - ✅ Offline-first: SSOT, stale-while-revalidate
    - ✅ Conflict resolution + optimistic updates
    - ✅ WorkManager: chaining, expedited, periodic
    - ✅ 1 ADR de sync escrita

- [X] ✅ **Semana 4 concluída**
- [X] ✅ **Mês 2 concluído**

---

## 📅 Mês 3 — Mobile System Design

### Semana 1: Framework de Design

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Estude Mobile System Design framework
    - Link: https://github.com/nicklama/mobile-system-design
    - Complementar (livro): https://www.mobilesystemdesign.com
    - Foco: Constraints → HLD → Deep Dives
  - [X] 💻 **Prática (30min):**
    - Pratique no StaffNotes:
      - Functional requirements (criar, editar, buscar, sync)
      - Non-functional (offline, performance, 100K notas)
      - High-level design (diagrama de componentes)
  - [X] ✍️ **Reflexão (10min):**
    - Framework: Requirements → Constraints → HLD → Deep Dive
    - Mobile: sempre começar por constraints

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Continue o guide — Networking layer
  - [X] 💻 **Prática (30min):**
    - Desenhe camada de rede do StaffNotes:
      - API design, pagination, auth
      - Retry, timeout, cache headers
  - [X] ✍️ **Reflexão (10min):**
    - Cursor pagination > offset para mobile
    - Sempre planejar para offline

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Continue — Persistence layer
  - [X] 💻 **Prática (30min):**
    - Desenhe camada de dados:
      - Room vs DataStore vs SharedPreferences
      - Schema design para performance
  - [X] ✍️ **Reflexão (10min):**
    - Room: dados estruturados
    - DataStore: key-value, preferências

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia exemplos de System Design interviews
    - Link: https://github.com/nicklama/mobile-system-design
  - [X] 💻 **Prática (30min):**
    - Design de app de delivery (30min timer)
    - Requirements, constraints, HLD, deep dive
  - [X] ✍️ **Reflexão (10min):**
    - Onde travei? O que preciso melhorar?

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia anotações, revise framework
  - [X] 💻 **Prática (30min):**
    - Refine design do delivery app
    - Documente trade-offs
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Framework de MSD aprendido
    - ✅ 2 designs praticados

- [X] ✅ **Semana 1 concluída**

---

### Semana 2: Design — App de Chat

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "chat app mobile system design"
    - Link: https://github.com/nicklama/mobile-system-design
    - Foco: data model, ordering, IDs
  - [X] 💻 **Prática (30min):**
    - Desenhe: Message(id, senderId, content, timestamp, status)
    - Como garantir ordem? Como lidar com falha no envio?
  - [X] ✍️ **Reflexão (10min):**
    - Status: pending → sent → delivered → read
    - ID local vs ID do servidor

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "WebSocket vs SSE vs polling mobile"
  - [X] 💻 **Prática (30min):**
    - Desenhe conexão real-time + presença (typing)
  - [X] ✍️ **Reflexão (10min):**
    - WebSocket: melhor para chat (bidirecional)
    - Custo: conexão aberta = bateria

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia "FCM Architecture"
    - Link: https://firebase.google.com/docs/cloud-messaging/concept-options
  - [X] 💻 **Prática (30min):**
    - Desenhe: notification system end-to-end
    - Data message vs Notification message
  - [X] ✍️ **Reflexão (10min):**
    - Data message: app processa (melhor para chat)
    - FCM não garante entrega

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "image upload mobile architecture"
  - [X] 💻 **Prática (30min):**
    - Desenhe: upload de mídia
      - Compressão, chunks, thumbnails, cache
  - [X] ✍️ **Reflexão (10min):**
    - Thumbnail primeiro, full-res sob demanda
    - Presigned URLs para upload

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia anotações do chat design
  - [X] 💻 **Prática (30min):**
    - Escreva Design Doc completa do Chat App
    - Template: Problem → Goals → Non-goals → Design → Risks
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Chat app design completo
    - ✅ Design Doc escrita

- [X] ✅ **Semana 2 concluída**

---

### Semana 3: Design — App de E-commerce

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "e-commerce mobile system design catalog"
  - [X] 💻 **Prática (30min):**
    - Desenhe: catálogo com Paging, busca com debounce, filtros
  - [X] ✍️ **Reflexão (10min):**
    - Cursor pagination > offset para dados dinâmicos
    - Debounce: 300ms antes de buscar

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "shopping cart mobile architecture"
  - [X] 💻 **Prática (30min):**
    - Desenhe: carrinho local + sync, conflitos, price changes
  - [X] ✍️ **Reflexão (10min):**
    - Cart híbrido: local para UX rápida
    - Sempre validar preços antes de checkout

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "mobile checkout payment architecture"
  - [X] 💻 **Prática (30min):**
    - Desenhe: checkout com idempotency keys, deep links
  - [X] ✍️ **Reflexão (10min):**
    - Checkout NUNCA processa no device
    - Idempotency evita double charge

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Deep Links on Android"
    - Link: https://developer.android.com/training/app-links/deep-linking
  - [X] 💻 **Prática (30min):**
    - Desenhe: deep links do e-commerce
    - `myapp://product/{id}`, `myapp://cart`, `myapp://order/{id}`
  - [X] ✍️ **Reflexão (10min):**
    - Deep links: essenciais para marketing, push
    - Sempre ter fallback

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia anotações
  - [X] 💻 **Pr��tica (30min):**
    - Escreva Design Doc do E-commerce
    - Compare com Design Doc do Chat
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ E-commerce design completo
    - ✅ 2 Design Docs escritas

- [X] ✅ **Semana 3 concluída**

---

### Semana 4: Sua Primeira RFC

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Estude RFCs de empresas:
    - Uber: https://www.uber.com/en-US/blog/engineering/
    - Figma: https://www.figma.com/blog/section/engineering/
  - [X] 💻 **Prática (30min):**
    - Escolha tema da RFC (ex: "Estratégia MVI do StaffNotes")
    - Escreva: Context + Problem Statement
  - [X] ✍️ **Reflexão (10min):**
    - RFC = qualquer dev entende o problema sem contexto prévio

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Estude templates de ADR:
    - Link: https://adr.github.io
    - Link: https://github.com/joelparkerhenderson/architecture-decision-record
  - [X] 💻 **Prática (30min):**
    - Escreva: Options Considered (3 opções com trade-offs)
  - [X] ✍️ **Reflexão (10min):**
    - Sempre listar pelo menos 2-3 opções
    - Incluir opção "não fazer nada"

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Releia exemplos de "Decision" e "Consequences"
  - [X] 💻 **Prática (30min):**
    - Escreva: Decision + Consequences + Rollout Plan
  - [X] ✍️ **Reflexão (10min):**
    - Consequences: positivas E negativas
    - Rollout: sempre gradual

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Pesquise "How to write a good RFC"
    - Link: https://buriti.ca/6-lessons-i-learned-while-implementing-technical-rfcs-as-a-management-tool-34687dbf46cb
  - [X] 💻 **Prática (30min):**
    - Revise RFC completa
    - Adicione diagramas (Mermaid ou draw.io)
    - Peça para alguém ler
  - [X] ✍️ **Reflexão (10min):**
    - Diagrama vale mais que 1000 palavras

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Releia TODAS as anotações do Q1
  - [X] 💻 **Prática (30min):**
    - Finalize RFC com feedback recebido
    - Rode StaffNotes completo
  - [X] ✍️ **Reflexão (10min):** RESUMO MÊS 3 + Q1
    - ✅ 4 System Designs (StaffNotes, Delivery, Chat, E-commerce)
    - ✅ 2 Design Docs + 1 RFC completa
    - ✅ StaffNotes: modularizado, MVI, offline-first, sync

- [X] ✅ **Semana 4 concluída**
- [X] ✅ **Mês 3 concluído**
- [X] ✅ **Q1 CONCLUÍDO 🎉**

---

### 📊 Checklist de Entregáveis Q1:

- [X] StaffNotes modularizado (:app, :core:*, :feature:*)
- [X] MVI + UDF implementado
- [X] Room com migrations, FTS, relationships
- [X] Offline-first com sync, SSOT, optimistic updates
- [X] WorkManager com chaining e monitoring
- [X] Convention Plugins + Version Catalog
- [X] Build otimizado com métricas
- [X] 4 Mobile System Designs praticados
- [X] 2 Design Docs escritas (Chat + E-commerce)
- [X] 1 RFC completa revisada por peer
- [X] 2+ ADRs escritas
- [X] LEARNING_LOG.md atualizado semanalmente

---

# 📅 Q2 — PERFORMANCE, QUALIDADE + IA (Meses 4-6)

---

## 📅 Mês 4 — Performance & Compose Avançado

### Semana 1: Compose Performance

- [X] **🟢 Segunda-feira**
  - [X] 📖 **Teoria (20min):** Leia "Jetpack Compose Performance"
    - Link: https://developer.android.com/develop/ui/compose/performance
    - Foco: recomposition, stability, skippable composables
  - [X] 💻 **Prática (30min):**
    - Ative "Show recomposition counts" no Layout Inspector
    - Identifique composables recompondo desnecessariamente no StaffNotes
    - Anote quais e por quê
  - [X] ✍️ **Reflexão (10min):**
    - Recomposition é normal — problema é recomposição excessiva
    - Composable skippável = todos params estáveis

- [X] **🟢 Terça-feira**
  - [X] 📖 **Teoria (20min):** Leia "Compose Stability"
    - Link: https://developer.android.com/develop/ui/compose/performance/stability
    - Foco: @Stable, @Immutable, unstable classes
  - [X] 💻 **Prática (30min):**
    - Adicione Compose Compiler Report ao build
    - Analise relatório: quais classes são instáveis?
    - Marque data classes do :core:model com @Immutable
  - [X] ✍️ **Reflexão (10min):**
    - List<T> é instável — use ImmutableList (kotlinx.collections)
    - @Immutable = promessa ao compilador

- [X] **🟢 Quarta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Lazy layout performance"
    - Link: https://developer.android.com/develop/ui/compose/lists#lazy-performance
  - [X] 💻 **Prática (30min):**
    - Adicione `key {}` em todos os LazyColumn/LazyRow
    - Use `contentType` para itens heterogêneos
    - Meça: scroll FPS antes e depois com Perfetto
  - [X] ✍️ **Reflexão (10min):**
    - key = evita reordenar itens incorretamente
    - contentType = recicla views do mesmo tipo

- [X] **🟢 Quinta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Defer reads as long as possible"
    - Link: https://developer.android.com/develop/ui/compose/performance/bestpractices#defer-reads
  - [X] 💻 **Prática (30min):**
    - Refatore animações: passar lambda em vez de State
    - Use `derivedStateOf` onde aplicável
  - [X] ✍️ **Reflexão (10min):**
    - derivedStateOf: recalcula só quando dependência muda
    - Lambda defer: lê state na fase de draw, não composition

- [X] **🟢 Sexta-feira**
  - [X] 📖 **Teoria (20min):** Leia "Baseline Profiles"
    - Link: https://developer.android.com/topic/performance/baselineprofiles
  - [X] 💻 **Prática (30min):**
    - Gere Baseline Profile para StaffNotes
    - Meça startup time: antes e depois
  - [X] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Recomposition analisada e reduzida
    - ✅ Stability configurada
    - ✅ Baseline Profile gerado

- [X] ✅ **Semana 1 concluída**

---

### Semana 2: App Startup & Memory

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "App Startup"
    - Link: https://developer.android.com/topic/libraries/app-startup
    - Foco: cold start, warm start, hot start
  - [ ] 💻 **Prática (30min):**
    - Meça cold start com `adb shell am start-activity -W`
    - Identifique o que inicializa no Application.onCreate()
    - Mova inicializações pesadas para lazy/background
  - [ ] ✍️ **Reflexão (10min):**
    - Cold start: process criado do zero
    - Objetivo: <500ms até primeiro frame

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Memory overview"
    - Link: https://developer.android.com/topic/performance/memory-overview
  - [ ] 💻 **Prática (30min):**
    - Rode Memory Profiler no StaffNotes
    - Identifique leaks com LeakCanary
    - Adicione LeakCanary ao projeto
  - [ ] ✍️ **Reflexão (10min):**
    - Memory leak = objeto não coletado pelo GC
    - Context leak: o mais comum no Android

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Reduce APK size"
    - Link: https://developer.android.com/topic/performance/reduce-apk-size
  - [ ] 💻 **Prática (30min):**
    - Ative R8/ProGuard
    - Analise APK com APK Analyzer
    - Remova resources não usados
  - [ ] ✍️ **Reflexão (10min):**
    - R8: shrinking + obfuscation + optimization
    - Menor APK = menor install time

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Battery optimization"
    - Link: https://developer.android.com/topic/performance/power
  - [ ] 💻 **Prática (30min):**
    - Revise WorkManager constraints (battery not low)
    - Revise wake locks e alarmes
    - Perfil de bateria com Battery Historian
  - [ ] ✍️ **Reflexão (10min):**
    - Bateria: o recurso mais crítico no mobile
    - Regra: menos wake-ups = mais bateria

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia todas as anotações da semana
  - [ ] 💻 **Prática (30min):**
    - Documente métricas: startup time, memory, APK size
    - Crie dashboard de performance no LEARNING_LOG
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Startup time medido e otimizado
    - ✅ LeakCanary configurado
    - ✅ APK size reduzido

- [ ] ✅ **Semana 2 concluída**

---

### Semana 3: Testing Avançado

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Testing in Compose"
    - Link: https://developer.android.com/develop/ui/compose/testing
    - Foco: semantics, finders, assertions
  - [ ] 💻 **Prática (30min):**
    - Escreva testes de UI para NoteListScreen
    - Teste: lista vazia, lista com itens, loading
    - Use `createComposeRule()`
  - [ ] ✍️ **Reflexão (10min):**
    - Compose testing = semantics tree (acessibilidade)
    - onNode(hasText("...")) vs onNodeWithText("...")

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Turbine — Flow testing"
    - Link: https://github.com/cashapp/turbine
  - [ ] 💻 **Prática (30min):**
    - Adicione Turbine
    - Teste Flows do ViewModel com Turbine
    - Teste: emit, loading, success, error
  - [ ] ✍️ **Reflexão (10min):**
    - Turbine simplifica teste de Flows
    - awaitItem(), awaitComplete(), awaitError()

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Fake vs Mock"
    - Foco: quando usar cada abordagem
  - [ ] 💻 **Prática (30min):**
    - Crie FakeNoteRepository implementando a interface
    - Substitua mocks por fakes nos testes
    - Compare: legibilidade, manutenção
  - [ ] ✍️ **Reflexão (10min):**
    - Fake: implementação real simplificada
    - Mock: objeto programado para respostas
    - Prefira fakes para repositórios

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Screenshot Testing"
    - Link: https://developer.android.com/studio/test/advanced-test-setup
  - [ ] 💻 **Prática (30min):**
    - Configure Paparazzi ou Roborazzi
    - Gere screenshots dos composables principais
    - Integre no CI (simule localmente)
  - [ ] ✍️ **Reflexão (10min):**
    - Screenshot test: evita regressão visual
    - Roda sem device (Robolectric)

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações da semana
  - [ ] 💻 **Prática (30min):**
    - Coverage report com Kover
    - Objetivo: >80% no :core:data
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Compose UI tests
    - ✅ Flow testing com Turbine
    - ✅ Screenshot tests configurados

- [ ] ✅ **Semana 3 concluída**

---

### Semana 4: Accessibility + Internationalização

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Accessibility in Compose"
    - Link: https://developer.android.com/develop/ui/compose/accessibility
  - [ ] 💻 **Prática (30min):**
    - Rode Accessibility Scanner no StaffNotes
    - Adicione contentDescription em imagens/ícones
    - Teste com TalkBack ligado
  - [ ] ✍️ **Reflexão (10min):**
    - Acessibilidade = 15% dos usuários dependem dela
    - contentDescription é obrigatório em ícones sem texto

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Support different screen sizes"
    - Link: https://developer.android.com/develop/ui/compose/adaptive
  - [ ] 💻 **Prática (30min):**
    - Adapte NoteListScreen para tablet (list-detail layout)
    - Use WindowSizeClass
  - [ ] ✍️ **Reflexão (10min):**
    - Compact/Medium/Expanded
    - List-detail: padrão para tablets

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Localization"
    - Link: https://developer.android.com/guide/topics/resources/localization
  - [ ] 💻 **Prática (30min):**
    - Mova todas as strings para strings.xml
    - Crie strings-pt.xml (português)
    - Teste com locale pt-BR
  - [ ] ✍️ **Reflexão (10min):**
    - Nunca hardcode string na UI
    - Pseudolocale: testa overflow de texto

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Dark theme"
    - Link: https://developer.android.com/develop/ui/compose/designsystems/material3
  - [ ] 💻 **Prática (30min):**
    - Garanta dark mode funcional no StaffNotes
    - Revise cores: sem hardcode, tudo via MaterialTheme
  - [ ] ✍️ **Reflexão (10min):**
    - Dynamic color: Android 12+
    - Cores = MaterialTheme.colorScheme.*

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações do mês
  - [ ] 💻 **Prática (30min):**
    - Revise StaffNotes completo
    - ADR: "Estratégia de Testes do StaffNotes"
  - [ ] ✍️ **Reflexão (10min):** RESUMO MÊS 4
    - ✅ Compose performance otimizada
    - ✅ Startup + memory + APK
    - ✅ Testing avançado com Turbine + Screenshots
    - ✅ Acessibilidade + i18n + dark mode

- [ ] ✅ **Semana 4 concluída**
- [ ] ✅ **Mês 4 concluído**

---

## 📅 Mês 5 — Inteligência Artificial no Android

### Semana 1: ML Kit + On-Device AI

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "ML Kit overview"
    - Link: https://developers.google.com/ml-kit
    - Foco: o que roda on-device, latência, privacidade
  - [ ] 💻 **Prática (30min):**
    - Adicione ML Kit ao StaffNotes
    - Implemente Smart Text Recognition: digitalize nota por foto
  - [ ] ✍️ **Reflexão (10min):**
    - On-device: privacidade, offline, latência baixa
    - ML Kit: APIs prontas sem treinar modelo

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Gemini Nano on-device"
    - Link: https://developer.android.com/ai/gemini-nano
  - [ ] 💻 **Prática (30min):**
    - Configure AICore / Gemini Nano (Android 14+)
    - Implemente: "Resumir esta nota" com Gemini Nano
  - [ ] ✍️ **Reflexão (10min):**
    - Gemini Nano: LLM que roda no device
    - Sem custo de API, sem dados na nuvem

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Firebase AI Logic (Vertex AI)"
    - Link: https://firebase.google.com/docs/vertex-ai
  - [ ] 💻 **Prática (30min):**
    - Configure Firebase AI no projeto
    - Implemente: auto-tagging de notas com Gemini
    - Prompt: "Dado este texto, sugira 3 tags: {content}"
  - [ ] ✍️ **Reflexão (10min):**
    - Cloud AI: mais poderoso, custa por token
    - Combinar: Nano para offline, Gemini para complexo

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "LangChain4j Android" ou "Kotlin AI agents"
  - [ ] 💻 **Prática (30min):**
    - Implemente busca semântica nas notas
    - Embeddings: transforme notas em vetores
    - Encontre notas similares sem match exato
  - [ ] ✍️ **Reflexão (10min):**
    - Busca semântica > busca por keyword
    - Embeddings: representação numérica do significado

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "AI UX patterns mobile"
  - [ ] 💻 **Prática (30min):**
    - Implemente: streaming de resposta (typewriter effect)
    - Loading states para AI (diferente de network)
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ ML Kit integrado
    - ✅ Gemini Nano on-device
    - ✅ Firebase AI (cloud)
    - ✅ Busca semântica

- [ ] ✅ **Semana 1 concluída**

---

### Semana 2: AI Features Avançadas

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "prompt engineering best practices"
  - [ ] 💻 **Prática (30min):**
    - Implemente: assistente de escrita (melhorar nota)
    - Prompts: system prompt + user prompt + context
    - Itere prompts até qualidade boa
  - [ ] ✍️ **Reflexão (10min):**
    - System prompt define o "personagem" da IA
    - Few-shot: exemplos no prompt melhoram output

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia sobre RAG (Retrieval-Augmented Generation)
  - [ ] 💻 **Prática (30min):**
    - Implemente: "pergunte sobre suas notas"
    - Busca semântica → contexto → Gemini responde
  - [ ] ✍️ **Reflexão (10min):**
    - RAG = busca relevante + LLM = respostas com seus dados
    - Evita hallucination com contexto real

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "AI error handling, fallbacks"
  - [ ] 💻 **Prática (30min):**
    - Implemente fallbacks: Nano falha → Gemini cloud
    - Rate limiting, custo por usuário
    - Cache de respostas similares
  - [ ] ✍️ **Reflexão (10min):**
    - AI pode falhar: sempre ter fallback
    - Cache: economiza tokens e latência

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "responsible AI mobile"
  - [ ] 💻 **Prática (30min):**
    - Adicione: disclaimer "Conteúdo gerado por IA"
    - Opção de desativar features de IA
    - Não envie dados sensíveis para cloud AI sem consentimento
  - [ ] ✍️ **Reflexão (10min):**
    - IA responsável: transparência + controle do usuário
    - LGPD/GDPR: dados pessoais na IA requer consentimento

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações da semana
  - [ ] 💻 **Prática (30min):**
    - Escreva Design Doc: "AI Features no StaffNotes"
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Prompt engineering
    - ✅ RAG implementado
    - ✅ Fallbacks e responsible AI

- [ ] ✅ **Semana 2 concluída**

---

### Semana 3: CI/CD + Code Quality

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "GitHub Actions for Android"
    - Link: https://docs.github.com/en/actions
  - [ ] 💻 **Prática (30min):**
    - Crie workflow: build + lint + unit tests no PR
    - `.github/workflows/ci.yml`
  - [ ] ✍️ **Reflexão (10min):**
    - CI: toda mudança deve passar nos checks
    - Falhou o CI = não mergeia

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Detekt — Static Analysis"
    - Link: https://detekt.dev
  - [ ] 💻 **Prática (30min):**
    - Configure Detekt no projeto
    - Adicione regras customizadas (sem magic numbers, max complexity)
    - Integre no CI
  - [ ] ✍️ **Reflexão (10min):**
    - Static analysis: encontra bugs sem rodar código
    - Detekt + ktlint = padrão Android

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "Android distribution — Play Store"
    - Link: https://developer.android.com/distribute
  - [ ] 💻 **Prática (30min):**
    - Configure Fastlane ou Gradle Play Publisher
    - Automatize geração de APK/AAB assinado
  - [ ] ✍️ **Reflexão (10min):**
    - AAB > APK para Play Store
    - Signing: nunca commitar keystore

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Firebase Crashlytics"
    - Link: https://firebase.google.com/docs/crashlytics
  - [ ] 💻 **Prática (30min):**
    - Configure Crashlytics no StaffNotes
    - Configure Firebase Performance Monitoring
    - Adicione custom traces nas operações críticas
  - [ ] ✍️ **Reflexão (10min):**
    - Crashlytics: crash rate < 0.5% = padrão Google
    - Custom traces: mede o que importa pra você

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações do mês
  - [ ] 💻 **Prática (30min):**
    - Revise StaffNotes completo
    - Pipeline completo: code → CI → lint → tests → build → deploy
  - [ ] ✍️ **Reflexão (10min):** RESUMO MÊS 5
    - ✅ AI features: ML Kit, Gemini, RAG
    - ✅ CI/CD configurado
    - ✅ Detekt + Crashlytics

- [ ] ✅ **Semana 3 concluída**

---

### Semana 4: Code Review + Mentoria Técnica

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "How to do good code review"
    - Link: https://google.github.io/eng-practices/review/
  - [ ] 💻 **Prática (30min):**
    - Revise uma PR do StaffNotes como se fosse de outro dev
    - Escreva comentários construtivos
  - [ ] ✍️ **Reflexão (10min):**
    - Code review: sobre o código, nunca sobre a pessoa
    - Aprove parcialmente com sugestões

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "Staff Engineer skills"
    - Link: https://staffeng.com/guides/
  - [ ] 💻 **Prática (30min):**
    - Escreva guia técnico: "Como criar feature no StaffNotes"
    - Onboarding doc para novo dev
  - [ ] ✍️ **Reflexão (10min):**
    - Staff = multiplica outros devs, não só coda
    - Documentação = alavancagem

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "Technical debt management"
  - [ ] 💻 **Prática (30min):**
    - Mapeie tech debt do StaffNotes
    - Priorize: impacto vs esforço
    - Escreva proposta de refactor (mini-RFC)
  - [ ] ✍️ **Reflexão (10min):**
    - Tech debt não é sempre ruim — é trade-off consciente
    - ADR: decisões que causaram debt

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Releia todos os docs/ADRs criados
  - [ ] 💻 **Prática (30min):**
    - Apresente StaffNotes como se fosse em uma entrevista Staff
    - Grave (vídeo ou áudio) 5min de apresentação
  - [ ] ✍️ **Reflexão (10min):**
    - Consegui explicar claramente as decisões?
    - O que ainda não consigo explicar bem?

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia TODAS as anotações do Q2
  - [ ] 💻 **Prática (30min):**
    - Finalize pendências do Q2
    - Escreva ADR: "AI Strategy no StaffNotes"
  - [ ] ✍️ **Reflexão (10min):** RESUMO MÊS 5 + Q2
    - ✅ Performance: Compose, startup, memory
    - ✅ AI: on-device + cloud + RAG
    - ✅ CI/CD + quality gates
    - ✅ Staff skills: docs, code review, mentoria

- [ ] ✅ **Semana 4 concluída**
- [ ] ✅ **Mês 5 concluído**

---

## 📅 Mês 6 — Revisão Q2 + Preparação Q3

### Semana 1–2: Consolidação

- [ ] **🟢 Todo dia (1h):**
  - Revise e consolide tópicos fracos do Q2
  - Refatore partes do StaffNotes que ficaram pendentes
  - Pratique 1 Mobile System Design por dia

### Semana 3–4: Preparação KMP

- [ ] **🟢 Todo dia (1h):**
  - Leia "Kotlin Multiplatform overview"
    - Link: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
  - Estude: o que compartilhar, o que não compartilhar
  - Crie projeto KMP sample (Android + iOS)

- [ ] ✅ **Mês 6 concluído**
- [ ] ✅ **Q2 CONCLUÍDO 🎉**

---

### 📊 Checklist de Entregáveis Q2:

- [ ] Compose performance analisada e otimizada
- [ ] Baseline Profile gerado
- [ ] LeakCanary configurado, 0 leaks
- [ ] Testing: Compose UI + Turbine + Screenshots
- [ ] ML Kit integrado (OCR)
- [ ] Gemini Nano on-device funcional
- [ ] RAG implementado (busca semântica)
- [ ] CI/CD com GitHub Actions
- [ ] Detekt + Crashlytics configurados
- [ ] 1 ADR de AI strategy
- [ ] LEARNING_LOG.md atualizado semanalmente

---

# 📅 Q3 — EXPANSÃO: KMP + BACK-END/AWS (Meses 7-9)

---

## 📅 Mês 7 — Kotlin Multiplatform

### Semana 1: KMP Fundamentos

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "KMP Get Started"
    - Link: https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-getting-started.html
    - Foco: shared module, expect/actual, targets
  - [ ] 💻 **Prática (30min):**
    - Crie projeto KMP: Android + iOS targets
    - Compartilhe: data class Note + NoteRepository interface
  - [ ] ✍️ **Reflexão (10min):**
    - KMP ≠ Flutter: UI nativa, lógica compartilhada
    - expect/actual = abstração de plataforma

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Sharing data layer"
    - Link: https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-ktor-sqldelight.html
  - [ ] 💻 **Prática (30min):**
    - Adicione SQLDelight (DB multiplatform)
    - Migre NoteEntity do Room para SQLDelight
    - Observe: mesma query roda em Android e iOS
  - [ ] ✍️ **Reflexão (10min):**
    - SQLDelight: gera type-safe queries
    - Room = Android only, SQLDelight = KMP

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Ktor Client"
    - Link: https://ktor.io/docs/getting-started-ktor-client.html
  - [ ] 💻 **Prática (30min):**
    - Migre Retrofit para Ktor Client
    - Configure engines: OkHttp (Android), Darwin (iOS)
  - [ ] ✍️ **Reflexão (10min):**
    - Ktor Client = Retrofit KMP
    - Engine = implementação de plataforma

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Koin Multiplatform"
    - Link: https://insert-koin.io/docs/reference/koin-mp/kmp
  - [ ] 💻 **Prática (30min):**
    - Migre Hilt (Android-only) → Koin (KMP) no shared module
    - Hilt permanece no :app Android
    - Shared module usa Koin
  - [ ] ✍️ **Reflexão (10min):**
    - Hilt = Android only (processa annotation)
    - Koin = runtime DI, funciona em KMP

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações da semana
  - [ ] 💻 **Prática (30min):**
    - Rode o shared module em Android e iOS Simulator
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ KMP project setup
    - ✅ SQLDelight + Ktor + Koin no shared

- [ ] ✅ **Semana 1 concluída**

---

### Semana 2–4: KMP — Compartilhando Business Logic

- [ ] **🟢 Semana 2:**
  - Compartilhe: UseCases, Repository, Sync logic
  - ViewModel multiplataforma com KMP-NativeCoroutines ou Compose Multiplatform

- [ ] **🟢 Semana 3:**
  - Compose Multiplatform: tela simples (Lista de notas) rodando em Android + Desktop
  - Explore: Kotlin/JS para web

- [ ] **🟢 Semana 4:**
  - Escreva ADR: "Estratégia KMP no StaffNotes"
  - Documente: o que vale a pena compartilhar, o que não vale

- [ ] ✅ **Mês 7 concluído**

---

## 📅 Mês 8 — Back-end com Kotlin (Ktor Server) + AWS

### Semana 1: Ktor Server

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Ktor Server Getting Started"
    - Link: https://ktor.io/docs/server-get-started.html
  - [ ] 💻 **Prática (30min):**
    - Crie servidor Ktor: GET /notes, POST /notes, PUT /notes/{id}
    - Use o mesmo :core:model do StaffNotes
  - [ ] ✍️ **Reflexão (10min):**
    - Ktor server: mesmo Kotlin, mesmo ecossistema
    - Full-stack Kotlin: Android + Server + iOS

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Leia sobre autenticação JWT
  - [ ] 💻 **Prática (30min):**
    - Adicione JWT authentication ao servidor
    - Android app: login → token → Authorization header
  - [ ] ✍️ **Reflexão (10min):**
    - JWT: stateless auth
    - Refresh token: evitar logout frequente

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Exposed — Kotlin ORM"
    - Link: https://github.com/JetBrains/Exposed
  - [ ] 💻 **Prática (30min):**
    - Configure PostgreSQL + Exposed no servidor Ktor
    - CRUD de notas persistido no Postgres
  - [ ] ✍️ **Reflexão (10min):**
    - Exposed: ORM type-safe em Kotlin
    - PostgreSQL: production-grade DB

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "WebSocket com Ktor"
    - Link: https://ktor.io/docs/server-websockets.html
  - [ ] 💻 **Prática (30min):**
    - Adicione endpoint WebSocket: /notes/live
    - Android: conecta e recebe updates em tempo real
  - [ ] ✍️ **Reflexão (10min):**
    - WebSocket bidirecional: push sem polling
    - Real-time sync = WebSocket ou SSE

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações da semana
  - [ ] 💻 **Prática (30min):**
    - Deploy local com Docker
    - StaffNotes Android conectando ao servidor local
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ API REST com Ktor Server
    - ✅ JWT auth
    - ✅ PostgreSQL + WebSocket

- [ ] ✅ **Semana 1 concluída**

---

### Semana 2–3: AWS Fundamentos

- [ ] **🟢 Semana 2:**
  - [ ] 📖 Leia "AWS para desenvolvedores mobile"
  - [ ] 💻 Configure: EC2 ou Elastic Beanstalk para deploy do Ktor
  - [ ] 💻 Configure: RDS PostgreSQL na AWS
  - [ ] 💻 Configure: S3 para upload de imagens/mídia das notas

- [ ] **🟢 Semana 3:**
  - [ ] 📖 Leia "AWS Lambda + API Gateway"
  - [ ] 💻 Experimente: serverless function em Kotlin (AWS Lambda + Ktor handler)
  - [ ] 💻 Configure: CloudWatch para logs e alertas

### Semana 4: Integração Completa

- [ ] **🟢 Semana 4:**
  - StaffNotes Android → AWS API (Ktor) → RDS PostgreSQL
  - Upload de foto da nota → S3
  - Escreva ADR: "Back-end Strategy do StaffNotes"

- [ ] ✅ **Mês 8 concluído**

---

## 📅 Mês 9 — Revisão Q3 + Preparação Q4

### Semana 1–2: Consolidação KMP + AWS

- [ ] Consolide tópicos fracos do Q3
- [ ] Refatore o que ficou pendente
- [ ] Pratique: 1 System Design por dia com KMP/Cloud focus

### Semana 3–4: Preparação Q4

- [ ] Defina o "projeto final" do Q4
- [ ] Revise todo o LEARNING_LOG
- [ ] Identifique gaps para entrevistas Staff

- [ ] ✅ **Mês 9 concluído**
- [ ] ✅ **Q3 CONCLUÍDO 🎉**

---

### 📊 Checklist de Entregáveis Q3:

- [ ] KMP: shared module com SQLDelight + Ktor + Koin
- [ ] Compose Multiplatform: 1 tela rodando Android + Desktop
- [ ] Ktor Server: API REST completa com JWT
- [ ] PostgreSQL + Exposed no servidor
- [ ] WebSocket real-time sync
- [ ] Deploy na AWS (EC2 + RDS + S3)
- [ ] 1 ADR KMP strategy
- [ ] 1 ADR back-end strategy
- [ ] LEARNING_LOG.md atualizado semanalmente

---

# 📅 Q4 — IMPACTO & CONSOLIDAÇÃO (Meses 10-12)

---

## 📅 Mês 10 — Open Source + Contribuição

### Semana 1–2: Contribuindo com OSS

- [ ] **🟢 Semana 1:**
  - [ ] 📖 Leia "How to contribute to open source"
    - Link: https://opensource.guide/how-to-contribute/
  - [ ] 💻 Escolha 1 biblioteca Android que você usa
  - [ ] 💻 Abra 1 issue ou 1 PR (bug fix ou docs)
  - [ ] ✍️ Reflexão: O que aprendi lendo código de outros?

- [ ] **🟢 Semana 2:**
  - [ ] 💻 Revise e itere PR até merge
  - [ ] 💻 Documente o processo no LEARNING_LOG
  - [ ] ✍️ Reflexão: OSS = reputação + aprendizado acelerado

### Semana 3–4: Publish da sua própria lib

- [ ] **🟢 Semana 3:**
  - [ ] 💻 Extraia algo reusável do StaffNotes como biblioteca
  - [ ] 💻 Publique no Maven Central ou JitPack
  - [ ] ✍️ Reflexão: Publish = compromisso com qualidade

- [ ] **🟢 Semana 4:**
  - [ ] 💻 Escreva README completo com exemplos
  - [ ] 💻 Configure CI para a lib
  - [ ] ✍️ Reflexão: RESUMO MÊS 10
    - ✅ 1 contribuição OSS
    - ✅ 1 lib publicada

- [ ] ✅ **Mês 10 concluído**

---

## 📅 Mês 11 — Liderança Técnica + Impacto

### Semana 1: Apresentações Técnicas

- [ ] **🟢 Semana 1:**
  - [ ] 📖 Leia "How to give a great tech talk"
  - [ ] 💻 Prepare talk de 20min: "Arquitetura do StaffNotes"
  - [ ] 💻 Apresente para alguém (colega, comunidade)
  - [ ] ✍️ Reflexão: Consegui explicar para não-Android?

### Semana 2: Blog Post Técnico

- [ ] **🟢 Semana 2:**
  - [ ] 💻 Escreva artigo técnico no Medium/dev.to
  - [ ] Tema sugerido: "Offline-first Android com Room + WorkManager"
  - [ ] ✍️ Reflexão: Escrever = solidifica o aprendizado

### Semana 3–4: Mentoria + Revisão de Código

- [ ] **🟢 Semana 3–4:**
  - [ ] Faça 2 code reviews de colegas com feedback detalhado
  - [ ] Escreva guia: "Boas práticas de Architecture no Android"
  - [ ] ✍️ Reflexão: RESUMO MÊS 11
    - ✅ 1 tech talk apresentado
    - ✅ 1 artigo publicado
    - ✅ 2+ code reviews feitos

- [ ] ✅ **Mês 11 concluído**

---

## 📅 Mês 12 — Consolidação Final + Próximos Passos

### Semana 1–2: Revisão Geral

- [ ] **🟢 Semana 1:**
  - Releia TODOS os LEARNING_LOGs
  - Identifique os 3 tópicos mais fracos
  - Dedique semana inteira a esses 3 tópicos

- [ ] **🟢 Semana 2:**
  - Pratique 5 System Designs em 5 dias (timer 45min cada)
  - Grave e revise suas explicações

### Semana 3: Simulação de Entrevistas

- [ ] **🟢 Semana 3:**
  - [ ] Dia 1–2: System Design interviews (mock com colega)
  - [ ] Dia 3: Coding challenge (Kotlin idiomático)
  - [ ] Dia 4: Behavioral questions (STAR method)
  - [ ] Dia 5: Revisão do StaffNotes — pronto para apresentar

### Semana 4: Próximos 12 Meses

- [ ] **🟢 Semana 4:**
  - Escreva plano de estudos para o próximo ano
  - Identifique: o que mudou na indústria?
  - Defina: qual o próximo nível de impacto?
  - ✍️ **Reflexão Final:** RESUMO Q4 + ANO 1
    - ✅ 12 meses de estudos consistentes
    - ✅ StaffNotes: Android + KMP + Back-end + AI
    - ✅ OSS contributions
    - ✅ Tech talks + artigos publicados
    - ✅ Pronto para entrevistas Staff Engineer

- [ ] ✅ **Semana 4 concluída**
- [ ] ✅ **Mês 12 concluído**
- [ ] ✅ **Q4 CONCLUÍDO 🎉**
- [ ] ✅ **ANO 1 CONCLUÍDO 🏆**

---

### 📊 Checklist de Entregáveis Q4:

- [ ] 1 contribuição aceita em projeto OSS
- [ ] 1 biblioteca própria publicada (Maven/JitPack)
- [ ] 1 tech talk apresentado
- [ ] 1 artigo técnico publicado
- [ ] StaffNotes completo: Android + KMP + Ktor Server + AWS + AI
- [ ] Mock interviews praticadas
- [ ] Plano de estudos Ano 2 escrito
- [ ] LEARNING_LOG.md completo (12 meses)

---

## 🎯 Habilidades ao Final dos 12 Meses:

| Área | Nível |
|------|-------|
| Android Architecture (MVI, Clean Arch) | ⭐⭐⭐⭐⭐ |
| Jetpack Compose (performance, testing) | ⭐⭐⭐⭐⭐ |
| Room + Offline-first | ⭐⭐⭐⭐⭐ |
| WorkManager + Sync | ⭐⭐⭐⭐⭐ |
| Kotlin Multiplatform | ⭐⭐⭐⭐ |
| AI / ML Kit / Gemini | ⭐⭐⭐⭐ |
| Back-end (Ktor Server) | ⭐⭐⭐ |
| AWS (EC2, RDS, S3) | ⭐⭐⭐ |
| Mobile System Design | ⭐⭐⭐⭐⭐ |
| CI/CD + Quality | ⭐⭐⭐⭐ |
```