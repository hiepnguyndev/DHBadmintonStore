import {test,expect} from '@playwright/test';
import { API, catalogFixture, login } from './helpers';
test('player profile, recommendations, favorite, cart and checkout against real API',async({page,request})=>{
 test.skip(process.env.RUN_E2E!=='true','Requires isolated MySQL, backend, React and E2E admin credentials');
 const fixture=await catalogFixture(request,true);
 const email=`commerce-${Date.now()}@example.com`,password='Password123!';
 const registered=await request.post(API+'/auth/register',{data:{fullName:'Commerce Player',email,password,confirmPassword:password}});expect(registered.status()).toBe(201);
 const logged=await request.post(API+'/auth/login',{data:{email,password}});const token=(await logged.json()).data.accessToken;
 const address=await request.post(API+'/addresses',{headers:{Authorization:`Bearer ${token}`},data:{receiverName:'Commerce Player',receiverPhone:'0901234567',province:'Ha Noi',district:'Ha Dong',ward:'Mo Lao',detailAddress:'123 Browser Test',isDefault:true}});expect(address.status()).toBe(201);
 await login(page,email,password);
 await page.getByRole('navigation',{name:'Tài khoản',exact:true}).getByRole('link',{name:'Hồ sơ người chơi',exact:true}).click();await page.getByRole('combobox',{name:'Trình độ',exact:true}).selectOption('INTERMEDIATE');await page.getByRole('combobox',{name:'Lối chơi',exact:true}).selectOption('BALANCED');await page.getByRole('combobox',{name:'Lực cổ tay',exact:true}).selectOption('MEDIUM');await page.getByRole('combobox',{name:'Trọng lượng ưa thích',exact:true}).selectOption('4U');await page.getByRole('button',{name:'Lưu và xem gợi ý'}).click();
 const recommended=page.locator('.chatbot-profile-results article').filter({has:page.getByText(fixture.productName,{exact:true})});
 await expect(page.getByRole('dialog',{name:'DH Chatbot'})).toBeVisible();await expect(recommended.getByText('100% phù hợp')).toBeVisible();await recommended.getByRole('link').click();await expect(page.getByRole('heading',{name:fixture.productName,level:1})).toBeVisible();
 await page.getByRole('button',{name:'Yêu thích',exact:true}).click();await expect(page.getByText('Đã lưu yêu thích.')).toBeVisible();await page.getByRole('button',{name:'Thêm vào giỏ',exact:true}).click();await page.getByRole('link',{name:'Xem giỏ hàng',exact:true}).click();await expect(page.getByText(fixture.productName,{exact:true})).toBeVisible();await page.getByRole('link',{name:'Đến thanh toán',exact:true}).click();await page.getByRole('button',{name:'Đặt hàng',exact:true}).click();await expect(page).toHaveURL(/\/orders\/\d+/);await expect(page.getByText('Chờ xác nhận',{exact:true})).toBeVisible();await page.getByRole('button',{name:'Hủy đơn hàng',exact:true}).click();await expect(page.getByText('Đã hủy',{exact:true})).toBeVisible();
 await page.getByRole('navigation',{name:'Tài khoản',exact:true}).getByRole('link',{name:'Yêu thích',exact:true}).click();await expect(page.getByRole('heading',{name:fixture.productName,exact:true})).toBeVisible();await page.getByRole('button',{name:'Bỏ yêu thích',exact:true}).click();await expect(page.getByRole('heading',{name:'Chưa có dữ liệu',exact:true})).toBeVisible();
});
