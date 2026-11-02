# ADR-0004: Eventos de domínio publicados em processo, depois de salvar

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

Criar, fechar e cancelar um pedido são fatos que outras partes do negócio vão querer saber (faturamento, estoque, notificação). Hoje nenhum consumidor existe, mas o domínio já precisa anunciar esses fatos sem conhecer quem vai reagir.

## Decisão

O agregado `Order` acumula os eventos (`OrderCreated`, `OrderPlaced`, `OrderCancelled`). O serviço de aplicação salva o pedido e só então publica os eventos pela porta `DomainEventPublisher`. O adaptador atual usa o `ApplicationEventPublisher` do Spring, e o único ouvinte (`DomainEventLogger`) registra cada evento no log.

## Alternativas consideradas

- **Publicar num broker (Kafka, RabbitMQ)**: não há consumidor fora da aplicação que justifique mais um componente para operar.
- **Outbox transacional**: grava o evento na mesma transação do pedido e garante a entrega. Passa a valer a pena quando surgir um consumidor externo.

## Consequências

- Uma reação nova a um evento é um ouvinte novo, sem alterar o agregado nem o serviço.
- A publicação não faz parte da gravação: se o processo cair entre salvar e publicar, o evento se perde. Aceitável enquanto o evento só vai para o log; com um consumidor externo, esta ADR deve ser substituída por uma que adote o outbox.
