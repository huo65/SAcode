<template>
  <div class="page-container">
    <AppSidebar
      role="driver"
      :current-page="currentPage"
      :mobile-open="mobileOpen"
      :badges="badges"
      @navigate="onNavigate"
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
import { Order as OrderApi } from '@/api/apis';
import fetch from '@/api/fetch';
import { isDriverOwnedOrder, normalizeOrderItems } from '@/lib/orderDriverHelper';

const route = useRoute();
const router = useRouter();

const mobileOpen = ref(false);
const availableCount = ref(0);
const deliveringCount = ref(0);

const badges = computed(() => ({
  availableOrders: availableCount.value,
  deliveringOrders: deliveringCount.value,
}));

const currentPage = computed(() => {
  return route.meta?.sidebarKey || 'overview';
});

const onNavigate = ({ key }) => {
  router.push(`/home/driver/${key}`);
};

// ==================== 拉取角标 ====================
let pollTimer = null;

const loadBadgeCounts = () => {
  if (!userInfo.value?.id || userInfo.value.type !== 'driver') return;
  fetch(OrderApi.getOrderList, {
    usrId: userInfo.value.id,
    timeOrder: 1,
  }).then((data) => {
    const items = normalizeOrderItems(data?.driverList || []);
    // 待接单：state=3 且不是自己的单
    availableCount.value = items.filter(
      (item) => item?.orderInfo?.state === 3 && !isDriverOwnedOrder(item, userInfo.value.id)
    ).length;
    // 配送中：state=1 且是自己的单
    deliveringCount.value = items.filter(
      (item) => item?.orderInfo?.state === 1 && isDriverOwnedOrder(item, userInfo.value.id)
    ).length;
  }).catch(() => {
    availableCount.value = 0;
    deliveringCount.value = 0;
  });
};

// 给 body 添加角色 class，驱动 CSS 变量覆盖
const setBodyRoleClass = () => {
  document.body.classList.remove('role-merchant', 'role-customer', 'role-admin', 'role-driver');
  document.body.classList.add('role-driver');
};

onMounted(() => {
  setBodyRoleClass();
  if (route.path === '/home' || route.path === '/home/driver') {
    router.replace('/home/driver/overview');
  }
  loadBadgeCounts();
  pollTimer = setInterval(loadBadgeCounts, 15000);
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
  min-height: calc(100vh - 64px);
  overflow-y: auto;
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

// ============================================================
//  骑手端 Sidebar 环境下的子组件全局覆盖（非 scoped）
// ============================================================
<style lang="less">

.page-content .list {
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}

.page-content .item .product-img,
.page-content .item .info > img {
  width: 140px;
  height: 140px;
}

.page-content .el-tag {
  border-radius: 999px;
  font-weight: 600;
  font-size: 12px;
  padding: 2px 10px;
  border: 1px solid transparent;
}

.page-content .section-heading {
  flex-wrap: wrap;
  gap: 12px;
}

.page-content .section-heading > div:first-child h3 {
  font-size: 20px;
}

.page-content .search-form {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (max-width: 1100px) {
  .page-content .search-form {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-content .search-form {
    grid-template-columns: 1fr;
  }
}
</style>
