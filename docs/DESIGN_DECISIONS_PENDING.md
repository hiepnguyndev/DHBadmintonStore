# Quyết định đã phê duyệt — D15–D17


## PHẦN bổ sung — quyết định D15–D17, phê duyệt 2026-10-08

Người dùng xác nhận “Ok thực hiện đi” sau đề nghị áp dụng cả ba phương án. Các quyết định dưới đây thay thế phần chưa xác định tương ứng; D01–D14 và các module khác giữ nguyên.

### CHG-015 / D15 — Valuation (APPROVED)

Thiết kế cũ thiếu dữ liệu phụ kiện/sửa chữa và tiêu chí matching. POST /api/valuations nhận listingId, accessoryCondition (FULL/PARTIAL/MANY_MISSING), repairLevel (NONE/MINOR/STRUCTURAL); hai phân loại bắt buộc. Backend ánh xạ 1.03/1.00/0.97 và 1.00/0.90/0.70. Report kiểm định hợp lệ ưu tiên condition score/grade; repairDetected và repairLevel phải nhất quán. Khi không có report hợp lệ dùng grade người bán; không tái dùng score của report đã vô hiệu. Chỉ match Product ACTIVE cùng brandId, tên sau trim không phân biệt hoa thường, duy nhất một kết quả. Dùng effectivePrice của backend; 0 hoặc nhiều kết quả dùng originalPrice. Không fuzzy matching. Ngày hết bảo hành ưu tiên; không rõ dùng 0.97, hết hạn 1.00. Snapshot lưu factor, nguồn và phân loại trong note, không thay lịch sử. Ảnh hưởng DTO/service/UI/tests, không thêm Entity hay relationship.

### CHG-016 / D16 — Category/Brand (APPROVED)

Status cũ nullable chưa có tập giá trị. Chốt ACTIVE/INACTIVE; tạo ACTIVE, update cho phép hai giá trị; Brand DELETE là INACTIVE, không hard delete. Public chỉ xem ACTIVE; ADMIN xem tất cả. Không cascade trạng thái sang Product/UsedListing. Migration V4 backfill NULL thành ACTIVE, đặt default/NOT NULL/check; không sửa migration đã áp dụng. Ảnh hưởng enum/DTO/service/UI/tests.

### CHG-017 / D17 — API bổ trợ (APPROVED)

Mở POST /api/market-orders/{id}/cancel cho Buyer sở hữu, chỉ PENDING_PAYMENT + WAITING_PAYMENT; backend gán BUYER/BUYER_CHANGED_MIND. Mở GET /api/admin/audit-logs?page=0&size=20 chỉ ADMIN, size tối đa 100. Không thay state machine hoặc quyền release/refund. Ảnh hưởng controller/UI/API tests.

Rollback: thay đổi quyết định cần CHANGE mới; không tự phục hồi quy tắc cũ. Cập nhật Implementation Status theo kết quả kiểm thử, không tự đánh dấu toàn bộ DONE.

## Lịch sử đề xuất trước phê duyệt (không còn là trạng thái hiện tại)

# Các quyết định thiết kế cần chốt — DRAFT, chưa phê duyệt

## PHẦN 1. Đầu vào Valuation

**Thiết kế hiện tại:** D04 quy định hệ số tuổi, tình trạng, bảo hành, phụ kiện, sửa chữa và nhu cầu; Valuation lưu snapshot. UsedListing/UsedRacketSpecification chưa có trường cấu trúc về tình trạng phụ kiện và mức sửa chữa. repairHistory là mô tả văn bản. POST /api/valuations hiện lấy listingId.

**Vấn đề:** Không thể suy hệ số phụ kiện chính xác từ dữ liệu hiện có; tự dùng 1.00 hoặc suy từ mô tả sẽ thay quy tắc. Chưa có tiêu chí xác định “model tương tự” ở các bước chọn giá tham chiếu.

**Đề xuất để duyệt:** Bổ sung input `accessoryCondition` (FULL, PARTIAL, MANY_MISSING) và `repairLevel` (NONE, MINOR, STRUCTURAL) vào request Valuation, coi đây là khai báo tình trạng, không nhận factor/price/score từ client. Backend ánh xạ lần lượt 1.03/1.00/0.97 và 1.00/0.90/0.70; report kiểm định hợp lệ được ưu tiên và phát hiện mâu thuẫn. Snapshot vẫn lưu các factor đã có, note ghi nguồn khai báo. Chốt matching MVP: chỉ lấy giá Product khi có duy nhất Product ACTIVE cùng brandId và productName bằng nhau sau trim/không phân biệt hoa thường; giá lấy effectivePrice do backend tính. Không có match duy nhất thì dùng originalPrice. Chưa suy “model tương tự” hoặc “listing tương tự” khi không có tiêu chí được duyệt; Product hiện không có field model riêng.

**Ảnh hưởng:** Request DTO, validation, ValuationService, màn hình người bán, API collection, test công thức và tài liệu API. Không cần thêm hệ thống hay module mới. Nếu yêu cầu lưu lâu dài tình trạng gốc trên Listing, phải duyệt một phương án schema riêng.

**Hiện trạng:** Chưa áp dụng đề xuất. API trả DESIGN_DECISION_REQUIRED, calculator/Entity/repository/history còn trong source. Không đánh dấu module DONE.

## PHẦN 2. Trạng thái Category và Brand

**Thiết kế hiện tại:** Có cột status VARCHAR(20), chưa tìm thấy tập giá trị và transition canonical trong tài liệu nguồn. Product đã có tập trạng thái riêng.

**Đề xuất để duyệt:** ACTIVE/INACTIVE cho Category và Brand; tạo mới ACTIVE, không xóa record có tham chiếu; chốt việc ẩn danh mục/thương hiệu có ảnh hưởng đến Product/Listing hiện hữu hay không (đề xuất không cascade thay trạng thái Product/Listing).

**Ảnh hưởng:** Enum Java, default/backfill trong migration mới, lọc API, UI quản trị và tests. Chưa áp dụng enum/migration này. Field hiện được giữ nullable; không tự coi ProductStatus là trạng thái Category/Brand.

## PHẦN 3. API bổ trợ

**Thiết kế hiện tại:** D02 cho phép Buyer cancel trước payment, D14 quy định attribution; tài liệu API chưa chốt URI action cancel Marketplace. Audit Log có nghiệp vụ lưu nhưng chưa có URI đọc canonical.

**Đề xuất để duyệt:**

- POST /api/market-orders/{id}/cancel: Buyer sở hữu; chỉ PENDING_PAYMENT + WAITING_PAYMENT; server ghi BUYER / BUYER_CHANGED_MIND. Không nhận actor/reason tùy ý.
- GET /api/admin/audit-logs?page=0&size=20: chỉ ADMIN, phân trang tối đa 100.

**Ảnh hưởng:** Controller, UI, tests API và OpenAPI. Service hủy và audit ghi đã có; hai endpoint trên hiện không được mở. Không thay state machine.

## PHẦN 4. Quy trình áp dụng

Không có quyết định nào trong tài liệu này mang trạng thái APPROVED. Sau khi người dùng chốt: cập nhật 04_CHANGELOG.md và các phần liên quan của Master/Rules, sau đó triển khai migration/API/UI/test theo vertical slice và cập nhật Implementation Status. Không tự thêm CHG-015 đã duyệt.

## PHẦN 5. Yêu cầu xác nhận để hoàn tất

Xin xác nhận áp dụng **cả ba đề xuất ở PHẦN 1–3**. Chỉ sau xác nhận mới cập nhật CHANGELOG/Master/Rules và nối source:

1. Request Valuation có accessoryCondition/repairLevel; các factor do backend tính; matching và fallback đúng PHẦN 1.
2. Category/Brand dùng ACTIVE/INACTIVE; không cascade trạng thái sang Product/Listing.
3. Mở hai URI cancel Marketplace và đọc Audit cho đúng quyền/state ở PHẦN 3.

Đây là quyết định về API/status/business rule, không phải xin phép build/test hoặc xuất source. Yêu cầu xác nhận xuất phát từ quy tắc người dùng: không tự thay Entity, relationship, API, status, business rule hoặc module boundary.
