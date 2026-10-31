import {test,expect} from "@playwright/test";
import {randomUUID} from "node:crypto";
import {execFileSync} from "node:child_process";
import path from "node:path";

test("register, log in and place one COD order from the browser",async({page,context})=>{
 const email="browser-"+randomUUID()+"@example.test";const password="Browser-test-password";
 let userId:string|undefined;
 page.on("pageerror",e=>console.log("Browser error:",e.message));
 page.on("console",m=>{if(m.type()==="error")console.log("Browser console:",m.text());});
 page.on("response",r=>{if(r.request().method()==="POST")console.log("Action HTTP:",r.status(),new URL(r.url()).pathname);});
 try{
  await page.goto("/dang-ky");
  const registration=page.locator("form").filter({has:page.getByRole("heading",{name:"Đăng ký",exact:true})});
  await registration.getByLabel("Email",{exact:true}).fill(email);
  await registration.getByLabel("Mật khẩu",{exact:true}).fill(password);
  await registration.getByRole("button",{name:"Tạo tài khoản",exact:true}).click();
  await expect(page.getByRole("status")).toContainText("Đăng ký thành công");
  const login=page.locator("form").filter({has:page.getByRole("heading",{name:"Đăng nhập",exact:true})});
  await login.getByLabel("Email",{exact:true}).fill(email);
  await login.getByLabel("Mật khẩu",{exact:true}).fill(password);
  await login.getByRole("button",{name:"Đăng nhập",exact:true}).click();
  await expect(page).toHaveURL(/gio-hang/);
  const token=(await context.cookies()).find(c=>c.name==="flora_session");
  expect(token?.httpOnly).toBe(true);
  const auth={Authorization:"Bearer "+token!.value};
  const profile=await context.request.get("http://127.0.0.1:8080/api/auth/me",{headers:auth});
  userId=(await profile.json()).id;
  await page.goto("/");
  const product=page.locator("article").filter({has:page.getByRole("heading",{name:"Nắng dịu dàng",exact:true})});
  await product.getByRole("button",{name:"Thêm vào giỏ",exact:true}).click();
  await expect(page).toHaveURL(/gio-hang/);
  await page.getByRole("link",{name:"Tiếp tục thanh toán COD"}).click();
  await page.getByLabel("Người nhận",{exact:true}).fill("Browser test recipient");
  await page.getByLabel("Số điện thoại",{exact:true}).fill("0900000000");
  await page.getByLabel("Địa chỉ giao",{exact:true}).fill("Synthetic browser test address");
  const date=new Date();date.setDate(date.getDate()+1);
  await page.getByLabel("Ngày giao",{exact:true}).fill([date.getFullYear(),String(date.getMonth()+1).padStart(2,"0"),String(date.getDate()).padStart(2,"0")].join("-"));
  await page.getByRole("button",{name:"Xem báo giá",exact:true}).click();
  await expect(page.getByRole("heading",{name:"Xác nhận đơn COD",exact:true})).toBeVisible();
  await expect(page.getByText("Tổng COD:",{exact:false})).toContainText("380.000");
  await page.getByRole("button",{name:"Xác nhận đặt đơn COD",exact:true}).click();
  await expect(page).toHaveURL(/\/don-hang\/[0-9a-f-]{36}/);
  await expect(page.getByText("Trạng thái: Chờ xử lý",{exact:true})).toBeVisible();
  await expect(page.getByText("Thanh toán COD: Chưa thu tiền",{exact:true})).toBeVisible();
  const orderId=page.url().split("/").pop();
  const detail=await context.request.get("http://127.0.0.1:8080/api/orders/"+orderId,{headers:auth});
  expect(detail.status()).toBe(200);expect((await detail.json()).total).toBe(380000);
  await page.getByRole("button",{name:"Hủy đơn",exact:true}).click();
  await expect(page.getByText("Trạng thái: Đã hủy",{exact:true})).toBeVisible();
 }finally{
  if(!userId && /^browser-[0-9a-f-]{36}@example[.]test$/.test(email)){
   const result=execFileSync("docker",["compose","exec","-T","postgres","psql","-U","flora","-d","flora","-At","-c","SELECT id FROM identity.users WHERE email='"+email+"'"],{cwd:path.resolve(process.cwd(),".."),encoding:"utf8"});
   userId=result.trim()||undefined;
  }
  if(userId && /^[0-9a-f-]{36}$/i.test(userId)){
   const sql='BEGIN; SELECT id FROM "order".checkout_intents WHERE user_id=\''+userId+'\' FOR UPDATE; '+
   "UPDATE inventory.stock s SET reserved=s.reserved-r.quantity FROM inventory.stock_reservations r WHERE s.item_id=r.item_id AND r.status='HELD' AND r.order_id IN (SELECT id FROM \"order\".checkout_intents WHERE user_id='"+userId+"'); "+
   'DELETE FROM inventory.stock_movements WHERE reference_id IN (SELECT id FROM "order".checkout_intents WHERE user_id=\''+userId+'\'); '+
   'DELETE FROM inventory.stock_reservations WHERE order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id=\''+userId+'\'); '+
   'DELETE FROM inventory.reservation_batches WHERE order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id=\''+userId+'\'); '+
   'DELETE FROM "order".checkout_requests WHERE user_id=\''+userId+'\'; '+
   'DELETE FROM "order".order_status_history WHERE order_id IN (SELECT id FROM "order".orders WHERE user_id=\''+userId+'\'); '+
   'DELETE FROM "order".order_items WHERE order_id IN (SELECT id FROM "order".orders WHERE user_id=\''+userId+'\'); '+
   'DELETE FROM "order".orders WHERE user_id=\''+userId+'\'; DELETE FROM "order".checkout_intents WHERE user_id=\''+userId+'\'; DELETE FROM "order".checkout_quotes WHERE user_id=\''+userId+'\'; '+
   "DELETE FROM identity.sessions WHERE user_id='"+userId+"'; DELETE FROM identity.user_roles WHERE user_id='"+userId+"'; DELETE FROM identity.credentials WHERE user_id='"+userId+"'; DELETE FROM identity.users WHERE id='"+userId+"'; COMMIT;";
   execFileSync("docker",["compose","exec","-T","postgres","psql","-U","flora","-d","flora","-v","ON_ERROR_STOP=1","-c",sql],{cwd:path.resolve(process.cwd(),".."),stdio:"pipe"});
  }
 }
});