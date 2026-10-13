import { expect } from "@playwright/test";
import { randomUUID } from "node:crypto";

export const API = process.env.E2E_API_URL || "http://localhost:8080/api";

export async function login(page, email, password) {
  await page.goto("/login");
  await page.getByLabel("Email hoặc số điện thoại").fill(email);
  await page.getByLabel("Mật khẩu", { exact: true }).fill(password);
  await page.getByRole("button", { name: "Đăng nhập", exact: true }).click();
  await expect(page).toHaveURL(/\/profile$/);
}

export async function catalogFixture(request, withProduct = false) {
  expect(process.env.E2E_ADMIN_EMAIL, "Set E2E_ADMIN_EMAIL for the isolated test database").toBeTruthy();
  expect(process.env.E2E_ADMIN_PASSWORD, "Set E2E_ADMIN_PASSWORD for the isolated test database").toBeTruthy();
  const logged = await request.post(API + "/auth/login", {
    data: { email: process.env.E2E_ADMIN_EMAIL, password: process.env.E2E_ADMIN_PASSWORD },
  });
  expect(logged.status()).toBe(200);
  const headers = { Authorization: `Bearer ${(await logged.json()).data.accessToken}` };
  const suffix = randomUUID();
  const category = await request.post(API + "/categories", { headers, data: { categoryName: "E2E Rackets " + suffix } });
  const brand = await request.post(API + "/brands", { headers, data: { brandName: "E2E Brand " + suffix } });
  expect(category.status()).toBe(201);
  expect(brand.status()).toBe(201);
  const fixture = { categoryId: (await category.json()).data.categoryId, brandId: (await brand.json()).data.brandId };
  if (withProduct) {
    const productName = "E2E All Round " + suffix;
    const product = await request.post(API + "/products", {
      headers,
      data: { ...fixture, productName, price: 1000000, stock: 5, status: "ACTIVE", specification: { weightClass: "4U", balanceType: "EVEN_BALANCE", shaftFlexibility: "MEDIUM", playStyle: "BALANCED", skillLevel: "INTERMEDIATE" } },
    });
    expect(product.status()).toBe(201);
    fixture.productId = (await product.json()).data.id;
    fixture.productName = productName;
  }
  return fixture;
}
