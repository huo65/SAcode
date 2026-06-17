<template>
  <div class="phone-app-shell role-rider">
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

        <nav class="phone-tab-bar" aria-label="骑手端导航">
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
import { Order as OrderApi } from '@/api/apis';
import fetch from '@/api/fetch';
import $store, { userInfo } from '@/store';
import { isDriverOwnedOrder, normalizeOrderItems } from '@/lib/orderDriverHelper';

const route = useRoute();
const router = useRouter();

const availableCount = ref(0);
const deliveringCount = ref(0);
const currentTime = ref('00:00');
let pollTimer = null;
let clockTimer = null;

const currentPage = computed(() => route.meta?.sidebarKey || 'overview');

const tabs = computed(() => [
  { key: 'overview', label: '概况', icon: 'fas fa-gauge-high', path: '/home/driver/overview' },
  {
    key: 'available',
    label: '接单',
    icon: 'fas fa-list-ul',
    path: '/home/driver/available',
    badge: availableCount.value,
  },
  {
    key: 'delivering',
    label: '配送',
    icon: 'fas fa-motorcycle',
    path: '/home/driver/delivering',
    badge: deliveringCount.value,
  },
  { key: 'history', label: '历史', icon: 'fas fa-clock-rotate-left', path: '/home/driver/history' },
  { key: 'info', label: '我的', icon: 'fas fa-user-circle', path: '/home/driver/info' },
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

const loadBadgeCounts = () => {
  if (!userInfo.value?.id || userInfo.value.type !== 'driver') return;
  fetch(OrderApi.getOrderList, {
    usrId: userInfo.value.id,
    timeOrder: 1,
  })
    .then((data) => {
      const items = normalizeOrderItems(data?.driverList || []);
      availableCount.value = items.filter(
        (item) => item?.orderInfo?.state === 3 && !isDriverOwnedOrder(item, userInfo.value.id)
      ).length;
      deliveringCount.value = items.filter(
        (item) => item?.orderInfo?.state === 1 && isDriverOwnedOrder(item, userInfo.value.id)
      ).length;
    })
    .catch(() => {
      availableCount.value = 0;
      deliveringCount.value = 0;
    });
};

const setBodyRoleClass = () => {
  document.body.classList.remove('role-merchant', 'role-customer', 'role-admin', 'role-rider', 'role-driver');
  document.body.classList.add('role-rider');
};

onMounted(() => {
  setBodyRoleClass();
  if (route.path === '/home' || route.path === '/home/driver') {
    router.replace('/home/driver/overview');
  }
  updateClock();
  loadBadgeCounts();
  clockTimer = window.setInterval(updateClock, 60000);
  pollTimer = window.setInterval(loadBadgeCounts, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) window.clearInterval(pollTimer);
  if (clockTimer) window.clearInterval(clockTimer);
});

watch(() => route.fullPath, loadBadgeCounts);
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
