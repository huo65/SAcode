import AdminLayout from "@/page/home/admin/index.vue";
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
    component: AdminLayout,
    redirect: "/home/admin/dashboard",
    children: [
      {
        path: "dashboard",
        name: "AdminDashboard",
        component: AdminDashboard,
        meta: { title: "平台概览", sidebarKey: "dashboard" },
      },
      {
        path: "wallet",
        name: "AdminWallet",
        component: AdminWallet,
        meta: { title: "钱包流水", sidebarKey: "wallet" },
      },
      {
        path: "audit",
        name: "AdminAudit",
        component: AdminAudit,
        meta: { title: "审计日志", sidebarKey: "audit" },
      },
      {
        path: "goods",
        name: "AdminGoods",
        component: Goods,
        meta: { title: "商品治理", sidebarKey: "goods" },
      },
      {
        path: "order",
        name: "AdminOrder",
        component: Order,
        meta: { title: "订单总览", sidebarKey: "order" },
      },
      {
        path: "category",
        name: "AdminCategory",
        component: Category,
        meta: { title: "分类管理", sidebarKey: "category" },
      },
      {
        path: "after-sale",
        name: "AdminAfterSale",
        component: AfterSaleBoard,
        props: { scope: "admin" },
        meta: { title: "售后工单", sidebarKey: "afterSale" },
      },
      {
        path: "user",
        name: "AdminUser",
        component: User,
        meta: { title: "用户管理", sidebarKey: "user" },
      },
      {
        path: "ops",
        name: "AdminOps",
        component: AdminOps,
        meta: { title: "权限配置", sidebarKey: "ops" },
      },
    ],
  },
];

export default adminRoutes;
