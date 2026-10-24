import { afterEach, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import PaymentPanel from "../components/PaymentPanel";
import { CheckoutPage } from "../pages/ShopPages";
import { apiClient } from "../services/apiClient";
import { useAuth } from "../context/AuthContext";

vi.mock("../services/apiClient", () => ({ apiClient: vi.fn(), errorMessage: e => e.message || "Không thể thanh toán" }));
vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));
afterEach(() => { cleanup(); vi.resetAllMocks(); });
const response = data => ({ data: { data } });
const order = { orderId: 12, paymentMethod: "VIETQR", paymentStatus: "UNPAID", orderStatus: "PENDING", totalAmount: 499000 };
const unpaid = { method: "VIETQR", status: "UNPAID", amount: 499000, available: true, expired: false, transferContent: "DH12", bankName: "Test Bank", accountNumber: "0000000000", accountName: "TEST ONLY" };
const names = { COD: "Thanh toán khi nhận hàng", BANK_TRANSFER: "Chuyển khoản ngân hàng", VIETQR: "VietQR", VNPAY: "VNPAY", ZALOPAY: "ZaloPay", MOMO: "MoMo" };
it.each(["cancelled", "expired"])("does not automatically start %s VietQR payments", async reason => {
  useAuth.mockReturnValue({ user: { role: "USER" } });
  apiClient.mockResolvedValue(response({ ...unpaid, expired: reason === "expired" }));
  render(<PaymentPanel order={{ ...order, orderStatus: reason === "cancelled" ? "CANCELLED" : "PENDING" }} onUpdate={vi.fn()} />);
  await waitFor(() => expect(screen.queryByText("Đang tải thông tin thanh toán...")).not.toBeInTheDocument());
  expect(apiClient.mock.calls.some(([request]) => request.method === "post")).toBe(false);
  expect(screen.queryByRole("button", { name: "Thông tin chuyển khoản / QR" })).not.toBeInTheDocument();
  expect(screen.queryByText("0000000000")).not.toBeInTheDocument();
});
it("keeps transfer details and allows retry after automatic QR creation fails", async () => {
  useAuth.mockReturnValue({ user: { role: "USER" } });
  let attempts = 0;
  apiClient.mockImplementation(({ method }) => {
    if (method !== "post") return Promise.resolve(response(unpaid));
    if (++attempts === 1) return Promise.reject(new Error("QR tạm thời lỗi"));
    return Promise.resolve(response({ ...unpaid, qrUrl: "https://img.vietqr.io/test.png" }));
  });
  render(<PaymentPanel order={order} onUpdate={vi.fn()} />);
  await screen.findByRole("alert");
  expect(screen.getByText("0000000000")).toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Thông tin chuyển khoản / QR" }));
  await screen.findByRole("img", { name: "Mã VietQR thanh toán đơn hàng" });
  expect(attempts).toBe(2);
  expect(screen.queryByRole("alert")).not.toBeInTheDocument();
});
it("shows all payment methods, disables unavailable gateways and sends only checkout fields", async () => {
  const writes = [];
  apiClient.mockImplementation(({ url, method, data }) => {
    if (url === "/addresses") return Promise.resolve(response([{ addressId: 4, receiverName: "Test", detailAddress: "123 Test", ward: "ML", province: "HN" }]));
    if (url === "/payments/methods") return Promise.resolve(response(Object.entries(names).map(([code, name]) => ({ code, name, available: ["COD", "VIETQR"].includes(code) }))));
    if (url === "/payments/bank-details") return Promise.resolve(response({ available: true, bankName: "Test Bank", accountNumber: "0000000000", accountName: "TEST ONLY" }));
    if (url === "/cart") return Promise.resolve(response({ items: [{ cartItemId: 1, product: { productName: "Vợt thử" }, quantity: 1, totalPrice: 499000 }], subtotalAmount: 499000 }));
    if (method === "post") { writes.push(data); return Promise.resolve(response({ order })); }
    throw new Error(url);
  });
  render(<MemoryRouter initialEntries={["/checkout"]}><Routes><Route path="/checkout" element={<CheckoutPage />} /><Route path="/orders/:id" element={<p>Đơn đã tạo</p>} /></Routes></MemoryRouter>);
  await screen.findByRole("radio", { name: "VietQR" });
  expect(screen.getAllByRole("radio")).toHaveLength(3);
  expect(screen.queryByRole("radio", { name: /MoMo|VNPAY|ZaloPay/ })).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole("radio", { name: "VietQR" }));
  fireEvent.click(screen.getByRole("button", { name: "Đặt hàng" }));
  await screen.findByText("Đơn đã tạo");
  expect(writes).toEqual([{ addressId: 4, paymentMethod: "VIETQR", note: "" }]);
});
it("does not show bank details until a bank-transfer order is placed", async () => {
  apiClient.mockImplementation(({ url }) => {
    if (url === "/addresses") return Promise.resolve(response([{ addressId: 4, receiverName: "Test", detailAddress: "123 Test", ward: "ML", province: "HN" }]));
    if (url === "/payments/methods") return Promise.resolve(response(Object.entries(names).map(([code, name]) => ({ code, name, available: ["COD", "BANK_TRANSFER"].includes(code) }))));
    if (url === "/payments/bank-details") return Promise.resolve(response({ available: true, bankName: "Test Bank", accountNumber: "0000000000", accountName: "TEST ONLY" }));
    if (url === "/cart") return Promise.resolve(response({ items: [{ cartItemId: 1, product: { productName: "Vợt thử" }, quantity: 1, totalPrice: 499000 }], subtotalAmount: 499000 }));
    throw new Error(url);
  });
  render(<MemoryRouter initialEntries={["/checkout"]}><Routes><Route path="/checkout" element={<CheckoutPage />} /></Routes></MemoryRouter>);
  fireEvent.click(await screen.findByRole("radio", { name: "Chuyển khoản ngân hàng" }));
  expect(await screen.findByText(/sẽ hiển thị sau khi bạn đặt hàng/i)).toBeInTheDocument();
  expect(screen.queryByText("0000000000")).not.toBeInTheDocument();
});
it("opens VietQR automatically and does not confirm payment", async () => {
  useAuth.mockReturnValue({ user: { role: "USER" } });
  apiClient.mockImplementation(({ method }) => Promise.resolve(response(method === "post" ? { ...unpaid, qrUrl: "https://img.vietqr.io/test.png" } : unpaid)));
  render(<PaymentPanel order={order} onUpdate={vi.fn()} />);
  await screen.findByRole("img", { name: "Mã VietQR thanh toán đơn hàng" });
  expect(screen.getByText("DH12")).toBeInTheDocument();
  expect(screen.getByText("Chưa thanh toán")).toBeInTheDocument();
  expect(screen.queryByRole("button", { name: "Đối soát tiền vào" })).not.toBeInTheDocument();
  expect(apiClient).toHaveBeenCalledWith({ url: "/orders/12/payment", method: "post", timeout: 45000 });
  fireEvent.error(screen.getByRole("img", { name: "Mã VietQR thanh toán đơn hàng" }));
  expect(screen.getByText("0000000000")).toBeInTheDocument();
});
it("admin must provide reconciliation details before recording actual money received", async () => {
  useAuth.mockReturnValue({ user: { role: "ADMIN" } });
  const onUpdate = vi.fn();
  apiClient.mockImplementation(({ method }) => Promise.resolve(response(method === "post" ? { ...unpaid, status: "PAID" } : unpaid)));
  render(<PaymentPanel order={order} onUpdate={onUpdate} />);
  fireEvent.click(await screen.findByRole("button", { name: "Đối soát tiền vào" }));
  fireEvent.change(screen.getByLabelText("Mã giao dịch trên sao kê"), { target: { value: "BANK-123" } });
  fireEvent.click(screen.getByRole("checkbox"));
  fireEvent.click(screen.getByRole("button", { name: "Xác nhận", exact: true }));
  await screen.findByText("Thanh toán đã được xác nhận.");
  expect(apiClient).toHaveBeenCalledWith({ url: "/admin/orders/12/payment/confirm", method: "post", data: { amount: 499000, reference: "BANK-123" } });
  expect(onUpdate).toHaveBeenCalled();
});
it("expired and cancelled orders cannot open payment again", async () => {
  useAuth.mockReturnValue({ user: { role: "USER" } });
  apiClient.mockResolvedValue(response({ ...unpaid, method: "VNPAY", expired: true, message: "Phiên đã hết hạn" }));
  const view = render(<PaymentPanel order={{ ...order, paymentMethod: "VNPAY" }} onUpdate={vi.fn()} />);
  await screen.findByText("Phiên đã hết hạn");
  expect(screen.queryByRole("button", { name: "Thanh toán với VNPAY" })).not.toBeInTheDocument();
  view.rerender(<PaymentPanel order={{ ...order, orderStatus: "CANCELLED" }} onUpdate={vi.fn()} />);
  expect(screen.queryByRole("button", { name: "Thông tin chuyển khoản / QR" })).not.toBeInTheDocument();
});
