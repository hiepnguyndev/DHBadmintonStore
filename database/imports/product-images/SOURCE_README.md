# DH Badminton Store – 150 hình hiển thị

- Số sản phẩm: 150.
- Ảnh tham khảo đúng *tên mẫu* (URL bên thứ ba, CHƯA kiểm chứng bản quyền/cùng màu): **47**.
- Hình minh họa thay ảnh: **103**. ĐÂY KHÔNG PHẢI ẢNH CHỤP SẢN PHẨM.
- Có đúng 150 SVG minh họa riêng để hiển thị khi ảnh từ bên ngoài hỏng.

## Cách xem trên máy

1. Giải nén toàn bộ ZIP.
2. Mở `xem_150_anh.html` bằng Chrome/Edge.
3. Tìm theo danh mục hoặc SKU.

## React

Copy thư mục `frontend/public/images/products/` vào `public/images/products/` của React.
Đường dẫn placeholder như `/images/products/DH-SEED-0043.svg` sẽ hoạt động với Vite sau khi phục vụ qua web server.

Ví dụ component: `<img src={product.thumbnailUrl} onError={e => {e.currentTarget.onerror=null;e.currentTarget.src='/images/products/'+product.skuDemo+'.svg'}} />`
Lưu ý: nếu API chưa cung cấp `skuDemo`, mapping bằng `slug` sang CSV/JSON; hoặc đặt fallback URL theo sản phẩm trong backend.

## MySQL

Sau khi nhập 150 sản phẩm gốc, chạy `seed_150_anh_mysql.sql` (database `badminton_ecommerce`, bảng `products`, `product_images`).
Script giữ ảnh có sẵn, không xóa hoặc thay đổi bản ghi đã có ảnh. Nó thêm ảnh cho sản phẩm đang trống.

## QUAN TRỌNG

- Các URL bên ngoài có thể không tải trong tương lai / bị chặn hotlink / cần xin phép sử dụng.
- Đối với website kinh doanh thật, phải thay toàn bộ ảnh minh họa bằng ảnh chụp thật của từng mẫu/màu có quyền sử dụng.
- Không dùng ảnh đại diện vợt/giày/túi để suy ra màu đang bán; dữ liệu màu trong bộ catalog là DEMO.
- 150 SVG là hình minh họa cho mục đích thử giao diện chứ không phải ảnh sản phẩm thật.
- Không phải 851 ảnh cho từng biến thể; biến thể màu/size hiện dùng chung thumbnail theo sản phẩm gốc.
