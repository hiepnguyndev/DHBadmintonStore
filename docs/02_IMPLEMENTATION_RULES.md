# 02_IMPLEMENTATION_RULES.md

# XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ CẦU LÔNG ĐA NỀN TẢNG TÍCH HỢP CHỨC NĂNG GỢI Ý SẢN PHẨM THEO NHU CẦU NGƯỜI DÙNG

---

# 1. Mục đích của tài liệu

Tài liệu này định nghĩa toàn bộ các **quy tắc bắt buộc khi triển khai source code** cho project.

Mục tiêu chính là đảm bảo rằng khi project được chia thành nhiều đoạn chat, nhiều module và nhiều giai đoạn phát triển, toàn bộ source code vẫn giữ được:

- cùng kiến trúc;
- cùng naming convention;
- cùng business rule;
- cùng API style;
- cùng security model;
- cùng cách xử lý transaction;
- cùng định nghĩa trạng thái;
- cùng cách tổ chức source code.

Tài liệu này không phải tài liệu mô tả yêu cầu nghiệp vụ đầy đủ.

Tài liệu yêu cầu nghiệp vụ chi tiết nằm trong:

`01_SYSTEM_ANALYSIS_MASTER.docx` và bản đồng hành `01_SYSTEM_ANALYSIS_MASTER.md`

Tài liệu hiện tại trả lời câu hỏi:

> Khi chuyển thiết kế thành source code, developer phải tuân theo những nguyên tắc nào?

---

# 2. Nguyên tắc Source of Truth

Khi triển khai, không được dựa chỉ vào một đoạn chat cũ.

Các nguồn chuẩn của project gồm:

1. `00_PROJECT_STRUCTURE.md`
2. `01_SYSTEM_ANALYSIS_MASTER.docx` và bản đồng hành `01_SYSTEM_ANALYSIS_MASTER.md`
3. `02_IMPLEMENTATION_RULES.md`
4. `03_IMPLEMENTATION_STATUS.md`
5. `04_CHANGELOG.md`

Nếu phát hiện mâu thuẫn, ưu tiên xem xét theo thứ tự:

1. Architecture Decision mới nhất có trạng thái APPROVED trong `04_CHANGELOG.md`. Ví dụ PROPOSED không phải quyết định áp dụng.
2. Quy tắc hiện tại trong `02_IMPLEMENTATION_RULES.md`.
3. Thiết kế chi tiết trong `01_SYSTEM_ANALYSIS_MASTER.docx` và bản đồng hành `01_SYSTEM_ANALYSIS_MASTER.md`.
4. Tổng quan trong `00_PROJECT_STRUCTURE.md`.

Không tự ý chọn một version khác chỉ vì code dễ hơn. Source code chỉ phản ánh trạng thái implementation, không tự động ghi đè thiết kế. Nếu DOCX và MD mâu thuẫn mà nguồn cấp cao hơn chưa phân định, ghi NEEDS_DECISION thay vì tự ưu tiên một bản.

---

# 3. Quy tắc thay đổi kiến trúc

Không thay đổi trực tiếp các nội dung sau trong một chat triển khai module:

- Entity;
- Relationship;
- Status;
- API lớn;
- Technology Stack;
- Authentication model;
- Database structure;
- Transaction flow;
- Module boundary;
- Business rule cốt lõi.

Nếu phát hiện cần thay đổi:

`Module Chat → Architecture Issue → 00 MASTER ARCHITECTURE → Phân tích ảnh hưởng → Quyết định → CHANGELOG → Update tài liệu → Update code → Retest`

Không sửa âm thầm.

---

# 4. Technology Stack chính thức

## 4.1 Frontend

Sử dụng:

- React;
- React Router;
- Axios;
- React Hook Form hoặc giải pháp tương đương;
- Context API hoặc Zustand cho global state phù hợp.

UI library có thể chọn khi bắt đầu implementation:

- Tailwind CSS;
- Material UI;
- Ant Design;

nhưng chỉ nên chọn một hướng chính để tránh UI không đồng nhất.

---

## 4.2 Backend

Sử dụng:

- Java;
- Spring Boot;
- Spring Security;
- Spring Data JPA;
- Hibernate;
- JWT.

---

## 4.3 Database

Sử dụng:

- MySQL.

---

## 4.4 Architecture

Backend sử dụng:

> **Modular Monolith**

Không chuyển sang Microservices trong MVP.

---

## 4.5 API

Sử dụng:

- REST;
- JSON.

---

## 4.6 File Storage

Ưu tiên:

- Cloudinary;

hoặc Object Storage tương đương.

Không lưu ảnh production trực tiếp trong source code.

---

# 5. Quy tắc kiến trúc Backend

Luồng xử lý chuẩn:

`Client → Controller → DTO → Service → Repository → Entity → Database`

Response:

`Database → Entity → Mapper → Response DTO → Controller → Client`

Không bỏ qua Service Layer đối với business nghiệp vụ chính.

---

# 6. Controller Layer

Controller có trách nhiệm:

- nhận HTTP Request;
- lấy Path Variable;
- lấy Query Parameter;
- nhận Request DTO;
- trigger validation;
- lấy Current User;
- gọi Service;
- trả Response.

Controller không nên:

- query Repository trực tiếp;
- tự xử lý nhiều business rule;
- tự tính tiền;
- tự tính Match Score;
- tự tính Valuation;
- tự thay đổi Escrow state;
- tự thực hiện nhiều thao tác Database liên tiếp;
- tự quyết định ownership phức tạp.

Ví dụ đúng:

`OrderController → OrderService.createOrder()`

Ví dụ sai:

`OrderController → CartRepository → ProductRepository → OrderRepository`

---

# 7. Service Layer

Service chịu trách nhiệm:

- business logic;
- business validation;
- ownership check;
- state transition;
- transaction boundary;
- phối hợp nhiều Repository;
- gọi Service khác khi cần;
- tạo notification;
- ghi Audit Log đối với action quan trọng.

Ví dụ:

`MarketplaceOrderService.createOrder()`

phải xử lý:

1. lấy Current User;
2. load Listing;
3. lock Listing;
4. kiểm tra Listing AVAILABLE;
5. kiểm tra Buyer != Seller;
6. tạo MarketplaceOrder;
7. chuyển Listing thành RESERVED;
8. tạo EscrowTransaction;
9. commit transaction.

---

# 8. Repository Layer

Repository phụ trách thao tác dữ liệu:

- find;
- search;
- insert;
- update;
- delete nếu nghiệp vụ cho phép;
- pagination;
- database lock;
- query tối ưu.

Repository không quyết định:

- User có được mua Listing không;
- Seller có quyền sửa Listing không;
- Escrow có được release không;
- Dispute có hợp lệ không.

Những rule này thuộc Service.

---

# 9. Entity Layer

Entity biểu diễn dữ liệu persistence.

Ví dụ:

`User ↔ users`

`Product ↔ products`

`UsedListing ↔ used_listings`

`MarketplaceOrder ↔ marketplace_orders`

Entity không được dùng như API contract mặc định.

---

# 10. DTO Rule

Mỗi API cần sử dụng DTO phù hợp.

Phân biệt:

- Request DTO;
- Response DTO.

Ví dụ:

`CreateProductRequest`

`UpdateProductRequest`

`ProductResponse`

`ProductDetailResponse`

Không nhận trực tiếp `Product Entity` trong Controller cho API public.

---

# 11. Không expose Entity trực tiếp

Không trả nguyên Entity nếu Entity có field không phù hợp cho Frontend.

Ví dụ `User` có:

- passwordHash;
- internal status;
- audit information.

Response public không được chứa `passwordHash`.

---

# 12. Mapper Rule

Mapper dùng để chuyển:

`Request DTO → Entity`

và:

`Entity → Response DTO`

Không copy thủ công cùng một logic mapping ở nhiều Controller.

Có thể map thủ công trong Mapper class để dễ hiểu trong đồ án.

---

# 13. Backend Package Structure

Cấu trúc gốc:

`com.badminton`

Các module:

- auth;
- user;
- category;
- brand;
- product;
- cart;
- order;
- review;
- favorite;
- recommendation;
- marketplace;
- valuation;
- inspection;
- marketorder;
- escrow;
- shipment;
- dispute;
- rating;
- notification;
- statistics;
- audit;
- security;
- exception;
- config;
- common.

---

# 14. Cấu trúc bên trong module

Ví dụ:

`product/`

gồm:

- controller;
- service;
- repository;
- entity;
- dto/request;
- dto/response;
- mapper.

Có thể thêm:

- specification;
- validator;
- query;

nếu thực sự cần.

Không tạo package không cần thiết chỉ để tăng số layer.

---

# 15. Naming Convention – Java

Entity dùng danh từ số ít:

- User;
- Product;
- Order;
- UsedListing;
- MarketplaceOrder.

Controller:

- UserController;
- ProductController;
- MarketplaceController.

Service:

- UserService;
- ProductService;
- MarketplaceOrderService.

Repository:

- UserRepository;
- ProductRepository.

DTO Request:

- CreateProductRequest;
- UpdateListingRequest;
- LoginRequest.

DTO Response:

- UserResponse;
- ProductResponse;
- MarketplaceOrderDetailResponse.

Calculator:

- MatchScoreCalculator;
- ValuationCalculator;
- ConditionScoreCalculator;
- SellerReputationCalculator.

---

# 16. Naming Convention – Database

Tên bảng dùng:

`snake_case`

Ví dụ:

- users;
- product_images;
- racket_specifications;
- used_listings;
- marketplace_orders;
- escrow_transactions.

Tên column:

- user_id;
- created_at;
- selling_price;
- market_order_id.

---

# 17. Primary Key Rule

Mặc định dùng:

`BIGINT`

Primary key:

`AUTO_INCREMENT`

nếu phù hợp.

Ví dụ:

`user_id BIGINT PRIMARY KEY AUTO_INCREMENT`

---

# 18. Foreign Key Rule

Các relationship chính cần có Foreign Key.

Ví dụ:

`orders.user_id → users.user_id`

`cart_items.product_id → products.product_id`

`marketplace_orders.listing_id → used_listings.listing_id`

Không tạo field ID không có relationship khi relationship cần được bảo vệ.

---

# 19. Money Rule

Mọi giá trị tiền sử dụng:

`DECIMAL(15,2)`

Không sử dụng:

- FLOAT;
- DOUBLE;

cho:

- product price;
- order total;
- listing price;
- escrow amount;
- valuation result;
- platform fee.

---

# 20. Date & Time Rule

Các Entity quan trọng sử dụng:

- createdAt;
- updatedAt.

Các nghiệp vụ cần thêm timestamp chuyên biệt.

Ví dụ Escrow:

- paidAt;
- heldAt;
- releasedAt;
- refundedAt.

Shipment:

- shippedAt;
- deliveredAt.

Dispute:

- createdAt;
- resolvedAt.

---

# 21. Enum và Status Rule

Status không được viết tùy ý bằng String rải rác trong source.

Nên sử dụng enum phía Backend.

Ví dụ:

`ListingStatus.AVAILABLE`

thay vì:

`"AVAILABLE"`

lặp lại trong nhiều file.

Database có thể lưu dạng VARCHAR để dễ migration nếu cần.

---

# 22. User Role

System role chính thức:

- USER;
- INSPECTOR;
- ADMIN.

Không tạo:

- BUYER;
- SELLER;

thành System Role.

Buyer và Seller là business role của USER.

---

# 23. User Status

Các trạng thái:

- ACTIVE;
- LOCKED;
- SUSPENDED;
- DELETED.

User bị LOCKED hoặc SUSPENDED không được tiếp tục thao tác nhạy cảm theo rule.

---

# 24. Shop Order Status

**Nguồn: CHG-010 / D10 — APPROVED 2026-10-05.**

Shop Order flow: PENDING → CONFIRMED → PREPARING → SHIPPING → COMPLETED.

User chỉ cancel order của mình khi PENDING hoặc CONFIRMED trước PREPARING. Không cancel từ PREPARING, SHIPPING, COMPLETED, CANCELLED.

Cancel transaction: lock Order → validate owner/state → restore stock từng OrderItem đúng một lần → cập nhật payment nếu cần → Order CANCELLED → commit.

UNPAID không refund; PAID → REFUNDED theo payment simulation MVP. Double cancel phải reject hoặc idempotent theo implementation policy, tuyệt đối không restore stock hai lần.

ADMIN hoặc backend-authorized operation quản lý fulfillment CONFIRMED/PREPARING/SHIPPING/COMPLETED qua API được Backend validate. USER không gửi arbitrary orderStatus.

---

# 25. Payment Status Shop

Chỉ dùng:

- UNPAID;
- PAID;
- REFUNDED;
- FAILED.

---

# 26. Listing Status

Chỉ dùng:

- DRAFT;
- PENDING_REVIEW;
- AVAILABLE;
- RESERVED;
- SOLD;
- REJECTED;
- HIDDEN.

Flow:

`DRAFT → PENDING_REVIEW → AVAILABLE → RESERVED → SOLD`

Nhánh:

`PENDING_REVIEW → REJECTED`

`AVAILABLE → HIDDEN`

---

# 27. Listing Creation Rule

Seller tạo Listing:

`status = DRAFT`

Frontend không được gửi:

`status = AVAILABLE`

để bypass Admin Review.

---

# 28. Listing Submit Rule

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

---

# 29. Listing Approval Rule

Chỉ ADMIN được:

`PENDING_REVIEW → AVAILABLE`

User thường không được approve Listing.

---

# 30. Listing Edit Rule

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

---

# 31. Marketplace Order Status

Chỉ dùng:

- PENDING_PAYMENT;
- PAID;
- WAITING_SHIPMENT;
- SHIPPING;
- DELIVERED;
- COMPLETED;
- CANCELLED;
- DISPUTED;
- REFUNDED.

---

# 32. Marketplace Purchase Rule

Buyer chỉ được mua nếu:

- đã đăng nhập;
- User ACTIVE;
- Listing AVAILABLE;
- Buyer != Seller.

Backend không tin buyerId do Frontend gửi.

Backend lấy:

`buyerId = currentUser.id`

Seller lấy từ:

`listing.sellerId`

---

# 33. Marketplace Price Rule

Frontend không quyết định giá.

Khi tạo MarketplaceOrder:

`itemPrice = listing.sellingPrice`

được lấy từ Database.

Nếu Request gửi amount khác:

- ignore field nếu không nằm trong DTO;
- hoặc reject request nếu có field không hợp lệ theo policy.

---

# 34. Một Listing chỉ có một MarketplaceOrder active

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

---

# 35. Marketplace Purchase Transaction

Transaction bắt buộc:

1. Begin transaction.
2. Load Listing with lock.
3. Check AVAILABLE.
4. Check Buyer != Seller.
5. Create MarketplaceOrder.
6. Update Listing → RESERVED.
7. Create EscrowTransaction.
8. Commit.

Nếu bất kỳ bước nào lỗi:

`ROLLBACK`

---

# 36. Escrow Status

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

---

# 37. Escrow State Transition

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

---

# 38. Escrow Ownership và Permission

Buyer không được tự release theo API generic.

Buyer chỉ gọi nghiệp vụ:

`confirmReceived()`

Backend kiểm tra:

- Current User là Buyer;
- Order DELIVERED;
- Escrow HELD;
- không có active Dispute.

Sau đó Service mới release Escrow.

---

# 39. Escrow Amount Rule

Escrow amount phải lấy từ MarketplaceOrder.

Không nhận amount tùy ý từ Client.

---

# 40. Double Release Protection

Trước release:

- lock Escrow nếu cần;
- kiểm tra status hiện tại.

Nếu đã RELEASED:

trả business error.

Không được release lần hai.

---

# 41. Shipment Rule

Seller được tạo Shipment khi:

- Current User là Seller của MarketplaceOrder;
- Order đang ở trạng thái phù hợp;
- Escrow đã HELD.

Không cho Seller shipment khi Buyer chưa hoàn thành payment simulation.

---

# 42. Confirm Received Rule

Chỉ Buyer được confirm.

Điều kiện tối thiểu:

- Current User = buyerId;
- MarketplaceOrder = DELIVERED;
- Escrow = HELD;
- không có active Dispute.

Sau thành công:

- MarketplaceOrder → COMPLETED;
- Escrow → RELEASED;
- Listing → SOLD.

Nên thực hiện transaction.

---

# 43. Inspection Status

Chỉ dùng:

- REQUESTED;
- ACCEPTED;
- INSPECTING;
- COMPLETED;
- REJECTED;
- CANCELLED.

Flow chính:

`REQUESTED → ACCEPTED → INSPECTING → COMPLETED`

---

# 44. Inspection Request Rule

**Nguồn: CHG-001 / D01 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N InspectionRequest; InspectionRequest 1 → 0..1 InspectionReport.

Một Listing có nhiều request trong lịch sử nhưng tối đa một request active: REQUESTED, ACCEPTED, INSPECTING. COMPLETED, REJECTED, CANCELLED không active.

Seller chỉ tạo request mới cho Listing của mình khi không có request active; Backend kiểm tra đồng thời trong transaction để ngăn request song song.

Mỗi request COMPLETED phải có report. Không xóa lịch sử kiểm định.

currentInspectionReport là report của request COMPLETED mới nhất còn hiệu lực. Verified Badge và Valuation dùng report còn hiệu lực này; hiệu lực sau sửa Listing theo CHG-007.

inspection_requests.listing_id dùng FK/index thường, không có unique trên listing_id.

---

# 45. Inspector Permission

INSPECTOR chỉ xử lý Inspection được assign cho mình hoặc theo quyền ADMIN.

Không cho một Inspector sửa report của Inspector khác nếu không được phép.

---

# 46. Inspection Report Integrity

Khi Inspection đã COMPLETED:

Report không nên bị sửa tùy tiện.

Nếu cần chỉnh:

- có Audit Log;
- hoặc có versioning.

---

# 47. Verified Badge Rule

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

---

# 48. Dispute Reasons

Chỉ dùng:

- NOT_AS_DESCRIBED;
- DAMAGED;
- SUSPECTED_FAKE;
- MISSING_ACCESSORIES;
- WRONG_ITEM;
- NOT_RECEIVED;
- OTHER.

---

# 49. Dispute Status

Chỉ dùng:

- OPEN;
- UNDER_REVIEW;
- WAITING_BUYER;
- WAITING_SELLER;
- RESOLVED;
- REJECTED.

---

# 50. Dispute Creation Rule

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

# 51. Dispute Creation Transaction

Khi Dispute hợp lệ:

1. Create Dispute.
2. MarketplaceOrder → DISPUTED.
3. Escrow → DISPUTED.
4. Create Notification.
5. Create AuditLog.
6. Commit.

Nếu lỗi:

Rollback.

---

# 52. Dispute Resolution Rule

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

# 53. Seller Rating Rule

Chỉ Buyer được rating Seller khi:

`MarketplaceOrder = COMPLETED`

và:

`marketOrder.buyerId == currentUser.id`

Mỗi MarketplaceOrder chỉ có một SellerRating.

Database cần Unique Constraint.

---

# 54. Seller Reputation Rule

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

# 55. Review Product Rule

User chỉ được Review Product khi:

- Order COMPLETED;
- Order thuộc User;
- Product xuất hiện trong OrderItem.

Mỗi OrderItem chỉ được review theo policy đã thống nhất.

---

# 56. Favorite Rule

Một User không được favorite cùng Product nhiều lần.

Database:

`UNIQUE(user_id, product_id)`

---

# 57. Recommendation Input

Recommendation sử dụng:

`PlayerProfile + Product + RacketSpecification`

PlayerProfile gồm:

- skillLevel;
- playStyle;
- playType;
- wristStrength;
- preferredWeight;
- minBudget;
- maxBudget.

---

# 58. Recommendation Weight

**Nguồn: CHG-003 / D03 — APPROVED 2026-10-05.**

Giữ weight Skill 20, Play Style 25, Wrist 15, Preferred Weight 15, Flex 10, Budget 15; tổng 100. MatchScoreCalculator tính deterministic/bounded/testable, score trong [0,100].

Skill order: BEGINNER=0, INTERMEDIATE=1, ADVANCED=2. abs(profileLevel-racketLevel) = 0/1/2 → SkillScore = 20/12/5. Bảng đối xứng: [20,12,5]; [12,20,12]; [5,12,20].

CHG-013: targetFlex chỉ theo skillLevel: BEGINNER → FLEXIBLE, INTERMEDIATE → MEDIUM, ADVANCED → STIFF; không dùng wristStrength. Flex ordinal FLEXIBLE=0, MEDIUM=1, STIFF=2, EXTRA_STIFF=3. abs(target-racket) = 0 →10; =1 →6; >=2 →2. Giữ bảng Wrist–Flex và weights.

effectivePrice = salePrice hợp lệ nếu có, ngược lại product.price.

BudgetScore: effectivePrice < minBudget → 12; minBudget <= effectivePrice <= maxBudget → 15; maxBudget < effectivePrice <= 1.10*maxBudget → 10; 1.10*maxBudget < effectivePrice <= 1.20*maxBudget → 5; effectivePrice > 1.20*maxBudget → 0.

Các scoring rule không mâu thuẫn khác giữ nguyên. Không đặt thuật toán trong Controller; Frontend chỉ trình bày.

---

# 59. Match Score Rule

Match Score:

`0 <= score <= 100`

Backend tính bằng:

`MatchScoreCalculator`

Frontend không tự tính.

---

# 60. Recommendation Output Rule

Recommendation không chỉ trả score.

Cần trả đủ thông tin để Frontend giải thích:

- Product;
- Match Score;
- Reasons;
- yếu tố phù hợp;
- yếu tố cần cân nhắc nếu có.

---

# 61. Recommendation Sorting

Mặc định:

`MatchScore DESC`

Có thể bổ sung filter khác nhưng không làm mất ranking chính.

---

# 62. Recommendation Explainability

MVP sử dụng rule-based vì:

- dễ giải thích;
- dễ test;
- dễ demo;
- phù hợp đồ án.

Không bắt buộc Machine Learning.

---

# 63. Valuation Method

MVP sử dụng:

`RULE_BASED`

Các method hỗ trợ tương lai:

- AI_ASSISTED;
- MANUAL.

---

# 64. Valuation Formula

**Nguồn: CHG-004 / D04 — APPROVED 2026-10-05.**

EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor.

Valuation lưu snapshot: referencePrice, ageFactor, conditionFactor, warrantyFactor, accessoryFactor, repairFactor, demandFactor, conditionScore, valuationMethod, suggestedMinPrice, suggestedMaxPrice, algorithmVersion, createdAt; giữ estimatedPrice đang có.

Schema valuations bổ sung repair_factor DECIMAL(5,3), condition_score DECIMAL(4,2), algorithm_version VARCHAR(50). Các factor dùng DECIMAL(5,3) như schema hiện hành; tiền DECIMAL(15,2), không FLOAT/DOUBLE. Snapshot không bị tính lại theo dữ liệu mới khi đọc lịch sử.

Condition Score 0.00–10.00; chuẩn hóa/làm tròn tới hai chữ số thập phân trước khi map Grade: [9.50,10.00] → S; [8.50,9.50) → A; [7.00,8.50) → B; [5.00,7.00) → C; [0.00,5.00) → D.

Nguồn hợp lệ/còn hiệu lực ưu tiên INSPECTION > AI > SELLER. Dùng currentInspectionReport theo CHG-001/007.

Các bảng factor hiện hành không mâu thuẫn được giữ nguyên; các khoảng dùng interval không chồng lấn.

---

# 65. Valuation Output Rule

Không trả duy nhất một giá quá chính xác.

Nên trả:

- suggestedMin;
- suggestedMax;

hoặc:

- quickSellPrice;
- fairPrice;
- expectedPrice.

---

# 66. Condition Score Rule

**Nguồn: CHG-004 / D04 — APPROVED 2026-10-05.**

EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor.

Valuation lưu snapshot: referencePrice, ageFactor, conditionFactor, warrantyFactor, accessoryFactor, repairFactor, demandFactor, conditionScore, valuationMethod, suggestedMinPrice, suggestedMaxPrice, algorithmVersion, createdAt; giữ estimatedPrice đang có.

Schema valuations bổ sung repair_factor DECIMAL(5,3), condition_score DECIMAL(4,2), algorithm_version VARCHAR(50). Các factor dùng DECIMAL(5,3) như schema hiện hành; tiền DECIMAL(15,2), không FLOAT/DOUBLE. Snapshot không bị tính lại theo dữ liệu mới khi đọc lịch sử.

Condition Score 0.00–10.00; chuẩn hóa/làm tròn tới hai chữ số thập phân trước khi map Grade: [9.50,10.00] → S; [8.50,9.50) → A; [7.00,8.50) → B; [5.00,7.00) → C; [0.00,5.00) → D.

Nguồn hợp lệ/còn hiệu lực ưu tiên INSPECTION > AI > SELLER. Dùng currentInspectionReport theo CHG-001/007.

Các bảng factor hiện hành không mâu thuẫn được giữ nguyên; các khoảng dùng interval không chồng lấn.

---

# 67. AI Rule

AI là tính năng hỗ trợ.

Không mô tả AI như nguồn quyết định duy nhất cho:

- tình trạng sản phẩm;
- giá chính xác;
- kết luận gian lận.

Sử dụng thuật ngữ:

> AI-assisted.

---

# 68. Fraud Wording Rule

Không tuyên bố:

> Hệ thống chống lừa đảo hoàn toàn.

Sử dụng:

> Hệ thống hỗ trợ giảm thiểu rủi ro gian lận thông qua kiểm định, trung gian giao dịch, tranh chấp, đánh giá uy tín và Audit Log.

---

# 69. Escrow Wording Rule

Nếu chưa có hệ thống payment thật:

Không mô tả:

> hệ thống giữ tiền ngân hàng thật.

Mô tả:

> cơ chế trung gian giao dịch mô phỏng / simulated escrow.

---

# 70. Authentication Rule

Sử dụng:

- email;
- password;
- JWT.

Password phải hash bằng cơ chế phù hợp, ví dụ BCrypt.

Không lưu plain password.

---

# 71. JWT Rule

Access Token có thể chứa:

- userId;
- role;
- issuedAt;
- expiration.

Không chứa:

- password;
- passwordHash;
- DB credentials;
- private sensitive data.

---

# 72. Authorization Rule

Backend phải kiểm tra:

1. Authentication.
2. Role.
3. Ownership.
4. Business State.

Không chỉ dựa vào việc Frontend ẩn button.

---

# 73. Ownership Rule tổng quát

Ví dụ:

Shop Order:

`order.userId == currentUser.id`

Listing:

`listing.sellerId == currentUser.id`

Marketplace Buy:

`currentUser.id != listing.sellerId`

Confirm Received:

`marketOrder.buyerId == currentUser.id`

Shipment:

`marketOrder.sellerId == currentUser.id`

---

# 74. Không tin ID nhạy cảm từ Frontend

Frontend không được quyết định:

- current userId;
- buyerId;
- sellerId;
- role.

Backend lấy Current User từ Authentication Context.

---

# 75. Không tin giá từ Frontend

Frontend không được quyết định:

- Product price;
- Order total;
- Marketplace itemPrice;
- Escrow amount;
- final Valuation result.

Backend tính hoặc load từ Database.

---

# 76. Không tin Status từ Frontend

Frontend không được tùy ý gửi:

- orderStatus;
- listingStatus;
- escrowStatus;
- verified;
- disputeStatus.

Status phải được thay đổi thông qua action API và Service rule.

---

# 77. Mass Assignment Protection

Không nhận Entity trực tiếp rồi copy toàn bộ field từ Request.

Request DTO chỉ chứa field User được phép nhập.

Ví dụ `CreateListingRequest` không chứa:

- sellerId;
- status;
- verified;
- condition source chính thức.

---

# 78. Input Validation

Backend validate:

- required;
- string length;
- numeric range;
- enum;
- email;
- quantity;
- price;
- file;
- pagination.

Frontend validation chỉ giúp UX.

Backend vẫn validate lại.

---

# 79. Error Handling Rule

Dùng:

`GlobalExceptionHandler`

Không try/catch lặp lại không cần thiết trong mọi Controller.

Business errors nên có code cụ thể.

---

# 80. Error Response Format

```json id="r9t2d4"
{
  "success": false,
  "code": "LISTING_NOT_AVAILABLE",
  "message": "Sản phẩm không còn khả dụng",
  "errors": null
}
```

Không trả stack trace cho Frontend production.

---

# 81. Success Response Format

```json id="8zzbss"
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

---

# 82. HTTP Status Code Rule

Sử dụng hợp lý:

- 200 OK;
- 201 Created;
- 204 No Content;
- 400 Bad Request;
- 401 Unauthorized;
- 403 Forbidden;
- 404 Not Found;
- 409 Conflict;
- 422 Unprocessable Entity;
- 500 Internal Server Error.

---

# 83. Business Error Codes

Các code chuẩn ban đầu:

- AUTH_INVALID_CREDENTIALS;
- USER_LOCKED;
- USER_NOT_FOUND;
- PRODUCT_NOT_FOUND;
- PRODUCT_OUT_OF_STOCK;
- CART_EMPTY;
- ORDER_INVALID_STATUS;
- PROFILE_REQUIRED;
- LISTING_NOT_AVAILABLE;
- LISTING_OWNER_CANNOT_BUY;
- INSPECTION_ALREADY_EXISTS;
- ESCROW_INVALID_STATUS;
- DISPUTE_NOT_ALLOWED.

Có thể bổ sung nhưng cần naming nhất quán.

---

# 84. Shop Checkout Transaction

Checkout phải xử lý trong transaction:

1. Load Cart.
2. Validate Cart.
3. Lock Product nếu cần.
4. Check Stock.
5. Tính giá.
6. Create Order.
7. Create OrderItem.
8. Decrease Stock.
9. Clear Cart.
10. Commit.

Lỗi bất kỳ:

Rollback.

---

# 85. Stock Concurrency Rule

Không để hai request làm stock âm.

Phải sử dụng:

- Pessimistic lock;
- hoặc atomic update;
- hoặc giải pháp transaction tương đương.

---

# 86. Order Snapshot Rule

OrderItem nên lưu snapshot:

- productName;
- productImage;
- unitPrice.

Lịch sử Order không được thay đổi khi Product đổi giá sau này.

---

# 87. Marketplace Snapshot Rule

MarketplaceOrder nên giữ snapshot cần thiết:

- Listing title;
- selling price;
- condition;
- main image.

Điều này bảo vệ lịch sử giao dịch.

---

# 88. Soft Delete Rule

Không hard delete dữ liệu quan trọng đã có reference.

Product có thể:

`status = INACTIVE`

User có thể:

`status = DELETED`

Không xóa Order, Escrow, Dispute chỉ vì UI không còn cần.

---

# 89. File Upload Rule

Các ảnh chỉ cho phép định dạng đã whitelist.

Ví dụ:

- JPG;
- JPEG;
- PNG;
- WEBP.

Validate:

- file size;
- MIME;
- file count;
- image type.

Không tin filename hoặc extension.

---

# 90. Image Storage Rule

Production:

- lưu file trên Cloudinary/Object Storage;
- Database lưu URL/publicId.

Không lưu ảnh trong source repository.

---

# 91. Pagination Rule

List API dùng:

- page;
- size;
- sort.

Phải có maximum page size.

Không cho Client request hàng chục nghìn record một lần.

---

# 92. Search Rule

Search query phải dùng parameter binding.

Không nối raw SQL với input User.

---

# 93. CORS Rule

Development:

cho phép Frontend local cần thiết.

Production:

chỉ cho phép domain hợp lệ.

Không mở toàn bộ origin nếu không cần.

---

# 94. Secret Management

Không commit:

- DB password;
- JWT secret;
- Cloudinary secret;
- payment key;
- `.env`.

Dùng environment variables.

---

# 95. Logging Rule

Không log:

- password;
- passwordHash;
- full JWT;
- secret key;
- sensitive payment data.

---

# 96. Audit Log Rule

Các action nhạy cảm cần Audit:

- lock User;
- unlock User;
- approve Listing;
- reject Listing;
- complete Inspection;
- release Escrow;
- refund;
- resolve Dispute.

Audit nên lưu:

- actor;
- action;
- entity;
- old data;
- new data;
- timestamp.

---

# 97. Notification Rule

MVP dùng Database Notification.

Không bắt buộc:

- WebSocket;
- push notification;
- mobile notification.

Có thể nâng cấp sau.

---

# 98. Frontend Folder Structure

`src/`

gồm:

- assets;
- components;
- pages;
- services;
- hooks;
- context;
- routes;
- utils;
- constants.

---

# 99. Frontend API Layer

Không gọi Axios trực tiếp lặp lại trong nhiều Component.

Dùng Service/API layer.

Ví dụ:

- authApi.js;
- productApi.js;
- cartApi.js;
- orderApi.js;
- marketplaceApi.js;
- recommendationApi.js.

---

# 100. Frontend State Rule

Global state chỉ dùng cho dữ liệu có phạm vi global.

Ví dụ:

- authenticated user;
- cart count;
- notification count.

Không đưa mọi form state lên global store.

---

# 101. Page State Rule

Mỗi page lấy dữ liệu API phải hỗ trợ:

- Loading;
- Success;
- Empty;
- Error.

Không chỉ xử lý success.

---

# 102. Frontend Security Rule

Frontend Route Guard chỉ phục vụ UX.

Backend Authorization mới là security thật.

Ví dụ `AdminRoute` không thay thế `hasRole('ADMIN')` ở Backend.

---

# 103. Shop và Marketplace UI Rule

Phải phân biệt rõ:

- sản phẩm mới của Shop;
- sản phẩm cũ của Marketplace.

Không để ProductCard và ListingCard gây nhầm lẫn.

Có thể sử dụng badge:

- SẢN PHẨM MỚI;
- ĐỒ CŨ;
- ĐÃ KIỂM ĐỊNH.

---

# 104. Git Rule

Không code toàn bộ trực tiếp trên `main`.

Khuyến nghị:

- main;
- develop;
- feature/*.

Ví dụ:

`feature/auth`

`feature/product`

`feature/recommendation`

---

# 105. Commit Convention

Khuyến nghị:

- feat:
- fix:
- refactor:
- test:
- docs:
- chore:

Ví dụ:

`feat: implement recommendation service`

`fix: prevent duplicate marketplace purchase`

---

# 106. Definition of Done

Một feature chỉ được DONE khi các phần cần thiết đã hoàn thành:

- Database;
- Entity;
- Repository;
- Service;
- Controller;
- DTO;
- Validation;
- Authorization;
- Error Handling;
- Frontend;
- API Integration;
- Main tests.

Không phải feature nào cũng cần tất cả, nhưng không được mark DONE chỉ vì code compile.

---

# 107. Unit Test Priority

Ưu tiên test:

- MatchScoreCalculator;
- ValuationCalculator;
- ConditionScoreCalculator;
- SellerReputationCalculator;
- state transitions.

---

# 108. Integration Test Priority

Ưu tiên:

- Shop Checkout;
- Marketplace Purchase;
- Escrow Release;
- Dispute Creation;
- Dispute Resolution.

---

# 109. Concurrency Test Priority

Bắt buộc test:

## Shop

Stock = 1, hai User checkout đồng thời.

Expected:

- một success;
- một out-of-stock.

## Marketplace

Một Listing AVAILABLE, hai Buyer mua đồng thời.

Expected:

- một success;
- một LISTING_NOT_AVAILABLE.

---

# 110. Security Test Priority

Test tối thiểu:

- no token;
- invalid token;
- expired token;
- wrong role;
- wrong owner;
- tampered price;
- fake verified;
- duplicate purchase;
- invalid file upload.

---

# 111. Critical Bugs trước Release

Không được release khi còn lỗi:

- Listing bán hai lần;
- Escrow release hai lần;
- User xem Order người khác;
- tampered price thành công;
- unauthorized Admin API;
- password leak;
- stock âm;
- Dispute không freeze Escrow.

---

# 112. API Documentation Rule

Các API đã triển khai nên được phản ánh trong Swagger/OpenAPI.

Không để API thực tế khác hoàn toàn tài liệu.

---

# 113. Database Migration Rule

Schema change cần migration.

Không chỉnh Production Database ngẫu nhiên mà không có migration hoặc tài liệu.

Khuyến nghị:

- Flyway.

---

# 114. Development Environment

Phân biệt:

- dev;
- test;
- prod.

Không dùng production credential trong dev.

---

# 115. Production Rule

Production phải có:

- HTTPS;
- restricted CORS;
- environment secret;
- Database access hạn chế;
- logging;
- backup;
- health check cơ bản.

---

# 116. Multi-platform Rule

Backend API phải tránh phụ thuộc trực tiếp vào Web UI.

Business logic nằm ở Backend để:

- Web dùng được;
- Mobile sau này dùng lại được.

Không viết nghiệp vụ quan trọng chỉ ở React.

---

# 117. Recommendation Priority Rule

Vì tên đề tài nhấn mạnh chức năng gợi ý sản phẩm, Recommendation phải được coi là feature cốt lõi.

Khi cắt giảm scope, không nên bỏ:

- PlayerProfile;
- Recommendation;
- Match Score;
- Recommendation Reason.

Nếu cần cắt, ưu tiên cắt các tính năng mở rộng trước như:

- AI;
- real payment;
- chat;
- advanced fraud detection.

---

# 118. Marketplace Scope Rule

Marketplace là module mở rộng nhưng vẫn thuộc phạm vi project hiện tại.

Không xóa Marketplace chỉ vì tên đề tài không nhắc trực tiếp.

Tuy nhiên khi trình bày báo cáo:

Recommendation = chức năng trọng tâm.

Marketplace = chức năng mở rộng.

---

# 119. Không over-engineering

Không thêm:

- Microservices;
- Kafka;
- Kubernetes;
- Redis Cluster;
- Event Sourcing;
- CQRS;

nếu chưa có quyết định kiến trúc APPROVED trong 04_CHANGELOG.md. Nhu cầu thực tế đơn lẻ không thay thế phê duyệt.

MVP ưu tiên code rõ ràng và đúng nghiệp vụ.

---

# 120. Quy tắc dành cho ChatGPT khi hỗ trợ project

Khi mở chat mới, ChatGPT phải:

1. dùng tài liệu Project làm nguồn chuẩn;
2. xác định module đang xử lý;
3. bám đúng Entity;
4. bám đúng Status;
5. bám đúng relationship;
6. không tự ý đổi stack;
7. không gộp Product với UsedListing;
8. không gộp Order với MarketplaceOrder;
9. không để Frontend quyết định business data;
10. không tạo architecture mới chỉ vì tiện code;
11. nếu thấy thiết kế có vấn đề, phải chỉ ra trước;
12. nếu thay đổi được chấp nhận, cập nhật CHANGELOG.

---

# 121. Implementation Order chính thức

Thứ tự:

1. Sprint 0 – Setup.
2. Auth & User.
3. Product Catalog.
4. Cart & Shop Order.
5. Recommendation.
6. Marketplace Listing.
7. Valuation & Condition.
8. Marketplace Order & Escrow.
9. Inspection.
10. Dispute & Seller Rating.
11. Admin & Statistics.
12. System Testing.
13. Deployment.

---

# 122. Quy tắc cập nhật Implementation Status

Sau mỗi module cần cập nhật:

`03_IMPLEMENTATION_STATUS.md`

Ghi:

- status;
- files đã tạo;
- API đã chạy;
- test đã pass;
- bug còn lại;
- next action.

Không để status tài liệu khác source code quá lâu.

---

# 123. Quy tắc cập nhật Changelog

Chỉ ghi vào `04_CHANGELOG.md` khi có thay đổi ảnh hưởng:

- kiến trúc;
- Entity;
- Database;
- API contract;
- business rule;
- state machine;
- technology decision.

Không cần ghi từng bug fix nhỏ.

---

# 124. Nguyên tắc cuối cùng

Project phải luôn giữ nguyên các nguyên tắc nền:

**Frontend** phụ trách trình bày và tương tác.

**Controller** nhận và trả API.

**Service** chịu trách nhiệm nghiệp vụ.

**Repository** chịu trách nhiệm Database.

**DTO** là hợp đồng API.

**Mapper** chuyển dữ liệu.

**Calculator** xử lý thuật toán.

**Backend** là nơi quyết định business data quan trọng.

**Database** bảo vệ integrity và uniqueness.

**Transaction** bảo vệ tính nhất quán.

**Project Documents** bảo vệ tính nhất quán giữa nhiều đoạn chat và nhiều giai đoạn phát triển.
# Approved contracts bổ sung — CHG-001–CHG-014

## D08 / CHG-008

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

## D09 / CHG-009

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

## D11 / CHG-011

**Nguồn: CHG-011 / D11 — APPROVED 2026-10-05.**

MVP chỉ JWT Access Token với expiration hữu hạn. Không Refresh Token, RefreshTokenRequest, persistence/rotation/revocation của Refresh Token trong MVP.

Auth API MVP: POST /api/auth/register; POST /api/auth/login; POST /api/auth/logout; GET /api/users/me; PUT /api/users/me; PUT /api/users/change-password.

Logout: Frontend xóa authentication state/token. Backend không tuyên bố revoke Access Token khi không có server-side token store. Token đã phát hành còn hiệu lực tới expiration.

Khi token hết hạn/401, Frontend yêu cầu đăng nhập lại; không tự gọi refresh endpoint.

Refresh Token là Post-MVP / Future Security Enhancement; cần Change riêng cho storage, expiration, rotation, revocation, logout, frontend storage và security tests. JWT và system roles không đổi.

## D12 / CHG-012

**Nguồn: CHG-012 / D12 — APPROVED 2026-10-05.**

Promotions không thuộc MVP/baseline hiện tại. Loại bỏ màn hình Admin Promotions, route promotions và mô tả quản lý khuyến mãi khỏi chức năng hiện tại.

Không tạo Promotion Entity/table/API/module hoặc discount calculation để hợp thức hóa nội dung legacy.

Product price và Order total tiếp tục do Backend quyết định. Promotions chỉ có thể được xem xét như Future Development qua change-management.

Pending D01–D14 = 0. D13/D14 đã APPROVED và đồng bộ qua CHG-013/014; Pending Architecture Issues = 0. Xem DOCUMENT_VERIFICATION_REPORT.md.

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
