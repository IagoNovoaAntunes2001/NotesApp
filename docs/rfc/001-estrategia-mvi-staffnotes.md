# RFC-001: Estratégia MVI + UDF no StaffNotes

**Status:** Aceita  
**Data:** 2026-05-22  
**Autor:** Iago Antunes  
**Revisores:** (peer review simulado — colega de equipe)  
**Contexto:** Mês 3 — Documentação Técnica e RFC

---

## Sumário

Este documento propõe e justifica a adoção do padrão **MVI (Model-View-Intent)** com **UDF (Unidirectional Data Flow)** como estratégia oficial de gerenciamento de estado da UI no StaffNotes. Avalia alternativas, trade-offs e define o contrato de implementação para todas as features atuais e futuras.

---

## 1. Contexto

O StaffNotes é um app Android multi-módulo (`:app`, `:core:*`, `:home`, `:detail`) que usa Jetpack Compose como toolkit de UI. À medida que novas features foram adicionadas, surgiu a necessidade de **padronizar como a UI consome e reage ao estado**.

Sem um padrão definido, cada feature pode adotar uma abordagem diferente:
- Uma screen usando `LiveData` + `observe()`
- Outra usando múltiplos `StateFlow` soltos
- Outra mutando estado diretamente no ViewModel

Isso aumenta a **carga cognitiva** ao navegar entre módulos, dificulta **testes de ViewModel** e cria bugs sutis de **race condition** e **estado inconsistente**.

---

## 2. Problem Statement

> Como padronizar o fluxo de estado entre a UI e o ViewModel em todas as features do StaffNotes de forma previsível, testável e escalável?

**Requisitos:**
- A UI deve ser uma **função pura do estado** — dado o mesmo estado, sempre a mesma UI
- O fluxo de dados deve ter **direção única** — sem mutação bidirecional
- O ViewModel deve ser **testável sem Android** (`CoroutineTestRule` + `turbine`)
- O padrão deve ser **compreensível por qualquer dev** que entre no projeto
- Deve ser compatível com **Jetpack Compose** e **coroutines/Flow**

---

## 3. Opções Consideradas

### Opção A: Múltiplos StateFlows independentes

**Descrição:** Cada pedaço do estado vive em um `StateFlow` separado no ViewModel.

```kotlin
class HomeViewModel : ViewModel() {
    val topics = MutableStateFlow<List<Topic>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
}
```

| ✅ Prós | ❌ Contras |
|---|---|
| Simples de implementar | UI precisa coletar múltiplos flows |
| Familiar para devs que migraram do LiveData | Risco de estados inconsistentes entre flows |
| Fácil de adicionar um novo campo | Dificulta testes (múltiplos asserts separados) |
| | Sem visão unificada do estado atual |

**Descartada** — estados inconsistentes são um risco real (ex: `isLoading = true` mas `topics` já preenchido).

---

### Opção B: MVVM com StateFlow único (`UiState`)

**Descrição:** Um único `StateFlow<UiState>` com data class imutável. Sem separar eventos de estado.

```kotlin
data class HomeUiState(
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
```

| ✅ Prós | ❌ Contras |
|---|---|
| Estado unificado e imutável | Sem separação clara entre "estado persistente" e "eventos únicos" |
| Compatível com Compose (`collectAsStateWithLifecycle`) | Eventos como "mostrar snackbar" ficam no estado e podem reaparecer |
| Simples e direto | Sem contrato formal de Intent/Action |

**Viável**, mas incompleta para eventos de navegação e efeitos colaterais.

---

### Opção C: MVI com UDF — `UiState` + `UiEvent` + `UiAction` ✅ ESCOLHIDA

**Descrição:** Padrão completo MVI com três contratos separados:

| Contrato | Tipo | Responsabilidade |
|---|---|---|
| `UiState` | `StateFlow<S>` | Estado persistente da tela (renderização) |
| `UiAction` | `sealed class` | Intenções do usuário enviadas ao ViewModel |
| `UiEvent` | `Channel<E>` → `Flow<E>` | Eventos únicos (navegação, snackbar, etc.) |

```kotlin
// Contrato da feature :home
data class HomeUiState(
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class HomeUiAction {
    data class OnTopicClicked(val topicId: String) : HomeUiAction()
    object OnRetryClicked : HomeUiAction()
    object OnRefresh : HomeUiAction()
}

sealed class HomeUiEvent {
    data class NavigateToDetail(val topicId: String) : HomeUiEvent()
    data class ShowSnackbar(val message: String) : HomeUiEvent()
}
```

```kotlin
class HomeViewModel(
    private val getTopicsUseCase: GetTopicsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvent: Flow<HomeUiEvent> = _uiEvent.receiveAsFlow()

    fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.OnTopicClicked -> navigateToDetail(action.topicId)
            HomeUiAction.OnRetryClicked -> loadTopics()
            HomeUiAction.OnRefresh -> loadTopics()
        }
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getTopicsUseCase()
                .onSuccess { topics ->
                    _uiState.update { it.copy(topics = topics, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    _uiEvent.send(HomeUiEvent.ShowSnackbar("Erro ao carregar tópicos"))
                }
        }
    }

    private fun navigateToDetail(topicId: String) {
        viewModelScope.launch {
            _uiEvent.send(HomeUiEvent.NavigateToDetail(topicId))
        }
    }
}
```

| ✅ Prós | ❌ Contras |
|---|---|
| Estado, ações e eventos claramente separados | Mais boilerplate inicial por feature |
| UI é função pura do estado | Curva de aprendizado para devs vindos do MVVM simples |
| Eventos únicos não "poluem" o estado | |
| Altamente testável (turbine + mock UseCase) | |
| Padrão explícito — qualquer dev entende o contrato | |
| Alinhado com Now in Android e guidelines do Google | |

---

## 4. Decisão

**Adotamos a Opção C: MVI com UDF completo.**

O overhead de boilerplate é justificado pela **clareza do contrato** e pela **previsibilidade do estado**. Em um projeto Staff-level, a legibilidade e testabilidade pesam mais que a velocidade de setup inicial.

---

## 5. Diagrama de Fluxo UDF

```
┌─────────────────────────────────────────────────────────┐
│                        FEATURE                          │
│                                                         │
│   ┌──────────┐   UiAction    ┌──────────────────────┐  │
│   │          │ ─────────────▶│                      │  │
│   │    UI    │               │      ViewModel        │  │
│   │(Compose) │◀─────────────│                      │  │
│   │          │   UiState     │  - onAction()        │  │
│   │          │               │  - loadData()        │  │
│   │          │◀─────────────│  - _uiState.update() │  │
│   └──────────┘   UiEvent     │  - _uiEvent.send()  │  │
│                              └──────────┬───────────┘  │
│                                         │               │
│                                         ▼               │
│                              ┌──────────────────────┐  │
│                              │      UseCase         │  │
│                              └──────────┬───────────┘  │
│                                         │               │
│                                         ▼               │
│                              ┌──────────────────────┐  │
│                              │     Repository       │  │
│                              │  (SSOT: Room + API)  │  │
│                              └──────────────────────��  │
└─────────────────────────────────────────────────────────┘
```

---

## 6. Consequences

### Positivas ✅
- **Previsibilidade:** `UiState` é sempre a fonte de verdade da tela
- **Testabilidade:** ViewModel testado com `turbine` + coroutines, sem Android
- **Debugabilidade:** Cada `UiAction` é um evento auditável (pode ser logado)
- **Escalabilidade:** Novos campos em `UiState` ou novos `UiAction` não quebram o contrato
- **Padronização:** Qualquer dev lê `HomeUiState.kt` e entende o que a tela pode exibir

### Negativas ⚠️
- **Boilerplate:** Cada nova feature requer 3 arquivos adicionais (`State`, `Action`, `Event`)
- **Overhead para telas simples:** Uma tela estática não precisa de `UiAction` com `Channel`
- **Curva de aprendizado:** Devs acostumados com LiveData + observe precisam entender Flow + Channel

### Mitigações
- Criar um **template/snippet** de ViewModel MVI para novas features
- Para telas verdadeiramente simples (sem interação), `UiState` + `StateFlow` simples é aceitável (Opção B como exceção documentada)

---

## 7. Rollout Plan

| Fase | Ação | Status |
|---|---|---|
| **Fase 1** | Implementar contrato MVI em `:home` (HomeViewModel) | ✅ Concluído |
| **Fase 2** | Implementar contrato MVI em `:detail` (DetailViewModel) | ✅ Concluído |
| **Fase 3** | Criar snippet de template para novas features | ✅ Concluído |
| **Fase 4** | Documentar no `learning_log.md` como padrão oficial | ✅ Concluído |
| **Fase 5** | Aplicar em novas features do Q2 sem exceções | 🔜 Próximo trimestre |

---

## 8. Referências

- [Now in Android — ViewModel State](https://github.com/android/nowinandroid)
- [Google Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ADR-001: Arquitetura do StaffNotes](../adr/001-arquitetura-staffnotes.md)
- [ADR-002: Estratégia de Sincronização](../adr/002-estrategia-sincronizacao.md)
- [How to write a good RFC](https://buriti.ca/6-lessons-i-learned-while-implementing-technical-rfcs-as-a-management-tool-34687dbf46cb)
- [Kotlin Coroutines — Channel](https://kotlinlang.org/docs/channels.html)

---

*RFC criada como entregável da Semana 4, Mês 3, Q1 do plano Staff Android Engineer.*

