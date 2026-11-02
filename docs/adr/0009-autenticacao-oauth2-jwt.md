# ADR-0009: Autenticação com OAuth2 Resource Server e JWT

- **Data:** 01/11/2026
- **Status:** Proposta
- **Dono:** Gean Ribeiro

## Contexto

A API não identifica quem chama. Qualquer cliente cadastra e desativa produtos e cancela pedidos de outra pessoa, e o `customerId` vem no corpo da requisição, informado pelo próprio cliente. No OWASP Top 10, isso cai em A01 (Broken Access Control) e A07 (Identification and Authentication Failures), hoje fora do escopo do lab.

## Decisão

Proposta: transformar a aplicação em um OAuth2 Resource Server do Spring Security, que valida JWTs emitidos por um provedor de identidade externo (Keycloak no Docker Compose para desenvolvimento).

- O `customerId` passa a vir da claim `sub` do token, e não mais do corpo.
- Os escopos separam os papéis do diagrama de contexto: `orders:write` para o Cliente e `catalog:admin` para o Gestor do catálogo.
- Cada operação sobre um pedido confere se o pedido pertence ao `sub` do token.

## Alternativas consideradas

- **Chave de API**: identifica o sistema que chama, não a pessoa, e não resolve o dono do pedido.
- **Sessão com cookie**: contraria a API stateless e reabre o risco de CSRF ([ADR-0008](0008-api-stateless-sem-csrf.md)).
- **Login próprio com senha no banco do lab**: põe na aplicação a responsabilidade de guardar senhas, que o provedor de identidade já resolve.

## Consequências

- Fecha A01 e A07 e tira do cliente o poder de dizer quem ele é.
- Mais um container para subir e operar (o provedor de identidade).
- Os testes de API passam a precisar de tokens (`spring-security-test` gera JWTs falsos).
- O contrato muda: `POST /orders` deixa de receber `customerId`. É uma quebra de compatibilidade que pede uma nova versão da API.

Para virar Aceita, falta implementar uma prova de conceito e revisar o impacto nos testes de carga e no DAST.
