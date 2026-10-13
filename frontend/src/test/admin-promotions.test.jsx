import { afterEach, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import AdminPromotionsPage from "../pages/AdminPromotionsPage";
import { apiClient } from "../services/apiClient";

vi.mock("../services/apiClient", () => ({ apiClient: vi.fn(), errorMessage: () => "Không lưu được ưu đãi" }));
afterEach(() => { cleanup(); vi.resetAllMocks(); });
const product = { id: 5, productName: "Vợt admin chọn", price: 1500000, salePrice: null, effectivePrice: 1500000, promotionPublished: false, status: "ACTIVE", stock: 3 };
const response = data => ({ data: { data } });
const list = content => response({ content, page: 0, size: 12, totalPages: 1, totalElements: content.length });
function mockCatalog(p = product) { apiClient.mockImplementation(({ method }) => Promise.resolve(method === "get" ? list([p]) : response({ ...p, promotionPublished: true, salePrice: 1200000, effectivePrice: 1200000 }))); }
async function view() {
  window.matchMedia = vi.fn(() => ({ matches: false }));
  render(<MemoryRouter><AdminPromotionsPage /></MemoryRouter>);
  await screen.findByRole("button", { name: "Chọn Vợt admin chọn" });
}
async function select() { await view(); fireEvent.click(screen.getByRole("button", { name: "Chọn Vợt admin chọn" })); }

it("requires explicit product selection and publishes a percentage with a price preview", async () => {
  mockCatalog(); await view();
  expect(screen.getByText("Chưa chọn sản phẩm")).toBeInTheDocument();
  expect(apiClient).toHaveBeenCalledTimes(1);
  fireEvent.click(screen.getByRole("button", { name: "Chọn Vợt admin chọn" }));
  expect(within(screen.getByRole("region", { name: "Thiết lập ưu đãi" })).getByText(/1\.200\.000/)).toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Đăng ưu đãi" }));
  await screen.findByText("Đã đăng ưu đãi lên cửa hàng.");
  expect(apiClient).toHaveBeenCalledWith({ url: "/admin/products/5/promotion", method: "put", data: { discountType: "PERCENT", value: 20 } });
  expect(screen.getByRole("button", { name: "Cập nhật ưu đãi" })).toBeInTheDocument();
});

it("accepts an admin-defined selling price and blocks invalid discounts", async () => {
  mockCatalog(); await select();
  fireEvent.change(screen.getByLabelText("Mức giảm (%)"), { target: { value: "101" } });
  expect(screen.getByRole("button", { name: "Đăng ưu đãi" })).toBeDisabled();
  fireEvent.click(screen.getByRole("radio", { name: "Giá bán ưu đãi" }));
  const input = screen.getByLabelText("Giá ưu đãi (₫)");
  fireEvent.change(input, { target: { value: "1500000" } });
  expect(screen.getByRole("button", { name: "Đăng ưu đãi" })).toBeDisabled();
  fireEvent.change(input, { target: { value: "1000000" } });
  fireEvent.click(screen.getByRole("button", { name: "Đăng ưu đãi" }));
  await waitFor(() => expect(apiClient).toHaveBeenCalledWith({ url: "/admin/products/5/promotion", method: "put", data: { discountType: "SALE_PRICE", value: 1000000 } }));
});

it("requires confirmation to end an offer and restores the server price", async () => {
  const live = { ...product, salePrice: 1200000, effectivePrice: 1200000, promotionPublished: true };
  apiClient.mockImplementation(({ method }) => Promise.resolve(method === "get" ? list([live]) : response(product)));
  await select();
  fireEvent.click(screen.getByRole("button", { name: "Kết thúc ưu đãi", exact: true }));
  expect(screen.getByRole("group", { name: "Xác nhận kết thúc ưu đãi" })).toBeInTheDocument();
  expect(apiClient).toHaveBeenCalledTimes(1);
  fireEvent.click(screen.getByRole("button", { name: "Hủy" }));
  expect(screen.queryByRole("group", { name: "Xác nhận kết thúc ưu đãi" })).toBeNull();
  fireEvent.click(screen.getByRole("button", { name: "Kết thúc ưu đãi", exact: true }));
  fireEvent.click(screen.getByRole("button", { name: "Kết thúc", exact: true }));
  await screen.findByText("Đã kết thúc ưu đãi và khôi phục giá gốc.");
  expect(apiClient).toHaveBeenCalledWith({ url: "/admin/products/5/promotion", method: "delete", data: undefined });
});

it("keeps values and allows retry after a server error", async () => {
  mockCatalog(); await select();
  apiClient.mockRejectedValueOnce(new Error("offline"));
  fireEvent.click(screen.getByRole("button", { name: "Đăng ưu đãi" }));
  expect(await screen.findByRole("alert")).toHaveTextContent("Không lưu được ưu đãi");
  expect(screen.getByLabelText("Mức giảm (%)")).toHaveValue(20);
  fireEvent.click(screen.getByRole("button", { name: "Đăng ưu đãi" }));
  await screen.findByText("Đã đăng ưu đãi lên cửa hàng.");
});

it.each([{ status: "INACTIVE" }, { price: 0 }])("cannot publish an unavailable product (%j)", async change => {
  mockCatalog({ ...product, ...change }); await select();
  expect(screen.getByRole("button", { name: "Đăng ưu đãi" })).toBeDisabled();
  expect(screen.getByRole("alert")).toBeInTheDocument();
});

it("searches and filters the server-side admin catalog", async () => {
  mockCatalog(); await view();
  fireEvent.change(screen.getByLabelText("Tìm sản phẩm"), { target: { value: "Vợt & giày" } });
  fireEvent.click(screen.getByRole("button", { name: "Tìm kiếm" }));
  fireEvent.change(screen.getByLabelText("Trạng thái ưu đãi"), { target: { value: "true" } });
  await waitFor(() => expect(apiClient).toHaveBeenCalledWith(expect.objectContaining({ url: "/admin/products?page=0&size=12&keyword=V%E1%BB%A3t+%26+gi%C3%A0y&published=true" })));
});
