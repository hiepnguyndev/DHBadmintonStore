import { afterEach, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import HomePage from "../pages/HomePage";
import { PromotionsPage } from "../pages/ShopPages";
import { ChatbotProvider } from "../context/ChatbotContext";
import { ProductCard } from "../components/CommerceUI";
import { apiClient } from "../services/apiClient";

vi.mock("../services/apiClient", () => ({ apiClient: vi.fn(), errorMessage: () => "Mất kết nối" }));
afterEach(() => { cleanup(); vi.resetAllMocks(); });
const product = { id: 1, productName: "Vợt ưu đãi", price: 1500000, salePrice: 1200000, effectivePrice: 1200000, status: "ACTIVE", stock: 2 };
const result = content => ({ data: { data: { content, page: 0, size: 4, totalPages: content.length ? 1 : 0, totalElements: content.length } } });
function view(node = <HomePage />) {
  return render(<MemoryRouter><ChatbotProvider>{node}</ChatbotProvider></MemoryRouter>);
}

it("replaces profile promotion with real offers and adds community below the catalog", async () => {
  apiClient.mockImplementation(({ url }) => Promise.resolve(result(url.includes("onSale=true") ? [product] : [])));
  view();
  const offers = screen.getByRole("region", { name: "Sản phẩm ưu đãi" });
  expect(await within(offers).findByText("Vợt ưu đãi")).toBeInTheDocument();
  expect(within(offers).getByText("-20%")).toBeInTheDocument();
  expect(within(offers).getByText(/1\.500\.000/).tagName).toBe("DEL");
  expect(screen.queryByText("GỢI Ý THEO HỒ SƠ")).not.toBeInTheDocument();
  expect(screen.queryByText("PLAYER DNA")).not.toBeInTheDocument();
  expect(apiClient).toHaveBeenCalledWith(expect.objectContaining({ url: "/products?onSale=true&page=0&size=4" }));
  const community = screen.getByRole("region", { name: "Cộng đồng & Tin tức" });
  expect(community.compareDocumentPosition(offers) & Node.DOCUMENT_POSITION_PRECEDING).toBeTruthy();
  expect(within(community).getAllByRole("article")).toHaveLength(3);
  expect(within(community).getByRole("link", { name: /Dụng cụ cũ, hành trình mới/ })).toHaveAttribute("href", "/marketplace");
  const news = within(community).getAllByRole("link", { name: /Đọc tại Yonex/ });
  expect(news).toHaveLength(2);
  for (const link of news) {
    expect(link).toHaveAttribute("href", expect.stringContaining("https://www.yonex.com/news/"));
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noopener noreferrer");
  }
});

it("shows honest empty offers without hiding community or inventing products", async () => {
  apiClient.mockResolvedValue(result([]));
  view();
  expect(await screen.findByText("Chưa có sản phẩm ưu đãi")).toBeInTheDocument();
  expect(screen.getByRole("region", { name: "Cộng đồng & Tin tức" })).toBeInTheDocument();
  expect(document.querySelectorAll(".home-offers .product-card")).toHaveLength(0);
  expect(screen.getByRole("link", { name: "Tất cả ưu đãi" })).toHaveAttribute("href", "/promotions");
});

it("retries only the failed offer query and leaves other sections available", async () => {
  let offerCalls = 0;
  apiClient.mockImplementation(({ url }) => {
    if (url.includes("onSale=true") && ++offerCalls === 1) return Promise.reject(new Error("offline"));
    return Promise.resolve(result(url.includes("onSale=true") ? [product] : []));
  });
  view();
  const offers = screen.getByRole("region", { name: "Sản phẩm ưu đãi" });
  expect(await within(offers).findByRole("alert")).toHaveTextContent("Mất kết nối");
  fireEvent.click(within(offers).getByRole("button", { name: "Thử lại" }));
  expect(await within(offers).findByText("Vợt ưu đãi")).toBeInTheDocument();
  expect(offerCalls).toBe(2);
});

it.each([null, 1500000])("does not label non-discounted products as offers (%s)", salePrice => {
  view(<ProductCard product={{ ...product, salePrice }} />);
  expect(document.querySelector(".sale-badge")).toBeNull();
  expect(document.querySelector("del")).toBeNull();
});

it("paginates offers using the server filter rather than a truncated general catalog", async () => {
  apiClient.mockResolvedValue({ data: { data: { ...result([product]).data.data, size: 12, totalPages: 2, totalElements: 13 } } });
  view(<PromotionsPage />);
  await screen.findByText("Vợt ưu đãi");
  fireEvent.click(screen.getByRole("button", { name: "Sau" }));
  await waitFor(() => expect(apiClient).toHaveBeenCalledWith(expect.objectContaining({ url: "/products?onSale=true&page=1&size=12" })));
});
