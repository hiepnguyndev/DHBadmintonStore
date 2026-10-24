import { test, expect } from "@playwright/test";

const offer = { id: 8, productName: "Vợt ưu đãi kiểm tra", price: 1500000, salePrice: 1200000, effectivePrice: 1200000, stock: 4, status: "ACTIVE", brand: { name: "DH" }, thumbnailUrl: "/images/gear-racket.jpg" };
const regular = { ...offer, id: 9, productName: "Vợt giá thường kiểm tra", salePrice: null, effectivePrice: 1500000 };

async function setup(page, mode = "populated") {
  const calls = [];
  let offersEnabled = mode !== "retry";
  await page.route("**/api/**", async route => {
    const url = new URL(route.request().url()), isOffers = url.searchParams.get("onSale") === "true";
    if (url.pathname === "/api/products") {
      calls.push({ isOffers, page: url.searchParams.get("page"), size: url.searchParams.get("size") });
      if (!offersEnabled && isOffers) {
        return route.fulfill({ status: 503, contentType: "application/json", body: JSON.stringify({ success: false, message: "Tạm mất kết nối" }) });
      }
      const content = mode === "empty" ? [] : [isOffers ? offer : regular];
      return route.fulfill({ contentType: "application/json", body: JSON.stringify({ success: true, data: { content, page: Number(url.searchParams.get("page") || 0), size: Number(url.searchParams.get("size") || 12), totalElements: mode === "empty" ? 0 : 13, totalPages: mode === "empty" ? 0 : 2 } }) });
    }
    await route.fulfill({ contentType: "application/json", body: JSON.stringify({ success: true, data: [] }) });
  });
  await page.goto("/");
  return { calls, enableOffers: () => { offersEnabled = true; } };
}

for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }, { width: 360, height: 640 }]) {
  test.describe(`home offers and community ${viewport.width}px`, () => {
    test.use({ viewport });
    test("shows real-discount cards, sourced news and working offer pagination", async ({ page }) => {
      const errors = [];
      page.on("pageerror", error => errors.push(error.message));
      const { calls } = await setup(page);
      await expect(page.locator(".home-match-band")).toHaveCount(0);
      await expect(page.getByText("PLAYER DNA")).toHaveCount(0);
      const offers = page.getByRole("region", { name: "Sản phẩm ưu đãi" });
      await expect(offers.getByRole("heading", { name: offer.productName })).toBeVisible();
      await expect(offers.locator(".sale-badge")).toHaveText("-20%");
      await expect(offers.locator("del")).toContainText("1.500.000");
      await expect(offers.getByText(regular.productName)).toHaveCount(0);
      expect(calls).toContainEqual({ isOffers: true, page: "0", size: "4" });
      await offers.scrollIntoViewIfNeeded();
      await expect.poll(() => offers.locator("img").evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
      await offers.screenshot({ path: `../docs/evidence/home-offers-${viewport.width}.png` });
      const community = page.getByRole("region", { name: "Cộng đồng & Tin tức" });
      await community.scrollIntoViewIfNeeded();
      await expect(community.locator("article")).toHaveCount(3);
      for (const story of await community.locator("article").all()) {
        await story.scrollIntoViewIfNeeded();
        expect(await story.evaluate(el => el.scrollWidth <= el.clientWidth)).toBe(true);
        await expect.poll(() => story.locator("img").evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
      }
      await expect(community.getByRole("link", { name: /Dụng cụ cũ, hành trình mới/ })).toHaveAttribute("href", "/marketplace");
      const news = community.getByRole("link", { name: /Đọc tại Yonex/ });
      await expect(news).toHaveCount(2);
      for (const link of await news.all()) {
        await expect(link).toHaveAttribute("href", /^https:\/\/www\.yonex\.com\/news\//);
        await expect(link).toHaveAttribute("target", "_blank");
      }
      await community.screenshot({ path: `../docs/evidence/home-community-${viewport.width}.png` });
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
      await offers.getByRole("link", { name: "Tất cả ưu đãi" }).click();
      await expect(page).toHaveURL(/\/promotions$/);
      await page.getByRole("button", { name: "Sau", exact: true }).click();
      expect(calls).toContainEqual({ isOffers: true, page: "1", size: "12" });
      expect(errors).toEqual([]);
    });

    test("empty catalog keeps honest offer state and community visible", async ({ page }) => {
      await setup(page, "empty");
      const offers = page.getByRole("region", { name: "Sản phẩm ưu đãi" });
      await expect(offers.getByText("Chưa có sản phẩm ưu đãi")).toBeVisible();
      await expect(offers.locator(".product-card")).toHaveCount(0);
      await expect(page.locator(".home-community article")).toHaveCount(3);
      await page.getByRole("button", { name: "Mở DH Chatbot", exact: true }).click();
      await expect(page.getByRole("dialog", { name: "DH Chatbot" }).getByRole("button", { name: "Gợi ý từ hồ sơ", exact: true })).toBeVisible();
    });
  });
}

test("offer failures can retry independently without hiding the catalog or news", async ({ page }) => {
  const { enableOffers } = await setup(page, "retry");
  const offers = page.getByRole("region", { name: "Sản phẩm ưu đãi" });
  await expect(offers.getByRole("alert")).toHaveText(/Tạm mất kết nối/);
  await expect(page.locator(".home-products").getByText(regular.productName)).toBeVisible();
  enableOffers();
  await offers.getByRole("button", { name: "Thử lại" }).click();
  await expect(offers.getByText(offer.productName)).toBeVisible();
});

test("home section labels translate without changing product names", async ({ page }) => {
  await setup(page);
  await page.getByRole("button", { name: "Chọn ngôn ngữ" }).click();
  await page.getByRole("menuitemradio", { name: "English" }).click();
  await expect(page.getByRole("region", { name: "Products on offer" })).toBeVisible();
  const community = page.getByRole("region", { name: "Community & News" });
  await community.scrollIntoViewIfNeeded();
  await expect(community.getByText("Takahashi and Nakade win their first Super 750 title")).toBeVisible();
  await expect(page.getByText(offer.productName, { exact: true })).toBeVisible();
});

test("the skip link stays hidden until keyboard focus", async ({ page }) => {
  await setup(page);
  const skip = page.locator(".skip-link");
  expect(await skip.evaluate(el => getComputedStyle(el).clipPath)).not.toBe("none");
  await page.keyboard.press("Tab");
  await expect(skip).toBeFocused();
  expect(await skip.evaluate(el => getComputedStyle(el).clipPath)).toBe("none");
  await page.keyboard.press("Enter");
  await expect(page).toHaveURL(/#main-content$/);
});
