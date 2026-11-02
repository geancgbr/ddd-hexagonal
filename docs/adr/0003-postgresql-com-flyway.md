# ADR-0003: PostgreSQL com o esquema versionado pelo Flyway

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

Pedidos, itens e produtos são dados relacionais, com integridade referencial e valores monetários exatos. O esquema vai evoluir (o cupom, por exemplo, chegou depois), e cada mudança precisa ser reproduzível em qualquer ambiente.

## Decisão

Usar PostgreSQL 18. O esquema é criado e alterado só por migrações SQL versionadas do Flyway (`src/main/resources/db/migration`), e o Hibernate roda com `ddl-auto=validate`: ele confere se as entidades batem com o banco, mas nunca altera o esquema.

Os limites de negócio também viram restrições no banco (`CHECK` de preço, de quantidade e do percentual do cupom), como segunda linha de defesa.

## Alternativas consideradas

- **H2 em memória**: rápido para começar, mas tem dialeto e comportamento diferentes do banco real.
- **`ddl-auto=update`**: não deixa histórico, não remove nada e pode alterar o banco de produção sem revisão.
- **Liquibase**: equivalente para este caso. O Flyway foi escolhido porque as migrações são SQL puro, sem uma camada XML ou YAML por cima.

## Consequências

- Qualquer ambiente chega ao mesmo esquema rodando as migrações em ordem.
- Uma divergência entre entidade e tabela derruba a aplicação na subida, em vez de aparecer como erro em produção.
- As migrações não se desfazem sozinhas: corrigir um erro exige uma migração nova.
- Os testes de integração precisam de um PostgreSQL de verdade ([ADR-0006](0006-testcontainers.md)).
