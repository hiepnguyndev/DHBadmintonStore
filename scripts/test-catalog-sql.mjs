import assert from 'node:assert/strict';
import { mkdtemp, readFile, writeFile, mkdir } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import net from 'node:net';
import { spawn, spawnSync } from 'node:child_process';
import { once } from 'node:events';
import { randomBytes } from 'node:crypto';
import { root, query } from './database.mjs';
import { buildBundle } from './build-sql-bundle.mjs';

const bin = process.env.MYSQL_BIN || 'C:/Program Files/MySQL/MySQL Server 8.0/bin';
const executable = path.join(bin, 'mysqld.exe');
const config = { host: '127.0.0.1', port: '3307', database: 'dh_catalog_test', user: 'root', password: '', test: true };
const log = message => console.log(message);
const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
const classpath = (await readFile(path.join(root, 'backend/target/admin-tool.classpath'), 'utf8')).trim();
const free = await new Promise(resolve => {
  const socket = net.connect({ host: config.host, port: 3307 });
  socket.once('connect', () => { socket.destroy(); resolve(false); });
  socket.once('error', () => resolve(true));
});
assert.ok(free, 'Port 3307 must be free; never reuse another MySQL server.');
const directory = await mkdtemp(path.join(tmpdir(), 'dh-catalog-sql-'));
const init = spawnSync(executable, ['--no-defaults', '--initialize-insecure', `--datadir=${directory}`],
  { encoding: 'utf8', windowsHide: true });
assert.equal(init.status, 0, init.stderr);
const server = spawn(executable, ['--no-defaults', `--datadir=${directory}`, '--port=3307', '--bind-address=127.0.0.1',
  '--mysqlx=0', '--skip-log-bin', '--innodb-buffer-pool-size=64M'], { windowsHide: true, stdio: 'ignore' });
const stopped = once(server, 'exit');
const java = (database = config.database) => {
  const result = spawnSync('java', ['-cp', classpath, 'scripts/CatalogSchemaCheck.java'], {
    cwd: root, encoding: 'utf8', windowsHide: true, env: { ...process.env,
      TEST_DB_URL: `jdbc:mysql://127.0.0.1:3307/${database}?connectionTimeZone=UTC`,
      TEST_DB_USERNAME: config.user, TEST_DB_PASSWORD: config.password }, maxBuffer: 8 * 1024 * 1024 });
  assert.equal(result.status, 0, result.stdout + result.stderr);
};
try {
  let ready = false;
  for (let attempt = 0; attempt < 60; attempt++) {
    try { query(config, 'SELECT 1;', false); ready = true; break; } catch { await pause(500); }
  }
  assert.ok(ready, 'Isolated MySQL failed to start.');
  const password = randomBytes(24).toString('hex');
  query(config, `ALTER USER 'root'@'localhost' IDENTIFIED BY '${password}';
    CREATE DATABASE dh_catalog_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;`, false);
  config.password = password;
  java();
  await buildBundle(config);
  log('PASS: all Flyway migrations and complete schema export.');
  const sql = await readFile(path.join(root, 'database/04_shopvnb_200.sql'), 'utf8');
  // A failure late in the import must roll back products, categories, brands and provenance together.
  query(config, "CREATE TRIGGER reject_import_image BEFORE INSERT ON product_images FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Injected import failure';");
  assert.throws(() => query(config, sql), /Injected import failure/);
  assert.equal(query(config, 'SELECT (SELECT COUNT(*) FROM products)+(SELECT COUNT(*) FROM brands)+(SELECT COUNT(*) FROM categories)+(SELECT COUNT(*) FROM catalog_import_sources);').trim(), '0');
  query(config, 'DROP TRIGGER reject_import_image;');
  log('PASS: import failure rolls back all catalog writes.');
  query(config, sql);
  assert.equal(query(config, `SELECT COUNT(*), SUM(stock), SUM(stock=20), SUM(status='ACTIVE'), SUM(status='INACTIVE'),
    SUM(sale_price IS NOT NULL), SUM(promotion_published) FROM products;`).trim(), '200\t4000\t200\t198\t2\t0\t0');
  assert.equal(query(config, `SELECT (SELECT COUNT(*) FROM catalog_import_sources), (SELECT COUNT(*) FROM product_images),
    (SELECT COUNT(*) FROM brands), (SELECT COUNT(*) FROM categories), (SELECT COUNT(*) FROM product_variants),
    (SELECT COUNT(*) FROM racket_specifications);`).trim(), '200\t191\t14\t4\t0\t0');
  assert.equal(query(config, 'SELECT COUNT(DISTINCT product_id), SUM(is_primary) FROM product_images;').trim(), '190\t190');
  const source = JSON.parse(await readFile(path.join(root, '../CSDL/san_pham_shopvnb_200.json'), 'utf8'));
  const records = query(config, 'SELECT raw_record FROM catalog_import_sources ORDER BY source_url;').trim().split('\n').map(line => JSON.parse(line));
  const byUrl = new Map(source.map(item => [item.url_nguon, item]));
  for (const record of records) assert.deepEqual(record, byUrl.get(record.url_nguon));
  assert.equal(query(config, "SELECT COUNT(*) FROM products WHERE price=0 AND status='INACTIVE' AND stock=20;").trim(), '2');
  log('PASS: 200 products, 4000 units, 198 active, 2 awaiting prices, 191 images on 190 products; original JSON preserved.');
  const edited = query(config, "SELECT MIN(product_id) FROM products WHERE status='ACTIVE';").trim();
  query(config, `UPDATE products SET stock=17, price=900000, sale_price=800000, promotion_published=TRUE,
    description='Admin description', thumbnail_url='https://example.com/admin.jpg' WHERE product_id=${edited};
    UPDATE product_images SET image_url='https://example.com/admin.jpg' WHERE product_id=${edited} AND is_primary=TRUE;`);
  query(config, sql);
  assert.equal(query(config, `SELECT stock,price,sale_price,promotion_published,description,thumbnail_url FROM products WHERE product_id=${edited};`).trim(),
    '17\t900000.00\t800000.00\t1\tAdmin description\thttps://example.com/admin.jpg');
  assert.equal(query(config, 'SELECT (SELECT COUNT(*) FROM products),(SELECT COUNT(*) FROM catalog_import_sources),(SELECT COUNT(*) FROM product_images);').trim(), '200\t200\t191');
  log('PASS: repeat import does not duplicate data or overwrite admin edits.');
  const schema = await readFile(path.join(root, 'database/03_schema_complete.sql'), 'utf8');
  assert.throws(() => query(config, schema), /Database must be empty/);
  assert.equal(query(config, 'SELECT COUNT(*) FROM products;').trim(), '200');
  query(config, 'CREATE DATABASE dh_restore_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;', false);
  const restored = { ...config, database: 'dh_restore_test' };
  query(restored, schema);
  java(restored.database);
  query(restored, sql);
  assert.equal(query(restored, 'SELECT COUNT(*), SUM(stock) FROM products;').trim(), '200\t4000');
  log('PASS: consolidated schema restores and validates against all Flyway checksums; rejects nonempty database.');
  query(config, await readFile(path.join(root, 'database/05_project_complete.sql'), 'utf8'), false);
  const complete = { ...config, database: 'badminton_ecommerce' };
  assert.equal(query(complete, 'SELECT COUNT(*), SUM(stock) FROM products;').trim(), '200\t4000');
  log('PASS: full SQL installs schema and 200 products from scratch.');
  if (process.argv.includes('--backend-tests')) {
    query(config, 'CREATE DATABASE dh_integration_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;', false);
    const output = spawnSync('cmd.exe', ['/d', '/c', 'mvn test'], { cwd: path.join(root, 'backend'), windowsHide: true,
      encoding: 'utf8', maxBuffer: 16 * 1024 * 1024, env: { ...process.env, RUN_MYSQL_TESTS: 'true',
        TEST_DB_URL: 'jdbc:mysql://127.0.0.1:3307/dh_integration_test?connectionTimeZone=UTC',
        TEST_DB_USERNAME: config.user, TEST_DB_PASSWORD: config.password, TEST_JWT_SECRET: randomBytes(48).toString('hex') } });
    await mkdir(path.join(root, 'backend/var'), { recursive: true });
    await writeFile(path.join(root, 'backend/var/catalog-backend-tests.log'), output.stdout + output.stderr, 'utf8');
    assert.equal(output.status, 0, 'Backend tests failed; see backend/var/catalog-backend-tests.log');
    log(output.stdout.match(/Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+\s*\r?\n\[INFO\]\s*\r?\n\[INFO\] -+/)?.[0] || 'PASS: backend tests.');
  }
} finally {
  try { query(config, 'SHUTDOWN;', false); } catch { server.kill(); }
  await stopped;
  log(`Isolated MySQL stopped. Test data: ${directory}`);
}
