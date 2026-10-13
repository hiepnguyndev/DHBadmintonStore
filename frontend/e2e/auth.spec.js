import { test, expect } from '@playwright/test';
test('register, login, profile update, address creation and logout against real API',async({page})=>{
  test.skip(process.env.RUN_E2E!=='true','Requires running Spring Boot + isolated MySQL + React');
  const email=`e2e-${Date.now()}@example.com`;
  await page.goto('/register');
  await page.getByLabel('Họ và tên').fill('E2E Player');await page.getByLabel('Email',{exact:true}).fill(email);
  await page.getByLabel('Mật khẩu',{exact:true}).fill('Password123!');await page.getByLabel('Xác nhận mật khẩu',{exact:true}).fill('Password123!');
  await page.getByRole('button',{name:'Tạo tài khoản',exact:true}).click();
  await expect(page.getByText('Đăng ký thành công. Bạn có thể đăng nhập ngay.')).toBeVisible();
  await page.getByLabel('Email hoặc số điện thoại').fill(email);await page.getByLabel('Mật khẩu',{exact:true}).fill('Password123!');
  await page.getByRole('button',{name:'Đăng nhập',exact:true}).click();await expect(page).toHaveURL(/profile/);
  await page.getByLabel('Họ và tên').fill('Updated E2E Player');await page.getByRole('button',{name:'Lưu thay đổi'}).click();await expect(page.getByText('Đã cập nhật hồ sơ.')).toBeVisible();
  await page.getByRole('navigation',{name:'Tài khoản',exact:true}).getByRole('link',{name:'Địa chỉ',exact:true}).click();await page.getByRole('button',{name:'Thêm địa chỉ',exact:true}).click();
  for(const [label,value] of [['Tên người nhận','E2E Player'],['Số điện thoại người nhận','0901234567'],['Tỉnh / Thành phố','Hà Nội'],['Quận / Huyện','Hà Đông'],['Phường / Xã','Mộ Lao'],['Địa chỉ chi tiết','123 ABC']])await page.getByLabel(label,{exact:true}).fill(value);
  await page.getByLabel('Đặt làm địa chỉ mặc định').check();await page.getByRole('button',{name:'Lưu địa chỉ',exact:true}).click();await expect(page.getByText('Đã lưu địa chỉ.')).toBeVisible();
  await page.getByRole('link',{name:'Tài khoản',exact:true}).click();
  await page.getByRole('button',{name:'Đăng xuất',exact:true}).click();await expect(page).toHaveURL(/login/);
});
