# DH Chatbot Integration

Requested on 2026-10-13: replace the DH Match branding with a working chatbot
and place its launcher in the lower-right corner of the website.

## Scope

- A single modeless, responsive chat panel is mounted inside the router. It stays
  available across storefront and admin routes. Home, navigation and profile
  actions open the same panel instead of redirecting to recommendations.
- Profile-based recommendations are integrated into the same chat panel.
  The standalone `/recommendations` route remains for existing deep links.
  MatchScoreCalculator and all backend matching rules remain unchanged.
- The local rule engine is adapted from DH_Badminton_Local_Chatbot_MVP, not its
  demo catalog or unapproved `/demo` endpoint. No SaaS keys or trained LLM are
  involved. The response explicitly identifies its engine as `LOCAL_RULES`.
- No schema changes, data reset, seed imports, account changes or purchase actions.
  Requesting profile recommendations retains the existing recommendation_logs
  writes; the public chat endpoint itself does not write to the database.

## Saved Player Profile

The panel is now named **DH Chatbot** in Vietnamese and English. Its
"Goi y tu ho so" action and the profile entry points on home, shop and account
pages request `GET /api/recommendations?limit=3` using the current JWT.
The backend resolves the current user and their saved profile. No user ID,
profile fields or personalized results are sent to the public chat endpoint.
Typed requests such as "Goi y tu ho so" also use the authenticated workflow.

Results show the backend's product, effective price, matchScore, reasons and
cautions, with links to product details. There is no frontend score calculation
or demo-product fallback. Missing profiles offer profile setup; guests or expired
sessions offer sign-in. HTTP 403 explains the existing USER-role restriction
without extending ADMIN/INSPECTOR permissions.

Saving on the player-profile page opens recommendations in chat instead of navigating
to the old result page. The profile is reloaded after save so a subsequent edit
uses PUT rather than attempting another POST. Profile requests support retry,
reset/abort and account-change cleanup. Profile messages/results never enter the
public chat history. Pending external profile triggers are discarded when the
account changes and consumed only once when the panel is reopened.

## API

`POST /api/ai/chat` is public, using the existing ApiResponse/error envelopes and
CORS policy. Other API authentication and method permissions are unchanged.

Request: `message` (nonblank, at most 1000 characters), optional `history` (at
most 10 nonblank user messages, each at most 1000 characters), optional
`language` (`vi` or `en`). Unknown JSON properties are rejected by the existing
Jackson policy.

Response: `resultKind` (`NEED_INPUT`, `NO_MATCH`, `RECOMMENDATIONS`), `answer`,
`products` (at most 3 standard ProductResponse objects), `engine: LOCAL_RULES`.

Conversation context is reconstructed from bounded client-supplied user history.
The server stores no conversation sessions and reads no user profiles or order
history. The browser stores chat only in component memory, clears it when the
account changes, and never persists messages to localStorage or MySQL.

The single-instance endpoint allows 30 requests per minute per remote address,
with at most 4096 active address windows. Forwarded IP headers are not trusted.
Production reverse proxies/multiple replicas need a shared edge rate limiter.

## Catalog And Limits

Supported rules cover Vietnamese/English category names, maximum VND budgets
(including grouped amounts and trieu/k units), follow-up messages, cheaper
requests, color, and racket weight/play style/skill level. Category switching
clears racket-only criteria. New conversation clears all client context.

Only ACTIVE products with positive stock and a purchasable variant (when
variant-managed) are returned. Budgets and ordering use effectivePrice. Color
is verified against available variants, never inferred from a product name.
Actual specifications/images are mapped through ProductService; unknown specs
are not invented and the chatbot does not manufacture match scores. Empty
catalogs return NO_MATCH, never demonstration products.

This is a limited rule-based assistant, not a general language model. Arbitrary
questions, complex price ranges, brand/size constraints, mixed intents and
general badminton knowledge are not supported as semantic search. Existing
profile recommendations within DH Chatbot provide richer racket matching.

Color token matching uses MySQL 8's Unicode-aware regular-expression support:
[MySQL regular expressions](https://dev.mysql.com/doc/refman/8.0/en/regexp.html).

## Verification

Backend tests cover parsing, follow-ups, no-demo fallback, request bounds,
anonymous access and unchanged private-route protection. Opt-in MySQL tests
cover sale prices, availability, variant colors and specifications, with
transaction rollback. Frontend tests cover sending, retries, reset/abort and
plain-text rendering; browser tests cover desktop/mobile layout, product
navigation, locale and preserved conversation state.

Initial floating-chat integration verified on 2026-10-13:

- Frontend production build passed without duplicate phrasebook warnings.
- Vitest: 15 passed.
- Playwright: 17 passed (7 chatbot cases plus 10 existing responsive regressions).
- Surefire: 91 passing test cases across the isolated MySQL full-suite run and
  the final targeted chatbot rerun; no failures, errors or skipped cases in the
  resulting reports. The PowerShell wrapper reported a native-stderr warning
  from Mockito after the full run; all individual test XML reports completed.
- Live anonymous API calls returned NEED_INPUT/LOCAL_RULES and NO_MATCH for the
  current catalog. The private profile endpoint still returned HTTP 401.
- Live browser checks at 1440x1000, 390x844 and 844x390 confirmed correct
  Vietnamese, no JavaScript errors, no horizontal overflow and an in-bounds panel.
- Screenshots: `docs/evidence/chatbot-*.png` (mocked populated catalog) and
  `docs/evidence/chatbot-live-*.png` (actual local API/current database).
- Temporary QA MySQL was shut down. Existing MySQL 3306, frontend 5173 and the
  restarted API 8080 remain available. Main application data was not modified.

### Profile Integration Verification (2026-10-13)

- Vitest: 29 passed, including saved-profile results, typed intents, guest/401/
  403/missing/empty states, retry, malformed responses, account changes and
  aborted or queued requests.
- Playwright: 29 passed (12 profile-chat, 7 general-chat and 10 existing
  responsive regressions). Authenticated profile flows use intercepted fixture
  responses; they do not create accounts, products or profiles in the main DB.
- Profile browser cases cover JWT headers, server score/reasons/cautions,
  POST-then-PUT profile saves, product links, locale and no automatic
  recommendation request on the account page.
- Inspected populated screenshots at 1440x1000, 390x844, 360x640 and 844x390;
  panel/log/composer stay in bounds, product images render, no horizontal
  overflow or JavaScript errors. Evidence: `docs/evidence/dh-chatbot-profile-*.png`.
- Live, unmocked browser smoke at 1440x1000, 390x844 and 844x390 confirmed
  DH Chatbot branding, guest sign-in handling without private requests and
  successful public LOCAL_RULES/NO_MATCH responses. Evidence:
  `docs/evidence/dh-chatbot-live-*.png`.
- Live unauthenticated GET /api/recommendations?limit=3 remains HTTP 401.
  A real authenticated recommendation request was not run against the main DB.
- Production build passed. Vite reports a non-blocking main-chunk size warning
  (503.35 kB before gzip). No backend changes or database reset were needed.
- Frontend remains available at http://localhost:5173 and API at port 8080.
