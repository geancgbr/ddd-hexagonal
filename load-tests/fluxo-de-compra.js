import http from 'k6/http';
import { check, group } from 'k6';

const API = __ENV.API || 'http://localhost:8080';
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

export function cadastrarProdutos() {
  const produtos = [
    { name: 'Teclado mecânico', price: 349.9 },
    { name: 'Mouse sem fio', price: 129.5 },
    { name: 'Monitor 27"', price: 1899.0 },
  ];
  return produtos.map((produto) => http.post(`${API}/products`, JSON.stringify(produto), JSON_HEADERS).json('id'));
}

export function comprar(produtos) {
  group('fluxo de compra', () => {
    const pedido = http.post(`${API}/orders`, JSON.stringify({ customerId: `cliente-${__VU}` }), JSON_HEADERS);
    check(pedido, { 'pedido criado (201)': (r) => r.status === 201 });
    const id = pedido.json('id');

    produtos.forEach((produtoId) => {
      const item = http.post(
        `${API}/orders/${id}/items`,
        JSON.stringify({ productId: produtoId, quantity: 1 + Math.floor(Math.random() * 3) }),
        JSON_HEADERS,
      );
      check(item, { 'item adicionado (200)': (r) => r.status === 200 });
    });

    const cupom = http.post(`${API}/orders/${id}/coupon`, JSON.stringify({ code: 'BEMVINDO10' }), JSON_HEADERS);
    check(cupom, { 'cupom aplicado (200)': (r) => r.status === 200 });

    const fechado = http.post(`${API}/orders/${id}/place`);
    check(fechado, { 'pedido fechado (200)': (r) => r.status === 200 });

    const consulta = http.get(`${API}/orders/${id}`);
    check(consulta, { 'status PLACED': (r) => r.json('status') === 'PLACED' });
  });
}
