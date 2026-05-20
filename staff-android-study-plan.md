# 📋 Plano de Estudos — Staff Android Engineer

## 🗓️ Duração: 12 meses | Seg a Sex | 1h por dia
## 📊 Distribuição: 70% Android + IA | 20% KMP | 10% React/Web

## Progresso Geral:

- [ ] Q1 — Fundações (Meses 1-3)
- [ ] Q2 — Performance, Qualidade + IA (Meses 4-6)
- [ ] Q3 — Expansão: KMP + React (Meses 7-9)
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

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Estude Mobile System Design framework
    - Link: https://github.com/nicklama/mobile-system-design
    - Complementar (livro): https://www.mobilesystemdesign.com
    - Foco: Constraints → HLD → Deep Dives
  - [ ] 💻 **Prática (30min):**
    - Pratique no StaffNotes:
      - Functional requirements (criar, editar, buscar, sync)
      - Non-functional (offline, performance, 100K notas)
      - High-level design (diagrama de componentes)
  - [ ] ✍️ **Reflexão (10min):**
    - Framework: Requirements → Constraints → HLD → Deep Dive
    - Mobile: sempre começar por constraints

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Continue o guide — Networking layer
  - [ ] 💻 **Prática (30min):**
    - Desenhe camada de rede do StaffNotes:
      - API design, pagination, auth
      - Retry, timeout, cache headers
  - [ ] ✍️ **Reflexão (10min):**
    - Cursor pagination > offset para mobile
    - Sempre planejar para offline

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Continue — Persistence layer
  - [ ] 💻 **Prática (30min):**
    - Desenhe camada de dados:
      - Room vs DataStore vs SharedPreferences
      - Schema design para performance
  - [ ] ✍️ **Reflexão (10min):**
    - Room: dados estruturados
    - DataStore: key-value, preferências

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia exemplos de System Design interviews
    - Link: https://github.com/nicklama/mobile-system-design
  - [ ] 💻 **Prática (30min):**
    - Design de app de delivery (30min timer)
    - Requirements, constraints, HLD, deep dive
  - [ ] ✍️ **Reflexão (10min):**
    - Onde travei? O que preciso melhorar?

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações, revise framework
  - [ ] 💻 **Prática (30min):**
    - Refine design do delivery app
    - Documente trade-offs
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Framework de MSD aprendido
    - ✅ 2 designs praticados

- [ ] ✅ **Semana 1 concluída**

---

### Semana 2: Design — App de Chat

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "chat app mobile system design"
    - Link: https://github.com/nicklama/mobile-system-design
    - Foco: data model, ordering, IDs
  - [ ] 💻 **Prática (30min):**
    - Desenhe: Message(id, senderId, content, timestamp, status)
    - Como garantir ordem? Como lidar com falha no envio?
  - [ ] ✍️ **Reflexão (10min):**
    - Status: pending → sent → delivered → read
    - ID local vs ID do servidor

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "WebSocket vs SSE vs polling mobile"
  - [ ] 💻 **Prática (30min):**
    - Desenhe conexão real-time + presença (typing)
  - [ ] ✍️ **Reflexão (10min):**
    - WebSocket: melhor para chat (bidirecional)
    - Custo: conexão aberta = bateria

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "FCM Architecture"
    - Link: https://firebase.google.com/docs/cloud-messaging/concept-options
  - [ ] 💻 **Prática (30min):**
    - Desenhe: notification system end-to-end
    - Data message vs Notification message
  - [ ] ✍️ **Reflexão (10min):**
    - Data message: app processa (melhor para chat)
    - FCM não garante entrega

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "image upload mobile architecture"
  - [ ] 💻 **Prática (30min):**
    - Desenhe: upload de mídia
      - Compressão, chunks, thumbnails, cache
  - [ ] ✍️ **Reflexão (10min):**
    - Thumbnail primeiro, full-res sob demanda
    - Presigned URLs para upload

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações do chat design
  - [ ] 💻 **Prática (30min):**
    - Escreva Design Doc completa do Chat App
    - Template: Problem → Goals → Non-goals → Design → Risks
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ Chat app design completo
    - ✅ Design Doc escrita

- [ ] ✅ **Semana 2 concluída**

---

### Semana 3: Design — App de E-commerce

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "e-commerce mobile system design catalog"
  - [ ] 💻 **Prática (30min):**
    - Desenhe: catálogo com Paging, busca com debounce, filtros
  - [ ] ✍️ **Reflexão (10min):**
    - Cursor pagination > offset para dados dinâmicos
    - Debounce: 300ms antes de buscar

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "shopping cart mobile architecture"
  - [ ] 💻 **Prática (30min):**
    - Desenhe: carrinho local + sync, conflitos, price changes
  - [ ] ✍️ **Reflexão (10min):**
    - Cart híbrido: local para UX rápida
    - Sempre validar preços antes de checkout

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "mobile checkout payment architecture"
  - [ ] 💻 **Prática (30min):**
    - Desenhe: checkout com idempotency keys, deep links
  - [ ] ✍️ **Reflexão (10min):**
    - Checkout NUNCA processa no device
    - Idempotency evita double charge

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Leia "Deep Links on Android"
    - Link: https://developer.android.com/training/app-links/deep-linking
  - [ ] 💻 **Prática (30min):**
    - Desenhe: deep links do e-commerce
    - `myapp://product/{id}`, `myapp://cart`, `myapp://order/{id}`
  - [ ] ✍️ **Reflexão (10min):**
    - Deep links: essenciais para marketing, push
    - Sempre ter fallback

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia anotações
  - [ ] 💻 **Prática (30min):**
    - Escreva Design Doc do E-commerce
    - Compare com Design Doc do Chat
  - [ ] ✍️ **Reflexão (10min):** RESUMO DA SEMANA
    - ✅ E-commerce design completo
    - ✅ 2 Design Docs escritas

- [ ] ✅ **Semana 3 concluída**

---

### Semana 4: Sua Primeira RFC

- [ ] **🟢 Segunda-feira**
  - [ ] 📖 **Teoria (20min):** Estude RFCs de empresas:
    - Uber: https://www.uber.com/en-US/blog/engineering/
    - Figma: https://www.figma.com/blog/section/engineering/
  - [ ] 💻 **Prática (30min):**
    - Escolha tema da RFC (ex: "Estratégia MVI do StaffNotes")
    - Escreva: Context + Problem Statement
  - [ ] ✍️ **Reflexão (10min):**
    - RFC = qualquer dev entende o problema sem contexto prévio

- [ ] **🟢 Terça-feira**
  - [ ] 📖 **Teoria (20min):** Estude templates de ADR:
    - Link: https://adr.github.io
    - Link: https://github.com/joelparkerhenderson/architecture-decision-record
  - [ ] 💻 **Prática (30min):**
    - Escreva: Options Considered (3 opções com trade-offs)
  - [ ] ✍️ **Reflexão (10min):**
    - Sempre listar pelo menos 2-3 opções
    - Incluir opção "não fazer nada"

- [ ] **🟢 Quarta-feira**
  - [ ] 📖 **Teoria (20min):** Releia exemplos de "Decision" e "Consequences"
  - [ ] 💻 **Prática (30min):**
    - Escreva: Decision + Consequences + Rollout Plan
  - [ ] ✍️ **Reflexão (10min):**
    - Consequences: positivas E negativas
    - Rollout: sempre gradual

- [ ] **🟢 Quinta-feira**
  - [ ] 📖 **Teoria (20min):** Pesquise "How to write a good RFC"
    - Link: https://buriti.ca/6-lessons-i-learned-while-implementing-technical-rfcs-as-a-management-tool-34687dbf46cb
  - [ ] 💻 **Prática (30min):**
    - Revise RFC completa
    - Adicione diagramas (Mermaid ou draw.io)
    - Peça para alguém ler
  - [ ] ✍️ **Reflexão (10min):**
    - Diagrama vale mais que 1000 palavras

- [ ] **🟢 Sexta-feira**
  - [ ] 📖 **Teoria (20min):** Releia TODAS as anotações do Q1
  - [ ] 💻 **Prática (30min):**
    - Finalize RFC com feedback recebido
    - Rode StaffNotes completo
  - [ ] ✍️ **Reflexão (10min):** RESUMO MÊS 3 + Q1
    - ✅ 4 System Designs (StaffNotes, Delivery, Chat, E-commerce)
    - ✅ 2 Design Docs + 1 RFC completa
    - ✅ StaffNotes: modularizado, MVI, offline-first, sync

- [ ] ✅ **Semana 4 concluída**
- [ ] ✅ **Mês 3 concluído**
- [ ] ✅ **Q1 CONCLUÍDO 🎉**

---

### 📊 Checklist de Entregáveis Q1:

- [ ] StaffNotes modularizado (:app, :core:*, :feature:*)
- [ ] MVI + UDF implementado
- [ ] Room com migrations, FTS, relationships
- [ ] Offline-first com sync, SSOT, optimistic updates
- [ ] WorkManager com chaining e monitoring
- [ ] Convention Plugins + Version Catalog
- [ ] Build otimizado com métricas
- [ ] 4 Mobile System Designs praticados
- [ ] 2 Design Docs escritas (Chat + E-commerce)
- [ ] 1 RFC completa revisada por peer
- [ ] 2+ ADRs escritas
- [ ] LEARNING_LOG.md atualizado semanalmente
```