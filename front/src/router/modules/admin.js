// 管理端子路由
// 在 src/router/index.js 中通过 ...adminRoutes 引入

import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Category from "@/components/category/index.vue";
import User from "@/components/user/index.vue";
import AfterSaleBoard from "@/components/after-sale/index.vue";
import AdminOps from "@/components/operation/admin-ops.vue";
import AdminWallet from "@/page/home/admin/wallet.vue";
import AdminAudit from "@/page/home/admin/audit.vue";
import AdminDashboard from "@/page/home/admin/dashboard.vue";

const adminRoutes = [
  {
    path: "/home/admin",
    redirect: "/home/admin/dashboard",
  },
  {
    path: "/home/admin/dashboard",
    name: "AdminDashboard",
    component: AdminDashboard,
    meta: { title: "平台概览", sidebarKey: "dashboard" },
  },
  {
    path: "/home/admin/wallet",
    name: "AdminWallet",
    component: AdminWallet,
    meta: { title: "钱包流水", sidebarKey: "wallet" },
  },
  {
    path: "/home/admin/audit",
    name: "AdminAudit",
    component: AdminAudit,
    meta: { title: "审计日志", sidebarKey: "audit" },
  },
  {
    path: "/home/admin/goods",
    name: "AdminGoods",
    component: Goods,
    meta: { title: "商品治理", sidebarKey: "goods" },
  },
  {
    path: "/home/admin/order",
    name: "AdminOrder",
    component: Order,
    meta: { title: "订单总览", sidebarKey: "order" },
  },
  {
    path: "/home/admin/category",
    name: "AdminCategory",
    component: Category,
    meta: { title: "分类管理", sidebarKey: "category" },
  },
  {
    path: "/home/admin/after-sale",
    name: "AdminAfterSale",
    component: AfterSaleBoard,
    props: { scope: "admin" },
    meta: { title: "售后工单", sidebarKey: "afterSale" },
  },
  {
    path: "/home/admin/user",
    name: "AdminUser",
    component: User,
    meta: { title: "用户管理", sidebarKey: "user" },
  },
  {
    path: "/home/admin/ops",
    name: "AdminOps",
    component: AdminOps,
    meta: { title: "权限配置", sidebarKey: "ops" },
  },
];

export default adminRoutes;
