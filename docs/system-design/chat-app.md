# System Design — App de Chat (Mobile)

> Exercício de entrevista técnica | Semana 2 — Mês 3
> Data: 2026-05-22

---

## 1. Functional Requirements

- Enviar e receber mensagens de texto em tempo real (1:1)
- Ver histórico de mensagens (paginado)
- Status da mensagem: enviando → enviado → entregue → lido
- Indicador de "digitando..." (typing indicator)
- Funcionar offline: mensagens rascunho e re-envio automático

**Fora do escopo (non-goals):**
- Grupos (evolução futura)
- Chamadas de voz/vídeo
- Criptografia E2E (mencionado como risco, não implementado agora)

---

## 2. Non-Functional Requirements (Constraints)

| Constraint | Decisão técnica gerada |
|---|---|
| **Latência < 1s** | WebSocket (não polling) |
| **Offline:** mensagem digitada sem internet | Room como SSOT, fila de envio (PENDING) |
| **Ordem garantida:** mensagens na sequência certa | ID local (timestamp + deviceId) + ID do servidor |
| **Bateria:** conexão aberta o tempo todo | Reconexão inteligente + FCM como fallback |
| **Histórico:** carregar mensagens antigas | Cursor pagination reversa (mais recentes primeiro) |

---

## 3. Data Model

### Message
```kotlin
data class Message(
    val localId: String,      // UUID gerado no device — referência local imediata
    val serverId: String?,    // ID do servidor — null até confirmação
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,      // epoch ms — gerado no device (para ordenação local)
    val serverTimestamp: Long?,// epoch ms do servidor — fonte de verdade final
    val status: MessageStatus
)

enum class MessageStatus {
    PENDING,    // ⏳ criada localmente, aguardando envio
    SENT,       // ✓  servidor recebeu
    DELIVERED,  // ✓✓ destinatário recebeu no device
    READ,       // ✓✓ (azul) destinatário abriu a conversa
    FAILED      // ✗  falhou após retries
}
```

### Por que dois IDs? (localId + serverId)

**Problema:** o usuário envia uma mensagem sem internet. O app precisa mostrar ela imediatamente na lista — mas ainda não tem ID do servidor.

```
Usuário toca "Enviar"
  → localId = UUID.randomUUID() → persiste no Room → UI mostra (PENDING)
  → app fica offline 30s
  → reconecta → envia ao servidor → servidor responde com serverId
  → Room atualiza: localId continua, serverId preenchido, status = SENT
```

Se usássemos só o serverId, a mensagem ficaria sem identidade até a confirmação — poderíamos duplicá-la ao receber o ACK do servidor.

---

## 4. Ordenação de Mensagens — o problema difícil

**Problema:** dois devices podem enviar mensagens "ao mesmo tempo". Quem aparece primeiro?

### Estratégia: Hybrid Ordering

1. **Localmente:** ordenar por `timestamp` do device (gerado no envio)
   - UX imediata, sem esperar servidor
2. **Após sync:** reordenar por `serverTimestamp` (autoridade final)
   - Se divergir: UI reordena suavemente (AnimatedList)

```
Device A envia às 10:00:001 → serverTimestamp = 10:00:003
Device B envia às 10:00:002 → serverTimestamp = 10:00:002

Ordem local:   A, B  (por timestamp do device)
Ordem final:   B, A  (por serverTimestamp — B chegou primeiro ao servidor)
```

**Por que não usar só o timestamp do device?**
- Clocks de devices diferentes podem estar dessincronizados (até segundos)
- O servidor é a fonte de verdade para ordenação

---

## 5. High-Level Design (HLD)

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                            │
│  ConversationListScreen │ ChatScreen                     │
│  (Flow de Room)         │ (Flow de Room + WebSocket)     │
└──────────────────────────┬──────────────────────────────┘
                           │ MVI Intents
┌──────────────────────────▼──────────────────────────────┐
│                    ViewModel Layer                        │
│  ChatViewModel: gerencia estado + WebSocket observer     │
└──────────────────────────┬──────────────────────────────┘
                           │ UseCases
┌──────────────────────────▼──────────────────────────────┐
│                    Domain Layer                           │
│  SendMessageUseCase     → Room (PENDING) → WebSocket     │
│  GetMessagesUseCase     → Flow do Room (paginado)        │
│  MarkAsReadUseCase      → Room + WebSocket (READ event)  │
└──────────┬────────────────────────┬─────────────────────┘
           │                        │
┌──────────▼──────┐    ┌────────────▼──────────────────┐
│  Room (SSOT)    │    │  WebSocketClient               │
│  messages       │    │  ws://api/chat/{conversationId}│
│  conversations  │    │  ├── onMessage() → Room insert │
│  (status, IDs)  │    │  ├── onTyping()  → UiState     │
└─────────────────┘    │  └── onError()  → reconnect    │
                       └───────────────┬────────────────┘
                                       │ fallback
                              ┌────────▼────────┐
                              │  FCM (push)      │
                              │  app em background│
                              └─────────────────┘
```

---

## 6. Deep Dives

### 6.1 WebSocket vs SSE vs Polling

| | Polling | SSE | WebSocket |
|---|---|---|---|
| Direção | cliente → servidor | servidor → cliente | bidirecional |
| Protocolo | HTTP | HTTP | WS (upgrade do HTTP) |
| Reconexão | manual (timer) | automática | manual |
| Bateria | boa (dorme) | média | pior (conexão aberta) |
| Caso de uso | status de pedido | notificações unidirecionais | **chat** ✅ |

**Chat precisa de WebSocket** porque:
- Servidor precisa empurrar mensagens ao cliente (sem o cliente pedir)
- Cliente precisa enviar mensagens ao servidor
- Typing indicator: servidor precisa saber que você está digitando agora

### 6.2 Typing Indicator — sem spam ao servidor

**Problema ingênuo:** enviar evento "typing" a cada tecla → 100 eventos por mensagem
**Solução: debounce + TTL**

```kotlin
// No ChatViewModel
private var typingJob: Job? = null

fun onTextChanged(text: String) {
    reduce { copy(draftText = text) }

    typingJob?.cancel()
    if (text.isNotEmpty()) {
        // Envia "typing" só depois de 300ms de pausa
        typingJob = viewModelScope.launch {
            delay(300)
            webSocket.sendTypingEvent(conversationId)
        }
    } else {
        webSocket.sendStoppedTypingEvent(conversationId)
    }
}

// No servidor: TTL de 5s
// Se não receber novo evento de "typing" em 5s → assume que parou
```

### 6.3 Reconexão do WebSocket

A conexão pode cair (Doze Mode, troca de rede WiFi → 4G, servidor reinicia).

```kotlin
class WebSocketClient {
    private var retryDelay = 1_000L // começa em 1s

    fun onConnectionLost() {
        viewModelScope.launch {
            while (!isConnected) {
                delay(retryDelay)
                tryConnect()
                retryDelay = minOf(retryDelay * 2, 30_000L) // cap em 30s
            }
            retryDelay = 1_000L // reseta após sucesso
        }
    }
}
```

**Quando o app está em background:** WebSocket é encerrado pelo sistema.
**Solução:** FCM como fallback — servidor envia push notification. Ao abrir o app, WebSocket reconecta e sincroniza mensagens perdidas via REST.

### 6.4 Mensagens Offline — Fila de Envio

```
Usuário sem internet envia mensagem:
  1. Room: INSERT com status = PENDING, localId gerado
  2. UI mostra imediatamente com ícone ⏳
  3. Tentativa de envio pelo WebSocket → falha → sem retry aqui
  4. WorkManager: PendingMessagesWorker monitora messages WHERE status = PENDING
     - constraint: CONNECTED
     - ao conectar: envia cada PENDING → atualiza para SENT/FAILED
```

### 6.5 Cursor Pagination Reversa (histórico)

Chat exibe do mais recente para o mais antigo. O usuário faz scroll para cima para ver mais.

```
GET /messages?conversationId=X&before=<timestamp>&limit=20

Primeira carga:  before=NOW         → mensagens 100..81
Scroll para cima: before=msg81.ts   → mensagens 80..61
Scroll para cima: before=msg61.ts   → mensagens 60..41
```

No Room, Room Paging 3 com `PagingSource` reverso cuida disso automaticamente.

### 6.6 FCM — Data Message vs Notification Message

| | Notification Message | Data Message |
|---|---|---|
| Processamento | SO exibe automaticamente | App processa no `onMessageReceived` |
| App em foreground | Não exibe (app ignora) | App processa ✅ |
| App em background | SO exibe na bandeja | App acorda e processa ✅ |
| App morto (killed) | SO exibe na bandeja | App acorda e processa ✅ |
| Garantia de entrega | Sem garantia | Sem garantia |

**Para chat: Data Message é o correto.**
- Quando app está em foreground: WebSocket já cuida → FCM é ignorado
- Quando app está em background/morto: FCM acorda o app → `onMessageReceived` → Room INSERT → notificação local customizada

```kotlin
// NotesFirebaseMessagingService
override fun onMessageReceived(message: RemoteMessage) {
    val data = message.data
    val newMessage = data.toMessage() // parse do payload
    // Salva no Room (sem abrir Activity)
    runBlocking { messageRepository.insertMessage(newMessage) }
    // Exibe notificação local com estilo InboxStyle
    showMessageNotification(newMessage)
}
```

---

## 7. Upload de Mídia (Imagens)

### Fluxo: Presigned URL

```
1. App → API: POST /upload/request { type: "image/jpeg", size: 2MB }
2. API → App: { uploadUrl: "https://s3.../uuid?token=...", mediaId: "uuid" }
3. App → S3 diretamente: PUT <uploadUrl> com bytes da imagem
   (evita que a imagem passe pelo servidor da API — escala melhor)
4. App → API: POST /messages { content: "", mediaId: "uuid", type: IMAGE }
5. API → destinatário via WebSocket: nova mensagem com mediaId
6. Destinatário → CDN: GET /media/uuid → download da imagem
```

### Otimizações mobile:
- **Compressão antes do upload:** `Bitmap.compress(JPEG, 80, outputStream)` — reduz 5MB → 500KB
- **Thumbnail primeiro:** envia thumbnail (10KB) junto com a mensagem → destinatário vê a prévia imediatamente
- **Full-res sob demanda:** só baixa a imagem full quando o usuário toca nela
- **Cache local:** Room + FileSystem — imagem baixada uma vez, nunca baixa de novo

---

## 8. Trade-offs e Decisões

| Decisão | Alternativa rejeitada | Motivo |
|---|---|---|
| WebSocket | Polling | Chat precisa de bidirecional < 1s |
| localId + serverId | Só serverId | PENDING sem ID duplicaria ao receber ACK |
| serverTimestamp para ordenação final | Só timestamp do device | Clocks dessincronizados entre devices |
| FCM Data Message | Notification Message | App controla UX da notificação |
| Presigned URL para upload | Upload via servidor | Servidor vira gargalo com arquivos grandes |
| Debounce 300ms no typing | Envio a cada tecla | Spam de eventos inutilizável |

---

## 9. Onde travei / O que preciso melhorar

- **Garantia de ordem em alta concorrência:** e se dois usuários enviarem no mesmo ms? → servidor pode usar Lamport clock ou ID incremental por conversa
- **Deduplicação ao reconectar:** ao reconectar o WebSocket, o servidor pode re-enviar mensagens. Como evitar duplicatas? → `INSERT OR IGNORE` no Room usando `localId` ou `serverId` como primary key
- **Read receipts em escala:** marcar como "lido" para 1000 mensagens de uma vez → batch update, não 1000 requests individuais

