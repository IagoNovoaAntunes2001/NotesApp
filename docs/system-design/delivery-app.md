# System Design — App de Delivery (Mobile)

> Exercício de entrevista técnica | Timer: 30min
> Data: 2026-05-22

---

## 1. Functional Requirements

O que o app **faz** (escopo de uma entrevista — não o app inteiro):

- [X] Usuário vê lista de restaurantes próximos
- [X] Usuário vê cardápio de um restaurante
- [X] Usuário monta carrinho e faz checkout
- [X] Usuário acompanha pedido em tempo real (status: recebido → preparando → saiu → entregue)
- [X] Histórico de pedidos

**Fora do escopo (non-goals):**
- Sistema do lado do restaurante (admin panel)
- Pagamento (delegar para gateway externo)
- Avaliações e reviews

---

## 2. Non-Functional Requirements (Constraints)

> ⚠️ **Regra de ouro:** sempre começar aqui — os constraints guiam tudo.

| Constraint | Decisão técnica gerada |
|---|---|
| **Offline parcial:** ver cardápio sem internet | Cache de restaurantes e cardápios no Room |
| **Lat/lng precisa:** localização do usuário | FusedLocationProvider + permissão fine location |
| **Real-time:** status do pedido muda a cada 30s | WebSocket ou polling com WorkManager |
| **Escala:** 100K restaurantes | Cursor pagination + busca por raio geográfico |
| **Bateria:** rastreamento GPS em background | Sem GPS contínuo — polling de status pela API |
| **Consistência:** carrinho nunca perde itens | Room como fonte de verdade do carrinho |

---

## 3. High-Level Design (HLD)

```
┌─────────────────────────────────────────────────┐
│                    UI Layer                      │
│  HomeScreen  │  RestaurantScreen  │  CartScreen  │
│  OrderTrackingScreen │ OrderHistoryScreen        │
└──────────────────────┬──────────────────────────┘
                       │ StateFlow + Intents (MVI)
┌──────────────────────▼──────────────────────────┐
│                 ViewModel Layer                  │
│  HomeViewModel │ CartViewModel │ OrderViewModel  │
└──────────────────────┬──────────────────────────┘
                       │ UseCases
┌──────────────────────▼──────────────────────────┐
│                  Domain Layer                    │
│  GetRestaurantsUseCase (paged, by location)      │
│  GetMenuUseCase                                  │
│  AddToCartUseCase / RemoveFromCartUseCase         │
│  PlaceOrderUseCase                               │
│  GetOrderStatusUseCase (polling/WebSocket)       │
└──────────┬───────────────────────┬──────────────┘
           │                       │
┌──────────▼──────┐   ┌────────────▼──────────────┐
│  LocalDataSource │   │     RemoteDataSource       │
│  (Room)          │   │     (Retrofit + OkHttp)    │
│                  │   │                            │
│  restaurants     │   │  GET /restaurants?lat&lng  │
│  menus           │   │  GET /restaurants/{id}/menu│
│  cart_items      │   │  POST /orders              │
│  orders          │   │  GET /orders/{id}/status   │
└──────────────────┘   └────────────────────────────┘
           │
┌──────────▼──────────────────────────────────────┐
│              WorkManager                         │
│  OrderStatusWorker — polling a cada 30s          │
│  (PeriodicWork com constraint: CONNECTED)         │
└─────────────────────────────────────────────────┘
```

---

## 4. Deep Dives

### 4.1 Listagem de Restaurantes — Cursor Pagination

**Por que cursor e não offset?**
- Restaurantes aparecem e somem (fecham, abrem) — offset pularia ou duplicaria itens
- Cursor usa o `id` ou `distance` do último restaurante recebido

```
GET /restaurants?lat=-23.55&lng=-46.63&cursor=null&limit=20
→ { data: [...20 restaurantes], nextCursor: "eyJpZCI6MjB9" }

GET /restaurants?lat=-23.55&lng=-46.63&cursor=eyJpZCI6MjB9&limit=20
→ { data: [...próximos 20], nextCursor: "eyJpZCI6NDB9" }
```

**Schema no Room:**
```sql
CREATE TABLE restaurants (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  distance_km REAL,  -- calculado pelo servidor com lat/lng
  rating REAL,
  is_open INTEGER,   -- Boolean
  cached_at INTEGER  -- epoch ms — para saber se cache expirou
)
```

**Quando invalidar o cache?**
- `cached_at` > 15 minutos → buscar da API novamente (stale-while-revalidate)

---

### 4.2 Carrinho — Offline-First

**Problema:** usuário adiciona item sem internet. Dados não podem ser perdidos.

**Solução:** Room é a fonte de verdade do carrinho.

```sql
CREATE TABLE cart_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  restaurant_id TEXT NOT NULL,
  item_id TEXT NOT NULL,
  item_name TEXT NOT NULL,
  quantity INTEGER NOT NULL,
  unit_price REAL NOT NULL,
  -- preço validado somente no checkout (servidor pode mudar o preço)
)
```

**Fluxo do checkout:**
1. User toca "Fazer Pedido"
2. `PlaceOrderUseCase` envia carrinho para a API
3. API valida preços e disponibilidade
4. Se preço mudou → `PriceChangedError` → UI mostra diff e pede confirmação
5. API confirma → Room salva order com status `RECEIVED` → cart_items limpo

---

### 4.3 Rastreamento do Pedido — Polling vs WebSocket

**Opção A: Polling (nossa escolha)**
```kotlin
// PeriodicWorkRequest a cada 30s enquanto pedido estiver ativo
PeriodicWorkRequestBuilder<OrderStatusWorker>(30, TimeUnit.SECONDS)
    .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
    .build()
```
✅ Simples, não mantém conexão aberta, poupa bateria
❌ Latência de até 30s para ver atualização

**Opção B: WebSocket**
```
ws://api.delivery.com/orders/{id}/status
→ emite eventos: { status: "PREPARING", estimatedMinutes: 15 }
```
✅ Tempo real (< 1s)
❌ Conexão persistente = bateria + complexidade de reconexão

**Decisão:** Polling com 30s para MVP. WebSocket como evolução futura se usuários reclamarem da latência.

---

### 4.4 Schema de Dados — Relacionamentos

```
Restaurant (1) ──── (N) MenuItem
     │
     └── (1) ──── (N) Order ──── (N) OrderItem
                       │
                  OrderStatus (histórico de transições)
```

```kotlin
// N:N implícito via OrderItem
data class OrderItem(
    val orderId: String,
    val menuItemId: String,
    val quantity: Int,
    val priceAtOrder: Double  // snapshot do preço — nunca referenciar o atual
)
```

**Por que `priceAtOrder`?**
- O preço pode mudar depois do pedido. O valor pago deve ser imutável.
- Nunca fazer `JOIN` com `MenuItem.price` para calcular total de um pedido histórico.

---

## 5. Trade-offs e Decisões

| Decisão | Alternativa rejeitada | Motivo |
|---|---|---|
| Polling a cada 30s | WebSocket | Mais simples, menos bateria para MVP |
| Room para carrinho | SharedPreferences | Precisa de queries (filtrar por restaurant, contar itens) |
| Cursor pagination | Offset pagination | Offset instável com dados dinâmicos |
| Snapshot de preço | JOIN com MenuItem atual | Histórico de pedido deve ser imutável |
| Stale-while-revalidate | Sempre da API | UX rápida + offline funciona |

---

## 6. Refinamento — Trade-offs Detalhados

### Trade-off 1: Polling vs WebSocket para rastreamento

| Critério | Polling (30s) | WebSocket |
|---|---|---|
| Implementação | Simples (WorkManager) | Complexa (reconexão, heartbeat) |
| Bateria | Boa (dorme entre polls) | Pior (conexão aberta) |
| Latência | Até 30s | < 1s |
| Escalabilidade servidor | Simples (REST stateless) | Difícil (servidor stateful) |
| **Decisão MVP** | ✅ **Polling** | Evolução v2 |

**Quando migrar para WebSocket?** Quando o produto pedir rastreamento em tempo real com mapa animado (ex: mototaxi ao vivo). Para "status do pedido" (5 transições em 30min), polling é suficiente.

---

### Trade-off 2: Cache dos restaurantes — quanto tempo?

**TTL (Time To Live) do cache de restaurantes:**
- Muito curto (< 5min): muitas requisições → bate bateria e dados
- Muito longo (> 1h): restaurante fecha e ainda aparece aberto
- **Escolha: 15 minutos** com stale-while-revalidate

```kotlin
// No Room, cada restaurante tem cached_at
fun isStale(cachedAt: Long): Boolean =
    System.currentTimeMillis() - cachedAt > 15 * 60 * 1000L // 15min

// Estratégia stale-while-revalidate:
// 1. Mostra cache imediatamente (UX rápida)
// 2. Se stale → sincroniza em background
// 3. Room emite novo Flow → UI atualiza sem spinner
```

---

### Trade-off 3: Múltiplos restaurantes no carrinho?

**Apps reais:**
- iFood, Rappi: **NÃO** — um restaurante por vez, limpa carrinho ao trocar
- Amazon: **SIM** — múltiplos sellers, checkout separado por seller

**Decisão para MVP:**
```kotlin
class AddToCartUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(item: CartItem): Result<Unit> {
        val currentRestaurant = repo.getCartRestaurantId()
        if (currentRestaurant != null && currentRestaurant != item.restaurantId) {
            // Opção A: lança erro → UI pergunta "Limpar carrinho?"
            return Result.failure(DifferentRestaurantException(currentRestaurant))
            // Opção B (futura): cria múltiplos carrinhos
        }
        return repo.addItem(item)
    }
}
```

---

### Trade-off 4: Schema de busca de restaurantes

**Opção A: Busca por bounding box (privacy-first)**
```
GET /restaurants?lat_min=X&lat_max=Y&lng_min=A&lng_max=B&limit=20
```
- ✅ Não expõe coordenada exata do usuário
- ❌ Box quadrada ≠ raio circular (mostra alguns longe demais)

**Opção B: Busca por raio (mais precisa)**
```
GET /restaurants?lat=-23.55&lng=-46.63&radius_km=3&limit=20
```
- ✅ Resultados mais relevantes
- ❌ Coordenada exata enviada ao servidor

**Decisão: Opção B para MVP** (simplifica o servidor). Adicionar ofuscação de coordenadas quando produto pedir feature de privacidade.

---

## 7. Onde travei / O que preciso melhorar

- [ ] Como lidar com múltiplos restaurantes no mesmo carrinho? (Amazon faz, iFood não faz)
  - Solução: validar no `AddToCartUseCase` — se restaurante diferente, limpar carrinho ou criar carrinho separado
- [ ] Deep links para compartilhar restaurante: `delivery://restaurant/{id}`
  - Próxima semana: Deep Links no estudo plan
- [ ] Push notifications para status do pedido:
  - FCM Data Message → app processa → atualiza Room → UI reage pelo Flow
- [ ] Como calcular distância dos restaurantes sem expor lat/lng do usuário para o servidor?
  - Enviar bounding box ao invés de coordenada exata (privacy-first)

---

## Reflexão

- **Cursor pagination > offset** para dados dinâmicos — especialmente listas de restaurantes que abrem/fecham
- **Sempre planejar para offline:** cardápio cacheado = usuário pode navegar no metrô
- **Snapshot de preço** é um padrão crítico em e-commerce/delivery — nunca esquecer
- **Polling vs WebSocket:** para MVP polling é suficiente; WebSocket é uma otimização de UX, não de funcionalidade

