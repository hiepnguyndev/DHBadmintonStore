-- Legacy imported copy may contain mojibake, so remove the known three-line
-- source header by line position rather than matching Vietnamese characters.
UPDATE products p
JOIN catalog_import_sources i ON i.product_id = p.product_id
SET p.description = CASE
    WHEN LOCATE('\n', p.description,
        LOCATE('\n', p.description, LOCATE('\n', p.description) + 1) + 1) > 0
    THEN SUBSTRING(p.description, LOCATE('\n', p.description,
        LOCATE('\n', p.description, LOCATE('\n', p.description) + 1) + 1) + 1)
    ELSE ''
END
WHERE p.description LIKE '%shopvnb.com/%';
