import { test, expect } from "@playwright/test";

const user = { id: 900, fullName: "Quản trị DH", email: "posts-preview@example.test", role: "ADMIN", status: "ACTIVE" };
const content = "Shop giới thiệu hoạt động giao lưu cầu lông.\n\n<script>window.postInjected=true</script>\n" + "DH".repeat(120);
const envelope = content => ({ content, page: 0, size: 12, totalPages: content.length ? 1 : 0, totalElements: content.length });

async function mockApi(page, failOnce = false) {
  const posts = [], writes = [];
  await page.route("**/api/**", async route => {
    const request = route.request(), url = new URL(request.url()), path = url.pathname.replace(/^\/api/, "");
    let data = [], status = 200;
    if (path === "/auth/login") data = { accessToken: "shop-post-preview-token", user };
    else if (path === "/users/me") data = user;
    else if (path === "/admin/statistics/overview") data = {};
    else if (path === "/admin/posts" && request.method() === "POST" || /^\/admin\/posts\/\d+$/.test(path)) {
      if (request.method() === "DELETE") {
        data = posts.find(post => post.id === Number(path.split("/").at(-1)));
        data.status = "ARCHIVED";
      } else {
        const form = await new Response(request.postDataBuffer(), { headers: { "Content-Type": request.headers()["content-type"] } }).formData();
        const payload = JSON.parse(await form.get("post").text());
        writes.push({ method: request.method(), ...payload, cover: form.get("cover")?.name });
        if (failOnce) { failOnce = false; status = 400; }
        else {
          data = request.method() === "POST" ? { id: posts.length + 1 } : posts.find(post => post.id === Number(path.split("/").at(-1)));
          Object.assign(data, payload, { updatedAt: "2026-10-13T03:00:00Z" });
          if (form.get("cover")) data.coverImageUrl = "/images/badminton-match-pexels.jpg";
          if (data.status === "PUBLISHED" && !data.publishedAt) data.publishedAt = data.updatedAt;
          if (request.method() === "POST") posts.push(data);
        }
      }
    } else if (path === "/posts" || path === "/admin/posts") {
      let found = posts.filter(post => path === "/posts" ? post.status === "PUBLISHED" : url.searchParams.has("status") ? post.status === url.searchParams.get("status") : post.status !== "ARCHIVED");
      if (url.searchParams.get("category")) found = found.filter(post => post.category === url.searchParams.get("category"));
      if (url.searchParams.get("keyword")) found = found.filter(post => post.title.includes(url.searchParams.get("keyword")));
      data = envelope(found);
    } else if (/^\/posts\/\d+$/.test(path)) {
      data = posts.find(post => post.id === Number(path.split("/").at(-1)) && post.status === "PUBLISHED");
      if (!data) status = 404;
    } else if (path === "/products") data = envelope([]);
    await route.fulfill({ status, contentType: "application/json", body: JSON.stringify({ success: status === 200, data, message: status === 200 ? "Thành công" : "Không lưu được bài viết" }) });
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
async function fillPost(page) {
  await page.getByLabel("Tiêu đề bài viết").fill("Giao lưu cầu lông tại DH");
  await page.getByRole("textbox", { name: "Tóm tắt", exact: true }).fill("Shop DH thông báo lịch giao lưu dành cho người chơi.");
  await page.getByRole("textbox", { name: "Nội dung bài viết", exact: true }).fill(content);
  await page.getByRole("combobox", { name: "Chuyên mục", exact: true }).selectOption("COMMUNITY");
}
for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }, { width: 360, height: 800 }]) {
  test.describe(`shop posts ${viewport.width}px`, () => {
    test.use({ viewport });
    test("admin creates a draft, publishes with cover, edits and removes a post from the public shop", async ({ page }) => {
      const writes = await mockApi(page), errors = [];
      page.on("pageerror", error => errors.push(error.message));
      await login(page); await navigate(page, "/admin/posts");
      await fillPost(page);
      await page.getByRole("button", { name: "Lưu bản nháp", exact: true }).click();
      await expect(page.locator(".post-feedback")).toHaveText("Đã lưu bản nháp.");
      expect(writes[0].status).toBe("DRAFT");
      await navigate(page, "/news");
      await expect(page.getByRole("heading", { name: "Chưa có bài viết" })).toBeVisible();
      await navigate(page, "/admin/posts");
      await page.getByRole("button", { name: "Sửa Giao lưu cầu lông tại DH" }).click();
      await page.getByLabel("Ảnh bìa PNG/JPEG, tối đa 5 MB").setInputFiles("public/images/badminton-match-pexels.jpg");
      await page.getByRole("button", { name: "Đăng bài", exact: true }).click();
      await expect(page.locator(".post-feedback")).toHaveText("Đã đăng bài viết lên cửa hàng.");
      expect(writes[1]).toMatchObject({ method: "PUT", status: "PUBLISHED", category: "COMMUNITY", cover: "badminton-match-pexels.jpg" });
      await expect.poll(() => page.locator(".post-cover-picker img").evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1)).toBe(true);
      await page.evaluate(() => window.scrollTo({ top: 0, behavior: "instant" }));
      expect(await page.evaluate(() => {
        const launcher = document.querySelector(".chatbot-launcher").getBoundingClientRect();
        return [...document.querySelectorAll(".post-form input, .post-form select, .post-form textarea, .post-form button")].every(control => {
          const box = control.getBoundingClientRect();
          return box.right <= launcher.left || box.left >= launcher.right || box.bottom <= launcher.top || box.top >= launcher.bottom;
        });
      })).toBe(true);
      await page.screenshot({ path: `../docs/evidence/shop-posts-admin-${viewport.width}.png`, fullPage: true });
      await navigate(page, "/");
      const community = page.getByRole("region", { name: "Cộng đồng & Tin tức" });
      await expect(community.locator("article")).toHaveCount(4);
      await community.getByRole("link", { name: /Giao lưu cầu lông tại DH/ }).click();
      await expect(page).toHaveURL(/\/news\/1$/);
      await expect(page.locator(".article-content")).toHaveText(content);
      await expect(page.locator(".article-content script")).toHaveCount(0);
      expect(await page.evaluate(() => window.postInjected)).toBeUndefined();
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1)).toBe(true);
      await expect.poll(() => page.locator(".article-cover").evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
      await page.evaluate(() => window.scrollTo({ top: 0, behavior: "instant" }));
      await page.screenshot({ path: `../docs/evidence/shop-posts-article-${viewport.width}.png`, fullPage: true });
      await navigate(page, "/admin/posts");
      await page.getByRole("button", { name: "Sửa Giao lưu cầu lông tại DH" }).click();
      await page.getByLabel("Tiêu đề bài viết").fill("Tin mới từ shop DH");
      await page.getByRole("button", { name: "Lưu thay đổi", exact: true }).click();
      await expect(page.locator(".post-feedback")).toHaveText("Đã đăng bài viết lên cửa hàng.");
      await page.getByRole("button", { name: "Chuyển về bản nháp", exact: true }).click();
      await page.getByRole("button", { name: "Hủy", exact: true }).click();
      expect(writes).toHaveLength(3);
      await page.getByRole("button", { name: "Chuyển về bản nháp", exact: true }).click();
      await page.getByRole("button", { name: "Xác nhận chuyển về bản nháp", exact: true }).click();
      await expect(page.locator(".post-feedback")).toHaveText("Đã lưu bản nháp.");
      await page.getByRole("button", { name: "Lưu trữ", exact: true }).click();
      await page.getByRole("button", { name: "Lưu trữ bài viết", exact: true }).click();
      await expect(page.locator(".post-feedback")).toHaveText("Đã lưu trữ bài viết.");
      await navigate(page, "/news");
      await expect(page.getByRole("heading", { name: "Chưa có bài viết" })).toBeVisible();
      expect(errors).toEqual([]);
    });
  });
}

test("failed save retains text and retries without duplicate publication", async ({ page }) => {
  const writes = await mockApi(page, true);
  await login(page); await navigate(page, "/admin/posts"); await fillPost(page);
  await page.getByRole("button", { name: "Đăng bài", exact: true }).click();
  await expect(page.getByRole("alert")).toHaveText("Không lưu được bài viết");
  await expect(page.getByRole("textbox", { name: "Nội dung bài viết", exact: true })).toHaveValue(content);
  await page.getByRole("button", { name: "Đăng bài", exact: true }).click();
  await expect(page.locator(".post-feedback")).toHaveText("Đã đăng bài viết lên cửa hàng.");
  expect(writes).toHaveLength(2);
  await expect(page.locator(".admin-post-row")).toHaveCount(1);
});
