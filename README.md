# ddd-hexagonal

Domain-Driven Design (tático) e Arquitetura Hexagonal em uma API de pedidos, com Java 25 e Spring Boot 4.

Lab da disciplina **Domain Driven Design** e da aula **Arquitetura Hexagonal** (Fase 1 da Pós Tech Software Architecture, FIAP). O código é original, escrito para praticar os conceitos; não é material do curso. As notas de estudo ficam em [pos-tech-software-architecture](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Domain%20Driven%20Design/Domain%20Driven%20Design.md).

## O que o lab mostra

| Conceito | Onde está no código | Nota |
|---|---|---|
| Objeto de Valor | `Money`, `Quantity`, `OrderId`, `CustomerId`: records imutáveis, iguais por valor, que se validam na criação | [Objeto de Valor](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Objeto%20de%20Valor.md) |
| Entidade | `OrderItem` (identidade dentro do pedido) e `Product` | [Entidade](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Entidade.md) |
| Agregado | `Order` é a raiz: itens só mudam por ela, e as regras valem sempre | [Agregado](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Agregado.md) |
| Serviço de Domínio | `ProductOrderingService`: a regra envolve dois agregados (`Product` e `Order`) | [Serviço de Domínio](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Servi%C3%A7o%20de%20Dom%C3%ADnio.md) |
| Evento de Domínio | `OrderCreated`, `OrderPlaced`, `OrderCancelled`, publicados depois de salvar | [Evento de Domínio](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Evento%20de%20Dom%C3%ADnio.md) |
| Repositório | `OrderRepository` e `ProductRepository` são do domínio; a implementação JPA é um adaptador | [Camadas do DDD](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Camadas%20do%20DDD.md) |
| Portas e Adaptadores | portas de entrada em `application.port.in`, de saída em `application.port.out` e nos repositórios; adaptadores em `adapter.in.web` e `adapter.out` | [Arquitetura Hexagonal](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Arquitetura%20Hexagonal.md) |
| Inversão de Controle | domínio e aplicação não usam Spring; `config.UseCaseConfig` liga as implementações | [ARQ 05](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Arquitetura%20de%20Software/Aulas/ARQ%2005%20-%20Arquitetura%20Hexagonal.md) |
| Contexto Delimitado | um contexto (Vendas); `Product` ficaria num contexto de Catálogo num sistema real | [Contexto Delimitado](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Conceitos/Contexto%20Delimitado.md) |

## Arquitetura

```mermaid
flowchart LR
    client([Cliente HTTP]) --> web

    subgraph adapter_in [Adaptador de entrada]
        web[OrderController / ProductController]
    end

    subgraph application [Aplicação]
        ports_in{{Portas de entrada<br/>CreateOrder, PlaceOrder...}}
        services[OrderApplicationService<br/>CatalogApplicationService]
        port_events{{DomainEventPublisher}}
    end

    subgraph domain [Domínio]
        order[Order + OrderItem]
        product[Product]
        ordering[ProductOrderingService]
        repos{{OrderRepository<br/>ProductRepository}}
    end

    subgraph adapter_out [Adaptadores de saída]
        jpa[JPA + PostgreSQL]
        events[Eventos do Spring]
    end

    web --> ports_in --> services
    services --> order & product & ordering
    services --> repos
    services --> port_events
    jpa -. implementa .-> repos
    events -. implementa .-> port_events
```

As dependências apontam sempre para o centro. O teste `HexagonalArchitectureTest` (ArchUnit) quebra o build se o domínio ou a aplicação passarem a depender de Spring, JPA ou de um adaptador.

```
src/main/java/io/github/geancgbr/orders
├── domain
│   ├── shared        Money, Quantity, DomainEvent, DomainException
│   ├── order         Order, OrderItem, eventos, OrderRepository, ProductOrderingService
│   └── product       Product, ProductRepository
├── application
│   ├── port/in       casos de uso
│   ├── port/out      DomainEventPublisher
│   └── service       OrderApplicationService, CatalogApplicationService
├── adapter
│   ├── in/web        REST e tratamento de erros (Problem Details)
│   └── out           persistence (JPA) e events (Spring)
└── config            ligação das portas com as implementações
```

## Regras de negócio

- Um pedido nasce como rascunho (`DRAFT`), é fechado (`PLACED`) ou cancelado (`CANCELLED`).
- Só um rascunho aceita itens; no máximo 10 produtos diferentes por pedido.
- Adicionar o mesmo produto de novo soma a quantidade e mantém o preço do primeiro registro.
- Quantidade entre 1 e 99; valores monetários não podem ser negativos.
- Produto inativo não entra no pedido.
- Pedido sem itens não pode ser fechado; cancelamento exige motivo e acontece uma vez.

Violação de regra vira `422` com a mensagem do domínio; recurso inexistente vira `404`.

## Como rodar

Pré-requisitos: Docker e JDK 25.

```bash
make up      # sobe PostgreSQL e a aplicação em http://localhost:8080
make demo    # roteiro completo: catálogo, pedido, regras violadas e eventos
make down    # derruba tudo e apaga os dados
```

Testes (domínio, aplicação, arquitetura e integração com PostgreSQL via Testcontainers) e verificação de formatação:

```bash
make test
```

## API

| Método | Caminho | Corpo |
|---|---|---|
| `POST` | `/products` | `{"name": "Teclado", "price": 349.90}` |
| `GET` | `/products` | |
| `POST` | `/products/{id}/deactivate` | |
| `POST` | `/orders` | `{"customerId": "cliente-42"}` |
| `GET` | `/orders/{id}` | |
| `POST` | `/orders/{id}/items` | `{"productId": "...", "quantity": 2}` |
| `POST` | `/orders/{id}/place` | |
| `POST` | `/orders/{id}/cancel` | `{"reason": "cliente desistiu"}` |

## Stack

Java 25 · Spring Boot 4.1 · Spring Data JPA · PostgreSQL 18 · Flyway · JUnit 5 · AssertJ · ArchUnit · Testcontainers · Spotless (palantir-java-format) · Docker Compose
