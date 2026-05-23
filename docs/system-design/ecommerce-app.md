# System Design — App de E-commerce (Mobile)

> Exercício de entrevista técnica | Semana 3 — Mês 3
> Data: 2026-05-22

---

## 1. Functional Requirements

- Navegar catálogo de produtos (paginado, com filtros e busca)
- Ver detalhe de produto (fotos, descrição, avaliações, estoque)
- Adicionar ao carrinho / wishlist
- Checkout com pagamento
- Histórico de pedidos + rastreamento de entrega
- Deep links para produtos via push/marketing

**Fora do escopo:**
- Sistema do vendedor (admin/seller panel)
- Programa de fidelidade / cupons (evolução futura)
- Streaming de vídeo do produto

---

## 2. Non-Functional Requirements (Constraints)

| Constraint | Decisão técnica gerada |
|---|---|
| **Catálogo dinâmico:** produtos aparecem/somem | Cursor pagination (não offset) |
| **Busca rápida:** resultado em < 500ms | Debounce 300ms + FTS no servidor + cache local |
| **Carrinho offline:** usuário sem internet não perde itens | Room como SSOT do carrinho |
| **Checkout seguro:** nunca processar pagamento no device | Servidor valida e cobra, device só envia intenção |
| **Double charge impossível:** retry não pode cobrar 2x | Idempotency Key em todo POST de pagamento |
| **Deep links:** links de marketing abrem produto direto | Android App Links + fallback para web |

---

## 3. Data Model

### Product
```kotlin
data class Product(
    val id: String,
    val name: String,
    val price: Double,          // preço atual — pode mudar a qualquer hora
    val imageUrls: List<String>,
    val category: String,
    val stock: Int,             // 0 = esgotado
    val rating: Float,
    val cachedAt: Long          // TTL para invalidar cache
)
```

### CartItem
```kotlin
data class CartItem(
    val localId: String,        // UUID local — existe mesmo offline
    val productId: String,
    val productName: String,    // snapshot do nome
    val priceAtAdd: Double,     // snapshot do preço quando adicionou
    val quantity: Int,
    val addedAt: Long
)
// NUNCA fazer JOIN com Product.price para calcular subtotal
// O preço pode ter mudado — sempre usar priceAtAdd
```

### Order
```kotlin
data class Order(
    val id: String,
    val items: List<OrderItem>, // cada item tem priceAtOrder (snapshot imutável)
    val totalPaid: Double,      // calculado no servidor — imutável após confirmação
    val status: OrderStatus,
    val trackingCode: String?
)
```

---

## 4. High-Level Design (HLD)

```
┌──────────────────────────────────────────────────────────────┐
│                         UI Layer                              │
│  CatalogScreen │ ProductScreen │ CartScreen │ CheckoutScreen  │
│  OrderHistoryScreen │ OrderTrackingScreen                     │
└──────────────────────┬───────────────────────────────────────┘
                       │ MVI Intents
┌──────────────────────▼───────────────────────────────────────┐
│                      ViewModels (MVI)                         │
└──────────────────────┬───────────────────────────────────────┘
                       │ UseCases
┌──────────────────────▼───────────────────────────────────────┐
│  SearchProductsUseCase   (debounce + cache)                   │
│  AddToCartUseCase         (valida restaurant único)           │
│  PlaceOrderUseCase        (idempotency key)                   │
│  GetOrderStatusUseCase    (polling)                           │
└──────────┬────────────────────────┬─────────────────────────┘
           │                        │
┌──────────▼──────┐    ┌────────────▼──────────────────────┐
│  Room (SSOT)    │    │  Retrofit + OkHttp                 │
│  products       │    │  GET /products?cursor=&limit=      │
│  cart_items     │    │  GET /products/search?q=           │
│  orders         │    │  POST /orders  (+ Idempotency-Key) │
│  wishlist       │    │  GET /orders/{id}/status           │
└─────────────────┘    └────────────────────────────────────┘
```

---

## 5. Deep Dives

### 5.1 Catálogo — Cursor Pagination + Busca com Debounce

**Cursor Pagination (por que não offset?):**
```
Problema: produto esgota e sai do catálogo enquanto usuário pagina
Offset:   GET /products?page=2 → produto 21 virou produto 20 → duplicata
Cursor:   GET /products?cursor=prod_20&limit=20 → sempre âncora no último visto
```

**Busca com Debounce:**
```kotlin
// No SearchViewModel
searchQuery
    .debounce(300)           // espera 300ms sem digitar
    .distinctUntilChanged()  // não busca se query igual à anterior
    .flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList())
        else repository.searchProducts(query)
    }
    .collect { results -> reduce { copy(products = results) } }
```

**Por que debounce 300ms?**
- Usuário digita "samsung" = 7 teclas → sem debounce = 7 requisições
- Com debounce: apenas 1 requisição (quando pausar 300ms)
- 300ms = imperceptível para o usuário, mas elimina 85% das requisições desnecessárias

**Cache de busca no Room:**
```kotlin
// Resultado da busca fica no Room com TTL de 5min
// Mesma query em < 5min → Room responde sem bater na API
// Evita: usuário digita, apaga, redigita a mesma coisa → 3 requisições → 1 requisição
```

---

### 5.2 Carrinho — Offline-First + Validação de Preço

**Carrinho como SSOT no Room:**
```
Usuário adiciona produto offline:
  1. CartItem inserido no Room com priceAtAdd = preço atual da tela
  2. UI atualiza imediatamente (subtotal calculado com priceAtAdd)
  3. Quando conectar → sync opcional (carrinho não precisa ir ao servidor até checkout)
```

**O problema do preço:**
```
Usuário adiciona produto às 10h: priceAtAdd = R$ 99,90
Produto tem promoção às 14h: Product.price = R$ 79,90
Usuário faz checkout às 15h

→ Qual preço cobrar? R$ 79,90 (preço atual)
→ Mas a UI estava mostrando R$ 99,90 no carrinho!
→ Solução: no checkout, servidor retorna preços atuais → UI exibe diff → usuário confirma
```

**`PriceChangedError` no checkout:**
```kotlin
sealed interface CheckoutResult {
    data class Success(val orderId: String) : CheckoutResult
    data class PriceChanged(
        val items: List<PriceChange> // produtoId + preçoAntigo + preçoNovo
    ) : CheckoutResult
    data class OutOfStock(val productIds: List<String>) : CheckoutResult
    data class PaymentFailed(val reason: String) : CheckoutResult
}
```

---

### 5.3 Checkout — Idempotency Key (nunca cobrar 2x)

**O problema:** usuário toca "Pagar" → rede cai → app não recebe resposta → toca "Pagar" de novo → **cobrança duplicada**.

**Solução: Idempotency Key**

```kotlin
// Gerado UMA VEZ por tentativa de checkout, salvo no Room
val idempotencyKey = UUID.randomUUID().toString() // ex: "a3f9-..."

// Enviado em TODA requisição daquela tentativa
POST /orders
Headers:
  Idempotency-Key: a3f9-...
Body:
  { items: [...], paymentToken: "..." }
```

**Como o servidor se comporta:**
```
1ª requisição com key "a3f9": processa, cobra R$ 150, retorna orderId
2ª requisição com key "a3f9": detecta que já processou → retorna o MESMO orderId sem cobrar de novo
3ª requisição com key "a3f9": idem → mesmo resultado
```

**Fluxo no app:**
```kotlin
class PlaceOrderUseCase {
    suspend operator fun invoke(cart: Cart): CheckoutResult {
        // Busca key existente ou gera nova — NUNCA gera nova se já existe
        val key = orderRepository.getOrCreateIdempotencyKey(cart.id)

        return api.placeOrder(
            items = cart.items,
            idempotencyKey = key  // garante que retry não cobra 2x
        )
    }
}
```

**Quando gerar nova key?**
- Nova tentativa de checkout intencional (usuário voltou ao carrinho e tentou de novo)
- Erro de validação (produto esgotado, preço mudou) → novo key para novo intento

---

### 5.4 Deep Links — Android App Links

**Por que deep links são obrigatórios em e-commerce?**
- Marketing envia push: "Produto X com 50% off!" → deve abrir o produto diretamente
- Compartilhar produto via WhatsApp → link deve abrir o app (não o browser)
- Email de confirmação → "Ver pedido" deve abrir o rastreamento

**Tipos de deep links no Android:**

| Tipo | Formato | Verificação | Fallback |
|---|---|---|---|
| Custom Scheme | `myapp://product/123` | Nenhuma | Nenhum — se app não instalado, erro |
| App Links (HTTP) | `https://loja.com/product/123` | Arquivo `.well-known/assetlinks.json` no servidor | Abre no browser se app não instalado ✅ |

**App Links são o padrão correto para produção:**
```xml
<!-- AndroidManifest.xml -->
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="loja.com"
            android:pathPrefix="/product/" />
    </intent-filter>
</activity>
```

```kotlin
// No MainActivity ou NavHost — processa o link ao abrir
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleDeepLink(intent)
}

private fun handleDeepLink(intent: Intent) {
    val uri = intent.data ?: return
    when {
        uri.path?.startsWith("/product/") == true -> {
            val productId = uri.lastPathSegment
            navController.navigate(ProductRoute(productId))
        }
        uri.path?.startsWith("/order/") == true -> {
            val orderId = uri.lastPathSegment
            navController.navigate(OrderTrackingRoute(orderId))
        }
        uri.path == "/cart" -> navController.navigate(CartRoute)
    }
}
```

**Deep links que o e-commerce precisa:**
```
https://loja.com/product/{id}     → ProductScreen
https://loja.com/cart             → CartScreen
https://loja.com/order/{id}       → OrderTrackingScreen
https://loja.com/category/{slug}  → CatalogScreen com filtro
https://loja.com/search?q={query} → CatalogScreen com busca pré-preenchida
```

---

## 6. Comparação com Chat App (lições reutilizadas)

| Padrão | Chat | E-commerce |
|---|---|---|
| ID local antes do servidor | `localId` das mensagens | `localId` dos CartItems |
| Snapshot de dado financeiro | `priceAtOrder` do pedido | `priceAtAdd` do carrinho |
| Offline-first | Mensagens PENDING no Room | Carrinho no Room |
| Presigned URL | Upload de fotos de perfil | Fotos de produtos (upload do seller) |
| Polling | Status do pedido de entrega | N/A (pedido não muda tão rápido) |
| FCM Data Message | Nova mensagem | Promoção relâmpago, pedido aprovado |

---

## 7. Trade-offs e Decisões

| Decisão | Alternativa rejeitada | Motivo |
|---|---|---|
| Cursor pagination | Offset | Produtos saem do estoque → offset instável |
| Debounce 300ms | Busca a cada tecla | 85% menos requisições sem impacto de UX |
| `priceAtAdd` no CartItem | JOIN com Product.price | Preço pode mudar antes do checkout |
| Idempotency Key | Sem proteção | Double charge = problema financeiro/legal |
| App Links (HTTPS) | Custom Scheme (`myapp://`) | App Links têm fallback para browser |
| Servidor valida preço no checkout | Device calcula | Device pode ser manipulado (jailbreak) |

---

## 8. Onde travei / O que preciso melhorar

- **Busca com filtros combinados:** categoria + preço + avaliação + disponibilidade → como estruturar a query paginada?
  - Solução: todos os filtros como query params no cursor: `cursor=X&category=phones&min_price=500&in_stock=true`
- **Sincronizar wishlist entre devices:** usuário adiciona no celular, deve aparecer no tablet
  - Solução: SSOT no servidor, Room como cache com TTL curto (5min)
- **Pagamento com múltiplos métodos:** cartão salvo + Pix + boleto
  - Cada método tem fluxo diferente (Pix tem QR Code + polling de confirmação)
  - Boleto tem prazo de vencimento → WorkManager para verificar pagamento periódico

