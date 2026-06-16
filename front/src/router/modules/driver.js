// 骑手端子路由
// 在 src/router/index.js 中通过 ...driverRoutes 引入

import DriverOverview from "@/page/home/driver/overview.vue";
import DriverAvailable from "@/page/home/driver/available.vue";
import DriverDelivering from "@/page/home/driver/delivering.vue";
import DriverHistory from "@/page/home/driver/history.vue";
import DriverEarnings from "@/page/home/driver/earnings.vue";
import DriverInfo from "@/components/info/index.vue";

const driverRoutes = [
  {
    path: "/home/driver",
    redirect: "/home/driver/overview",
  },
  {
    path: "/home/driver/overview",
    name: "DriverOverview",
    component: DriverOverview,
    meta: { title: "今日概况", sidebarKey: "overview" },
  },
  {
    path: "/home/driver/available",
    name: "DriverAvailable",
    component: DriverAvailable,
    meta: { title: "待接订单", sidebarKey: "available" },
  },
  {
    path: "/home/driver/delivering",
    name: "DriverDelivering",
    component: DriverDelivering,
    meta: { title: "配送中", sidebarKey: "delivering" },
  },
  {
    path: "/home/driver/history",
    name: "DriverHistory",
    component: DriverHistory,
    meta: { title: "历史订单", sidebarKey: "history" },
  },
  {
    path: "/home/driver/earnings",
    name: "DriverEarnings",
    component: DriverEarnings,
    meta: { title: "收益明细", sidebarKey: "earnings" },
  },
  {
    path: "/home/driver/info",
    name: "DriverInfo",
    component: DriverInfo,
    meta: { title: "账号信息", sidebarKey: "info" },
  },
];

export default driverRoutes;
