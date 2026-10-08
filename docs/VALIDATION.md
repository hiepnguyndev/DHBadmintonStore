# Validation — bản bàn giao local 2026-10-08

## PHẦN 1. Kết quả

| Phạm vi | Kết quả | Ghi chú |
|---|---|---|
| Backend Java 17 / Maven | PASS | 66 tests, 0 failures, 0 errors, 0 skipped khi bật MySQL |
| MySQL thật 8.4.6 | PASS | Database biệt lập; Flyway V1–V4, Hibernate validate; 19 integration test methods |
| MatchScore / Condition / Reputation | PASS | Matrix Flex D13, biên ngân sách, score 0–100, grade rounding, D14 denominator/attribution |
| Frontend component tests | PASS | 9 tests qua Vitest/Testing Library |
| Frontend production build | PASS | Vite; có cảnh báo kích thước bundle, không phải lỗi build |
| Spring Boot HTTP thật | PASS | Register/login/JWT/profile/recommendations/addresses/cart/authorization/CORS |
| Browser E2E Chromium | PASS: 3 tests | Định giá/lịch sử người bán, Auth và luồng PlayerProfile → Recommendation → Favorite → Cart → Checkout → Cancel; không đại diện toàn bộ màn hình quản trị/Marketplace |
| Production deployment | CHƯA CHẠY | Không có cấu hình hạ tầng/Cloudinary production |

## PHẦN 2. Các ca nghiệp vụ đã kiểm tra trên MySQL

- Catalog public và chặn USER tạo Product; từ chối giá do client chèn vào Cart.
- PlayerProfile và Match Score trả từ backend; gợi ý đúng thứ tự điểm.
- Shop checkout lấy giá server, giảm tồn kho; hủy đúng quyền, hoàn tồn kho một lần; double cancel bị chặn.
- Marketplace mua/gửi/thanh toán/giao/xác nhận/đánh giá; chặn người ngoài, double pay, confirm sớm, double confirm và double rating.
- Hai request mua cùng Listing: một thành công, một conflict; database chỉ có một đơn mới.
- Cancel trước thanh toán qua service và API, Listing mua lại được, giữ lịch sử đơn hủy.
- Ba resolution dispute MVP, chặn release khi disputed, chặn resolution Post-MVP, chặn resolution lặp, FULL_REFUND đưa Listing về HIDDEN.
- Inspection: một active request, quyền inspector, report, badge có hiệu lực rồi mất hiệu lực khi thông tin liên quan thay đổi.
- Admin statistics, khóa/mở user; token bị từ chối ngay khi user bị khóa.
- SQL demo chạy hai lần chỉ có ba Product demo.
- Review chỉ sau COMPLETED và đúng OrderItem/owner; duplicate review bị chặn. Favorite thêm lặp không tạo trùng và xóa được.
- Notification read/read-all đúng chủ sở hữu, không đổi dữ liệu của người khác.
- Bằng chứng tranh chấp không truy cập được qua public media; từ chối file giả ảnh và upload sau resolution.
- Pay và confirm-received đồng thời chỉ một lần thành công; chỉ một audit xác nhận hoàn tất.
- Timeout ghi SYSTEM/PAYMENT_TIMEOUT và không hủy đơn đã thanh toán.
- Hai Shop checkout cạnh tranh sản phẩm tồn kho 1: một thành công, một conflict, tồn kho không âm.
- ValuationCalculator đủ tuổi/tình trạng/bảo hành/phụ kiện/sửa chữa/nhu cầu và dải giá; không tự mặc định dữ liệu phụ kiện/sửa chữa thiếu.

## PHẦN 3. Giới hạn bằng chứng

Integration API dùng MockMvc với Service/Repository/Flyway/MySQL thật, không mock database. HTTP smoke khởi động Spring Boot jar thật trên loopback và gọi bằng HTTP riêng. Component tests dùng mock API; không thay thế browser E2E.

Chromium E2E đã chạy với API và MySQL thật, không mock response. Có một lần locator select không tìm được label; đã sửa locator theo combobox accessible name và chạy lại: 3/3 PASS.

Chưa kiểm thử tải, migration nâng cấp database có dữ liệu thực, toàn bộ upload error cases, các race resolve/inspection và toàn bộ màn hình người dùng cuối. Không suy rộng PASS thành toàn hệ thống DONE.

Môi trường MySQL kiểm thử là tạm thời và đã dừng sau test. Không có database/tài khoản test runtime hoặc secret trong ZIP. Log/tổng hợp kết quả được đưa vào thư mục evidence, không đưa token/password vào tài liệu.

## PHẦN 4. D15–D17

- Valuation: thiếu classification bị 400, sai chủ bị 403; reference fallback, match duy nhất không phân biệt hoa thường/trim, giá sale từ server, ambiguous fallback và loại Product INACTIVE; snapshot cũ giữ giá. Report hợp lệ ưu tiên score, repair conflict bị 409; sửa Listing làm report hết hiệu lực.
- Category/Brand: tạo ACTIVE, public lọc INACTIVE, ADMIN thấy đầy đủ; ẩn không cascade Product/Listing; enum sai bị 400, USER sửa bị 403.
- Cancel API: chỉ Buyer sở hữu, trước thanh toán; double cancel và cancel sau pay bị 409; attribution do server. Audit: USER bị 403, anonymous bị 401, ADMIN phân trang được; size 101 bị 400.
- Master DOCX đã bổ sung quyết định D15–D17 và render 783 trang; kiểm tra trực quan trang bổ sung. Nội dung/bố cục 782 trang gốc được giữ, không tuyên bố đã rà soát lại toàn bộ bản gốc.
