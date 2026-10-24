# 01_SYSTEM_ANALYSIS_MASTER.md

# XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ CẦU LÔNG ĐA NỀN TẢNG TÍCH HỢP CHỨC NĂNG GỢI Ý SẢN PHẨM THEO NHU CẦU NGƯỜI DÙNG

> Đây là bản Markdown đồng hành với `01_SYSTEM_ANALYSIS_MASTER.docx`, tối ưu cho Codex/AI và quá trình triển khai source code. File này không thay thế bản Word dùng cho báo cáo. Nội dung được chuẩn hóa theo Architecture Baseline v1.0 + CHG-001–CHG-014 (document version v1.14) và các quy tắc triển khai hiện hành.
>
> Khi các nguồn có khác biệt, áp dụng thứ tự ưu tiên: **Quyết định APPROVED mới nhất trong CHANGELOG → IMPLEMENTATION_RULES → SYSTEM_ANALYSIS_MASTER (DOCX và MD) → PROJECT_STRUCTURE**. Source code chỉ phản ánh implementation hiện tại. Không tự ưu tiên DOCX hoặc MD khi hai bản mâu thuẫn mà nguồn cấp cao hơn chưa phân định. D01–D14 đã được phê duyệt tại CHG-001–CHG-014. D13/D14 đã APPROVED tại CHG-013/014; Pending Architecture Issues = 0.

---

# PHẦN 1. TỔNG QUAN HỆ THỐNG

## 1.1. Tên đề tài chính thức

**Xây dựng hệ thống thương mại điện tử cầu lông đa nền tảng tích hợp chức năng gợi ý sản phẩm theo nhu cầu người dùng.**

Tên đề tài không thay đổi khi bổ sung các chức năng mở rộng như Marketplace, Valuation, Inspection, simulated Escrow, Shipment, Dispute và Seller Reputation.

## 1.2. Bài toán

Hệ thống giải quyết hai nhóm nhu cầu chính:

1. Người chơi cần một nền tảng thương mại điện tử chuyên biệt để xem, tìm kiếm, lọc, so sánh, mua và đánh giá dụng cụ cầu lông.
2. Người dùng thường khó xác định sản phẩm, đặc biệt là vợt, có phù hợp với trình độ, lối chơi, lực cổ tay, trọng lượng mong muốn và ngân sách hay không.

Ngoài chức năng trọng tâm trên, hệ thống mở rộng sang Marketplace đồ cầu lông đã qua sử dụng để hỗ trợ đăng bán, định giá, kiểm định, giao dịch có cơ chế trung gian mô phỏng, xử lý tranh chấp và đánh giá người bán.

## 1.3. Mục tiêu

### Thương mại điện tử
- Xem danh mục và sản phẩm.
- Tìm kiếm, lọc, sắp xếp.
- Xem chi tiết sản phẩm.
- Quản lý giỏ hàng.
- Checkout và tạo đơn hàng.
- Theo dõi lịch sử và trạng thái đơn hàng.
- Review và Favorite.
- Quản trị sản phẩm, đơn hàng và người dùng.

### Cá nhân hóa
- Tạo `PlayerProfile`.
- Phân tích nhu cầu người chơi.
- Tính `Match Score`.
- Xếp hạng sản phẩm theo mức độ phù hợp.
- Trả về lý do gợi ý có thể giải thích.

### Marketplace mở rộng
- USER có thể đăng bán đồ cầu lông cũ.
- USER khác có thể mua Listing hợp lệ.
- Hỗ trợ Valuation và Condition Score.
- Seller có thể yêu cầu Inspection.
- Listing đã kiểm định có thể nhận Verified Badge theo rule Backend.
- Giao dịch Marketplace sử dụng `MarketplaceOrder` riêng và simulated Escrow.
- Có Shipment, Dispute, Evidence, Seller Rating và Seller Reputation.

## 1.4. Phạm vi đa nền tảng

Kiến trúc định hướng:

`Web Client → REST API ← Mobile Client`

Trong phạm vi hiện tại:
- Web là nền tảng triển khai chính.
- Mobile có thể phát triển sau.
- Backend REST API phải độc lập với UI Web để tái sử dụng cho Mobile.

## 1.5. Các luồng nghiệp vụ lớn

### Shop B2C
`Product → Cart → Checkout → Order → Shipping → Completed`

### Recommendation
`PlayerProfile → RecommendationService → Product + RacketSpecification → MatchScoreCalculator → Recommendation Result`

### Marketplace C2C
Seller: `Create Listing → Valuation → Submit → Admin Review → AVAILABLE`

Buyer: `Marketplace → Listing Detail → Purchase → MarketplaceOrder → Escrow → Shipment → Confirm`

### Transaction Protection
`Payment Simulation → Escrow HELD → Shipment → Buyer Confirmation → RELEASED`

Nếu có vấn đề:
`Dispute → Escrow DISPUTED → Admin Resolution → RELEASED / REFUNDED`

Escrow trong đồ án là **cơ chế trung gian giao dịch mô phỏng**, không phải ngân hàng giữ tiền thật.

---

# PHẦN 2. ACTOR, ROLE VÀ USE CASE

## 2.1. Actor

### Guest
- xem trang chủ;
- xem/tìm kiếm/lọc sản phẩm;
- xem chi tiết Product và Marketplace Listing;
- đăng ký;
- đăng nhập.

### USER
USER có thể đồng thời đóng vai trò nghiệp vụ Customer, Buyer và Seller. **BUYER và SELLER không phải System Role riêng.**

USER có thể:
- quản lý tài khoản và Address;
- quản lý PlayerProfile;
- quản lý Cart và Shop Order;
- Favorite/Review;
- nhận Recommendation;
- tạo và quản lý UsedListing;
- yêu cầu Valuation/Inspection;
- mua Listing của người khác;
- theo dõi MarketplaceOrder;
- tạo Shipment khi là Seller;
- xác nhận nhận hàng khi là Buyer;
- tạo Dispute;
- đánh giá Seller sau giao dịch hợp lệ.

### INSPECTOR
- xem Inspection Request;
- nhận yêu cầu;
- bắt đầu kiểm định;
- nhập InspectionReport;
- xác minh model/serial/tình trạng;
- hoàn thành kiểm định.

### ADMIN
- quản lý User, Category, Brand, Product;
- quản lý Shop Order;
- quản lý Review;
- quản lý Recommendation configuration;
- duyệt Marketplace Listing;
- quản lý Inspection, Escrow, Dispute;
- xem Statistics và Audit Log.

## 2.2. System Role
- `USER`
- `INSPECTOR`
- `ADMIN`

## 2.3. Nhóm Use Case chính

### Shop
Đăng ký, đăng nhập, quản lý tài khoản, tra cứu sản phẩm, tìm kiếm, lọc, xem chi tiết, so sánh, Favorite, Cart, Checkout, Order, Review.

### Recommendation
Tạo/cập nhật PlayerProfile, nhận Recommendation, tính Match Score, xem lý do phù hợp.

### Marketplace
Tạo UsedListing, upload ảnh, chỉnh sửa theo status, submit, approve/reject, Valuation, Inspection, purchase, MarketplaceOrder, Escrow, Shipment, confirm received, Dispute, Evidence, Seller Rating.

### Admin
Quản lý dữ liệu Shop, User, Marketplace, Inspection, Escrow, Dispute, Recommendation, Statistics và Audit.

---

# PHẦN 3. KIẾN TRÚC HỆ THỐNG

## 3.1. Technology Stack

Frontend:
- React
- React Router
- Axios
- React Hook Form hoặc tương đương
- Context API hoặc Zustand khi phù hợp

Backend:
- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- JWT

Database:
- MySQL

Khác:
- REST/JSON
- Swagger/OpenAPI
- Postman
- Cloudinary hoặc Object Storage tương đương
- Git/GitHub

## 3.2. Architecture Style

Backend sử dụng **Modular Monolith**.

Luồng chuẩn:
`Frontend → Controller → Request DTO → Service → Repository → Entity → MySQL`

Response:
`Entity → Mapper → Response DTO → Controller → Frontend`

## 3.3. Trách nhiệm Layer

### Controller
Nhận request, validate DTO, lấy Current User, gọi Service, trả response. Không chứa business logic lớn.

### Service
Business logic, ownership, role/business-state validation, transaction, state transition, phối hợp Repository/Service, Notification/Audit khi cần.

### Repository
Persistence, query, pagination, DB lock. Không quyết định business rule.

### Entity
Ánh xạ persistence. Không dùng làm API contract mặc định.

### DTO
Request chỉ chứa field client được phép gửi; Response không expose field nhạy cảm.

### Mapper
Chuyển DTO ↔ Entity.

### Calculator
- `MatchScoreCalculator`
- `ValuationCalculator`
- `ConditionScoreCalculator`
- `SellerReputationCalculator`

---

# PHẦN 4. MODULE BOUNDARY

Các module chính:
1. Auth
2. User
3. Category
4. Brand
5. Product
6. Cart
7. Order
8. Review
9. Favorite
10. Recommendation
11. Marketplace
12. Valuation
13. Inspection
14. MarketOrder
15. Escrow
16. Shipment
17. Dispute
18. Rating
19. Notification
20. Statistics
21. Audit
22. Security
23. Exception
24. Config
25. Common

Ranh giới bắt buộc:
- **Không gộp `Product` với `UsedListing`.**
- **Không gộp `Order` với `MarketplaceOrder`.**

---

# PHẦN 5. DATABASE VÀ ENTITY

## 5.1. AUTH / USER

### users
- user_id BIGINT PK AI
- full_name VARCHAR(100)
- email VARCHAR(150) UNIQUE
- password_hash VARCHAR(255)
- phone VARCHAR(20)
- avatar_url VARCHAR(500)
- role VARCHAR(30)
- status VARCHAR(30)
- created_at
- updated_at

Role: USER / INSPECTOR / ADMIN.

Status: ACTIVE / LOCKED / SUSPENDED / DELETED.

### addresses
- address_id
- user_id FK
- receiver_name
- receiver_phone
- province
- district
- ward
- detail_address
- is_default
- timestamps

### player_profiles
USER 1:0..1 PlayerProfile.

- profile_id
- user_id UNIQUE FK
- skill_level
- play_style
- play_type
- wrist_strength
- preferred_weight
- min_budget DECIMAL(15,2)
- max_budget DECIMAL(15,2)

## 5.2. SHOP

### categories
category_id, category_name, slug UNIQUE, description, status, timestamps.

### brands
brand_id, brand_name, slug UNIQUE, description, logo_url, status, timestamps.

### products
product_id, category_id FK, brand_id FK, product_name, slug, description, price DECIMAL(15,2), sale_price DECIMAL(15,2), stock, thumbnail_url, status, timestamps.

Product status định hướng: ACTIVE / INACTIVE / OUT_OF_STOCK / DISCONTINUED.

### product_images
image_id, product_id FK, image_url, is_primary.

### racket_specifications
Product 1:0..1 RacketSpecification.

Các field: weight_class, balance_point/balance_type, shaft_flexibility, max_tension_lbs, play_style, skill_level, grip_size, racket_length_mm, material.

### carts / cart_items
Cart thuộc USER; `cart_items` nên có `UNIQUE(cart_id, product_id)`.

### orders / order_items
Shop Order riêng với MarketplaceOrder.

Order lưu receiver/address snapshot và các tổng tiền do Backend tính.

OrderItem lưu snapshot tối thiểu: productName, productImage, unitPrice, quantity.

### reviews
Chỉ cho review khi User đã mua Product trong Order COMPLETED.

### favorites
`UNIQUE(user_id, product_id)`.

## 5.3. RECOMMENDATION

### recommendation_weights
Lưu cấu hình trọng số nếu triển khai bằng DB.

### recommendation_logs
Có thể lưu profile/user, product, match_score, reasons, algorithm_version, timestamp.

## 5.4. MARKETPLACE

### used_listings
listing_id, seller_id FK, category_id FK, brand_id FK, listing_code, product_name, model, description, original_price, selling_price, purchase_date, usage_months, condition_grade, condition_score, warranty_status, warranty_until, status, view_count, timestamps.

### used_racket_specifications
weight_class, grip_size, string_model, string_tension_lbs, frame_condition, paint_condition, grommet_condition, handle_condition, repair_history, serial_number.

### used_listing_images
Image type: GENERAL, FRONT, BACK, FRAME_TOP, FRAME_LEFT, FRAME_RIGHT, HANDLE, SERIAL, DAMAGE.

### valuations
valuation_id, listing_id FK, reference_price, age_factor, condition_factor, warranty_factor, accessory_factor, repair_factor, demand_factor, condition_score, estimated_price, suggested_min_price, suggested_max_price, valuation_method, algorithm_version, created_at. Money DECIMAL(15,2), factor DECIMAL(5,3), condition_score DECIMAL(4,2); algorithm_version VARCHAR(50).

Method: RULE_BASED / AI_ASSISTED / MANUAL.

### inspection_requests / inspection_reports
InspectionRequest lưu listing, seller, inspector, status, requested/completed time.

InspectionReport lưu tình trạng frame/paint/grommet/handle, model/serial verification, repair, overall score, recommended price/range, notes.

## 5.5. MARKETPLACE TRANSACTION

### marketplace_orders
Không dùng bảng `orders` của Shop.

- market_order_id
- order_code
- listing_id FK (index thường; không unique theo CHG-002)
- cancelled_by_type VARCHAR(30) NULL (Backend; business values BUYER/SELLER/ADMIN/SYSTEM)
- cancellation_reason VARCHAR(100) NULL (Backend; reason code theo nghiệp vụ, CHG-014)
- buyer_id FK
- seller_id FK
- item_price
- shipping_fee
- platform_fee
- total_amount
- status
- receiver snapshot
- listing snapshot quan trọng
- timestamps

### escrow_transactions
market_order_id UNIQUE FK, amount, status, paid_at, held_at, released_at, refunded_at.

### shipments
market_order_id UNIQUE FK, carrier, tracking_code, status, shipped_at, delivered_at.

### disputes / dispute_evidences
Dispute liên kết MarketplaceOrder; Evidence có file_url, file_type, description.

### seller_ratings
seller_id, buyer_id, market_order_id UNIQUE, rating, comment, created_at.

## 5.6. SYSTEM

### notifications
DB-based notification cho MVP.

### audit_logs
Ghi các action nhạy cảm như approve/reject Listing, lock/unlock User, complete Inspection, release/refund Escrow, resolve Dispute.

## 5.7. Quy tắc dữ liệu
- Money dùng `DECIMAL(15,2)`; không dùng FLOAT/DOUBLE.
- Ưu tiên soft delete/status cho dữ liệu nghiệp vụ.
- Không hard delete Order, MarketplaceOrder, Escrow, Dispute, InspectionReport sau giao dịch.
- Dùng Foreign Key, Unique Constraint và index phù hợp.

---

# PHẦN 6. RELATIONSHIP TỔNG QUÁT

**Nguồn: CHG-009 / D09 — APPROVED 2026-10-05.**

User 1 → 0..1 PlayerProfile

User 1 → 0..1 Cart

User 1 → 0..N Address

User 1 → 0..N Order

User 1 → 0..N Review

User 1 → 0..N Favorite

Cart 1 → 0..N CartItem

Order 1 → 1..N OrderItem sau khi tạo thành công

Product 1 → 0..N ProductImage

Product 1 → 0..1 RacketSpecification (chỉ Product loại vợt)

Category 1 → 0..N Product

Brand 1 → 0..N Product

UsedListing 1 → 0..N UsedListingImage

UsedListing 1 → 0..N Valuation

UsedListing 1 → 0..N InspectionRequest

UsedListing 1 → 0..N MarketplaceOrder

InspectionRequest 1 → 0..1 InspectionReport (COMPLETED bắt buộc có report)

MarketplaceOrder 1 → 1 EscrowTransaction

MarketplaceOrder 1 → 0..1 Shipment

MarketplaceOrder 1 → 0..1 Dispute trong MVP

MarketplaceOrder 1 → 0..1 SellerRating

Dispute 1 → 0..N DisputeEvidence

Cart có thể lazy-create lần đầu dùng; không bắt buộc tạo khi register. PlayerProfile optional cho User nhưng required khi dùng Recommendation.

---

# PHẦN 7. STATE MACHINE VÀ BUSINESS RULE

## 7.1. Shop Order

**Nguồn: CHG-010 / D10 — APPROVED 2026-10-05.**

Shop Order flow: PENDING → CONFIRMED → PREPARING → SHIPPING → COMPLETED.

User chỉ cancel order của mình khi PENDING hoặc CONFIRMED trước PREPARING. Không cancel từ PREPARING, SHIPPING, COMPLETED, CANCELLED.

Cancel transaction: lock Order → validate owner/state → restore stock từng OrderItem đúng một lần → cập nhật payment nếu cần → Order CANCELLED → commit.

UNPAID không refund; PAID → REFUNDED theo payment simulation MVP. Double cancel phải reject hoặc idempotent theo implementation policy, tuyệt đối không restore stock hai lần.

ADMIN hoặc backend-authorized operation quản lý fulfillment CONFIRMED/PREPARING/SHIPPING/COMPLETED qua API được Backend validate. USER không gửi arbitrary orderStatus.

---

## 7.2. Listing

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

---

## 7.3. MarketplaceOrder
Status: PENDING_PAYMENT, PAID, WAITING_SHIPMENT, SHIPPING, DELIVERED, COMPLETED, CANCELLED, DISPUTED, REFUNDED.

Buyer chỉ mua khi authenticated, ACTIVE, Listing AVAILABLE và Buyer != Seller.

Backend lấy buyerId từ Current User, sellerId và price từ Listing. Không tin Client.

## 7.4. Atomic Marketplace Purchase
1. Begin transaction.
2. Lock UsedListing.
3. Check AVAILABLE.
4. Check Buyer != Seller.
5. Create MarketplaceOrder.
6. Listing → RESERVED.
7. Create EscrowTransaction.
8. Commit.

Lỗi → rollback.

Bảo vệ một giao dịch active cho mỗi Listing bằng Service validation + row lock + transaction + AVAILABLE/RESERVED. Cho phép lịch sử nhiều order sau CANCELLED; không unique marketplace_orders.listing_id.

## 7.5. Escrow

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

---

## 7.6. Shipment
Status: WAITING_PICKUP, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, RETURNED.

Seller chỉ tạo Shipment cho MarketplaceOrder của mình khi Escrow ở trạng thái hợp lệ.

## 7.7. Confirm Received
Điều kiện tối thiểu:
- Current User là Buyer.
- MarketplaceOrder = DELIVERED.
- Escrow = HELD.
- Không có active Dispute.

Transaction:
- MarketplaceOrder → COMPLETED.
- Escrow → RELEASED.
- Listing → SOLD.

## 7.8. Inspection

**Nguồn: CHG-001 / D01 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N InspectionRequest; InspectionRequest 1 → 0..1 InspectionReport.

Một Listing có nhiều request trong lịch sử nhưng tối đa một request active: REQUESTED, ACCEPTED, INSPECTING. COMPLETED, REJECTED, CANCELLED không active.

Seller chỉ tạo request mới cho Listing của mình khi không có request active; Backend kiểm tra đồng thời trong transaction để ngăn request song song.

Mỗi request COMPLETED phải có report. Không xóa lịch sử kiểm định.

currentInspectionReport là report của request COMPLETED mới nhất còn hiệu lực. Verified Badge và Valuation dùng report còn hiệu lực này; hiệu lực sau sửa Listing theo CHG-007.

inspection_requests.listing_id dùng FK/index thường, không có unique trên listing_id.

---

## 7.9. Verified Badge

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

---

## 7.10. Dispute

**Nguồn: CHG-006 / D06 — APPROVED 2026-10-05.**

Current User phải là Buyer hoặc Seller của MarketplaceOrder; Escrow HELD; Order SHIPPING hoặc DELIVERED. MVP một order tối đa một Dispute (do đó tối đa một active dispute).

Khi SHIPPING, NOT_RECEIVED phù hợp nhất cho Buyer; reason khác chỉ chấp nhận khi context nghiệp vụ hợp lệ. DELIVERED cho NOT_AS_DESCRIBED, DAMAGED, SUSPECTED_FAKE, MISSING_ACCESSORIES, WRONG_ITEM, NOT_RECEIVED, OTHER.

Tạo dispute trong transaction: Dispute OPEN; MarketplaceOrder DISPUTED; Escrow DISPUTED; Notification và Audit.

ADMIN resolution:

RELEASE_TO_SELLER → Dispute RESOLVED, Escrow RELEASED, MarketplaceOrder COMPLETED, Listing SOLD.

FULL_REFUND → Dispute RESOLVED, Escrow REFUNDED, MarketplaceOrder REFUNDED, Listing HIDDEN. Không tự trả Listing AVAILABLE.

REJECT_DISPUTE → Dispute REJECTED, Escrow RELEASED, MarketplaceOrder COMPLETED, Listing SOLD.

PARTIAL_REFUND và RETURN_AND_REFUND thuộc thiết kế mở rộng Post-MVP; không nhận xử lý trong MVP, không logic giả một nửa.

Mọi resolution kiểm tra quyền/state và transaction, chống release/refund lặp.

---

## 7.11. Seller Rating
Chỉ Buyer được rating khi MarketplaceOrder COMPLETED. Mỗi MarketplaceOrder tối đa một SellerRating.

---

# PHẦN 8. RECOMMENDATION ENGINE

## 8.1. PlayerProfile
- skillLevel: Beginner / Intermediate / Advanced
- playStyle: Attack / Defense / Balanced / Speed / Control
- playType: Singles / Doubles / Both
- wristStrength: Weak / Medium / Strong
- preferredWeight: 3U / 4U / 5U
- minBudget
- maxBudget

## 8.2. Recommendation Flow
`PlayerProfile → RecommendationService → Product + RacketSpecification → MatchScoreCalculator → Sort DESC → Top Recommendations`

## 8.3. Weighted Match Score

**Nguồn: CHG-003 / D03 — APPROVED 2026-10-05.**

Giữ weight Skill 20, Play Style 25, Wrist 15, Preferred Weight 15, Flex 10, Budget 15; tổng 100. MatchScoreCalculator tính deterministic/bounded/testable, score trong [0,100].

Skill order: BEGINNER=0, INTERMEDIATE=1, ADVANCED=2. abs(profileLevel-racketLevel) = 0/1/2 → SkillScore = 20/12/5. Bảng đối xứng: [20,12,5]; [12,20,12]; [5,12,20].

CHG-013: targetFlex chỉ theo skillLevel: BEGINNER → FLEXIBLE, INTERMEDIATE → MEDIUM, ADVANCED → STIFF; không dùng wristStrength. Flex ordinal FLEXIBLE=0, MEDIUM=1, STIFF=2, EXTRA_STIFF=3. abs(target-racket) = 0 →10; =1 →6; >=2 →2. Giữ bảng Wrist–Flex và weights.

effectivePrice = salePrice hợp lệ nếu có, ngược lại product.price.

BudgetScore: effectivePrice < minBudget → 12; minBudget <= effectivePrice <= maxBudget → 15; maxBudget < effectivePrice <= 1.10*maxBudget → 10; 1.10*maxBudget < effectivePrice <= 1.20*maxBudget → 5; effectivePrice > 1.20*maxBudget → 0.

Các scoring rule không mâu thuẫn khác giữ nguyên. Không đặt thuật toán trong Controller; Frontend chỉ trình bày.

---

## 8.4. Output
Recommendation cần trả Product, Match Score, Reasons, yếu tố phù hợp và điểm cần cân nhắc.

MVP ưu tiên rule-based Weighted Scoring vì dễ giải thích, test và demo. Machine Learning không bắt buộc.

---

# PHẦN 9. VALUATION VÀ CONDITION

## 9.1. Formula
`EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor`

Dùng `ValuationCalculator`, không hard-code rải rác.

## 9.2. Output
Ưu tiên suggestedMin/suggestedMax hoặc quickSell/fair/expected price. Không đưa ra một giá tuyệt đối tạo cảm giác chính xác giả.

## 9.3. Condition Score

**Nguồn: CHG-004 / D04 — APPROVED 2026-10-05.**

EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor.

Valuation lưu snapshot: referencePrice, ageFactor, conditionFactor, warrantyFactor, accessoryFactor, repairFactor, demandFactor, conditionScore, valuationMethod, suggestedMinPrice, suggestedMaxPrice, algorithmVersion, createdAt; giữ estimatedPrice đang có.

Schema valuations bổ sung repair_factor DECIMAL(5,3), condition_score DECIMAL(4,2), algorithm_version VARCHAR(50). Các factor dùng DECIMAL(5,3) như schema hiện hành; tiền DECIMAL(15,2), không FLOAT/DOUBLE. Snapshot không bị tính lại theo dữ liệu mới khi đọc lịch sử.

Condition Score 0.00–10.00; chuẩn hóa/làm tròn tới hai chữ số thập phân trước khi map Grade: [9.50,10.00] → S; [8.50,9.50) → A; [7.00,8.50) → B; [5.00,7.00) → C; [0.00,5.00) → D.

Nguồn hợp lệ/còn hiệu lực ưu tiên INSPECTION > AI > SELLER. Dùng currentInspectionReport theo CHG-001/007.

Các bảng factor hiện hành không mâu thuẫn được giữ nguyên; các khoảng dùng interval không chồng lấn.

---

## 9.4. AI
AI là tính năng hỗ trợ: `Images → AI-assisted condition analysis → Estimated Condition → Valuation Engine → Price Range`.

Không mô tả AI là nguồn duy nhất quyết định tình trạng, giá chính xác hay gian lận.

---

# PHẦN 10. SELLER REPUTATION

**Nguồn: CHG-005 / D05 — APPROVED 2026-10-05.**

Seller Reputation do Backend tính, tối đa 100: Rating 40 + Completed Orders 20 + Dispute 20 + Cancellation 10 + Account Age 10.

RatingScore = averageRating/5*40 khi có rating; chưa có rating → 0.

Completed Transaction Score: 0 → 0; 1–4 → 5; 5–19 → 10; 20–49 → 15; >=50 → 20.

Dispute Score theo rate: 0% → 20; (0%,2%] → 18; (2%,5%] → 14; (5%,10%] → 8; >10% → 3.

Cancellation Score: 0% → 10; (0%,2%] → 9; (2%,5%] → 7; (5%,10%] → 4; >10% → 0.

Account Age Score theo tháng: [0,1) → 2; [1,3) → 4; [3,6) → 6; [6,12) → 8; >=12 → 10.

Ví dụ 4.8 rating, 35 completed, dispute 1%, cancellation 2%, age 14 tháng: 38.4+15+18+9+10=90.4.

Seller chưa có eligibleReputationTransactions hiển thị NEW SELLER; rate null hiển thị Chưa có dữ liệu; không tự gán REPUTABLE SELLER chỉ do tuổi tài khoản. Frontend không gửi reputationScore.

CHG-014: denominator là DISTINCT order của Seller COMPLETED, REFUNDED hoặc CANCELLED do Seller chịu trách nhiệm; loại active, Buyer/System cancel trước payment và timeout. Dispute numerator là DISTINCT eligible order có Dispute record; cancellation numerator là eligible seller-attributable CANCELLED. Denominator=0 → hai rate null, Dispute/Cancellation Score=0; có dữ liệu thì dùng mapping CHG-005. Backend tính tại Service/Repository.

---

# PHẦN 11. REST API

## 11.1. Base

**Nguồn: CHG-011 / D11 — APPROVED 2026-10-05.**

MVP chỉ JWT Access Token với expiration hữu hạn. Không Refresh Token, RefreshTokenRequest, persistence/rotation/revocation của Refresh Token trong MVP.

Auth API MVP: POST /api/auth/register; POST /api/auth/login; POST /api/auth/logout; GET /api/users/me; PUT /api/users/me; PUT /api/users/change-password.

Logout: Frontend xóa authentication state/token. Backend không tuyên bố revoke Access Token khi không có server-side token store. Token đã phát hành còn hiệu lực tới expiration.

Khi token hết hạn/401, Frontend yêu cầu đăng nhập lại; không tự gọi refresh endpoint.

Refresh Token là Post-MVP / Future Security Enhancement; cần Change riêng cho storage, expiration, rotation, revocation, logout, frontend storage và security tests. JWT và system roles không đổi.

---

## 11.2. Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`

## 11.3. User
- `GET /api/users/me`
- `PUT /api/users/me`
- `PUT /api/users/change-password`
- Address CRUD theo module design.

## 11.4. Product
- `GET /api/categories`
- `GET /api/brands`
- `GET /api/products`
- `GET /api/products/{id}`
- Admin create/update/disable và image upload.

Filter có thể gồm keyword, category, brand, minPrice, maxPrice, weight, balance, flex, playStyle, skillLevel, page, size, sort.

## 11.5. PlayerProfile / Recommendation
- `GET /api/player-profile`
- `POST /api/player-profile`
- `PUT /api/player-profile`
- `GET /api/recommendations`

## 11.6. Cart
- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{id}`
- `DELETE /api/cart/items/{id}`

## 11.7. Shop Order
- `POST /api/orders`
- `GET /api/orders/my`
- `GET /api/orders/{id}`
- `POST /api/orders/{id}/cancel`
- Admin status update theo Controller design hiện hành.

## 11.8. Marketplace Listing
- `GET /api/marketplace/listings`
- `GET /api/marketplace/listings/{id}`
- `POST /api/marketplace/listings`
- `PUT /api/marketplace/listings/{id}`
- `GET /api/marketplace/my-listings`
- `POST /api/marketplace/listings/{id}/images`
- `POST /api/marketplace/listings/{id}/submit`
- Admin approve/reject.

## 11.9. Valuation
- `POST /api/valuations`
- `GET /api/valuations/listing/{listingId}`
- AI valuation chỉ advanced.

## 11.10. Inspection

**Nguồn: CHG-001 / D01 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N InspectionRequest; InspectionRequest 1 → 0..1 InspectionReport.

Một Listing có nhiều request trong lịch sử nhưng tối đa một request active: REQUESTED, ACCEPTED, INSPECTING. COMPLETED, REJECTED, CANCELLED không active.

Seller chỉ tạo request mới cho Listing của mình khi không có request active; Backend kiểm tra đồng thời trong transaction để ngăn request song song.

Mỗi request COMPLETED phải có report. Không xóa lịch sử kiểm định.

currentInspectionReport là report của request COMPLETED mới nhất còn hiệu lực. Verified Badge và Valuation dùng report còn hiệu lực này; hiệu lực sau sửa Listing theo CHG-007.

inspection_requests.listing_id dùng FK/index thường, không có unique trên listing_id.

**Nguồn: CHG-008 / D08 — APPROVED 2026-10-05.**

Canonical API:

POST /api/orders/{id}/cancel

POST /api/valuations

GET /api/valuations/listing/{listingId}

POST /api/inspections

GET /api/inspections/my

POST /api/inspector/inspections/{id}/accept

POST /api/inspector/inspections/{id}/start

POST /api/inspector/inspections/{id}/report

GET /api/market-orders/buying

GET /api/market-orders/selling

GET /api/market-orders/{id}

POST /api/escrow/{marketOrderId}/pay

POST /api/disputes/{id}/evidences

Các action API phải kiểm tra authentication, role, ownership, request và business state. Không dùng generic Inspection PUT cho transition. Buyer chỉ confirm-received, không có generic release/refund API cho Buyer.

DB → Java/DTO: thumbnail_url → thumbnailUrl; max_tension_lbs → maxTensionLbs; racket_length_mm → racketLengthMm; string_tension_lbs → stringTensionLbs. Không alias song song.

Success wrapper: success, message, data. Error wrapper: success, code, message, errors. Object domain minh họa là data, không phải response envelope.

Breaking normalization ở mức tài liệu; chưa có runtime client cần backward compatibility.

---

## 11.11. MarketplaceOrder
- `POST /api/market-orders`
- `GET /api/market-orders/buying`
- `GET /api/market-orders/selling`
- `GET /api/market-orders/{id}`
- `POST /api/market-orders/{id}/confirm-received`

## 11.12. Escrow / Shipment / Dispute / Rating

**Nguồn: CHG-006 / D06 — APPROVED 2026-10-05.**

Current User phải là Buyer hoặc Seller của MarketplaceOrder; Escrow HELD; Order SHIPPING hoặc DELIVERED. MVP một order tối đa một Dispute (do đó tối đa một active dispute).

Khi SHIPPING, NOT_RECEIVED phù hợp nhất cho Buyer; reason khác chỉ chấp nhận khi context nghiệp vụ hợp lệ. DELIVERED cho NOT_AS_DESCRIBED, DAMAGED, SUSPECTED_FAKE, MISSING_ACCESSORIES, WRONG_ITEM, NOT_RECEIVED, OTHER.

Tạo dispute trong transaction: Dispute OPEN; MarketplaceOrder DISPUTED; Escrow DISPUTED; Notification và Audit.

ADMIN resolution:

RELEASE_TO_SELLER → Dispute RESOLVED, Escrow RELEASED, MarketplaceOrder COMPLETED, Listing SOLD.

FULL_REFUND → Dispute RESOLVED, Escrow REFUNDED, MarketplaceOrder REFUNDED, Listing HIDDEN. Không tự trả Listing AVAILABLE.

REJECT_DISPUTE → Dispute REJECTED, Escrow RELEASED, MarketplaceOrder COMPLETED, Listing SOLD.

PARTIAL_REFUND và RETURN_AND_REFUND thuộc thiết kế mở rộng Post-MVP; không nhận xử lý trong MVP, không logic giả một nửa.

Mọi resolution kiểm tra quyền/state và transaction, chống release/refund lặp.

POST /api/escrow/{marketOrderId}/pay là payment simulation. Buyer dùng POST /api/market-orders/{id}/confirm-received, không generic release/refund. Shipment giữ thiết kế hiện hành; POST /api/seller-ratings khi giao dịch hợp lệ.

---

## 11.13. Notification / Statistics
Notification: get, mark read, mark all read.

Admin Statistics: overview, revenue, top-products, top-sellers.

---

# PHẦN 12. FRONTEND DESIGN

## 12.1. Public Routes
- `/`
- `/products`
- `/products/:id`
- `/marketplace`
- `/marketplace/:id`
- `/login`
- `/register`

## 12.2. Protected Routes
- `/cart`
- `/checkout`
- `/profile`
- `/player-profile`
- `/orders`
- `/orders/:id`
- `/favorites`
- `/recommendations`
- `/marketplace/sell`
- `/marketplace/my-listings`
- Marketplace buying/selling orders

Inspector: `/inspector/*`

Admin: `/admin/*`

## 12.3. Frontend Structure
```text
src/
├── assets/
├── components/
├── pages/
├── services/
├── hooks/
├── context/
├── routes/
├── constants/
└── utils/
```

Không gọi Axios rải rác trong component; dùng API service theo module.

Mỗi page gọi API phải xử lý Loading / Success / Empty / Error.

Shop và Marketplace phải phân biệt rõ trong UI.

Logo, hero banner, footer, màu sắc, typography và style chi tiết được phép thiết kế linh hoạt miễn không thay business contract.

---

# PHẦN 13. SECURITY

## 13.1. Authentication / Authorization

**Nguồn: CHG-011 / D11 — APPROVED 2026-10-05.**

MVP chỉ JWT Access Token với expiration hữu hạn. Không Refresh Token, RefreshTokenRequest, persistence/rotation/revocation của Refresh Token trong MVP.

Auth API MVP: POST /api/auth/register; POST /api/auth/login; POST /api/auth/logout; GET /api/users/me; PUT /api/users/me; PUT /api/users/change-password.

Logout: Frontend xóa authentication state/token. Backend không tuyên bố revoke Access Token khi không có server-side token store. Token đã phát hành còn hiệu lực tới expiration.

Khi token hết hạn/401, Frontend yêu cầu đăng nhập lại; không tự gọi refresh endpoint.

Refresh Token là Post-MVP / Future Security Enhancement; cần Change riêng cho storage, expiration, rotation, revocation, logout, frontend storage và security tests. JWT và system roles không đổi.

---

## 13.2. Backend Authority
Frontend không quyết định userId, buyerId/sellerId, role, Product price, Order total, Marketplace price, Match Score, Valuation, status, Verified Badge, Escrow state hoặc Seller Reputation.

## 13.3. Validation / Mass Assignment
Dùng Request DTO. Validate required, length, numeric range, enum, email, quantity, price, file và pagination.

## 13.4. Upload
Whitelist JPG/JPEG/PNG/WEBP; validate MIME, size, count; generate filename/UUID; production dùng Cloudinary/Object Storage.

## 13.5. Transaction / Concurrency
Transaction bắt buộc cho Shop checkout, Marketplace purchase, confirm received, Escrow release, Dispute creation/resolution.

Dùng lock/atomic update để chống stock âm và duplicate Marketplace purchase.

## 13.6. Secrets
Không commit DB password, JWT secret, Cloudinary secret, payment key, `.env` thực.

## 13.7. Fraud Wording
Không tuyên bố “chống lừa đảo hoàn toàn”. Mô tả là **giảm thiểu rủi ro gian lận** thông qua authentication, authorization, inspection, transaction locking, simulated Escrow, dispute, reputation và audit.

---

# PHẦN 14. TESTING

Các loại: Unit, Integration, API, Security, Concurrency, E2E, UAT khi cần.

Unit Test ưu tiên:
- MatchScoreCalculator
- ValuationCalculator
- ConditionScoreCalculator
- SellerReputationCalculator
- state transitions

Integration ưu tiên:
- Shop Checkout
- Marketplace Purchase
- Escrow Release
- Dispute Create/Resolve

Concurrency bắt buộc:
- Stock=1, hai checkout → một success, một out-of-stock, stock không âm.
- Listing AVAILABLE, hai Buyer purchase → một success, một `LISTING_NOT_AVAILABLE`.

Security test: no token, invalid/expired token, wrong role, wrong owner/IDOR, price tampering, fake verified/status, double release, invalid upload.

E2E Shop:
`Register → Login → Products → Cart → Checkout → Order → Admin Processing → Shipping → Completed → Review`

E2E Recommendation:
`Login → PlayerProfile → Recommendation → Match Score → Product Detail`

E2E Marketplace:
`Seller Create Listing → Valuation → Submit → Admin Approve → Buyer Purchase → Escrow → Shipment → Confirm → Seller Rating`

E2E Dispute:
`Purchase → Escrow HELD → Shipment → Dispute → Evidence → Escrow DISPUTED → Admin Resolve`

---

# PHẦN 15. SOURCE CODE STRUCTURE VÀ DEVELOPMENT METHOD

## 15.1. Backend
```text
src/main/java/com/badminton/
├── auth/
├── user/
├── category/
├── brand/
├── product/
├── cart/
├── order/
├── review/
├── favorite/
├── recommendation/
├── marketplace/
├── valuation/
├── inspection/
├── marketorder/
├── escrow/
├── shipment/
├── dispute/
├── rating/
├── notification/
├── statistics/
├── audit/
├── security/
├── exception/
├── config/
└── common/
```

Module tiêu chuẩn:
```text
module/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/request/
├── dto/response/
└── mapper/
```

## 15.2. Vertical Slice
`Database → Entity → Repository → Service → Controller/API → API Test → Frontend → Integration Test`

## 15.3. Thứ tự Implementation
1. Sprint 0 – Project Setup
2. Auth & User
3. Product Catalog
4. Cart & Shop Order
5. Recommendation
6. Marketplace Listing
7. Valuation & Condition
8. Marketplace Order & Escrow
9. Inspection
10. Dispute & Seller Rating
11. Admin & Statistics
12. System Testing
13. Deployment

Recommendation là chức năng trọng tâm, không cắt khỏi MVP.

---

# PHẦN 16. DEPLOYMENT VÀ OPERATIONS

Kiến trúc định hướng:
`User → HTTPS/Nginx → React + Spring Boot API → MySQL + Cloudinary`

Nên có `application-dev.yml`, `application-test.yml`, `application-prod.yml`; production lấy credential từ environment variables.

Khuyến nghị Flyway cho migration. Không ALTER production DB tùy tiện ngoài migration workflow.

Nếu dùng VPS:
`Internet → Nginx :443 → Spring Boot :8080`

Database production:
- không public 3306 tùy tiện;
- không dùng root cho app nếu có thể;
- backup định kỳ;
- test restore khi phù hợp.

Deployment flow định hướng:
`Code → Commit → Push → PR → Review → CI → Test → Build → Staging → UAT → Production`

Có Development / Staging / Production. Có rollback application; migration DB phải review, backup và test trước production.

---

# PHẦN 17. DEFINITION OF DONE

Một feature chỉ DONE khi các phần cần thiết đã hoàn thành:
- Database/migration
- Entity
- Repository
- Service
- DTO/Mapper
- Controller/API
- Validation
- Authorization/Ownership
- Business Rules
- Error Handling
- API Test
- Frontend Integration
- Main error cases
- Unit/Integration Test phù hợp

Không đánh dấu DONE chỉ vì code compile.

---

# PHẦN 18. SOURCE OF TRUTH VÀ CHANGE MANAGEMENT

Bộ nguồn:
- `00_PROJECT_STRUCTURE.md`
- `01_SYSTEM_ANALYSIS_MASTER.docx`
- `01_SYSTEM_ANALYSIS_MASTER.md` (file hiện tại)
- `02_IMPLEMENTATION_RULES.md`
- `03_IMPLEMENTATION_STATUS.md`
- `04_CHANGELOG.md`

Vai trò:
- `00`: bản đồ project.
- `01 docx`: thiết kế đầy đủ/báo cáo.
- `01 md`: companion cho Codex/AI.
- `02`: luật triển khai.
- `03`: trạng thái source code thực tế.
- `04`: lịch sử architecture/business change.

Không tự ý thay Entity, relationship, API contract, status/state machine, business rule, technology stack hoặc module boundary.

Quy trình thay đổi:
`Issue → Review → Proposal → Approval → CHANGELOG → Update docs → Implement → Retest → Update Implementation Status`

---

# PHẦN 19. QUY TẮC DÀNH CHO CODEX/AI

Trước khi bắt đầu module:
1. Đọc `AGENTS.md`.
2. Đọc `00_PROJECT_STRUCTURE.md`.
3. Đọc phần liên quan trong file này.
4. Đọc `02_IMPLEMENTATION_RULES.md`.
5. Kiểm tra `03_IMPLEMENTATION_STATUS.md`.
6. Kiểm tra `04_CHANGELOG.md`.
7. Kiểm tra source code/migration hiện tại trước khi tạo mới.

Khi hoàn thành milestone:
- cập nhật `03_IMPLEMENTATION_STATUS.md`;
- chỉ cập nhật `04_CHANGELOG.md` nếu có Architecture/Business Design Change;
- không biến Changelog thành Git commit log.

---

# PHẦN 20. TRẠNG THÁI THIẾT KẾ HIỆN TẠI

D01–D14 APPROVED và đồng bộ tài liệu qua CHG-001–CHG-014. Pending D01–D14 = 0. Pending Architecture Issues = 0. Sprint 0 và mọi module implementation vẫn NOT_STARTED; xem 03_IMPLEMENTATION_STATUS.md.

**Nguồn: CHG-012 / D12 — APPROVED 2026-10-05.**

Promotions không thuộc MVP/baseline hiện tại. Loại bỏ màn hình Admin Promotions, route promotions và mô tả quản lý khuyến mãi khỏi chức năng hiện tại.

Không tạo Promotion Entity/table/API/module hoặc discount calculation để hợp thức hóa nội dung legacy.

Product price và Order total tiếp tục do Backend quyết định. Promotions chỉ có thể được xem xét như Future Development qua change-management.

---

# PHỤ LỤC A. BUSINESS ERROR CODE BAN ĐẦU

- `AUTH_INVALID_CREDENTIALS`
- `USER_LOCKED`
- `USER_NOT_FOUND`
- `PRODUCT_NOT_FOUND`
- `PRODUCT_OUT_OF_STOCK`
- `CART_EMPTY`
- `ORDER_INVALID_STATUS`
- `PROFILE_REQUIRED`
- `LISTING_NOT_AVAILABLE`
- `LISTING_OWNER_CANNOT_BUY`
- `INSPECTION_ALREADY_EXISTS`
- `ESCROW_INVALID_STATUS`
- `DISPUTE_NOT_ALLOWED`

---

# PHỤ LỤC B. NGUYÊN TẮC KHÔNG ĐƯỢC PHÁ VỠ

1. Không gộp Product và UsedListing.
2. Không gộp Shop Order và MarketplaceOrder.
3. BUYER/SELLER không phải system role.
4. Backend quyết định dữ liệu nghiệp vụ quan trọng.
5. Match Score do Backend tính.
6. Valuation do Backend tính.
7. Verified Badge do Backend quyết định.
8. Escrow state do Backend quản lý.
9. Một Listing không được bán đồng thời cho hai Buyer.
10. Không release Escrow hai lần.
11. Ownership phải được kiểm tra ở Backend.
12. Frontend validation không thay thế Backend validation.
13. Money dùng DECIMAL.
14. Transaction đặt ở Service.
15. Không hard-code business algorithm trong Controller.
16. Không tự thêm Microservices/Kafka/Kubernetes/CQRS khi chưa có decision.
17. Trong báo cáo tiếng Việt dùng **PHẦN**, không dùng **CHƯƠNG**.

---

# PHỤ LỤC C. GHI CHÚ ĐỒNG BỘ VỚI FILE WORD

`01_SYSTEM_ANALYSIS_MASTER.docx` vẫn là tài liệu đầy đủ dùng cho báo cáo, hình ảnh, sơ đồ và diễn giải dài.

File Markdown này tối ưu cho implementation nên tập trung vào architecture, actor/use case, entity/relationship, state machine, business rule, API contract, algorithm, security, testing, deployment và source structure.

Không cần sao chép toàn bộ phần diễn giải học thuật, hình minh họa hoặc nội dung trình bày dài từ file Word vào đây.

# PHỤ LỤC D. TRACEABILITY VÀ CONTRACT ĐÃ PHÊ DUYỆT

## D01 → CHG-001

**Nguồn: CHG-001 / D01 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N InspectionRequest; InspectionRequest 1 → 0..1 InspectionReport.

Một Listing có nhiều request trong lịch sử nhưng tối đa một request active: REQUESTED, ACCEPTED, INSPECTING. COMPLETED, REJECTED, CANCELLED không active.

Seller chỉ tạo request mới cho Listing của mình khi không có request active; Backend kiểm tra đồng thời trong transaction để ngăn request song song.

Mỗi request COMPLETED phải có report. Không xóa lịch sử kiểm định.

currentInspectionReport là report của request COMPLETED mới nhất còn hiệu lực. Verified Badge và Valuation dùng report còn hiệu lực này; hiệu lực sau sửa Listing theo CHG-007.

inspection_requests.listing_id dùng FK/index thường, không có unique trên listing_id.

## D02 → CHG-002

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

## D04 → CHG-004

**Nguồn: CHG-004 / D04 — APPROVED 2026-10-05.**

EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor.

Valuation lưu snapshot: referencePrice, ageFactor, conditionFactor, warrantyFactor, accessoryFactor, repairFactor, demandFactor, conditionScore, valuationMethod, suggestedMinPrice, suggestedMaxPrice, algorithmVersion, createdAt; giữ estimatedPrice đang có.

Schema valuations bổ sung repair_factor DECIMAL(5,3), condition_score DECIMAL(4,2), algorithm_version VARCHAR(50). Các factor dùng DECIMAL(5,3) như schema hiện hành; tiền DECIMAL(15,2), không FLOAT/DOUBLE. Snapshot không bị tính lại theo dữ liệu mới khi đọc lịch sử.

Condition Score 0.00–10.00; chuẩn hóa/làm tròn tới hai chữ số thập phân trước khi map Grade: [9.50,10.00] → S; [8.50,9.50) → A; [7.00,8.50) → B; [5.00,7.00) → C; [0.00,5.00) → D.

Nguồn hợp lệ/còn hiệu lực ưu tiên INSPECTION > AI > SELLER. Dùng currentInspectionReport theo CHG-001/007.

Các bảng factor hiện hành không mâu thuẫn được giữ nguyên; các khoảng dùng interval không chồng lấn.

## D08 → CHG-008

**Nguồn: CHG-008 / D08 — APPROVED 2026-10-05.**

Canonical API:

POST /api/orders/{id}/cancel

POST /api/valuations

GET /api/valuations/listing/{listingId}

POST /api/inspections

GET /api/inspections/my

POST /api/inspector/inspections/{id}/accept

POST /api/inspector/inspections/{id}/start

POST /api/inspector/inspections/{id}/report

GET /api/market-orders/buying

GET /api/market-orders/selling

GET /api/market-orders/{id}

POST /api/escrow/{marketOrderId}/pay

POST /api/disputes/{id}/evidences

Các action API phải kiểm tra authentication, role, ownership, request và business state. Không dùng generic Inspection PUT cho transition. Buyer chỉ confirm-received, không có generic release/refund API cho Buyer.

DB → Java/DTO: thumbnail_url → thumbnailUrl; max_tension_lbs → maxTensionLbs; racket_length_mm → racketLengthMm; string_tension_lbs → stringTensionLbs. Không alias song song.

Success wrapper: success, message, data. Error wrapper: success, code, message, errors. Object domain minh họa là data, không phải response envelope.

Breaking normalization ở mức tài liệu; chưa có runtime client cần backward compatibility.

## D09 → CHG-009

**Nguồn: CHG-009 / D09 — APPROVED 2026-10-05.**

User 1 → 0..1 PlayerProfile

User 1 → 0..1 Cart

User 1 → 0..N Address

User 1 → 0..N Order

User 1 → 0..N Review

User 1 → 0..N Favorite

Cart 1 → 0..N CartItem

Order 1 → 1..N OrderItem sau khi tạo thành công

Product 1 → 0..N ProductImage

Product 1 → 0..1 RacketSpecification (chỉ Product loại vợt)

Category 1 → 0..N Product

Brand 1 → 0..N Product

UsedListing 1 → 0..N UsedListingImage

UsedListing 1 → 0..N Valuation

UsedListing 1 → 0..N InspectionRequest

UsedListing 1 → 0..N MarketplaceOrder

InspectionRequest 1 → 0..1 InspectionReport (COMPLETED bắt buộc có report)

MarketplaceOrder 1 → 1 EscrowTransaction

MarketplaceOrder 1 → 0..1 Shipment

MarketplaceOrder 1 → 0..1 Dispute trong MVP

MarketplaceOrder 1 → 0..1 SellerRating

Dispute 1 → 0..N DisputeEvidence

Cart có thể lazy-create lần đầu dùng; không bắt buộc tạo khi register. PlayerProfile optional cho User nhưng required khi dùng Recommendation.

## D10 → CHG-010

**Nguồn: CHG-010 / D10 — APPROVED 2026-10-05.**

Shop Order flow: PENDING → CONFIRMED → PREPARING → SHIPPING → COMPLETED.

User chỉ cancel order của mình khi PENDING hoặc CONFIRMED trước PREPARING. Không cancel từ PREPARING, SHIPPING, COMPLETED, CANCELLED.

Cancel transaction: lock Order → validate owner/state → restore stock từng OrderItem đúng một lần → cập nhật payment nếu cần → Order CANCELLED → commit.

UNPAID không refund; PAID → REFUNDED theo payment simulation MVP. Double cancel phải reject hoặc idempotent theo implementation policy, tuyệt đối không restore stock hai lần.

ADMIN hoặc backend-authorized operation quản lý fulfillment CONFIRMED/PREPARING/SHIPPING/COMPLETED qua API được Backend validate. USER không gửi arbitrary orderStatus.

## D11 → CHG-011

**Nguồn: CHG-011 / D11 — APPROVED 2026-10-05.**

MVP chỉ JWT Access Token với expiration hữu hạn. Không Refresh Token, RefreshTokenRequest, persistence/rotation/revocation của Refresh Token trong MVP.

Auth API MVP: POST /api/auth/register; POST /api/auth/login; POST /api/auth/logout; GET /api/users/me; PUT /api/users/me; PUT /api/users/change-password.

Logout: Frontend xóa authentication state/token. Backend không tuyên bố revoke Access Token khi không có server-side token store. Token đã phát hành còn hiệu lực tới expiration.

Khi token hết hạn/401, Frontend yêu cầu đăng nhập lại; không tự gọi refresh endpoint.

Refresh Token là Post-MVP / Future Security Enhancement; cần Change riêng cho storage, expiration, rotation, revocation, logout, frontend storage và security tests. JWT và system roles không đổi.

## D12 → CHG-012

**Nguồn: CHG-012 / D12 — APPROVED 2026-10-05.**

Promotions không thuộc MVP/baseline hiện tại. Loại bỏ màn hình Admin Promotions, route promotions và mô tả quản lý khuyến mãi khỏi chức năng hiện tại.

Không tạo Promotion Entity/table/API/module hoặc discount calculation để hợp thức hóa nội dung legacy.

Product price và Order total tiếp tục do Backend quyết định. Promotions chỉ có thể được xem xét như Future Development qua change-management.


# Quyết định bổ sung CHG-013 và CHG-014

CHG-013 / D13 — APPROVED 2026-10-05.

ShaftFlexibility chính thức: FLEXIBLE, MEDIUM, STIFF, EXTRA_STIFF; thứ tự ordinal tương ứng 0, 1, 2, 3.

Flex Score và Wrist Score riêng biệt. Giữ nguyên bảng Wrist–Flex Compatibility; không suy targetFlex từ wristStrength.

targetFlex chỉ theo skillLevel: BEGINNER → FLEXIBLE; INTERMEDIATE → MEDIUM; ADVANCED → STIFF. EXTRA_STIFF không là target mặc định và không thêm skill level.

distance = abs(targetFlex.ordinal - racketFlex.ordinal). Distance 0 → FlexScore 10; distance 1 → 6; distance >= 2 → 2.

MatchScoreCalculator deterministic. Recommendation weights giữ nguyên 20/25/15/15/10/15, tổng 100.

Test cases FlexScore, theo cột FLEXIBLE / MEDIUM / STIFF / EXTRA_STIFF: BEGINNER [10,6,2,2]; INTERMEDIATE [6,10,6,2]; ADVANCED [2,6,10,6].

Wrist Strength vẫn có trọng số riêng 15%; Skill-derived Flex Score có trọng số 10%, tránh đếm wristStrength hai lần.

CHG-014 / D14 — APPROVED 2026-10-05.

eligibleReputationTransactions là tập DISTINCT MarketplaceOrder của Seller có status COMPLETED, REFUNDED hoặc CANCELLED do Seller chịu trách nhiệm.

Loại khỏi denominator/numerator: PENDING_PAYMENT, PAID, WAITING_SHIPMENT, SHIPPING, DELIVERED, DISPUTED chưa resolve; Buyer cancel trước payment, payment timeout, System cancel trước payment.

Nguồn hủy do Backend quyết định từ action: cancelledByType = BUYER / SELLER / ADMIN / SYSTEM. Đây là business values, không phải system roles; system roles vẫn USER / INSPECTOR / ADMIN.

Thiết kế marketplace_orders thêm cancelled_by_type VARCHAR(30) NULL và cancellation_reason VARCHAR(100) NULL; null khi chưa hủy. Entity/response DTO tương ứng cancelledByType/cancellationReason. Client không được tùy ý gán nguồn/nguyên nhân hủy.

Reason ví dụ: BUYER_CHANGED_MIND, PAYMENT_TIMEOUT, SELLER_CANNOT_FULFILL, ADMIN_CANCELLED, SYSTEM_CANCELLED, OTHER. Không coi danh sách ví dụ này là enum đóng mới.

Seller-attributable cancellation gồm cancelledByType=SELLER, hoặc cancelledByType=ADMIN với cancellationReason=SELLER_CANNOT_FULFILL đánh dấu nguyên nhân do Seller. Không coi mọi ADMIN cancellation là do Seller.

Backend Service ghi nguồn hủy và Audit cùng nghiệp vụ; cancel/timeout trước payment vẫn theo CHG-002. CHG-014 không tự mở thêm quyền/action cancel hoặc state transition.

eligibleReputationTransactionCount = COUNT DISTINCT eligible MarketplaceOrder. Một order chỉ tính một lần, kể cả join nhiều evidence/rating.

disputeTransactionCount = COUNT DISTINCT eligible MarketplaceOrder có Dispute record, kể cả khi complaint bị bác; chỉ tính khi Order đã có eligible outcome.

sellerCancellationCount = COUNT DISTINCT eligible MarketplaceOrder CANCELLED do Seller chịu trách nhiệm.

disputeRate = disputeTransactionCount / eligibleReputationTransactionCount * 100; cancellationRate = sellerCancellationCount / eligibleReputationTransactionCount * 100.

Nếu denominator=0: disputeRate=null, cancellationRate=null, Dispute Score=0, Cancellation Score=0. Không thay null bằng 0%.

Nếu denominator>0: dùng các bảng mapping rate của CHG-005; 0% có dữ liệu cho Dispute Score=20 hoặc Cancellation Score=10 tương ứng.

Seller không có eligible transaction: Frontend hiển thị NEW SELLER; cả hai rate hiển thị Chưa có dữ liệu. Frontend không tính rate/reputationScore.

Completed Transaction Score chỉ dùng MarketplaceOrder COMPLETED; Average Rating chỉ dùng SellerRating hợp lệ. Giữ trọng số Reputation 40/20/20/10/10.

Tests dự kiến: zero denominator; Buyer pre-payment cancel; timeout; Seller-attributable cancel; completed có/không dispute; refunded sau dispute; active order; distinct order không nhân đôi khi join; ADMIN reason phân biệt attribution. Chưa chạy runtime tests.

## PHẦN bổ sung — quyết định D15–D17, phê duyệt 2026-10-08

Người dùng xác nhận “Ok thực hiện đi” sau đề nghị áp dụng cả ba phương án. Các quyết định dưới đây thay thế phần chưa xác định tương ứng; D01–D14 và các module khác giữ nguyên.

### CHG-015 / D15 — Valuation (APPROVED)

Thiết kế cũ thiếu dữ liệu phụ kiện/sửa chữa và tiêu chí matching. POST /api/valuations nhận listingId, accessoryCondition (FULL/PARTIAL/MANY_MISSING), repairLevel (NONE/MINOR/STRUCTURAL); hai phân loại bắt buộc. Backend ánh xạ 1.03/1.00/0.97 và 1.00/0.90/0.70. Report kiểm định hợp lệ ưu tiên condition score/grade; repairDetected và repairLevel phải nhất quán. Khi không có report hợp lệ dùng grade người bán; không tái dùng score của report đã vô hiệu. Chỉ match Product ACTIVE cùng brandId, tên sau trim không phân biệt hoa thường, duy nhất một kết quả. Dùng effectivePrice của backend; 0 hoặc nhiều kết quả dùng originalPrice. Không fuzzy matching. Ngày hết bảo hành ưu tiên; không rõ dùng 0.97, hết hạn 1.00. Snapshot lưu factor, nguồn và phân loại trong note, không thay lịch sử. Ảnh hưởng DTO/service/UI/tests, không thêm Entity hay relationship.

### CHG-016 / D16 — Category/Brand (APPROVED)

Status cũ nullable chưa có tập giá trị. Chốt ACTIVE/INACTIVE; tạo ACTIVE, update cho phép hai giá trị; Brand DELETE là INACTIVE, không hard delete. Public chỉ xem ACTIVE; ADMIN xem tất cả. Không cascade trạng thái sang Product/UsedListing. Migration V4 backfill NULL thành ACTIVE, đặt default/NOT NULL/check; không sửa migration đã áp dụng. Ảnh hưởng enum/DTO/service/UI/tests.

### CHG-017 / D17 — API bổ trợ (APPROVED)

Mở POST /api/market-orders/{id}/cancel cho Buyer sở hữu, chỉ PENDING_PAYMENT + WAITING_PAYMENT; backend gán BUYER/BUYER_CHANGED_MIND. Mở GET /api/admin/audit-logs?page=0&size=20 chỉ ADMIN, size tối đa 100. Không thay state machine hoặc quyền release/refund. Ảnh hưởng controller/UI/API tests.

Rollback: thay đổi quyết định cần CHANGE mới; không tự phục hồi quy tắc cũ. Cập nhật Implementation Status theo kết quả kiểm thử, không tự đánh dấu toàn bộ DONE.
