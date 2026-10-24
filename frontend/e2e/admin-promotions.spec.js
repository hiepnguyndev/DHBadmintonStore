import { test, expect } from "@playwright/test";

const user = { id: 900, fullName: "Quản trị DH", email: "admin-preview@example.test", role: "ADMIN", status: "ACTIVE" };
const original = { id: 1, productName: "Vợt ưu đãi do admin chọn", price: 1500000, salePrice: null, effectivePrice: 1500000, promotionPublished: false, stock: 3, status: "ACTIVE", thumbnailUrl: "/images/gear-racket.jpg", brand: { id: 1, name: "DH" }, category: { id: 1, name: "Vợt" }, images: [], specification: null };

async function mockApi(page, options = {}) {
  let products = [{ ...original }, { ...original, id: 2, productName: "Vợt ngừng kinh doanh", status: "INACTIVE" }, { ...original, id: 3, productName: "Vợt giảm giá chưa được đăng", salePrice: 900000, effectivePrice: 900000 }];
  let fail = options.failOnce;
  const writes = [];
  await page.route("**/api/**", async route => {
    const request = route.request(), url = new URL(request.url()), path = url.pathname.replace(/^\/api/, "");
    let data = [], status = 200;
    if (path === "/auth/login") data = { accessToken: "promotion-preview-token", user: { ...user, role: options.role || "ADMIN" } };
    else if (path === "/users/me") data = { ...user, role: options.role || "ADMIN" };
    else if (path === "/admin/statistics/overview") data = {};
    else if (/\/admin\/products\/\d+\/promotion$/.test(path)) {
      writes.push({ method: request.method(), body: request.postDataJSON() });
      if (fail) { fail = false; status = 400; }
      else {
        const product = products.find(p => p.id === Number(path.split("/")[3]));
        if (request.method() === "PUT") {
          const { discountType, value } = request.postDataJSON();
          product.salePrice = discountType === "PERCENT" ? Math.round(product.price * (100 - value)) / 100 : value;
          product.promotionPublished = true;
        } else { product.salePrice = null; product.promotionPublished = false; }
        product.effectivePrice = product.salePrice ?? product.price;
        data = product;
      }
    } else if (path === "/admin/products" || path === "/products") {
      let content = products.filter(p => path.startsWith("/admin") || p.status !== "INACTIVE");
      if (url.searchParams.get("onSale") === "true") content = content.filter(p => p.promotionPublished && p.salePrice < p.price);
      if (url.searchParams.has("published")) content = content.filter(p => p.promotionPublished === (url.searchParams.get("published") === "true"));
      if (url.searchParams.get("keyword")) content = content.filter(p => p.productName.includes(url.searchParams.get("keyword")));
      data = { content, page: 0, size: 12, totalPages: content.length ? 1 : 0, totalElements: content.length };
    }
    await route.fulfill({ status, contentType: "application/json", body: JSON.stringify({ success: status === 200, data, message: status === 200 ? "Thành công" : "Không lưu được ưu đãi" }) });
  });
  return writes;
}

async function login(page) {
  await page.goto("/login");
  await page.getByLabel("Email hoặc số điện thoại").fill(user.email);
  await page.getByLabel("Mật khẩu", { exact: true }).fill("Preview123!");
  await page.getByRole("button", { name: "Đăng nhập", exact: true }).click();
  await expect(page).toHaveURL(/\/admin$/);
}
async function navigate(page, path) {
  await page.evaluate(path => { window.history.pushState({}, "", path); window.dispatchEvent(new PopStateEvent("popstate")); }, path);
  await page.waitForLoadState("networkidle");
}

for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }, { width: 360, height: 800 }]) {
  test.describe(`admin offers ${viewport.width}px`, () => {
    test.use({ viewport });
    test("admin selects, publishes, updates and ends offers without automatically publishing other discounts", async ({ page }) => {
      const writes = await mockApi(page), errors = [];
      page.on("pageerror", error => errors.push(error.message));
      await login(page); await navigate(page, "/admin/promotions");
      await expect(page.getByRole("heading", { name: "Quản lý ưu đãi" })).toBeVisible();
      await expect(page.getByText("Chưa chọn sản phẩm")).toBeVisible();
      expect(writes).toHaveLength(0);
      await page.getByRole("button", { name: "Chọn Vợt ngừng kinh doanh" }).click();
      await expect(page.getByRole("button", { name: "Đăng ưu đãi", exact: true })).toBeDisabled();
      await page.getByRole("button", { name: `Chọn ${original.productName}` }).click();
      await page.getByLabel("Mức giảm (%)").fill("15");
      await expect(page.locator(".promotion-preview")).toContainText("1.275.000");
      await page.getByRole("button", { name: "Đăng ưu đãi", exact: true }).click();
      await expect(page.locator(".promotion-feedback")).toHaveText("Đã đăng ưu đãi lên cửa hàng.");
      expect(writes[0]).toEqual({ method: "PUT", body: { discountType: "PERCENT", value: 15 } });
      await expect(page.locator(".promotion-thumb img").first()).toBeVisible();
      await expect.poll(() => page.locator("img").evaluateAll(images => images.every(img => img.complete && img.naturalWidth > 0))).toBe(true);
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1)).toBe(true);
      await page.screenshot({ path: `../docs/evidence/admin-offers-${viewport.width}.png`, fullPage: true });
      await navigate(page, "/");
      const offers = page.getByRole("region", { name: "Sản phẩm ưu đãi" });
      await expect(offers.locator(".product-card")).toHaveCount(1);
      await expect(offers).toContainText(original.productName);
      await expect(offers).toContainText("-15%");
      await expect(offers).not.toContainText("Vợt giảm giá chưa được đăng");
      await navigate(page, "/admin/promotions");
      await page.getByLabel("Trạng thái ưu đãi").selectOption("true");
      await expect(page.locator(".promotion-product-row")).toHaveCount(1);
      await page.getByRole("button", { name: `Chọn ${original.productName}` }).click();
      await page.getByLabel("Giá ưu đãi (₫)").fill("1000000");
      await page.getByRole("button", { name: "Cập nhật ưu đãi" }).click();
      await expect(page.locator(".promotion-feedback")).toHaveText("Đã đăng ưu đãi lên cửa hàng.");
      expect(writes[1]).toEqual({ method: "PUT", body: { discountType: "SALE_PRICE", value: 1000000 } });
      await page.getByRole("button", { name: "Kết thúc ưu đãi", exact: true }).click();
      await page.getByRole("button", { name: "Hủy", exact: true }).click();
      expect(writes).toHaveLength(2);
      await page.getByRole("button", { name: "Kết thúc ưu đãi", exact: true }).click();
      await page.getByRole("button", { name: "Kết thúc", exact: true }).click();
      await expect(page.locator(".promotion-feedback")).toHaveText("Đã kết thúc ưu đãi và khôi phục giá gốc.");
      await expect(page.locator(".promotion-prices").first()).toHaveText(/Giá gốc1.500.000/);
      await navigate(page, "/promotions");
      await expect(page.getByRole("heading", { name: "Chưa có sản phẩm ưu đãi" })).toBeVisible();
      expect(errors).toEqual([]);
    });
  });
}

test("publish errors retain admin inputs for retry", async ({ page }) => {
  const writes = await mockApi(page, { failOnce: true });
  await login(page); await navigate(page, "/admin/promotions");
  await page.getByRole("button", { name: `Chọn ${original.productName}` }).click();
  await page.getByLabel("Mức giảm (%)").fill("25");
  await page.getByRole("button", { name: "Đăng ưu đãi", exact: true }).click();
  await expect(page.getByRole("alert")).toHaveText("Không lưu được ưu đãi");
  await expect(page.getByLabel("Mức giảm (%)")).toHaveValue("25");
  await page.getByRole("button", { name: "Đăng ưu đãi", exact: true }).click();
  await expect(page.locator(".promotion-feedback")).toHaveText("Đã đăng ưu đãi lên cửa hàng.");
  expect(writes).toHaveLength(2);
});
