import express from 'express';
import helmet from 'helmet';
import { rateLimit } from 'express-rate-limit';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const dist = fileURLToPath(new URL('../dist/', import.meta.url));
if (!existsSync(path.join(dist, 'index.html'))) throw new Error('Run npm run build first.');
const app = express();
app.disable('x-powered-by');
// Only loopback cloudflared may reach this listener. Do not trust client-supplied forwarding headers.
app.use((req, res, next) => {
  delete req.headers.forwarded;
  delete req.headers['x-forwarded-for'];
  delete req.headers['x-forwarded-host'];
  delete req.headers['x-forwarded-proto'];
  next();
});
app.use(helmet({ contentSecurityPolicy: false, strictTransportSecurity: false,
  crossOriginEmbedderPolicy: false, referrerPolicy: { policy: 'strict-origin-when-cross-origin' } }));
const limiter = rateLimit({ windowMs: 60_000, limit: 300, standardHeaders: 'draft-8', legacyHeaders: false });
const loginLimiter = rateLimit({ windowMs: 60_000, limit: 15, standardHeaders: 'draft-8', legacyHeaders: false });
// A shared limit avoids trusting spoofed IP headers; suitable for a small demonstration deployment.
app.use((req, res, next) => {
  if (/^\/api\/payments\/callbacks\/(vnpay|momo|zalopay)$/.test(req.path)) return next();
  if (/^\/api\/(auth\/(login|register)|ai\/chat)$/.test(req.path)) return loginLimiter(req, res, next);
  if (req.path.startsWith('/api/')) return limiter(req, res, next);
  next();
});
app.use(createProxyMiddleware({
  target: 'http://127.0.0.1:8080', changeOrigin: true,
  pathFilter: pathname => pathname.startsWith('/api/') || pathname.startsWith('/media/'),
  proxyTimeout: 45_000, timeout: 50_000,
  on: { error: (_err, _req, res) => { if (!res.headersSent) res.writeHead(502, { 'Content-Type': 'application/json' }); res.end('{"message":"Backend unavailable"}'); } },
}));
app.use((req, res, next) => {
  let pathname;
  try { pathname = decodeURIComponent(req.path); } catch { return res.sendStatus(400); }
  if (pathname.split('/').some(part => part.startsWith('.')) || /\.(map|sql|ps1|java|ya?ml|toml)$/i.test(pathname)
      || /^\/(swagger-ui|v3|actuator|src|backend|database|scripts)(\/|$)/.test(pathname)) return res.sendStatus(404);
  next();
});
app.use(express.static(dist, { dotfiles: 'deny', index: false, setHeaders(res, file) {
  res.setHeader('Cache-Control', file.includes(`${path.sep}assets${path.sep}`) ? 'public, max-age=31536000, immutable' : 'no-cache');
} }));
app.use((req, res) => {
  if (!['GET', 'HEAD'].includes(req.method) || path.extname(req.path) || !req.accepts('html')) return res.sendStatus(404);
  res.setHeader('Cache-Control', 'no-store');
  res.sendFile(path.join(dist, 'index.html'));
});
const server = app.listen(8787, '127.0.0.1', () => console.log('Public build ready: http://127.0.0.1:8787 (loopback only)'));
server.on('error', error => { console.error(error.code); process.exit(1); });
