<template>
  <div class="phone-app-shell role-customer">
    <div class="phone-frame">
      <div class="phone-inner">
        <div class="phone-status-bar">
          <span class="time">{{ currentTime }}</span>
          <span class="icons">
            <i class="fas fa-signal"></i>
            <i class="fas fa-wifi"></i>
            <i class="fas fa-battery-three-quarters"></i>
            <button class="phone-logout" type="button" title="退出登录" @click="logout">
              <i class="fas fa-right-from-bracket"></i>
            </button>
          </span>
        </div>

        <main class="phone-screen">
          <router-view v-slot="{ Component }">
            <transition name="phone-fade" mode="out-in">
              <component :is="Component" :key="$route.fullPath" />
            </transition>
          </router-view>
        </main>

        <nav class="phone-tab-bar" aria-label="顾客端导航">
          <button
            v-for="item in tabs"
            :key="item.key"
            class="phone-tab-item"
            :class="{ active: currentPage === item.key }"
            type="button"
            @click="go(item.path)"
          >
            <i :class="item.icon"></i>
            <span>{{ item.label }}</span>
            <span v-if="item.badge" class="phone-tab-badge">{{ item.badge }}</span>
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Cart as CartApi } from '@/api/apis';
import fetch from '@/api/fetch';
import $store, { curStatus, userInfo } from '@/store';

const route = useRoute();
const router = useRouter();

const cartCount = ref(0);
const currentTime = ref('00:00');
let pollTimer = null;
let clockTimer = null;

const currentPage = computed(() => route.meta?.sidebarKey || 'home');

const tabs = computed(() => [
  { key: 'home', label: '首页', icon: 'fas fa-home', path: '/home/customer/home' },
  { key: 'restaurants', label: '点餐', icon: 'fas fa-utensils', path: '/home/customer/restaurants' },
  {
    key: 'cart',
    label: '购物车',
    icon: 'fas fa-shopping-cart',
    path: '/home/customer/cart',
    badge: cartCount.value,
  },
  { key: 'orders', label: '订单', icon: 'fas fa-receipt', path: '/home/customer/orders' },
  { key: 'info', label: '我的', icon: 'fas fa-user-circle', path: '/home/customer/info' },
]);

const go = (path) => {
  if (route.path !== path) router.push(path);
};

const logout = () => {
  $store.commit('clearUserInfo');
  router.replace('/login');
};

const updateClock = () => {
  const d = new Date();
  currentTime.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

const loadCartCount = () => {
  if (curStatus.value !== 'customer' || !userInfo.value?.id) return;
  fetch(CartApi.getCart, { usrId: userInfo.value.id })
    .then((data) => {
      const items = data?.data || data?.items || data || [];
      cartCount.value = Array.isArray(items) ? items.length : 0;
    })
    .catch(() => {
      cartCount.value = 0;
    });
};

const setBodyRoleClass = () => {
  document.body.classList.remove('role-merchant', 'role-customer', 'role-admin', 'role-rider', 'role-driver');
  document.body.classList.add('role-customer');
};

onMounted(() => {
  setBodyRoleClass();
  if (route.path === '/home' || route.path === '/home/customer') {
    router.replace('/home/customer/home');
  }
  updateClock();
  loadCartCount();
  clockTimer = window.setInterval(updateClock, 60000);
  pollTimer = window.setInterval(loadCartCount, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) window.clearInterval(pollTimer);
  if (clockTimer) window.clearInterval(clockTimer);
});

watch(() => route.fullPath, loadCartCount);
</script>

<style lang="less" scoped>
.phone-fade-enter-active,
.phone-fade-leave-active {
  transition: all 0.18s ease;
}

.phone-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.phone-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
