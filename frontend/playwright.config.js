import { defineConfig, devices } from '@playwright/test';
export default defineConfig({testDir:'./e2e',use:{baseURL:process.env.E2E_BASE_URL||'http://localhost:5173',trace:'retain-on-failure',launchOptions:process.env.PLAYWRIGHT_EXECUTABLE_PATH?{executablePath:process.env.PLAYWRIGHT_EXECUTABLE_PATH}:{}},projects:[{name:'chromium',use:{...devices['Desktop Chrome']}}]});
