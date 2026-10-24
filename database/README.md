# CSDL DH Badminton Store

## Chuyển CSDL sang thiết bị khác

Để giữ nguyên sản phẩm đã chỉnh sửa, tài khoản admin/user, mật khẩu đã mã hóa,
đơn hàng và các dữ liệu khác, cần chuyển toàn bộ CSDL. Chỉ chép các file trong
`database` sang máy mới sẽ không chuyển được dữ liệu người dùng.

### 1. Trên máy cũ: tạo bản sao lưu

Đảm bảo MySQL đang chạy. Mở PowerShell trong thư mục dự án và chạy:

```powershell
mysqldump --user=root --password --host=localhost --port=3306 --single-transaction --routines --triggers --default-character-set=utf8mb4 badminton_ecommerce --result-file=badminton_ecommerce_backup.sql
```

Nhập mật khẩu MySQL khi được hỏi. Nếu tài khoản MySQL của bạn không phải `root`,
thay `root` bằng tài khoản có quyền đọc toàn bộ schema. Chép file backup sang máy
mới bằng USB hoặc nơi lưu trữ riêng tư; file này chứa thông tin tài khoản và khách hàng.

### 2. Trên máy mới: cài môi trường và chuẩn bị MySQL

1. Cài MySQL 8.0+, Java 17+ và Node.js; chép/clonar dự án sang máy mới.
2. Khởi động MySQL và tạo schema rỗng:

```powershell
mysql --user=root --password --host=localhost --port=3306 -e "CREATE DATABASE badminton_ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
```

3. Khôi phục toàn bộ dữ liệu:

```powershell
cmd /c "mysql --user=root --password --host=localhost --port=3306 --default-character-set=utf8mb4 badminton_ecommerce < badminton_ecommerce_backup.sql"
```

Nhập mật khẩu MySQL khi được hỏi. Giữ nguyên tên schema `badminton_ecommerce` để
khớp cấu hình và các script hiện tại. Nếu MySQL nằm ở cổng khác, sửa cổng ở các
lệnh trên và trong `backend/.env`.

### 3. Cấu hình ứng dụng và kiểm tra

1. Tạo `backend/.env` từ `backend/.env.example`; điền `DB_USERNAME` và
   `DB_PASSWORD` của tài khoản MySQL trên máy mới. `DB_URL` mặc định dùng
   `localhost:3306/badminton_ecommerce`.
2. Đảm bảo tài khoản MySQL trong `.env` có quyền đọc/ghi schema. Nếu cần tạo
   riêng tài khoản kết nối ứng dụng, cấp quyền trước khi khởi động backend:

```sql
CREATE USER 'badminton_app'@'localhost' IDENTIFIED BY 'MAT_KHAU_MOI';
GRANT ALL PRIVILEGES ON badminton_ecommerce.* TO 'badminton_app'@'localhost';
```

   Dùng cùng thông tin đó trong `.env`. Không dùng mật khẩu mẫu trong file SQL.
3. Khởi động backend (hoặc `start-web.ps1`) để Flyway tự áp dụng các migration
   còn thiếu cho bản backup.
4. Nhập catalog hiện tại của dự án, để máy mới có sản phẩm mới nhất. Từ thư mục
   dự án chạy:

```powershell
node .\scripts\database.mjs import
```

   Lệnh này sao lưu CSDL trên máy mới trước khi nhập `04_shopvnb_200.sql`. File
   bổ sung các sản phẩm chưa có và giữ nguyên giá, tồn kho, khuyến mãi, trạng thái
   cùng nội dung admin đã sửa. Nó không xóa sản phẩm cũ không còn trong nguồn.
5. Chạy `node .\scripts\database.mjs inspect` để kiểm tra số sản phẩm, tài khoản,
   đơn hàng và phiên bản schema; đăng nhập thử bằng tài khoản admin cũ. Mật khẩu
   đăng nhập web được giữ nguyên trong bản sao lưu, khác với mật khẩu MySQL.

Nếu phiên bản dự án trên máy mới có migrations Flyway mới hơn bản backup, hãy sao
lưu thêm một bản trước khi chạy backend; Flyway có thể cập nhật schema lúc khởi động.
Sau khi khôi phục, nhập `04_shopvnb_200.sql` để bổ sung catalog hiện hành của dự án.
File có thể chạy lặp, giữ các giá trị thương mại đã chỉnh sửa, và không xóa sản phẩm
cũ không còn trong nguồn. Nếu muốn xóa sản phẩm cũ, cần rà soát thủ công trước.

## Quy trình dùng một file SQL để cập nhật sản phẩm

`04_shopvnb_200.sql` là file SQL duy nhất cần chạy khi nhập/cập nhật bộ sản phẩm.
Trên máy đã cài ứng dụng, backend phải được khởi động để Flyway cập nhật schema,
sau đó mở file này trong MySQL Workbench và chạy trên schema `badminton_ecommerce`.
Hoặc dùng lệnh `node .\scripts\database.mjs import` để tự sao lưu trước khi nhập.
File nhập nhận diện sản phẩm theo URL nguồn; chạy lại không nhân đôi sản phẩm và
không ghi đè giá, tồn kho, trạng thái, khuyến mãi hay nội dung admin đã chỉnh sửa.
Nó không xóa hoặc sửa tài khoản admin/user, đơn hàng hay bài viết.

File cập nhật sản phẩm không mang theo tài khoản hoặc dữ liệu cũ. Khi đổi máy,
trước tiên chuyển toàn bộ CSDL theo quy trình phía trên, sau đó chạy file cập nhật
này để bổ sung catalog hiện hành. Không dùng `05_project_complete.sql` để cập nhật
hoặc chuyển dữ liệu; file đó dành riêng cho CSDL mới, trống.

Ví dụ sao lưu/khôi phục bằng MySQL client (thay `root` bằng tài khoản MySQL phù hợp):

```powershell
mysqldump -u root -p --single-transaction --routines --triggers badminton_ecommerce > badminton_ecommerce_backup.sql
mysql -u root -p -e "CREATE DATABASE badminton_ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -u root -p badminton_ecommerce < badminton_ecommerce_backup.sql
```

Giữ file sao lưu riêng tư: file này chứa thông tin tài khoản và dữ liệu khách hàng.
Không gửi hoặc đưa file backup lên Git.

MySQL 8.0+, UTF-8 (`utf8mb4`), schema ứng dụng `badminton_ecommerce`.
Các migration trong backend là nguồn cấu trúc chính thức; bộ hiện tại có V1–V19.
Lưu ý `03_schema_complete.sql` và `05_project_complete.sql` là bản xuất cũ V1–V13,
chưa đủ để cài mới đúng phiên bản backend hiện tại. Khi chuyển thiết bị, ưu tiên
khôi phục backup toàn CSDL như quy trình bên trên.

## Các file SQL

| File | Mục đích |
| --- | --- |
| `00_create_database.sql` | Tạo/chọn CSDL, giữ nguyên dữ liệu và quyền đăng nhập MySQL. |
| `03_schema_complete.sql` | Bản cấu trúc cũ V1–V13; không dùng để cài mới bản backend hiện tại. |
| `04_shopvnb_200.sql` | Nhập 200 sản phẩm vào CSDL ứng dụng đã chạy migration V12 trở lên. Có thể chạy lại. |
| `05_project_complete.sql` | Bản cài mới cũ V1–V13; không dùng để cài mới bản backend hiện tại. |
| `templates/shopvnb_import.sql` | Mẫu sinh file nhập, không chạy trực tiếp. |

`01_demo_catalog.sql`, `02_reset_data.sql` và `seeds/product-images/` là công cụ
cũ, không nằm trong quy trình nhập mới. **Không chạy `02_reset_data.sql` để cập
nhật sản phẩm: file đó xóa cả tài khoản và dữ liệu nghiệp vụ.**

## Cập nhật dự án đang chạy

Khởi động backend để Flyway tự áp dụng các migration còn thiếu. Sau đó, từ thư mục
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
