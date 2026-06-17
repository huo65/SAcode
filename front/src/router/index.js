import { createRouter, createWebHashHistory } from "vue-router";
import Login from "@/page/login/index.vue";
import Home from "@/page/home/index.vue";
import merchantRoutes from "@/router/modules/merchant";
import adminRoutes from "@/router/modules/admin";
import customerRoutes from "@/router/modules/customer";
import driverRoutes from "@/router/modules/driver";
import $store from "@/store";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", component: Login },
  {
    path: "/home",
    component: Home,
  },
  ...merchantRoutes,
  ...adminRoutes,
  ...customerRoutes,
  ...driverRoutes,
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

const getUserRole = () => {
  const role = $store.state.userInfo?.status || $store.state.userInfo?.type;
  const roleMap = {
    cus: "customer",
    customer: "customer",
    mer: "merchant",
    merchant: "merchant",
    admin: "admin",
    driver: "driver",
  };
  return roleMap[role] || "";
};

const roleHomePath = {
  customer: "/home/customer/home",
  merchant: "/home/merchant/dashboard",
  admin: "/home/admin/dashboard",
  driver: "/home/driver/overview",
};

router.beforeEach((to) => {
  const hasToken = Boolean(sessionStorage.getItem("token"));
  const guestMode = Boolean(sessionStorage.getItem("guestMode"));
  const isHomeRoute = to.path === "/home" || to.path.startsWith("/home/");
  const role = getUserRole();

  if (isHomeRoute && !$store.state.userInfo.id && !hasToken && !guestMode) {
    return "/login";
  }
  if (to.path === "/login" && $store.state.userInfo.id && hasToken) {
    return "/home";
  }
  if (to.path === "/home" && roleHomePath[role]) {
    return roleHomePath[role];
  }
  if (isHomeRoute && role) {
    const targetRole = to.path.split("/")[2];
    if (["customer", "merchant", "admin", "driver"].includes(targetRole) && targetRole !== role) {
      return roleHomePath[role] || "/home";
    }
  }
  if (guestMode && to.path.startsWith("/home/") && !to.path.startsWith("/home/customer")) {
    return "/home/customer/home";
  }
  return true;
});

export default router;
