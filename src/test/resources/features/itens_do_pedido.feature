# language: pt
Funcionalidade: Montar o pedido
  Para comprar o que preciso
  Como cliente da loja
  Quero adicionar produtos ao meu pedido

  Contexto:
    Dado que o catálogo tem os produtos:
      | produto | preço |
      | Mouse   | 50,00 |

  Cenário: Produto fora de linha não entra no pedido
    Dado que o produto "Mouse" saiu de linha
    E que o cliente "ana" abriu um pedido
    Quando adiciona 1 "Mouse" ao pedido
    Então o pedido é recusado com a mensagem "Produto indisponível: Mouse"

  Esquema do Cenário: Limites de quantidade por item
    Dado que o cliente "ana" abriu um pedido
    Quando adiciona <quantidade> "Mouse" ao pedido
    Então o resultado é "<resultado>"

    Exemplos:
      | quantidade | resultado |
      | 1          | aceito    |
      | 99         | aceito    |
      | 100        | recusado  |
