import { afterEach, expect, it, vi } from "vitest";
import {
  cleanup,
  render,
  screen,
  fireEvent,
  waitFor,
} from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import {
  RecommendationsPage,
  PlayerProfilePage,
} from "../pages/RecommendationPages";
import { SellerProfilePage } from "../pages/MarketplacePages";
import { Form } from "../components/CommerceUI";
import { apiClient } from "../services/apiClient";
import { ChatbotProvider } from "../context/ChatbotContext";
vi.mock("../services/apiClient", () => ({
  apiClient: vi.fn(),
  errorMessage: (e) => e.response?.data?.message || "Mất kết nối",
}));
afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});
function view(node) {
  return render(<MemoryRouter><ChatbotProvider>{node}</ChatbotProvider></MemoryRouter>);
}
it("shows the server match score and reasons without deriving a frontend score", async () => {
  apiClient.mockResolvedValue({
    data: {
      data: [
        {
          productId: 1,
          matchScore: 73,
          reasons: ["Phù hợp với lực cổ tay"],
          cautions: ["Giá vượt ngân sách"],
          breakdown: {
            skill: 12,
            playStyle: 18,
            wrist: 15,
            weight: 10,
            flex: 6,
            budget: 12,
          },
          product: {
            id: 1,
            productName: "Vợt thử",
            effectivePrice: 1200000,
            stock: 1,
            status: "ACTIVE",
            brand: { name: "Demo" },
            specification: { weightClass: "4U" },
          },
        },
      ],
    },
  });
  view(<RecommendationsPage />);
  expect(await screen.findByText("73% phù hợp")).toBeInTheDocument();
  expect(screen.getByText("Phù hợp với lực cổ tay")).toBeInTheDocument();
  expect(screen.getByText("Giá vượt ngân sách")).toBeInTheDocument();
});
it("offers profile creation only when backend reports PROFILE_REQUIRED", async () => {
  apiClient.mockRejectedValue({
    response: { data: { code: "PROFILE_REQUIRED", message: "Chưa có hồ sơ" } },
  });
  view(<PlayerProfilePage />);
  expect(
    await screen.findByRole("button", { name: "Lưu và xem gợi ý" }),
  ).toBeInTheDocument();
});
it("does not turn a network failure into a create-profile form", async () => {
  apiClient.mockRejectedValue(new Error("Network"));
  view(<PlayerProfilePage />);
  expect(await screen.findByRole("alert")).toHaveTextContent("Mất kết nối");
  expect(
    screen.queryByRole("button", { name: "Lưu và xem gợi ý" }),
  ).not.toBeInTheDocument();
});
it("new seller rates stay unknown, never 0 percent", async () => {
  apiClient.mockResolvedValue({
    data: {
      data: {
        fullName: "Seller mới",
        joinedAt: "2026-10-01T00:00:00",
        completedTransactions: 0,
        ratingCount: 0,
        averageRating: null,
        reputation: {
          newSeller: true,
          disputeRate: null,
          cancellationRate: null,
          reputationScore: 2,
        },
      },
    },
  });
  render(
    <MemoryRouter initialEntries={["/sellers/3"]}>
      <Routes>
        <Route path="/sellers/:id" element={<SellerProfilePage />} />
      </Routes>
    </MemoryRouter>,
  );
  expect(await screen.findByText("Người bán mới")).toBeInTheDocument();
  expect(
    screen.getByText("Tỷ lệ tranh chấp: Chưa có dữ liệu"),
  ).toBeInTheDocument();
  expect(
    screen.getByText("Tỷ lệ hủy do người bán: Chưa có dữ liệu"),
  ).toBeInTheDocument();
});
it("prevents double submit while waiting and displays the server error", async () => {
  let reject;
  const onSubmit = vi.fn(
    () =>
      new Promise((_, r) => {
        reject = r;
      }),
  );
  view(
    <Form
      fields={[{ name: "name", label: "Tên" }]}
      initial={{ name: "Player" }}
      onSubmit={onSubmit}
    />,
  );
  const button = screen.getByRole("button", { name: "Lưu" });
  fireEvent.click(button);
  expect(button).toBeDisabled();
  await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1));
  reject({ response: { data: { message: "Dữ liệu chưa hợp lệ" } } });
  expect(await screen.findByRole("alert")).toHaveTextContent(
    "Dữ liệu chưa hợp lệ",
  );
  expect(screen.getByRole("button", { name: "Lưu" })).toBeEnabled();
});
it("binary switches preserve the API's true/false string values", async () => {
  const onSubmit = vi.fn().mockResolvedValue(undefined);
  view(<Form fields={[{ name: "modelVerified", label: "Model đúng", options: [{ value: "false", label: "Không" }, { value: "true", label: "Có" }] }]} initial={{ modelVerified: "false" }} onSubmit={onSubmit} />);
  const toggle = screen.getByRole("switch");
  expect(toggle).not.toBeChecked();
  fireEvent.click(screen.getByRole("button", { name: "Lưu" }));
  await waitFor(() => expect(onSubmit).toHaveBeenLastCalledWith({ modelVerified: "false" }));
  await waitFor(() => expect(screen.getByRole("button", { name: "Lưu" })).toBeEnabled());
  fireEvent.click(toggle);
  expect(toggle).toBeChecked();
  fireEvent.click(screen.getByRole("button", { name: "Lưu" }));
  await waitFor(() => expect(onSubmit).toHaveBeenLastCalledWith({ modelVerified: "true" }));
});
