<template>
  <aside class="side-bar" :class="[`role-${role}`, { 'side-bar--dark': role === 'admin' }]">
    <!-- 品牌区 -->
    <div class="sidebar-brand">
      <div class="brand-icon"><i class="fas fa-store"></i></div>
      <div>
        <div class="brand-text">味掌柜</div>
        <div class="brand-sub">外卖管理平台</div>
      </div>
    </div>

    <!-- 导航区（静态，后续改为动态） -->
    <nav class="sidebar-nav">
      <div class="nav-group">
        <div class="nav-group-title">核心功能</div>
        <div
          class="nav-item"
          :class="{ active: currentPage === 'dashboard' }"
          @click="emit('navigate', { key: 'dashboard', label: '经营概览' })"
        >
          <i class="fas fa-tachometer-alt"></i>
          <span>经营概览</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentPage === 'goods' }"
          @click="emit('navigate', { key: 'goods', label: '商品管理' })"
        >
          <i class="fas fa-box-open"></i>
          <span>商品管理</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentPage === 'orders' }"
          @click="emit('navigate', { key: 'orders', label: '订单管理' })"
        >
          <i class="fas fa-clipboard-list"></i>
          <span>订单管理</span>
          <span class="nav-badge" v-if="badges.pendingOrders > 0">{{ badges.pendingOrders }}</span>
        </div>
      </div>
    </nav>

    <!-- Footer -->
    <div class="sidebar-footer">
      <div class="admin-profile">
        <div class="admin-avatar">U</div>
        <div class="admin-info">
          <div class="admin-name">{{ userInfo?.name || '用户' }}</div>
          <div class="admin-role">{{ role }}</div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue';
import { userInfo } from '@/store';

const props = defineProps({
  role: { type: String, default: 'merchant' },
  currentPage: { type: String, default: 'dashboard' },
  badges: { type: Object, default: () => ({}) },
});

const emit = defineEmits(['navigate']);

const roleLabel = computed(() => {
  const map = { merchant: '商家', customer: '顾客', admin: '管理员', driver: '骑手' };
  return map[props.role] || '用户';
});
</script>

<style lang="less" scoped>
@import '@/style/sidebar.less';
</style>
