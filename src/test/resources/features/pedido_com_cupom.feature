# language: pt
Funcionalidade: Fechar um pedido com cupom de desconto
  Para pagar menos nas compras
  Como cliente da loja
  Quero aplicar um cupom antes de fechar o pedido

  Contexto:
    Dado que o catálogo tem os produtos:
      | produto | preço  |
      | Teclado | 300,00 |
      | Mouse   | 50,00  |
    E o cupom "BEMVINDO10" dá 10% de desconto

  Cenário: Fechar um pedido com cupom
    Dado que o cliente "ana" abriu um pedido
    Quando adiciona 1 "Teclado" ao pedido
    E adiciona 2 "Mouse" ao pedido
    E aplica o cupom "BEMVINDO10"
    E fecha o pedido
    Então o pedido fica com status "PLACED"
    E o subtotal é R$ 400,00
    E o desconto é R$ 40,00
    E o total é R$ 360,00

  Cenário: Cupom inexistente é recusado
    Dado que o cliente "ana" abriu um pedido
    E adiciona 1 "Mouse" ao pedido
    Quando aplica o cupom "NAOEXISTE"
    Então o pedido é recusado com a mensagem "Cupom inválido: NAOEXISTE"
    E o total é R$ 50,00

  Cenário: Pedido fechado não aceita cupom
    Dado que o cliente "ana" abriu um pedido
    E adiciona 1 "Mouse" ao pedido
    E fecha o pedido
    Quando aplica o cupom "BEMVINDO10"
    Então o pedido é recusado com a mensagem "Só é possível aplicar cupom em um pedido em rascunho (status atual: PLACED)"
