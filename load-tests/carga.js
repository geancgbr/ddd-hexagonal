import { sleep } from 'k6';
import { cadastrarProdutos, comprar } from './fluxo-de-compra.js';

export const options = {
  stages: [
    { duration: '15s', target: 20 },
    { duration: '45s', target: 20 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300'],
    checks: ['rate>0.99'],
  },
};

export function setup() {
  return cadastrarProdutos();
}

export default function (produtos) {
  comprar(produtos);
  sleep(1);
}
