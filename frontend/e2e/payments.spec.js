import { test, expect } from "@playwright/test";

const names = { COD: "Thanh toán khi nhận hàng", BANK_TRANSFER: "Chuyển khoản ngân hàng", VIETQR: "VietQR", VNPAY: "VNPAY", ZALOPAY: "ZaloPay", MOMO: "MoMo" };
const product = { id: 1, productName: "Vợt cầu lông kiểm thử thanh toán", thumbnailUrl: "/images/gear-racket.jpg" };
async function setup(page, { role = "USER", paymentStatus = "UNPAID", method = "VIETQR", timeout = false } = {}) {
  const writes = [];
  const user = { id: 800, fullName: "Payment Preview", email: "payment-preview@example.test", role, status: "ACTIVE" };
  const order = { orderId: 42, orderCode: "ORD-PAYMENT-PREVIEW", paymentMethod: method, paymentStatus,
    orderStatus: paymentStatus === "REFUND_PENDING" ? "CANCELLED" : "PENDING", receiverName: "Khách kiểm thử", receiverPhone: "0901234567",
    shippingAddress: "123 Đường kiểm thử, Hà Nội", subtotalAmount: 499000, totalAmount: 499000, shippingFee: 0 };
  let created = role === "ADMIN";
  let bankDetails = false;
  const payment = () => ({ method: order.paymentMethod, status: order.paymentStatus, amount: order.totalAmount, available: true, sandbox: ["VNPAY", "MOMO", "ZALOPAY"].includes(order.paymentMethod),
    expired: false, reference: "DH-PREVIEW-42", transferContent: "DH42", bankName: "Vietcombank", accountNumber: "0000000000", accountName: "TEST ONLY",
    qrUrl: bankDetails ? "https://img.vietqr.io/image/970436-0000000000-compact2.png?amount=499000&addInfo=DH42&accountName=TEST%20ONLY" : null });
  await page.route("**/api/**", async route => {
    const req = route.request(), path = new URL(req.url()).pathname.replace(/^\/api/, "");
    let data = [], status = 200;
    if (req.method() !== "GET") writes.push({ path, body: req.postData() ? req.postDataJSON() : null });
    if (path === "/auth/login") data = { accessToken: "preview-payment-token", user };
    else if (path === "/users/me") data = user;
    else if (path === "/addresses") data = [{ addressId: 3, receiverName: order.receiverName, detailAddress: "123 Đường kiểm thử", province: "Hà Nội", isDefault: true }];
    else if (path === "/payments/methods") data = Object.entries(names).map(([code, name]) => ({ code, name, available: code !== "MOMO", sandbox: ["VNPAY", "ZALOPAY"].includes(code) }));
    else if (path === "/cart") data = { cartId: 1, items: [{ cartItemId: 1, product, quantity: 1, totalPrice: 499000 }], subtotalAmount: 499000 };
    else if (path === "/orders" && req.method() === "POST") {
      created = true; order.paymentMethod = req.postDataJSON().paymentMethod; status = 201; data = { order };
    } else if (path === "/orders/42") data = { order, items: [{ orderItemId: 1, productId: 1, productName: product.productName, quantity: 1, unitPrice: 499000, totalPrice: 499000 }] };
    else if (path === "/orders/42/payment") {
      if (req.method() === "POST") bankDetails = true;
      data = payment();
      if (timeout && req.method() === "POST") data.message = "Chưa lấy được liên kết thanh toán. Kiểm tra trạng thái trước khi thử lại; không chuyển tiền nhiều lần.";
    } else if (path === "/admin/orders/42/payment/confirm") { order.paymentStatus = "PAID"; data = payment(); }
    else if (path === "/admin/orders/42/payment/refund-confirm") { order.paymentStatus = "REFUNDED"; data = payment(); }
    else if (path === "/admin/statistics/overview") data = {};
    else if (path === "/orders/my" || path === "/admin/orders") data = { content: created ? [order] : [], page: 0, size: 20, totalPages: 1 };
    else if (path === "/products" || path === "/admin/products") data = { content: [], page: 0, totalElements: 0, totalPages: 0 };
    await route.fulfill({ status, contentType: "application/json", body: JSON.stringify({ success: true, data }) });
  });
  await page.goto("/checkout");
  await page.getByLabel("Email hoặc số điện thoại").fill(user.email);
  await page.getByLabel("Mật khẩu", { exact: true }).fill("Preview123!");
  await page.getByRole("button", { name: "Đăng nhập", exact: true }).click();
  await expect(page).toHaveURL(role === "ADMIN" ? /\/admin$/ : /\/checkout$/);
  return writes;
}
async function navigate(page, path) {
  await page.evaluate(path => { history.pushState({}, "", path); window.dispatchEvent(new PopStateEvent("popstate")); }, path);
}
for (const width of [1440, 390, 360]) {
  test.describe(`payments ${width}px`, () => {
    test.use({ viewport: { width, height: 900 } });
    test("checkout selects VietQR and shows a real QR without marking payment successful", async ({ page }) => {
      const errors = []; page.on("pageerror", e => errors.push(e.message));
      const writes = await setup(page);
      await expect(page.getByRole("radio")).toHaveCount(6);
      await expect(page.getByRole("radio", { name: /MoMo/ })).toBeDisabled();
      await page.getByRole("radio", { name: "VietQR" }).check();
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
      await page.screenshot({ path: `../docs/evidence/payment-checkout-${width}.png`, fullPage: true });
      await page.getByRole("button", { name: "Đặt hàng", exact: true }).click();
      await expect(page).toHaveURL(/\/orders\/42$/);
      expect(writes.find(w => w.path === "/orders").body).toEqual({ addressId: 3, paymentMethod: "VIETQR", note: "" });
      const panel = page.getByRole("region", { name: "Thanh toán", exact: true });
      await panel.getByRole("button", { name: "Thông tin chuyển khoản / QR" }).click();
      await expect(panel.getByRole("img", { name: "Mã VietQR thanh toán đơn hàng" })).toBeVisible();
      await expect.poll(() => panel.locator("img").evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
      await expect(panel.getByText("DH42", { exact: true })).toBeVisible();
      await expect(panel.getByText("Chưa thanh toán", { exact: true })).toBeVisible();
      expect(writes.filter(w => w.path.includes("/confirm"))).toHaveLength(0);
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
      await panel.screenshot({ path: `../docs/evidence/payment-qr-${width}.png` });
      await navigate(page, "/orders/42?vnp_ResponseCode=00&resultCode=0");
      await expect(panel.getByText("Chưa thanh toán", { exact: true })).toBeVisible();
      expect(errors).toEqual([]);
    });
    test("admin records a refund only after entering bank evidence", async ({ page }) => {
      const writes = await setup(page, { role: "ADMIN", paymentStatus: "REFUND_PENDING" });
      await navigate(page, "/orders/42");
      const panel = page.getByRole("region", { name: "Thanh toán", exact: true });
      await panel.getByRole("button", { name: "Ghi nhận hoàn tiền" }).click();
      await panel.getByLabel("Mã giao dịch trên sao kê").fill("REFUND-PREVIEW-42");
      await panel.getByRole("checkbox").check();
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
      await panel.screenshot({ path: `../docs/evidence/payment-admin-${width}.png` });
      await panel.getByRole("button", { name: "Xác nhận", exact: true }).click();
      await expect(panel.getByText("Đã hoàn tiền", { exact: true })).toBeVisible();
      expect(writes.find(w => w.path.endsWith("refund-confirm")).body).toEqual({ amount: 499000, reference: "REFUND-PREVIEW-42" });
    });
  });
}
test("gateway timeout retains unpaid state and offers status refresh", async ({ page }) => {
  await setup(page, { method: "VNPAY", timeout: true });
  await navigate(page, "/orders/42");
  const panel = page.getByRole("region", { name: "Thanh toán", exact: true });
  await panel.getByRole("button", { name: "Thanh toán với VNPAY" }).click();
  await expect(panel.getByRole("status")).toContainText("Chưa lấy được liên kết thanh toán");
  await expect(panel.getByText("Chưa thanh toán", { exact: true })).toBeVisible();
  await expect(panel.getByRole("button", { name: "Cập nhật trạng thái" })).toBeEnabled();
});
