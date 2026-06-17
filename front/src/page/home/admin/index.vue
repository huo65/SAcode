<template>
  <div class="page-container role-admin">
    <AppSidebar
      role="admin"
      :current-page="currentSidebarKey"
      :badges="badges"
      :mobile-open="mobileOpen"
      @navigate="handleNavigate"
    />

    <div class="page-main">
      <Header @toggle-mobile="mobileOpen = !mobileOpen" />

      <main class="page-content">
        <div v-if="mobileOpen" class="mobile-mask" @click="mobileOpen = false" />
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="$route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import Header from "@/page/home/components/Header.vue";
import AppSidebar from "@/components/AppSidebar/index.vue";
import fetch from "@/api/fetch";
import { AfterSale } from "@/api/apis";

const route = useRoute();
const router = useRouter();

const mobileOpen = ref(false);
const pendingAfterSale = ref(0);

const badges = computed(() => ({
  pendingAfterSale: pendingAfterSale.value,
}));

const currentSidebarKey = computed(() => route.meta?.sidebarKey || "dashboard");

const handleNavigate = ({ path }) => {
  mobileOpen.value = false;
  if (path && route.path !== path) router.push(path);
};

let pollTimer = null;
const pollAfterSale = () => {
  fetch(AfterSale.stats, { scope: "admin" })
    .then((data) => {
      pendingAfterSale.value = Number(data?.stats?.pending || 0);
    })
    .catch(() => {
      pendingAfterSale.value = 0;
    });
};

const setBodyRoleClass = () => {
  document.body.classList.remove("role-merchant", "role-customer", "role-admin", "role-rider", "role-driver");
  document.body.classList.add("role-admin");
};

watch(() => route.fullPath, () => {
  mobileOpen.value = false;
});

onMounted(() => {
  setBodyRoleClass();
  if (route.path === "/home" || route.path === "/home/admin") {
    router.replace("/home/admin/dashboard");
  }
  pollAfterSale();
  pollTimer = window.setInterval(pollAfterSale, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) window.clearInterval(pollTimer);
});
</script>

<style lang="less" scoped>
.page-content {
  position: relative;
  padding: 24px 28px;
  min-height: calc(100vh - var(--header-h));
  overflow-y: auto;
}

.mobile-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.42);
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
  transition: all 0.18s ease;
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
