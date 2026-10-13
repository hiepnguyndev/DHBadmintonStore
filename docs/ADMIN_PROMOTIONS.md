# Admin-Published Offers

Requested on 2026-10-13: let the admin choose products and discount levels before
publishing them to the homepage and complete offers page.

## Admin Workflow

1. Sign in as ADMIN and open `/admin/promotions` (sidebar: "Ưu đãi").
2. Search/select a product. The list includes inactive products for visibility,
   but publishing is disabled/rejected for inactive products or zero base prices.
3. Choose a percentage or an exact selling price. Review the calculated price
   and savings, then use "Đăng ưu đãi". Existing published offers use "Cập nhật ưu đãi".
4. "Kết thúc ưu đãi" requires confirmation, removes publication and clears the
   sale price, restoring the base price for future purchases.

The product-management page links to this workspace. Choosing a product or changing
an input never publishes automatically. Search and publication filters are server
paginated (12 products per page), not limited to the first catalog page.

## Storage And API

- Flyway V10 adds `products.promotion_published BOOLEAN NOT NULL DEFAULT FALSE`
  and a publication/status index. Existing sale prices remain unchanged and are
  not automatically published. Do not change already-applied migrations.
- `GET /api/admin/products?keyword=...&published=true&page=0&size=12`: ADMIN-only
  catalog for offer selection; omit `published` to include all products.
- `PUT /api/admin/products/{id}/promotion`: ADMIN-only publish/update with JSON
  `{"discountType":"PERCENT","value":20}` or
  `{"discountType":"SALE_PRICE","value":1200000}`.
- `DELETE /api/admin/products/{id}/promotion`: ADMIN-only, idempotently end an
  offer and restore the base price. Returns the updated product, as does PUT.
- Product responses include `promotionPublished`. Public catalog contracts remain
  compatible; `onSale=true` now means admin-published, valid discounted products.
- Percentage must be greater than 0 and at most 100, with up to two decimals.
  Exact selling price must be nonnegative and below the current base price.
  The server locks the product, calculates against its authoritative price and
  rounds HALF_UP to two decimals; discounts that round back to base price fail.
- Pricing still uses the existing `salePrice` / `effectivePrice` path for product
  details, cart, checkout and recommendations. No display-only fake discount.
  Existing order price snapshots are unchanged when an offer is ended.
- Ordinary product edits cannot publish a previously unpublished product. Removing
  a discount, setting sale price equal to base price or disabling a product clears
  its publication flag. Valid price edits to an already-published offer retain it.
- A sale price entered through product editing still affects actual selling
  price, but only the explicit publishing command adds it to the offers sections.

No reset, user/account modification, automatic publication or demo product insertion
is performed. News and DH Chatbot behavior are unchanged.

## Verification

- Vitest: 42 passed, including seven new admin-offer UI cases.
- Backend: 105 passed, zero failures/errors/skips, with all write-based integration
  tests on a fresh isolated MySQL server at 127.0.0.1:3307. Covers authorization,
  request validation, authoritative prices/rounding, publish/update/end, search,
  publication filtering before pagination, hidden and unpublished products,
  product-edit/disable safeguards and existing commerce/chat/profile regressions.
- Playwright: 42 passed with API fixtures. New workflows run at 1440px, 390px
  and 360px; include explicit selection, inactive products, percentage/exact
  price, homepage publication, filters, update, cancel/end and failed-save retry.
  Screenshots: `docs/evidence/admin-offers-{1440,390,360}.png`.
- Production frontend build passed. Existing non-blocking >500 kB main-chunk
  warning remains (507.05 kB before gzip).
- Main API restarted; Flyway successfully applied V10 without resetting data.
  Live read-only checks: public offers HTTP 200 (current catalog empty), anonymous
  admin requests HTTP 401. Main MySQL 3306, Vite 5173 and API 8080 remain running;
  the temporary QA MySQL server was stopped after testing.

Local admin URL: `http://localhost:5173/admin/promotions`.
