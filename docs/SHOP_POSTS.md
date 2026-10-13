# Shop Posts In Community And News

Requested on 2026-10-13: include content published by the shop in the existing
Community & News section, alongside Marketplace and curated Yonex articles.

## Workflow

- ADMIN opens `/admin/posts` (sidebar: "Bài viết"). Create/edit title, summary,
  plain-text content, category (shop news or community) and optional cover image.
- "Lưu bản nháp" stores content without public exposure. "Đăng bài" explicitly
  publishes it. Saving edits to a published post keeps it published.
- The latest three published shop posts appear first in the homepage community
  section. Existing Marketplace and Yonex entries remain present.
- `/news` lists all published shop posts, with category filter and pagination.
  `/news/{id}` is the full article. Public navigation includes "Tin tức".
- Move to draft or archive requires confirmation and removes the public article.
  Archived posts remain recoverable through the admin status filter; no hard delete.

## Storage And API

- Flyway V11 creates `shop_posts`, with category/status constraints, publication
  index and UTC timestamps. Main local schema was migrated to V11 by the API
  restart. No reset, account change, imported post or automatic publication.
- `GET /api/posts?category=NEWS&page=0&size=12` and `GET /api/posts/{id}` are public.
  Only PUBLISHED posts are visible, including when an admin reads the public API.
  Missing, draft and archived article IDs return 404.
- `GET /api/admin/posts?keyword=...&status=DRAFT&page=0&size=12` is ADMIN-only.
  Without a status filter it returns drafts/published posts; ARCHIVED is explicit.
- `POST /api/admin/posts` and `PUT /api/admin/posts/{id}` accept multipart data:
  JSON part `post` contains title, summary, content, category NEWS/COMMUNITY,
  status DRAFT/PUBLISHED. Optional file part `cover` replaces the cover.
- `DELETE /api/admin/posts/{id}` archives and returns the updated post.
- Title/summary/content limits: 180/400/30000 characters. MEDIUMTEXT supports the
  full Vietnamese content limit. Categories and statuses are validated server-side.
- Content is rendered as escaped plain text with preserved line breaks, never
  executable HTML. Author-entered content is not rewritten by UI localization.
- Cover upload uses existing ImageStorage validation (real PNG/JPEG, 1 byte to
  5 MB, at most 24 megapixels), UUID public media paths and rollback cleanup.
  Post and cover are saved in one transaction, with product-style row locking.
  Uploaded cover images are public media, not confidential draft attachments.
  No file uses the original client filename. Old cover assets are retained.
- Missing cover uses the existing court photo; no artificial shop posts are added.
- First publication timestamp is retained through edits/draft/restore. Public
  ordering is publication date then ID; admin ordering is last update then ID.

## Verification

- 48 frontend unit tests passed, including six new shop-post cases.
- 114 backend tests passed with zero failures/errors/skips on an isolated MySQL
  server at 127.0.0.1:3307. Covers public visibility, admin permissions, multipart
  binding/validation, publish/edit/draft/archive/restore, search/category/status,
  filtering before pagination, bad-cover safeguards and long Vietnamese content.
- 23 focused Playwright cases passed (shop posts, home and responsive regression).
  New workflow tests run at 1440px, 390px and 360px and verify draft exclusion,
  cover upload, publication on home, full article, escaped HTML, edits, removal,
  cancellation and retry. No horizontal overflow, broken cover or page errors.
  A final four-case rerun also verifies the compact DH Chatbot launcher does not
  overlap editor inputs/actions on any of these viewports.
  Evidence: `docs/evidence/shop-posts-admin-*.png` and `shop-posts-article-*.png`.
- Production frontend build passed with the existing >500 kB main-chunk warning
  (509.07 kB before gzip).
- Live read-only API checks: public posts 200, anonymous admin 401, absent article
  404. Current shop-post catalog is empty until the admin publishes a real post.
- Isolated QA MySQL stopped. Main MySQL 3306, Vite 5173 and API 8080 remain running.

Admin: `http://localhost:5173/admin/posts`. Public: `http://localhost:5173/news`.
