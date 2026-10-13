import { test, expect } from "@playwright/test";

const product = { id: 1, productName: "Vợt tư vấn kiểm tra", effectivePrice: 850000, price: 1000000, stock: 4, status: "ACTIVE", thumbnailUrl: "/images/gear-racket.jpg", brand: { name: "DH" }, images: [], category: { name: "Vợt" } };
const noMatch = "Cửa hàng hiện chưa có sản phẩm còn hàng đáp ứng các điều kiện này.";

async function setup(page) {
  const requests = [];
  await page.route("**/api/**", async route => {
    const path = new URL(route.request().url()).pathname;
    let data;
    if (path === "/api/ai/chat") {
      const body = route.request().postDataJSON();
      requests.push(body);
      if (body.message === "lỗi mạng" && requests.filter(item => item.message === "lỗi mạng").length === 1) {
        return route.fulfill({ status: 503, contentType: "application/json", body: JSON.stringify({ success: false, message: "Tạm mất kết nối" }) });
      }
      data = { answer: body.language === "en" ? "Here are the available products." : body.message === "không có hàng" ? noMatch : "Mình tìm thấy sản phẩm phù hợp.", products: body.message === "không có hàng" ? [] : [product], engine: "LOCAL_RULES" };
    } else if (path === "/api/products/1") data = product;
    else if (path.endsWith("/variants")) data = [];
    else data = { content: [], totalElements: 0, totalPages: 0, page: 0, size: 12 };
    await route.fulfill({ contentType: "application/json", body: JSON.stringify({ success: true, data }) });
  });
  await page.goto("/");
  await expect(page.locator("h1")).toBeVisible();
  return requests;
}

for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }, { width: 360, height: 640 }]) {
  test.describe(`DH Chatbot ${viewport.width}px`, () => {
    test.use({ viewport });

    test("corner widget chats, preserves history and links to real product routes", async ({ page }) => {
      const errors = [];
      page.on("pageerror", error => errors.push(error.message));
      const requests = await setup(page);
      await expect(page.getByText(/DH MATCH/i)).toHaveCount(0);
      const launcher = page.getByRole("button", { name: "Mở DH Chatbot", exact: true });
      const bounds = await launcher.boundingBox();
      expect(bounds.x).toBeGreaterThan(viewport.width - 200);
      expect(bounds.y).toBeGreaterThan(viewport.height - 90);
      await launcher.click();
      const panel = page.getByRole("dialog", { name: "DH Chatbot" });
      await expect(panel).toBeVisible();
      await expect(page.getByRole("textbox", { name: "Nhập tin nhắn" })).toBeFocused();
      await page.getByRole("button", { name: "Vợt dưới 1 triệu", exact: true }).click();
      await expect(panel.getByRole("link", { name: /Vợt tư vấn kiểm tra/ })).toBeVisible();
      await page.getByRole("textbox", { name: "Nhập tin nhắn" }).fill("Màu xanh");
      await page.getByRole("button", { name: "Gửi tin nhắn" }).click();
      await expect(panel.locator(".chatbot-pending")).toHaveCount(0);
      expect(requests[1].history).toEqual(["Vợt dưới 1 triệu"]);
      const box = await panel.boundingBox();
      expect(box.x).toBeGreaterThanOrEqual(0);
      expect(box.y).toBeGreaterThanOrEqual(0);
      expect(box.x + box.width).toBeLessThanOrEqual(viewport.width);
      expect(box.y + box.height).toBeLessThan(viewport.height);
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
      expect(await panel.locator("img").evaluateAll(images => images.every(img => img.complete && img.naturalWidth > 0))).toBe(true);
      await page.screenshot({ path: `../docs/evidence/chatbot-${viewport.width}.png` });
      await page.getByRole("button", { name: "Đóng DH Chatbot" }).click();
      await launcher.click();
      await expect(panel.getByText("Màu xanh", { exact: true })).toBeVisible();
      await panel.getByRole("link", { name: /Vợt tư vấn kiểm tra/ }).last().click();
      await expect(page).toHaveURL(/\/products\/1$/);
      await expect(panel).toHaveCount(0);
      await launcher.click();
      await expect(panel.getByText("Màu xanh", { exact: true })).toBeVisible();
      expect(errors).toEqual([]);
    });

    test("home actions open chat without login; error, retry and reset remain usable", async ({ page }) => {
      await setup(page);
      await page.locator(".hero-secondary").click();
      await expect(page).toHaveURL(/\/$/);
      const panel = page.getByRole("dialog", { name: "DH Chatbot" });
      const input = page.getByRole("textbox", { name: "Nhập tin nhắn" });
      await input.fill("lỗi mạng");
      await input.press("Enter");
      await expect(panel.getByRole("alert")).toHaveText("Tạm mất kết nối");
      await panel.getByRole("button", { name: "Thử lại" }).click();
      await expect(panel.getByRole("alert")).toHaveCount(0);
      await expect(panel.locator(".chatbot-message.user")).toHaveCount(1);
      await panel.getByRole("button", { name: "Cuộc trò chuyện mới" }).click();
      await expect(panel.locator(".chatbot-message.user")).toHaveCount(0);
      await input.fill("không có hàng");
      await page.getByRole("button", { name: "Gửi tin nhắn" }).click();
      await expect(panel.getByText(noMatch)).toBeVisible();
      await expect(panel.locator(".chatbot-products a")).toHaveCount(0);
      await page.keyboard.press("Escape");
      await expect(panel).toHaveCount(0);
    });
  });
}

test("English mode leaves user messages untouched and sends the correct locale", async ({ page }) => {
  const requests = await setup(page);
  await page.getByRole("button", { name: "Chọn ngôn ngữ" }).click();
  await page.getByRole("menuitemradio", { name: "English" }).click();
  await page.getByRole("button", { name: "Open DH Chatbot", exact: true }).click();
  await page.getByRole("textbox", { name: "Enter a message" }).fill("Tư vấn chọn vợt");
  await page.getByRole("button", { name: "Send message" }).click();
  await expect(page.locator(".chatbot-message.user")).toContainText("Tư vấn chọn vợt");
  await expect(page.getByText("Here are the available products.")).toBeVisible();
  expect(requests[0].language).toBe("en");
});
