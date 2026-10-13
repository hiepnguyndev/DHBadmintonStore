import { afterEach, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import AdminPostsPage from "../pages/AdminPostsPage";
import { NewsDetailPage, NewsPage } from "../pages/NewsPages";
import HomePage from "../pages/HomePage";
import { ChatbotProvider } from "../context/ChatbotContext";
import { apiClient } from "../services/apiClient";

vi.mock("../services/apiClient", () => ({ apiClient: vi.fn(), errorMessage: () => "Không lưu được bài viết" }));
afterEach(() => { cleanup(); vi.resetAllMocks(); });
const post = { id: 10, title: "Tin của DH", summary: "Thông báo từ shop", content: "Dòng thứ nhất\n<script>window.bad=true</script>", category: "NEWS", status: "PUBLISHED", publishedAt: "2026-10-13T02:00:00Z", updatedAt: "2026-10-13T02:00:00Z" };
const response = data => ({ data: { data } });
const list = content => response({ content, page: 0, size: 12, totalPages: content.length ? 1 : 0, totalElements: content.length });
function view(node) {
  window.matchMedia = vi.fn(() => ({ matches: false }));
  return render(<MemoryRouter>{node}</MemoryRouter>);
}
function fillPost() {
  fireEvent.change(screen.getByLabelText("Tiêu đề bài viết"), { target: { value: post.title } });
  fireEvent.change(screen.getByLabelText("Tóm tắt"), { target: { value: post.summary } });
  fireEvent.change(screen.getByLabelText("Nội dung bài viết"), { target: { value: post.content } });
}
function readBlob(blob) { return new Promise(resolve => { const reader = new FileReader(); reader.onload = () => resolve(reader.result); reader.readAsText(blob); }); }

it("includes shop posts alongside community and publisher stories on the home page", async () => {
  apiClient.mockImplementation(({ url }) => Promise.resolve(list(url.startsWith("/posts") ? [post] : [])));
  view(<ChatbotProvider><HomePage /></ChatbotProvider>);
  const community = screen.getByRole("region", { name: "Cộng đồng & Tin tức" });
  expect(await within(community).findByText(post.title)).toBeInTheDocument();
  expect(within(community).getAllByRole("article")).toHaveLength(4);
  expect(within(community).getByRole("link", { name: /Tin của DH/ })).toHaveAttribute("href", "/news/10");
  expect(apiClient).toHaveBeenCalledWith(expect.objectContaining({ url: "/posts?page=0&size=3" }));
});

it("renders article content as text, never executable HTML", async () => {
  apiClient.mockResolvedValue(response(post));
  render(<MemoryRouter initialEntries={["/news/10"]}><Routes><Route path="/news/:id" element={<NewsDetailPage />} /></Routes></MemoryRouter>);
  await screen.findByRole("heading", { name: post.title });
  expect(document.querySelector(".article-content")).toHaveTextContent("<script>window.bad=true</script>");
  expect(document.querySelector(".article-content script")).toBeNull();
});

it("filters the public shop news list and shows an honest empty state", async () => {
  apiClient.mockResolvedValue(list([])); view(<NewsPage />);
  await screen.findByText("Chưa có bài viết");
  fireEvent.change(screen.getByLabelText("Chuyên mục"), { target: { value: "COMMUNITY" } });
  await waitFor(() => expect(apiClient).toHaveBeenCalledWith(expect.objectContaining({ url: "/posts?page=0&size=12&category=COMMUNITY" })));
});

it("saves a draft then publishes through explicit admin commands", async () => {
  const writes = [];
  apiClient.mockImplementation(async ({ method, data }) => {
    if (method === "get") return list([]);
    const payload = JSON.parse(await readBlob(data.get("post"))); writes.push(payload);
    return response({ ...post, ...payload });
  });
  view(<AdminPostsPage />); await screen.findByText("Chưa có bài viết"); fillPost();
  fireEvent.click(screen.getByRole("button", { name: "Lưu bản nháp", exact: true }));
  await screen.findByText("Đã lưu bản nháp."); expect(writes[0].status).toBe("DRAFT");
  expect(writes[0].content).toBe(post.content);
  fireEvent.click(screen.getByRole("button", { name: "Đăng bài", exact: true }));
  await screen.findByText("Đã đăng bài viết lên cửa hàng."); expect(writes[1].status).toBe("PUBLISHED");
  expect(apiClient).toHaveBeenCalledWith(expect.objectContaining({ url: "/admin/posts/10", method: "put" }));
});

it("confirms archive and permits restoring an archived post", async () => {
  apiClient.mockImplementation(({ method }) => Promise.resolve(method === "get" ? list([post]) : response({ ...post, status: "ARCHIVED" })));
  view(<AdminPostsPage />); fireEvent.click(await screen.findByRole("button", { name: "Sửa Tin của DH" }));
  fireEvent.click(screen.getByRole("button", { name: "Lưu trữ", exact: true }));
  expect(screen.getByRole("group", { name: "Xác nhận gỡ bài viết" })).toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Hủy" }));
  expect(apiClient).toHaveBeenCalledTimes(1);
  fireEvent.click(screen.getByRole("button", { name: "Lưu trữ", exact: true }));
  fireEvent.click(screen.getByRole("button", { name: "Lưu trữ bài viết", exact: true }));
  await screen.findByText("Đã lưu trữ bài viết.");
  expect(apiClient).toHaveBeenCalledWith({ url: "/admin/posts/10", method: "delete", data: undefined });
  expect(screen.getByRole("button", { name: "Đăng bài" })).toBeInTheDocument();
});

it("retains unsaved text after errors and rejects unsupported cover files", async () => {
  apiClient.mockResolvedValue(list([])); view(<AdminPostsPage />);
  await screen.findByText("Chưa có bài viết"); fillPost();
  fireEvent.change(screen.getByLabelText("Ảnh bìa PNG/JPEG, tối đa 5 MB"), { target: { files: [new File(["svg"], "bad.svg", { type: "image/svg+xml" })] } });
  expect(screen.getByRole("alert")).toHaveTextContent("Chỉ hỗ trợ ảnh PNG/JPEG");
  apiClient.mockRejectedValueOnce(new Error("offline"));
  fireEvent.click(screen.getByRole("button", { name: "Đăng bài" }));
  await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent("Không lưu được bài viết"));
  expect(screen.getByLabelText("Nội dung bài viết")).toHaveValue(post.content);
});
