-- District is retained only for legacy rows. New addresses use province, ward and detail_address.
ALTER TABLE addresses MODIFY COLUMN district VARCHAR(100) NOT NULL DEFAULT '';
