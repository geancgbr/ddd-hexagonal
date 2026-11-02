# ADR-0010: Documentação de arquitetura como código, verificada no build

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

Documentação em ferramentas de desenho ou em wikis desatualiza sem ninguém perceber: o código muda e o diagrama fica para trás. Diagramas soltos, desenhados à mão, também repetem os mesmos elementos em cada desenho, e um nome corrigido num lugar continua errado nos outros.

## Decisão

- **C4 em Structurizr DSL** (`docs/c4/workspace.dsl`): um único modelo gera as visões de Contexto, Contêineres, Componentes, Implantação e o fluxo dinâmico. `make c4` valida o modelo e exporta as imagens; `make c4-local` abre o modelo no navegador.
- **Contrato OpenAPI gerado pelo springdoc** e versionado em `docs/api/openapi.json`. O teste `OpenApiContractTest` compara o arquivo com o contrato que a aplicação publica e quebra o build se divergirem; `make openapi` atualiza o arquivo.
- **Mermaid** nos documentos em Markdown (sequência, classes, estados, modelo de dados), renderizado pelo próprio GitHub. `make docs-check` renderiza cada diagrama para pegar erro de sintaxe.
- **Requisitos com rastreabilidade**: cada requisito aponta para o teste que o comprova ([requisitos.md](../requisitos.md)).

## Alternativas consideradas

- **draw.io ou ferramenta de desenho**: bom resultado visual, mas cada mudança é manual e o arquivo não mostra diferenças legíveis num code review.
- **PlantUML com C4-PlantUML**: também é texto, mas o GitHub não renderiza PlantUML, e cada visão continua sendo um desenho separado, sem um modelo único.
- **Swagger UI publicada na aplicação**: mostra o contrato, mas não detecta mudança não intencional e aumenta a superfície exposta.

## Consequências

- Diagrama e contrato mudam no mesmo commit que o código, e a revisão vê a diferença em texto.
- Uma mudança de contrato não intencional quebra o build em vez de chegar ao cliente.
- O Structurizr roda em container (Docker) e não precisa de instalação local.
- A coerência entre o modelo C4 e o código ainda depende de revisão: nenhum teste compara os componentes do DSL com as classes.
