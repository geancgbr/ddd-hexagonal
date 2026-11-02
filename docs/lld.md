# Low-Level Design (LLD)

Detalha cada componente do [HLD](hld.md) no nível de implementação: classes, interfaces, algoritmos, fluxos e o mapeamento para o banco. É a referência para implementar e para conferir se o código segue o desenho. As decisões por trás deste desenho estão nas [ADRs](adr/README.md).

## 1. Modelo de domínio (C4 nível 4, Código)

```mermaid
classDiagram
    direction LR

    class Order {
        <<Aggregate Root>>
        +MAX_DISTINCT_PRODUCTS = 10
        -OrderId id
        -CustomerId customerId
        -OrderStatus status
        -Coupon coupon
        -List~OrderItem~ items
        -List~DomainEvent~ events
        +create(CustomerId) Order$
        +restore(...) Order$
        +addItem(ProductId, Money, Quantity)
        +applyCoupon(Coupon)
        +place()
        +cancel(String reason)
        +subtotal() Money
        +discount() Money
        +total() Money
        +pullEvents() List~DomainEvent~
    }

    class OrderItem {
        <<Entity>>
        -ProductId productId
        -Money unitPrice
        -Quantity quantity
        ~increase(Quantity)
        +subtotal() Money
    }

    class Product {
        <<Aggregate Root>>
        -ProductId id
        -String name
        -Money price
        -boolean active
        +register(String, Money) Product$
        +changePrice(Money)
        +deactivate()
    }

    class Coupon {
        <<Value Object>>
        +String code
        +int percentage
    }

    class Money {
        <<Value Object>>
        +BigDecimal amount
        +plus(Money) Money
        +minus(Money) Money
        +times(int) Money
        +percentage(int) Money
    }

    class Quantity {
        <<Value Object>>
        +int value
        +plus(Quantity) Quantity
    }

    class OrderStatus {
        <<enumeration>>
        DRAFT
        PLACED
        CANCELLED
    }

    class ProductOrderingService {
        <<Domain Service>>
        +addToOrder(Order, Product, Quantity)
    }

    class OrderRepository {
        <<interface>>
        +save(Order) Order
        +findById(OrderId) Optional~Order~
    }

    class ProductRepository {
        <<interface>>
        +save(Product) Product
        +findById(ProductId) Optional~Product~
        +findAll() List~Product~
        +searchByName(String) List~Product~
    }

    class CouponCatalog {
        <<interface>>
        +findByCode(String) Optional~Coupon~
    }

    Order "1" *-- "0..10" OrderItem
    Order --> OrderStatus
    Order --> "0..1" Coupon
    OrderItem --> Money
    OrderItem --> Quantity
    Product --> Money
    ProductOrderingService ..> Order
    ProductOrderingService ..> Product
    OrderRepository ..> Order
    ProductRepository ..> Product
    CouponCatalog ..> Coupon
```

`OrderId`, `ProductId` e `CustomerId` são objetos de valor (records) que embrulham o identificador e se validam na criação. Os eventos `OrderCreated`, `OrderPlaced` e `OrderCancelled` são records que implementam `DomainEvent`.

## 2. Ciclo de vida do pedido

```mermaid
stateDiagram-v2
    [*] --> DRAFT: create(customerId)
    DRAFT --> DRAFT: addItem / applyCoupon
    DRAFT --> PLACED: place() com ao menos 1 item
    DRAFT --> CANCELLED: cancel(motivo)
    PLACED --> CANCELLED: cancel(motivo)
    CANCELLED --> [*]
    PLACED --> [*]
```

| Operação | Pré-condição | Efeito | Evento |
|---|---|---|---|
| `create` | `customerId` preenchido | pedido em `DRAFT`, sem itens | `OrderCreated` |
| `addItem` | `DRAFT`; produto ativo; quantidade final de 1 a 99; até 10 produtos diferentes | novo item ou quantidade somada ao item existente, com o preço original | |
| `applyCoupon` | `DRAFT`; ao menos 1 item; sem cupom anterior | guarda código e percentual | |
| `place` | `DRAFT`; ao menos 1 item | `PLACED` | `OrderPlaced` (com o total e o número de itens) |
| `cancel` | não estar `CANCELLED`; motivo preenchido | `CANCELLED` | `OrderCancelled` (com o motivo) |

Toda pré-condição violada lança `DomainException`, que a API traduz em `422`.

## 3. Interfaces (portas)

| Porta | Tipo | Assinatura | Implementação |
|---|---|---|---|
| `CreateOrder` | entrada | `OrderId create(String customerId)` | `OrderApplicationService` |
| `AddItemToOrder` | entrada | `Order addItem(OrderId, UUID productId, int quantity)` | `OrderApplicationService` |
| `ApplyCoupon` | entrada | `Order applyCoupon(OrderId, String couponCode)` | `OrderApplicationService` |
| `PlaceOrder` | entrada | `Order place(OrderId)` | `OrderApplicationService` |
| `CancelOrder` | entrada | `Order cancel(OrderId, String reason)` | `OrderApplicationService` |
| `GetOrder` | entrada | `Order get(OrderId)` | `OrderApplicationService` |
| `RegisterProduct` | entrada | `Product register(String name, BigDecimal price)` | `CatalogApplicationService` |
| `DeactivateProduct` | entrada | `void deactivate(UUID productId)` | `CatalogApplicationService` |
| `ListProducts` | entrada | `List<Product> list()` | `CatalogApplicationService` |
| `SearchProducts` | entrada | `List<Product> search(String term)` | `CatalogApplicationService` |
| `OrderRepository` | saída | `save`, `findById` | `JpaOrderRepositoryAdapter` |
| `ProductRepository` | saída | `save`, `findById`, `findAll`, `searchByName` | `JpaProductRepositoryAdapter` |
| `CouponCatalog` | saída | `Optional<Coupon> findByCode(String code)` | `InMemoryCouponCatalog` |
| `DomainEventPublisher` | saída | `void publish(List<DomainEvent> events)` | `SpringDomainEventPublisher` |

## 4. Algoritmos

**Totais do pedido**

```text
subtotal = Σ (preço unitário × quantidade) de cada item
desconto = subtotal × percentual do cupom ÷ 100   (0 sem cupom)
total    = subtotal − desconto
```

`Money` guarda sempre duas casas decimais e arredonda com `HALF_EVEN` (arredondamento bancário). Exemplo de `OrderCouponTest.the_discount_is_rounded_to_cents`: 3 × R$ 33,33 = R$ 99,99; 15% de desconto dá R$ 14,9985, arredondado para R$ 15,00; total de R$ 84,99.

**Normalização de entrada**: o código do cupom passa por `strip()` e `toUpperCase(Locale.ROOT)`, então `bemvindo10` e `BEMVINDO10` são o mesmo cupom. `CustomerId` e o nome do produto também passam por `strip()`.

## 5. Fluxograma: aplicar cupom

```mermaid
flowchart TD
    start([POST /orders/id/coupon]) --> valid{Código só com letras,<br/>números, - e _<br/>até 30 caracteres?}
    valid -- não --> e400[400 Dados inválidos]
    valid -- sim --> load{Pedido existe?}
    load -- não --> e404[404 Não encontrado]
    load -- sim --> known{Cupom existe<br/>no catálogo?}
    known -- não --> e422a[422 Cupom inválido]
    known -- sim --> draft{Pedido em DRAFT?}
    draft -- não --> e422b[422 Só rascunho aceita cupom]
    draft -- sim --> items{Tem itens?}
    items -- não --> e422c[422 Pedido sem itens não aceita cupom]
    items -- sim --> once{Já tem cupom?}
    once -- sim --> e422d[422 Pedido já tem um cupom]
    once -- não --> save[Guarda código e percentual,<br/>salva o pedido]
    save --> ok([200 com subtotal, desconto e total])
```

## 6. Diagramas de sequência

### Adicionar item

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant W as OrderController
    participant S as OrderApplicationService
    participant OR as OrderRepository
    participant PR as ProductRepository
    participant D as ProductOrderingService
    participant O as Order
    participant DB as PostgreSQL

    C->>W: POST /orders/{id}/items {productId, quantity}
    W->>W: valida o corpo (Bean Validation)
    W->>S: addItem(orderId, productId, quantity)
    S->>OR: findById(orderId)
    OR->>DB: SELECT orders + order_items
    DB-->>OR: linhas
    OR-->>S: Order (restore)
    S->>PR: findById(productId)
    PR->>DB: SELECT products
    DB-->>PR: linha
    PR-->>S: Product
    S->>D: addToOrder(order, product, new Quantity(quantity))
    alt produto inativo
        D-->>S: DomainException
        S-->>W: DomainException
        W-->>C: 422 Produto indisponível
    else produto ativo
        D->>O: addItem(productId, price, quantity)
        O-->>D: item novo ou quantidade somada
        S->>OR: save(order)
        OR->>DB: UPDATE orders, regrava order_items
        S-->>W: Order
        W-->>C: 200 OrderResponse
    end
```

### Fechar pedido e publicar o evento

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant W as OrderController
    participant S as OrderApplicationService
    participant OR as OrderRepository
    participant O as Order
    participant P as DomainEventPublisher
    participant L as DomainEventLogger

    C->>W: POST /orders/{id}/place
    W->>S: place(orderId)
    S->>OR: findById(orderId)
    OR-->>S: Order
    S->>O: place()
    O->>O: exige DRAFT e ao menos 1 item
    O->>O: status = PLACED, registra OrderPlaced(total)
    S->>OR: save(order)
    S->>O: pullEvents()
    O-->>S: [OrderPlaced]
    S->>P: publish([OrderPlaced])
    P->>L: ApplicationEvent
    L->>L: log "Evento de domínio: OrderPlaced[...]"
    S-->>W: Order
    W-->>C: 200 OrderResponse (status PLACED)
```

O fluxo de aplicar cupom, no nível de componentes, está na visão dinâmica do [C4](c4/README.md#aplicar-cupom).

## 7. Tratamento de erros

As respostas de erro seguem Problem Details (RFC 9457) e nunca trazem stack trace nem o conteúdo enviado.

| Origem | HTTP | `title` | Observação |
|---|---|---|---|
| `DomainException` (regra violada) | 422 | Regra de negócio violada | `detail` com a mensagem do domínio |
| `NotFoundException` | 404 | Não encontrado | |
| Bean Validation do corpo ou de parâmetro | 400 | Dados inválidos | `erros`: lista de `{campo, erro}` |
| JSON malformado | 400 | Corpo inválido | |
| Parâmetro malformado (rejeitado pelo Tomcat) | 400 | Parâmetros inválidos | |
| Qualquer outra exceção | 500 | Erro interno | registrada no log com o stack trace |

## 8. Persistência

| Domínio | JPA | Tabela |
|---|---|---|
| `Order` | `OrderJpaEntity` | `orders` (`id`, `customer_id`, `status`, `coupon_code`, `coupon_percentage`) |
| `OrderItem` | `OrderItemEmbeddable`, coleção de elementos carregada junto com o pedido | `order_items` (`order_id`, `product_id`, `unit_price`, `quantity`) |
| `Product` | `ProductJpaEntity` | `products` (`id`, `name`, `price`, `active`) |

Os adaptadores convertem nos dois sentidos: `toEntity` monta a entidade JPA a partir do agregado, e `toDomain` reconstrói o agregado com `Order.restore` e `Product.restore`, sem disparar eventos. O modelo entidade-relacionamento está no [DAS](das.md#7-modelo-de-dados).
