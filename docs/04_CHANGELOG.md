# 04_CHANGELOG.md

# XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ CẦU LÔNG ĐA NỀN TẢNG TÍCH HỢP CHỨC NĂNG GỢI Ý SẢN PHẨM THEO NHU CẦU NGƯỜI DÙNG

---

# 1. Mục đích của tài liệu

`04_CHANGELOG.md` dùng để ghi lại **các thay đổi quan trọng trong thiết kế, kiến trúc và nghiệp vụ của project**.

Tài liệu này trả lời các câu hỏi:

- Hệ thống ban đầu được thiết kế như thế nào?
- Thiết kế nào đã được thay đổi?
- Tại sao phải thay đổi?
- Ai hoặc giai đoạn nào đưa ra quyết định?
- Module nào bị ảnh hưởng?
- Database phải sửa gì?
- API phải sửa gì?
- Frontend phải sửa gì?
- Business Rule nào thay đổi?
- Test nào cần chạy lại?
- Tài liệu nào cần cập nhật?
- Source code nào cần refactor?
- Phiên bản thiết kế hiện tại là gì?

---

# 2. Changelog không phải Git Commit Log

Không dùng file này để ghi những thay đổi nhỏ như:

- đổi tên biến;
- sửa typo;
- chỉnh CSS;
- sửa margin;
- fix một bug nhỏ không làm thay đổi kiến trúc;
- refactor code nhưng giữ nguyên behavior;
- thêm comment;
- đổi icon.

Những thay đổi đó đã được Git quản lý.

---

# 3. Những thay đổi bắt buộc ghi vào Changelog

Phải ghi khi thay đổi một trong các nhóm sau:

## 3.1 Architecture

Ví dụ:

- đổi Modular Monolith sang Microservices;
- tách một module thành hai module;
- gộp hai module;
- thay đổi layer;
- thêm external service quan trọng.

---

## 3.2 Database

Ví dụ:

- thêm bảng;
- xóa bảng;
- đổi quan hệ;
- thêm Foreign Key;
- thêm Unique Constraint;
- đổi data type;
- thay đổi snapshot strategy;
- thay đổi soft delete;
- thay đổi cardinality.

---

## 3.3 Entity

Ví dụ:

- thêm Entity mới;
- xóa Entity;
- đổi field quan trọng;
- đổi ownership;
- đổi Entity relationship.

---

## 3.4 API

Ví dụ:

- đổi endpoint;
- đổi HTTP method;
- đổi Request DTO;
- đổi Response DTO;
- đổi API base;
- thêm versioning;
- thay authentication requirement.

---

## 3.5 Business Rule

Ví dụ:

- thay đổi điều kiện cancel Order;
- thay đổi khi Seller được edit Listing;
- thay đổi khi Buyer được tạo Dispute;
- thay đổi Escrow transition.

---

## 3.6 State Machine

Ví dụ:

thêm trạng thái mới:

`RETURN_REQUESTED`

hoặc đổi flow:

`DELIVERED → COMPLETED`

thành:

`DELIVERED → CONFIRMED → COMPLETED`

---

## 3.7 Recommendation Algorithm

Ví dụ:

- đổi trọng số;
- thêm tiêu chí mới;
- bỏ tiêu chí;
- thay scoring matrix;
- thay rule-based bằng hybrid approach.

---

## 3.8 Valuation Algorithm

Ví dụ:

- thêm RepairFactor;
- đổi Condition Weight;
- thêm market reference data;
- đổi output từ một giá thành price range.

---

## 3.9 Security

Ví dụ:

- thay đổi JWT strategy;
- thêm Refresh Token storage;
- thay đổi authorization;
- thêm ownership validation;
- thêm rate limiting.

---

## 3.10 Technology

Ví dụ:

- đổi React sang framework khác;
- đổi Spring Boot sang Node.js;
- đổi MySQL sang PostgreSQL;
- đổi image storage.

Những thay đổi dạng này phải được xem là **Major Architecture Change**.

---

# 4. Quy tắc cập nhật Changelog

Mỗi thay đổi phải được ghi theo thứ tự:

1. Phát hiện vấn đề.
2. Tạo Architecture Issue hoặc Change Request.
3. Phân tích thiết kế cũ.
4. Đề xuất thiết kế mới.
5. Phân tích tác động.
6. Quyết định.
7. Ghi Changelog.
8. Cập nhật tài liệu nguồn.
9. Cập nhật Database/API/source code.
10. Chạy lại test liên quan.
11. Cập nhật Implementation Status.

---

# 5. Không sửa thiết kế trước khi ghi quyết định

Không thực hiện:

`Phát hiện vấn đề → sửa ngay code`

với thay đổi kiến trúc lớn.

Quy trình đúng:

`Phát hiện vấn đề → Review → Decision → Changelog → Implementation`

---

# 6. Change ID

Mỗi thay đổi quan trọng có một ID riêng.

Format:

`CHG-YYYY-NNN`

Ví dụ:

`CHG-2026-001`

hoặc nếu muốn theo thứ tự đơn giản:

`CHG-001`

Trong project này, ưu tiên format:

`CHG-001`

để dễ sử dụng trong đồ án.

---

# 7. Change Type

Mỗi Change phải thuộc một hoặc nhiều loại:

- ARCHITECTURE
- DATABASE
- ENTITY
- API
- BUSINESS_RULE
- STATE_MACHINE
- RECOMMENDATION
- VALUATION
- SECURITY
- FRONTEND
- DEPLOYMENT
- TECHNOLOGY
- DOCUMENTATION

---

# 8. Change Severity

Dùng ba mức.

## MINOR

Thay đổi nhỏ nhưng cần đồng bộ tài liệu.

Ví dụ:

- thêm một optional field vào Response DTO;
- thêm một index;
- thêm một validation nhỏ.

---

## MAJOR

Ảnh hưởng nhiều module.

Ví dụ:

- thay relationship;
- đổi state machine;
- thay API contract;
- thêm Entity mới.

---

## CRITICAL

Thay đổi kiến trúc nền tảng.

Ví dụ:

- đổi Database;
- đổi Backend framework;
- đổi authentication model;
- đổi toàn bộ transaction architecture.

---

# 9. Change Status

Các trạng thái:

- PROPOSED
- UNDER_REVIEW
- APPROVED
- IMPLEMENTING
- IMPLEMENTED
- TESTED
- REJECTED
- DEPRECATED

---

# 10. Template chuẩn cho một Change

Mỗi thay đổi sử dụng format:

## CHG-XXX – Tên thay đổi

### Metadata

**Type:**  
DATABASE / API / BUSINESS_RULE / ...

**Severity:**  
MINOR / MAJOR / CRITICAL

**Status:**  
PROPOSED / APPROVED / IMPLEMENTED / TESTED

**Date:**  
YYYY-MM-DD

**Related Module:**  
Tên module.

**Related Chat:**  
Tên đoạn chat nơi quyết định được đưa ra.

---

### 1. Bối cảnh

Mô tả vấn đề.

---

### 2. Thiết kế cũ

Ghi chính xác cách hệ thống đang được thiết kế.

---

### 3. Vấn đề của thiết kế cũ

Giải thích vì sao cần thay đổi.

---

### 4. Thiết kế mới

Ghi quyết định mới.

---

### 5. Lý do chọn thiết kế mới

Nêu rationale.

---

### 6. Thành phần bị ảnh hưởng

- Database
- Entity
- Repository
- Service
- API
- Frontend
- Security
- Test
- Documentation

---

### 7. Database Changes

Nếu có.

---

### 8. API Changes

Nếu có.

---

### 9. Business Rule Changes

Nếu có.

---

### 10. Migration

Mô tả migration cần thực hiện.

---

### 11. Test cần chạy lại

Danh sách Unit/Integration/E2E test.

---

### 12. Documentation cần cập nhật

Ví dụ:

- SYSTEM_ANALYSIS_MASTER
- IMPLEMENTATION_RULES
- PROJECT_STRUCTURE
- API specification
- ERD

---

### 13. Rollback Consideration

Nếu thiết kế mới không hoạt động, rollback như thế nào.

---

### 14. Final Decision

APPROVED / REJECTED.

---

# 11. Kiến trúc ban đầu của Project

Tại thời điểm tạo Changelog, thiết kế nền của project được xem là:

### Project Title

> Xây dựng hệ thống thương mại điện tử cầu lông đa nền tảng tích hợp chức năng gợi ý sản phẩm theo nhu cầu người dùng.

### Frontend

React.

### Backend

Spring Boot.

### Database

MySQL.

### Architecture

Modular Monolith.

### API

REST/JSON.

### Authentication

JWT.

### Core Feature

Recommendation theo nhu cầu User.

### Extended Features

- Marketplace;
- Valuation;
- Inspection;
- Simulated Escrow;
- Shipment;
- Dispute;
- Seller Rating;
- Seller Reputation.

Tất cả nội dung trên được xem là **Baseline Architecture**.

---

# 12. Baseline Version

Version thiết kế ban đầu:

`ARCHITECTURE VERSION: 1.0`

Ngày:

`Initial design phase`

Status:

`ACTIVE`

---

# 13. Baseline – Core Module List

Version 1.0 gồm:

1. Auth.
2. User.
3. Product Catalog.
4. Cart.
5. Shop Order.
6. Review.
7. Favorite.
8. Player Profile.
9. Recommendation.
10. Marketplace Listing.
11. Valuation.
12. Inspection.
13. Marketplace Order.
14. Escrow.
15. Shipment.
16. Dispute.
17. Seller Rating.
18. Notification.
19. Admin.
20. Statistics.
21. Audit.

---

# 14. Baseline – Roles

System Roles:

- USER
- INSPECTOR
- ADMIN

Buyer/Seller không phải System Role.

---

# 15. Baseline – Shop Order Status

- PENDING
- CONFIRMED
- PREPARING
- SHIPPING
- COMPLETED
- CANCELLED

---

# 16. Baseline – Listing Status

- DRAFT
- PENDING_REVIEW
- AVAILABLE
- RESERVED
- SOLD
- REJECTED
- HIDDEN

---

# 17. Baseline – Marketplace Order Status

- PENDING_PAYMENT
- PAID
- WAITING_SHIPMENT
- SHIPPING
- DELIVERED
- COMPLETED
- CANCELLED
- DISPUTED
- REFUNDED

---

# 18. Baseline – Escrow Status

- WAITING_PAYMENT
- HELD
- RELEASED
- DISPUTED
- REFUNDED

---

# 19. Baseline – Inspection Status

- REQUESTED
- ACCEPTED
- INSPECTING
- COMPLETED
- REJECTED
- CANCELLED

---

# 20. Baseline – Dispute Status

- OPEN
- UNDER_REVIEW
- WAITING_BUYER
- WAITING_SELLER
- RESOLVED
- REJECTED

---

# 21. Baseline – Recommendation Weight

- Skill Level: 20%
- Play Style: 25%
- Wrist Strength: 15%
- Preferred Weight: 15%
- Shaft Flexibility: 10%
- Budget: 15%

Tổng:

100%.

---

# 22. Baseline – Valuation Formula

`EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor`

Output:

- suggestedMin;
- suggestedMax;

hoặc:

- quickSellPrice;
- fairPrice;
- expectedPrice.

---

# 23. Baseline – Marketplace Transaction

Luồng:

`AVAILABLE → Purchase → RESERVED → Escrow → Shipment → DELIVERED → Confirm Received → RELEASED → SOLD`

Nếu tranh chấp:

`HELD → DISPUTED → Admin Resolution → RELEASED / REFUNDED`

---

# 24. Current Approved Changes

Hiện tại:

**14 Architecture Changes APPROVED: CHG-001–CHG-014 (2026-10-05).**

Thiết kế tài liệu hiện hành: v1.14; baseline v1.0 được giữ làm lịch sử. Implementation NOT_STARTED.

---

# 25. Ví dụ Change – Thêm Relationship mới

Ví dụ minh họa, **không có nghĩa thay đổi này đã được áp dụng**.

## CHG-EXAMPLE-001 – Cho phép Listing có nhiều Inspection Request

### Metadata

**Type:** DATABASE, ENTITY, BUSINESS_RULE

**Severity:** MAJOR

**Status:** PROPOSED

---

### Bối cảnh

Thiết kế ban đầu có thể giới hạn một Listing chỉ có một Inspection Request.

Trong thực tế Seller có thể cần kiểm định lại sau thời gian dài hoặc sau khi thay đổi tình trạng sản phẩm.

---

### Thiết kế cũ

`UsedListing 1 : 0..1 InspectionRequest`

---

### Thiết kế mới đề xuất

`UsedListing 1 : N InspectionRequest`

Mỗi InspectionRequest có:

- requestedAt;
- status;
- completedAt;
- InspectionReport riêng.

---

### Ảnh hưởng Database

Không đặt UNIQUE trên:

`inspection_requests.listing_id`

---

### Ảnh hưởng API

`GET /api/inspections/listing/{listingId}`

có thể trả danh sách lịch sử.

---

### Ảnh hưởng Frontend

Listing Detail hiển thị report kiểm định mới nhất.

---

### Ảnh hưởng Business Rule

Verified Badge được xác định dựa vào Inspection COMPLETED mới nhất còn hiệu lực.

---

### Test cần chạy

- request inspection nhiều lần;
- latest report;
- Verified Badge;
- Seller ownership.

---

### Final Decision

`NOT APPROVED – EXAMPLE ONLY`

---

# 26. Ví dụ Change – API Versioning

## CHG-EXAMPLE-002 – Chuyển API Base từ /api sang /api/v1

**Type:** API

**Severity:** MAJOR

**Status:** PROPOSED

Thiết kế cũ:

`/api/products`

Thiết kế mới:

`/api/v1/products`

Ảnh hưởng:

- Backend Controller Mapping;
- React environment;
- Swagger;
- Postman Collection;
- documentation.

Không áp dụng nếu chưa APPROVED.

---

# 27. Ví dụ Change – Recommendation Weight

## CHG-EXAMPLE-003 – Thay đổi trọng số Recommendation

Thiết kế cũ:

| Factor | Weight |
|---|---:|
| Skill | 20 |
| Play Style | 25 |
| Wrist | 15 |
| Weight | 15 |
| Flex | 10 |
| Budget | 15 |

Đề xuất mới ví dụ:

| Factor | Weight |
|---|---:|
| Skill | 20 |
| Play Style | 30 |
| Wrist | 15 |
| Weight | 10 |
| Flex | 10 |
| Budget | 15 |

Thay đổi này cần:

- cập nhật RecommendationWeight;
- unit test;
- documentation;
- sample recommendation results.

Không thay trực tiếp trong Calculator nếu Changelog chưa APPROVED.

---

# 28. Ví dụ Change – Thêm Status

## CHG-EXAMPLE-004 – Thêm RETURN_REQUESTED vào MarketplaceOrder

Nếu sau này hỗ trợ trả hàng:

Thiết kế hiện tại:

`DISPUTED → REFUNDED`

Có thể cần:

`DISPUTED → RETURN_REQUESTED → RETURNING → REFUNDED`

Đây là thay đổi State Machine và phải phân tích toàn bộ:

- MarketplaceOrder;
- Shipment;
- Escrow;
- Dispute;
- Admin;
- Frontend Timeline.

---

# 29. Ví dụ Change – Real Payment

## CHG-EXAMPLE-005 – Thay Escrow Simulation bằng Payment Gateway

Hiện tại:

`POST /api/escrow/{orderId}/pay`

mô phỏng payment.

Sau này nếu tích hợp Payment Gateway:

Flow sẽ đổi thành:

`Buyer → Payment Provider → Backend Callback/Webhook → Verify → Escrow HELD`

Ảnh hưởng:

- Payment Entity;
- webhook;
- signature validation;
- transaction;
- security;
- refund;
- deployment secrets.

Đây là MAJOR CHANGE.

---

# 30. Ví dụ Change – Mobile Application

Mobile App không yêu cầu thay business architecture nếu Backend API đã dùng chung.

Nếu thêm Mobile:

Change chủ yếu:

- new client;
- authentication storage strategy;
- push notification;
- responsive/API considerations.

Core Backend vẫn dùng lại.

---

# 31. Database Change Template chi tiết

Khi thay Database, ghi thêm:

### Existing Schema

Tên bảng và column cũ.

### Target Schema

Tên bảng và column mới.

### Migration Script

Ví dụ:

`V5__alter_inspection_relationship.sql`

### Existing Data Impact

Có dữ liệu cũ không?

### Backfill

Có cần chuyển dữ liệu không?

### Constraint Impact

Foreign Key/Unique/Index nào thay đổi?

### Rollback Risk

Có thể rollback hay mất dữ liệu?

---

# 32. API Change Template chi tiết

Mỗi API Change cần ghi:

### Old Endpoint

Method + URL.

### New Endpoint

Method + URL.

### Old Request

DTO cũ.

### New Request

DTO mới.

### Old Response

Response cũ.

### New Response

Response mới.

### Compatibility

Breaking hay non-breaking.

### Frontend Changes

Service nào phải sửa.

### Documentation

Swagger/Postman cần cập nhật.

---

# 33. Business Rule Change Template

Ví dụ:

### Old Rule

Buyer được cancel khi PENDING_PAYMENT.

### New Rule

Buyer được cancel khi PENDING_PAYMENT hoặc PAID trước khi Seller tạo Shipment.

Sau đó phải phân tích:

- refund;
- Listing release;
- Escrow;
- notification;
- audit.

---

# 34. State Machine Change Template

Phải ghi:

### Old States

Danh sách trạng thái cũ.

### New States

Danh sách trạng thái mới.

### Old Transition

Flow cũ.

### New Transition

Flow mới.

### Invalid Transitions

Các transition bị cấm.

### Affected API

Danh sách endpoint.

### Affected UI

Timeline/status badge.

---

# 35. Security Change Template

Ghi:

### Existing Security

Ví dụ JWT Access Token.

### New Security

Ví dụ Access + Refresh Token.

### Reason

Tại sao thay đổi.

### Threat Addressed

Ví dụ token lifetime.

### Backend Impact

SecurityConfig, filter, database.

### Frontend Impact

Axios interceptor, cookie/local state.

### Test

- expired token;
- refresh;
- logout;
- revoked refresh token.

---

# 36. Recommendation Change Template

Mỗi lần thay thuật toán phải ghi:

### Algorithm Version

Ví dụ:

`REC-V1`

### Current Factors

Danh sách tiêu chí.

### Current Weights

Trọng số.

### Proposed Factors

Danh sách mới.

### Proposed Weights

Trọng số mới.

### Expected Behavioral Change

Ví dụ Attack Product được ưu tiên hơn.

### Test Dataset

Các profile dùng để so sánh.

---

# 37. Valuation Change Template

Ghi:

### Algorithm Version

Ví dụ:

`VAL-V1`

### Existing Factors

- Age;
- Condition;
- Warranty;
- Accessories;
- Repair;
- Demand.

### New Factor

Ví dụ:

`MarketLiquidityFactor`

### Expected Effect

Ảnh hưởng giá.

### Backward Compatibility

Valuation cũ có cần tính lại không?

---

# 38. Architecture Versioning

Mỗi khi có MAJOR Architecture Change được APPROVED:

tăng version.

Ví dụ:

`Architecture v1.0 → v1.1`

Thay đổi cực lớn:

`v1.x → v2.0`

---

# 39. Recommendation Algorithm Versioning

Có thể dùng:

- REC-V1;
- REC-V2.

Nếu RecommendationLog được lưu, có thể lưu:

`algorithmVersion`

để biết score được tạo bởi version nào.

---

# 40. Valuation Algorithm Versioning

Tương tự:

- VAL-V1;
- VAL-V2.

Có thể lưu:

`valuationMethod`

và:

`algorithmVersion`

nếu cần phân tích lịch sử.

---

# 41. Changelog và Implementation Status

Sau khi Change được IMPLEMENTED:

phải cập nhật:

`03_IMPLEMENTATION_STATUS.md`

Ví dụ:

Change thêm field `repairFactor`.

Implementation Status ghi:

`ValuationCalculator – DONE`

`Valuation Migration – DONE`

---

# 42. Changelog và Master Document

Nếu Change được APPROVED:

không để Changelog trở thành nơi duy nhất chứa thiết kế mới.

Phải cập nhật:

`01_SYSTEM_ANALYSIS_MASTER.docx`

để Master phản ánh kiến trúc mới.

Changelog chỉ giữ lịch sử:

> Tại sao và khi nào thay đổi.

---

# 43. Changelog và Implementation Rules

Nếu thay đổi tạo ra một rule mới:

phải update:

`02_IMPLEMENTATION_RULES.md`

Ví dụ:

Nếu thêm:

`RETURNING`

thì rule state transition phải được thêm vào Implementation Rules.

---

# 44. Changelog và Project Structure

Nếu thêm module lớn:

phải cập nhật:

`00_PROJECT_STRUCTURE.md`

Ví dụ thêm:

`Chat Module`

thì Module List và Chat Structure cần cập nhật.

---

# 45. Quy tắc khi ChatGPT đề xuất thay đổi

Nếu ChatGPT phát hiện thiết kế cũ có vấn đề:

không tự sửa.

Phải trình bày:

1. Thiết kế hiện tại.
2. Vấn đề.
3. Phương án mới.
4. Ưu điểm.
5. Nhược điểm.
6. Module ảnh hưởng.
7. Có phải Breaking Change không.

Sau khi User chấp nhận mới tạo Change chính thức.

---

# 46. Quy tắc khi User yêu cầu tính năng mới

Ví dụ User nói:

> Thêm chat giữa Buyer và Seller.

Không code ngay.

Phải xác định:

- có Entity Message không;
- Conversation relationship;
- security;
- moderation;
- realtime;
- notification;
- dispute evidence có liên quan không;
- database impact;
- API impact.

Sau đó mới quyết định scope.

---

# 47. Quy tắc giữ phạm vi đề tài

Tên đề tài chính thức vẫn giữ nguyên ngay cả khi có các tính năng mở rộng.

Marketplace, Valuation, Inspection, Escrow và Dispute là module mở rộng.

Không thay tên đề tài nếu không có yêu cầu chính thức.

---

# 48. Không dùng Changelog để thay đổi lịch sử

Một Change đã IMPLEMENTED không được sửa nội dung cũ để làm như nó chưa từng xảy ra.

Nếu cần thay đổi tiếp:

tạo Change mới.

Ví dụ:

`CHG-010`

sau đó:

`CHG-015` thay lại quyết định của CHG-010.

Như vậy lịch sử được giữ.

---

# 49. Rejected Change

Change bị từ chối vẫn có thể giữ trong Changelog nếu nó quan trọng.

Status:

`REJECTED`

Và ghi rõ lý do.

Điều này tránh việc một chat sau đề xuất lại cùng một ý tưởng mà không biết đã từng bị loại.

---

# 50. Deprecated Decision

Nếu một decision cũ không còn áp dụng:

không xóa.

Đổi:

`Status: DEPRECATED`

và reference đến Change thay thế.

Ví dụ:

`Deprecated by CHG-020`

---

# 51. Breaking Change

Một Change được xem là Breaking nếu làm Client/Database hiện tại không còn tương thích.

Ví dụ:

- đổi endpoint;
- đổi required field;
- xóa enum value;
- đổi relationship;
- đổi authentication.

Breaking Change phải được ghi rõ:

`Breaking Change: YES`

---

# 52. Migration Required

Mỗi Change ghi:

`Migration Required: YES/NO`

Nếu YES:

ghi migration ID.

Ví dụ:

`V8__add_repair_factor.sql`

---

# 53. Frontend Update Required

Ghi:

`Frontend Update Required: YES/NO`

Ví dụ API Response đổi:

Frontend phải sửa:

`marketplaceApi.js`

và:

`ListingDetailPage`.

---

# 54. Retest Required

Mỗi Change phải chỉ rõ phạm vi retest.

Ví dụ thay Escrow transition:

Retest:

- Marketplace Order;
- Escrow;
- Shipment;
- Confirm Received;
- Dispute.

---

# 55. Change Dependency

Một Change có thể phụ thuộc Change khác.

Ví dụ:

`CHG-012 depends on CHG-010`

Không implement trước dependency.

---

# 56. Current Change Registry

Bảng theo dõi:

| ID | Title | Type | Severity | Status |
|---|---|---|---|---|
| BASELINE | Architecture Version 1.0 | ARCHITECTURE | MAJOR | ACTIVE |

Có 14 Change chính thức APPROVED: CHG-001–CHG-014, ngày 2026-10-05; áp dụng ở mức tài liệu. Chưa IMPLEMENTED/TESTED trong source.

---

# 57. Pending Architecture Issues

Hiện tại:

**Pending Architecture Issues = 0.** D01–D14 đã APPROVED và đồng bộ qua CHG-001–CHG-014. D13 → CHG-013; D14 → CHG-014. Xem DOCUMENT_VERIFICATION_REPORT.md.

Sau này format:

| Issue | Description | Module | Status |
|---|---|---|---|
| ARCH-001 | ... | Inspection | UNDER_REVIEW |

Architecture Issue chưa phải Change.

Chỉ khi quyết định được APPROVED mới tạo CHG.

---

# 58. Example Architecture Issue

Ví dụ:

`ARCH-001`

Question:

> Có nên cho phép một Listing có nhiều lần kiểm định không?

Affected:

- UsedListing;
- InspectionRequest;
- InspectionReport;
- Verified Badge.

Status:

`UNDER_REVIEW`

Sau khi quyết định:

tạo:

`CHG-00X`

---

# 59. Quy trình đầy đủ của Architecture Issue

`Issue Found`

↓

`ARCH-XXX created`

↓

`Review`

↓

Nếu Reject:

`ARCH-XXX CLOSED`

Nếu Approve:

`CHG-XXX created`

↓

`Update Design`

↓

`Implementation`

↓

`Testing`

↓

`CHG status = TESTED`

---

# 60. Quy tắc Final Decision

Một Change chỉ được coi là chính thức khi:

`Status = APPROVED`

hoặc cao hơn:

- IMPLEMENTING;
- IMPLEMENTED;
- TESTED.

`PROPOSED` không phải thiết kế chính thức.

---

# 61. Initial Changelog Entry

## BASELINE – Architecture Version 1.0

**Type:** ARCHITECTURE

**Severity:** MAJOR

**Status:** ACTIVE

**Project:**  
Xây dựng hệ thống thương mại điện tử cầu lông đa nền tảng tích hợp chức năng gợi ý sản phẩm theo nhu cầu người dùng.

### Decision

Thiết kế hiện tại sử dụng:

- React Frontend;
- Spring Boot Backend;
- MySQL;
- REST API;
- JWT;
- Modular Monolith.

Core business:

- Ecommerce Shop;
- Personalized Recommendation.

Extended business:

- Marketplace;
- Valuation;
- Inspection;
- Simulated Escrow;
- Shipment;
- Dispute;
- Seller Reputation.

### Reason

Kiến trúc này đáp ứng phạm vi đồ án, có độ phức tạp vừa phải, dễ triển khai nhưng vẫn cho phép mở rộng.

### Status

`ACTIVE`

---

# 62. Cách thêm Change mới

Khi có Change mới:

copy Template tại phần đầu.

Ví dụ:

`CHG-001 – ...`

Không ghi đè Baseline.

---

# 63. Quy tắc đặt Title

Title phải mô tả thay đổi rõ.

Tốt:

`CHG-001 – Change Listing–Inspection relationship to one-to-many`

Không tốt:

`CHG-001 – Update database`

---

# 64. Quy tắc mô tả Reason

Không ghi:

> Vì thấy tốt hơn.

Phải ghi lý do cụ thể:

- business requirement;
- integrity;
- performance;
- security;
- maintainability;
- user experience;
- scalability;
- implementation constraint.

---

# 65. Quy tắc Impact Analysis

Một Change quan trọng phải kiểm tra tối thiểu:

### Backend

- Entity?
- DTO?
- Mapper?
- Repository?
- Service?
- Controller?

### Database

- table?
- column?
- constraint?
- index?
- migration?

### Frontend

- page?
- component?
- API service?
- state?

### Testing

- unit?
- integration?
- E2E?
- security?

### Documentation

- Master?
- Rule?
- API?
- ERD?

---

# 66. Changelog không được lỗi thời

Nếu code đã triển khai một kiến trúc khác mà Changelog không ghi:

project bị mất Source of Truth.

Do đó sau mọi thay đổi lớn:

Changelog phải cập nhật trước hoặc cùng lúc với implementation.

---

# 67. Trạng thái tại thời điểm tạo file

Tại thời điểm tạo file:

`Architecture Version: 1.0`

`Approved Changes after Baseline: 0`

`Pending Changes: 0`

`Pending Architecture Issues: 0`

---

# 68. Next Action

Sau khi tạo file này:

bộ tài liệu quản lý project gồm:

- `00_PROJECT_STRUCTURE.md`
- `01_SYSTEM_ANALYSIS_MASTER.docx`
- `02_IMPLEMENTATION_RULES.md`
- `03_IMPLEMENTATION_STATUS.md`
- `04_CHANGELOG.md`

Bước tiếp theo không phải tạo thêm nhiều file quản lý tùy ý.

Cần chuyển sang chuẩn hóa:

`01_SYSTEM_ANALYSIS_MASTER.docx`

để 12 phần phân tích–thiết kế trở thành một tài liệu nguồn thống nhất, có cấu trúc rõ ràng và ít xuống dòng dư thừa.

> Snapshot audit 2026-10-04 được thay thế bởi các Change APPROVED ngày 2026-10-05 và báo cáo verification hiện hành. Các ví dụ PROPOSED ở trên vẫn chỉ là ví dụ.


# 69. Approved Decision Matrix — 2026-10-05

Người phê duyệt: User, Decision Matrix được cung cấp trực tiếp trong chat này. Áp dụng tài liệu, không phải bằng chứng implementation.

SHA-256 nguồn phê duyệt: `c4d8e39d07d52fbfdd0eec64e8eb48f3a99bdafa1088246510bd8983088ba2a4`.

Architecture document version hiện hành: **v1.14** (baseline v1.0 + 14 MAJOR document-design changes).

| Decision | Change | Status | Source implementation |
|---|---|---|---|
| D01 | CHG-001 | APPROVED | NOT_STARTED |
| D02 | CHG-002 | APPROVED | NOT_STARTED |
| D03 | CHG-003 | APPROVED | NOT_STARTED |
| D04 | CHG-004 | APPROVED | NOT_STARTED |
| D05 | CHG-005 | APPROVED | NOT_STARTED |
| D06 | CHG-006 | APPROVED | NOT_STARTED |
| D07 | CHG-007 | APPROVED | NOT_STARTED |
| D08 | CHG-008 | APPROVED | NOT_STARTED |
| D09 | CHG-009 | APPROVED | NOT_STARTED |
| D10 | CHG-010 | APPROVED | NOT_STARTED |
| D11 | CHG-011 | APPROVED | NOT_STARTED |
| D12 | CHG-012 | APPROVED | NOT_STARTED |
| D13 | CHG-013 | APPROVED | NOT_STARTED |
| D14 | CHG-014 | APPROVED | NOT_STARTED |

## CHG-001 — LISTING–INSPECTION CARDINALITY

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D01; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.0 → v1.1

### Old design (historical, không dùng cho implementation)

mô tả `UsedListing 1 → 0..1 InspectionRequest` ở tổng quan, nhưng ERD ghi `0..N` và class diagram dùng `*`. MD dùng placeholder “Inspection relationship theo baseline hiện hành”. Rules chỉ giới hạn active request nếu policy quy định. Ví dụ trong Changelog là PROPOSED, không giải quyết mâu thuẫn.

### Problem

không có lựa chọn đủ thẩm quyền xác định quan hệ, số lần kiểm định lại và cách chọn report hiện hành.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

New Design:

1. Quan hệ chính thức:

UsedListing 1 → 0..N InspectionRequest

InspectionRequest 1 → 0..1 InspectionReport

2. Một Listing được phép có nhiều InspectionRequest trong lịch sử.

3. Tại cùng một thời điểm, một Listing chỉ được có tối đa 1 InspectionRequest ACTIVE.

Các status được xem là ACTIVE:

- REQUESTED
- ACCEPTED
- INSPECTING

Các status không còn active:

- COMPLETED
- REJECTED
- CANCELLED

4. Seller chỉ được tạo request mới khi không có InspectionRequest active.

5. Mỗi InspectionRequest COMPLETED phải có InspectionReport tương ứng.

6. Không xóa InspectionRequest cũ chỉ vì có lần kiểm định mới.

Phải giữ lịch sử kiểm định.

7. Verified Badge sử dụng InspectionReport COMPLETED mới nhất còn hiệu lực.

8. Nếu có nhiều InspectionReport trong lịch sử:

currentInspectionReport
=
report của InspectionRequest COMPLETED mới nhất còn hiệu lực.

9. Valuation có thể ưu tiên Condition/Inspection data từ InspectionReport COMPLETED mới nhất còn hiệu lực.

10. Không đặt UNIQUE trên:

inspection_requests.listing_id

Có thể dùng index thông thường.

Reason:

- giữ lịch sử kiểm định;
- cho phép kiểm định lại;
- tránh mất dữ liệu cũ;
- vẫn ngăn nhiều quy trình kiểm định chạy song song.

Needs CHANGELOG:
YES

Database impact:
YES

Migration required before implementation:
YES nếu schema đã tồn tại.
Hiện project chưa implementation nên phản ánh vào thiết kế/migration ban đầu.
```

### Rationale

Giải quyết mâu thuẫn D01 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Inspection, Listing, Valuation.
- Database/model: listing_id FK/index thường; giữ report unique theo inspection_id.
- API: Lịch sử Inspection và report hiện hành; giữ action API D08.
- Frontend: Hiển thị lịch sử và badge hiệu lực.
- Security: Seller ownership; tối đa một active request kể cả đồng thời.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Tạo nhiều request lịch sử; reject active song song; COMPLETED có report; report mới nhất hợp lệ. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-002 — MARKETPLACE CANCEL / TIMEOUT / UNIQUE LISTING

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D02; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.1 → v1.2

### Old design (historical, không dùng cho implementation)

`marketplace_orders.listing_id UNIQUE`; hủy/timeout trả Listing về AVAILABLE nhưng Order cũ vẫn CANCELLED. Escrow sau hủy được ghi `cancelled/invalid`, không thuộc enum. Refund Rule còn nêu trường hợp cancellation trước release ngoài transition `DISPUTED → REFUNDED` của Rules.

### Problem

tạo order thứ hai sẽ vi phạm UNIQUE; không có trạng thái/transition escrow hợp lệ được chỉ rõ cho hủy chưa thanh toán. “Một lần bán thành công” và “một order trong toàn bộ lịch sử” chưa được phân biệt.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

New Design:

1. Bỏ thiết kế:

UNIQUE(marketplace_orders.listing_id)

MarketplaceOrder phải lưu được lịch sử nhiều lần đặt mua cùng một Listing nếu các giao dịch trước đã CANCELLED.

Quan hệ:

UsedListing 1 → 0..N MarketplaceOrder

2. Tuy nhiên tại cùng một thời điểm chỉ được tồn tại một MarketplaceOrder ACTIVE cho Listing.

3. Việc chống double purchase vẫn bắt buộc bằng:

- lock UsedListing;
- check Listing = AVAILABLE;
- transaction;
- Listing → RESERVED;
- service validation;
- concurrency test.

4. Không phụ thuộc duy nhất vào UNIQUE(listing_id).

5. Buyer chỉ được normal-cancel MarketplaceOrder khi:

MarketplaceOrder = PENDING_PAYMENT

6. Payment timeout cũng chỉ áp dụng ở PENDING_PAYMENT.

Khi cancel/timeout:

MarketplaceOrder:
PENDING_PAYMENT → CANCELLED

Listing:
RESERVED → AVAILABLE

Escrow:
WAITING_PAYMENT → CANCELLED

7. Bổ sung EscrowStatus:

CANCELLED

Escrow status chính thức sau quyết định:

- WAITING_PAYMENT
- HELD
- RELEASED
- DISPUTED
- REFUNDED
- CANCELLED

8. Sau khi Escrow đã HELD, Buyer không dùng normal cancel.

Nếu giao dịch đã thanh toán và phát sinh vấn đề:
→ xử lý qua Dispute/Admin Resolution.

9. MarketplaceOrder CANCELLED vẫn giữ trong lịch sử.

Không hard delete Order/Escrow để Listing được mua lại.

10. Khi Listing quay lại AVAILABLE, Buyer khác hoặc cùng Buyer có thể tạo MarketplaceOrder mới.

Reason:

Thiết kế cũ vừa:
- UNIQUE listing_id
vừa:
- CANCELLED → Listing AVAILABLE

làm Listing không thể tạo order lần hai.

Thiết kế mới phân biệt:

“một Listing chỉ có một giao dịch active”
khác với
“một Listing chỉ từng có một order trong toàn bộ lịch sử”.

Needs CHANGELOG:
YES

Database impact:
YES

State machine impact:
YES
```

### Rationale

Giải quyết mâu thuẫn D02 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: MarketplaceOrder, Listing, Escrow.
- Database/model: Bỏ unique listing_id; thêm escrow CANCELLED; giữ lịch sử.
- API: Cancel/timeout theo state; pay không nhận escrow CANCELLED.
- Frontend: Mua lại sau hủy; hiển thị lịch sử.
- Security: Lock/transaction; buyer ownership; không normal-cancel sau HELD.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Concurrent purchase; cancel/timeout; race pay-cancel; mua lại sau CANCELLED; giữ lịch sử. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

> Hiệu lực hiện hành: phần suy targetFlex và miền Flex của CHG-003 được bổ sung/thay thế bởi CHG-013; các weight và Skill/Budget rules giữ nguyên.

## CHG-003 — MATCH SCORE SUB-SCORE RULES

**Type:** RECOMMENDATION

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D03; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.2 → v1.3

### Old design (historical, không dùng cho implementation)

quy tắc SkillScore lệch một mức = 12, hai mức = 5. Bảng ngay sau đó lại có INTERMEDIATE→BEGINNER = 14, ADVANCED→INTERMEDIATE = 15, ADVANCED→BEGINNER = 8. Các mô tả Flex/Budget chưa bao phủ đầy đủ trường hợp và biên.

### Problem

cùng input có thể trả score và thứ hạng khác nhau. Rules chỉ xác định weight, không chọn bảng điểm con thay thế.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Không thay Recommendation Weight.

Weights vẫn giữ nguyên:

Skill Level       20
Play Style        25
Wrist Strength    15
Preferred Weight  15
Shaft Flexibility 10
Budget            15

Total             100

Skill Level mapping chính thức:

Level order:

Beginner = 0
Intermediate = 1
Advanced = 2

difference = abs(profileLevel - racketLevel)

difference = 0:
SkillScore = 20

difference = 1:
SkillScore = 12

difference = 2:
SkillScore = 5

Loại bỏ các bảng cũ có:
14 / 15 / 8
nếu chúng mâu thuẫn với mapping trên.

Shaft Flex mapping:

Derived target flex theo rule hiện hành của profile/requirement.

Nếu targetFlex == racketFlex:
FlexScore = 10

Nếu lệch một mức:
FlexScore = 6

Nếu lệch hai mức:
FlexScore = 2

Flex order:

FLEXIBLE
→ MEDIUM
→ STIFF

Budget Score:

effectivePrice =
salePrice hợp lệ nếu có
ngược lại product.price

Nếu:

minBudget <= effectivePrice <= maxBudget

BudgetScore = 15

Nếu effectivePrice < minBudget:

BudgetScore = 12

Nếu:

maxBudget < effectivePrice <= maxBudget * 1.10

BudgetScore = 10

Nếu:

maxBudget * 1.10 < effectivePrice <= maxBudget * 1.20

BudgetScore = 5

Nếu:

effectivePrice > maxBudget * 1.20

BudgetScore = 0

Các scoring rule khác đã không mâu thuẫn trong Master được giữ nguyên.

Mọi sub-score phải:

- deterministic;
- bounded;
- testable;
- được implement trong MatchScoreCalculator;
- không hard-code trong Controller.

MatchScore:

0 <= score <= 100

Needs CHANGELOG:
YES
Type:
RECOMMENDATION
```

### Rationale

Giải quyết mâu thuẫn D03 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Recommendation, PlayerProfile.
- Database/model: Không đổi entity hoặc weights; thuật toán thay đổi.
- API: MatchScore/reasons đổi theo mapping chính thức.
- Frontend: Hiển thị score/reasons từ Backend.
- Security: Không tin score từ client.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Skill 3x3; flex 0/1/2; budget tại min/max/110%/120%; salePrice; bounds. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-004 — VALUATION / CONDITION PERSISTENCE & PRECISION

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D04; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.3 → v1.4

### Old design (historical, không dùng cho implementation)

công thức có RepairFactor nhưng bảng valuations liệt kê age/condition/warranty/accessory/demand, không có repair_factor. Condition Score dùng Crack và Repair cùng các thành phần khác; cách lưu các điểm thành phần, nguồn condition và tái tạo giá lịch sử chưa được mô tả đồng bộ. Các khoảng tuổi và grade chưa nói rõ cách xử lý biên/độ chính xác.

### Problem

chưa biết factor là snapshot cần lưu hay tính lại; không thể tự thêm cột hoặc chọn chính sách rounding. Các khoảng grade như 8.5–9.49 không bao phủ số có nhiều chữ số thập phân nếu chưa có quy tắc làm tròn.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Valuation formula giữ nguyên:

EstimatedPrice =
ReferencePrice
× AgeFactor
× ConditionFactor
× WarrantyFactor
× AccessoryFactor
× RepairFactor
× DemandFactor

Valuation phải lưu snapshot đủ để giải thích/tái tạo kết quả tại thời điểm tính.

Valuation cần lưu hoặc biểu diễn tương đương:

- referencePrice
- ageFactor
- conditionFactor
- warrantyFactor
- accessoryFactor
- repairFactor
- demandFactor
- conditionScore
- valuationMethod
- suggestedMinPrice
- suggestedMaxPrice
- algorithmVersion
- createdAt

Nếu schema hiện tại thiếu các factor snapshot này:
cập nhật thiết kế schema.

Money:
DECIMAL(15,2)

Factor:
dùng DECIMAL precision phù hợp, không FLOAT/DOUBLE.

Condition Score:

Range:
0.00–10.00

Chuẩn hóa/làm tròn đến 2 chữ số thập phân trước khi map Grade.

Grade chính thức:

9.50 <= score <= 10.00
→ S

8.50 <= score < 9.50
→ A

7.00 <= score < 8.50
→ B

5.00 <= score < 7.00
→ C

score < 5.00
→ D

Condition source:

- SELLER
- AI
- INSPECTION

Priority:

INSPECTION
>
AI
>
SELLER

khi dữ liệu tương ứng hợp lệ và còn hiệu lực.

Các bảng factor cụ thể đang có trong Master nếu không mâu thuẫn được giữ nguyên.

Các khoảng giá trị phải được viết bằng interval không chồng lấn.

Needs CHANGELOG:
YES

Database impact:
YES
```

### Rationale

Giải quyết mâu thuẫn D04 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Valuation, Inspection, Condition.
- Database/model: Snapshot đủ factors; thêm repair_factor/condition_score/algorithm_version.
- API: Trả range/snapshot; source condition hợp lệ.
- Frontend: Grade và range dùng kết quả Backend.
- Security: Không cho client giả condition source/inspection.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Snapshot tái tạo; grade 5/7/8.5/9.5/10; condition source priority. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

> Hiệu lực hiện hành: rate population, attribution, null-rate và NEW SELLER của CHG-005 được bổ sung/thay thế bởi CHG-014; các weight/mapping còn lại giữ nguyên.

## CHG-005 — SELLER REPUTATION

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D05; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.4 → v1.5

### Old design (historical, không dùng cho implementation)

Cancellation Score chỉ nêu tỷ lệ thấp = 10, cao = 0–5 nhưng ví dụ cancellation 2% = 9. Các ngưỡng tuổi có biên trùng; mẫu số/khoảng thời gian thống kê và seller chưa có giao dịch chưa rõ.

### Problem

ví dụ tổng 90.4 không suy ra được từ rule hiện có. Phép cộng ví dụ không sai; mapping tạo ra từng số mới là phần chưa chốt.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Seller Reputation Score:

Total = 100

Components:

Rating             40
Completed Orders   20
Dispute            20
Cancellation       10
Account Age        10

--------------------------------
Rating Score
--------------------------------

Nếu có rating:

ratingScore =
averageRating / 5 * 40

Ví dụ:

4.8 / 5
→ 38.4

Nếu chưa có rating:
ratingScore = 0

--------------------------------
Completed Transaction Score
--------------------------------

completed = 0
→ 0

1–4
→ 5

5–19
→ 10

20–49
→ 15

>= 50
→ 20

Ví dụ:

35 completed
→ 15

--------------------------------
Dispute Score
--------------------------------

0%
→ 20

>0% và <=2%
→ 18

>2% và <=5%
→ 14

>5% và <=10%
→ 8

>10%
→ 3

--------------------------------
Cancellation Score
--------------------------------

0%
→ 10

>0% và <=2%
→ 9

>2% và <=5%
→ 7

>5% và <=10%
→ 4

>10%
→ 0

Ví dụ:

2%
→ 9

--------------------------------
Account Age Score
--------------------------------

age < 1 month
→ 2

1 <= age < 3 months
→ 4

3 <= age < 6 months
→ 6

6 <= age < 12 months
→ 8

age >= 12 months
→ 10

--------------------------------

Reputation Score:

ratingScore
+ transactionScore
+ disputeScore
+ cancellationScore
+ accountAgeScore

Ví dụ cũ:

Rating 4.8
→ 38.4

Completed = 35
→ 15

Dispute = 1%
→ 18

Cancellation = 2%
→ 9

Account Age = 14 months
→ 10

Total:
90.4

Seller mới:

completedTransactions = 0

Frontend nên hiển thị:

NEW SELLER

Không tự gán REPUTABLE SELLER chỉ vì account age.

Seller Reputation do Backend tính.

Frontend không gửi reputationScore.

Rate denominator:

Chỉ dùng MarketplaceOrder đã đi đến terminal business outcome phù hợp cho statistic.

Các query cụ thể được triển khai trong SellerReputationService/Repository, không tính từ client.

Needs CHANGELOG:
YES
```

### Rationale

Giải quyết mâu thuẫn D05 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Seller Reputation, Rating, Statistics.
- Database/model: Không tạo entity mới; query terminal outcome tại Backend.
- API: Score và metrics từ Backend.
- Frontend: NEW SELLER; không tự gán uy tín.
- Security: Không nhận reputationScore do client gửi.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Mapping từng biên; rating rỗng; completed 0/1/5/20/50; ví dụ 90.4. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-006 — DISPUTE ELIGIBILITY & RESOLUTION

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D06; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.5 → v1.6

### Old design (historical, không dùng cho implementation)

cho mở khi SHIPPING/DELIVERED “tùy chính sách”, rồi ưu tiên DELIVERED; có reason NOT_RECEIVED. Resolution gồm REJECT_DISPUTE và RETURN_AND_REFUND nhưng chưa xác định đầy đủ trạng thái Order/Escrow sau reject hoặc thời điểm hoàn tiền sau hoàn hàng. PARTIAL_REFUND có điều kiện scope nhưng chưa có mô hình tiền chi tiết.

### Problem

có thể khóa escrow vô thời hạn hoặc cho refund/release sai thời điểm; chưa đủ cơ sở để tự chọn transition. Việc Buyer hoặc Seller được tạo dispute đã rõ trong Rules và đã sửa ở đoạn chỉ cho Buyer.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

1. Current User phải là Buyer hoặc Seller của MarketplaceOrder.

2. Một MarketplaceOrder chỉ có tối đa một active Dispute trong MVP.

3. Escrow phải đang HELD khi tạo Dispute.

4. Order state cho phép:

SHIPPING
hoặc
DELIVERED

5. Nếu Order = SHIPPING:

reason phù hợp nhất cho Buyer là:

NOT_RECEIVED

Các reason khác chỉ được chấp nhận khi business context hợp lệ.

6. Nếu Order = DELIVERED:

có thể sử dụng:

- NOT_AS_DESCRIBED
- DAMAGED
- SUSPECTED_FAKE
- MISSING_ACCESSORIES
- WRONG_ITEM
- NOT_RECEIVED
- OTHER

7. Khi Dispute được tạo:

MarketplaceOrder → DISPUTED
Escrow → DISPUTED
Dispute → OPEN

transactionally.

8. MVP chính thức triển khai hai resolution tiền hoàn chỉnh:

A. RELEASE_TO_SELLER

Dispute → RESOLVED
Escrow → RELEASED
MarketplaceOrder → COMPLETED
Listing → SOLD

B. FULL_REFUND

Dispute → RESOLVED
Escrow → REFUNDED
MarketplaceOrder → REFUNDED

Listing không tự động AVAILABLE.

Sau FULL_REFUND:
Listing → HIDDEN

để Admin/Seller kiểm tra trước khi có thể đăng lại.

9. REJECT_DISPUTE:

được hiểu là khiếu nại bị bác bỏ.

Hậu điều kiện:

Dispute → REJECTED
Escrow → RELEASED
MarketplaceOrder → COMPLETED
Listing → SOLD

10. PARTIAL_REFUND và RETURN_AND_REFUND:

giữ trong thiết kế mở rộng,
không implement đầy đủ trong MVP cho tới khi có thiết kế return/refund flow tương ứng.

Không viết logic giả một nửa.

Needs CHANGELOG:
YES

State-machine impact:
YES
```

### Rationale

Giải quyết mâu thuẫn D06 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Dispute, Escrow, MarketplaceOrder, Shipment.
- Database/model: Không thêm partial/return refund model MVP.
- API: Ba resolution MVP và hậu điều kiện; deferred action không xử lý.
- Frontend: Hiển thị trạng thái/ẩn lựa chọn Post-MVP.
- Security: Buyer/Seller ownership, HELD, Admin resolution.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Eligibility; FULL_REFUND HIDDEN; rejected complaint RELEASED/COMPLETED; repeat resolution. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-007 — LISTING REJECT / RESUBMIT / VERIFIED INVALIDATION

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D07; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.6 → v1.7

### Old design (historical, không dùng cho implementation)

Seller được sửa REJECTED nhưng chỉ submit khi DRAFT; không nêu action chuyển lại DRAFT. Sửa AVAILABLE có thể cần PENDING_REVIEW. Badge dựa trên completed inspection/report, chưa nói rõ hiệu lực khi condition/images thay đổi sau kiểm định.

### Problem

Listing bị reject có thể không gửi lại được; report cũ có thể không còn phản ánh dữ liệu đang hiển thị.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Listing transitions bổ sung:

REJECTED → PENDING_REVIEW

khi Seller sửa hợp lệ và submit lại.

Seller được edit:

- DRAFT
- REJECTED

Seller không được edit transaction-critical fields khi:

- RESERVED
- SOLD

Nếu Listing = AVAILABLE và Seller thay đổi field quan trọng:

- sellingPrice
- condition
- description
- product/model information
- repairHistory
- serial
- images quan trọng

thì:

AVAILABLE → PENDING_REVIEW

và cần Admin review lại.

Inspection/Verified rule:

Nếu sau Inspection COMPLETED Seller thay đổi các field có thể ảnh hưởng kết quả kiểm định, gồm tối thiểu:

- condition
- model
- serial
- repairHistory
- frame/condition related data
- inspection-relevant images

thì Verified Badge cũ không còn hiệu lực cho trạng thái Listing mới.

Không xóa InspectionReport cũ.

Report vẫn tồn tại trong lịch sử.

Listing cần Inspection mới nếu Seller muốn Verified Badge trở lại.

Needs CHANGELOG:
YES
```

### Rationale

Giải quyết mâu thuẫn D07 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Listing, Inspection, Valuation.
- Database/model: Giữ lịch sử report; không thêm cột ngoài quyết định.
- API: Submit REJECTED; edit AVAILABLE yêu cầu review.
- Frontend: Ẩn badge mất hiệu lực; báo cần kiểm định lại.
- Security: Owner/state checks; không nhận verified=true.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Resubmit; critical edits; RESERVED/SOLD immutable; invalid report không trở lại current. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-008 — CANONICAL API CONTRACT & FIELD NAMES

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D08; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.7 → v1.8

### Old design (historical, không dùng cho implementation)

các cặp sau cùng tồn tại:
  - Cancel Order: `PUT /api/orders/{id}/cancel` và `POST /api/orders/{id}/cancel`.
  - Valuation: `GET /api/valuations/{listingId}` và `GET /api/valuations/listing/{listingId}`.
  - Inspection: `PUT /api/inspections/{id}` tổng quát và các endpoint action accept/start/complete.
  - Marketplace history: `/api/market-orders/my` và `/buying`, `/selling`.
  - Escrow payment: `/api/escrow/pay` và `/api/escrow/{marketOrderId}/pay`.
  - Evidence upload: `/evidence` và `/evidences`.
  - Field: `thumbnail`/`thumbnail_url`, `max_tension`/`max_tension_lbs`, `racket_length`/`racket_length_mm`, `string_tension`/`string_tension_lbs`; chưa nêu rõ đâu là alias DTO, đâu là field database.

### Problem

Rules xác định bảo mật/layering/response convention nhưng không chọn toàn bộ URI/method hay mapping field này. Không tự coi một đoạn cùng cấp là phiên bản được phê duyệt mới hơn.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Canonical API cho các conflict đã phát hiện:

--------------------------------
Shop Order Cancel
--------------------------------

POST /api/orders/{id}/cancel

Không dùng:

PUT /api/orders/{id}/cancel

--------------------------------
Valuation
--------------------------------

POST /api/valuations

GET /api/valuations/listing/{listingId}

Không dùng:

GET /api/valuations/{listingId}

--------------------------------
Inspection
--------------------------------

Không dùng generic update:

PUT /api/inspections/{id}

cho state transition.

Dùng action API:

POST /api/inspector/inspections/{id}/accept

POST /api/inspector/inspections/{id}/start

POST /api/inspector/inspections/{id}/report

Seller:

POST /api/inspections

GET /api/inspections/my

--------------------------------
MarketplaceOrder History
--------------------------------

GET /api/market-orders/buying

GET /api/market-orders/selling

GET /api/market-orders/{id}

Không dùng generic:

GET /api/market-orders/my

--------------------------------
Escrow payment simulation
--------------------------------

POST /api/escrow/{marketOrderId}/pay

Không dùng:

POST /api/escrow/pay

--------------------------------
Dispute Evidence
--------------------------------

POST /api/disputes/{id}/evidences

Dùng plural:

evidences

--------------------------------
Canonical Database Field Naming
--------------------------------

Sử dụng tên có đơn vị/ý nghĩa rõ:

thumbnail_url

max_tension_lbs

racket_length_mm

string_tension_lbs

Java/DTO tương ứng:

thumbnailUrl
maxTensionLbs
racketLengthMm
stringTensionLbs

Không dùng hai tên song song như alias không được định nghĩa.

--------------------------------

Response vẫn phải dùng wrapper chuẩn:

success
message
data

hoặc error:

success
code
message
errors

Generic Escrow release/refund không được expose cho Buyer.

Buyer chỉ gọi confirm-received.

Admin/service action phải kiểm tra permission/state.

Needs CHANGELOG:
YES

Breaking API normalization:
YES

Vì implementation chưa bắt đầu nên không cần backward compatibility runtime.
```

### Rationale

Giải quyết mâu thuẫn D08 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: API/DTO, các module liên quan.
- Database/model: Chuẩn hóa bốn tên field có đơn vị.
- API: Canonical methods/URIs/wrappers; breaking design-only.
- Frontend: Cập nhật service mapping; không alias cũ.
- Security: Permission/state; không generic escrow release cho Buyer.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Contract routes/verbs; wrappers; tên field; unauthorized actions. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-009 — RELATIONSHIP OPTIONALITY

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D09; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.8 → v1.9

### Old design (historical, không dùng cho implementation)

User–PlayerProfile và User–Cart có chỗ 1:1, chỗ 0..1; Product–RacketSpecification có chỗ 1:1 dù catalog gồm cả sản phẩm không phải vợt; một số tập Address/Order/CartItem được viết 1..N.

### Problem

chưa rõ ký hiệu 1:N chỉ là mô tả loại quan hệ hay bắt buộc tối thiểu một bản ghi; nếu hiểu theo min/max chặt, user mới/cart rỗng hoặc sản phẩm không phải vợt không đáp ứng.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Multiplicity chính thức:

User 1 → 0..1 PlayerProfile

User 1 → 0..1 Cart

User 1 → 0..N Address

User 1 → 0..N Order

User 1 → 0..N Review

User 1 → 0..N Favorite

Cart 1 → 0..N CartItem

Order 1 → 1..N OrderItem
sau khi Order được tạo thành công.

Product 1 → 0..N ProductImage

Product 1 → 0..1 RacketSpecification

RacketSpecification chỉ tồn tại cho Product thuộc loại vợt.

Category 1 → 0..N Product

Brand 1 → 0..N Product

UsedListing 1 → 0..N UsedListingImage

UsedListing 1 → 0..N Valuation

UsedListing 1 → 0..N InspectionRequest
theo D01.

UsedListing 1 → 0..N MarketplaceOrder
theo D02.

MarketplaceOrder 1 → 1 EscrowTransaction

MarketplaceOrder 1 → 0..1 Shipment

MarketplaceOrder 1 → 0..1 Dispute trong MVP

MarketplaceOrder 1 → 0..1 SellerRating

Dispute 1 → 0..N DisputeEvidence

Cart creation:

Có thể lazy-create Cart khi User lần đầu sử dụng Cart.

Không bắt buộc Cart phải được tạo ngay khi register.

PlayerProfile là optional cho User nói chung,
nhưng required khi sử dụng Recommendation.

Needs CHANGELOG:
YES

Database/model impact:
YES
```

### Rationale

Giải quyết mâu thuẫn D09 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: User, Cart, Catalog, Marketplace.
- Database/model: Optional cardinalities và FK/unique đúng quan hệ.
- API: Profile optional tới khi recommendation; lazy cart.
- Frontend: User mới/cart rỗng hợp lệ.
- Security: User ownership khi lazy-create.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Register không cart/profile; empty cart; order có items; non-racket product. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-010 — SHOP ORDER CANCEL / COMPLETE SIDE EFFECTS

**Type:** DATABASE / API / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D10; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.9 → v1.10

### Old design (historical, không dùng cho implementation)

rule cho cancel PENDING, có thể cho CONFIRMED nếu chưa chuẩn bị; API mô tả PENDING/CONFIRMED. Luồng SHIPPING→COMPLETED có nhưng actor/action hoàn thành và tác động cancel lên tồn kho/payment chưa được chốt nhất quán.

### Problem

không thể xác định chắc chắn khi nào hủy hợp lệ, hoàn stock/tiền ra sao hoặc ai được hoàn thành đơn chỉ từ enum.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Shop Order flow chính:

PENDING
→ CONFIRMED
→ PREPARING
→ SHIPPING
→ COMPLETED

User được cancel khi:

PENDING

hoặc:

CONFIRMED

nhưng chỉ trước khi Order chuyển PREPARING.

Không cancel từ:

PREPARING
SHIPPING
COMPLETED
CANCELLED

Khi cancel hợp lệ:

1. Lock Order.
2. Validate owner.
3. Validate state.
4. Restore stock của từng OrderItem.
5. Update payment state nếu cần.
6. Order → CANCELLED.
7. Commit transaction.

Nếu:

paymentStatus = UNPAID

thì không cần refund.

Nếu:

paymentStatus = PAID

thì:

paymentStatus → REFUNDED

theo payment simulation/MVP policy.

Stock phải được restore đúng một lần.

Double cancel phải bị reject/idempotent theo implementation policy.

Actor quản lý fulfillment:

ADMIN hoặc backend-authorized operation.

USER không tự gửi arbitrary orderStatus.

ADMIN xử lý:

CONFIRMED
PREPARING
SHIPPING
COMPLETED

qua action/status API được Backend validate.

Needs CHANGELOG:
YES
```

### Rationale

Giải quyết mâu thuẫn D10 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Shop Order, Product stock, Payment.
- Database/model: Không thêm entity; stock/payment update trong transaction.
- API: Cancel PENDING/CONFIRMED; Admin fulfillment.
- Frontend: Không cho cancel PREPARING trở đi.
- Security: Owner + lock + state; không nhận arbitrary user status.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Cancel từng state; concurrent/double cancel; PAID refund; UNPAID không refund. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-011 — REFRESH TOKEN SCOPE

**Type:** SECURITY / SCOPE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D11; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.10 → v1.11

### Old design (historical, không dùng cho implementation)

security ghi “có thể bổ sung Refresh Token”, trong khi login response và API đã liệt kê refresh token/refresh endpoint; logout có câu “nếu dùng refresh token”. Chưa có mô hình persistence/revocation tương ứng được chốt.

### Problem

Auth slice có thể được triển khai theo hai contract khác nhau; không tự thêm entity/session store hoặc quyết định logout chỉ xóa token ở client.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

MVP Authentication sử dụng:

JWT Access Token

Không triển khai Refresh Token trong MVP.

Do đó loại khỏi MVP contract:

POST /api/auth/refresh

RefreshTokenRequest

Refresh Token persistence/revocation.

Auth API MVP:

POST /api/auth/register

POST /api/auth/login

POST /api/auth/logout

GET /api/users/me

PUT /api/users/me

PUT /api/users/change-password

Logout trong MVP:

- Frontend xóa authentication state/token.
- Backend không giả vờ revoke Access Token nếu không có server-side token store.

Access Token cần expiration hữu hạn.

Refresh Token được đưa sang:

Post-MVP / Future Security Enhancement.

Nếu sau này thêm Refresh Token:

phải tạo Architecture/Security Change riêng,
bao gồm:

- storage;
- expiration;
- rotation;
- revocation;
- logout;
- frontend storage;
- security tests.

JWT và system roles không thay đổi.

Needs CHANGELOG:
YES

Scope/Security decision:
YES
```

### Rationale

Giải quyết mâu thuẫn D11 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Auth, User, Security.
- Database/model: Không refresh-token storage MVP.
- API: Bỏ refresh contract; login chỉ access token.
- Frontend: 401 đăng nhập lại; logout xóa token/state.
- Security: Finite expiry; không tuyên bố revoke JWT stateless.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Login payload; expired JWT; logout UX; refresh không thuộc MVP. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-012 — PROMOTIONS

**Type:** SCOPE / DOCUMENTATION

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D12; phê duyệt trực tiếp bởi User.

**Architecture Version:** v1.11 → v1.12

### Old design (historical, không dùng cho implementation)

còn “quản lý khuyến mãi”/Promotions trong Admin nhưng không có thiết kế entity, module, API và quy tắc giảm giá tương ứng trong baseline được đối chiếu.

### Problem

chưa đủ cơ sở coi đây là phần thừa để xóa hoặc tính năng bắt buộc để tự thêm.

### Approved new design — nguyên văn Decision Matrix

```text
Decision:
APPROVED

Promotions không thuộc MVP/baseline hiện tại.

Các dòng còn sót như:

- quản lý khuyến mãi;
- /admin/promotions;
- Promotion admin screen;

được xác định là LEGACY DOCUMENTATION.

Hãy xóa chúng khỏi phần mô tả chức năng hiện tại.

Không tạo:

- Promotion Entity;
- Promotion table;
- Promotion API;
- Promotion module;
- discount calculation;

chỉ để hợp thức hóa nội dung legacy.

Có thể ghi Promotions trong:

Future Development

nếu muốn.

Product price và Shop Order total vẫn do Backend quyết định.

Needs CHANGELOG:
YES

Classification:
Scope clarification / documentation cleanup.
```

### Rationale

Giải quyết mâu thuẫn D12 theo phương án User phê duyệt, giữ ranh giới Product/UsedListing và Order/MarketplaceOrder; không mở rộng ngoài quyết định.

### Impact

- Affected modules: Scope, Admin, tài liệu.
- Database/model: Không tạo Promotion table/entity.
- API: Không Promotion API.
- Frontend: Loại current Admin promotions route/screen.
- Security: Giá và tổng do Backend tính.
- Migration: Chưa có schema/runtime. Phản ánh thay đổi vào thiết kế migration ban đầu khi implementation được bắt đầu; nếu áp dụng cho schema đã tồn tại phải kiểm tra dữ liệu và constraint trước migration. Không tạo/chạy migration trong lần này.
- Tests cần triển khai/chạy sau: Scope audit; không route/module/payload promotion trong MVP. Document verification hiện tại không thay thế runtime tests.
- Documentation: 00_PROJECT_STRUCTURE, Master DOCX/MD, 02_IMPLEMENTATION_RULES, 03_IMPLEMENTATION_STATUS, DOCUMENT_VERIFICATION_REPORT; specification ngoài bộ nguồn chỉ cập nhật khi được xác định là specification chính thức.

### Rollback consideration

Không rollback runtime vì chưa implementation. Thay đổi hoặc rút lại quyết định cần Change APPROVED mới; không phục hồi thiết kế cũ trái quyết định hiện hành bằng sửa tài liệu tùy ý.

### Final Decision

APPROVED. Implementation: NOT_STARTED.

## CHG-013 — TARGET FLEX & EXTRA_STIFF

**Type:** RECOMMENDATION / BUSINESS_RULE

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D13; User phê duyệt trực tiếp trong nội dung đính kèm của chat này. Không yêu cầu phê duyệt lại.

**Architecture Version:** v1.12 → v1.13

### Old design (historical)

TargetFlex chưa duy nhất; Flex order chỉ ba mức trong khi catalog/Wrist table có EXTRA_STIFF.

### Problem

Không tính được FlexScore deterministic trên toàn bộ enum.

### Approved new design — nguyên văn User

```text
Status:
APPROVED

Problem:

Flex Score đã được quyết định dùng khoảng cách giữa targetFlex và
racket.shaftFlexibility nhưng chưa có rule duy nhất để xác định targetFlex.

Tài liệu hiện cũng sử dụng EXTRA_STIFF trong Wrist–Flex Compatibility,
trong khi mapping Flex trước đó chỉ có FLEXIBLE → MEDIUM → STIFF.

Approved New Design:

1. ShaftFlexibility enum chính thức:

FLEXIBLE
MEDIUM
STIFF
EXTRA_STIFF

2. Flex Score và Wrist Score là hai tiêu chí riêng.

Wrist Score:
- đánh giá compatibility giữa wristStrength và shaftFlexibility;
- giữ bảng Wrist–Flex Compatibility đã được thiết kế.

Flex Score:
- KHÔNG suy targetFlex từ wristStrength;
- targetFlex chỉ suy từ skillLevel.

Lý do:
wristStrength đã có trọng số riêng 15%.
Nếu tiếp tục dùng wristStrength để tạo targetFlex cho Flex 10% thì
cùng một yếu tố bị tính hai lần.

3. Mapping targetFlex chính thức:

BEGINNER
→ FLEXIBLE

INTERMEDIATE
→ MEDIUM

ADVANCED
→ STIFF

4. Thứ tự Flex:

FLEXIBLE = 0
MEDIUM = 1
STIFF = 2
EXTRA_STIFF = 3

5. Flex Score:

distance =
abs(targetFlex.ordinal - racketFlex.ordinal)

distance = 0
→ 10

distance = 1
→ 6

distance >= 2
→ 2

6. EXTRA_STIFF không phải target mặc định của Skill Level.

Nó vẫn có thể được Recommendation đánh giá phù hợp với User Advanced /
Strong Wrist nhờ:

- Skill/Flex compatibility;
- Wrist–Flex compatibility;

nhưng không tạo thêm Skill Level mới.

7. Recommendation weights không thay đổi:

Skill Level       20
Play Style        25
Wrist Strength    15
Preferred Weight  15
Shaft Flexibility 10
Budget            15

Total             100

8. MatchScoreCalculator phải deterministic.

9. Bổ sung Unit Test tối thiểu:

Beginner + Flexible = 10
Beginner + Medium = 6
Beginner + Stiff = 2
Beginner + Extra Stiff = 2

Intermediate + Flexible = 6
Intermediate + Medium = 10
Intermediate + Stiff = 6
Intermediate + Extra Stiff = 2

Advanced + Flexible = 2
Advanced + Medium = 6
Advanced + Stiff = 10
Advanced + Extra Stiff = 6

10. Không sửa Wrist–Flex table trừ khi phát hiện mâu thuẫn mới độc lập.

Create:
CHG-013

Type:
RECOMMENDATION / BUSINESS_RULE

Status:
APPROVED
```

### Rationale

Skill quyết định target; tránh đếm wristStrength hai lần.

### Impact

- Database/model: Giữ enum bốn giá trị đã có; không đổi bảng Wrist hoặc weights.
- API/DTO: MatchScore/reasons theo skill-derived target và distance >=2.
- Frontend: Hiển thị score/reasons Backend; không tự tính target.
- Service/Repository/Calculator: thực hiện đúng CHG-013 trong vertical slice tương ứng, chưa code.
- Security/Audit: Backend authoritative; reject invalid skill/flex theo validation hiện hành.
- Migration: schema chưa triển khai; cập nhật thiết kế migration ban đầu, không tạo/chạy migration. Nếu có dữ liệu cũ, không tự gán attribution thiếu bằng SELLER/BUYER.
- Tests dự kiến: 12 tổ hợp skill × flex; target không đổi theo wrist; weights/bounds; bảng Wrist không đổi. Chưa chạy runtime tests.
- Documentation: Structure, Master DOCX/MD, Rules, Status, Verification Report.

### Final Decision và rollback

APPROVED; implementation NOT_STARTED. Thay đổi/rút lại quyết định cần Change mới; không tự phục hồi rule cũ.

## CHG-014 — SELLER REPUTATION RATE POPULATION

**Type:** BUSINESS_RULE / DATABASE / SELLER_REPUTATION

**Severity:** MAJOR

**Status:** APPROVED

**Date:** 2026-10-05

**Decision:** D14; User phê duyệt trực tiếp trong nội dung đính kèm của chat này. Không yêu cầu phê duyệt lại.

**Architecture Version:** v1.13 → v1.14

### Old design (historical)

Terminal outcome population/attribution/mẫu số 0 chưa xác định.

### Problem

Rate/reputation có thể khác nhau, chưa có dữ liệu có thể bị hiểu nhầm 0%.

### Approved new design — nguyên văn User

```text
Status:
APPROVED

Problem:

Dispute Rate và Cancellation Rate chưa xác định:
- tập MarketplaceOrder nào được dùng làm denominator;
- cancellation nào thực sự phản ánh Seller;
- xử lý denominator = 0.

Approved New Design:

1. Định nghĩa:

eligibleReputationTransactions

là các MarketplaceOrder của Seller đã có business outcome đủ để đánh giá
độ uy tín Seller.

Bao gồm:

COMPLETED

REFUNDED

CANCELLED chỉ khi cancellation được xác định là seller-attributable.

2. Không đưa các order đang hoạt động vào denominator:

PENDING_PAYMENT
PAID
WAITING_SHIPMENT
SHIPPING
DELIVERED
DISPUTED đang chưa resolve

3. Không đưa các cancellation sau vào reputation denominator/numerator:

Buyer cancel trước payment
Payment timeout
System cancel trước payment

vì chúng không phản ánh Seller reliability.

4. Cần lưu hoặc có dữ liệu tương đương đủ tin cậy để xác định cancellation source:

cancelledByType

Allowed business values:

BUYER
SELLER
ADMIN
SYSTEM

Đây không phải System Role.
System Role vẫn:

USER
INSPECTOR
ADMIN

5. Có thể lưu:

cancellationReason

Ví dụ:

BUYER_CHANGED_MIND
PAYMENT_TIMEOUT
SELLER_CANNOT_FULFILL
ADMIN_CANCELLED
SYSTEM_CANCELLED
OTHER

Backend quyết định cancelledByType/cancellationReason từ action nghiệp vụ.
Không tin giá trị do Frontend tùy ý gửi.

6. Seller-attributable cancellation tối thiểu gồm:

cancelledByType = SELLER

hoặc:

ADMIN cancellation được đánh dấu nguyên nhân do Seller không thể thực hiện giao dịch.

7. Dispute Rate:

disputeTransactionCount =
COUNT DISTINCT eligible MarketplaceOrder có Dispute record

disputeRate =
disputeTransactionCount
/
eligibleReputationTransactionCount
× 100

8. Cancellation Rate:

sellerCancellationCount =
COUNT eligible MarketplaceOrder bị seller-attributable cancellation

cancellationRate =
sellerCancellationCount
/
eligibleReputationTransactionCount
× 100

9. Một MarketplaceOrder chỉ được tính một lần trong denominator.

10. denominator = 0:

Nếu:

eligibleReputationTransactionCount = 0

thì:

disputeRate = null
cancellationRate = null

KHÔNG gán 0%.

Lý do:

0% nghĩa là có dữ liệu và không xảy ra incident.

null nghĩa là chưa có dữ liệu để tính.

11. Khi disputeRate = null:

Dispute Score = 0

12. Khi cancellationRate = null:

Cancellation Score = 0

13. Seller chưa có eligible transaction:

Frontend hiển thị:

NEW SELLER

Dispute Rate:
Chưa có dữ liệu

Cancellation Rate:
Chưa có dữ liệu

14. Completed Transaction Score vẫn chỉ dựa trên:

MarketplaceOrder = COMPLETED

15. Average Rating vẫn dựa trên SellerRating hợp lệ.

16. Reputation vẫn giữ tổng trọng số:

Rating             40
Completed Orders   20
Dispute            20
Cancellation       10
Account Age        10

Không thay trọng số.

17. Unit tests tối thiểu:

- denominator = 0
  → disputeRate null
  → cancellationRate null
  → disputeScore 0
  → cancellationScore 0

- Buyer cancel before payment
  → không làm tăng Seller cancellation rate

- payment timeout
  → không làm tăng Seller cancellation rate

- Seller attributable cancel
  → cancellation numerator +1

- COMPLETED without dispute
  → denominator +1
  → dispute numerator unchanged

- COMPLETED with dispute history
  → denominator +1
  → dispute numerator +1

- REFUNDED after dispute
  → denominator +1
  → dispute numerator +1

- active order
  → không tính vào denominator

18. Nếu cần thêm field database cancelledByType/cancellationReason:

đây là phần của CHG-014 và phải cập nhật:

Database schema
Entity
DTO nếu cần
Service
Audit
Tests
Master documents

Frontend không được quyết định các field này.

Create:
CHG-014

Type:
BUSINESS_RULE / DATABASE / SELLER_REPUTATION

Status:
APPROVED
```

### Rationale

Chỉ đánh giá Seller trên outcome đủ dữ liệu; phân biệt không có dữ liệu và không có incident.

### Impact

- Database/model: marketplace_orders: cancelled_by_type VARCHAR(30) NULL, cancellation_reason VARCHAR(100) NULL; dữ liệu do Backend gán.
- API/DTO: Response DTO có cancelledByType/cancellationReason khi cần; rate nullable, không thêm endpoint/quyền cancel.
- Frontend: NEW SELLER khi không có eligible transaction; null rate hiển thị Chưa có dữ liệu.
- Service/Repository/Calculator: thực hiện đúng CHG-014 trong vertical slice tương ứng, chưa code.
- Security/Audit: Backend xác định attribution từ action; không tin client; Audit nguồn/nguyên nhân.
- Migration: schema chưa triển khai; cập nhật thiết kế migration ban đầu, không tạo/chạy migration. Nếu có dữ liệu cũ, không tự gán attribution thiếu bằng SELLER/BUYER.
- Tests dự kiến: zero sample; exclusion Buyer/timeout/System; seller/Admin-attributed; completed/refunded dispute history; active excluded; distinct count. Chưa chạy runtime tests.
- Documentation: Structure, Master DOCX/MD, Rules, Status, Verification Report.

### Final Decision và rollback

APPROVED; implementation NOT_STARTED. Thay đổi/rút lại quyết định cần Change mới; không tự phục hồi rule cũ.

## PHẦN bổ sung — quyết định D15–D17, phê duyệt 2026-10-08

Người dùng xác nhận “Ok thực hiện đi” sau đề nghị áp dụng cả ba phương án. Các quyết định dưới đây thay thế phần chưa xác định tương ứng; D01–D14 và các module khác giữ nguyên.

### CHG-015 / D15 — Valuation (APPROVED)

Thiết kế cũ thiếu dữ liệu phụ kiện/sửa chữa và tiêu chí matching. POST /api/valuations nhận listingId, accessoryCondition (FULL/PARTIAL/MANY_MISSING), repairLevel (NONE/MINOR/STRUCTURAL); hai phân loại bắt buộc. Backend ánh xạ 1.03/1.00/0.97 và 1.00/0.90/0.70. Report kiểm định hợp lệ ưu tiên condition score/grade; repairDetected và repairLevel phải nhất quán. Khi không có report hợp lệ dùng grade người bán; không tái dùng score của report đã vô hiệu. Chỉ match Product ACTIVE cùng brandId, tên sau trim không phân biệt hoa thường, duy nhất một kết quả. Dùng effectivePrice của backend; 0 hoặc nhiều kết quả dùng originalPrice. Không fuzzy matching. Ngày hết bảo hành ưu tiên; không rõ dùng 0.97, hết hạn 1.00. Snapshot lưu factor, nguồn và phân loại trong note, không thay lịch sử. Ảnh hưởng DTO/service/UI/tests, không thêm Entity hay relationship.

### CHG-016 / D16 — Category/Brand (APPROVED)

Status cũ nullable chưa có tập giá trị. Chốt ACTIVE/INACTIVE; tạo ACTIVE, update cho phép hai giá trị; Brand DELETE là INACTIVE, không hard delete. Public chỉ xem ACTIVE; ADMIN xem tất cả. Không cascade trạng thái sang Product/UsedListing. Migration V4 backfill NULL thành ACTIVE, đặt default/NOT NULL/check; không sửa migration đã áp dụng. Ảnh hưởng enum/DTO/service/UI/tests.

### CHG-017 / D17 — API bổ trợ (APPROVED)

Mở POST /api/market-orders/{id}/cancel cho Buyer sở hữu, chỉ PENDING_PAYMENT + WAITING_PAYMENT; backend gán BUYER/BUYER_CHANGED_MIND. Mở GET /api/admin/audit-logs?page=0&size=20 chỉ ADMIN, size tối đa 100. Không thay state machine hoặc quyền release/refund. Ảnh hưởng controller/UI/API tests.

Rollback: thay đổi quyết định cần CHANGE mới; không tự phục hồi quy tắc cũ. Cập nhật Implementation Status theo kết quả kiểm thử, không tự đánh dấu toàn bộ DONE.
