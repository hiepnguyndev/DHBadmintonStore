import { afterEach, beforeEach, expect, it, vi } from "vitest";
import { act, cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { ChatbotProvider, useChatbot } from "../context/ChatbotContext";
import { useAuth } from "../context/AuthContext";
import ChatbotWidget from "../components/ChatbotWidget";
import { apiClient } from "../services/apiClient";

vi.mock("../context/AuthContext", () => ({ useAuth: vi.fn() }));
vi.mock("../services/apiClient", () => ({ apiClient: vi.fn(), errorMessage: () => "Mất kết nối" }));
afterEach(() => { cleanup(); vi.resetAllMocks(); });
beforeEach(() => { useAuth.mockReturnValue({ user: null }); });
const response = { data: { data: { answer: "Chưa có sản phẩm phù hợp", products: [], engine: "LOCAL_RULES" } } };
const profileResponse = { data: { data: [{
  productId: 8, matchScore: 73, reasons: ["Phù hợp với lực cổ tay"],
  cautions: ["Giá vượt ngân sách"],
  product: { id: 8, productName: "Vợt từ hồ sơ", effectivePrice: 1200000, thumbnailUrl: null },
}] } };
const member = { id: 1 };

function ProfileOpener() {
  const { openChatbot } = useChatbot();
  return <button onClick={() => openChatbot("profile")}>Mở gợi ý cá nhân</button>;
}
function App() {
  return <MemoryRouter><ChatbotProvider><ProfileOpener /><ChatbotWidget /></ChatbotProvider></MemoryRouter>;
}

function view() {
  const result = render(<App />);
  fireEvent.click(screen.getByRole("button", { name: "Mở DH Chatbot" }));
  return result;
}

it("opens without making a request and returns focus when closed", async () => {
  view();
  expect(screen.getByRole("dialog", { name: "DH Chatbot" })).toBeInTheDocument();
  expect(screen.getByRole("textbox", { name: "Nhập tin nhắn" })).toHaveFocus();
  expect(apiClient).not.toHaveBeenCalled();
  fireEvent.keyDown(document, { key: "Escape" });
  expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
});

it("sends trimmed messages with only successful bounded user history", async () => {
  apiClient.mockResolvedValue(response);
  view();
  const input = screen.getByRole("textbox", { name: "Nhập tin nhắn" });
  fireEvent.change(input, { target: { value: "  Vợt dưới 1 triệu  " } });
  fireEvent.click(screen.getByRole("button", { name: "Gửi tin nhắn" }));
  await screen.findByText(response.data.data.answer);
  expect(apiClient.mock.calls[0][0].data).toEqual({ message: "Vợt dưới 1 triệu", history: [], language: "vi" });
  await waitFor(() => expect(screen.getByRole("button", { name: "Gửi tin nhắn" })).toBeDisabled());
  fireEvent.change(input, { target: { value: "Màu xanh" } });
  fireEvent.keyDown(input, { key: "Enter" });
  await waitFor(() => expect(apiClient).toHaveBeenCalledTimes(2));
  expect(apiClient.mock.calls[1][0].data.history).toEqual(["Vợt dưới 1 triệu"]);
});

it("keeps failed messages retryable without adding duplicate user bubbles", async () => {
  apiClient.mockRejectedValueOnce(new Error("offline")).mockResolvedValue(response);
  view();
  fireEvent.click(screen.getByRole("button", { name: "Vợt dưới 1 triệu" }));
  expect(await screen.findByRole("alert")).toHaveTextContent("Mất kết nối");
  fireEvent.click(screen.getByRole("button", { name: "Thử lại" }));
  expect(await screen.findByText(response.data.data.answer)).toBeInTheDocument();
  expect(screen.getAllByText("Vợt dưới 1 triệu")).toHaveLength(1);
  expect(apiClient.mock.calls[1][0].data.history).toEqual([]);
});

it("prevents duplicate sends and discards an aborted reply after reset", async () => {
  let finish;
  apiClient.mockImplementation(() => new Promise(resolve => { finish = resolve; }));
  view();
  fireEvent.click(screen.getByRole("button", { name: "Vợt dưới 1 triệu" }));
  expect(screen.getByRole("button", { name: "Gửi tin nhắn" })).toBeDisabled();
  fireEvent.click(screen.getByRole("button", { name: "Cuộc trò chuyện mới" }));
  expect(apiClient.mock.calls[0][0].signal.aborted).toBe(true);
  finish(response);
  await waitFor(() => expect(screen.getByRole("button", { name: "Vợt dưới 1 triệu" })).toBeInTheDocument());
  expect(screen.queryByText(response.data.data.answer)).not.toBeInTheDocument();
});

it("renders untrusted message and catalog text as plain text", async () => {
  apiClient.mockResolvedValue({ data: { data: { answer: "<script>alert(1)</script>", products: [] } } });
  view();
  fireEvent.click(screen.getByRole("button", { name: "Vợt dưới 1 triệu" }));
  expect(await screen.findByText("<script>alert(1)</script>")).toBeInTheDocument();
  expect(document.querySelector(".chatbot-log script")).toBeNull();
});

it("asks guests to sign in without requesting private recommendations", async () => {
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  expect(await screen.findByRole("link", { name: "Đăng nhập", exact: true })).toHaveAttribute("href", "/login");
  expect(apiClient).not.toHaveBeenCalled();
});

it("uses saved-profile results, server score, reasons and cautions", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockResolvedValue(profileResponse);
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  expect(await screen.findByText("73% phù hợp")).toBeInTheDocument();
  expect(screen.getByText("Phù hợp với lực cổ tay")).toBeInTheDocument();
  expect(screen.getByText("Giá vượt ngân sách")).toBeInTheDocument();
  expect(screen.getByRole("link", { name: /Vợt từ hồ sơ/ })).toHaveAttribute("href", "/products/8");
  expect(apiClient).toHaveBeenCalledWith(expect.objectContaining({ url: "/recommendations", method: "get", params: { limit: 3 } }));
  expect(apiClient.mock.calls[0][0]).not.toHaveProperty("data");
});

it("routes typed profile requests privately and excludes them from public chat history", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockResolvedValueOnce(profileResponse).mockResolvedValue(response);
  view();
  const input = screen.getByRole("textbox", { name: "Nhập tin nhắn" });
  fireEvent.change(input, { target: { value: "Tư vấn dựa trên hồ sơ của tôi" } });
  fireEvent.keyDown(input, { key: "Enter" });
  await screen.findByText("73% phù hợp");
  expect(apiClient.mock.calls[0][0].url).toBe("/recommendations");
  fireEvent.change(input, { target: { value: "Giày dưới 2 triệu" } });
  fireEvent.keyDown(input, { key: "Enter" });
  await screen.findByText(response.data.data.answer);
  expect(apiClient.mock.calls[1][0].data).toEqual({ message: "Giày dưới 2 triệu", history: [], language: "vi" });
});

it("offers profile setup for PROFILE_REQUIRED, not a retry or invented result", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockRejectedValue({ response: { status: 404, data: { code: "PROFILE_REQUIRED" } } });
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  expect(await screen.findByText("Bạn chưa có hồ sơ người chơi để nhận gợi ý cá nhân hóa.")).toBeInTheDocument();
  expect(screen.getByRole("link", { name: "Cập nhật hồ sơ người chơi" })).toHaveAttribute("href", "/player-profile");
  expect(screen.queryByRole("button", { name: "Thử lại" })).not.toBeInTheDocument();
});

it("handles an empty recommendation catalog without demo products", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockResolvedValue({ data: { data: [] } });
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  expect(await screen.findByText("Hiện chưa có vợt còn hàng để gợi ý từ hồ sơ của bạn.")).toBeInTheDocument();
  expect(document.querySelectorAll(".chatbot-profile-results article")).toHaveLength(0);
});

it("retries profile requests using the private endpoint without duplicate prompts", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockRejectedValueOnce(new Error("offline")).mockResolvedValue(profileResponse);
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  await screen.findByRole("alert");
  fireEvent.click(screen.getByRole("button", { name: "Thử lại" }));
  await screen.findByText("73% phù hợp");
  expect(apiClient.mock.calls.map(([call]) => call.url)).toEqual(["/recommendations", "/recommendations"]);
  expect(document.querySelectorAll(".chatbot-message.user")).toHaveLength(1);
});

it("aborts pending profile results when reset and consumes the external trigger once", async () => {
  useAuth.mockReturnValue({ user: member });
  let finish;
  apiClient.mockImplementation(() => new Promise(resolve => { finish = resolve; }));
  view();
  fireEvent.click(screen.getByRole("button", { name: "Mở gợi ý cá nhân" }));
  await waitFor(() => expect(apiClient).toHaveBeenCalledTimes(1));
  fireEvent.click(screen.getByRole("button", { name: "Cuộc trò chuyện mới" }));
  expect(apiClient.mock.calls[0][0].signal.aborted).toBe(true);
  await act(async () => finish(profileResponse));
  fireEvent.click(screen.getByRole("button", { name: "Đóng DH Chatbot" }));
  fireEvent.click(screen.getByRole("button", { name: "Mở DH Chatbot" }));
  expect(apiClient).toHaveBeenCalledTimes(1);
  expect(screen.queryByText("73% phù hợp")).not.toBeInTheDocument();
});

it("clears private recommendations when the signed-in account changes", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockResolvedValue(profileResponse);
  const result = view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  await screen.findByText("73% phù hợp");
  useAuth.mockReturnValue({ user: { id: 2 } });
  result.rerender(<App />);
  expect(screen.queryByText("Vợt từ hồ sơ")).not.toBeInTheDocument();
  expect(screen.getByRole("button", { name: "Vợt dưới 1 triệu" })).toBeInTheDocument();
});

it("shows a sign-in action for an expired profile request", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockRejectedValue({ response: { status: 401, data: {} } });
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  expect(await screen.findByRole("link", { name: "Đăng nhập", exact: true })).toBeInTheDocument();
  expect(screen.queryByRole("alert")).not.toBeInTheDocument();
});

it("queues an external profile trigger while a general chat request finishes", async () => {
  useAuth.mockReturnValue({ user: member });
  let finish;
  apiClient.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; })).mockResolvedValue(profileResponse);
  view();
  fireEvent.click(screen.getByRole("button", { name: "Vợt dưới 1 triệu" }));
  fireEvent.click(screen.getByRole("button", { name: "Mở gợi ý cá nhân" }));
  expect(apiClient).toHaveBeenCalledTimes(1);
  await act(async () => finish(response));
  await screen.findByText("73% phù hợp");
  expect(apiClient.mock.calls.map(([call]) => call.url)).toEqual(["/ai/chat", "/recommendations"]);
});

it("treats a malformed recommendation as a retryable error instead of crashing", async () => {
  useAuth.mockReturnValue({ user: member });
  apiClient.mockResolvedValue({ data: { data: [{ productId: 1 }] } });
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  expect(await screen.findByRole("alert")).toHaveTextContent("Mất kết nối");
});

it("explains role restrictions without changing backend permissions", async () => {
  useAuth.mockReturnValue({ user: { id: 1, role: "ADMIN" } });
  apiClient.mockRejectedValue({ response: { status: 403, data: {} } });
  view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  expect(await screen.findByText("Gợi ý từ hồ sơ dành cho tài khoản người chơi. Bạn vẫn có thể hỏi DH Chatbot về sản phẩm.")).toBeInTheDocument();
  expect(screen.queryByRole("button", { name: "Thử lại" })).not.toBeInTheDocument();
});

it.each([null, { id: 2 }])("discards pending private replies on an account change to %j", async nextUser => {
  useAuth.mockReturnValue({ user: member });
  let finish;
  apiClient.mockImplementation(() => new Promise(resolve => { finish = resolve; }));
  const result = view();
  fireEvent.click(screen.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }));
  fireEvent.click(screen.getByRole("button", { name: "Mở gợi ý cá nhân" }));
  useAuth.mockReturnValue({ user: nextUser });
  result.rerender(<App />);
  expect(apiClient.mock.calls[0][0].signal.aborted).toBe(true);
  await act(async () => finish(profileResponse));
  expect(apiClient).toHaveBeenCalledTimes(1);
  expect(screen.queryByText("Vợt từ hồ sơ")).not.toBeInTheDocument();
});
