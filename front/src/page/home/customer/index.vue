<template>
  <div class="page-container">
    <AppSidebar
      role="customer"
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
import { userInfo, curStatus } from '@/store';
import { Cart as CartApi } from '@/api/apis';
import fetch from '@/api/fetch';

const route = useRoute();
const router = useRouter();

const mobileOpen = ref(false);
const cartCount = ref(0);

const badges = computed(() => ({
  cartCount: cartCount.value,
}));

const currentPage = computed(() => {
  return route.meta?.sidebarKey || 'home';
});

const onNavigate = ({ key }) => {
  router.push(`/home/customer/${key}`);
};

// ==================== 拉取角标 ====================
let pollTimer = null;

const loadCartCount = () => {
  if (curStatus.value !== 'customer' || !userInfo.value?.id) return;
  fetch(CartApi.getCart, { usrId: userInfo.value.id }).then((data) => {
    const items = data?.data || data?.items || data || [];
    cartCount.value = Array.isArray(items) ? items.length : 0;
  }).catch(() => {
    cartCount.value = 0;
  });
};

// 给 body 添加角色 class，驱动 CSS 变量覆盖
const setBodyRoleClass = () => {
  document.body.classList.remove('role-merchant', 'role-customer', 'role-admin', 'role-rider');
  document.body.classList.add('role-customer');
};

onMounted(() => {
  setBodyRoleClass();
  if (route.path === '/home' || route.path === '/home/customer') {
    router.replace('/home/customer/home');
  }
  loadCartCount();
  pollTimer = setInterval(loadCartCount, 15000);
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
//  顾客端 Sidebar 环境下的子组件全局覆盖（非 scoped）
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
  border-color: fade(#3B82F6, 20%);
}

.page-content .el-tag--success {
  background: var(--success-light);
  color: var(--success);
  border-color: fade(#10B981, 20%);
}

.page-content .el-tag--warning {
  background: var(--warning-light);
  color: darken(#F59E0B, 20%);
  border-color: fade(#F59E0B, 40%);
}

.page-content .el-tag--danger {
  background: var(--danger-light);
  color: var(--danger);
  border-color: fade(#EF4444, 20%);
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
