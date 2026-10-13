import { test, expect } from "@playwright/test";

const user = { id: 901, fullName: "Người chơi kiểm tra", email: "chat-profile@example.test", role: "USER", status: "ACTIVE" };
const product = { id: 8, productName: "Vợt hồ sơ kiểm tra 4U", effectivePrice: 1200000, price: 1500000, stock: 4, status: "ACTIVE", thumbnailUrl: "/images/gear-racket.jpg", brand: { name: "DH" }, images: [] };
const recommendation = { productId: 8, product, matchScore: 73, reasons: ["Phù hợp với lực cổ tay", "Trọng lượng 4U phù hợp hồ sơ"], cautions: ["Giá vượt ngân sách"], breakdown: {} };
const playerProfile = { skillLevel: "INTERMEDIATE", playStyle: "ATTACK", playType: "BOTH", wristStrength: "MEDIUM", dominantHand: "RIGHT", preferredWeight: "4U", balanceType: "EVEN_BALANCE", shaftFlexibility: "MEDIUM", shirtSize: "L", pantsSize: "L", shoeSize: 42, favoriteColors: "", favoriteBrands: "", minBudget: 500000, maxBudget: 2000000 };
const emptyPage = { content: [], totalElements: 0, totalPages: 0, page: 0, size: 12 };

async function setup(page, options = {}) {
  const requests = [], writes = [], chat = [];
  let savedProfile = options.missing ? null : playerProfile;
  await page.route("**/api/**", async route => {
    const request = route.request(), url = new URL(request.url()), path = url.pathname.replace(/^\/api/, "");
    let data = [], status = 200, code;
    if (path === "/auth/login") data = { accessToken: "profile-test-token", user };
    else if (path === "/users/me") data = user;
    else if (path === "/auth/logout") data = null;
    else if (path === "/player-profile") {
      if (request.method() !== "GET") {
        writes.push(request.method());
        savedProfile = request.postDataJSON();
        data = savedProfile;
      } else if (savedProfile) data = savedProfile;
      else { status = 404; code = "PROFILE_REQUIRED"; }
    } else if (path === "/recommendations") {
      requests.push({ authorization: request.headers().authorization, limit: url.searchParams.get("limit") });
      if (options.expired) { status = 401; code = "UNAUTHORIZED"; }
      else if (options.failFirst && requests.length === 1) { status = 503; code = "SERVICE_UNAVAILABLE"; }
      else if (!savedProfile) { status = 404; code = "PROFILE_REQUIRED"; }
      else data = options.empty ? [] : [recommendation];
    } else if (path === "/ai/chat") {
      chat.push(request.postDataJSON());
      data = { answer: "Kết quả tư vấn dụng cụ", products: [], engine: "LOCAL_RULES" };
    } else if (path === "/products/8") data = product;
    else if (path === "/products" || path.endsWith("/reviews")) data = emptyPage;
    else if (path === "/cart") data = { items: [], subtotalAmount: 0 };
    await route.fulfill({ status, contentType: "application/json", body: JSON.stringify({ success: status === 200, data: status === 200 ? data : null, code, message: code === "PROFILE_REQUIRED" ? "Chưa có hồ sơ người chơi" : "Tạm mất kết nối" }) });
  });
  await page.goto("/");
  return { requests, writes, chat };
}

async function signIn(page) {
  await page.goto("/login");
  await page.getByLabel("Email hoặc số điện thoại").fill(user.email);
  await page.getByLabel("Mật khẩu", { exact: true }).fill("Preview123!");
  await page.getByRole("button", { name: "Đăng nhập", exact: true }).click();
  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByLabel("Họ và tên")).toHaveValue(user.fullName);
}

async function navigate(page, path) {
  await page.evaluate(path => { history.pushState({}, "", path); dispatchEvent(new PopStateEvent("popstate")); }, path);
  await expect(page).toHaveURL(new RegExp(path + "$"));
}

for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }, { width: 360, height: 640 }, { width: 844, height: 390 }]) {
  test.describe(`profile chat ${viewport.width}px`, () => {
    test.use({ viewport });
    test("personalized entry opens DH Chatbot with authenticated server results", async ({ page }) => {
      const errors = [];
      page.on("pageerror", error => errors.push(error.message));
      const { requests, chat } = await setup(page);
      await signIn(page);
      expect(requests).toHaveLength(0);
      await navigate(page, "/products");
      await page.getByRole("button", { name: "Gợi ý dành cho tôi" }).click();
      const panel = page.getByRole("dialog", { name: "DH Chatbot" });
      await expect(panel.getByText("73% phù hợp")).toBeAttached();
      await expect(panel.getByText("Phù hợp với lực cổ tay")).toBeAttached();
      await expect(panel.getByText("Giá vượt ngân sách")).toBeAttached();
      expect(requests).toEqual([{ authorization: "Bearer profile-test-token", limit: "3" }]);
      expect(chat).toHaveLength(0);
      const box = await panel.boundingBox();
      expect(box.x).toBeGreaterThanOrEqual(0);
      expect(box.y).toBeGreaterThanOrEqual(0);
      expect(box.x + box.width).toBeLessThanOrEqual(viewport.width);
      expect(box.y + box.height).toBeLessThan(viewport.height);
      expect(await panel.evaluate(el => el.scrollWidth <= el.clientWidth)).toBe(true);
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
      const image = panel.locator(".chatbot-profile-results img");
      await image.scrollIntoViewIfNeeded();
      await expect.poll(() => image.evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
      const composer = await panel.locator(".chatbot-composer").boundingBox();
      const log = await panel.locator(".chatbot-log").boundingBox();
      expect(log.y + log.height).toBeLessThanOrEqual(composer.y + 1);
      await page.screenshot({ path: `../docs/evidence/dh-chatbot-profile-${viewport.width}.png` });
      await panel.getByRole("button", { name: "Đóng DH Chatbot" }).click();
      await page.getByRole("button", { name: "Mở DH Chatbot", exact: true }).click();
      expect(requests).toHaveLength(1);
      await panel.getByRole("link", { name: /Vợt hồ sơ kiểm tra/ }).click();
      await expect(page).toHaveURL(/\/products\/8$/);
      await expect(panel).toHaveCount(0);
      expect(errors).toEqual([]);
    });
  });
}

for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }]) {
  test.describe(`profile flow ${viewport.width}px`, () => {
    test.use({ viewport });
    test("chatbot profile action offers guests sign in without a private API request", async ({ page }) => {
      const { requests, chat } = await setup(page);
      await page.getByRole("button", { name: "Mở DH Chatbot", exact: true }).click();
      await page.locator(".chatbot-profile-tools").getByRole("button", { name: "Gợi ý từ hồ sơ" }).click();
      const panel = page.getByRole("dialog", { name: "DH Chatbot" });
      await expect(panel.getByRole("link", { name: "Đăng nhập", exact: true })).toBeVisible();
      expect(requests).toHaveLength(0);
      expect(chat).toHaveLength(0);
      await panel.getByRole("link", { name: "Đăng nhập", exact: true }).click();
      await expect(page).toHaveURL(/\/login$/);
      await expect(panel).toHaveCount(0);
    });

    test("missing profile can be saved, then edited, with recommendations in chat", async ({ page }) => {
      const { requests, writes } = await setup(page, { missing: true });
      await signIn(page);
      await page.locator(".profile-ai").getByRole("button", { name: "Gợi ý từ hồ sơ" }).click();
      const panel = page.getByRole("dialog", { name: "DH Chatbot" });
      await expect(panel.getByText("Bạn chưa có hồ sơ người chơi để nhận gợi ý cá nhân hóa.")).toBeVisible();
      await panel.getByRole("link", { name: "Cập nhật hồ sơ người chơi" }).click();
      await expect(page).toHaveURL(/\/player-profile$/);
      await page.getByRole("button", { name: "Lưu và xem gợi ý" }).click();
      await expect(panel.getByText("73% phù hợp")).toBeVisible();
      await expect(page).toHaveURL(/\/player-profile$/);
      expect(writes).toEqual(["POST"]);
      await panel.getByRole("button", { name: "Đóng DH Chatbot" }).click();
      await page.getByRole("combobox", { name: "Lối chơi", exact: true }).selectOption("SPEED");
      await page.getByRole("button", { name: "Lưu và xem gợi ý" }).click();
      await expect(panel.getByText("73% phù hợp")).toHaveCount(2);
      expect(writes).toEqual(["POST", "PUT"]);
      expect(requests).toHaveLength(3);
    });
  });
}

test("typed profile intent stays out of public chat history; failed profile requests retry privately", async ({ page }) => {
  const { requests, chat } = await setup(page, { failFirst: true });
  await signIn(page);
  await page.getByRole("button", { name: "Mở DH Chatbot", exact: true }).click();
  const panel = page.getByRole("dialog", { name: "DH Chatbot" });
  const input = panel.getByRole("textbox", { name: "Nhập tin nhắn" });
  await input.fill("Tư vấn theo hồ sơ của tôi");
  await input.press("Enter");
  await expect(panel.getByRole("alert")).toHaveText("Tạm mất kết nối");
  await panel.getByRole("button", { name: "Thử lại" }).click();
  await expect(panel.getByText("73% phù hợp")).toBeVisible();
  expect(requests).toHaveLength(2);
  await expect(panel.locator(".chatbot-message.user")).toHaveCount(1);
  await input.fill("Giày dưới 2 triệu");
  await input.press("Enter");
  await expect(panel.getByText("Kết quả tư vấn dụng cụ")).toBeVisible();
  expect(chat).toEqual([{ message: "Giày dưới 2 triệu", history: [], language: "vi" }]);
});

test("empty recommendations do not fall back to demonstration products", async ({ page }) => {
  await setup(page, { empty: true });
  await signIn(page);
  await page.getByRole("button", { name: "Mở DH Chatbot", exact: true }).click();
  const panel = page.getByRole("dialog", { name: "DH Chatbot" });
  await panel.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }).click();
  await expect(panel.getByText("Hiện chưa có vợt còn hàng để gợi ý từ hồ sơ của bạn.")).toBeVisible();
  await expect(panel.locator(".chatbot-profile-results article")).toHaveCount(0);
});

test("expired authentication clears old profile data and offers sign in", async ({ page }) => {
  await setup(page, { expired: true });
  await signIn(page);
  await page.getByRole("button", { name: "Mở DH Chatbot", exact: true }).click();
  const panel = page.getByRole("dialog", { name: "DH Chatbot" });
  await panel.getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true }).click();
  await expect(panel.getByRole("link", { name: "Đăng nhập", exact: true })).toBeVisible();
  await expect(panel.locator(".chatbot-profile-results article")).toHaveCount(0);
});

test("English profile controls keep the DH Chatbot name and server product name", async ({ page }) => {
  await setup(page);
  await signIn(page);
  await page.getByRole("button", { name: "Chọn ngôn ngữ" }).click();
  await page.getByRole("menuitemradio", { name: "English" }).click();
  await page.getByRole("button", { name: "Open DH Chatbot", exact: true }).click();
  const panel = page.getByRole("dialog", { name: "DH Chatbot" });
  await panel.getByRole("button", { name: "From my profile" }).click();
  await expect(panel.getByText("73% match")).toBeVisible();
  await expect(panel.getByRole("link", { name: /Vợt hồ sơ kiểm tra 4U/ })).toBeVisible();
  await expect(panel.getByRole("link", { name: "Update player profile" })).toBeVisible();
});
