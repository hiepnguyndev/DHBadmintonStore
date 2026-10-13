-- Colors and sizes now have dedicated selectors. Remove the legacy copy from
-- existing imported descriptions, while retaining genuine product specifications.
UPDATE products p
JOIN catalog_import_sources i ON i.product_id = p.product_id
SET p.description = TRIM(CASE
    WHEN JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.mau_sac')) > 0
     AND JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.kich_co')) > 0
    THEN SUBSTRING(p.description, LOCATE('\n', p.description,
        LOCATE('\n', p.description, LOCATE('\n', p.description) + 1) + 1) + 1)
    WHEN JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.mau_sac')) > 0
      OR JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.kich_co')) > 0
    THEN SUBSTRING(p.description, LOCATE('\n', p.description, LOCATE('\n', p.description) + 1) + 1)
    ELSE SUBSTRING(p.description, LOCATE('\n', p.description) + 1)
END)
WHERE p.description IS NOT NULL;
