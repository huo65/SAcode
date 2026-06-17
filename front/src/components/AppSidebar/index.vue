<!--
  AppSidebar 通用侧边栏组件
  按 role 字段动态渲染 4 种角色菜单（merchant/customer/admin/driver）
  父组件监听 navigate 事件，跳转对应路由
-->
<template>
  <aside
    class="sidebar"
    :class="[
      `role-${role}`,
      {
        'sidebar--dark': role === 'admin',
        'sidebar--open': mobileOpen,
      },
    ]"
  >
    <!-- 品牌区 -->
    <div class="sidebar-brand">
      <div class="brand-icon"><i :class="brandIcon"></i></div>
      <div>
        <div class="brand-text">西电外卖</div>
        <div class="brand-sub">{{ roleLabel }}工作台</div>
      </div>
    </div>

    <!-- 导航区（按 role 动态渲染） -->
    <nav class="sidebar-nav">
      <div
        v-for="group in menuGroups"
        :key="group.title"
        class="nav-group"
      >
        <div class="nav-group-title">{{ group.title }}</div>
        <div
          v-for="item in group.items"
          :key="item.key"
          class="nav-item"
          :class="{ active: currentPage === item.key }"
          @click="emit('navigate', { key: item.key, label: item.label, path: item.path })"
        >
          <i :class="item.icon"></i>
          <span>{{ item.label }}</span>
          <span
            v-if="getBadge(item.key) > 0"
            class="nav-badge"
          >{{ getBadge(item.key) }}</span>
        </div>
      </div>
    </nav>

    <!-- Footer -->
    <div class="sidebar-footer">
      <div class="admin-profile" v-if="userInfo">
        <div class="admin-avatar">{{ avatarText }}</div>
        <div class="admin-info">
          <div class="admin-name">{{ userInfo.name || '未登录' }}</div>
          <div class="admin-role">{{ roleLabel }}</div>
        </div>
      </div>
      <el-button
        v-if="role === 'merchant'"
        class="store-toggle"
        :type="storeOpen ? 'success' : 'info'"
        size="small"
        @click="emit('toggle-store-status')"
      >
        <i :class="storeOpen ? 'fas fa-toggle-on' : 'fas fa-toggle-off'"></i>
        {{ storeOpen ? '营业中' : '已打烊' }}
      </el-button>
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
  storeOpen: { type: Boolean, default: true },
  mobileOpen: { type: Boolean, default: false },
});

const emit = defineEmits(['navigate', 'toggle-store-status']);

const roleLabel = computed(() => {
  const map = {
    merchant: '商家',
    customer: '顾客',
    admin: '管理员',
    driver: '骑手',
  };
  return map[props.role] || '用户';
});

const brandIcon = computed(() => {
  const map = {
    merchant: 'fas fa-store',
    customer: 'fas fa-utensils',
    admin: 'fas fa-shield-alt',
    driver: 'fas fa-motorcycle',
  };
  return map[props.role] || 'fas fa-utensils';
});

const avatarText = computed(() => {
  return (props.userInfo?.name || props.userInfo?.userName || 'U').slice(0, 1);
});

// 4 角色菜单配置
const MENU_CONFIG = {
  merchant: [
    {
      title: '核心功能',
      items: [
        { key: 'dashboard', label: '经营概览', icon: 'fas fa-tachometer-alt', path: '/home/merchant/dashboard' },
        { key: 'goods', label: '商品管理', icon: 'fas fa-box-open', path: '/home/merchant/goods' },
        { key: 'orders', label: '订单管理', icon: 'fas fa-clipboard-list', path: '/home/merchant/orders', badgeKey: 'pendingOrders' },
        { key: 'after-sale', label: '售后管理', icon: 'fas fa-undo-alt', path: '/home/merchant/after-sale', badgeKey: 'pendingAfterSale' },
        { key: 'review', label: '评价回复', icon: 'fas fa-comment-dots', path: '/home/merchant/review', badgeKey: 'pendingReviews' },
      ],
    },
    {
      title: '门店',
      items: [
        { key: 'store', label: '门店资料', icon: 'fas fa-store-alt', path: '/home/merchant/store' },
        { key: 'ops', label: '经营分析', icon: 'fas fa-chart-line', path: '/home/merchant/ops' },
        { key: 'info', label: '账号信息', icon: 'fas fa-user-circle', path: '/home/merchant/info' },
      ],
    },
  ],
  customer: [
    {
      title: '点餐',
      items: [
        { key: 'home', label: '推荐首页', icon: 'fas fa-home', path: '/home/customer/home' },
        { key: 'restaurants', label: '全部餐厅', icon: 'fas fa-utensils', path: '/home/customer/restaurants' },
        { key: 'cart', label: '购物车', icon: 'fas fa-shopping-cart', path: '/home/customer/cart', badgeKey: 'cartCount' },
        { key: 'orders', label: '我的订单', icon: 'fas fa-receipt', path: '/home/customer/orders' },
      ],
    },
    {
      title: '账户',
      items: [
        { key: 'wallet', label: '钱包中心', icon: 'fas fa-wallet', path: '/home/customer/wallet' },
        { key: 'address', label: '地址管理', icon: 'fas fa-map-marker-alt', path: '/home/customer/address' },
        { key: 'review', label: '我的评价', icon: 'fas fa-star', path: '/home/customer/review' },
        { key: 'info', label: '账号信息', icon: 'fas fa-user-circle', path: '/home/customer/info' },
      ],
    },
  ],
  admin: [
    {
      title: '运营',
      items: [
        { key: 'dashboard', label: '平台概览', icon: 'fas fa-chart-pie', path: '/home/admin/dashboard' },
        { key: 'wallet', label: '钱包流水', icon: 'fas fa-wallet', path: '/home/admin/wallet' },
        { key: 'audit', label: '审计日志', icon: 'fas fa-clipboard-list', path: '/home/admin/audit' },
      ],
    },
    {
      title: '治理',
      items: [
        { key: 'goods', label: '商品治理', icon: 'fas fa-box-open', path: '/home/admin/goods' },
        { key: 'order', label: '订单总览', icon: 'fas fa-receipt', path: '/home/admin/order' },
        { key: 'category', label: '分类管理', icon: 'fas fa-tags', path: '/home/admin/category' },
        { key: 'afterSale', label: '售后工单', icon: 'fas fa-undo-alt', path: '/home/admin/after-sale', badgeKey: 'pendingAfterSale' },
      ],
    },
    {
      title: '资源',
      items: [
        { key: 'user', label: '用户管理', icon: 'fas fa-users', path: '/home/admin/user' },
        { key: 'ops', label: '权限配置', icon: 'fas fa-shield-alt', path: '/home/admin/ops' },
      ],
    },
  ],
  driver: [
    {
      title: '工作',
      items: [
        { key: 'overview', label: '今日概况', icon: 'fas fa-tachometer-alt', path: '/home/driver/overview' },
        { key: 'available', label: '待接订单', icon: 'fas fa-list-ul', path: '/home/driver/available', badgeKey: 'availableOrders' },
        { key: 'delivering', label: '配送中', icon: 'fas fa-motorcycle', path: '/home/driver/delivering', badgeKey: 'deliveringOrders' },
      ],
    },
    {
      title: '账户',
      items: [
        { key: 'history', label: '历史订单', icon: 'fas fa-history', path: '/home/driver/history' },
        { key: 'earnings', label: '收益明细', icon: 'fas fa-coins', path: '/home/driver/earnings' },
        { key: 'info', label: '账号信息', icon: 'fas fa-user-circle', path: '/home/driver/info' },
      ],
    },
  ],
};

const menuGroups = computed(() => MENU_CONFIG[props.role] || MENU_CONFIG.merchant);

const getBadge = (key) => {
  // 通过 menu item 上的 badgeKey 映射到 props.badges 中的字段
  const item = menuGroups.value
    .flatMap((g) => g.items)
    .find((i) => i.key === key);
  if (!item || !item.badgeKey) return 0;
  return props.badges[item.badgeKey] || 0;
};
</script>

<style lang="less" scoped>
@import '@/style/sidebar.less';

.store-toggle {
  width: 100%;
  margin-top: 10px;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.store-toggle i {
  margin-right: 6px;
}
</style>
