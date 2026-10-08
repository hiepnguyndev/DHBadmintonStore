# Bàn giao source local — 2026-10-08

## PHẦN 1. Bản bàn giao

Source hiện tại bao phủ backend và giao diện các luồng Shop, PlayerProfile/Recommendation, Marketplace, Escrow mô phỏng, Shipment, Inspection, Dispute, Rating/Reputation, Notification, Admin và Statistics. Đây là bản chạy local để tiếp tục kiểm thử; chưa phải tuyên bố toàn hệ thống DONE.

Cấu trúc và hướng dẫn chạy nằm trong README.md. Source ZIP không chứa node_modules, target, .env thật, ảnh upload, tài khoản/mật khẩu hoặc database runtime. Maven/npm tải dependencies ở lần chạy đầu.

## PHẦN 2. Quyết định đã tuân thủ

- Giữ React, Spring Boot, MySQL, REST/JWT, JPA/Hibernate và Modular Monolith.
- Không thêm Refresh Token, Promotion, Microservices, Kafka, Kubernetes hoặc CQRS.
- Tách Product/UsedListing và Shop Order/MarketplaceOrder; BUYER/SELLER không là system role.
- Giá, tổng tiền, score, trạng thái và uy tín tính/kiểm tra trên backend.
- Khóa Listing rồi MarketplaceOrder theo thứ tự nhất quán; cancel Shop hoàn tồn kho một lần; giải quyết Dispute đúng ba phương án MVP.
- Badge hết hiệu lực khi sửa dữ liệu liên quan; không xóa report lịch sử.

## PHẦN 3. Điểm cần tiếp tục

1. D15–D17 đã được phê duyệt và triển khai: Valuation, Category/Brand, cancel Marketplace và Audit. Tài liệu nguồn và V4 đã đồng bộ.
2. Đã chạy 3 E2E Chromium cho Auth, luồng mua Shop/gợi ý/yêu thích và định giá/lịch sử người bán. Tiếp tục các workflow trình duyệt Marketplace, INSPECTOR, ADMIN; kiểm tra responsive và accessibility.
3. Đã bổ sung test concurrency thanh toán/confirm và Shop checkout, timeout, bằng chứng, Review/Favorite/Notification. Còn cần kiểm thử race resolve/inspection và các luồng màn hình quản trị.
4. Tối ưu phân trang/aggregation của Reputation khi dữ liệu lớn; MVP chưa có cache và batch recommendation.
5. Production cần Cloudinary/Object Storage, cấu hình secret/HTTPS/backup và kiểm thử triển khai riêng. Chưa thực hiện deploy.

Sau mỗi milestone phải cập nhật 03_IMPLEMENTATION_STATUS.md. Chỉ cập nhật 04_CHANGELOG.md với quyết định kiến trúc đã được duyệt.
