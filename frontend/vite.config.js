import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig({
  base: process.env.VITE_BASE_PATH || '/',
  plugins: [react()],
  // Keep Rollup's tree-shaker from exhausting memory on the full page graph.
  build: { rollupOptions: { treeshake: false } },
  server: {
    port: 5173,
    strictPort: true,
    proxy: { '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true } },
    // Permit the configured public hostname when Vite is exposed through Cloudflare Tunnel.
    allowedHosts: ['ngdhiep-fithau.id.vn'],
  },
  test: { environment: 'jsdom', setupFiles: './src/test/setup.js', exclude: ['e2e/**', 'node_modules/**'] },
});
