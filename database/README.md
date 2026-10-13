# CSDL DH Badminton Store

MySQL 8.0+, UTF-8 (`utf8mb4`), schema ứng dụng `badminton_ecommerce`.
Flyway V1-V13 là nguồn cấu trúc chính thức. Các file SQL trọn gói được xuất từ
CSDL kiểm thử đã chạy đủ migration, gồm khóa ngoại, index, CHECK, lịch sử
Flyway với checksum thật và trọng số gợi ý mặc định. Không kèm tài khoản hoặc mật khẩu.

## Các file SQL

| File | Mục đích |
| --- | --- |
| `00_create_database.sql` | Tạo/chọn CSDL, giữ nguyên dữ liệu và quyền đăng nhập MySQL. |
| `03_schema_complete.sql` | Toàn bộ cấu trúc V1-V13. Chọn một CSDL trống trước khi chạy. |
| `04_shopvnb_200.sql` | Nhập 200 sản phẩm vào CSDL đã có V12. Có thể chạy lại. |
| `05_project_complete.sql` | Cài mới một lần: tạo CSDL, cấu trúc đầy đủ và 200 sản phẩm. |
| `templates/shopvnb_import.sql` | Mẫu sinh file nhập, không chạy trực tiếp. |

`01_demo_catalog.sql`, `02_reset_data.sql` và `seeds/product-images/` là công cụ
cũ, không nằm trong quy trình nhập mới. **Không chạy `02_reset_data.sql` để cập
nhật sản phẩm: file đó xóa cả tài khoản và dữ liệu nghiệp vụ.**

## Cập nhật dự án đang chạy

Khởi động backend để Flyway tự áp dụng V12. Sau đó, từ thư mục
`DH Badminton Store`, chạy:

```powershell
node .\scripts\database.mjs inspect
node .\scripts\database.mjs import
```

Công cụ đọc kết nối từ `backend/.env`, tự sao lưu trước khi nhập vào
`backend/var/backups/`, rồi kiểm tra tài khoản, mật khẩu, vai trò, đơn hàng và
bài viết shop vẫn giữ nguyên. Mật khẩu MySQL không được đưa vào tham số dòng lệnh.
Chỉ cho phép CSDL local `badminton_ecommerce`. Thư mục backup chứa dữ liệu riêng
tư và đã được Git bỏ qua.

Cũng có thể chọn `badminton_ecommerce` trong MySQL Workbench, mở và chạy toàn bộ
`04_shopvnb_200.sql` sau khi sao lưu, bật dừng thực thi khi có lỗi. File nhập chỉ
cần quyền đọc/ghi bảng, không cần CREATE ROUTINE hoặc CREATE TEMPORARY TABLES.
Công cụ Node đóng kết nối khi có lỗi để rollback; nếu chạy thủ công thì thực hiện
`ROLLBACK;` khi gặp lỗi, đóng kết nối rồi sửa nguyên nhân và chạy lại toàn bộ file.
Không dùng tùy chọn bỏ qua lỗi (`--force`).

Không chạy file cài mới `05_project_complete.sql` trên CSDL đang có dữ liệu.

## Cài mới

Mở `05_project_complete.sql` bằng MySQL Workbench và chạy toàn bộ trên MySQL 8.0+.
File yêu cầu CSDL `badminton_ecommerce` trống và quyền tạo routine. Nó không
xóa bảng hoặc reset dữ liệu nếu phát hiện CSDL đã có bảng. Sau đó cấu hình
`backend/.env` và chạy `start-web.ps1`; Flyway sẽ kiểm tra lịch sử đã cài.

Nếu dùng một tên CSDL khác: tạo/chọn CSDL trống, chạy `03_schema_complete.sql`,
rồi `04_shopvnb_200.sql`; đặt `DB_URL` tương ứng trước khi chạy backend.

Tài khoản admin trên CSDL hiện có được giữ nguyên. Bản cài mới không có admin
mặc định. Tạo/đặt lại tài khoản bằng mật khẩu tự chọn trong terminal tương tác:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\set-admin.ps1 -Email admin@gmail.com
```

Script dùng BCrypt và không lưu mật khẩu rõ trong SQL. Nếu email đã tồn tại,
script đổi mật khẩu và đặt vai trò ADMIN cho đúng tài khoản đó. Tài khoản MySQL
trong `.env` là tài khoản kết nối CSDL, khác tài khoản đăng nhập web.

## Dữ liệu 200 sản phẩm

Nguồn: `../CSDL/san_pham_shopvnb_200.json`, bộ dữ liệu người dùng cung cấp ngày
13/10/2026. Không chạy trực tiếp `CSDL/schema.sql` hoặc `du_lieu_shopvnb_200.sql`
vào ứng dụng: các file nguồn dùng cấu trúc staging khác (`dh_badminton_store`).

- 200 sản phẩm: 100 vợt, 24 giày, 40 trang phục, 36 túi/balo/phụ kiện; 14 thương hiệu.
- Tồn kho ban đầu **20/sản phẩm**, tổng **4.000**, theo yêu cầu chủ shop.
- 198 sản phẩm có giá được đặt ACTIVE; 2 sản phẩm VS Blade 9200 Vàng và VS Blade
  9100 Xanh Dương thiếu giá được đặt INACTIVE. Giá 0 chỉ là giá trị chờ bổ sung;
  admin cần nhập giá và chọn ACTIVE trước khi mở bán.
- Dùng giá thấp nhất trong bộ nguồn làm giá cơ sở. Khoảng giá nguồn không phải
  mức giảm giá. `sale_price` để NULL, `promotion_published` FALSE; admin chọn và
  đăng ưu đãi trong `/admin/promotions`.
- 191 URL ảnh gốc cho 190 sản phẩm, 10 sản phẩm chưa có ảnh. Không tạo ảnh giả hoặc
  thay bằng ảnh của sản phẩm khác. Ảnh CDN cần Internet và có thể thay đổi từ nguồn.
- Màu, size, giá cao nhất, mã nguồn, thông số và tình trạng nguồn được giữ đầy đủ
  trong `catalog_import_sources.raw_record`; mô tả sản phẩm có thông tin nguồn.
- Chưa tạo biến thể màu-size/SKU vì nguồn không xác minh tổ hợp. Không tự gán
  trình độ hoặc lối chơi. `racket_specifications` chỉ được bổ sung sau khi admin
  xác minh đủ trường bắt buộc; gợi ý từ hồ sơ phụ thuộc các thông số này.

File nhập nhận diện bằng SHA-256 của URL nguồn. Chạy lại không tạo trùng và
không ghi đè tồn kho, giá, ưu đãi, trạng thái hoặc nội dung/ảnh admin đã nhập.
Chỉ bổ sung mô tả/thumbnail đang trống và cập nhật bản ghi nguồn. Các giá trị tồn
kho không được đặt lại về 20 sau khi bán hàng. Giao dịch và khóa nhập ngăn dữ
liệu dở dang; bảng nguồn liên kết đến sản phẩm bằng khóa ngoại.

## Kiểm tra và tái tạo bộ SQL

```sql
SELECT COUNT(*) AS so_san_pham, SUM(stock) AS tong_ton_kho,
       SUM(status = 'ACTIVE') AS dang_ban,
       SUM(status = 'INACTIVE') AS chua_mo_ban
FROM products p JOIN catalog_import_sources s USING (product_id);

SELECT p.product_id, p.product_name, p.price, p.stock, p.status
FROM products p JOIN catalog_import_sources s USING (product_id)
WHERE p.price = 0;
```

Sinh lại file nhập từ JSON gốc:

```powershell
node .\scripts\generate-shopvnb-sql.mjs
```

Kiểm thử SQL và sinh lại `03`/`05` từ migration hiện tại (Java 17+, Node.js,
Maven, MySQL Server/client; cổng 3307 phải trống):

```powershell
Push-Location backend
mvn dependency:build-classpath -Dmdep.outputFile=target/admin-tool.classpath
Pop-Location
node .\scripts\test-catalog-sql.mjs --backend-tests
```

Kiểm thử tự tạo MySQL riêng trên 3307, kiểm tra rollback, Unicode, dữ liệu nguồn,
nhập lặp, bảo toàn chỉnh sửa, khôi phục SQL, checksum Flyway, cài mới đầy đủ và
toàn bộ backend. Server kiểm thử tự dừng; đường dẫn dữ liệu tạm được in cuối.
Không trỏ bộ kiểm thử vào CSDL chính. Có thể đặt `MYSQL_BIN` nếu MySQL không ở
`C:/Program Files/MySQL/MySQL Server 8.0/bin`.
