import { sleep } from 'k6';
import { cadastrarProdutos, comprar } from './fluxo-de-compra.js';

export const options = {
  stages: [
    { duration: '20s', target: 50 },
    { duration: '20s', target: 150 },
    { duration: '20s', target: 300 },
    { duration: '20s', target: 500 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000'],
  },
};

export function setup() {
  return cadastrarProdutos();
}

export default function (produtos) {
  comprar(produtos);
  sleep(0.5);
}
