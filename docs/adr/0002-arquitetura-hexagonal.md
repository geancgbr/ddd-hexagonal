# ADR-0002: Arquitetura hexagonal com o domínio livre de framework

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

As regras de pedido (estados, limites de itens, cupom, totais) são o núcleo do sistema e mudam por motivos de negócio. Banco, HTTP e mensageria mudam por motivos técnicos. Quando as regras dependem do framework, cada teste de regra precisa subir o Spring e o banco, e trocar uma tecnologia obriga a mexer no domínio.

## Decisão

O código segue portas e adaptadores:

- `domain` tem as regras, em Java puro, com os repositórios como interfaces (`OrderRepository`, `ProductRepository`, `CouponCatalog`).
- `application` tem os casos de uso: portas de entrada em `port.in`, porta de saída `DomainEventPublisher` em `port.out` e os serviços que as implementam.
- `adapter.in.web` e `adapter.out.*` implementam as portas com Spring MVC, JPA e eventos do Spring.
- `config.UseCaseConfig` liga as interfaces às implementações (inversão de controle).

O teste `HexagonalArchitectureTest` (ArchUnit) quebra o build se `domain` ou `application` passarem a depender de Spring, JPA ou de um adaptador.

## Alternativas consideradas

- **Camadas tradicionais com as entidades JPA no domínio**: menos classes, mas o agregado passa a precisar de construtor vazio e campos mutáveis para o Hibernate, o que enfraquece as invariantes, e os testes de regra passam a depender do contexto de persistência.
- **Clean Architecture completa** (um caso de uso por classe, presenters, modelos de entrada e saída por camada): mais separação do que um contexto deste tamanho precisa.

## Consequências

- Os testes de domínio rodam sem Spring, em milissegundos, e a cobertura de ramos do domínio chega a 100%.
- Trocar o banco ou acrescentar uma entrada (fila, CLI) é escrever um adaptador novo, sem tocar nas regras.
- Há mapeamento duplicado: `OrderJpaEntity` espelha `Order`, e o agregado precisa de um `restore` para ser reconstruído a partir do banco.
