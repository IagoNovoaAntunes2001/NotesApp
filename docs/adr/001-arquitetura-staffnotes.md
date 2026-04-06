# ADR-001: Arquitetura do StaffNotes

**Status:** Aceita  
**Data:** 2026-04-06  
**Autores:** Iago Antunes  
**Contexto:** Mês 1 — Fundações do projeto StaffNotes

---

## Contexto

O StaffNotes é um app Android de estudo pessoal para o plano "Staff Android Engineer". O objetivo é construir um projeto real que sirva de laboratório para praticar e consolidar os padrões usados em apps Android modernos de grande escala (referência: Now in Android).

O projeto precisava de uma arquitetura que:
- Suportasse **crescimento gradual** (mais features, mais devs simulados)
- Fosse **testável** em todas as camadas
- Seguisse os **guias oficiais do Android** (Google Architecture Guide)
- Tivesse **build rápido e escalável** com multi-module
- Facilitasse o estudo de padrões avançados como offline-first, sync, WorkManager

---

## Problema

Escolher a arquitetura, estrutura de módulos, padrão de estado da UI e estratégia de DI que melhor atendam aos objetivos acima, considerando que:

1. O app será **expandido ao longo de 12 meses** (Meses 1–12 do plano)
2. O **build time** importa — trabalho local com múltiplos módulos
3. A arquitetura deve ser **didaticamente clara** (é um projeto de estudo)
4. Precisa ser **compatível com as melhores práticas da indústria** (entrevistas Staff)

---

## Opções Consideradas

### Opção A: Monolito (1 módulo `:app`)

**Descrição:** Todo o código em `:app`, sem modularização.

| Pronto | Contra |
|--------|--------|
| Setup simples e rápido | Build sempre recompila tudo |
| Sem overhead de grafo de módulos | Dificulta testes isolados por feature |
| | Acopla UI com dados e lógica |
| | Não escala com time nem com features |

**Descartada** — não reflete a realidade de apps Staff-level.

---

### Opção B: Multi-module por Layer (horizontal slicing)

**Descrição:** Módulos `:ui`, `:domain`, `:data` — todas as features dentro de cada camada.

| Pronto | Contra |
|--------|--------|
| Separação de camadas clara | Adicionar feature requer mexer em 3+ módulos |
| Caches Gradle por camada | Features não são independentes |
| | Parallelismo de build limitado |
| | Merges frequentes entre devs |

**Descartada** — não favorece independência de feature nem parallelismo máximo.

---

### Opção C: Multi-module por Feature + Core layers (vertical slicing) ✅ ESCOLHIDA

**Descrição:** Módulos organizados em `:app`, `:core:*` e features independentes (`:home`, `:detail`).

```
:app                 ← orquestrador: NavHost + @HiltAndroidApp
:home                ← feature: lista (MVI autossuficiente)
:detail              ← feature: detalhe (MVI autossuficiente)
:core:model          ← entidades puras (Topic) — zero deps
:core:data           ← contratos (TopicRepository) + UseCases
:core:database       ← implementação Room (Entity, DAO, RepositoryImpl)
:design-system       ← componentes visuais compartilhados
```

| Pronto | Contra |
|--------|--------|
| Features compilam em paralelo | Mais arquivos de build inicialmente |
| Feature nunca depende de outra | Overhead de configurar grafo de módulos |
| Build cache granular por módulo | Curva de aprendizado do multi-module Gradle |
| Testabilidade isolada por feature | Hilt multi-module requer @Module em cada módulo |
| Escala com mais features sem tocar `:app` | |
| Reflete a arquitetura do Now in Android | |

**Escolhida.**

---

## Decisão

### Estrutura de Módulos

```
:app
├── :home
├── :detail
├── :design-system
└── :core
    ├── :core:model
    ├── :core:data
    └── :core:database
```

**Regra fundamental (validada pelo plugin `modules-graph-assert`):**
> `:home` e `:detail` nunca se dependem mutuamente. Quem orquestra é o `:app`.

---

### Padrão de UI: MVI (Model–View–Intent)

Cada feature implementa o padrão MVI com três tipos de artefatos:

```kotlin
// Intent: o que o usuário pode fazer
sealed interface HomeIntent {
    data object LoadTopics : HomeIntent
    data class AddTopic(val title: String, val description: String) : HomeIntent
    data class DeleteTopic(val topic: Topic) : HomeIntent
    data class Search(val query: String) : HomeIntent
    data class NavigateToDetail(val topic: Topic) : HomeIntent
}

// State: snapshot imutável de toda a UI
data class HomeUiState(
    val isLoading: Boolean = false,
    val topics: List<Topic> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

// SideEffect: eventos únicos (não fazem parte do estado)
sealed interface HomeSideEffect {
    data class ShowSnackbar(val message: String) : HomeSideEffect
    data class NavigateToDetail(val topicId: Int) : HomeSideEffect
}
```

**Fluxo de dados (UDF):**

```
UI → processIntent(intent) → ViewModel
ViewModel → reduce { copy(...) } → StateFlow<UiState>
ViewModel → _sideEffect.send(...) → Channel<SideEffect>
StateFlow → collectAsStateWithLifecycle() → UI re-render
Channel → LaunchedEffect → navegação/snackbar
```

**Justificativa para Channel nos side-effects:**
- `StateFlow` emitiria o mesmo side-effect novamente após recomposição
- `Channel` garante: emitiu → consumiu → descartou (exactly-once)
- Evita navegação duplicada ou snackbar aparecendo duas vezes

---

### Processo de Tratamento de Estado

O ViewModel usa uma função `reduce` para aplicar mudanças de estado de forma funcional:

```kotlin
private fun reduce(reducer: HomeUiState.() -> HomeUiState) {
    _uiState.update { it.reducer() }
}
```

Isso garante que **cada mudança de estado é uma função pura** aplicada ao estado atual, facilitando debugging e testes.

---

### Sobrevivência ao Process Death: SavedStateHandle

Dados que o usuário digitou (ex: `searchQuery`) são salvos via `SavedStateHandle`:

```kotlin
var searchQuery: String
    get() = savedStateHandle.get<String>(KEY_SEARCH_QUERY) ?: ""
    set(value) {
        savedStateHandle[KEY_SEARCH_QUERY] = value
        reduce { copy(searchQuery = value) }
    }
```

**O que sobrevive ao process death:**
| Mecanismo | Sobrevive config change | Sobrevive process death | Sobrevive reboot |
|-----------|------------------------|------------------------|-----------------|
| `StateFlow` | ✅ (ViewModel fica vivo) | ❌ | ❌ |
| `SavedStateHandle` | ✅ | ✅ | ❌ |
| `Room` / `DataStore` | ✅ | ✅ | ✅ |

---

### Data Layer: Repository Pattern + Room

```
TopicRepository (interface)         → :core:data
TopicRepositoryImpl (Room)          → :core:database
TopicEntity, TopicDao, NoteDatabase → :core:database
Topic (domain model)                → :core:model
TopicMapper (Entity ↔ Domain)       → :core:database
```

**Inversão de dependência (DIP aplicado ao Gradle):**
- `:home` e `:detail` dependem de `:core:data` (contrato)
- `:core:database` implementa o contrato
- `:app` instala a implementação via Hilt `@Binds`
- Features **nunca veem o Room** — apenas a interface

---

### DI: Hilt Multi-Module

Cada módulo declara seu `@Module`:

| Módulo | @Module | O que registra |
|--------|---------|----------------|
| `:core:database` | `DatabaseModule` | `AppDatabase`, `TopicDao` |
| `:core:database` | (RepositoryImpl binding) | `TopicRepository` ← `TopicRepositoryImpl` |
| `:core:data` | `DataModule` | UseCases |
| `:home` | `HomeModule` | recursos de string (`HomeResources`) |
| `:detail` | `DetailModule` | recursos de string (`DetailResources`) |
| `:app` | `@HiltAndroidApp` | instala tudo via component hierarchy |

---

### Navegação: Multi-Module Navigation Compose

Cada feature expõe sua rota como extension function de `NavGraphBuilder`:

```kotlin
// :home
fun NavGraphBuilder.homeNavGraph(onNavigateToDetail: (Int) -> Unit) { ... }

// :detail  
fun NavGraphBuilder.detailNavGraph(onNavigateBack: () -> Unit) { ... }
```

`:app` monta o NavHost orquestrando todas as rotas:

```kotlin
NavHost(navController, startDestination = HOME_ROUTE) {
    homeNavGraph(onNavigateToDetail = { id -> navController.navigate(createDetailRoute(id)) })
    detailNavGraph(onNavigateBack = { navController.popBackStack() })
}
```

**Benefício:** Cada feature pode ser adicionada/removida do grafo de navegação sem que `:app` precise conhecer a implementação interna — apenas os contratos de entrada/saída.

---

### Build: Convention Plugins + Version Catalog + Otimizações

**Convention Plugins** (`build-logic/`) eliminam duplicação nos `build.gradle.kts`:

| Plugin | O que configura |
|--------|----------------|
| `notes.android.library` | `compileSdk`, `minSdk`, `compileOptions`, Kotlin JVM target |
| `notes.android.library.compose` | Compose plugin + deps BOM |
| `notes.android.hilt` | Hilt plugin + KSP + `hilt-android` + `hilt-compiler` |
| `notes.android.application` | Extend library config + `applicationId` padrão |
| `notes.android.application.compose` | Application + Compose |

**Otimizações ativas** (`gradle.properties`):

```properties
org.gradle.parallel=true             # módulos compilam em paralelo
org.gradle.caching=true              # outputs cacheados por hash
org.gradle.configuration-cache=true  # task graph serializado, reutilizado
org.gradle.jvmargs=-Xmx4096m        # menos GC pressure
```

**Resultado medido:**
- 1º build (clean, sem cache): ~38s
- 2º build (configuration cache reused + build cache hit): ~5s

---

## Consequências

### Positivas
- ✅ Build incremental muito rápido — mudança em `:home` não recompila `:detail` nem `:core:*`
- ✅ Features testáveis em isolamento — sem dependência de outras features
- ✅ Inversão de dependência real — features não conhecem Room
- ✅ Processo de death tratado com SavedStateHandle para dados do usuário
- ✅ Side-effects tratados com Channel — sem duplicação de eventos
- ✅ Arquitetura alinhada com Now in Android e guias oficiais
- ✅ Módulo grafo validado por `modules-graph-assert` em tempo de build
- ✅ Convention Plugins: adicionar nova feature = 3 linhas no `build.gradle.kts`

### Negativas / Trade-offs
- ⚠️ Setup inicial mais complexo que monolito — muitos arquivos de build
- ⚠️ Hilt multi-module tem mais cerimônia que Koin (mais anotações, mais `@Module`)
- ⚠️ `Channel` para side-effects pode perder eventos se a UI não estiver coletando (app em background) — para casos críticos, `SharedFlow(replay=1)` seria mais seguro
- ⚠️ `getTopics()` retorna `List<Topic>` (snapshot) em vez de `Flow<List<Topic>>` — não é reativo ainda; Mês 2 (Offline-First) refatorará para `Flow`

---

## Plano de Evolução

| Mês | Evolução arquitetural planejada |
|-----|---------------------------------|
| Mês 2 | `Flow<List<Topic>>` no DAO + Repository reativo + offline-first |
| Mês 2 | WorkManager para sync periódico |
| Mês 3 | Mobile System Design: design da arquitetura de sync em docs |
| Futuro | `:core:network` (Ktor/Retrofit) + `NoteRemoteDataSource` |
| Futuro | KMP: shared business logic em `:core:*` pure Kotlin |

---

## Referências

- [Guide to app architecture — Android Developers](https://developer.android.com/topic/architecture)
- [Now in Android — Architecture Learning Journey](https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md)
- [MVI Architecture — Hannes Dorfmann](https://hannesdorfmann.com/android/model-view-intent/)
- [Modularization guide — Android Developers](https://developer.android.com/topic/modularization)
- [Hilt in multi-module apps](https://developer.android.com/training/dependency-injection/hilt-multi-module)
- [Migrate to Version Catalogs](https://developer.android.com/build/migrate-to-catalogs)
- [modules-graph-assert](https://github.com/jraska/modules-graph-assert)

