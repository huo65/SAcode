import { createRouter, createWebHashHistory } from "vue-router";
import Login from "@/page/login/index.vue";
import Home from "@/page/home/index.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", component: Login },
  {
    path: "/home",
    component: Home,
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

export default router;
