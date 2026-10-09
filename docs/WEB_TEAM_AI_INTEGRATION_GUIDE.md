# HƯỚNG DẪN CHO NGƯỜI PHỤ TRÁCH WEB — TÍCH HỢP AI BADMINTON SHOPPING ADVISOR

> **Tên đề tài gốc (giữ nguyên):** Xây dựng hệ thống thương mại điện tử cầu lông đa nền tảng tích hợp chức năng gợi ý sản phẩm theo nhu cầu người dùng.  
> **Đối tượng đọc:** Thành viên phụ trách Website, Product Catalog, giao diện thương mại và luồng giỏ hàng/đơn hàng.  
> **Phiên bản:** DRAFT 1.1 — 2026-10-10.  
> **Mục đích:** Thống nhất việc Website cần chuẩn bị, dữ liệu phải có, cơ chế đồng bộ trạng thái sản phẩm, đầu mối API và trách nhiệm tích hợp. **Không phải lệnh tự ý sửa source.**  
> **Tài liệu AI cùng bộ:** `AI_RECOMMENDATION_ARCHITECTURE_AND_ROADMAP.md`.  
> **Đối chiếu source:** `taptappp/Batmintonweb` nhánh `main` commit `f1425400b3a30800b3d8dbc15ef18f716f4937a4`; phải xác minh lại trên nhánh/commit triển khai thực tế.

---

## PHẦN 0. NGUỒN CHUẨN VÀ RANH GIỚI KIẾN TRÚC

**Trước khi bắt đầu**, đọc `00_PROJECT_STRUCTURE.md`, `01_SYSTEM_ANALYSIS_MASTER.docx` + `.md`, `02_IMPLEMENTATION_RULES.md`, `03_IMPLEMENTATION_STATUS.md`, `04_CHANGELOG.md`. Quyết định **APPROVED** mới nhất trong CHANGELOG là nguồn chuẩn cao nhất khi có mâu thuẫn; `03_IMPLEMENTATION_STATUS` chỉ mô tả trạng thái triển khai. Bản status đính kèm Project là snapshot cũ; `main` đã đối chiếu ghi `PlayerProfile / Recommendation` **TESTING**, chưa phải DONE. Kiểm tra lại commit hiện hành trước khi code.

- Giữ **React, Spring Boot, MySQL, REST API, JWT, Spring Data JPA, Modular Monolith**, REST dùng lại được cho Mobile.
- **Backend quyết định** userId, quyền, giá hiệu lực, tồn kho, trạng thái, tổng tiền, Match Score. UI/AI không tự đặt số liệu.
- **Giữ nguyên `MatchScoreCalculator` dành cho vợt**, không đổi sáu trọng số hoặc score đã duyệt. Không áp điểm vợt cho các danh mục khác.
- Không gộp `Product` với `UsedListing`, không gộp Shop `Order` với `MarketplaceOrder`; BUYER/SELLER không phải system role mới.
- Không đổi Entity/relationship/API/status/algorithm/module boundary/migration trước khi có bản đề xuất, đánh giá tác động và phê duyệt. Nếu được duyệt: cập nhật CHANGELOG và Master/Rules liên quan, triển khai vertical slice, test, rồi cập nhật Implementation Status.
- Tài liệu này là **bản bàn giao/đề xuất**, không phải API/DB đã được phê duyệt.

## PHẦN 1. PHỐI HỢP 2 NGƯỜI — AI KHÔNG TÁCH KHỎI WEBSITE

| Hạng mục | Người phụ trách Web | Người phụ trách AI |
|---|---|---|
| Product, Category, Brand, ProductImage, thuộc tính, biến thể, stock | **Sở hữu schema/API/CRUD/catalog data**; trình phê duyệt khi đổi model | Nêu trường cần, test khả năng tìm/gợi ý, không clone bảng Product |
| PlayerProfile, Auth, JWT | Sở hữu nghiệp vụ tài khoản hiện có và thay đổi liên quan nếu được duyệt | Đọc qua Service/API đúng quyền; không lấy userId từ client |
| Vợt & RacketSpecification | Đảm bảo dữ liệu catalog và spec thật | Tái dùng MatchScoreCalculator, giữ score cũ |
| AI NLP & Dialogue | Cung cấp dữ liệu cần để tư vấn | **Sở hữu classifier/extractor/dialogue/tri thức và kiểm thử ML** |
| AI Side Panel | Phối hợp vị trí mount layout, responsive, route và style | Viết nội dung panel, ba mode, chat, Top 3/Xem thêm nếu thống nhất |
| Product Detail | **Sở hữu trang chi tiết và hình ảnh** | Điều hướng đúng `/products/:id`, không tạo detail riêng |
| Cart / Favorites / Order | **Sở hữu API, giá, stock, auth, checkout** | Gọi đúng API từ hành động đã xác nhận, không tự tính tiền |
| Test & release | Regression Catalog/Cart/Order/Frontend | Regression Recommendation/NLP/chat/AI UI; hai bên chạy E2E chung |

**Ranh giới công việc thống nhất:** Web cung cấp dữ liệu đúng và cập nhật; AI hiểu yêu cầu và gợi ý; Backend xác minh và xử lý nghiệp vụ. Có thể phân công lại việc viết AI Panel tùy năng lực, nhưng **quyền sở hữu module và hợp đồng API phải nhất quán**.

## PHẦN 2. WEB HIỆN ĐÃ CÓ GÌ?

Bảng này mô tả source đối chiếu, **không có nghĩa tất cả đều đã test E2E trên máy hai thành viên**.

| Hạng mục | Đang có | Điểm cần chú ý |
|---|---|---|
| `Category` và `Brand` | Có Entity/API | Chỉ tư vấn các danh mục thực sự có trong Catalog |
| `Product` | Tên, mô tả, category, brand, price, salePrice, stock, thumbnailUrl, status | **Chưa có màu/size/variant/nguồn xác minh có cấu trúc** |
| `ProductImage` | Hình ảnh sản phẩm | Không được lấy ảnh mạng bất kỳ thay ảnh sản phẩm thật |
| `RacketSpecification` | Weight, balance, flex, maxTensionLbs, playStyle, skillLevel, gripSize, material, v.v. | `maxTensionLbs` là **mức tối đa lbs**, không phải mức căng AI khuyên dùng |
| `ProductResponse` | `id`, `productName`, `description`, `effectivePrice`, `stock`, `status`, `thumbnailUrl`, `images`, `brand`, `category`, `specification` | `stock` là **cấp Product**, chưa phải từng size/màu |
| `GET /api/products` | Search/list theo categoryId/brandId/keyword/giá; filter vợt | Kết quả hiện có thể gồm `ACTIVE` và `OUT_OF_STOCK` |
| `GET /api/products/{id}` | Chi tiết sản phẩm | Route React Product Detail dự kiến đang dùng `/products/:id` |
| `GET /api/player-profile` | Đọc Profile khi đăng nhập USER | Profile chưa có sở thích tự do |
| `GET /api/recommendations` | Gợi ý vợt từ Profile | Đang chỉ lấy `ACTIVE && stock > 0`, limit mặc định 10, Match Score hiện hữu |
| Product statuses | `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`, `DISCONTINUED` | Không thêm status mới khi chưa duyệt |

**Phần dữ liệu demo hiện có:** script `database/01_demo_catalog.sql` của source đang đối chiếu chỉ tạo **ba vợt giả lập**, không phải đủ giày, áo, cước, túi. Muốn demo nhiều danh mục, phải phối hợp xây dữ liệu mẫu hợp lệ, **đánh dấu rõ là giả lập** nếu không phải thông số hãng; không biến data demo thành sự thật thương mại.

## PHẦN 3. SẢN PHẨM VÀ THUỘC TÍNH CẦN CHUẨN BỊ CHO AI

**Yêu cầu quan trọng:** AI chỉ trả về được những thuộc tính có dữ liệu và nguồn đáng tin cậy; thiếu thì **phải báo thiếu**, không được tự đoán. Những trường dưới đây là yêu cầu đề xuất, **chưa phải cột DB đang tồn tại**.

| Danh mục | Thuộc tính tối thiểu để tìm kiếm / tư vấn | Thuộc tính bổ sung hữu ích | Chủ sở hữu nhập/xác minh |
|---|---|---|---|
| **Vợt** | Thông số `RacketSpecification` hiện có, brand, giá, stock, ảnh | Màu, mẫu năm, mức căng tối đa đã xác minh | Web/Catalog |
| **Dây cước** | Loại dây, độ dày mm, brand, màu, giá | Độ nảy, độ bền, kiểm soát, cảm giác và nguồn thông số | Web/Catalog |
| **Tư vấn mức căng** | Mẫu vợt, `maxTensionLbs` được kiểm chứng; đặc điểm dây và người chơi | Khoảng căng khuyến nghị có tài liệu hãng/chuyên gia đã kiểm tra | Web lưu nguồn; AI phân tích & giải thích |
| **Giày** | Size theo mẫu/hãng, số đo cm / chart, màu, giá, tình trạng size | Form chân bè/hẹp, độ ổn định, độ bám, chất liệu | Web/Catalog |
| **Quần áo** | Size, màu, chất liệu, giá, tồn kho size | Bảng số đo, form, chất liệu/thoáng khí | Web/Catalog |
| **Tất/bảo hộ** | Loại, size, vật liệu, công dụng, giá | Kích thước và độ phù hợp theo nhu cầu | Web/Catalog |
| **Quấn cán** | Loại, độ dày, độ bám, màu, giá | Khả năng hút ẩm, tương thích với cán | Web/Catalog |
| **Quả cầu** | Lông/nhựa, tốc độ theo mô tả hãng, số lượng/hộp, giá | Mục đích chơi, điều kiện dùng | Web/Catalog |
| **Túi/balo** | Kích thước, sức chứa, số ngăn, giá | Ngăn giày, chống ẩm, màu | Web/Catalog |
| **Phụ kiện** | Category, mô tả kỹ thuật, tính tương thích và giá | Size, hướng dẫn sử dụng | Web/Catalog |

### 3.1. Cần đề xuất mô hình thuộc tính (không tự triển khai)

Hiện các Product không phải vợt thiếu lớp thuộc tính chuyên biệt. Hai hướng phân tích cho Architecture Review:

- **Phương án thuộc tính có kiểu theo từng category:** dễ validate và lọc chính xác, nhưng thêm model/DTO cho mỗi nhóm; phù hợp danh mục ổn định.
- **Phương án bảng định nghĩa thuộc tính + giá trị có kiểu và validation:** dễ mở rộng nhiều nhóm nhưng phức tạp hơn về truy vấn, đơn vị, chỉ mục, giới hạn và quản trị.

Không chốt phương án ở tài liệu bàn giao. Phải nêu **thiết kế hiện tại, vấn đề, phương án, ảnh hưởng tới DB/Entity/Repository/API/Admin/UI/Test** để hai người phê duyệt. Không lưu toàn bộ thông số quan trọng trong một chuỗi `description` rồi để AI tự đoán trong khi cần lọc chính xác.

### 3.2. Biến thể (SKU) và tồn kho size/màu

Để trả lời “giày size 41 còn mấy đôi?”, cần tồn kho **theo biến thể**. Hiện `Product.stock` chỉ là tồn kho của Product, **không chứng minh stock của size 41**.

Phương án đề xuất cần review: `Product` (mẫu) → nhiều SKU/variant (ví dụ size, màu) → stock, giá/phụ thu và trạng thái theo SKU nếu cần. Ảnh hưởng **Product, CartItem, OrderItem snapshot, kiểm soát stock đồng thời, giỏ hàng/checkout**. Không tự thêm bảng/quan hệ khi chưa có quyết định.

Nếu chưa triển khai SKU, chỉ hiển thị: “Sản phẩm này còn **N** ở cấp Product; chưa xác minh số lượng từng size/màu”. Không cho AI hứa size đang còn hàng khi không có căn cứ.

### 3.3. Thông số kỹ thuật và nguồn xác minh

Đề xuất về mặt nghiệp vụ: mỗi thông số chuyên ngành quan trọng có thể cần `source`, `lastVerifiedAt`, `verificationStatus`, `unit`. **Đây chỉ là field logic/ý tưởng, không phải API hay DB đã duyệt.**

- Dây/căng: phân biệt mức căng tối đa `maxTensionLbs` của vợt và **mức căng khuyến nghị**; khi hiển thị kg làm rõ kgf, quy đổi `kgf = lbf × 0.45359237`.
- Giày/áo: size chart theo model/brand; **không áp một bảng quy đổi chung** nếu chưa có nguồn.
- Màu: xác minh màu bằng metadata thay vì suy từ ảnh.
- Thông số lỗi thời/mâu thuẫn: cần cờ “Chưa xác minh” hoặc yêu cầu Admin kiểm tra; AI không tự sửa Product.

## PHẦN 4. QUY TẮC UI KHI DATA THIẾU, HẾT HÀNG, KHÔNG CÓ KẾT QUẢ

**Phân biệt năm tình trạng:** thiếu đầu vào người dùng, thiếu metadata sản phẩm, kết quả không khớp, có hàng nhưng hết stock, và lỗi dịch vụ. Không dùng cùng một thông báo cho tất cả.

| Tình huống | UI chuẩn đề xuất | Hành động / trách nhiệm |
|---|---|---|
| Thiếu chiều dài chân/mẫu vợt/size người dùng | “Bạn cho biết thêm [thông tin] để mình tư vấn nhé.” | AI hỏi bổ sung; Web chỉ cung cấp trường nhập nếu cần |
| Catalog thiếu màu/size/chart/căng tối đa | “Hiện chưa có thông tin xác minh về [trường] của sản phẩm.” | Web kiểm tra/cập nhật metadata; AI không đoán |
| Thuộc tính đang chờ bổ sung | “Thông tin đang được cập nhật. **Chưa có lịch dự kiến**.” | **Chỉ hiển thị ngày dự kiến** khi Web có thông tin thật |
| Không có sản phẩm hợp hard constraints | “Không tìm thấy sản phẩm phù hợp với các điều kiện hiện tại.” | AI nêu điều kiện lọc và hỏi có muốn nới ràng buộc không |
| Có sản phẩm phù hợp nhưng hết hàng | “Sản phẩm phù hợp hiện đã hết hàng — số lượng 0.” | Gợi ý thay thế; tách khỏi Top 3 có thể mua |
| Product `ACTIVE`, stock > 0 | “Còn N sản phẩm” | Backend quyết định N, kiểm tra lại lúc mua |
| `stock = 0` (hoặc `OUT_OF_STOCK` đã xác minh số lượng 0) | “Hết hàng — số lượng 0” | Không trình bày như có thể checkout; không tự gán ACTIVE |
| `OUT_OF_STOCK` nhưng stock > 0, status–stock mâu thuẫn | “Tình trạng hàng cần xác minh” (không ghi sai số lượng 0) | Web kiểm tra nguồn dữ liệu; AI không cho mua |
| Stock tổng N nhưng chưa có SKU size/màu | “Còn N sản phẩm ở cấp mẫu; chưa xác minh size/màu bạn cần” | Không cam kết còn size màu riêng |
| Có restock ETA từ Web | “Dự kiến hàng về ngày X (có thể thay đổi)” | Chỉ Web/backend cung cấp, **không cho AI tự ước đoán ngày** |
| Không có ETA chính thức | “Chưa có lịch hàng về” | Có thể giới thiệu hàng thay thế; không hứa sẽ thông báo |
| `INACTIVE` hoặc `DISCONTINUED` | Không giới thiệu như sản phẩm còn bán | Xử lý view link cũ theo rule Web hiện hành |
| Thiếu ảnh | Placeholder “Chưa có ảnh sản phẩm” | Không tự gắn ảnh minh họa thành ảnh thật |
| Giá/tồn kho thay đổi | Cập nhật thông tin từ Backend khi mở Detail, thêm Cart, Checkout | Không chốt giá/stock bằng snapshot chatbot |
| API timeout/lỗi mạng | “Chưa thể tải dữ liệu. Thử lại.” | Retry giới hạn; fallback Catalog/form; không hiện No Match sai |

### 4.1. Chính sách Top 3 và xem thêm

- **Top 3 mua được:** tối đa 3 sản phẩm thuộc Catalog có tình trạng hợp lệ, thuộc tính khớp theo quy tắc đã duyệt. Không cố đủ 3 bằng cách chèn hàng hết hoặc sai điều kiện.
- **Xem thêm:** đề xuất 3 → 9 → 15… nhưng page size, API cursor/page/snapshot **cần duyệt**; giữ thứ tự ổn định, không tự lặp/tráo kết quả.
- **Hết hàng:** nếu cần hiển thị riêng “Các mẫu phù hợp nhưng đã hết hàng”, dùng danh sách phụ có nhãn rõ; **không trộn vào Top 3 có thể mua**.
- **Nhiều danh mục:** không so điểm vợt với giày bằng cùng Match Score; có thể hiển thị Top 3 theo danh mục nếu được duyệt.
- **Không có kết quả:** hiển thị đúng trạng thái và gợi ý cách điều chỉnh với sự xác nhận của người dùng, không xóa hard constraint trong nền.

### 4.2. Dự kiến hàng về và đăng ký nhận thông báo

Trong phiên bản đầu, **chưa bắt buộc** có nguồn lịch hàng về; vì vậy hiển thị “Chưa có lịch hàng về”. Nếu sau này Web tích hợp ETA thật thì phân biệt:

- **Dự kiến cập nhật thông số**: khi nào dữ liệu sản phẩm có thể được hoàn thiện.
- **Dự kiến hàng về**: khi nào nhập thêm hàng; đây là thông tin **ước tính có nguồn**.
- **Ngày xác minh cuối**: mốc dữ liệu cuối cùng được Web/Admin kiểm tra, không đồng nghĩa với ETA.

Đăng ký thông báo khi có hàng (opt-in) là chức năng **Later**, chỉ sau khi đã phê duyệt cơ chế lưu yêu cầu, quyền riêng tư, thông báo, hạn chế spam, xác định SKU và API. Không đặt nút bấm giả hoặc hứa gửi email khi chưa có chức năng thật.

## PHẦN 5. GIAO DIỆN WEB CẦN PHỐI HỢP

### 5.1. Floating icon + Side Panel

- Đặt icon “Tư vấn AI” góc dưới của layout dùng chung, nhưng tránh che nút giỏ hàng, thanh điều hướng mobile hoặc action thanh toán.
- Desktop: panel chiếm khoảng **45–50% chiều rộng viewport**, phần trang còn lại vẫn xem được. Mobile: panel phù hợp full-screen; hỗ trợ nút đóng, ESC, focus, scroll, keyboard.
- Mở/thu nhỏ/đóng không đổi route sản phẩm đang xem; giữ context ở mức phiên đã thống nhất.
- Ba chế độ: **Lấy từ Profile**, **Tự nhập thông số**, **Chat trực tiếp với AI**.
- Ô **“Sở thích/yêu cầu bổ sung”** tự do cho Profile và Manual; Chat cho nhắn tự nhiên. Không ép người dùng điền form chỉ để chat.

### 5.2. Product Card AI

Trên card phải hiển thị:

1. `thumbnailUrl` hoặc ảnh sản phẩm thật trong `images` từ Product API; nếu không có thì placeholder rõ ràng.
2. `productName`, brand/danh mục nếu cần.
3. `effectivePrice` từ Backend; giá hiển thị như snapshot, không phải bảo đảm giá checkout.
4. **Tình trạng hàng và số lượng** dựa trên `status` + `stock` Backend (theo chính sách trình bày và dữ liệu hiện có).
5. Lý do và điểm cần cân nhắc; `matchScore` **chỉ cho vợt** nếu Backend trả qua thuật toán hiện hữu.
6. Nút **Xem chi tiết**, đi tới route Web `/products/:id` (xác minh lại với route thực tế lúc tích hợp).
7. Nút **Xem thêm** và **Thu gọn** trên tập kết quả; không hiện nút khi đã hết sản phẩm.

Nên tái sử dụng ProductCard/utility tiền/ảnh có sẵn để giao diện thống nhất, không dựng component Catalog khác nhau nếu không cần. Có thể hiển thị **Quick View** sau, không thay thế Product Detail chính.

### 5.3. Giỏ hàng / yêu thích / thao tác thương mại

- AI chỉ đưa đề xuất, việc mua/yêu thích phải gọi **API nghiệp vụ Web đã có**, không cập nhật stock hay tổng tiền từ frontend.
- Nếu muốn thêm vào giỏ trực tiếp trong Side Panel, yêu cầu login và kiểm tra số lượng, SKU/size, giá hiệu lực theo luật Cart hiện tại.
- Nếu product hết hàng, `INACTIVE`, `DISCONTINUED`, hoặc variant thiếu xác nhận tồn kho, không hiển thị CTA đặt hàng như thể chắc chắn mua được.
- Khi đi từ chat → Product Detail → quay lại, giữ context trong mức chính sách đã chốt, không tự lưu lịch sử cá nhân không có đồng ý.

## PHẦN 6. HỢP ĐỒNG API — PHÂN BIỆT ĐANG CÓ VÀ ĐỀ XUẤT

### 6.1. API đang tồn tại (đối chiếu `main`)

| Method | Path | Dữ liệu cần cho Web–AI |
|---|---|---|
| GET | `/api/products` | Product Catalog; lọc category/brand/giá/keyword; phân trang |
| GET | `/api/products/{id}` | Product Detail; `ProductResponse` gồm image, effectivePrice, stock, status, spec |
| GET | `/api/categories` | Danh mục thật |
| GET | `/api/brands` | Thương hiệu thật |
| GET | `/api/player-profile` | PlayerProfile người dùng hiện tại (USER) |
| POST/PUT | `/api/player-profile` | Tạo/sửa các thông số Profile hiện có |
| GET | `/api/recommendations?limit=10` | Recommendation vợt dựa trên Profile, Match Score của backend, default hiện tại 10 |

**Không được sửa default `GET /api/recommendations` thành 3 chỉ vì AI Side Panel muốn Top 3.** Top 3 là hành vi của giao diện AI mới.

### 6.2. Contract đề xuất cho API AI (chưa APPROVED)

| Giao tiếp | Ai sở hữu nghiệp vụ | Thỏa thuận phải có |
|---|---|---|
| Nhận yêu cầu Profile/Manual/Chat | AI + backend | `sourceMode`, category IDs, sở thích, hard/soft constraints, userId backend lấy từ JWT |
| Trả sản phẩm gợi ý | AI / Recommendation backend | ProductResponse thật, Match Score chỉ cho vợt, `reasons`, `cautions`, `hasMore` |
| Thiếu input / catalog | AI + Web data quality | `resultKind`, `missingFields`/`unverifiedFields` logic, câu hỏi tiếp theo |
| Tồn kho/variant/ETA | **Web Product backend** | Số lượng, status, timestamp xác minh, ETA **nullable** khi có nguồn chính thức |
| So sánh sản phẩm | AI + Product Catalog | ProductIds xác thực, cùng danh mục/khác danh mục, không bịa thông số |
| Lưu sở thích mặc định | Web/User + AI | Tùy chọn đồng ý lưu, ownership, sửa/xóa, không ghi đè khi mua hộ |
| Feedback/History | AI + User/DB | Phê duyệt lưu trữ, bảo mật, chính sách xóa, thống kê |

Tất cả JSON cần dùng **wrapper hiện tại** `ApiResponse` (`success`, `message`, `data`) và error wrapper chuẩn theo Rules. Bảng bên dưới là payload logic minh họa, **không phải DTO/entity/API thực**:

```json
{
  "success": true,
  "message": "Đã tải tình trạng sản phẩm",
  "data": {
    "productId": 101,
    "name": "Sản phẩm minh họa",
    "status": "OUT_OF_STOCK",
    "stock": 0,
    "stockUnit": "product",
    "selectedVariant": null,
    "variantStock": null,
    "verifiedAt": null,
    "expectedRestockAt": null,
    "missingAttributes": ["color", "sizeChart"],
    "expectedMetadataUpdateAt": null
  }
}
```

Trong đó `stockUnit`, `variantStock`, `verifiedAt`, `expectedRestockAt`, `missingAttributes`, `expectedMetadataUpdateAt` **chưa tồn tại trong `ProductResponse` hiện hành**. **Không tự thêm** hoặc coi ví dụ JSON là hợp đồng đã phê duyệt.

### 6.3. Định nghĩa rõ kết quả AI khác nhau

Đề xuất biểu diễn logic, tên code chờ duyệt:

| Kết quả | Khi nào xảy ra | Đầu ra |
|---|---|---|
| `RECOMMENDATIONS` | Có sản phẩm đáp ứng | Items + hasMore |
| `NEED_INPUT` | Người dùng thiếu thông tin cần thiết | Câu hỏi tiếp + trường còn thiếu |
| `INSUFFICIENT_CATALOG_DATA` | Thiếu metadata đã xác minh để áp điều kiện | Ghi rõ thuộc tính thiếu, không kết luận không có sản phẩm |
| `NO_MATCH` | Đã kiểm tra đầy đủ nhưng không khớp hard constraints | Zero items, diễn giải ràng buộc, lựa chọn nới lỏng |
| `MATCHED_BUT_OUT_OF_STOCK` | Có mẫu khớp nhưng không mua được do hết hàng | Khu vực hàng hết và thay thế, không nằm Top 3 mua được |
| `SERVICE_ERROR` | Backend/NLP/API lỗi | Error wrapper, Retry, fallback; không phải `NO_MATCH` |

Các code trên chỉ là yêu cầu **phân biệt tình huống UI và nghiệp vụ**, chưa phải việc thêm Java enum hay trạng thái sản phẩm mới.

## PHẦN 7. DỮ LIỆU CẦN BÀN GIAO QUA HAI BÊN

| Deliverable | Người Web cung cấp | Người AI xác minh / sử dụng |
|---|---|---|
| Catalog categories/brands | ID, tên, status và mẫu dữ liệu | Map category trong NLP, không hardcode danh mục không có |
| Catalog ProductResponse | JSON thật với image, effectivePrice, status, stock | Dùng vào Top 3, cards, filters, explanation |
| RacketSpecification | Các trường hiện hành kèm giá trị hợp lệ và nguồn | Regression MatchScoreCalculator + tư vấn vợt |
| Thuộc tính giày/áo/cước/túi | Schema/DTO/field được phê duyệt + dữ liệu kiểm chứng | Category-specific AI extraction/filter |
| Size chart / variant | Bảng kích thước theo hãng, tồn kho từng SKU đã duyệt | Tư vấn size và “còn size nào” không suy từ stock tổng |
| Data completeness | Danh sách trường đủ/thiếu/xung đột có nguồn | UI “thiếu dữ liệu”, confidence, follow-up |
| Tình trạng hàng / ETA | Status/stock chính thức và ngày hàng về nullable | Kết luận còn/hết, không tự bịa ngày |
| Routes/actions | Route Product Detail, Cart, Favorite, trạng thái auth | Điều hướng/call API đúng |
| Error contract | Mã lỗi và wrapper thực tế | Phân biệt not found, no match, timeout, out of stock |
| Fixture + test access | Tập dữ liệu giả lập có cờ DEMO, bộ User test phân quyền | E2E tích hợp bằng sản phẩm thật trong DB test |

**Trường dữ liệu chỉ được coi “đã bàn giao” khi có file/schema/API thật + test mẫu**, không chỉ ghi tên trong chat.

## PHẦN 8. KẾ HOẠCH LÀM VIỆC SONG SONG

| Mốc | Web chịu trách nhiệm | AI chịu trách nhiệm | Cổng phê duyệt |
|---|---|---|---|
| W0 — Review | Kiểm kê Catalog, status, stock, Product Detail | Kiểm kê Recommendation, datasets, 3 mode | Chốt phạm vi theo nguồn chuẩn |
| W1 — Thiết kế dữ liệu | Đề xuất category attributes, size/variant, source/verification nếu cần | Đề xuất trường cần trích xuất và ranking theo category | Duyệt thay đổi schema/API/business rule |
| W2 — Catalog vertical slice | DB → Entity → Repo → Service → Controller → API Test → Frontend → Integration Test | Viết tests với contract, chưa thay Web DB | Migration không sửa bản đã apply |
| W3 — AI Recommendation | Giữ API Web ổn định, cung cấp fixtures | Profile/Manual + free-text; Top 3/Xem thêm | Duyệt API AI + rank theo category |
| W4 — Chat và UX | Mount layout, Product Detail, responsive | NLP/dialogue/knowledge + panel, card và feedback | Auth/guest/history/privacy được duyệt |
| W5 — Tích hợp | Giá/stock/variant, cart/detail, lỗi/error | E2E theo ba mode, ranking, so sánh, no-match/missing | Regression Web + AI PASS có evidence |
| W6 — Mở rộng | ETA/restock, thống kê field thiếu, theo dõi hàng về nếu duyệt | Hiển thị dự kiến, gợi ý thay thế, báo sai dữ liệu | Tránh hứa tính năng chưa tồn tại |

**MVP đề xuất Web cần ưu tiên:** ProductResponse có dữ liệu thật, đủ Category, ảnh/stock/status chính xác, route Product Detail hoạt động, thống nhất data gaps, và chỉ chốt biến thể/ETA theo nhu cầu demo được giảng viên thông qua. Không biến mọi tiện ích thành blocker phải hoàn thành trước khi AI-01 chạy.

## PHẦN 9. TEST CASE CHUNG WEB–AI (CHECKLIST NGHIỆM THU)

### 9.1. Dữ liệu và Product Catalog

- [ ] Product ACTIVE + stock 5 → UI “Còn 5 sản phẩm”, score và giá lấy backend.
- [ ] Product OUT_OF_STOCK + stock 0 → UI “Hết hàng — số lượng 0”; không nhấn mua hợp lệ.
- [ ] Product ACTIVE + stock 0 → backend/UI xử lý nhất quán theo quyết định được duyệt; không quảng cáo còn hàng.
- [ ] Product OUT_OF_STOCK + stock 5 (mâu thuẫn) → UI “Tình trạng hàng cần xác minh”, không sai thành “còn 5” hay “hết hàng — 0”.
- [ ] Product có stock tổng 5 nhưng không có variant size → **không nói size 41 còn 5**.
- [ ] Thiếu size chart/độ dày dây/max tension/màu → hiện “Chưa có thông tin xác minh”, không bịa thông số.
- [ ] Có thông số mâu thuẫn giữa hai nguồn → hiện “Cần xác minh”, không cam kết an toàn/cỡ giày.
- [ ] `expectedRestockAt = null` (chỉ nếu schema mới được duyệt) → “Chưa có lịch hàng về”.
- [ ] Có ETA chính thức → hiển thị nhãn “Dự kiến”, không nói chắc chắn.
- [ ] `INACTIVE`/`DISCONTINUED` → không giới thiệu mua và không cho qua checkout.
- [ ] Thiếu ảnh → placeholder; không tự chọn ảnh sản phẩm khác.
- [ ] Giá/stock đổi khi giữ panel → Product Detail và Cart/Checkout kiểm tra dữ liệu mới.

### 9.2. AI và giao diện

- [ ] Ba mode hoạt động; Profile lấy JWT, Manual không sửa Profile, Chat giữ context theo session policy.
- [ ] Sở thích tự do “thích màu xanh”, “chỉ màu xanh” phân biệt soft/hard; thiếu metadata màu → báo chưa xác minh.
- [ ] Vợt vẫn giữ nguyên Match Score và sorted theo rule hiện tại; non-racket không có fake Match Score.
- [ ] Top 3, xem thêm/thu gọn; nếu 2 kết quả chỉ có 2; không trùng/bỏ sót khi phân trang.
- [ ] Khi zero result nhưng Catalog đủ dữ liệu, hiện `NO_MATCH` và lựa chọn đổi điều kiện.
- [ ] Khi thiếu input, hỏi đúng thông số, không coi là `NO_MATCH`.
- [ ] Khi API/NLP lỗi, hiện lỗi tải và retry; không gán 0 sản phẩm.
- [ ] Có hàng khớp nhưng hết stock → khu riêng, không cộng vào Top 3 mua được.
- [ ] Card ảnh + tên + giá + lý do + tồn kho; click đúng Product Detail ID.
- [ ] Mobile panel không che toàn bộ nút đóng/nút mua, hoạt động bàn phím/ARIA cơ bản.

### 9.3. Bảo mật, regression và evidence

- [ ] Không tin userId/role/score/giá từ client.
- [ ] Thiếu JWT không truy cập PlayerProfile cá nhân; guest chat chỉ nếu có phê duyệt.
- [ ] Không lưu thông tin mua hộ/lịch sử chat khi chưa có chính sách.
- [ ] No XSS từ preference/chat/description, kiểm tra chiều dài payload và rate limit.
- [ ] Regression sản phẩm–cart–order không giảm chất lượng khi thêm variant/metadata.
- [ ] Backend test, frontend Vitest, Playwright E2E và MySQL migration/upgrade test có log và commit đối chiếu. Không tự ghi PASS.

## PHẦN 10. PHIẾU THỐNG NHẤT CẦN HAI NGƯỜI XÁC NHẬN TRƯỚC KHI CODE

| Chủ đề | Quyết định cần có | Trạng thái tài liệu |
|---|---|---|
| Thuộc tính đa danh mục | Schema theo category hay attribute model? Cấu trúc typed/filter? | NEEDS_DECISION |
| Variant và stock | Có SKU size/màu không, ảnh hưởng Cart/Order ra sao? | NEEDS_DECISION |
| Sở thích Profile | Field/bảng nào lưu văn bản; ai xóa/sửa? | NEEDS_DECISION |
| Recommendation ngoài vợt | Hard/soft rules, ranking/tie break, hiển thị score không? | NEEDS_DECISION |
| Mua hộ | Manual session độc lập Profile, không ghi đè | YÊU CẦU ĐÃ THỐNG NHẤT; API chờ duyệt |
| AI auth/guest | Guest được chat hay phải login? | NEEDS_DECISION |
| AI panel | Ai mount layout, ai phát triển nội dung? | NEEDS_ASSIGNMENT |
| No-match vs missing vs stock | UI phải phân biệt, không bịa dữ liệu | YÊU CẦU ĐÃ THỐNG NHẤT; contract chờ duyệt |
| Restock ETA | Nguồn nhập, nullable, hiển thị dự kiến, retention | NEEDS_DECISION/LATER |
| Notify restock | Opt-in, event/notification, auth, chống spam | LATER/NEEDS_DECISION |
| Đặt dịch vụ căng dây | **Chỉ tư vấn trong MVP**; đặt dịch vụ là thay đổi Shop/Cart/Order mới | KHÔNG TRIỂN KHAI TRƯỚC DUYỆT |
| Marketplace UsedListing | Shop Product only ban đầu; tách riêng hàng cũ | MVP GIỮ NGUYÊN; later nếu duyệt |

**Việc cần làm ngay giữa hai thành viên:** họp chốt những trường Product Catalog tối thiểu để AI tư vấn được vợt, giày, dây cước; thống nhất hai ví dụ API thật và định nghĩa năm trạng thái UI. Nếu cần thay đổi kiến trúc, lập Architecture Issue nêu thiết kế hiện tại, khó khăn, phương án, tác động, người duyệt trước khi tạo migration/code.

---

## PHẦN 11. CÁCH DUY TRÌ TÀI LIỆU VÀ BÀN GIAO

1. **Chưa duyệt**: tài liệu này và tài liệu AI là `PROPOSAL`; không coi sample JSON, class, biến thể hay tên result logic là source chuẩn.
2. **Duyệt**: ghi quyết định APPROVED trong `04_CHANGELOG.md`; cập nhật `01_SYSTEM_ANALYSIS_MASTER.docx` và `.md`, `02_IMPLEMENTATION_RULES.md` và tài liệu thiết kế liên quan.
3. **Triển khai**: code theo vertical slice Database → Entity → Repository → Service → Controller/API → API Test → Frontend → Integration Test; migration Flyway mới nếu schema thay đổi, không sửa migration đã áp dụng.
4. **Nghiệm thu**: ghi test evidence và giới hạn vào `03_IMPLEMENTATION_STATUS.md`, cập nhật `baocao.md` nếu đang dùng theo quy trình đồ án; module mới không phải DONE chỉ vì build thành công.
5. **Thay đổi khi làm song song**: giao tiếp bằng PR/commit + docs/contract, không đổi ngầm endpoint, DTO, ý nghĩa Product.stock, MatchScore hoặc route.

**Kết luận:** Web cần cung cấp Product Catalog đúng, đầy đủ thuộc tính theo danh mục và dữ liệu trạng thái có thể kiểm chứng; AI tư vấn bằng nguồn dữ liệu đó. Khi thiếu/không có/hết hàng, giao diện phải thông báo đúng **nguyên nhân thật**. Các trường metadata/variant/ETA/restock và contract AI chỉ được xây dựng sau quyết định kiến trúc, không làm sai các module commerce đang vận hành.
