# ADR 002 — Estratégia de Sincronização do StaffNotes

**Data:** 2026-05-20  
**Status:** Aceito  
**Autores:** Iago Antunes

---

## Contexto

O StaffNotes é um app **offline-first**: o Room é a fonte de verdade e a UI nunca lê diretamente da API. Precisávamos definir:

1. Como e quando sincronizar dados com o servidor
2. Como tratar conflitos entre local e remoto
3. Como garantir que edições offline cheguem ao servidor mesmo se o app fechar
4. Como dar feedback visual do estado de sync ao usuário

---

## Decisão

### 1. Padrão Offline-First (SSOT — Single Source of Truth)

**O banco local (Room) é a única fonte de verdade.**

```
API → Room → UI
```

- A UI **nunca** lê da API diretamente
- O Repository expõe `Flow<List<Topic>>` do Room — a UI reage passivamente
- `sync()` é um efeito colateral: escreve no Room, que por sua vez notifica a UI via Flow
- Se a API falhar, o usuário ainda vê o cache — nunca tela em branco com cache disponível

**Alternativa rejeitada:** UI lê da API + Room como cache  
**Por quê rejeitada:** Requer lógica de deduplicação, a UI precisa decidir qual fonte usar, mais pontos de falha.

---

### 2. Três Estratégias de Sync Complementares

Cada estratégia resolve um cenário diferente:

```
┌─────────────────────────────────────────────────────────────────────┐
│  App abre (onCreate)                                                │
│    └─► sync_chain (UniqueWork)                                      │
│        SyncUpWorker → SyncDownWorker → CleanupWorker                │
│        Cadeia completa: PUSH + PULL + limpeza                       │
├─────────────────────────────────────────────────────────────────────┤
│  App volta ao foreground (ProcessLifecycleOwner.onStart)            │
│    └─► urgent_sync (Expedited UniqueWork)                           │
│        UrgentSyncWorker: PUSH + PULL imediatos                      │
│        Alta prioridade, roda em segundos                            │
├─────────────────────────────────────────────────────────────────────┤
│  A cada 15 minutos (mínimo Android)                                 │
│    └─► periodic_sync (PeriodicWork)                                 │
│        SyncUpWorker: só PUSH (pendentes)                            │
│        PeriodicWork não suporta chaining                            │
└─────────────────────────────────────────────────────────────────────┘
```

**Por que 3 estratégias e não 1?**
- `PeriodicWork` não suporta chaining → não pode fazer cadeia completa
- `Expedited` não suporta periodic → não pode substituir o periódico
- A cadeia OneTime cobre a abertura do app; o periodic cobre o background; o expedited cobre o retorno ao foreground

---

### 3. WorkManager Chaining: SyncUp → SyncDown → CleanupWorker

A cadeia garante **ordem e dependência** entre as fases:

```
SyncUpWorker   → PUSH: envia tópicos PENDING ao servidor
       ↓ (só se success)
SyncDownWorker → PULL: busca dados novos, aplica LWW
       ↓ (só se success)
CleanupWorker  → Remove tópicos ERROR > 7 dias do Room
```

- Se qualquer fase falha → `Result.retry()` → backoff exponencial (10s→20s→40s→...)
- A cadeia seguinte só roda se a atual completou com sucesso
- `ExistingWorkPolicy.KEEP` garante que nunca existam 2 cadeias simultâneas

**Por que PUSH antes de PULL?**  
Se fizermos PULL primeiro e o servidor tiver dados mais recentes, podemos sobrescrever edições locais PENDING antes de enviá-las. PUSH primeiro protege as edições offline.

---

### 4. Conflict Resolution: Last Write Wins (LWW)

Quando servidor e local têm versões diferentes do mesmo tópico:

```
server.updatedAt > local.updatedAt  →  Server vence  → SYNCED
server.updatedAt < local.updatedAt  →  Local vence   → mantém PENDING (edição offline protegida)
server.updatedAt == local.updatedAt →  Empate        → local mantido → CONFLICT
local == null                       →  Novo do server → SYNCED
```

**Por que LWW e não Server Wins puro?**  
Server Wins descartaria edições offline feitas enquanto sem internet. LWW protege edições locais mais recentes — o cenário comum para um app de notas pessoal.

**Por que não CRDTs ou OT (Operational Transform)?**  
Overkill para notas pessoais em um único dispositivo. CRDTs/OT fazem sentido em apps colaborativos em tempo real (Google Docs). O LWW perderá dados apenas em conflitos simultâneos, que são raros neste contexto.

---

### 5. Optimistic Updates com Rollback

Para operações de edição, atualizamos a UI **antes** de confirmar com o servidor:

```
1. Salva snapshot do estado atual
2. Persiste no Room imediatamente → UI atualiza (Flow emite)
3. Envia para API em background
   - Sucesso → marca SYNCED → confirma a operação
   - Falha   → ROLLBACK: restaura snapshot no Room → UI reverte via Flow
```

**Por que Optimistic e não Pessimistic?**  
Pessimistic mostra spinner enquanto espera a API — latência de 200ms a 2s de UX ruim. Optimistic parece instantâneo. O risco (rollback) é aceitável pois falhas de API em edições simples são raras.

---

### 6. SyncStatus — Rastreamento de Estado por Registro

Cada `Topic` tem um campo `syncStatus: SyncStatus`:

| Status | Significado |
|---|---|
| `SYNCED` | Confirmado pelo servidor |
| `PENDING` | Criado/editado localmente, aguardando envio |
| `CONFLICT` | Empate de timestamp detectado |
| `ERROR` | Falha persistente (removido após 7 dias pelo CleanupWorker) |

Isso permite que o `SyncUpWorker` saiba exatamente quais registros enviar sem re-sincronizar tudo.

---

### 7. Feedback Visual (SyncState)

O `WorkInfo.State` do WorkManager é mapeado para um `SyncState` (sealed interface) antes de chegar à UI:

```
WorkInfo.State  →  SyncState       →  UI
RUNNING         →  Running(33%)    →  Barra de progresso determinada + "Sincronizando... 33%"
ENQUEUED/BLOCKED→  Enqueued        →  ⏳ "Aguardando sync..."
SUCCEEDED       →  Succeeded       →  ✓ "Sincronizado ✓" (verde)
FAILED          →  Failed          →  ✗ "Erro ao sincronizar" (vermelho)
CANCELLED/null  →  Idle            →  Invisível
```

O progresso é granular por fase da cadeia: 0→33% (PUSH), 33→66% (PULL), 66→100% (Cleanup).

---

## Consequências

### Positivas

- **UX offline completa:** usuário nunca vê tela em branco se há cache
- **Edições offline seguras:** PENDING garante que nada é perdido
- **UI sempre reativa:** Flow do Room notifica automaticamente, sem polling
- **Sync garantido:** WorkManager sobrevive a reboot, process death e Doze Mode
- **Sem sobrecarga:** 3 constraints (rede, bateria) evitam desperdício de recursos

### Negativas / Trade-offs

- **LWW pode perder dados** em conflitos simultâneos raros (dois devices editando o mesmo tópico ao mesmo tempo)
- **15 min mínimo** para PeriodicWork — não há como sync em tempo real sem WebSocket/FCM
- **Expedited tem cota** — se abusado, o sistema degrada para worker normal
- **Rollback é visível** — o usuário vê a UI reverter, o que pode causar estranheza

### Possíveis evoluções futuras

- Push notifications (FCM) para sync em tempo real quando o servidor tiver dados novos
- Substituir LWW por merge baseado em campo (ex: title e body resolvidos separadamente)
- Paginação remota para listas grandes (Paging 3)
- Sync delta (apenas registros alterados desde a última sync) para reduzir tráfego

---

## Diagrama Completo do Fluxo de Sync

```
┌────────���─────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  HomeScreen coleta Flow<UiState> ← HomeViewModel             │
│  SyncStatusBar mostra SyncState (Idle/Enqueued/Running/...)  │
└───────────────────────┬──────────────────────────────────────┘
                        │ collectAsStateWithLifecycle
                        ▼
┌──────────────────────────────────────────────────────────────┐
│                      ViewModel                               │
│  observeTopicsStream() → coleta Flow do Room                 │
│  observeSyncWorkerProgress() → coleta WorkInfo Flow          │
│  syncTopics() → chama SyncTopicsUseCase (manual/refresh)     │
└────────────┬──────────────────────┬──────────────────────────┘
             │                      │
             ▼                      ▼
┌────────────────────┐   ┌──────────────────────────────────┐
│  Room (SSOT)       │   │  WorkManager                     │
│  Flow<List<Topic>> │   │  sync_chain    (onCreate)        │
│  emite ao mudar    │   │  urgent_sync   (foreground)      │
└────────────────────┘   │  periodic_sync (15 min)          │
         ▲               └──────────────┬───────────────────┘
         │ insertAll/insert              │ doWork()
         └──────────────────────────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │  TopicRemoteDataSource│
            │  (jsonplaceholder API)│
            └───────────────────────┘
```

