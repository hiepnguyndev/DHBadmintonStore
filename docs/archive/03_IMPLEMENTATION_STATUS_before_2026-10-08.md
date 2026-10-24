> **Cập nhật triển khai 2026-10-07:** Sprint 0 `IN_PROGRESS`, Auth & User `TESTING`; các module còn lại `NOT_STARTED`. Đã có source thực, backend build và unit/MockMvc tests. MySQL integration và E2E API thật chưa chạy. Các ghi chú NOT_STARTED trong snapshot 2026-10-05 chỉ phản ánh thời điểm trước triển khai. Xem `IMPLEMENTATION_HANDOFF.md` cho bằng chứng và giới hạn hiện tại.

# BADMINTON ECOMMERCE PLATFORM
# IMPLEMENTATION STATUS

---

# 1. Mục đích của tài liệu

`03_IMPLEMENTATION_STATUS.md` là tài liệu dùng để theo dõi **trạng thái triển khai thực tế của source code**.

Nếu:

```text
01_SYSTEM_ANALYSIS_MASTER
```

trả lời câu hỏi:

> Hệ thống được thiết kế như thế nào?

và:

```text
02_IMPLEMENTATION_RULES
```

trả lời:

> Khi code phải tuân theo những quy tắc nào?

thì:

```text
03_IMPLEMENTATION_STATUS
```

trả lời:

> Project hiện tại đã code đến đâu?

Đây là tài liệu đặc biệt quan trọng khi project được chia thành nhiều đoạn chat.

Ví dụ sau này mở một chat mới:

```text
09 - MARKETPLACE ORDER & ESCROW
```

ChatGPT cần biết:

```text
User đã hoàn thành chưa?

Product đã hoàn thành chưa?

Marketplace Listing đã có chưa?

Database migration nào đã chạy?

API Listing hiện tại là API nào?

Có bug nào chưa sửa?

Escrow đang ở NOT_STARTED hay đã code một phần?
```

Những thông tin đó không nên lấy bằng cách đoán từ chat cũ.

Chúng phải được ghi tại:

```text
03_IMPLEMENTATION_STATUS.md
```

---

# 2. Vai trò của tài liệu trong Project

Project có năm loại tài liệu nền chính:

```text
00_PROJECT_STRUCTURE
→ Bản đồ toàn bộ project

01_SYSTEM_ANALYSIS_MASTER
→ Thiết kế hệ thống đầy đủ

02_IMPLEMENTATION_RULES
→ Quy tắc bắt buộc khi triển khai

03_IMPLEMENTATION_STATUS
→ Trạng thái source code hiện tại

04_CHANGELOG
→ Lịch sử thay đổi kiến trúc
```

Do đó:

```text
SYSTEM_ANALYSIS
≠
IMPLEMENTATION_STATUS
```

Ví dụ tài liệu thiết kế có:

```text
EscrowService
```

không có nghĩa source code hiện tại đã có:

```text
EscrowService.java
```

`03_IMPLEMENTATION_STATUS.md` phải phản ánh sự khác biệt đó.

---

# 3. Nguyên tắc cập nhật

Tài liệu này phải được cập nhật sau mỗi module hoặc feature quan trọng.

Ví dụ:

```text
Auth trước khi code:
NOT_STARTED
```

Sau khi bắt đầu:

```text
IN_PROGRESS
```

Sau khi Backend chạy nhưng Frontend chưa làm:

```text
BACKEND_DONE
```

Sau khi tích hợp Frontend:

```text
INTEGRATION
```

Sau khi test:

```text
TESTING
```

Hoàn tất:

```text
DONE
```

---

# 4. Các trạng thái được phép sử dụng

Để tránh mỗi chat sử dụng một cách gọi khác nhau, project dùng các trạng thái sau.

## 4.1 NOT_STARTED

Chưa bắt đầu.

```text
Status:
NOT_STARTED
```

---

## 4.2 IN_PROGRESS

Đang code.

```text
Status:
IN_PROGRESS
```

Có thể Backend hoặc Frontend đang được triển khai.

---

## 4.3 BACKEND_DONE

Backend đã hoàn thành ở mức chức năng chính nhưng Frontend chưa tích hợp đầy đủ.

```text
Status:
BACKEND_DONE
```

---

## 4.4 FRONTEND_DONE

Frontend đã được xây dựng nhưng có thể integration chưa hoàn thiện.

Trạng thái này ít dùng độc lập nhưng vẫn có thể cần.

---

## 4.5 INTEGRATION

Backend và Frontend đang được nối với nhau.

```text
Status:
INTEGRATION
```

---

## 4.6 TESTING

Feature đã code xong và đang kiểm thử.

```text
Status:
TESTING
```

---

## 4.7 BLOCKED

Không thể tiếp tục vì phụ thuộc một vấn đề khác.

Ví dụ:

```text
Valuation
BLOCKED

Reason:
UsedListing chưa hoàn thành.
```

---

## 4.8 DONE

Feature đã đáp ứng Definition of Done.

```text
Status:
DONE
```

DONE không chỉ có nghĩa:

```text
Code compile thành công.
```

Mà phải đáp ứng các điều kiện kiểm thử và tích hợp cần thiết.

---

# 5. Definition of Done

Một module chỉ được chuyển sang:

```text
DONE
```

khi các phần cần thiết đã hoàn thành.

Checklist mặc định:

```text
Database
✓

Entity
✓

Repository
✓

Service
✓

DTO
✓

Mapper
✓

Controller
✓

Validation
✓

Authorization
✓

Business Rules
✓

Exception Handling
✓

API Test
✓

Frontend Integration
✓

Main Error Cases
✓

Unit / Integration Test phù hợp
✓
```

Không phải module nào cũng cần toàn bộ các thành phần trên.

Ví dụ:

```text
MatchScoreCalculator
```

không cần Controller riêng.

Nhưng phải có:

```text
Implementation
✓

Unit Test
✓
```

---

# 6. Trạng thái tổng thể của Project

## Project Information

```text
Project:
Badminton Ecommerce Platform
```

```text
Architecture:
Modular Monolith
```

```text
Frontend:
React
```

```text
Backend:
Spring Boot
```

```text
Database:
MySQL
```

---

## Current Phase

Hiện tại:

```text
PHASE:
IMPLEMENTATION — SPRINT 0 + AUTH & USER
```

Phần phân tích và thiết kế:

```text
D01–D14 APPROVED / DOCUMENTS SYNCHRONIZED — ALL_OK
```

Đây là trạng thái rà soát thiết kế, không phải status của module implementation ở mục 4.

Implementation:

```text
IN_PROGRESS
```

---

# 7. Tổng quan tiến độ Module

Bảng trạng thái ban đầu:

| Module | Status | Backend | Frontend | Test |
|---|---|---|---|---|
| Sprint 0 – Setup | IN_PROGRESS | Build PASS | Source ready | MySQL integration pending |
| Auth & User | TESTING | Unit/MockMvc PASS | Component tests PASS | MySQL/E2E pending |
| Product Catalog | NOT_STARTED | No | No | No |
| Cart | NOT_STARTED | No | No | No |
| Shop Order | NOT_STARTED | No | No | No |
| Review | NOT_STARTED | No | No | No |
| Favorite | NOT_STARTED | No | No | No |
| Player Profile | NOT_STARTED | No | No | No |
| Recommendation | NOT_STARTED | No | No | No |
| Marketplace Listing | NOT_STARTED | No | No | No |
| Valuation | NOT_STARTED | No | No | No |
| Marketplace Order | NOT_STARTED | No | No | No |
| Escrow | NOT_STARTED | No | No | No |
| Shipment | NOT_STARTED | No | No | No |
| Inspection | NOT_STARTED | No | No | No |
| Dispute | NOT_STARTED | No | No | No |
| Seller Rating | NOT_STARTED | No | No | No |
| Notification | NOT_STARTED | No | No | No |
| Admin | NOT_STARTED | No | No | No |
| Statistics | NOT_STARTED | No | No | No |
| Audit Log | NOT_STARTED | No | No | No |
| System Testing | NOT_STARTED | - | - | No |
| Deployment | NOT_STARTED | - | - | No |

Bảng này phải được cập nhật khi project phát triển.

---

# 8. SPRINT 0 – PROJECT SETUP

## Status

```text
IN_PROGRESS
```

## Mục tiêu

Sprint 0 không tập trung vào business feature.

Mục tiêu là xây nền tảng kỹ thuật để các module sau có thể phát triển.

---

## Backend cần hoàn thành

```text
Spring Boot project
```

```text
Maven configuration
```

```text
Package structure
```

```text
MySQL connection
```

```text
Application profiles
```

```text
Global API Response
```

```text
Global Exception Handler
```

```text
Base Entity
```

```text
Common Enum structure
```

```text
CORS configuration
```

```text
Swagger / OpenAPI
```

---

## Frontend cần hoàn thành

```text
React project
```

```text
React Router
```

```text
Base layout
```

```text
Axios configuration
```

```text
Environment variables
```

```text
Folder structure
```

---

## Database

Cần:

```text
Create badminton database
```

Ví dụ tên:

```text
badminton_ecommerce
```

Migration system:

```text
Flyway
```

nếu được chọn.

---

## Kiểm tra hoàn thành

Sprint 0 chỉ được DONE khi:

```text
Frontend chạy được
```

```text
Backend chạy được
```

```text
Backend kết nối được MySQL
```

```text
Frontend gọi được một API test từ Backend
```

---

## Current Tasks

```text
[x] Create Git repository

[x] Create Spring Boot backend

[x] Create React frontend

[ ] Configure MySQL

[x] Configure application-dev.yml

[x] Create environment variables

[x] Configure CORS

[x] Configure Swagger

[x] Create base API response

[x] Create global exception handler

[ ] Test Frontend → Backend connection
```

---

# 9. AUTH & USER MODULE

## Status

```text
TESTING
```

## Phạm vi

Module gồm:

```text
User

Address

Authentication

Authorization

JWT

Profile
```

---

## Backend Entities

Cần:

```text
User
```

```text
Address
```

---

## User Entity dự kiến

Các field quan trọng:

```text
userId

fullName

email

passwordHash

phone

avatarUrl

role

status

emailVerified

createdAt

updatedAt
```

---

## Backend Components

Cần triển khai:

```text
UserRepository
```

```text
AuthService
```

```text
UserService
```

```text
AuthController
```

```text
UserController
```

```text
JwtService
```

```text
JwtAuthenticationFilter
```

```text
SecurityConfig
```

```text
CustomUserDetailsService
```

---

## DTO

Dự kiến:

```text
RegisterRequest

LoginRequest


UpdateUserRequest

ChangePasswordRequest
```

Response:

```text
AuthResponse

UserResponse
```

---

## API cần hoàn thành

```text
POST /api/auth/register
```

```text
POST /api/auth/login
```


```text
POST /api/auth/logout
```

```text
GET /api/users/me
```

```text
PUT /api/users/me
```

```text
PUT /api/users/change-password
```

---

## Frontend

Cần:

```text
RegisterPage

LoginPage

ProfilePage

AuthContext

ProtectedRoute

AdminRoute

InspectorRoute
```

---

## Test Cases chính

```text
Register success
```

```text
Duplicate email
```

```text
Login success
```

```text
Wrong password
```

```text
Locked user
```

```text
Invalid JWT
```

```text
Expired JWT
```

```text
Unauthorized API
```

---

## Current Tasks

```text
[x] User Entity

[x] Address Entity

[x] UserRepository

[x] Password hashing

[x] JWT Service

[x] Register API

[x] Login API

[x] Current User API

[x] User Profile API

[x] Frontend Login

[x] Frontend Register

[x] AuthContext

[x] ProtectedRoute

[x] Auth unit + MockMvc tests (31 PASS)

[ ] MySQL integration + real API E2E
```

---

# 10. PRODUCT CATALOG MODULE

## Status

```text
NOT_STARTED
```

## Entities

```text
Category

Brand

Product

ProductImage

RacketSpecification
```

---

## Backend

Cần:

```text
CategoryRepository
BrandRepository
ProductRepository
ProductImageRepository
RacketSpecificationRepository
```

Services:

```text
CategoryService
BrandService
ProductService
```

Controller:

```text
CategoryController
BrandController
ProductController
```

---

## API

Public:

```text
GET /api/categories
```

```text
GET /api/brands
```

```text
GET /api/products
```

```text
GET /api/products/{id}
```

Admin:

```text
POST /api/products
```

```text
PUT /api/products/{id}
```

```text
DELETE /api/products/{id}
```

```text
POST /api/products/{id}/images
```

---

## Search / Filter

Cần hỗ trợ:

```text
keyword

category

brand

minPrice

maxPrice

weight

balance

flex

playStyle

skillLevel
```

---

## Frontend

```text
HomePage

ProductListPage

ProductDetailPage

ProductCard

ProductFilter

ProductGallery

ProductSpecification

Pagination
```

---

## Current Tasks

```text
[ ] Category Entity

[ ] Brand Entity

[ ] Product Entity

[ ] ProductImage Entity

[ ] RacketSpecification Entity

[ ] Repositories

[ ] Product Service

[ ] Product APIs

[ ] Search

[ ] Filter

[ ] Pagination

[ ] Product List UI

[ ] Product Detail UI

[ ] Admin Product CRUD

[ ] Product tests
```

---

# 11. CART MODULE

## Status

```text
NOT_STARTED
```

## Entities

```text
Cart

CartItem
```

---

## Backend Components

```text
CartRepository

CartItemRepository

CartService

CartController
```

---

## APIs

```text
GET /api/cart
```

```text
POST /api/cart/items
```

```text
PUT /api/cart/items/{id}
```

```text
DELETE /api/cart/items/{id}
```

---

## Business Rules

Backend phải kiểm tra:

```text
Product exists

Product ACTIVE

quantity > 0

quantity <= stock
```

Không tin giá từ Frontend.

---

## Frontend

```text
CartPage

CartItem

CartSummary

CartBadge
```

---

# 12. SHOP ORDER MODULE

## Status

```text
NOT_STARTED
```

## Entities

```text
Order

OrderItem
```

---

## Business Flow

```text
Cart
↓
Checkout
↓
Validate Stock
↓
Calculate Total
↓
Create Order
↓
Create OrderItem
↓
Decrease Stock
↓
Clear Cart
```

Toàn bộ phải nằm trong Database Transaction.

---

## APIs

```text
POST /api/orders
```

```text
GET /api/orders/my
```

```text
GET /api/orders/{id}
```

```text
POST /api/orders/{id}/cancel
```

Admin:

```text
PATCH /api/admin/orders/{id}/status
```

---

## Frontend

```text
CheckoutPage

OrderSuccessPage

MyOrdersPage

OrderDetailPage
```

---

## Critical Tests

```text
Empty cart
```

```text
Out of stock
```

```text
Concurrent checkout
```

```text
Wrong owner
```

```text
Invalid cancel status
```

---

# 13. REVIEW & FAVORITE

## Status

```text
NOT_STARTED
```

## Review

Entity:

```text
Review
```

Rule:

```text
Order COMPLETED
+
User owns Order
+
Product exists in OrderItem
```

---

## Favorite

Entity:

```text
Favorite
```

Rule:

```text
UNIQUE(user_id, product_id)
```

---

# 14. PLAYER PROFILE

## Status

```text
NOT_STARTED
```

Entity:

```text
PlayerProfile
```

Fields:

```text
skillLevel

playStyle

playType

wristStrength

preferredWeight

minBudget

maxBudget
```

API:

```text
GET /api/player-profile

POST /api/player-profile

PUT /api/player-profile
```

---

# 15. RECOMMENDATION MODULE

## Status

```text
NOT_STARTED
```

## Components

```text
RecommendationService

MatchScoreCalculator

RecommendationWeightRepository
```

Optional:

```text
RecommendationLogRepository
```

---

## Algorithm

Default weighting:

```text
Skill            20%

Play Style       25%

Wrist            15%

Weight           15%

Flex             10%

Budget           15%
```

---

## API

```text
GET /api/recommendations
```

---

## Response cần có

```text
Product

MatchScore

Reasons
```

---

## Tests

```text
Exact match

Poor match

Budget mismatch

Weight mismatch

Correct sorting

Score <= 100

Score >= 0
```

---

# 16. MARKETPLACE LISTING MODULE

## Status

```text
NOT_STARTED
```

## Entities

```text
UsedListing

UsedListingImage

UsedRacketSpecification
```

---

## Listing Status

```text
DRAFT

PENDING_REVIEW

AVAILABLE

RESERVED

SOLD

REJECTED

HIDDEN
```

---

## APIs

```text
GET /api/marketplace/listings

GET /api/marketplace/listings/{id}

POST /api/marketplace/listings

PUT /api/marketplace/listings/{id}

GET /api/marketplace/my-listings

POST /api/marketplace/listings/{id}/images

POST /api/marketplace/listings/{id}/submit
```

Admin:

```text
POST /api/admin/marketplace/listings/{id}/approve

POST /api/admin/marketplace/listings/{id}/reject
```

---

## Frontend

```text
MarketplacePage

ListingDetailPage

SellListingPage

MyListingsPage

ListingCard

ConditionBadge

VerifiedBadge
```

---

# 17. VALUATION MODULE

## Status

```text
NOT_STARTED
```

## Components

```text
Valuation

ValuationService

ValuationCalculator
```

---

## Factors

```text
Reference Price

Age

Condition

Warranty

Accessories

Repair

Demand
```

---

## Result

```text
Estimated Price

Suggested Min

Suggested Max
```

hoặc:

```text
Quick Sell

Fair Price

Expected Price
```

---

## APIs

```text
POST /api/valuations

GET /api/valuations/listing/{listingId}
```

Advanced:

```text
POST /api/valuations/ai
```

chưa thuộc MVP bắt buộc.

---

# 18. MARKETPLACE ORDER MODULE

## Status

```text
NOT_STARTED
```

## Entity

```text
MarketplaceOrder
```

---

## APIs

```text
POST /api/market-orders

GET /api/market-orders/buying

GET /api/market-orders/selling

GET /api/market-orders/{id}

POST /api/market-orders/{id}/confirm-received
```

---

## Critical Business Rule

Purchase phải:

```text
Lock Listing
↓
Check AVAILABLE
↓
Check Buyer != Seller
↓
Create MarketplaceOrder
↓
Listing RESERVED
↓
Create Escrow
```

trong một transaction.

---

# 19. ESCROW MODULE

## Status

```text
NOT_STARTED
```

## Entity

```text
EscrowTransaction
```

---

## Status

```text
WAITING_PAYMENT

HELD

RELEASED

DISPUTED

REFUNDED

CANCELLED
```

---

## API MVP

```text
POST /api/escrow/{marketOrderId}/pay
```

Đây là payment simulation trong phạm vi đồ án.

---

## Critical Tests

```text
Pay valid order

Double pay

Double release

Release while disputed

Invalid transition
```

---

# 20. SHIPMENT MODULE

## Status

```text
NOT_STARTED
```

Entity:

```text
Shipment
```

Status:

```text
WAITING_PICKUP

PICKED_UP

IN_TRANSIT

DELIVERED

FAILED

RETURNED
```

APIs:

```text
POST /api/shipments

PATCH /api/shipments/{id}/status
```

---

# 21. INSPECTION MODULE

## Status

```text
NOT_STARTED
```

Entities:

```text
InspectionRequest

InspectionReport
```

---

## Flow

```text
REQUESTED
↓
ACCEPTED
↓
INSPECTING
↓
COMPLETED
```

---

## APIs

Seller:

```text
POST /api/inspections
```

```text
GET /api/inspections/my
```

Inspector:

```text
GET /api/inspector/inspections

POST /api/inspector/inspections/{id}/accept

POST /api/inspector/inspections/{id}/start

POST /api/inspector/inspections/{id}/report
```

---

# 22. DISPUTE MODULE

## Status

```text
NOT_STARTED
```

Entities:

```text
Dispute

DisputeEvidence
```

---

## Reasons

```text
NOT_AS_DESCRIBED

DAMAGED

SUSPECTED_FAKE

MISSING_ACCESSORIES

WRONG_ITEM

NOT_RECEIVED

OTHER
```

---

## APIs

```text
POST /api/disputes

GET /api/disputes/{id}

POST /api/disputes/{id}/evidences
```

Admin:

```text
GET /api/admin/disputes

POST /api/admin/disputes/{id}/resolve
```

---

# 23. SELLER RATING & REPUTATION

## Status

```text
NOT_STARTED
```

Entity:

```text
SellerRating
```

Services:

```text
SellerRatingService

SellerReputationService
```

API:

```text
POST /api/seller-ratings

GET /api/sellers/{sellerId}
```

---

# 24. NOTIFICATION

## Status

```text
NOT_STARTED
```

MVP:

```text
Database Notification
```

API:

```text
GET /api/notifications

PATCH /api/notifications/{id}/read

PATCH /api/notifications/read-all
```

---

# 25. ADMIN MODULE

## Status

```text
NOT_STARTED
```

Admin Dashboard cần:

```text
Products

Categories

Brands

Orders

Users

Marketplace Listings

Inspections

Escrow

Disputes

Reviews

Recommendation Configuration

Statistics
```

Admin không được tạo lại business logic đã tồn tại.

Ví dụ:

```text
AdminMarketplaceController
↓
MarketplaceService
```

---

# 26. STATISTICS

## Status

```text
NOT_STARTED
```

API dự kiến:

```text
GET /api/admin/statistics/overview

GET /api/admin/statistics/revenue

GET /api/admin/statistics/top-products

GET /api/admin/statistics/top-sellers
```

---

# 27. AUDIT LOG

## Status

```text
NOT_STARTED
```

Các hành động cần audit:

```text
Approve Listing

Reject Listing

Lock User

Unlock User

Complete Inspection

Release Escrow

Refund

Resolve Dispute
```

---

# 28. SYSTEM TESTING

## Status

```text
NOT_STARTED
```

Cần thực hiện:

```text
Unit Test

Integration Test

API Test

Security Test

Concurrency Test

End-to-End Test
```

---

# 29. Critical E2E Flow – Shop

```text
Register
↓
Login
↓
Products
↓
Cart
↓
Checkout
↓
Order
↓
Admin Confirm
↓
Shipping
↓
Completed
↓
Review
```

Status hiện tại:

```text
NOT_TESTED
```

---

# 30. Critical E2E Flow – Recommendation

```text
Login
↓
Player Profile
↓
Recommendation
↓
Match Score
↓
Product Detail
```

Status:

```text
NOT_TESTED
```

---

# 31. Critical E2E Flow – Marketplace

```text
Seller Create Listing
↓
Valuation
↓
Submit
↓
Admin Approve
↓
Buyer Purchase
↓
Escrow
↓
Shipment
↓
Confirm
↓
Seller Rating
```

Status:

```text
NOT_TESTED
```

---

# 32. Critical E2E Flow – Dispute

```text
Marketplace Purchase
↓
Escrow HELD
↓
Shipment
↓
Buyer Dispute
↓
Evidence
↓
Escrow DISPUTED
↓
Admin Resolve
```

Status:

```text
NOT_TESTED
```

---

# 33. Deployment

## Status

```text
NOT_STARTED
```

Target architecture:

```text
React Frontend
↓
HTTPS
↓
Spring Boot Backend
↓
MySQL
```

Storage:

```text
Cloudinary
```

---

# 34. Known Bugs

Hiện tại:

```text
NONE
```

Khi có bug phải ghi theo format:

```text
BUG-ID:
BUG-001

Module:
Marketplace

Severity:
CRITICAL

Status:
OPEN

Description:
Một listing có thể tạo hai order khi mua đồng thời.

Expected:
Chỉ một order thành công.

Current Result:
Hai order được tạo.

Root Cause:
Chưa sử dụng row lock.

Fix:
Pending.
```

---

# 35. Known Technical Debt

Hiện tại:

```text
NONE
```

Sau này ví dụ:

```text
TD-001

Module:
Product

Description:
ProductMapper đang map thủ công lặp lại nhiều nơi.

Priority:
MEDIUM
```

---

# 36. Architecture Issues Pending

Hiện tại:

D01–D14: APPROVED, áp dụng tài liệu qua CHG-001–CHG-014; pending = 0.

D13 → CHG-013; D14 → CHG-014: APPROVED, tài liệu đã đồng bộ. Pending Architecture Issues = 0.

Nếu phát hiện vấn đề kiến trúc nhưng chưa quyết định:

```text
ARCH-001

Question:
Một Listing có thể request inspection nhiều lần hay chỉ một lần?

Affected:
Database
Inspection
Listing Detail

Status:
PENDING_REVIEW
```

Sau khi quyết định:

```text
Update CHANGELOG
+
Update SYSTEM_ANALYSIS_MASTER
```

---

# 37. Current Database Migration

Source: `backend/src/main/resources/db/migration/V1__create_users_and_addresses.sql`.

V1 tạo users, addresses và FK/unique/check/index theo thiết kế Auth & User. **Chưa chạy migration trên MySQL trong môi trường bàn giao; database runtime version chưa xác minh.**

---

# 38. Current API Version

Hiện tại thiết kế:

```text
/api
```

API versioning:

```text
/api là baseline hiện hành; chưa có quyết định APPROVED áp dụng /api/v1.
```

Nếu quyết định:

```text
/api/v1
```

phải cập nhật tài liệu liên quan.

---

# 39. Current Branch

Local working branch: `feature/sprint0-auth-user` (khởi tạo từ main). Chưa push remote; ZIP không chứa `.git`.

---

# 40. Last Completed Milestone

Mốc kiểm chứng gần nhất (2026-10-07): source Sprint 0/Auth & User; backend package PASS; 31 unit/MockMvc tests PASS; 4 frontend component tests PASS; frontend build PASS; browser smoke desktop/mobile PASS.

Đây là mốc bàn giao source và kiểm thử độc lập, **không phải milestone DONE** của Sprint 0/Auth vì chưa chạy MySQL integration/E2E API thật.

Chi tiết: `IMPLEMENTATION_HANDOFF.md` và `backend-test-results.json`.

---

# 41. Next Milestone

SPRINT 0 + AUTH & USER MYSQL / E2E ACCEPTANCE

Backend source + frontend source đã viết. Chưa được đánh dấu DONE trước khi chạy trên MySQL và kiểm thử luồng frontend → backend → database.

---

# 42. Next Action

1. Tạo MySQL development/test database và cấu hình environment riêng.
2. Chạy Flyway V1 + Hibernate validate và backend runtime trên MySQL.
3. Chạy AuthMySqlIntegrationTest và Playwright E2E qua API thật.
4. Ghi kết quả thực tế; chỉ chuyển DONE khi đáp ứng Definition of Done.
5. Tiếp tục Product Catalog theo thứ tự chính thức, sau đó Cart/Order và Recommendation.

Chi tiết files, API, test và giới hạn: `IMPLEMENTATION_HANDOFF.md`.

---

# 43. Quy tắc cập nhật file này

Sau mỗi buổi phát triển hoặc milestone quan trọng, cần kiểm tra:

```text
Module status có thay đổi không?

Task nào hoàn thành?

API nào đã chạy?

Database migration nào mới?

Bug nào mới?

Bug nào đã sửa?

Có technical debt mới không?

Có architecture issue không?

Next Action là gì?
```

Sau đó cập nhật file.

---

# 44. Không ghi kế hoạch giả thành trạng thái thật

Ví dụ:

```text
EscrowService
```

có trong thiết kế không có nghĩa:

```text
EscrowService DONE
```

Chỉ đánh:

```text
DONE
```

khi source code thật đã được triển khai và kiểm thử theo tiêu chí của project.

---

# 45. Không để trạng thái file lỗi thời

Nếu source đã làm:

```text
Auth DONE
```

nhưng file vẫn:

```text
Auth NOT_STARTED
```

thì chat mới có thể đưa ra hướng dẫn sai.

Do đó `IMPLEMENTATION_STATUS` cần được coi là tài liệu sống.

---

# 46. Quan hệ với Source Code

Khi source code tồn tại:

```text
Source Code
=
bằng chứng triển khai thực tế
```

`IMPLEMENTATION_STATUS`:

```text
=
bản đồ trạng thái của source code
```

Không dùng file này để thay thế source code.

---

# 47. Snapshot trước triển khai — 2026-10-05 (lịch sử)

Tại thời điểm tạo tài liệu:

```text
Analysis:
DONE

System Design:
DONE

Project Setup:
NOT_STARTED

Implementation:
NOT_STARTED

Testing:
NOT_STARTED

Deployment:
NOT_STARTED
```

---

# 48. Snapshot tổng thể — 2026-10-05 (lịch sử)

```text
PROJECT STATUS

████████░░░░░░░░░░░░

PHASE:
DOCUMENT VERIFICATION — ALL_OK
IMPLEMENTATION NOT_STARTED
```

Bước tiếp theo:

```text
DOCUMENT VERIFICATION ALL_OK — PENDING ARCHITECTURE ISSUES 0
THEN SPRINT 0 (NOT_STARTED)
```
---

# Document Verification — 2026-10-05 (lịch sử; không phải tiến độ runtime hiện tại)

- D01–D14: APPROVED qua CHG-001–CHG-014; đồng bộ tài liệu, pending D01–D14 = 0.
- Document architecture version: v1.14; implementation vẫn NOT_STARTED.
- D13 → CHG-013; D14 → CHG-014: APPROVED, đồng bộ; Pending Architecture Issues = 0.
- Sprint 0 NOT_STARTED; không tạo Backend/Frontend, không migration, không source code.
- api/API_SPECIFICATION.md và database/DATABASE_DESIGN.md vẫn là placeholder rỗng: các source rules chưa quy định chúng thành specification chính thức. Contract/schema chính thức được cập nhật ở Master/Rules/Changelog. database/schema.sql và seed.sql không đổi.
- Build/API/integration tests NOT_STARTED; document verification không thay thế tests implementation.
- Render DOCX đã thử và thất bại vì thiếu LibreOffice/soffice.exe trong runtime; visual QA UNVERIFIED. Không đánh dấu render thành công.

## Implementation acceptance bổ sung (kế hoạch, chưa chạy)

### D01 / CHG-001

**Nguồn: CHG-001 / D01 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N InspectionRequest; InspectionRequest 1 → 0..1 InspectionReport.

Một Listing có nhiều request trong lịch sử nhưng tối đa một request active: REQUESTED, ACCEPTED, INSPECTING. COMPLETED, REJECTED, CANCELLED không active.

Seller chỉ tạo request mới cho Listing của mình khi không có request active; Backend kiểm tra đồng thời trong transaction để ngăn request song song.

Mỗi request COMPLETED phải có report. Không xóa lịch sử kiểm định.

currentInspectionReport là report của request COMPLETED mới nhất còn hiệu lực. Verified Badge và Valuation dùng report còn hiệu lực này; hiệu lực sau sửa Listing theo CHG-007.

inspection_requests.listing_id dùng FK/index thường, không có unique trên listing_id.

Status implementation: NOT_STARTED.

### D02 / CHG-002

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

Status implementation: NOT_STARTED.

### D03 / CHG-003

**Nguồn: CHG-003 / D03 — APPROVED 2026-10-05.**

Giữ weight Skill 20, Play Style 25, Wrist 15, Preferred Weight 15, Flex 10, Budget 15; tổng 100. MatchScoreCalculator tính deterministic/bounded/testable, score trong [0,100].

Skill order: BEGINNER=0, INTERMEDIATE=1, ADVANCED=2. abs(profileLevel-racketLevel) = 0/1/2 → SkillScore = 20/12/5. Bảng đối xứng: [20,12,5]; [12,20,12]; [5,12,20].

CHG-013: targetFlex chỉ theo skillLevel: BEGINNER → FLEXIBLE, INTERMEDIATE → MEDIUM, ADVANCED → STIFF; không dùng wristStrength. Flex ordinal FLEXIBLE=0, MEDIUM=1, STIFF=2, EXTRA_STIFF=3. abs(target-racket) = 0 →10; =1 →6; >=2 →2. Giữ bảng Wrist–Flex và weights.

effectivePrice = salePrice hợp lệ nếu có, ngược lại product.price.

BudgetScore: effectivePrice < minBudget → 12; minBudget <= effectivePrice <= maxBudget → 15; maxBudget < effectivePrice <= 1.10*maxBudget → 10; 1.10*maxBudget < effectivePrice <= 1.20*maxBudget → 5; effectivePrice > 1.20*maxBudget → 0.

Các scoring rule không mâu thuẫn khác giữ nguyên. Không đặt thuật toán trong Controller; Frontend chỉ trình bày.

Status implementation: NOT_STARTED.

### D04 / CHG-004

**Nguồn: CHG-004 / D04 — APPROVED 2026-10-05.**

EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor.

Valuation lưu snapshot: referencePrice, ageFactor, conditionFactor, warrantyFactor, accessoryFactor, repairFactor, demandFactor, conditionScore, valuationMethod, suggestedMinPrice, suggestedMaxPrice, algorithmVersion, createdAt; giữ estimatedPrice đang có.

Schema valuations bổ sung repair_factor DECIMAL(5,3), condition_score DECIMAL(4,2), algorithm_version VARCHAR(50). Các factor dùng DECIMAL(5,3) như schema hiện hành; tiền DECIMAL(15,2), không FLOAT/DOUBLE. Snapshot không bị tính lại theo dữ liệu mới khi đọc lịch sử.

Condition Score 0.00–10.00; chuẩn hóa/làm tròn tới hai chữ số thập phân trước khi map Grade: [9.50,10.00] → S; [8.50,9.50) → A; [7.00,8.50) → B; [5.00,7.00) → C; [0.00,5.00) → D.

Nguồn hợp lệ/còn hiệu lực ưu tiên INSPECTION > AI > SELLER. Dùng currentInspectionReport theo CHG-001/007.

Các bảng factor hiện hành không mâu thuẫn được giữ nguyên; các khoảng dùng interval không chồng lấn.

Status implementation: NOT_STARTED.

### D05 / CHG-005

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

Status implementation: NOT_STARTED.

### D06 / CHG-006

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

Status implementation: NOT_STARTED.

### D07 / CHG-007

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

Status implementation: NOT_STARTED.

### D08 / CHG-008

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

Status implementation: NOT_STARTED.

### D09 / CHG-009

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

Status implementation: NOT_STARTED.

### D10 / CHG-010

**Nguồn: CHG-010 / D10 — APPROVED 2026-10-05.**

Shop Order flow: PENDING → CONFIRMED → PREPARING → SHIPPING → COMPLETED.

User chỉ cancel order của mình khi PENDING hoặc CONFIRMED trước PREPARING. Không cancel từ PREPARING, SHIPPING, COMPLETED, CANCELLED.

Cancel transaction: lock Order → validate owner/state → restore stock từng OrderItem đúng một lần → cập nhật payment nếu cần → Order CANCELLED → commit.

UNPAID không refund; PAID → REFUNDED theo payment simulation MVP. Double cancel phải reject hoặc idempotent theo implementation policy, tuyệt đối không restore stock hai lần.

ADMIN hoặc backend-authorized operation quản lý fulfillment CONFIRMED/PREPARING/SHIPPING/COMPLETED qua API được Backend validate. USER không gửi arbitrary orderStatus.

Status implementation: NOT_STARTED.

### D11 / CHG-011

**Nguồn: CHG-011 / D11 — APPROVED 2026-10-05.**

MVP chỉ JWT Access Token với expiration hữu hạn. Không Refresh Token, RefreshTokenRequest, persistence/rotation/revocation của Refresh Token trong MVP.

Auth API MVP: POST /api/auth/register; POST /api/auth/login; POST /api/auth/logout; GET /api/users/me; PUT /api/users/me; PUT /api/users/change-password.

Logout: Frontend xóa authentication state/token. Backend không tuyên bố revoke Access Token khi không có server-side token store. Token đã phát hành còn hiệu lực tới expiration.

Khi token hết hạn/401, Frontend yêu cầu đăng nhập lại; không tự gọi refresh endpoint.

Refresh Token là Post-MVP / Future Security Enhancement; cần Change riêng cho storage, expiration, rotation, revocation, logout, frontend storage và security tests. JWT và system roles không đổi.

Status implementation: NOT_STARTED.

### D12 / CHG-012

**Nguồn: CHG-012 / D12 — APPROVED 2026-10-05.**

Promotions không thuộc MVP/baseline hiện tại. Loại bỏ màn hình Admin Promotions, route promotions và mô tả quản lý khuyến mãi khỏi chức năng hiện tại.

Không tạo Promotion Entity/table/API/module hoặc discount calculation để hợp thức hóa nội dung legacy.

Product price và Order total tiếp tục do Backend quyết định. Promotions chỉ có thể được xem xét như Future Development qua change-management.

Status implementation: NOT_STARTED.


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


# Implementation Update — 2026-10-07

- Sprint 0: IN_PROGRESS. Auth & User: TESTING. Các module khác: NOT_STARTED.
- Source: React + Spring Boot Modular Monolith + MySQL/Flyway; package com.badminton.
- API: register, login, logout, current user, update profile, change password, Address CRUD.
- CHG-011 được áp dụng ở source: chỉ Access Token, không refresh/revocation giả; kiểm thử Auth ở mức unit/MockMvc.
- CHG-008 áp dụng response envelope trong phạm vi Auth/User; CHG-009 áp dụng User–Address và không ép tạo Cart/PlayerProfile khi register. Các phần còn lại của các CHG này chưa triển khai.
- Chưa có kiến trúc mới cần phê duyệt; không thêm CHG.
- Kết quả và phần chưa chạy: IMPLEMENTATION_HANDOFF.md.
