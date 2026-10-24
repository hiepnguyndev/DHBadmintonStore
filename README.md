# Chatbot hiện tại

AI nội bộ đã được gỡ bỏ. Xem [hướng dẫn kết nối API bên ngoài](docs/CHATBOT_EXTERNAL_API.md). Các mô tả thử nghiệm AI bên dưới là tài liệu lịch sử.

# DH Badminton Store — Source chạy local

**Đề tài:** Xây dựng hệ thống thương mại điện tử cầu lông đa nền tảng tích hợp chức năng gợi ý sản phẩm theo nhu cầu người dùng.

React + Spring Boot + MySQL, REST API/JWT, Spring Data JPA/Hibernate, Modular Monolith. Web là nền tảng chính; API tách khỏi giao diện để có thể dùng lại cho Mobile.

Bộ source chứa toàn bộ phần đã triển khai, không chỉ Auth. **Đã triển khai ba quyết định D15–D17:** Valuation, trạng thái Category/Brand, API hủy Marketplace và đọc Audit. Chưa triển khai production hoặc tuyên bố toàn hệ thống DONE. Chi tiết: `docs/03_IMPLEMENTATION_STATUS.md` và `docs/DESIGN_DECISIONS_PENDING.md`.

### Chạy trên Windows khi chưa cài Maven

Các script khởi động dùng Maven Wrapper trong `backend`, tự tải Maven 3.9.11 ở lần chạy đầu. Không cần thêm `mvn` vào PATH. Để kiểm thử backend, chạy `cd backend` rồi `.\mvnw.cmd test`.

`start-web.ps1` chạy backend và frontend ở chế độ nền, đợi cả hai sẵn sàng rồi mở trình duyệt. Log đầu ra nằm ở `backend/start.log` và `frontend/start.log`; lỗi không được chuyển vào file riêng. Nếu MySQL báo `Access denied`, sửa tài khoản trong `backend/.env` trước khi chạy lại.

## PHẦN 1. Cấu trúc

| Thư mục | Nội dung |
|---|---|
| backend | Source Java, Maven, cấu hình, Flyway V1–V8, unit/API/MySQL tests |
| frontend | React, routes người dùng/kiểm định/quản trị, CSS, tests; `public/images/products` chứa ảnh fallback catalog |
| database | Khởi tạo DB, seeds và dữ liệu import được phân nhóm; xem `database/README.md` |
| scripts | Chạy bằng Bash/Git Bash hoặc PowerShell |
| tests | Postman collection Auth và danh mục API thực tế |
| docs | Năm tài liệu nguồn, trạng thái, quyết định đã phê duyệt, bàn giao và bằng chứng kiểm thử |

## PHẦN 2. Chuẩn bị

Cài **JDK 17**, **Maven 3.9+**, **Node.js 22**, **MySQL 8.0.16+ hoặc 8.4**. Các phiên bản thư viện đã khóa trong `pom.xml` và `package-lock.json`.

```bash
java -version
mvn -version
node -v
npm -v
```

Giải nén ZIP. Mở terminal tại thư mục `dh-badminton-store` có file README này.

## PHẦN 3. Tạo database

Mở MySQL Workbench, chạy `database/00_create_database.sql`. Tạo user ứng dụng bằng mật khẩu của bạn:

```sql
CREATE USER 'badminton_app'@'localhost' IDENTIFIED BY 'THAY_MAT_KHAU_LOCAL';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES, DROP
ON badminton_ecommerce.* TO 'badminton_app'@'localhost';
```

Quyền DDL dùng cho Flyway trên database phát triển. Không chạy tay SQL migration V1–V6; Spring Boot sẽ áp dụng đúng thứ tự.

## PHẦN 4. Cấu hình backend

Sao chép `backend/.env.example` thành `backend/.env`. Điền DB_USERNAME và DB_PASSWORD. Nếu cổng MySQL khác 3306, sửa DB_URL.

Đăng ký không gửi OTP email/SMS và không cần tài khoản nhà cung cấp. Người dùng có thể đăng nhập ngay bằng email hoặc số điện thoại cùng mật khẩu đã tạo. Flyway V5 cho phép đăng ký bằng số điện thoại; V6 xóa các cột trạng thái xác thực liên hệ đã không còn dùng. Cả hai migration tự chạy khi backend khởi động.

V5 tạo unique index cho số điện thoại. Các chuỗi điện thoại rỗng được chuyển thành NULL tự động. Trước khi khởi động backend trên database đã có dữ liệu, kiểm tra số thật bị trùng bằng câu SQL này và xử lý trùng nếu có kết quả:

```sql
SELECT phone, COUNT(*) AS total
FROM users
WHERE phone IS NOT NULL AND TRIM(phone) <> ''
GROUP BY phone
HAVING COUNT(*) > 1;
```

Tạo JWT_SECRET bằng Node (dùng được trên Windows, macOS, Linux):

```bash
node -e "console.log(require('node:crypto').randomBytes(48).toString('base64'))"
```

Dán kết quả vào JWT_SECRET trong `backend/.env`. Không thêm dấu xuống dòng vào giá trị.

Chạy với **Windows PowerShell**, từ thư mục gốc:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-backend.ps1
```

Hoặc **Bash/Git Bash**:

```bash
bash scripts/start-backend.sh
```

Script nạp `.env`; Spring Boot/IDE không tự đọc file này. Nếu chạy từ IDE, đặt các environment variables tương ứng trong Run Configuration.

Backend: http://localhost:8080/api
Swagger: http://localhost:8080/swagger-ui.html

Lần đầu Maven cần Internet để tải dependencies. Flyway tạo bảng, Hibernate chỉ `validate`, không `update` schema.

## PHẦN 5. Dữ liệu mẫu và tài khoản

Sau khi backend khởi động thành công, chạy `database/01_demo_catalog.sql` trong Workbench. Script tạo 3 vợt **giả lập** với ba nhóm thông số để thử gợi ý; chạy lại không tạo bản sao. Không có tài khoản/mật khẩu mặc định và không có hình ảnh giả mạo sản phẩm thật.

Ảnh cho catalog 150 sản phẩm được tổ chức trong `database/seeds/product-images` và `database/imports/product-images`; 150 SVG fallback được phục vụ từ `frontend/public/images/products`. Sau khi seed catalog, có thể chạy `database/seeds/product-images/seed_150_anh_mysql.sql`. Script chỉ điền thumbnail còn trống và thêm ảnh chính nếu sản phẩm chưa có ảnh chính. Xem `database/README.md` về cấu trúc, thứ tự import và giới hạn của ảnh tham khảo.

Bạn đăng ký tài khoản USER trên giao diện. Để thử ADMIN và INSPECTOR, đăng ký thêm hai tài khoản riêng. Trong Workbench trên database local, thay email bằng tài khoản của bạn rồi chạy:

```sql
USE badminton_ecommerce;
UPDATE users SET role='ADMIN' WHERE email='email_quan_tri_cua_ban';
UPDATE users SET role='INSPECTOR' WHERE email='email_kiem_dinh_cua_ban';
```

Đăng xuất và đăng nhập lại. BUYER và SELLER vẫn là nghiệp vụ của USER, không tạo role mới.

## PHẦN 6. Chạy frontend

Mở terminal **thứ hai** tại thư mục gốc. Sao chép `frontend/.env.example` thành `frontend/.env`.

PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-frontend.ps1
```

Bash/Git Bash:

```bash
bash scripts/start-frontend.sh
```

Script tự chạy `npm ci` khi chưa có node_modules. Mở **http://localhost:5173**.

Nếu dùng `127.0.0.1` thay `localhost`, cập nhật CORS_ALLOWED_ORIGINS trong backend/.env cho đúng origin rồi khởi động lại backend.

### Chạy nhanh local

Cài JDK 17, Node.js 22, MySQL 8.0+ và Python 3.11; cấu hình `backend/.env`, cài dependencies cho AI một lần, rồi bật MySQL. Trong PowerShell, tại thư mục gốc dự án chạy:

Không cần cài Python hoặc model.

Script khởi động backend và frontend, chờ chúng sẵn sàng rồi mở `http://localhost:5173`. Lần đầu có thể cần thời gian để Maven/npm tải dependencies. Nếu service nào dừng, xem log tương ứng trong `ai-research`, `backend` hoặc `frontend`. Cách này chạy đầy đủ tính năng; GitHub Pages chỉ chạy phần giao diện.

## PHẦN 6A. Deploy giao diện lên GitHub Pages

Workflow `.github/workflows/deploy-pages.yml` build và deploy frontend tự động khi push lên nhánh `main`. Trong GitHub, mở **Settings → Pages**, chọn **GitHub Actions** làm Build and deployment source. Với repo `DHBadmintonStore`, website có URL dạng `https://hiepnguyndev.github.io/DHBadmintonStore/`.

Để frontend gọi được API từ GitHub Pages, cần deploy backend và MySQL ở một host khác. Thêm repository variable `VITE_API_BASE_URL` trong **Settings → Secrets and variables → Actions → Variables**, đặt giá trị là URL API đầy đủ, ví dụ `https://api.ten-mien-cua-ban/api`, rồi push lại để build. Backend cần cho phép origin Pages trong `CORS_ALLOWED_ORIGINS`. Nếu chưa cấu hình API, trang giao diện vẫn mở nhưng các chức năng cần dữ liệu/đăng nhập sẽ không hoạt động.

Để dùng custom domain trên GitHub Pages, cấu hình domain tại **Settings → Pages** và DNS theo hướng dẫn GitHub. Nếu deploy dưới custom domain ở root, đặt repository variable `VITE_BASE_PATH` là `/` trước khi build; đường dẫn mặc định `/DHBadmintonStore/` dành cho URL project Pages ở trên. GitHub Pages không chạy Spring Boot, không lưu ảnh upload bền vững và không host MySQL.

## PHẦN 7. Luồng chạy thử

1. USER: đăng ký → đăng nhập → thêm địa chỉ → Cửa hàng → chọn vợt → giỏ hàng → thanh toán COD hoặc chuyển khoản mô phỏng → xem đơn.
2. Gợi ý: mở `/player-profile`, chọn trình độ/lối chơi/cổ tay/trọng lượng/ngân sách → xem `/recommendations`. Điểm và lý do được tính trên backend.
3. ADMIN: `/admin/products` để thêm/sửa sản phẩm và ảnh; `/admin/orders` để chuyển trạng thái lần lượt đến COMPLETED. Người mua chỉ đánh giá khi đơn hoàn thành.
4. Người bán USER: `/sell` → lưu bản nháp → tải ảnh → xem tin → gửi duyệt. ADMIN duyệt ở `/admin/listings`.
5. Người mua USER khác: chọn tin AVAILABLE → tạo đơn → thanh toán Escrow **mô phỏng**. Người bán mở `/market-orders` → tab Tôi bán → tạo vận đơn.
6. ADMIN mở `/admin/shipments`, nhập ID giao dịch Marketplace trong URL chi tiết đơn, chuyển PICKED_UP → IN_TRANSIT → DELIVERED. Người mua xác nhận nhận hàng để hoàn tất và giải ngân mô phỏng.
7. Tranh chấp: người mua báo chưa nhận hàng khi SHIPPING, hoặc người tham gia báo vấn đề khi DELIVERED → tải bằng chứng → ADMIN xem và xử lý tại `/admin/disputes`. Tiền không được giải ngân khi DISPUTED.
8. Kiểm định: người bán yêu cầu từ tin đăng → INSPECTOR nhận, bắt đầu và lập báo cáo tại `/inspector`. Sửa thông tin/ảnh liên quan sẽ làm mất hiệu lực badge cũ.

Ảnh local lưu trong `backend/var/uploads/` khi chạy qua script, không nằm trong source ZIP. Ảnh sản phẩm/tin dùng `/media/`; bằng chứng tranh chấp chỉ được trả qua API chi tiết có kiểm tra quyền. Giữ thư mục upload nếu cần giữ ảnh qua các lần chạy.

Token chỉ giữ trong bộ nhớ của tab; tải lại trang phải đăng nhập lại. Không có refresh token hoặc thu hồi token giả. Phí vận chuyển/phí nền tảng local mặc định 0, được tính phía server; không có thanh toán ngân hàng thật.

## PHẦN 8. Kiểm thử

Backend unit/MockMvc, không cần MySQL:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm ci
npm test
npm run build
```

MySQL integration: tạo database riêng **badminton_ecommerce_test**, cấp quyền user test trên database này. Đặt RUN_MYSQL_TESTS=true, TEST_DB_URL, TEST_DB_USERNAME, TEST_DB_PASSWORD, TEST_JWT_SECRET (Base64 >=32 byte).

Bash:

```bash
export RUN_MYSQL_TESTS=true
export TEST_DB_URL='jdbc:mysql://localhost:3306/badminton_ecommerce_test?connectionTimeZone=UTC'
export TEST_DB_USERNAME='badminton_test'
export TEST_DB_PASSWORD='MAT_KHAU_TEST'
export TEST_JWT_SECRET='CHUOI_BASE64_CUA_BAN'
cd backend
mvn test
```

PowerShell dùng `$env:TEN_BIEN='gia_tri'` thay `export`. Test tạo dữ liệu riêng; không trỏ test vào database chứa dữ liệu cần giữ. Khi không bật RUN_MYSQL_TESTS, các test MySQL bị skip có chủ đích.

Playwright Auth và Commerce E2E có source trong `frontend/e2e`; cần backend/frontend/database test đang chạy và đã nạp SQL demo. Commerce test đăng ký user riêng và dùng ba sản phẩm demo:

```bash
cd frontend
npx playwright install chromium
# Bash: RUN_E2E=true npm run test:e2e
# PowerShell: $env:RUN_E2E='true'; npm run test:e2e
```

Xem kết quả thực tế và giới hạn trong `docs/VALIDATION.md`. Có test không đồng nghĩa đã kiểm chứng mọi luồng trên trình duyệt.

## PHẦN 9. Chức năng bổ sung đã triển khai

- Người bán mở **Tin của tôi → Chi tiết tin → Ước tính giá dụng cụ**, chọn tình trạng phụ kiện và mức sửa chữa. Backend tính giá tham khảo và lưu lịch sử; report hợp lệ được ưu tiên. Nếu khai báo sửa chữa mâu thuẫn report, API từ chối.
- ADMIN mở **Quản trị → Sản phẩm** để ẩn/kích hoạt Category hoặc Brand. Public chỉ liệt kê ACTIVE; Product/Listing hiện hữu giữ trạng thái riêng. Flyway V4 nâng cấp status NULL thành ACTIVE, không sửa migration cũ.
- Buyer mở chi tiết Marketplace Order chưa thanh toán để **Hủy đơn chưa thanh toán**. Backend kiểm tra ownership, trạng thái đơn/escrow và ghi nguyên nhân hủy.
- ADMIN mở **Quản trị → Nhật ký hệ thống** để xem Audit Log phân trang.
- API và ví dụ request: `tests/API_CATALOG.md`, `tests/D15_D17.http`; lịch sử quyết định: `docs/04_CHANGELOG.md` và `docs/DESIGN_DECISIONS_PENDING.md` (tên file giữ để bảo toàn tham chiếu, trạng thái hiện tại là APPROVED).

Chưa có Cloudinary/Object Storage production, domain/HTTPS hoặc triển khai production. Local upload phục vụ chạy local. Kiểm thử đã chạy và các giới hạn nằm trong `docs/VALIDATION.md`.

## PHẦN 10. Lỗi thường gặp

| Lỗi | Cách kiểm tra |
|---|---|
| Không tìm thấy mvn/java/node | Cài công cụ, thêm PATH và mở terminal mới |
| Access denied for user | DB_USERNAME/DB_PASSWORD và quyền database |
| Communications link failure | MySQL service, host và port |
| JWT Base64 không hợp lệ | Tạo lại bằng lệnh Node ở PHẦN 4 |
| Cửa hàng trống | Chạy SQL demo sau khi Flyway tạo bảng hoặc nhập sản phẩm bằng ADMIN |
| Giỏ hàng/tạo hồ sơ bị 403 | Đăng nhập USER; ADMIN/INSPECTOR không thay thế vai trò nghiệp vụ người mua |
| Không thể thêm ảnh | PNG/JPEG <=5 MB, <=24 megapixel, tối đa 10 ảnh/đối tượng, đúng quyền và trạng thái |
| Schema validation/Flyway checksum | Không sửa migration đã áp dụng; dùng database local sạch cho bản source này hoặc lập migration nâng cấp có kiểm soát |

Không gộp Product với UsedListing hoặc Shop Order với MarketplaceOrder. Đọc năm tài liệu nguồn trước khi phát triển tiếp và cập nhật Implementation Status sau mỗi milestone.

## Thanh toán shop

COD, chuyển khoản ngân hàng và VietQR: xem [cấu hình, xác minh SePay và đối soát](docs/PAYMENTS.md). Mặc định chỉ COD được bật; chuyển khoản cần tài khoản nhận và webhook SePay có HMAC để tự xác minh tiền vào. Flyway V13 nâng cấp CSDL hiện tại mà không xóa dữ liệu.

## Chatbot tư vấn theo 21 intent

Chatbot PhoBERT hỗ trợ nhiều danh mục, giữ điều kiện nhiều lượt, xem thêm không
lặp, so sánh, kiểm tra tồn kho biến thể và tạo combo theo ngân sách. Xem
[phạm vi triển khai, giới hạn dữ liệu và cách kiểm thử](docs/CHATBOT_REQUIREMENTS.md).
Sau khi cập nhật mã cần khởi động lại AI service và backend; không cần migration
CSDL cho thay đổi chatbot này.
