# ADR-0008: API stateless, sem proteção CSRF e com cabeçalhos de segurança

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

O Spring Security liga a proteção contra CSRF por padrão, e o SonarQube (regra `java:S4502`) e o FindSecBugs (`SPRING_CSRF_PROTECTION_DISABLED`) acusam quando ela é desligada. O ataque de CSRF depende de o navegador enviar sozinho uma credencial guardada em cookie. Esta API não usa sessão nem cookie, e só responde JSON.

## Decisão

O `SecurityConfig` desliga o CSRF, cria a sessão como `STATELESS`, não habilita login por formulário nem HTTP Basic e envia os cabeçalhos `Content-Security-Policy: default-src 'none'; frame-ancestors 'none'`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer` e `Cross-Origin-Resource-Policy: same-origin`. Os dois achados de análise estática ficam aceitos com justificativa (`@SuppressWarnings("java:S4502")` e `spotbugs-exclude.xml`).

## Alternativas consideradas

- **Manter o CSRF ligado**: todo `POST` passaria a exigir um token, sem proteger nada, porque não há cookie para o atacante aproveitar.

## Consequências

- Clientes de API chamam os endpoints sem token anti-CSRF.
- Se a autenticação um dia usar cookie, esta decisão deixa de valer e precisa ser revista. A proposta de autenticação ([ADR-0009](0009-autenticacao-oauth2-jwt.md)) usa token no cabeçalho `Authorization` e mantém a API stateless.
