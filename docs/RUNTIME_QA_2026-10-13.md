# Runtime QA - 2026-10-13

## Fixes

- Header search now uses React Router navigation instead of a full document
  reload, preserving the intentionally memory-only login session.
- Product filters follow URL changes, repeated searches and browser history.
  Applied filters are reflected in the URL and restored in the form.
- Hibernate binds Java time values directly through JDBC. This prevents UTC
  LocalDateTime values from being shifted by the Windows/JVM timezone when
  stored in MySQL DATETIME columns. The old configuration stored times seven
  hours earlier on this machine and misread SQL-written expiry timestamps.
  See [Hibernate 6.6 MappingSettings](https://docs.jboss.org/hibernate/orm/6.6/javadocs/org/hibernate/cfg/MappingSettings.html#JAVA_TIME_USE_DIRECT_JDBC).
- Added timestamp/expiry and authenticated search regression coverage.
- Updated obsolete browser selectors and replaced the removed demo catalog
  dependency with API-created, uniquely named test fixtures.
- Updated the catalog SQL test for the current cleanup-only script, including
  idempotence, removal of unused demos and preservation of order history.
  Catalog visibility assertions use unique names so reruns do not conflict.

## Results

| Check | Result |
| --- | --- |
| Frontend production build | Passed |
| Frontend unit tests | 10 passed |
| Backend tests with MySQL integration enabled | 67 passed; none skipped |
| Playwright suite with real-API tests enabled | 13 passed; none skipped |
| Real browser visual check | 1440x1000 and 390x1000; no overflow, broken images or JavaScript errors |
| Main frontend | HTTP 200 at http://localhost:5173 |
| Main product/category/brand APIs | HTTP 200 at http://localhost:8080/api |

The browser suite includes ten mocked responsive UI tests and three real-API
workflows: registration/profile/addresses/logout, recommendations/favorites/
checkout/cancellation, and Marketplace valuation history.

Visual evidence: `docs/evidence/qa-search-1440.png` and
`docs/evidence/qa-search-390.png`. Products shown there are test-only fixtures
without uploaded photos; their intentional image placeholders render correctly.

## Isolation

All write-based tests ran on a temporary MySQL server bound to 127.0.0.1:3307.
Backend integration tests used `badminton_ecommerce_test`; real browser tests
used `badminton_ecommerce_e2e`, API port 8081 and frontend port 5174.
Test credentials were generated in process memory and were not saved to source.

No write or reset command was run against the main `badminton_ecommerce` schema
on port 3306. Exact final COUNT(*) queries confirmed all 31 application tables
still contain zero rows, including users. No main-site admin was created.

The main backend was restarted to load the time-binding fix. The frontend on
5173 and backend on 8080 remain available. Temporary QA services were stopped.

## Repeating The Tests

Backend integration tests require RUN_MYSQL_TESTS=true, TEST_DB_URL,
TEST_DB_USERNAME, TEST_DB_PASSWORD and TEST_JWT_SECRET, pointing to a dedicated
test schema. Run `mvn test` from `backend`.

For real browser tests, use a separate backend/frontend connected to an isolated
test schema. Set RUN_E2E=true, E2E_BASE_URL, E2E_API_URL, E2E_ADMIN_EMAIL and
E2E_ADMIN_PASSWORD for an admin in that schema. Run `npx playwright test` from
`frontend`. The commerce and valuation suites create their own catalog fixtures;
do not import the retired demo seed. PLAYWRIGHT_EXECUTABLE_PATH may select an
installed Chromium browser when the bundled browser is unavailable.

Do not point write-based tests at a live application database.
