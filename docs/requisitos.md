# Requisitos

Cada requisito tem um identificador, uma prioridade e o teste ou a ferramenta que comprova o atendimento. Quando a regra muda, o teste muda junto, e a tabela aponta para ele.

Prioridade: **Alta** é indispensável para a API cumprir o objetivo; **Média** melhora a experiência ou a operação, mas a API funciona sem ela.

[Testes de domínio]: ../src/test/java/io/github/geancgbr/orders/domain
[Testes de API]: ../src/test/java/io/github/geancgbr/orders/adapter
[Cenários BDD]: ../src/test/resources/features

## Requisitos funcionais

| ID | Requisito | Prioridade | Verificado por |
|---|---|---|---|
| RF-01 | Cadastrar produto com nome e preço maior ou igual a zero, com até duas casas decimais | Alta | `CatalogApiBlackBoxTest.registers_searches_and_deactivates_a_product` |
| RF-02 | Listar os produtos e buscar por parte do nome, sem diferenciar maiúsculas de minúsculas | Média | `CatalogApiBlackBoxTest.registers_searches_and_deactivates_a_product`, `SecurityBlackBoxTest.sql_injection_in_the_search_is_treated_as_plain_text` |
| RF-03 | Desativar produto; produto inativo não entra em pedido | Alta | `CatalogApiBlackBoxTest.registers_searches_and_deactivates_a_product`, `ProductOrderingServiceTest.an_inactive_product_cannot_be_ordered`, cenário "Produto fora de linha não entra no pedido" |
| RF-04 | Abrir pedido para um cliente; o pedido nasce como rascunho (`DRAFT`) | Alta | `OrderTest.a_new_order_starts_as_draft_and_announces_its_creation` |
| RF-05 | Adicionar item ao rascunho com o preço do catálogo. Quantidade de 1 a 99; até 10 produtos diferentes; o mesmo produto de novo soma a quantidade e mantém o preço do primeiro registro | Alta | `ProductOrderingServiceTest.the_item_enters_the_order_with_the_catalog_price`, `OrderTest.adding_the_same_product_again_increases_quantity_and_keeps_the_original_price`, `OrderTest.an_order_accepts_at_most_ten_distinct_products`, `OrderApiBlackBoxTest.quantity_boundary_values`, esquema "Limites de quantidade por item" |
| RF-06 | Aplicar um cupom por pedido, só em rascunho com itens. O código não diferencia maiúsculas; o desconto vai de 1% a 50% do subtotal, arredondado em centavos | Média | `OrderCouponTest`, `CouponTest`, `OrderApplicationServiceTest.an_unknown_coupon_is_a_business_error`, [pedido_com_cupom.feature](../src/test/resources/features/pedido_com_cupom.feature) |
| RF-07 | Fechar o pedido: só rascunho com itens; o fechamento anuncia o total com desconto | Alta | `OrderTest.an_empty_order_cannot_be_placed`, `OrderTest.placing_an_order_freezes_its_items_and_publishes_the_total`, `OrderCouponTest.the_placed_event_carries_the_total_with_discount` |
| RF-08 | Cancelar o pedido com um motivo, uma vez só | Média | `OrderTest.cancelling_requires_a_reason_and_happens_only_once` |
| RF-09 | Consultar o pedido com itens, cupom, subtotal, desconto e total; pedido inexistente responde 404 | Alta | `OrderApiBlackBoxTest.creates_fills_and_places_an_order`, `OrderApiBlackBoxTest.unknown_order_is_404` |
| RF-10 | Publicar cada evento de domínio (`OrderCreated`, `OrderPlaced`, `OrderCancelled`) uma vez, depois de salvar o pedido | Média | `OrderApplicationServiceTest.full_flow_saves_the_order_and_publishes_each_event_once` |

Os testes ficam em [Testes de domínio], [Testes de API] e [Cenários BDD].

## Requisitos não funcionais

| ID | Atributo | Requisito e métrica | Verificado por | Resultado medido |
|---|---|---|---|---|
| RNF-01 | Desempenho | Fluxo de compra completo com 20 usuários simultâneos: p95 abaixo de 300 ms e menos de 1% de erros | `make load` (limites em `load-tests/carga.js`) | p95 de 4,39 ms, 0% de erros |
| RNF-02 | Escalabilidade | Até 500 usuários virtuais: p95 abaixo de 2 s e menos de 5% de erros | `make stress` (limites em `load-tests/estresse.js`) | p95 de 14,36 ms, 0% de erros |
| RNF-03 | Segurança | Texto livre e códigos validados por lista de caracteres permitidos e tamanho máximo; entrada inválida responde 400 sem devolver o conteúdo enviado; consultas sempre parametrizadas | `SecurityBlackBoxTest` | 12 casos aprovados |
| RNF-04 | Segurança | Nenhuma dependência com vulnerabilidade HIGH ou CRITICAL | `make deps` (SBOM CycloneDX + Trivy) | 0 vulnerabilidades em 99 componentes |
| RNF-05 | Segurança | Análise estática sem achados abertos e varredura dinâmica sem alertas | SpotBugs + FindSecBugs no `make test`, `make sonar`, `make dast` | Quality Gate aprovado com nota A; ZAP com 120 verificações aprovadas e 0 alertas |
| RNF-06 | Segurança | O usuário da aplicação não cria, altera nem apaga tabelas | `make privileges` | `CREATE`, `ALTER` e `DROP` negados ao `orders_app` |
| RNF-07 | Manutenibilidade | Domínio e aplicação sem dependência de Spring, JPA ou adaptadores; ao menos 90% dos ramos do domínio cobertos; formatação padronizada | `HexagonalArchitectureTest` (ArchUnit), JaCoCo e Spotless no `make test` | 100% dos ramos do domínio |
| RNF-08 | Integridade dos dados | Esquema versionado e só validado pelo Hibernate; limites de negócio repetidos como restrições no banco | Flyway, `spring.jpa.hibernate.ddl-auto=validate`, `JpaOrderRepositoryAdapterTest` | Restrições `CHECK` de preço, quantidade e percentual do cupom |
| RNF-09 | Observabilidade | Verificação de saúde disponível; erro inesperado registrado no log e devolvido como 500 genérico; eventos de domínio registrados | `/actuator/health`, `SecurityBlackBoxTest.actuator_exposes_only_health_and_info` | Actuator expõe só `health` e `info` |
| RNF-10 | Portabilidade | A aplicação e o banco sobem com um comando em qualquer máquina com Docker, com o processo da aplicação sem root | `make up`, `Dockerfile` | Imagem com usuário `app` (uid 1001) |
| RNF-11 | Documentação | O contrato OpenAPI versionado no repositório é igual ao que a aplicação publica | `OpenApiContractTest` | Falha o build se divergir |

## Fora do escopo

| Atributo | Situação | Registro |
|---|---|---|
| Autenticação e autorização | A API não identifica quem chama | [ADR-0009](adr/0009-autenticacao-oauth2-jwt.md), com status Proposta |
| Disponibilidade | Uma instância da aplicação e do banco, sem réplica | [DAS, plano de continuidade](das.md#10-plano-de-continuidade-e-backup) |
| Criptografia em trânsito | Sem TLS na aplicação; ficaria no proxy de entrada | [DAS, segurança e compliance](das.md#11-planos-de-segurança-e-compliance) |

## Restrições

- Java 25 e Spring Boot 4, a mesma base dos Hands On do curso.
- PostgreSQL como banco relacional ([ADR-0003](adr/0003-postgresql-com-flyway.md)).
- Execução local com Docker Compose; não há ambiente em nuvem.
