import { test, expect } from "@playwright/test";

const user = { id: 900, fullName: "Người kiểm tra", email: "preview@example.test", role: "ADMIN", status: "ACTIVE" };
const emptyPage = { content: [], page: 0, size: 12, totalPages: 0, totalElements: 0 };
const product = { id: 1, productName: "Vợt cầu lông kiểm tra", price: 2000000, effectivePrice: 2000000, stock: 5, status: "ACTIVE", brand: { name: "DH" }, category: { name: "Vợt" }, description: "Dụng cụ dành cho người chơi toàn diện.", thumbnailUrl: "/images/gear-racket.jpg", images: [{ imageId: 1, imageUrl: "/images/gear-racket.jpg" }], specification: { weightClass: "4U", shaftFlexibility: "MEDIUM", balanceType: "EVEN_BALANCE", playStyle: "BALANCED", skillLevel: "INTERMEDIATE" } };

async function mockApi(page, populated = false) {
  await page.route("**/api/**", async route => {
    const url = new URL(route.request().url()), path = url.pathname.replace(/^\/api/, "");
    let data = [], status = 200, code;
    if (path === "/auth/login") data = { accessToken: "browser-preview-token", user };
    else if (path === "/users/me") data = user;
    else if (path === "/cart") data = { items: [], subtotalAmount: 0 };
    else if (path === "/player-profile" || path === "/recommendations") {
      if (populated && path === "/recommendations") data = [{ productId: 1, product, matchScore: 87, breakdown: { skill: 18, playStyle: 22, wrist: 14, weight: 13, flex: 8, budget: 12 }, reasons: ["Phù hợp với lối chơi toàn diện"], cautions: [] }];
      else { status = 404; code = "PROFILE_REQUIRED"; }
    }
    else if (path === "/admin/statistics/overview") data = { products: 0, pendingOrders: 0, lowStockProducts: 0, shopRevenue: 0 };
    else if (path === "/products/1") data = product;
    else if (path === "/products/1/variants") data = [];
    else if (path === "/products/1/reviews") data = emptyPage;
    else if (path === "/products") data = { ...emptyPage, ...(populated ? { content: [product], totalElements: 1, totalPages: 1 } : {}) };
    else if (/\/\d+$/.test(path)) { status = 404; code = "NOT_FOUND"; }
    else if (url.searchParams.has("page")) data = emptyPage;
    await route.fulfill({ status, contentType: "application/json", body: JSON.stringify({ success: status === 200, data: status === 200 ? data : null, code, message: status === 200 ? "Thành công" : code === "PROFILE_REQUIRED" ? "Chưa có hồ sơ người chơi" : "Không tìm thấy dữ liệu" }) });
  });
}

async function navigate(page, path) {
  await page.evaluate(path => { window.history.pushState({}, "", path); window.dispatchEvent(new PopStateEvent("popstate")); }, path);
  await page.waitForLoadState("networkidle");
  await expect(page.locator("h1")).toHaveCount(1);
  const headings = {
    "/products": "Tìm dụng cụ cho đường cầu của bạn", "/promotions": "Khuyến mại", "/marketplace": "Sân chơi mới cho dụng cụ đã qua sử dụng",
    "/register": "Tạo tài khoản", "/login": "Đăng nhập", "/products/999": "Chi tiết sản phẩm", "/marketplace/999": "Tin đăng", "/sellers/999": "Hồ sơ người bán", "/missing-page": "Không tìm thấy trang",
    "/profile": "Hồ sơ cá nhân", "/addresses": "Giao đến nơi bạn muốn.", "/cart": "Giỏ hàng", "/checkout": "Thanh toán cửa hàng", "/orders": "Đơn hàng cửa hàng", "/orders/999": "Đơn hàng",
    "/favorites": "Sản phẩm yêu thích", "/player-profile": "Hồ sơ người chơi", "/recommendations": "Vợt phù hợp với bạn", "/sell": "Đăng bán dụng cụ", "/sell/999": "Chỉnh sửa tin đăng", "/my-listings": "Tin đăng của tôi",
    "/market-orders": "Giao dịch Marketplace", "/market-orders/999": "Giao dịch Marketplace", "/marketplace/999/buy": "Xác nhận mua dụng cụ", "/disputes/999": "Chi tiết tranh chấp", "/my-inspections": "Kiểm định của tôi", "/notifications": "Thông báo",
    "/inspector": "Bàn làm việc kiểm định", "/inspector/999/report": "Báo cáo kiểm định", "/admin": "Tổng quan cửa hàng", "/admin/products": "Quản lý sản phẩm", "/admin/shipments": "Cập nhật vận chuyển Marketplace",
    "/admin/orders": "Xử lý đơn cửa hàng", "/admin/listings": "Tin Marketplace chờ duyệt", "/admin/disputes": "Giải quyết tranh chấp", "/admin/users": "Quản lý người dùng", "/admin/statistics": "Thống kê", "/admin/audit-logs": "Nhật ký hệ thống", "/products/1": product.productName,
  };
  await expect(page.locator("h1")).toHaveText(headings[path]);
  await expect(page.locator(".loading-state")).toHaveCount(0);
  await expect(page.locator("h1")).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), `Horizontal overflow at ${path}`).toBe(true);
}

async function login(page) {
  await page.goto("/login");
  await page.getByLabel("Email hoặc số điện thoại").fill(user.email);
  await page.getByLabel("Mật khẩu", { exact: true }).fill("Preview123!");
  await page.getByRole("button", { name: "Đăng nhập", exact: true }).click();
  await expect(page).toHaveURL(/\/admin$/);
}

for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }]) {
  test.describe(`${viewport.width}px responsive redesign`, () => {
    test.use({ viewport });

    test("public pages, assets and search stay usable", async ({ page }) => {
      await mockApi(page);
      const errors = [];
      page.on("pageerror", error => errors.push(error.message));
      await page.goto("/");
      await page.waitForLoadState("networkidle");
      expect(await page.locator(".home-categories").evaluate(el => el.getBoundingClientRect().top < innerHeight)).toBe(true);
      for (const image of await page.locator("img").all()) {
        await image.scrollIntoViewIfNeeded();
        await expect.poll(() => image.evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
      }
      expect(await page.locator("img").evaluateAll(images => images.every(img => img.complete && img.naturalWidth > 0))).toBe(true);
      await page.evaluate(() => window.scrollTo({ top: 0, behavior: "instant" }));
      await page.screenshot({ path: `../docs/evidence/redesign-home-${viewport.width}.png`, fullPage: true });
      for (const path of ["/products", "/promotions", "/marketplace", "/register", "/login", "/products/999", "/marketplace/999", "/sellers/999", "/missing-page"]) await navigate(page, path);
      await page.getByRole("textbox", { name: "Tìm kiếm sản phẩm" }).fill("vợt 4U");
      await page.getByRole("button", { name: "Tìm kiếm", exact: true }).click();
      await expect(page).toHaveURL(/products\?keyword=/);
      expect(errors).toEqual([]);
    });

    test("all account, marketplace, inspection and admin routes handle an empty database", async ({ page }) => {
      test.setTimeout(90000);
      await mockApi(page);
      const errors = [];
      page.on("pageerror", error => errors.push(error.message));
      await login(page);
      await page.waitForLoadState("networkidle");
      await page.screenshot({ path: `../docs/evidence/redesign-admin-${viewport.width}.png`, fullPage: true });
      for (const path of ["/profile", "/addresses", "/cart", "/checkout", "/orders", "/orders/999", "/favorites", "/player-profile", "/recommendations", "/sell", "/sell/999", "/my-listings", "/market-orders", "/market-orders/999", "/marketplace/999/buy", "/disputes/999", "/my-inspections", "/notifications", "/inspector", "/inspector/999/report", "/admin", "/admin/products", "/admin/shipments", "/admin/orders", "/admin/listings", "/admin/disputes", "/admin/users", "/admin/statistics", "/admin/audit-logs"]) await navigate(page, path);
      expect(errors).toEqual([]);
    });

    test("populated product and recommendation layouts remain readable", async ({ page }) => {
      await mockApi(page, true);
      const errors = [];
      page.on("pageerror", error => errors.push(error.message));
      await login(page);
      await navigate(page, "/products/1");
      await page.screenshot({ path: `../docs/evidence/redesign-product-${viewport.width}.png`, fullPage: true });
      await navigate(page, "/recommendations");
      await expect(page.getByText("87% phù hợp")).toBeVisible();
      await page.screenshot({ path: `../docs/evidence/redesign-match-${viewport.width}.png`, fullPage: true });
      await navigate(page, "/profile");
      await expect(page.getByLabel("Họ và tên")).toHaveValue(user.fullName);
      await page.screenshot({ path: `../docs/evidence/redesign-profile-${viewport.width}.png`, fullPage: true });
      expect(errors).toEqual([]);
    });

    test("search preserves login and updates results on repeated searches", async ({ page }) => {
      await mockApi(page, true);
      await login(page);
      await navigate(page, "/profile");
      for (const keyword of ["racket", "shoes"]) {
        const requested = page.waitForRequest(request => {
          const url = new URL(request.url());
          return url.pathname === "/api/products" && url.searchParams.get("keyword") === keyword;
        });
        await page.getByRole("textbox", { name: "Tìm kiếm sản phẩm" }).fill(keyword);
        await page.getByRole("button", { name: "Tìm kiếm", exact: true }).click();
        await requested;
        await expect(page.getByRole("navigation", { name: "Tài khoản", exact: true })).toBeVisible();
        await expect(page.locator('.filters input[name="keyword"]')).toHaveValue(keyword);
      }
      await page.goBack();
      await expect(page.locator('.filters input[name="keyword"]')).toHaveValue("racket");
    });

    test("language, password visibility and mobile menus respond correctly", async ({ page }) => {
      await mockApi(page);
      await page.goto("/register");
      await page.getByRole("button", { name: "Số điện thoại", exact: true }).click();
      await expect(page.getByLabel("Số điện thoại", { exact: true })).toBeVisible();
      await page.getByRole("button", { name: "Email", exact: true }).click();
      await expect(page.getByLabel("Email", { exact: true })).toBeVisible();
      const password = page.getByLabel("Mật khẩu", { exact: true });
      await expect(password).toHaveAttribute("type", "password");
      await page.getByRole("button", { name: "Hiện mật khẩu", exact: true }).first().click();
      await expect(password).toHaveAttribute("type", "text");
      await page.getByRole("button", { name: "Chọn ngôn ngữ" }).click();
      await page.getByRole("menuitemradio", { name: "English" }).click();
      await expect(page.locator("html")).toHaveAttribute("lang", "en");
      await page.getByRole("button", { name: "Choose language" }).click();
      await page.getByRole("menuitemradio", { name: "Tiếng Việt" }).click();
      if (viewport.width < 980) {
        await page.getByRole("button", { name: "Mở menu" }).click();
        await expect(page.locator(".main-nav")).toBeVisible();
        await page.keyboard.press("Escape");
        await expect(page.locator(".main-nav")).not.toBeVisible();
        await login(page);
        await page.getByRole("button", { name: "Mở menu" }).click();
        await expect(page.getByRole("navigation", { name: "Điều hướng quản trị" })).toBeVisible();
        await page.keyboard.press("Escape");
        await expect(page.locator(".admin-sidebar")).not.toHaveClass(/is-open/);
      }
    });
  });
}
