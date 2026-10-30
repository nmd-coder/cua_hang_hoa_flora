import { defineConfig } from "@playwright/test";
export default defineConfig({
 testDir:"./tests",testMatch:"cod.spec.ts",workers:1,retries:0,timeout:180000,expect:{timeout:20000},
 use:{baseURL:"http://127.0.0.1:3000",channel:"msedge",headless:true,trace:"off",screenshot:"only-on-failure"}
});