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

// 给 body 添加角色 class，驱动 CSS 变量覆盖
const setBodyRoleClass = () => {
  document.body.classList.remove('role-merchant', 'role-customer', 'role-admin', 'role-rider');
  document.body.classList.add('role-merchant');
};

// 默认跳到 dashboard
onMounted(() => {
  if (route.path === '/home' || route.path === '/home/merchant') {
    router.replace('/home/merchant/dashboard');
  }
  setBodyRoleClass();
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
//  商家端 Sidebar 环境下的子组件全局覆盖（非 scoped）
//  适配 Sidebar 占用 240px 后的窄内容区
// ============================================================
<style lang="less">

// --- 商品网格：Sidebar 后从 3 列改为自适应 ---
.page-content .list {
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}

// --- 订单图片：缩小以适应窄布局 ---
.page-content .item .product-img,
.page-content .item .info > img {
  width: 140px;
  height: 140px;
}

// --- el-tag 状态标签覆盖成 badge 风格 ---
.page-content .el-tag {
  border-radius: 999px;
  font-weight: 600;
  font-size: 12px;
  padding: 2px 10px;
  border: 1px solid transparent;
}

.page-content .el-tag--primary {
  background: var(--info-light);
  color: var(--info);
  border-color: fade(#0984E3, 20%);
}

.page-content .el-tag--success {
  background: var(--success-light);
  color: var(--success);
  border-color: fade(#00B894, 20%);
}

.page-content .el-tag--warning {
  background: var(--warning-light);
  color: darken(#FDCB6E, 20%);
  border-color: fade(#FDCB6E, 40%);
}

.page-content .el-tag--danger {
  background: var(--danger-light);
  color: var(--danger);
  border-color: fade(#E17055, 20%);
}

.page-content .el-tag--info {
  background: #f4f4f5;
  color: #909399;
  border-color: #e9e9eb;
}

// --- 搜索区 section-heading 在窄布局下自动换行 ---
.page-content .section-heading {
  flex-wrap: wrap;
  gap: 12px;
}

.page-content .section-heading > div:first-child h3 {
  font-size: 20px;
}

// --- 搜索表单：Sidebar 后从 5 列减为 3 列 ---
.page-content .search-form {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (max-width: 1100px) {
  .page-content .search-form {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-content .item .info {
    flex-direction: column;
  }

  .page-content .item .product-img,
  .page-content .item .info > img {
    width: 100%;
    height: 180px;
    margin: 0 0 12px;
  }
}

@media (max-width: 768px) {
  .page-content .search-form {
    grid-template-columns: 1fr;
  }
}
</style>
