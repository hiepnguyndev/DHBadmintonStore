import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { spawnSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
export async function configuration(test = false) {
  const env = { ...process.env };
  if (!test) {
    for (const line of (await readFile(path.join(root, 'backend/.env'), 'utf8')).split(/\r?\n/)) {
      const match = line.trim().match(/^([A-Za-z_][A-Za-z0-9_]*)=(.*)$/);
      if (match) env[match[1]] = match[2].trim().replace(/^(["'])(.*)\1$/, '$2');
    }
  }
  const prefix = test ? 'TEST_' : '';
  const url = new URL((env[`${prefix}DB_URL`] || (test ? '' : 'jdbc:mysql://localhost:3306/badminton_ecommerce')).replace(/^jdbc:/, ''));
  const config = { host: url.hostname, port: url.port || '3306', database: url.pathname.slice(1),
    user: env[`${prefix}DB_USERNAME`], password: env[`${prefix}DB_PASSWORD`], test };
  if (!['localhost', '127.0.0.1'].includes(config.host) || !/^[a-zA-Z0-9_]+$/.test(config.database) || !config.user)
    throw new Error('A local MySQL database and username are required.');
  if (test && (config.port !== '3307' || !config.database.endsWith('_test')))
    throw new Error('SQL tests require an isolated MySQL on port 3307 and a database ending in _test.');
  if (!test && config.database !== 'badminton_ecommerce') throw new Error('Expected the local badminton_ecommerce database.');
  return config;
}
function runClient(config, program, args, input) {
  const bin = process.env.MYSQL_BIN || 'C:/Program Files/MySQL/MySQL Server 8.0/bin';
  const result = spawnSync(path.join(bin, `${program}.exe`), ['--no-defaults', '--protocol=TCP',
    `--host=${config.host}`, `--port=${config.port}`, `--user=${config.user}`, '--default-character-set=utf8mb4', ...args],
  { input, encoding: 'utf8', windowsHide: true, maxBuffer: 64 * 1024 * 1024,
    env: { ...process.env, MYSQL_PWD: config.password || '' } });
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error((result.stderr || `MySQL exit ${result.status}`).replaceAll(config.password || '\0', '[redacted]'));
  return result.stdout;
}
export const query = (config, sql, selectDatabase = true) => runClient(config, 'mysql',
  ['--batch', '--raw', '--skip-column-names', ...(selectDatabase ? [`--database=${config.database}`] : [])], sql);
export const dump = (config, extra = [], tables = []) => runClient(config, 'mysqldump',
  ['--single-transaction', '--skip-lock-tables', '--no-tablespaces', '--set-gtid-purged=OFF', ...extra, config.database, ...tables]);
export async function backup(config) {
  const directory = path.join(root, 'backend/var/backups');
  await mkdir(directory, { recursive: true });
  const filename = path.join(directory, `before-shopvnb-${new Date().toISOString().replace(/[:.]/g, '-')}.sql`);
  await writeFile(filename, dump(config, ['--routines', '--triggers']), 'utf8');
  return filename;
}
export const inspect = config => query(config, `SELECT JSON_OBJECT('products', (SELECT COUNT(*) FROM products),
  'users', (SELECT COUNT(*) FROM users), 'admins', (SELECT COUNT(*) FROM users WHERE role='ADMIN'),
  'orders', (SELECT COUNT(*) FROM orders), 'posts', (SELECT COUNT(*) FROM shop_posts),
  'schemaVersion', (SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1));`);

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    const config = await configuration(process.argv.includes('--test'));
    const action = process.argv[2] || 'inspect';
    if (action === 'inspect') console.log(inspect(config));
    else if (action === 'backup') console.log(await backup(config));
    else if (action === 'import') {
      console.log('Backup:', await backup(config));
      const protectedData = () => createHash('sha256').update(query(config,
        'SELECT * FROM users ORDER BY user_id; SELECT * FROM orders ORDER BY order_id; SELECT * FROM shop_posts ORDER BY post_id;')).digest('hex');
      const before = protectedData();
      console.log(query(config, await readFile(path.join(root, 'database/04_shopvnb_200.sql'), 'utf8')));
      if (before !== protectedData()) throw new Error('Accounts/orders/posts changed during import. Check for concurrent edits and compare the backup.');
      console.log('Verified: accounts, passwords, roles, orders and shop posts unchanged.');
      console.log(inspect(config));
    } else throw new Error('Usage: node scripts/database.mjs inspect|backup|import [--test]');
  } catch (error) { console.error(error.message); process.exitCode = 1; }
}
