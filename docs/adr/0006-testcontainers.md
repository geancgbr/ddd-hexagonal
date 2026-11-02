# ADR-0006: Testes de integração com Testcontainers

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

O adaptador de persistência e a API precisam ser testados contra o banco real ([ADR-0003](0003-postgresql-com-flyway.md)): as migrações, as restrições `CHECK` e a busca por nome sem diferenciar maiúsculas, gerada pelo Spring Data, só se comportam como em produção num PostgreSQL.

## Decisão

Os testes de integração sobem um PostgreSQL 18 descartável com Testcontainers (`TestcontainersConfiguration`, com `@ServiceConnection`). O Flyway roda as migrações nele antes dos testes. Os testes de domínio e de aplicação continuam sem banco, com adaptadores em memória (`support/InMemoryAdapters`).

## Alternativas consideradas

- **H2 em modo compatível com PostgreSQL**: não executa todas as construções do PostgreSQL e esconde diferenças que só apareceriam em produção.
- **Um banco de testes compartilhado**: os testes passam a depender de estado deixado por outras execuções e não rodam em paralelo com segurança.

## Consequências

- O mesmo banco e as mesmas migrações de produção são exercitados em todo `make test`.
- Rodar os testes exige Docker, e o primeiro teste de integração espera alguns segundos pelo container.
- O contexto do Spring é reaproveitado entre as classes de teste, então os testes não podem depender de um banco vazio.
