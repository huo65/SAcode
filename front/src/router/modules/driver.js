import DriverLayout from "@/page/home/driver/index.vue";
import DriverOverview from "@/page/home/driver/overview.vue";
import DriverAvailable from "@/page/home/driver/available.vue";
import DriverDelivering from "@/page/home/driver/delivering.vue";
import DriverHistory from "@/page/home/driver/history.vue";
import DriverEarnings from "@/page/home/driver/earnings.vue";
import DriverInfo from "@/components/info/index.vue";

const driverRoutes = [
  {
    path: "/home/driver",
    component: DriverLayout,
    redirect: "/home/driver/overview",
    children: [
      {
        path: "overview",
        name: "DriverOverview",
        component: DriverOverview,
        meta: { title: "今日概况", sidebarKey: "overview" },
      },
      {
        path: "available",
        name: "DriverAvailable",
        component: DriverAvailable,
        meta: { title: "待接订单", sidebarKey: "available" },
      },
      {
        path: "delivering",
        name: "DriverDelivering",
        component: DriverDelivering,
        meta: { title: "配送中", sidebarKey: "delivering" },
      },
      {
        path: "history",
        name: "DriverHistory",
        component: DriverHistory,
        meta: { title: "历史订单", sidebarKey: "history" },
      },
      {
        path: "earnings",
        name: "DriverEarnings",
        component: DriverEarnings,
        meta: { title: "收益明细", sidebarKey: "earnings" },
      },
      {
        path: "info",
        name: "DriverInfo",
        component: DriverInfo,
        meta: { title: "账号信息", sidebarKey: "info" },
      },
    ],
  },
];

export default driverRoutes;
