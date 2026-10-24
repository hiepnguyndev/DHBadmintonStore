# Chatbot theo bộ yêu cầu người dùng

Cập nhật ngày 23/10/2026. Áp dụng cho `CHATBOT_ENGINE=PHOBERT_V1` và
`HYBRID_INTENT_RULES_ENABLED=true` (script `scripts/start-ai.ps1` đã bật).

## Kiến trúc và phạm vi

Giữ nguyên 21 nhãn PhoBERT và checkpoint hiện có. Không huấn luyện lại hoặc thay
đổi taxonomy. Python nhận diện intent, bổ sung luật hội thoại có dấu vết, trích
xuất slot và quản lý ngữ cảnh. Spring Boot truy vấn MySQL, lọc, xếp hạng và tạo
câu trả lời dựa trên dữ liệu cửa hàng. Không gửi catalog hoặc tài khoản CSDL
sang Python.

| Nhóm yêu cầu | Xử lý |
| --- | --- |
| Tìm kiếm, tư vấn, cập nhật điều kiện | Kết hợp danh mục, thương hiệu, model, giá, thông số, màu/size; tối đa ba sản phẩm và lý do đề xuất. |
| Danh mục | Vợt, giày, balo/túi, dây cước, quả cầu, trang phục, phụ kiện. |
| Cá nhân hóa | Trình độ, lối chơi, sức cổ tay và sở thích dùng để xếp hạng; các yêu cầu kỹ thuật tường minh vẫn là bộ lọc. |
| Tồn kho | Tra cả sản phẩm hết hàng, màu và size của cùng biến thể; kiểm tra số lượng trên một biến thể thay vì cộng các size khác nhau. |
| Thông tin, đánh giá, tương thích, độ căng | Truy vấn lại dữ liệu hiện tại; phân biệt mô tả cửa hàng, giới hạn kỹ thuật và suy luận. Không tạo nhận xét người mua giả. |
| So sánh | Tra riêng từng model; tên chưa xác định duy nhất thì hỏi lại. Hỗ trợ tham chiếu mẫu thứ nhất/hai/ba và so sánh sau khi tìm sản phẩm. |
| Xem thêm | Loại ID đã trả về trong phiên, giữ bộ điều kiện tìm kiếm; truy vấn lại giá và tồn kho. |
| Chuyển danh mục | Giữ ngân sách, thương hiệu, màu; bỏ điều kiện riêng của danh mục trước. |
| Combo | Chọn các nhóm được yêu cầu, bỏ món đã có, kiểm tra tồn kho, tính tổng giá thực và từ chối bộ vượt ngân sách. Ưu tiên danh mục bằng độ phù hợp có thể xác minh, không coi giá cao là chất lượng tốt. |
| Sở thích | Lưu, xem, cập nhật, xóa tạm trong phiên. Lưu lâu dài qua trang Hồ sơ người chơi sau khi đăng nhập; chatbot không tự ghi hồ sơ. |
| Đặt lịch, nhập hàng | Thông báo chưa có nghiệp vụ/lịch xác nhận và hướng dẫn liên hệ; không hứa đã đặt lịch hoặc đăng ký thông báo. |
| Kích cỡ | Thu thập số đo, size, độ rộng chân; không tự quy đổi cm/tuổi sang size khi chưa có bảng của đúng mẫu. |
| Sản phẩm cũ | Hướng dẫn mục Chợ đồ cũ; không tự xác nhận thu mua/bảo hành cho tin đăng. |
| Sức khỏe | Chỉ mô tả dụng cụ, không chẩn đoán, kê thuốc hay hứa điều trị; hướng đến chuyên gia khi cần. |
| Ngoài phạm vi, trái phép | Phản hồi giới hạn phạm vi; không truy cập thông tin tài khoản/đơn hàng khác hoặc sửa dữ liệu. |
| Chính sách, đơn hàng, nhân viên | Tách khỏi taxonomy; dẫn tới luồng website tương ứng, không tự xác nhận dữ liệu chưa tích hợp. |

## Quy ước dữ liệu

- “Dưới” là `<`; “không quá/tối đa” là `<=`. “Có/khoảng N” dùng N làm trần để
  không tự vượt ngân sách. “Từ A đến B” lọc cả hai cận.
- Thương hiệu, model, màu/size, trọng lượng, độ cứng và các yêu cầu kỹ thuật
  tường minh không bị tự nới khi không có kết quả.
- Thuộc tính ngoài bảng thông số vợt chỉ được lọc khi có bằng chứng trong mô tả
  sản phẩm hoặc bảng biến thể: ví dụ ngăn giày, sức chứa, đường kính dây,
  chất liệu, phom giày, size áo. Thiếu dữ liệu trả lời chưa xác minh được,
  không suy đoán rằng sản phẩm đáp ứng.
- Giá lấy theo chương trình khuyến mãi đã công bố. Tồn kho thẻ sản phẩm dựa trên
  biến thể khớp điều kiện; không còn nhãn “Còn hàng” cố định.
- ID/model đã xem và bộ lọc được lưu bằng token một lần ở backend, có thời hạn,
  gắn với phiên trình duyệt và danh tính đăng nhập. Không dùng lịch sử do client
  gửi để quyết định quyền. Lỗi AI tạm thời giữ lại ngữ cảnh trước đó nếu có thể.

## Kiểm tra lại

```powershell
# Backend: unit/API tests. Nhóm MySQL test chuyên dụng cần TEST_DB_* riêng.
cd backend
.\mvnw.cmd test

# Python: từ thư mục ai-research
.\.venv\Scripts\python.exe -m unittest discover -s tests

# Frontend: từ thư mục frontend
npm.cmd test -- --run src/test/chatbot.test.jsx
npm.cmd run build

# Từ thư mục gốc, khi dịch vụ đang chạy:
python tests/check_chatbot_requirements_live.py --url http://127.0.0.1:8100
python tests/check_chatbot_catalog_live.py --url http://127.0.0.1:8080
```

Script kiểm tra PhoBERT đọc `AI_SERVICE_TOKEN` từ môi trường nếu dịch vụ yêu cầu.
Script catalog chỉ đọc dữ liệu và tạo phiên hội thoại, không sửa sản phẩm. Nó cần
catalog có ít nhất một vợt dưới 5 triệu; không phải kiểm thử với dữ liệu giả.

Kết quả kiểm tra: backend và kiểm thử giao diện chatbot qua; frontend build được.
Kiểm thử giao diện chatbot: 20/20. PhoBERT thật: 17/17 tình huống hành động.
Chuỗi Spring Boot → PhoBERT → MySQL: 12/12 lượt kiểm tra, kèm kiểm tra token
không sử dụng được từ trình duyệt khác. Các dịch vụ kiểm tra dùng cổng riêng
8081/8101, tắt migration và tác vụ định kỳ; không thay đổi catalog.
Các integration test cần database kiểm thử riêng vẫn được bỏ qua khi chưa đặt
`RUN_MYSQL_TESTS=true`; kiểm tra catalog thật ở trên là kiểm tra chỉ đọc riêng.
Bộ Python có 249/250 ca qua. Ca còn lại
`test_only_one_training_dataset_and_config` giả định chỉ có `datasets/phobert-vi`,
trong khi thư mục dự án đã có thêm `datasets/intent-v3-experiment`. Không xóa bộ
dữ liệu thí nghiệm để làm xanh kiểm thử này. Các kiểm thử chức năng mới không
được dùng làm số đo accuracy/F1 của mô hình.

## Giới hạn còn phụ thuộc dữ liệu/nghiệp vụ

Chưa bổ sung API đặt lịch, lịch nhập hàng, nguồn review đã xác minh hoặc bảng
size cho từng hãng/model. Việc lưu sở thích lâu dài, kiểm tra/hủy đơn thực hiện
qua màn hình website có xác thực hiện có. Tư vấn bằng ngôn ngữ tự nhiên tùy ý
vẫn phụ thuộc chất lượng checkpoint và phạm vi từ vựng NER; các ca chức năng
đã qua không đồng nghĩa mô hình luôn hiểu đúng mọi câu.

Sau khi cập nhật mã cần khởi động lại tiến trình AI và backend đang chạy để nạp
thay đổi. Không cần migration CSDL cho bản sửa này.
