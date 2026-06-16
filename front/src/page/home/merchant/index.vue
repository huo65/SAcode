<template>
  <div class="page-container">
    <AppSidebar
      role="merchant"
      :current-page="currentPage"
      :store-open="storeOpen"
      :mobile-open="mobileOpen"
      :badges="badges"
      @navigate="onNavigate"
      @toggle-store-status="toggleStoreStatus"
      @close-mobile="mobileOpen = false"
    />

    <div class="page-main">
      <Header @toggle-mobile="mobileOpen = !mobileOpen" />

      <div class="page-content">
        <!-- 移动端遮罩 -->
        <div class="mobile-mask" v-if="mobileOpen" @click="mobileOpen = false"></div>

        <!-- 路由出口 -->
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="$route.fullPath" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppSidebar from '@/components/AppSidebar/index.vue';
import Header from '@/page/home/components/Header.vue';
import { userInfo } from '@/store';
import { Order as OrderApi, AfterSale } from '@/api/apis';
import fetch from '@/api/fetch';

const route = useRoute();
const router = useRouter();

const mobileOpen = ref(false);
const storeOpen = ref(true);
const pendingOrderCount = ref(0);
const pendingTicketCount = ref(0);

const badges = computed(() => ({
  pendingOrders: pendingOrderCount.value,
  pendingTickets: pendingTicketCount.value,
}));

const currentPage = computed(() => {
  return route.meta?.sidebarKey || 'dashboard';
});

const onNavigate = ({ key }) => {
  router.push(`/home/merchant/${key}`);
};

const toggleStoreStatus = () => {
  storeOpen.value = !storeOpen.value;
};

// ==================== 拉取角标 ====================
let pollTimer = null;
let initialized = false;

const loadPendingCounts = () => {
  if (userInfo.value.type !== 'mer') return;
  fetch(OrderApi.getOrderList, {
    usrId: userInfo.value.id,
    state: 0,
    timeOrder: 1,
  }).then((data) => {
    pendingOrderCount.value = data?.merList?.length || 0;
    initialized = true;
  });
  fetch(AfterSale.stats, { scope: 'merchant' }).then((data) => {
    pendingTicketCount.value = Number(data?.stats?.pending || 0);
  });
};

// 默认跳到 dashboard
onMounted(() => {
  if (route.path === '/home' || route.path === '/home/merchant') {
    router.replace('/home/merchant/dashboard');
  }
  loadPendingCounts();
  pollTimer = setInterval(loadPendingCounts, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer);
});

// 监听路由变化关闭移动端 sidebar
watch(() => route.fullPath, () => {
  mobileOpen.value = false;
});
</script>

<style lang="less" scoped>
.page-content {
  position: relative;
  padding: 28px 32px;
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

// 路由切换动画
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
