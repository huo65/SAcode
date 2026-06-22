import MerchantLayout from "@/page/home/merchant/index.vue";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Info from "@/components/info/index.vue";
import StoreManage from "@/components/restaurant/store-manage.vue";
import AfterSaleBoard from "@/components/after-sale/index.vue";
import MerchantOps from "@/components/operation/merchant-ops.vue";
import MerchantReply from "@/components/review/merchant-reply.vue";
import MerchantDashboard from "@/page/home/merchant/dashboard.vue";
import MerchantWallet from "@/page/home/merchant/wallet.vue";

const merchantRoutes = [
  {
    path: "/home/merchant",
    component: MerchantLayout,
    redirect: "/home/merchant/dashboard",
    children: [
      {
        path: "dashboard",
        name: "MerchantDashboard",
        component: MerchantDashboard,
        meta: { title: "经营概览", sidebarKey: "dashboard" },
      },
      {
        path: "goods",
        name: "MerchantGoods",
        component: Goods,
        meta: { title: "商品管理", sidebarKey: "goods" },
      },
      {
        path: "orders",
        name: "MerchantOrders",
        component: Order,
        meta: { title: "订单管理", sidebarKey: "orders" },
      },
      {
        path: "after-sale",
        name: "MerchantAfterSale",
        component: AfterSaleBoard,
        meta: { title: "售后管理", sidebarKey: "after-sale" },
      },
      {
        path: "store",
        name: "MerchantStore",
        component: StoreManage,
        meta: { title: "门店资料", sidebarKey: "store" },
      },
      {
        path: "ops",
        name: "MerchantOps",
        component: MerchantOps,
        meta: { title: "经营分析", sidebarKey: "ops" },
      },
      {
        path: "wallet",
        name: "MerchantWallet",
        component: MerchantWallet,
        meta: { title: "Merchant Wallet", sidebarKey: "wallet" },
      },
      {
        path: "review",
        name: "MerchantReview",
        component: MerchantReply,
        meta: { title: "评价回复", sidebarKey: "review" },
      },
      {
        path: "info",
        name: "MerchantInfo",
        component: Info,
        meta: { title: "账号信息", sidebarKey: "info" },
      },
    ],
  },
];

export default merchantRoutes;
