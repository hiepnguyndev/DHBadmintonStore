# Homepage Offers And Community

Requested on 2026-10-13: replace the homepage profile-promotion band with offers
and add Community & News below. DH Chatbot's saved-profile flow is unchanged.

## Behavior

- Homepage offers use GET /api/products?onSale=true&page=0&size=4.
- onSale defaults to false, preserving the ordinary catalog. When true, the
  database filters promotionPublished = true AND salePrice IS NOT NULL AND
  salePrice >= 0 AND salePrice < price before pagination. Only offers explicitly
  published by an admin are included; a discounted price alone does not publish.
  Existing ACTIVE/OUT_OF_STOCK public visibility remains unchanged.
- The complete offers page uses the same filter with 12 items per page, removing
  the previous first-100-items client-side filtering limit.
- Cards display the server's effectivePrice, original price and a percentage
  based on the actual price difference, floored rather than overstated.
  Sub-one-percent discounts show an offer label instead of "-0%".
- Empty/error/loading states are real; retry reloads only the affected query.
  There are no demo offers or database imports.
- Community & News follows the existing catalog/service links. It contains a
  Marketplace entry and two attributed Yonex previews with publication dates,
  locally hosted source photos and external article links.
- External Yonex news remains curated static content, not an automatically
  refreshed feed. The subsequent shop-post request adds admin-published articles
  ahead of these entries; see [SHOP_POSTS.md](SHOP_POSTS.md).
  External sources and rights notes: [REDESIGN_ASSETS.md](REDESIGN_ASSETS.md).

## Verification

- Vitest: 35 passed, including six home/offer tests.
- Playwright: 38 passed (nine home/offer/accessibility cases plus the existing
  chat/profile/responsive regressions), zero failures. Verified at 1440px,
  390px and 360px; no horizontal overflow, broken images or JavaScript errors.
- Backend: 95 tests passed, zero failures/errors/skips, using a fresh isolated
  MySQL server on 127.0.0.1:3307. The new tests cover public/default/invalid
  filter parameters and SQL filtering before pagination, equal/NULL/zero sale
  prices, hidden products, OUT_OF_STOCK visibility and effective prices.
- Production build passed with the existing non-blocking bundle-size warning
  (main chunk 505.57 kB before gzip).
- Browser evidence: home-offers-*.png and home-community-*.png at desktop and
  mobile widths; tests exercise offer navigation/pagination, retry, empty
  catalog, image loading, localization and retained DH Chatbot profile controls.
- Live unmocked smoke on desktop/mobile confirmed an honest empty offers section
  for the current catalog, three community/news entries and working local image
  assets. Evidence: home-live-*.png and home-community-live-*.png.
- The restarted API returns HTTP 200 for onSale=true and HTTP 400 for an invalid
  boolean flag, confirming the new filter is loaded. Existing MySQL 3306,
  frontend 5173 and API 8080 remain available; temporary QA MySQL was stopped.

The initial homepage implementation needed no main-database write/reset, account
change or schema migration. The subsequent admin-publishing request adds migration
V10 (no reset), as described in [ADMIN_PROMOTIONS.md](ADMIN_PROMOTIONS.md).
