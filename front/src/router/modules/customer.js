// 顾客端子路由
// 在 src/router/index.js 中通过 ...customerRoutes 引入

import CustomerHome from "@/page/home/customer/home.vue";
import CustomerRestaurants from "@/components/restaurant/index.vue";
import CustomerCart from "@/page/home/customer/cart.vue";
import CustomerOrders from "@/components/order/index.vue";
import CustomerWallet from "@/page/home/customer/wallet.vue";
import CustomerAddress from "@/page/home/customer/address.vue";
import CustomerReview from "@/page/home/customer/review.vue";
import CustomerInfo from "@/components/info/index.vue";

const customerRoutes = [
  {
    path: "/home/customer",
    redirect: "/home/customer/home",
  },
  {
    path: "/home/customer/home",
    name: "CustomerHome",
    component: CustomerHome,
    meta: { title: "推荐首页", sidebarKey: "home" },
  },
  {
    path: "/home/customer/restaurants",
    name: "CustomerRestaurants",
    component: CustomerRestaurants,
    meta: { title: "全部餐厅", sidebarKey: "restaurants" },
  },
  {
    path: "/home/customer/cart",
    name: "CustomerCart",
    component: CustomerCart,
    meta: { title: "购物车", sidebarKey: "cart" },
  },
  {
    path: "/home/customer/orders",
    name: "CustomerOrders",
    component: CustomerOrders,
    meta: { title: "我的订单", sidebarKey: "orders" },
  },
  {
    path: "/home/customer/wallet",
    name: "CustomerWallet",
    component: CustomerWallet,
    meta: { title: "钱包中心", sidebarKey: "wallet" },
  },
  {
    path: "/home/customer/address",
    name: "CustomerAddress",
    component: CustomerAddress,
    meta: { title: "地址管理", sidebarKey: "address" },
  },
  {
    path: "/home/customer/review",
    name: "CustomerReview",
    component: CustomerReview,
    meta: { title: "我的评价", sidebarKey: "review" },
  },
  {
    path: "/home/customer/info",
    name: "CustomerInfo",
    component: CustomerInfo,
    meta: { title: "账号信息", sidebarKey: "info" },
  },
];

export default customerRoutes;
