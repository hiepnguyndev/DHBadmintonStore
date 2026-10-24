# Chạy bản build qua Cloudflare Tunnel

Chạy `powershell -NoProfile -ExecutionPolicy Bypass -File .\start-public.ps1` từ thư mục dự án. Cần MySQL, Java, Maven, Node và các dependency đã cài. Script từ chối chạy nếu 8080/8787 đang có ứng dụng khác, không tự dừng ứng dụng của bạn.

Frontend build được phục vụ tại `http://127.0.0.1:8787`; `/api/` và `/media/` chuyển tiếp tới backend `127.0.0.1:8080`. Không dùng Vite dev/preview để công khai. Máy phải bật và cloudflared phải tiếp tục chạy. Không công khai cổng MySQL 3306.

Backend dùng profile prod, tắt Swagger; script ghi đè URL thanh toán/CORS trong tiến trình, không sửa `.env` và không bật cổng thanh toán còn thiếu cấu hình. Cả PAYMENT_WEB_URL và PAYMENT_PUBLIC_API_URL dùng `https://ngdhiep-fithau.id.vn` vì API cùng tên miền. Không cần subdomain api riêng.

Trong Cloudflare, thêm Published application:

| Trường | Giá trị |
| --- | --- |
| Subdomain | Trống |
| Domain | ngdhiep-fithau.id.vn |
| Path | Trống |
| Service URL | http://127.0.0.1:8787 |

Tên miền gốc hiện có bốn bản ghi A của GitHub Pages. Sao lưu chúng trước; khi sẵn sàng chuyển website, bỏ đúng bốn bản ghi A của tên miền gốc rồi tạo route Tunnel. Không thay MX, mail, mx hoặc các bản ghi email khác. www vẫn là cấu hình cũ, không tự chuyển sang Tunnel.

Xem log trong `backend/var/public-*.log`. Script in PID của các tiến trình để dừng khi cần. `node frontend/scripts/test-public-server.mjs` kiểm tra local. Kết nối local thành công chưa chứng minh DNS/HTTPS hoặc thanh toán merchant hoạt động; phải kiểm tra thêm sau khi lưu route.

Đây là cấu hình phục vụ demo trên máy cá nhân, chưa phải đánh giá an toàn toàn diện cho shop nhận tiền thật. Bảo đảm mật khẩu admin mạnh, có backup CSDL và thử sandbox trước. Giới hạn API hiện dùng chung cho toàn bộ khách truy cập, thích hợp demo nhỏ. Không đặt Cloudflare Access/challenge chặn callback của nhà cung cấp thanh toán.

Không đưa `.env`, token cloudflared hay bản sao lưu CSDL lên GitHub. Cảnh báo npm audit của công cụ test/dev không được giải quyết bằng cách công khai các công cụ đó. Proxy dùng bộ lọc hàm cố định, không nhận mẫu glob do người dùng cung cấp.
