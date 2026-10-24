import { readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { root, dump, query } from './database.mjs';

export async function buildBundle(config) {
  if (!config.test || config.port !== '3307') throw new Error('Export only from an isolated, freshly migrated test database.');
  if (query(config, 'SELECT (SELECT COUNT(*) FROM users) + (SELECT COUNT(*) FROM products) + (SELECT COUNT(*) FROM orders);').trim() !== '0')
    throw new Error('Schema export requires an empty application database.');
  const structure = dump(config, ['--no-data', '--skip-add-drop-table', '--skip-comments', '--skip-triggers'])
    .replace(/ AUTO_INCREMENT=\d+/g, '');
  const defaults = dump(config, ['--no-create-info', '--skip-add-locks', '--skip-disable-keys', '--skip-comments', '--complete-insert'],
    ['flyway_schema_history', 'recommendation_weights']);
  const version = query(config, 'SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;').trim();
  if (!/^[0-9]+$/.test(version)) throw new Error('Invalid schema version');
  const schema = `-- Generated from a clean database migrated by Flyway V1-V${version}.
-- MySQL 8.0+. Select an EMPTY database first. Never run this to upgrade an existing database.
-- Includes real Flyway checksums and default recommendation weights; no accounts or passwords.
SET NAMES utf8mb4;
DELIMITER $$
CREATE PROCEDURE dh_install_schema_v${version}()
BEGIN
DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
    SET FOREIGN_KEY_CHECKS = 1;
    RESIGNAL;
END;
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE()) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Database must be empty. Existing installations: start backend, then run 04_shopvnb_200.sql';
END IF;
${structure}
${defaults}
END$$
CALL dh_install_schema_v${version}()$$
DROP PROCEDURE dh_install_schema_v${version}$$
DELIMITER ;
`;
  await writeFile(path.join(root, 'database/03_schema_complete.sql'), schema, 'utf8');
  const create = await readFile(path.join(root, 'database/00_create_database.sql'), 'utf8');
  const products = await readFile(path.join(root, 'database/04_shopvnb_200.sql'), 'utf8');
  await writeFile(path.join(root, 'database/05_project_complete.sql'),
    '-- FRESH INSTALL ONLY. Existing database: use backend Flyway + 04_shopvnb_200.sql.\n' + create + '\n' + schema + '\n' + products, 'utf8');
}
