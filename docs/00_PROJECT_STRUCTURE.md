# 00_PROJECT_STRUCTURE.md

# XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ CẦU LÔNG ĐA NỀN TẢNG TÍCH HỢP CHỨC NĂNG GỢI Ý SẢN PHẨM THEO NHU CẦU NGƯỜI DÙNG

---

## 1. Mục đích của tài liệu

Tài liệu `00_PROJECT_STRUCTURE.md` đóng vai trò là **bản đồ tổng thể và điểm bắt đầu của toàn bộ project**.

Khi một đoạn chat mới được tạo trong ChatGPT Project, tài liệu này cần giúp hệ thống nhanh chóng xác định được:

- tên chính thức của đề tài;
- mục tiêu của hệ thống;
- phạm vi chức năng;
- chức năng nào là trọng tâm của đề tài;
- chức năng nào là chức năng mở rộng;
- kiến trúc tổng thể;
- công nghệ sử dụng;
- các actor;
- các module;
- các entity chính;
- trạng thái nghiệp vụ quan trọng;
- tài liệu nào là nguồn tham chiếu;
- thứ tự ưu tiên khi các tài liệu có sự khác nhau;
- project hiện đang ở giai đoạn nào;
- đoạn chat nào phụ trách module nào.

Tài liệu này **không thay thế** tài liệu phân tích và thiết kế chi tiết.

Nó là:

> **Project Index + Architecture Map + Source Map + Project Orientation Document**

---

# 2. Tên đề tài chính thức

Tên đề tài được sử dụng thống nhất trong báo cáo, tài liệu và toàn bộ project:

> **Xây dựng hệ thống thương mại điện tử cầu lông đa nền tảng tích hợp chức năng gợi ý sản phẩm theo nhu cầu người dùng**

Không tự ý thay đổi tên đề tài thành tên có chứa Marketplace, Escrow, định giá hoặc kiểm định.

Các chức năng này vẫn tồn tại trong hệ thống nhưng được xem là **các chức năng mở rộng của nền tảng thương mại điện tử**.

---

# 3. Định hướng của đề tài

Đề tài xây dựng một nền tảng thương mại điện tử chuyên biệt cho lĩnh vực cầu lông.

Hệ thống không chỉ cung cấp các chức năng thương mại điện tử cơ bản như:

- xem sản phẩm;
- tìm kiếm;
- lọc;
- giỏ hàng;
- đặt hàng;
- quản lý đơn hàng;
- đánh giá sản phẩm;

mà còn tập trung vào chức năng đặc trưng:

> **Gợi ý sản phẩm cầu lông phù hợp với nhu cầu và đặc điểm của từng người dùng.**

Ngoài ra, hệ thống được mở rộng thêm Marketplace dành cho các sản phẩm cầu lông đã qua sử dụng, kết hợp với các cơ chế hỗ trợ như:

- định giá sản phẩm cũ;
- đánh giá tình trạng;
- kiểm định sản phẩm;
- trung gian giao dịch mô phỏng;
- quản lý vận chuyển;
- xử lý tranh chấp;
- đánh giá và uy tín người bán.

---

# 4. Chức năng trọng tâm của đề tài

Chức năng trọng tâm cần luôn được ưu tiên trong báo cáo và khi demo là:

## 4.1 Personalized Product Recommendation

Hệ thống thu thập đặc điểm và nhu cầu của người chơi thông qua:

`PlayerProfile`

Các thông tin chính gồm:

- trình độ;
- phong cách chơi;
- loại hình chơi;
- lực cổ tay;
- trọng lượng vợt mong muốn;
- ngân sách.

Ví dụ:

- Skill Level: Beginner / Intermediate / Advanced.
- Play Style: Attack / Defense / Balanced / Speed / Control.
- Play Type: Singles / Doubles / Both.
- Wrist Strength: Weak / Medium / Strong.
- Preferred Weight: 3U / 4U / 5U.
- Budget: khoảng giá mong muốn.

---

## 4.2 Recommendation Flow

Luồng cơ bản:

`PlayerProfile → RecommendationService → Product + RacketSpecification → MatchScoreCalculator → Recommendation Result`

Kết quả trả về:

- danh sách sản phẩm phù hợp;
- Match Score từ 0–100;
- lý do sản phẩm phù hợp;
- các điểm cần cân nhắc.

Ví dụ:

> Yonex Astrox 88D Pro – 92% phù hợp với bạn.

Lý do:

- phù hợp với lối chơi tấn công;
- trọng lượng 4U đúng sở thích;
- mức giá nằm trong ngân sách;
- độ cứng thân vợt tương đối phù hợp lực cổ tay.

---

# 5. Vai trò của các chức năng mở rộng

Ngoài chức năng gợi ý sản phẩm, project vẫn triển khai các module mở rộng đã được thiết kế.

Các chức năng mở rộng không làm thay đổi tên đề tài.

Chúng có mục tiêu làm hệ thống gần hơn với một nền tảng thương mại điện tử cầu lông hoàn chỉnh.

Các module mở rộng gồm:

1. Marketplace đồ cầu lông đã qua sử dụng.
2. Định giá sản phẩm cũ.
3. Condition Score.
4. Kiểm định sản phẩm.
5. Verified Badge.
6. Marketplace Order.
7. Escrow mô phỏng.
8. Shipment.
9. Dispute.
10. Seller Rating.
11. Seller Reputation.
12. Notification.
13. Audit Log.
14. Statistics.

---

# 6. Phạm vi đa nền tảng

Tên đề tài có thành phần:

> **đa nền tảng**

Kiến trúc hệ thống cần hỗ trợ khả năng sử dụng chung Backend cho nhiều loại client.

Kiến trúc định hướng:

`Web Client → REST API ← Mobile Client`

Backend:

`Spring Boot → MySQL`

Trong phạm vi triển khai hiện tại:

### Web

Được ưu tiên phát triển hoàn chỉnh.

Bao gồm:

- Customer Web;
- Seller Marketplace Web;
- Inspector Web;
- Admin Web.

### Mobile

Không bắt buộc phải triển khai đầy đủ trong phiên bản đầu nếu thời gian không cho phép.

Tuy nhiên Backend phải được thiết kế theo REST API để có khả năng dùng lại cho Mobile sau này.

Không nên mô tả rằng Mobile đã được triển khai nếu thực tế chưa có source code Mobile.

Cách mô tả phù hợp:

> Hệ thống được thiết kế theo kiến trúc Backend REST API dùng chung, cho phép Web và Mobile cùng sử dụng các nghiệp vụ của hệ thống. Trong phạm vi hiện tại, nền tảng Web được ưu tiên triển khai đầy đủ, trong khi Mobile có thể tiếp tục phát triển dựa trên cùng hệ thống API.

---

# 7. Mô hình nghiệp vụ tổng thể

Toàn bộ hệ thống có thể chia thành bốn nhóm nghiệp vụ lớn.

## 7.1 Shop B2C

Khách hàng mua sản phẩm cầu lông mới từ hệ thống.

Luồng chính:

`Product → Cart → Checkout → Order → Shipping → Completed`

---

## 7.2 Personalized Recommendation

Người dùng cung cấp nhu cầu chơi cầu lông.

Luồng:

`PlayerProfile → MatchScore → Recommended Products`

---

## 7.3 Marketplace C2C

Người dùng có thể bán hoặc mua sản phẩm cầu lông đã qua sử dụng.

Luồng phía Seller:

`Create Listing → Valuation → Submit → Admin Review → AVAILABLE`

Luồng phía Buyer:

`Marketplace → Listing Detail → Purchase → Escrow → Shipment → Confirm`

---

## 7.4 Transaction Protection

Đối với giao dịch Marketplace:

`Payment → Escrow HELD → Shipment → Buyer Confirmation → RELEASED`

Nếu có vấn đề:

`Dispute → Escrow DISPUTED → Admin Resolution → Refund / Release`

Trong phạm vi đồ án, Escrow được xem là:

> **cơ chế trung gian giao dịch mô phỏng**

Không mô tả đây là hệ thống giữ tiền ngân hàng thật nếu chưa tích hợp payment infrastructure thực tế.

---

# 8. Kiến trúc kỹ thuật tổng thể

Kiến trúc hệ thống:

`Client → REST API → Backend → Database`

Chi tiết:

`React Frontend → Spring Boot REST API → Service Layer → Repository Layer → MySQL`

Các dịch vụ bên ngoài có thể gồm:

- Cloudinary hoặc Object Storage;
- Email Service;
- Payment Gateway trong tương lai;
- AI Service trong tương lai.

---

# 9. Kiến trúc Backend

Backend sử dụng mô hình:

> **Modular Monolith**

Không sử dụng Microservices trong phiên bản hiện tại.

Lý do:

- quy mô đồ án chưa cần microservices;
- dễ phát triển;
- dễ debug;
- dễ deploy;
- vẫn có thể chia module rõ ràng;
- có khả năng tách service trong tương lai nếu cần.

Luồng xử lý chuẩn:

`Controller → DTO → Service → Repository → Entity → Database`

Response:

`Entity → Mapper → Response DTO → Controller → Frontend`

---

# 10. Technology Stack chính

## Frontend

- React.
- React Router.
- Axios.
- React Hook Form.
- Context API hoặc Zustand cho state phù hợp.
- Tailwind CSS, Material UI hoặc Ant Design có thể lựa chọn ở giai đoạn triển khai.

## Backend

- Java.
- Spring Boot.
- Spring Security.
- Spring Data JPA.
- Hibernate.
- JWT.

## Database

- MySQL.

## Image Storage

Ưu tiên:

- Cloudinary;

hoặc object storage tương đương.

## API

- REST.
- JSON.

## API Testing

- Postman.

## Version Control

- Git.
- GitHub hoặc repository tương đương.

## Documentation

- Swagger / OpenAPI.

---

# 11. Các actor của hệ thống

Hệ thống có bốn actor chính.

## 11.1 Guest

Khách chưa đăng nhập.

Có thể:

- xem trang chủ;
- xem danh sách sản phẩm;
- tìm kiếm;
- lọc;
- xem chi tiết sản phẩm;
- xem Marketplace;
- xem chi tiết Listing;
- đăng ký;
- đăng nhập.

---

## 11.2 User

Người dùng đã đăng nhập.

USER có thể đóng nhiều vai trò nghiệp vụ.

### Customer

Có thể:

- quản lý hồ sơ;
- quản lý địa chỉ;
- mua hàng Shop;
- sử dụng Recommendation;
- xem lịch sử đơn hàng;
- review;
- favorite.

### Buyer

Có thể:

- mua sản phẩm Marketplace;
- theo dõi Marketplace Order;
- xác nhận nhận hàng;
- tạo dispute;
- đánh giá Seller.

### Seller

Có thể:

- tạo UsedListing;
- upload ảnh;
- định giá;
- yêu cầu Inspection;
- theo dõi Listing;
- xử lý Marketplace Order;
- tạo Shipment.

Buyer và Seller **không phải System Role riêng**.

Cả hai đều sử dụng:

`USER`

---

## 11.3 Inspector

Có quyền:

- xem danh sách yêu cầu kiểm định;
- nhận yêu cầu;
- bắt đầu kiểm định;
- nhập InspectionReport;
- hoàn thành kiểm định.

System Role:

`INSPECTOR`

---

## 11.4 Admin

Có quyền quản trị toàn hệ thống:

- sản phẩm;
- danh mục;
- thương hiệu;
- đơn hàng Shop;
- User;
- Marketplace Listing;
- Inspection;
- Escrow;
- Dispute;
- Review;
- Recommendation configuration;
- statistics;
- audit.

System Role:

`ADMIN`

---

# 12. Account Role chính thức

Hệ thống sử dụng đúng ba Role:

- `USER`
- `INSPECTOR`
- `ADMIN`

Không tạo System Role:

- BUYER;
- SELLER.

Buyer và Seller chỉ là vai trò nghiệp vụ của User.

---

# 13. Các module chính

Backend dự kiến có các module sau.

## 13.1 Authentication

Phụ trách:

- register;
- login;
- JWT;
- JWT Access Token với expiration hữu hạn; Refresh Token thuộc Post-MVP theo CHG-011;
- logout;
- password.

---

## 13.2 User

Phụ trách:

- user profile;
- address;
- account information;
- player profile liên quan có thể tách riêng.

---

## 13.3 Product Catalog

Bao gồm:

- Category;
- Brand;
- Product;
- ProductImage;
- RacketSpecification.

---

## 13.4 Cart

Bao gồm:

- Cart;
- CartItem.

---

## 13.5 Shop Order

Bao gồm:

- Order;
- OrderItem;
- checkout;
- stock control.

---

## 13.6 Review

Quản lý review sản phẩm Shop.

---

## 13.7 Favorite

Quản lý sản phẩm yêu thích.

---

## 13.8 Recommendation

Bao gồm:

- PlayerProfile;
- RecommendationWeight;
- RecommendationService;
- MatchScoreCalculator;
- RecommendationLog.

---

## 13.9 Marketplace Listing

Bao gồm:

- UsedListing;
- UsedListingImage;
- UsedRacketSpecification.

---

## 13.10 Valuation

Bao gồm:

- Valuation;
- ValuationService;
- ValuationCalculator;
- Condition Score.

---

## 13.11 Inspection

Bao gồm:

- InspectionRequest;
- InspectionReport.

---

## 13.12 Marketplace Order

Bao gồm:

- MarketplaceOrder;
- purchase;
- reservation;
- buyer/seller order management.

---

## 13.13 Escrow

Bao gồm:

- EscrowTransaction;
- payment simulation;
- hold;
- release;
- refund;
- dispute freeze.

---

## 13.14 Shipment

Bao gồm:

- Shipment;
- carrier;
- tracking;
- status.

---

## 13.15 Dispute

Bao gồm:

- Dispute;
- DisputeEvidence;
- admin resolution.

---

## 13.16 Seller Rating

Bao gồm:

- SellerRating;
- SellerReputation.

---

## 13.17 Notification

Bao gồm:

- Notification;
- unread/read state;
- reference to business entities.

---

## 13.18 Statistics

Bao gồm:

- overview;
- revenue;
- top product;
- marketplace statistic;
- seller statistics.

---

## 13.19 Audit

Bao gồm:

- AuditLog;
- business-sensitive actions.

---

# 14. Nhóm Entity – User

Các Entity:

- User.
- Address.
- PlayerProfile.

### User

Thông tin chính:

- userId;
- fullName;
- email;
- passwordHash;
- phone;
- avatarUrl;
- role;
- status;
- createdAt;
- updatedAt.

### Address

Thông tin:

- addressId;
- userId;
- receiverName;
- receiverPhone;
- province;
- district;
- ward;
- detailAddress;
- isDefault.

### PlayerProfile

Thông tin:

- profileId;
- userId;
- skillLevel;
- playStyle;
- playType;
- wristStrength;
- preferredWeight;
- minBudget;
- maxBudget.

---

# 15. Nhóm Entity – Shop

Các Entity:

- Category.
- Brand.
- Product.
- ProductImage.
- RacketSpecification.
- Cart.
- CartItem.
- Order.
- OrderItem.
- Review.
- Favorite.

---

# 16. Product và RacketSpecification

Không gộp toàn bộ thông số vợt vào Product.

`Product` chứa thông tin thương mại chung.

Ví dụ:

- productName;
- price;
- stock;
- description;
- brand;
- category.

`RacketSpecification` chứa thông số chuyên biệt cho vợt.

Ví dụ:

- weightClass;
- balanceType;
- balancePoint;
- shaftFlexibility;
- maxTensionLbs;
- playStyle;
- skillLevel;
- gripSize;
- material.

Quan hệ:

`Product 1 : 0..1 RacketSpecification`

---

# 17. Shop Order

Shop Order sử dụng:

- Order.
- OrderItem.

Không gộp Shop Order với MarketplaceOrder.

Hai loại đơn hàng có business flow khác nhau.

---

# 18. Nhóm Entity – Recommendation

Các Entity:

- RecommendationWeight.
- RecommendationLog.

Các thành phần logic:

- RecommendationService.
- MatchScoreCalculator.

---

# 19. Recommendation Weight mặc định

Bộ trọng số ban đầu:

| Tiêu chí | Trọng số |
|---|---:|
| Skill Level | 20% |
| Play Style | 25% |
| Wrist Strength | 15% |
| Preferred Weight | 15% |
| Shaft Flexibility | 10% |
| Budget | 15% |
| Tổng | 100% |

Các giá trị có thể được cấu hình về sau; chỉ thay đổi khi có quyết định APPROVED trong 04_CHANGELOG.md và đã cập nhật tài liệu thiết kế liên quan.

---

# 20. Nhóm Entity – Marketplace

Các Entity:

- UsedListing.
- UsedListingImage.
- UsedRacketSpecification.
- Valuation.
- InspectionRequest.
- InspectionReport.
- SellerRating.

Không gộp:

`Product`

với:

`UsedListing`

vì Product thuộc Shop B2C, còn UsedListing thuộc Marketplace C2C.

---

# 21. UsedListing

Thông tin chính:

- listingId;
- sellerId;
- categoryId;
- brandId;
- listingCode;
- productName;
- model;
- description;
- originalPrice;
- sellingPrice;
- purchaseDate;
- usageMonths;
- conditionGrade;
- conditionScore;
- warrantyStatus;
- warrantyUntil;
- status;
- viewCount;
- createdAt;
- updatedAt.

---

# 22. UsedRacketSpecification

Đối với vợt cũ có thêm:

- weightClass;
- gripSize;
- stringModel;
- stringTensionLbs;
- frameCondition;
- paintCondition;
- grommetCondition;
- handleCondition;
- repairHistory;
- serialNumber.

---

# 23. Marketplace Listing Image Types

Các loại ảnh có thể gồm:

- GENERAL;
- FRONT;
- BACK;
- FRAME_TOP;
- FRAME_LEFT;
- FRAME_RIGHT;
- HANDLE;
- SERIAL;
- DAMAGE.

Không bắt buộc mọi Listing phải có tất cả loại ảnh, nhưng các yêu cầu tối thiểu sẽ được quy định trong business rule.

---

# 24. Listing Status

Các trạng thái chính thức:

- `DRAFT`
- `PENDING_REVIEW`
- `AVAILABLE`
- `RESERVED`
- `SOLD`
- `REJECTED`
- `HIDDEN`

Flow cơ bản:

`DRAFT → PENDING_REVIEW → AVAILABLE → RESERVED → SOLD`

Nhánh từ Admin Review:

`PENDING_REVIEW → REJECTED`

Listing khả dụng có thể:

`AVAILABLE → HIDDEN`

Seller không được tự gán Listing thành AVAILABLE.

---

# 25. Valuation

Định giá sử dụng phương pháp rule-based trong MVP.

Công thức định hướng:

`EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor`

Kết quả nên trả:

- Suggested Min;
- Suggested Max;

hoặc:

- Quick Sell;
- Fair Price;
- Expected Price.

Không trả một giá tuyệt đối tạo cảm giác chính xác giả.

---

# 26. Valuation Method

Các giá trị có thể có:

- `RULE_BASED`
- `AI_ASSISTED`
- `MANUAL`

MVP ưu tiên:

`RULE_BASED`

AI chỉ là tính năng mở rộng.

---

# 27. Condition Score

**Nguồn: CHG-004 / D04 — APPROVED 2026-10-05.**

EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor.

Valuation lưu snapshot: referencePrice, ageFactor, conditionFactor, warrantyFactor, accessoryFactor, repairFactor, demandFactor, conditionScore, valuationMethod, suggestedMinPrice, suggestedMaxPrice, algorithmVersion, createdAt; giữ estimatedPrice đang có.

Schema valuations bổ sung repair_factor DECIMAL(5,3), condition_score DECIMAL(4,2), algorithm_version VARCHAR(50). Các factor dùng DECIMAL(5,3) như schema hiện hành; tiền DECIMAL(15,2), không FLOAT/DOUBLE. Snapshot không bị tính lại theo dữ liệu mới khi đọc lịch sử.

Condition Score 0.00–10.00; chuẩn hóa/làm tròn tới hai chữ số thập phân trước khi map Grade: [9.50,10.00] → S; [8.50,9.50) → A; [7.00,8.50) → B; [5.00,7.00) → C; [0.00,5.00) → D.

Nguồn hợp lệ/còn hiệu lực ưu tiên INSPECTION > AI > SELLER. Dùng currentInspectionReport theo CHG-001/007.

Các bảng factor hiện hành không mâu thuẫn được giữ nguyên; các khoảng dùng interval không chồng lấn.

---

# 28. Inspection

**Nguồn: CHG-001 / D01 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N InspectionRequest; InspectionRequest 1 → 0..1 InspectionReport.

Một Listing có nhiều request trong lịch sử nhưng tối đa một request active: REQUESTED, ACCEPTED, INSPECTING. COMPLETED, REJECTED, CANCELLED không active.

Seller chỉ tạo request mới cho Listing của mình khi không có request active; Backend kiểm tra đồng thời trong transaction để ngăn request song song.

Mỗi request COMPLETED phải có report. Không xóa lịch sử kiểm định.

currentInspectionReport là report của request COMPLETED mới nhất còn hiệu lực. Verified Badge và Valuation dùng report còn hiệu lực này; hiệu lực sau sửa Listing theo CHG-007.

inspection_requests.listing_id dùng FK/index thường, không có unique trên listing_id.

---

# 29. Verified Badge

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

---

# 30. Nhóm Entity – Marketplace Transaction

Các Entity:

- MarketplaceOrder.
- EscrowTransaction.
- Shipment.
- Dispute.
- DisputeEvidence.

---

# 31. MarketplaceOrder

Không gộp với Shop Order.

Thông tin chính:

- marketOrderId;
- orderCode;
- listingId;
- buyerId;
- sellerId;
- itemPrice;
- shippingFee;
- platformFee;
- totalAmount;
- status;
- receiver information;
- createdAt;
- updatedAt.

`listingId` cần được bảo vệ để một Listing không thể bán cho nhiều Buyer.

---

# 32. Marketplace Order Status

Các trạng thái:

- `PENDING_PAYMENT`
- `PAID`
- `WAITING_SHIPMENT`
- `SHIPPING`
- `DELIVERED`
- `COMPLETED`
- `CANCELLED`
- `DISPUTED`
- `REFUNDED`

---

# 33. Marketplace Purchase Rule

Buyer chỉ được mua nếu:

- User đã đăng nhập;
- User ACTIVE;
- Listing AVAILABLE;
- Buyer không phải Seller.

Khi mua:

1. Lock Listing.
2. Kiểm tra AVAILABLE.
3. Kiểm tra Buyer != Seller.
4. Tạo MarketplaceOrder.
5. Chuyển Listing sang RESERVED.
6. Tạo EscrowTransaction.
7. Commit.

Toàn bộ nằm trong một Database Transaction.

---

# 34. Escrow

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

---

# 35. Escrow Transition

**Nguồn: CHG-002 / D02 — APPROVED 2026-10-05.**

UsedListing 1 → 0..N MarketplaceOrder, giữ lịch sử các lần đặt mua đã CANCELLED. marketplace_orders.listing_id là FK/index thường, không unique.

Tối đa một MarketplaceOrder active cho Listing. Mua trong một transaction: lock UsedListing → check AVAILABLE → check Buyer != Seller → tạo MarketplaceOrder → Listing RESERVED → tạo EscrowTransaction → commit. Backend quyết định buyerId, sellerId, price và total.

Buyer chỉ normal-cancel order của mình ở PENDING_PAYMENT. Timeout chỉ áp dụng ở PENDING_PAYMENT.

Cancel/timeout atomically: MarketplaceOrder PENDING_PAYMENT → CANCELLED; Listing RESERVED → AVAILABLE; Escrow WAITING_PAYMENT → CANCELLED. Giữ toàn bộ lịch sử Order/Escrow; có thể mua lại bằng order mới.

EscrowStatus: WAITING_PAYMENT, HELD, RELEASED, DISPUTED, REFUNDED, CANCELLED.

Escrow transitions: WAITING_PAYMENT → HELD; WAITING_PAYMENT → CANCELLED; HELD → RELEASED khi confirm-received hợp lệ; HELD → DISPUTED; DISPUTED → RELEASED hoặc REFUNDED theo Admin resolution.

Sau HELD không normal-cancel; vấn đề sau thanh toán đi qua Dispute/Admin. Không release/refund hai lần.

---

# 36. Shipment

Entity:

`Shipment`

Thông tin chính:

- shipmentId;
- marketOrderId;
- carrier;
- trackingCode;
- status;
- shippedAt;
- deliveredAt.

Status:

- WAITING_PICKUP;
- PICKED_UP;
- IN_TRANSIT;
- DELIVERED;
- FAILED;
- RETURNED.

---

# 37. Dispute

Entity:

- Dispute.
- DisputeEvidence.

Các lý do:

- NOT_AS_DESCRIBED;
- DAMAGED;
- SUSPECTED_FAKE;
- MISSING_ACCESSORIES;
- WRONG_ITEM;
- NOT_RECEIVED;
- OTHER.

---

# 38. Dispute Status

Các trạng thái:

- OPEN;
- UNDER_REVIEW;
- WAITING_BUYER;
- WAITING_SELLER;
- RESOLVED;
- REJECTED.

Khi Dispute hợp lệ được tạo:

- Dispute được tạo;
- MarketplaceOrder → DISPUTED;
- Escrow → DISPUTED;
- Notification được tạo;
- Audit Log được tạo.

---

# 39. Dispute Resolution

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

# 40. Seller Rating

Buyer chỉ được rating Seller khi:

`MarketplaceOrder = COMPLETED`

Mỗi MarketplaceOrder chỉ có tối đa:

`1 SellerRating`

Thông tin:

- rating;
- comment;
- buyer;
- seller;
- marketOrder.

---

# 41. Seller Reputation

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

# 42. Notification

MVP sử dụng notification lưu Database.

Ví dụ sự kiện:

- Listing approved;
- Listing rejected;
- Order confirmed;
- Marketplace payment received;
- Seller shipped item;
- Inspection completed;
- Dispute created;
- Dispute resolved;
- Escrow released.

Realtime WebSocket không bắt buộc ở MVP.

---

# 43. Audit Log

Các hành động quan trọng cần Audit:

- Admin lock User;
- Admin unlock User;
- approve Listing;
- reject Listing;
- complete Inspection;
- release Escrow;
- refund Escrow;
- create Dispute;
- resolve Dispute.

Audit Log cần lưu:

- actor;
- action;
- entityType;
- entityId;
- oldData;
- newData;
- timestamp;
- IP nếu có.

---

# 44. Security Principles

Backend không tin dữ liệu từ Frontend.

Frontend không được quyết định:

- userId;
- buyerId;
- sellerId;
- role;
- Product price;
- Shop Order total;
- Marketplace price;
- Match Score;
- Valuation result;
- Listing status;
- Order status;
- Escrow status;
- Verified status;
- Seller Reputation.

Backend phải tính hoặc lấy từ Database.

---

# 45. Ownership Rules

Ví dụ:

### Shop Order

`order.userId == currentUser.id`

hoặc ADMIN.

### Listing

`listing.sellerId == currentUser.id`

khi Seller edit.

### Marketplace Confirm Received

`marketOrder.buyerId == currentUser.id`

### Shipment

`marketOrder.sellerId == currentUser.id`

### Dispute

Current User phải là Buyer hoặc Seller của MarketplaceOrder liên quan.

---

# 46. Password Security

Không lưu plain password.

Sử dụng password hashing phù hợp, ví dụ BCrypt.

Database chỉ lưu:

`passwordHash`

Không trả passwordHash ra API.

---

# 47. JWT

Authentication sử dụng JWT.

Access Token có thể chứa:

- userId;
- role;
- issuedAt;
- expiration.

Không chứa:

- password;
- DB credentials;
- sensitive private data.

---

# 48. Database Rules

Money:

`DECIMAL(15,2)`

Không dùng:

- FLOAT;
- DOUBLE;

cho giá tiền.

Dữ liệu nghiệp vụ quan trọng không nên hard delete.

Ví dụ:

- User;
- Product;
- Order;
- MarketplaceOrder;
- Escrow;
- Dispute;
- Inspection.

---

# 49. Transaction Rules

Các nghiệp vụ bắt buộc cần Transaction:

- Shop checkout;
- Marketplace purchase;
- confirm received;
- Escrow release;
- Dispute creation;
- Dispute resolution.

Nếu một bước lỗi:

`ROLLBACK`

---

# 50. API Base

Base:

`/api`

Có thể chuyển sang:

`/api/v1`

nếu quyết định áp dụng API Versioning.

Không tự ý thay giữa quá trình triển khai nếu chưa cập nhật tài liệu.

---

# 51. API Response Format

Success:

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

Error:

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "Error message",
  "errors": null
}
```

---

# 52. HTTP Status Code

Sử dụng:

- 200 – OK;
- 201 – Created;
- 204 – No Content;
- 400 – Bad Request;
- 401 – Unauthorized;
- 403 – Forbidden;
- 404 – Not Found;
- 409 – Conflict;
- 422 – Unprocessable Entity;
- 500 – Internal Server Error.

---

# 53. Một số Business Error Code

- AUTH_INVALID_CREDENTIALS
- USER_LOCKED
- PRODUCT_NOT_FOUND
- PRODUCT_OUT_OF_STOCK
- CART_EMPTY
- ORDER_INVALID_STATUS
- PROFILE_REQUIRED
- LISTING_NOT_AVAILABLE
- LISTING_OWNER_CANNOT_BUY
- INSPECTION_ALREADY_EXISTS
- ESCROW_INVALID_STATUS
- DISPUTE_NOT_ALLOWED

---

# 54. File Upload

Các endpoint upload ảnh chỉ cho phép các định dạng phù hợp.

Ví dụ:

- JPG;
- JPEG;
- PNG;
- WEBP.

Backend cần kiểm tra:

- file size;
- MIME type;
- file count;
- file name;
- storage location.

Không tin extension của file.

---

# 55. Source Code Backend Structure

Dự kiến:

`src/main/java/com/badminton/`

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

# 56. Source Code Frontend Structure

`src/`

Bao gồm:

- assets;
- components;
- pages;
- services;
- hooks;
- context;
- routes;
- utils;
- constants.

Pages chia theo:

- auth;
- product;
- customer;
- recommendation;
- marketplace;
- inspector;
- admin.

---

# 57. Các tài liệu nguồn chính của Project

Project sử dụng bộ tài liệu sau.

## 57.1 00_PROJECT_STRUCTURE.md

Tài liệu hiện tại.

Vai trò:

- định hướng project;
- bản đồ kiến trúc;
- danh sách module;
- nguồn tham chiếu.

---

## 57.2 01_SYSTEM_ANALYSIS_MASTER.docx và 01_SYSTEM_ANALYSIS_MASTER.md

Tài liệu phân tích và thiết kế chi tiết.

Gồm 12 phần:

1. Phân tích tổng thể.
2. Use Case.
3. ERD và Database.
4. REST API.
5. Frontend.
6. Activity + Sequence.
7. Backend Architecture + Class Diagram.
8. Source Structure.
9. Algorithm + Business Rules.
10. Security.
11. Testing + Sprint.
12. Deployment + CI/CD.

Đây là tài liệu thiết kế đầy đủ nhất.

---

## 57.3 02_IMPLEMENTATION_RULES.md

Chứa các quy tắc bắt buộc khi code:

- naming;
- layer;
- ownership;
- transaction;
- status;
- security;
- validation;
- DTO;
- API;
- Database.

---

## 57.4 03_IMPLEMENTATION_STATUS.md

Theo dõi source code thực tế đã làm đến đâu.

Ví dụ:

- NOT_STARTED;
- IN_PROGRESS;
- BACKEND_DONE;
- INTEGRATION;
- TESTING;
- DONE;
- BLOCKED.

---

## 57.5 04_CHANGELOG.md

Ghi lại các thay đổi kiến trúc và nghiệp vụ.

Không sử dụng Changelog để ghi từng commit nhỏ.

Chỉ ghi các thay đổi ảnh hưởng thiết kế.

---

# 58. Thứ tự ưu tiên tài liệu

Nếu có khác biệt:

1. Architecture Decision mới nhất có trạng thái APPROVED trong CHANGELOG; các ví dụ PROPOSED không ghi đè baseline hiện hành.
2. IMPLEMENTATION_RULES.
3. SYSTEM_ANALYSIS_MASTER.
4. PROJECT_STRUCTURE.

Khi source code đã tồn tại:

Source code cho biết hệ thống **đang được triển khai thế nào**, nhưng không tự động có nghĩa source code đúng thiết kế.

Nếu code khác tài liệu:

> Phải xác định xem code sai hay tài liệu đã lỗi thời, sau đó đồng bộ.

---

# 59. Cấu trúc các đoạn chat trong ChatGPT Project

Các chat chính:

### 00 – MASTER ARCHITECTURE

Dùng để:

- quyết định thay đổi kiến trúc;
- thay Entity;
- thay relationship;
- thay API lớn;
- thêm module mới;
- phân tích ảnh hưởng.

Không dùng để code hằng ngày.

---

### 01 – SYSTEM ANALYSIS & DESIGN

Dùng cho:

- Use Case;
- ERD;
- Sequence;
- Activity;
- Class Diagram;
- tài liệu báo cáo.

---

### 02 – SPRINT 0 – PROJECT SETUP

Dùng cho:

- tạo project;
- React;
- Spring Boot;
- MySQL;
- config;
- environment;
- Swagger;
- CORS.

---

### 03 – AUTH & USER

Dùng cho:

- register;
- login;
- JWT;
- user;
- address;
- profile;
- authorization.

---

### 04 – PRODUCT CATALOG

Dùng cho:

- Category;
- Brand;
- Product;
- ProductImage;
- RacketSpecification;
- search;
- filter.

---

### 05 – CART & SHOP ORDER

Dùng cho:

- Cart;
- CartItem;
- Checkout;
- Order;
- OrderItem;
- Review;
- Favorite.

---

### 06 – RECOMMENDATION ENGINE

Dùng cho:

- PlayerProfile;
- MatchScore;
- RecommendationService;
- MatchScoreCalculator.

---

### 07 – MARKETPLACE LISTING

Dùng cho:

- UsedListing;
- UsedListingImage;
- UsedRacketSpecification;
- Seller Listing;
- Admin Approval.

---

### 08 – VALUATION & CONDITION

Dùng cho:

- Valuation;
- ValuationCalculator;
- ConditionScore;
- PriceRange.

---

### 09 – MARKETPLACE ORDER & ESCROW

Dùng cho:

- MarketplaceOrder;
- Escrow;
- payment simulation;
- concurrent purchase;
- Shipment;
- confirm received.

---

### 10 – INSPECTION

Dùng cho:

- InspectionRequest;
- InspectionReport;
- Inspector;
- Verified Badge.

---

### 11 – DISPUTE & SELLER RATING

Dùng cho:

- Dispute;
- Evidence;
- refund;
- SellerRating;
- SellerReputation.

---

### 12 – ADMIN & STATISTICS

Dùng cho:

- Admin Dashboard;
- Statistics;
- User management;
- Audit UI.

---

### 13 – TESTING & BUG FIXING

Dùng cho:

- Unit Test;
- Integration Test;
- API Test;
- Security Test;
- Concurrency;
- E2E.

---

### 14 – DEPLOYMENT

Dùng cho:

- build;
- server;
- Nginx;
- Docker;
- MySQL production;
- Domain;
- HTTPS;
- CI/CD;
- monitoring.

---

### 15 – FUTURE FEATURES

Dùng cho:

- Mobile;
- AI Condition Analysis;
- AI Valuation;
- Real Payment;
- Real Shipping;
- Buyer–Seller Chat;
- Advanced Fraud Detection.

---

# 60. Quy tắc khi mở chat mới

Mỗi chat mới phải:

1. sử dụng tài liệu Project làm Source of Truth;
2. xác định module đang xử lý;
3. không tự ý đổi kiến trúc;
4. không tạo Entity mới nếu chưa cần thiết;
5. không thay status đã thống nhất;
6. không gộp Shop Order với MarketplaceOrder;
7. không gộp Product với UsedListing;
8. không cho Frontend quyết định business data;
9. nếu yêu cầu mới xung đột thiết kế, phải chỉ rõ trước.

---

# 61. Quy trình khi cần thay đổi kiến trúc

Ví dụ phát hiện cần thêm bảng mới.

Không làm:

`Chat Module → tạo ngay bảng mới`

Mà thực hiện:

`Chat Module → Architecture Issue → Master Architecture → Phân tích → Quyết định → CHANGELOG → Cập nhật Master → Code → Test`

---

# 62. Implementation Order

Thứ tự phát triển chính thức:

1. Sprint 0 – Project Setup.
2. Auth & User.
3. Product Catalog.
4. Cart & Shop Order.
5. Recommendation.
6. Marketplace Listing.
7. Valuation.
8. Marketplace Order & Escrow.
9. Inspection.
10. Dispute & Seller Rating.
11. Admin & Statistics.
12. System Testing.
13. Deployment.

---

# 63. MVP

MVP ưu tiên:

- Authentication;
- User;
- Product;
- Search;
- Filter;
- Cart;
- Checkout;
- Order;
- Admin Product;
- Admin Order;
- PlayerProfile;
- Recommendation;
- UsedListing;
- Valuation;
- MarketplaceOrder;
- Simulated Escrow;
- basic Inspection;
- basic Dispute.

---

# 64. Chức năng chưa phải ưu tiên MVP

Có thể phát triển sau:

- Mobile App hoàn chỉnh;
- AI image-based condition analysis;
- AI-assisted valuation nâng cao;
- real banking escrow;
- real payment gateway;
- real shipping integration;
- Buyer–Seller Chat;
- real-time notification;
- advanced fraud detection;
- advanced partial refund.

---

# 65. Nguyên tắc mô tả chống gian lận

Không sử dụng:

> Hệ thống chống lừa đảo hoàn toàn.

Sử dụng:

> Hệ thống áp dụng các cơ chế kiểm định, trung gian giao dịch, tranh chấp, audit và đánh giá uy tín nhằm **giảm thiểu rủi ro gian lận**.

---

# 66. Trạng thái hiện tại của Project

Tại thời điểm tạo tài liệu:

### Analysis

`DONE`

### System Design

`DONE`

### Master Documentation

`IN_PROGRESS`

### Source Code Setup

`NOT_STARTED`

### Implementation

`NOT_STARTED`

### Testing

`NOT_STARTED`

### Deployment

`NOT_STARTED`

---

# 67. Milestone đã hoàn thành

Mốc soạn thảo trước audit:

> **System Analysis & Design Draft Complete**

Đã soạn 12 phần phân tích và thiết kế trong DOCX. Ngày 2026-10-05 D01–D14 đã APPROVED qua CHG-001–CHG-014; Pending Architecture Issues = 0. Xem DOCUMENT_VERIFICATION_REPORT.md và 03_IMPLEMENTATION_STATUS.md.

---

# 68. Milestone tiếp theo

Milestone kế tiếp:

> **Project Source Documentation Complete**

Bao gồm:

- 00_PROJECT_STRUCTURE.md;
- 01_SYSTEM_ANALYSIS_MASTER.docx và 01_SYSTEM_ANALYSIS_MASTER.md;
- 02_IMPLEMENTATION_RULES.md;
- 03_IMPLEMENTATION_STATUS.md;
- 04_CHANGELOG.md.

---

# 69. Milestone triển khai đầu tiên

Sau khi bộ Source Documents hoàn chỉnh:

> **Sprint 0 – Project Setup**

Mục tiêu:

- Git repository hoạt động;
- React project chạy;
- Spring Boot chạy;
- MySQL kết nối;
- Frontend gọi được Backend;
- cấu trúc source chuẩn được tạo.

---

# 70. Nguyên tắc cuối cùng

Bốn khái niệm cần phân biệt rõ:

### Thiết kế

`01_SYSTEM_ANALYSIS_MASTER`

trả lời:

> Hệ thống phải hoạt động như thế nào?

### Quy tắc triển khai

`02_IMPLEMENTATION_RULES`

trả lời:

> Khi code phải tuân thủ điều gì?

### Trạng thái triển khai

`03_IMPLEMENTATION_STATUS`

trả lời:

> Source code hiện đã làm tới đâu?

### Lịch sử thay đổi

`04_CHANGELOG`

trả lời:

> Thiết kế đã thay đổi những gì và tại sao?

ChatGPT chỉ là công cụ hỗ trợ triển khai.

Không sử dụng một đoạn chat đơn lẻ để thay thế các nguồn chuẩn của Project.
# Decision Matrix đã phê duyệt — document version v1.14

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

## D03 → CHG-003

**Nguồn: CHG-003 / D03 — APPROVED 2026-10-05.**

Giữ weight Skill 20, Play Style 25, Wrist 15, Preferred Weight 15, Flex 10, Budget 15; tổng 100. MatchScoreCalculator tính deterministic/bounded/testable, score trong [0,100].

Skill order: BEGINNER=0, INTERMEDIATE=1, ADVANCED=2. abs(profileLevel-racketLevel) = 0/1/2 → SkillScore = 20/12/5. Bảng đối xứng: [20,12,5]; [12,20,12]; [5,12,20].

CHG-013: targetFlex chỉ theo skillLevel: BEGINNER → FLEXIBLE, INTERMEDIATE → MEDIUM, ADVANCED → STIFF; không dùng wristStrength. Flex ordinal FLEXIBLE=0, MEDIUM=1, STIFF=2, EXTRA_STIFF=3. abs(target-racket) = 0 →10; =1 →6; >=2 →2. Giữ bảng Wrist–Flex và weights.

effectivePrice = salePrice hợp lệ nếu có, ngược lại product.price.

BudgetScore: effectivePrice < minBudget → 12; minBudget <= effectivePrice <= maxBudget → 15; maxBudget < effectivePrice <= 1.10*maxBudget → 10; 1.10*maxBudget < effectivePrice <= 1.20*maxBudget → 5; effectivePrice > 1.20*maxBudget → 0.

Các scoring rule không mâu thuẫn khác giữ nguyên. Không đặt thuật toán trong Controller; Frontend chỉ trình bày.

## D04 → CHG-004

**Nguồn: CHG-004 / D04 — APPROVED 2026-10-05.**

EstimatedPrice = ReferencePrice × AgeFactor × ConditionFactor × WarrantyFactor × AccessoryFactor × RepairFactor × DemandFactor.

Valuation lưu snapshot: referencePrice, ageFactor, conditionFactor, warrantyFactor, accessoryFactor, repairFactor, demandFactor, conditionScore, valuationMethod, suggestedMinPrice, suggestedMaxPrice, algorithmVersion, createdAt; giữ estimatedPrice đang có.

Schema valuations bổ sung repair_factor DECIMAL(5,3), condition_score DECIMAL(4,2), algorithm_version VARCHAR(50). Các factor dùng DECIMAL(5,3) như schema hiện hành; tiền DECIMAL(15,2), không FLOAT/DOUBLE. Snapshot không bị tính lại theo dữ liệu mới khi đọc lịch sử.

Condition Score 0.00–10.00; chuẩn hóa/làm tròn tới hai chữ số thập phân trước khi map Grade: [9.50,10.00] → S; [8.50,9.50) → A; [7.00,8.50) → B; [5.00,7.00) → C; [0.00,5.00) → D.

Nguồn hợp lệ/còn hiệu lực ưu tiên INSPECTION > AI > SELLER. Dùng currentInspectionReport theo CHG-001/007.

Các bảng factor hiện hành không mâu thuẫn được giữ nguyên; các khoảng dùng interval không chồng lấn.

## D05 → CHG-005

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

## D06 → CHG-006

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

## D07 → CHG-007

**Nguồn: CHG-007 / D07 — APPROVED 2026-10-05.**

Seller được sửa Listing của mình khi DRAFT hoặc REJECTED. Submit hợp lệ: DRAFT → PENDING_REVIEW hoặc REJECTED → PENDING_REVIEW; Admin review trước AVAILABLE.

Khi AVAILABLE, đổi sellingPrice, condition, description, product/model information, repairHistory, serial hoặc ảnh quan trọng → PENDING_REVIEW.

Không sửa transaction-critical fields khi RESERVED hoặc SOLD.

Nếu thay condition, model, serial, repairHistory, frame/condition data hoặc inspection-relevant images sau kiểm định: Verified Badge cũ mất hiệu lực cho Listing mới. Không xóa report cũ; cần Inspection mới để có badge trở lại.

Backend chọn report COMPLETED mới nhất còn hiệu lực; không phục hồi badge bằng một report lịch sử đã bị vô hiệu hóa.

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

## PHẦN bổ sung quyết định triển khai 2026-10-08

Nguồn chuẩn hiện gồm D01–D17. CHG-015 mở input định giá phụ kiện/sửa chữa và chốt matching MVP; CHG-016 chốt Category/Brand ACTIVE/INACTIVE không cascade; CHG-017 mở cancel Marketplace và Audit ADMIN. Xem phần bổ sung trong Master/Rules/CHANGELOG; trạng thái triển khai thực tế tại 03_IMPLEMENTATION_STATUS.md. Stack và module boundary giữ nguyên.
