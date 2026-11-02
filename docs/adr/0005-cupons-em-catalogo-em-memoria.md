# ADR-0005: Cupons num catálogo em memória, atrás de uma porta

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

O desconto por cupom precisava validar o código e conhecer o percentual. Existem dois cupons (`BEMVINDO10` e `BLACKFRIDAY25`), não há requisito para cadastrá-los pela API e não existe um sistema de promoções para consultar.

## Decisão

O domínio define a porta `CouponCatalog` (`findByCode`), e o adaptador `InMemoryCouponCatalog` guarda os cupons vigentes. Ao aplicar o cupom, o pedido grava o código **e o percentual** (colunas `coupon_code` e `coupon_percentage`): o pedido guarda uma cópia do desconto que valia naquele momento.

## Alternativas consideradas

- **Tabela de cupons com cadastro pela API**: resolve um requisito que ainda não existe e traz migração, validações e endpoints a mais.
- **Consultar um serviço de promoções**: não há esse sistema; seria um elemento externo inventado no diagrama de contexto.

## Consequências

- Implementação mínima, sem tabela nem endpoint extra.
- Mudar o percentual de um cupom não altera pedidos antigos, porque cada pedido guarda o seu.
- Incluir ou alterar um cupom exige um novo deploy. Quando isso incomodar, a troca é um adaptador novo (banco ou serviço externo), sem mexer no domínio.
