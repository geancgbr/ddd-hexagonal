# ADR-0001: Registrar as decisões de arquitetura com ADRs

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

O lab cresce a cada disciplina e acumula escolhas que não aparecem no código: por que o domínio não usa JPA, por que o CSRF está desligado, por que os cupons não ficam no banco. Sem registro, quem lê o projeto depois (inclusive o próprio autor) não sabe o que foi decidido de propósito e o que foi acaso, e pode desfazer uma decisão sem conhecer o motivo.

## Decisão

Cada decisão que afete a estrutura, os requisitos não funcionais, as dependências entre componentes, as interfaces ou as ferramentas de construção vira uma ADR em `docs/adr`, numerada em sequência, no formato de [template.md](template.md).

Uma ADR nasce como **Proposta**. Depois de revisada, vira **Aceita** ou **Rejeitada**, com o motivo registrado. Aceita, ela não muda mais: uma decisão nova gera outra ADR, e a anterior passa a **Substituída**, com o link para a nova, ou a **Depreciada**, quando deixa de fazer sentido sem sucessora.

Ficam de fora decisões triviais ou temporárias, como atualizar uma dependência menor; para elas basta a mensagem do commit.

## Alternativas consideradas

- **Só o README**: mistura o "como usar" com o "por que foi feito assim" e perde o histórico quando o texto é reescrito.
- **Wiki ou Confluence**: fica longe do código e não passa pela mesma revisão dos commits.

## Consequências

- As decisões ficam versionadas junto do código, e o índice em [README.md](README.md) dá o contexto rápido só pelos títulos.
- O code review pode conferir se uma mudança respeita as ADRs aceitas.
- Cada decisão relevante custa alguns minutos de escrita.
