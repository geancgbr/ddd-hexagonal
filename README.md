# ddd-hexagonal

Domain-Driven Design (tático) e Arquitetura Hexagonal em uma API de pedidos, com Java 25 e Spring Boot 4.

Lab da **Fase 1** da Pós Tech Software Architecture (FIAP): **Domain Driven Design**, **Arquitetura Hexagonal** e **Qualidade de Software**. O código é original, escrito para praticar os conceitos; não é material do curso. As notas de estudo ficam em [pos-tech-software-architecture](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Domain%20Driven%20Design/Domain%20Driven%20Design.md).

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
- Cupom: um por pedido, só em rascunho com itens, de 1% a 50% sobre o subtotal, arredondado em centavos. Cupons válidos: `BEMVINDO10` e `BLACKFRIDAY25`.

Violação de regra vira `422` com a mensagem do domínio; recurso inexistente vira `404`.

## Qualidade de Software

| Aula | Onde está no projeto |
|---|---|
| [QS 01 – Principais conceitos](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Qualidade%20de%20Software/Aulas/QS%2001%20-%20Principais%20conceitos.md) | **Caixa branca**: `OrderAddItemWhiteBoxTest` (um teste por caminho do código) e `InputValidationWhiteBoxTest`, com cobertura de ramos medida pelo JaCoCo; o build exige 90% de ramos cobertos no domínio. **Caixa preta**: `OrderApiBlackBoxTest`, só entradas e saídas HTTP, com valores-limite de quantidade (0, 1, 99, 100) |
| [QS 02 – Testes de unidade](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Qualidade%20de%20Software/Aulas/QS%2002%20-%20Testes%20de%20unidade.md) | Testes do domínio em JUnit 5 + AssertJ, isolados e rápidos, no formato Arrange / Act / Assert. A aula usa NUnit, xUnit e MSTest (.NET); `@Test` equivale a `[Test]` / `[Fact]` / `[TestMethod]` |
| [QS 03 – TDD](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Qualidade%20de%20Software/Aulas/QS%2003%20-%20Aplicando%20TDD%20na%20pr%C3%A1tica.md) | O **cupom de desconto** foi feito em três ciclos Red-Green-Refactor. Cada etapa é um commit marcado com `[TDD vermelho]`, `[TDD verde]` ou `[TDD refatoração]` no [histórico](https://github.com/geancgbr/ddd-hexagonal/commits/trunk) |
| [QS 04 – Testes de integração](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Qualidade%20de%20Software/Aulas/QS%2004%20-%20Testes%20de%20integra%C3%A7%C3%A3o.md) | Ambiente semelhante ao de produção com **Testcontainers** (PostgreSQL real). Bottom-up: `JpaOrderRepositoryAdapterTest` testa o adaptador de persistência sozinho; depois a API inteira em `OrderApiBlackBoxTest`. Os testes de aplicação usam adaptadores falsos em memória (`support/InMemoryAdapters`) |
| [QS 05 – BDD](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Qualidade%20de%20Software/Aulas/QS%2005%20-%20Trabalhando%20com%20BDD.md) | Cenários em **Gherkin em português** (`src/test/resources/features`), executados com **Cucumber**: especificação viva na linguagem ubíqua. Relatório em `target/cucumber/relatorio.html` |
| [QS 06 – Testes de carga](https://github.com/geancgbr/pos-tech-software-architecture/blob/notas/P%C3%B3s%20Tech%20-%20Software%20Architecture/Fase%201%20-%20Arquitetura%20de%20Software/Qualidade%20de%20Software/Aulas/QS%2006%20-%20Testes%20de%20carga.md) | **k6** com o fluxo de compra completo: `load-tests/carga.js` (uso esperado, p95 < 300 ms e menos de 1% de erros) e `load-tests/estresse.js` (até 500 usuários virtuais, para achar o ponto de degradação) |

```bash
make test       # 61 testes: unidade, caixa branca/preta, integração, BDD, arquitetura e formatação
make coverage   # abre o relatório de cobertura do JaCoCo
make up         # sobe a aplicação
make load       # teste de carga com k6 (roda em container)
make stress     # teste de estresse com k6
```

## Como rodar

Pré-requisitos: Docker e JDK 25.

```bash
make up      # sobe PostgreSQL e a aplicação em http://localhost:8080
make demo    # roteiro completo: catálogo, pedido, regras violadas e eventos
make down    # derruba tudo e apaga os dados
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
| `POST` | `/orders/{id}/coupon` | `{"code": "BEMVINDO10"}` |
| `POST` | `/orders/{id}/place` | |
| `POST` | `/orders/{id}/cancel` | `{"reason": "cliente desistiu"}` |

## Stack

Java 25 · Spring Boot 4.1 · Spring Data JPA · PostgreSQL 18 · Flyway · JUnit 5 · AssertJ · ArchUnit · Testcontainers · Cucumber · JaCoCo · k6 · Spotless (palantir-java-format) · Docker Compose
