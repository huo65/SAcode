<!--
  管理端 · 入口页
  集成 AppSidebar 侧边栏 + Header + router-view 三段式布局
  通过 role=admin 自动给 body 添加 role-admin class，驱动 CSS 变量切换
-->
<template>
  <div class="merchant-layout" :class="`role-${userInfo.type}`">
    <Header @toggle-sidebar="mobileOpen = !mobileOpen" />

    <div class="layout-body">
      <AppSidebar
        role="admin"
        :current-page="currentSidebarKey"
        :badges="badges"
        @navigate="handleNavigate"
      />

      <main class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <div v-if="mobileOpen" class="mobile-mask" @click="mobileOpen = false" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import Header from "@/page/home/components/Header.vue";
import AppSidebar from "@/components/AppSidebar/index.vue";
import { userInfo } from "@/store";
import fetch from "@/api/fetch";
import { AfterSale } from "@/api/apis";

const route = useRoute();
const router = useRouter();

const mobileOpen = ref(false);
const pendingAfterSale = ref(0);

const badges = computed(() => ({
  pendingAfterSale: pendingAfterSale.value,
}));

// 根据当前路由计算 sidebar 当前选中 key
const currentSidebarKey = computed(() => {
  return route.meta?.sidebarKey || "dashboard";
});

const handleNavigate = ({ key, label, path }) => {
  mobileOpen.value = false;
  if (path) {
    router.push(path);
  }
};

let pollTimer = null;
const pollAfterSale = () => {
  fetch(AfterSale.stats, { scope: "admin" }).then((data) => {
    pendingAfterSale.value = Number(data?.stats?.pending || 0);
  });
};

// 给 body 添加角色 class，驱动 CSS 变量覆盖
const setBodyRoleClass = () => {
  document.body.classList.remove("role-merchant", "role-customer", "role-admin", "role-rider");
  document.body.classList.add(`role-${userInfo.value?.type || "admin"}`);
};

watch(
  () => userInfo.value?.type,
  () => setBodyRoleClass(),
  { immediate: false }
);

onMounted(() => {
  setBodyRoleClass();
  pollAfterSale();
  pollTimer = window.setInterval(pollAfterSale, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
});
</script>

<style lang="less" scoped>
.merchant-layout {
  min-height: 100vh;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.layout-body {
  position: relative;
  display: flex;
  flex: 1;
  min-height: 0;
}

.page-content {
  position: relative;
  padding: 28px 32px;
  min-height: calc(100vh - 64px);
  overflow-y: auto;
  flex: 1;
}

.mobile-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 99;
  display: none;
}

@media (max-width: 768px) {
  .mobile-mask {
    display: block;
  }
  .page-content {
    padding: 16px;
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
