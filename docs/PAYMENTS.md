# Thanh toán đơn hàng shop

## Cập nhật: ngừng cung cấp cổng thanh toán

Checkout hiện chỉ cung cấp COD, chuyển khoản ngân hàng và VietQR. VNPAY, MoMo và ZaloPay bị chặn tạo thanh toán mới, kể cả khi biến ENABLED cũ còn bật. Dữ liệu đơn cũ và xử lý callback được giữ để đối soát, không xóa lịch sử. Các hướng dẫn merchant bên dưới chỉ là tài liệu tích hợp cũ, không còn áp dụng để bật checkout.

Hỗ trợ COD, chuyển khoản ngân hàng, VietQR, VNPAY, MoMo và ZaloPay. Mặc định chỉ COD hoạt động. Không có tài khoản nhận tiền hay khóa merchant được điền sẵn. Luồng ký quỹ marketplace không thay đổi.

## Cấu hình local

Điền các biến tương ứng trong `backend/.env`, tham khảo `backend/.env.example`, rồi khởi động lại backend. Không thay toàn bộ `.env` vì sẽ mất cấu hình CSDL/JWT hiện tại. Không gửi khóa bí mật vào chat hoặc commit Git.

Chuyển khoản và VietQR dùng chung cấu hình:

```dotenv
PAYMENT_BANK_ENABLED=true
PAYMENT_BANK_ID=
PAYMENT_BANK_NAME=
PAYMENT_BANK_ACCOUNT_NUMBER=
PAYMENT_BANK_ACCOUNT_NAME=
```

Điền mã ngân hàng/BIN, tên ngân hàng, số tài khoản và tên chủ tài khoản thực tế của shop. Có thể bật tự xác minh tiền về qua SePay theo hướng dẫn bên dưới. Phí chuyển khoản phụ thuộc ngân hàng/gói dịch vụ; dự án không cam kết mọi giao dịch miễn phí.

## Tự xác minh chuyển khoản qua SePay

Backend xác minh webhook SePay bằng HMAC-SHA256, thời gian ký (tối đa lệch 5 phút), mã giao dịch duy nhất, tài khoản nhận, mã đơn dạng `DH` + ID được đệm đủ tối thiểu 6 chữ số (ví dụ `DH000005`) và số tiền phải khớp tuyệt đối. Với BIDV kết nối API, SePay báo VA ở trường `subAccount`; webhook chấp nhận VA đúng với số nhận tiền đã lưu cho đơn. Giao dịch lặp không ghi nhận hai lần; tiền vào đơn đã hủy được chuyển thành **Chờ hoàn tiền**.

1. Kết nối BIDV với SePay. Với kết nối API BIDV cá nhân, tạo VA trong SePay và đặt `PAYMENT_BANK_ACCOUNT_NUMBER` bằng giá trị `acc` trong link VietQR của VA (có thể là mã chữ và số như `96247BADMINTONSTORE`). QR được tạo theo mẫu SePay/VietQR `https://vietqr.app/img?acc=...&bank=BIDV&amount=...&des=DH000005`; không dùng số tài khoản chính cho kiểu kết nối này. Nếu muốn nhận vào tài khoản chính, dùng kết nối SMS Banking nếu SePay hỗ trợ cho tài khoản/gói của bạn. Đặt `PAYMENT_PUBLIC_API_URL` thành URL HTTPS công khai của backend.
2. Tạo webhook trong SePay: sự kiện **Có tiền vào**, định dạng JSON, xác thực **HMAC-SHA256**, URL `https://<backend>/api/payments/callbacks/sepay`.
3. Sao chép Secret Key SePay cấp vào `SEPAY_WEBHOOK_SECRET`; đặt `SEPAY_WEBHOOK_ENABLED=true` trong môi trường backend rồi khởi động lại.
4. Trong cài đặt cấu trúc mã thanh toán SePay, cấu hình tiền tố `DH`, phần số tối thiểu 6 chữ số (tối đa ít nhất 8 chữ số) và bật nhận diện mã; giữ bộ lọc webhook là `DH`. Dùng chức năng gửi thử SePay để kiểm tra chữ ký; sau đó chuyển thử một giao dịch thật nhỏ với nội dung theo QR, ví dụ `DH000005`, và xác nhận đơn tự chuyển sang **Đã thanh toán**.

Không chọn webhook không xác thực. Nếu nội dung giao dịch không chứa đúng một mã đơn `DH` hoặc số tiền/tài khoản không khớp, đơn sẽ không được xác nhận. SePay nhận phản hồi lỗi; kiểm tra giao dịch đó trong sao kê trước khi đối soát thủ công.

Cổng thanh toán cần hợp đồng/tài khoản merchant và bộ khóa tương ứng:

| Cổng | Biến cấu hình |
| --- | --- |
| VNPAY | `VNPAY_ENABLED`, `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` |
| MoMo | `MOMO_ENABLED`, `MOMO_PARTNER_CODE`, `MOMO_ACCESS_KEY`, `MOMO_SECRET_KEY` |
| ZaloPay | `ZALOPAY_ENABLED`, `ZALOPAY_APP_ID`, `ZALOPAY_KEY1`, `ZALOPAY_KEY2` |

Đặt `PAYMENT_PUBLIC_API_URL` thành địa chỉ HTTPS công khai của backend (không thêm `/api`), `PAYMENT_WEB_URL` thành địa chỉ frontend. Production yêu cầu frontend HTTPS. `PAYMENT_SANDBOX=true` dùng endpoint thử nghiệm của các cổng; chuyển khoản ngân hàng/VietQR **không phải giao dịch sandbox**.

Đăng ký/cho phép callback với nhà cung cấp:

- VNPAY: GET `/api/payments/callbacks/vnpay` (IPN).
- MoMo: POST `/api/payments/callbacks/momo`.
- ZaloPay: POST `/api/payments/callbacks/zalopay`.

Trang quay về là `/orders/{id}` trên frontend; khách có thể cần đăng nhập lại. Tham số thành công trên URL quay về không chứng minh đã thanh toán. Chỉ callback hợp lệ về chữ ký, mã đơn và số tiền mới cập nhật trạng thái. Không đổi khóa merchant khi còn giao dịch chờ xử lý.

## Vận hành

- Đơn mới luôn chưa thanh toán. COD ghi nhận đã thanh toán khi hoàn tất đơn.
- Khách mở thông tin chuyển khoản/QR tại chi tiết đơn. Chuyển đúng số tiền và nội dung `DH` theo sau bởi ID đơn được đệm tối thiểu 6 chữ số, ví dụ `DH000005`.
- Nếu chưa bật SePay, admin mở chi tiết đơn, chọn **Đối soát tiền vào**, kiểm tra sao kê thực tế rồi nhập số tiền và mã giao dịch. Không xác nhận chỉ từ ảnh chụp của khách.
- Admin không được xác nhận thủ công thay callback VNPAY/MoMo/ZaloPay.
- Callback lặp không ghi nhận hai lần. Giao dịch đến sau khi đơn bị hủy chuyển thành **Chờ hoàn tiền**, không khôi phục đơn.
- Hủy đơn đã trả tiền cũng chuyển sang **Chờ hoàn tiền**. Admin hoàn tiền ngoài ứng dụng trước, sau đó dùng **Ghi nhận hoàn tiền** với số tiền và mã giao dịch hoàn. Nút này chỉ ghi nhận, không thực hiện chuyển tiền/API refund.
- Liên kết cổng thanh toán hết hạn sau 15 phút. Tồn kho được giữ khi đặt đơn và trả lại khi hủy; chưa có tác vụ tự hủy đơn hết hạn.
- **Cập nhật trạng thái** đọc trạng thái đã lưu từ callback, chưa gọi API truy vấn giao dịch của nhà cung cấp. Nếu callback thất lạc hoặc timeout, kiểm tra cổng merchant trước khi thanh toán lại; không coi timeout là giao dịch thất bại chắc chắn.

## CSDL và kiểm thử

Flyway V13 thêm `shop_payments`, khóa chống trùng giao dịch và dữ liệu đối soát; không xóa tài khoản/sản phẩm. SQL cài mới `database/03_schema_complete.sql` và `05_project_complete.sql` đã bao gồm V13. Dự án đang chạy nên dùng Flyway, không chạy SQL cài mới lên CSDL có dữ liệu.

Kiểm thử backend sử dụng CSDL MySQL riêng và khóa giả; kiểm thử trình duyệt sử dụng API mô phỏng. Cần cấu hình tài khoản SePay, HMAC Secret và URL HTTPS công khai của backend để chạy giao dịch xác minh thật.

## Tài liệu nhà cung cấp

- [VNPAY thanh toán và IPN](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html)
- [MoMo tạo thanh toán](https://developers.momo.vn/v3/vi/docs/payment/api/wallet/onetime/)
- [MoMo IPN](https://developers.momo.vn/v3/vi/docs/payment/api/result-handling/notification/)
- [ZaloPay tạo đơn](https://docs.zalopay.vn/docs/specs/order-create/)
- [ZaloPay callback](https://docs.zalopay.vn/docs/developer-tools/knowledge-base/callback/)
- [VietQR Quick Link](https://www.vietqr.io/danh-sach-api/link-tao-ma-nhanh/)
- [SePay Webhooks](https://docs.sepay.vn/tich-hop-webhooks.html)
- [SePay xác thực HMAC-SHA256](https://developer.sepay.vn/vi/sepay-webhooks/xac-thuc)
