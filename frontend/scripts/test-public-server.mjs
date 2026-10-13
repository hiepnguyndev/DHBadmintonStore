import assert from 'node:assert/strict';
const base = 'http://127.0.0.1:8787';
for (const route of ['/', '/products', '/orders/42']) {
  const res = await fetch(base + route, { headers: { Accept: 'text/html' } });
  assert.equal(res.status, 200, route);
  assert.match(await res.text(), /id="root"/);
  assert.equal(res.headers.get('x-content-type-options'), 'nosniff');
}
for (const route of ['/.env', '/backend/.env', '/.git/config', '/database/05_project_complete.sql', '/src/App.jsx', '/swagger-ui/index.html', '/assets/missing.js']) {
  assert.equal((await fetch(base + route)).status, 404, route);
}
const products = await fetch(base + '/api/products');
assert.equal(products.status, 200);
assert.equal((await products.json()).success, true);
assert.equal((await fetch(base + '/api/orders/1/payment')).status, 401);
assert.equal((await fetch(base + '/api/admin/orders')).status, 401);
const callback = await fetch(base + '/api/payments/callbacks/vnpay?vnp_ResponseCode=00');
assert.equal((await callback.json()).RspCode, '97');
console.log('PASS: SPA routes, security headers, private files blocked, API proxy, auth and unsigned callback.');
