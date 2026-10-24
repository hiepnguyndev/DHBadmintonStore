# Kết nối chatbot với API bên ngoài

AI nội bộ (PhoBERT/FastAPI và bộ trả lời theo luật) đã được gỡ bỏ.
Giao diện chatbot và gợi ý từ hồ sơ qua `/api/recommendations` được giữ lại.

Điểm tích hợp: `backend/src/main/java/com/badminton/chatbot/service/ChatbotService.java`, phương thức `reply`.
Hiện tại chỉ trả `NOT_CONFIGURED`, `engine: NONE`, không gọi mạng hoặc sinh câu trả lời AI.

Frontend gọi `POST /api/ai/chat` với `message`, `history`, `language` (`vi`/`en`) và `contextToken` tùy chọn.
Phản hồi dùng ApiResponse, phần `data` gồm `resultKind`, `answer`, `products`, `engine` và `contextToken` tùy chọn.
Khi tích hợp, giữ khóa API ở backend, chuyển phản hồi của nhà cung cấp sang ChatResponse và xử lý timeout/lỗi.
Validation, giới hạn tần suất và kiểm soát truy cập được giữ lại.

`start-web.ps1` chỉ khởi động backend và frontend, không cần Python/model/cổng 8100.
Tài liệu bằng chứng chatbot cũ trong docs phản ánh phiên bản trước khi gỡ AI.
