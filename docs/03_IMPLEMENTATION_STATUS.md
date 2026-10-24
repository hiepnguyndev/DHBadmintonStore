# Implementation Status — 2026-10-08

## PHẦN 1. Nguồn chuẩn

Đọc cùng 00_PROJECT_STRUCTURE.md, 01_SYSTEM_ANALYSIS_MASTER.docx, 02_IMPLEMENTATION_RULES.md và 04_CHANGELOG.md. Áp dụng D01–D17. Ba đề xuất trong DESIGN_DECISIONS_PENDING.md đã được người dùng phê duyệt ngày 2026-10-08; bản ghi cũ được giữ dưới mục lịch sử.

Bản trạng thái trước được giữ tại `archive/03_IMPLEMENTATION_STATUS_before_2026-10-08.md`; các NOT_STARTED cũ trong bản đó không mô tả source hiện tại.

## PHẦN 2. Trạng thái tổng thể

**IN_PROGRESS — bản source chạy local, chưa đánh dấu toàn bộ DONE.**

| Module | Status | Bằng chứng/phạm vi |
|---|---|---|
| Sprint 0 / Auth & User | TESTING | MySQL/Flyway/Hibernate; Auth API, component tests và Chromium E2E PASS |
| Product Catalog | TESTING | CRUD/filter/images và UI; Category/Brand ACTIVE/INACTIVE, không cascade; API test PASS |
| Cart / Shop Order | TESTING | Checkout/cancel/stock, UI, kiểm thử MySQL |
| Review / Favorite | TESTING | Backend/UI, ownership/completed/duplicate/idempotence đã kiểm thử MySQL |
| PlayerProfile / Recommendation | TESTING | Match Score D13, logging, UI, unit/MySQL và Chromium E2E PASS |
| Marketplace Listing | TESTING | Draft/review/images/ownership, UI và MySQL |
| Valuation | TESTING | D04/D15 calculator, matching/fallback, snapshot/history, API + UI và Chromium E2E PASS |
| Marketplace Order / Escrow | TESTING | Purchase/pay/confirm, DB transaction; concurrency purchase PASS |
| Shipment | TESTING | Create/admin transition, UI và MySQL |
| Inspection | TESTING | Assign/start/report, hiệu lực badge; MySQL PASS |
| Dispute | TESTING | Freeze + 3 resolution MVP; upload evidence và UI |
| Seller Rating / Reputation | TESTING | D14 denominator, nullable rates, rating sau COMPLETED |
| Notification | TESTING | List/read/read-all và UI; kiểm thử ownership, isolation read-all trên MySQL |
| Admin / Statistics | TESTING | Users/catalog/orders/listings/disputes/statistics, API MySQL |
| Audit Log | TESTING | Ghi DB trong transaction; API đọc ADMIN phân trang + UI; API authorization test PASS |
| System Testing | IN_PROGRESS | 66 backend/MySQL + 9 component + 3 Chromium E2E PASS; chưa đủ E2E toàn hệ thống |
| Deployment | NOT_STARTED | Có hướng dẫn local; chưa cấu hình/deploy production |

## PHẦN 3. Database và API

- Flyway V1: users/addresses. V2: domain Shop, Recommendation, Marketplace và các module mở rộng. V3: weights đã duyệt và indexes. V4: Category/Brand ACTIVE/INACTIVE, default/backfill/constraints.
- Hibernate ddl-auto=validate. Migration đã chạy trong MySQL 8.4.6 biệt lập, chưa áp dụng trên database của người dùng.
- SQL demo riêng, không phải migration production; tạo 3 sản phẩm giả lập, không tạo tài khoản đặc quyền.
- Danh mục endpoint hiện mở: tests/API_CATALOG.md, Swagger khi chạy local.
- Valuation nhận accessoryCondition/repairLevel bắt buộc. Đã mở cancel Marketplace và GET Audit theo D17. Không còn placeholder DESIGN_DECISION_REQUIRED trong implementation.

## PHẦN 4. Kiểm thử và Definition of Done

Kết quả kiểm thử thực tế nằm trong VALIDATION.md. Có source/biên dịch PASS chưa đủ để chuyển DONE. Cần tiếp tục browser integration toàn bộ workflow, error paths và kiểm thử tương ứng trước khi đánh dấu DONE.

Module được triển khai theo Database → Entity → Repository → Service → Controller/API → API Test → Frontend → Integration Test; phạm vi thiếu được ghi ở bảng trên và IMPLEMENTATION_HANDOFF.md.

## PHẦN 5. Việc tiếp theo

1. Chạy source local bằng README, kiểm tra USER/INSPECTOR/ADMIN.
2. Tiếp tục E2E các màn hình quản trị/kiểm định/tranh chấp.
3. CHANGELOG/Master DOCX+MD/Rules đã cập nhật D15–D17; V4 là migration mới. Kiểm thử nâng cấp database có dữ liệu thực khi có bản sao phù hợp.
4. Bổ sung test còn thiếu, kiểm tra triển khai production khi có cấu hình thực tế.

Nhắc cập nhật Implementation Status sau mỗi module/milestone. Không chuyển DONE chỉ vì build thành công.
