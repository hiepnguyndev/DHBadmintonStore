ALTER TABLE orders ADD COLUMN inventory_committed BOOLEAN NOT NULL DEFAULT FALSE;

-- Previous versions reserved inventory at checkout. Return it for orders that were never paid.
UPDATE product_variants v
JOIN (
  SELECT oi.variant_id, SUM(oi.quantity) AS quantity
  FROM order_items oi JOIN orders o ON o.order_id = oi.order_id
  WHERE oi.variant_id IS NOT NULL AND o.payment_status NOT IN ('PAID', 'REFUND_PENDING')
  GROUP BY oi.variant_id
) pending ON pending.variant_id = v.variant_id
SET v.stock = v.stock + pending.quantity,
    v.status = IF(v.status = 'OUT_OF_STOCK', 'ACTIVE', v.status);

UPDATE products p
JOIN (
  SELECT oi.product_id, SUM(oi.quantity) AS quantity
  FROM order_items oi JOIN orders o ON o.order_id = oi.order_id
  WHERE o.payment_status NOT IN ('PAID', 'REFUND_PENDING')
  GROUP BY oi.product_id
) pending ON pending.product_id = p.product_id
SET p.stock = p.stock + pending.quantity,
    p.status = IF(p.status = 'OUT_OF_STOCK', 'ACTIVE', p.status);

UPDATE products p
JOIN (SELECT product_id, SUM(stock) AS stock FROM product_variants GROUP BY product_id) v
  ON v.product_id = p.product_id
SET p.stock = v.stock,
    p.status = IF(p.status = 'INACTIVE', 'INACTIVE', IF(v.stock = 0, 'OUT_OF_STOCK', 'ACTIVE'));

UPDATE orders
SET inventory_committed = payment_status IN ('PAID', 'REFUND_PENDING');
