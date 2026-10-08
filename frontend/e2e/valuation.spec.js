import {test,expect} from '@playwright/test';
const API=process.env.E2E_API_URL||'http://localhost:8080/api';
test('seller valuation uses classifications and preserves visible history',async({page,request})=>{
 test.skip(process.env.RUN_E2E!=='true','Requires isolated MySQL, demo catalog, backend and React');
 const email=`valuation-${Date.now()}@example.com`,password='Password123!';
 expect((await request.post(API+'/auth/register',{data:{fullName:'Valuation Seller',email,password,confirmPassword:password}})).status()).toBe(201);
 const token=(await (await request.post(API+'/auth/login',{data:{email,password}})).json()).data.accessToken;
 const categories=(await (await request.get(API+'/categories')).json()).data;
 const brands=(await (await request.get(API+'/brands')).json()).data;
 const created=await request.post(API+'/marketplace/listings',{headers:{Authorization:`Bearer ${token}`},data:{categoryId:categories[0].categoryId,brandId:brands[0].brandId,productName:'Browser Valuation '+Date.now(),model:'Browser Test',description:'Valuation fixture',originalPrice:2000000,sellingPrice:1000000,usageMonths:6,conditionGrade:'A',warrantyStatus:'EXPIRED'}});
 expect(created.status()).toBe(201);const data=(await created.json()).data;const id=data.listing.listingId;
 await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Mật khẩu',{exact:true}).fill(password);await page.getByRole('button',{name:'Đăng nhập',exact:true}).click();await expect(page).toHaveURL(/profile/);
 // Navigate inside the SPA because JWT is intentionally memory-only.
 await page.getByRole('link',{name:'Tin của tôi',exact:true}).click();
 await page.getByRole('link',{name:/Browser Valuation/}).first().click();
 await page.getByRole('combobox',{name:'Tình trạng phụ kiện',exact:true}).selectOption('FULL');
 await page.getByRole('combobox',{name:'Mức sửa chữa',exact:true}).selectOption('NONE');
 await page.getByRole('button',{name:'Ước tính giá',exact:true}).click();
 await expect(page.getByText(/Giá ước tính:/)).toHaveCount(1);
 await expect(page.getByText(/Giá ước tính:/)).toContainText('1.610.920');
 await page.getByRole('combobox',{name:'Mức sửa chữa',exact:true}).selectOption('MINOR');
 await page.getByRole('button',{name:'Ước tính giá',exact:true}).click();
 await expect(page.getByText(/Giá ước tính:/)).toHaveCount(2);
});
