<template>
  <component :is="currentPage" v-if="currentPage" />
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { curStatus } from '@/store';
import Admin from './admin/index.vue';
import Customer from './customer/index.vue';
import Merchant from './merchant/index.vue';
import Driver from './driver/index.vue';

const route = useRoute();

// 商家端：直接渲染新版 Merchant 组件（内部已含 Sidebar + router-view）
// 其他角色保持原状
const currentPage = computed(() => {
  if (curStatus.value === 'merchant') return Merchant;
  if (curStatus.value === 'admin') return Admin;
  if (curStatus.value === 'customer' || curStatus.value === 'guest') return Customer;
  if (curStatus.value === 'driver') return Driver;
  return null;
});
</script>

<style lang="less" scoped>
// 商家端 Merchant 组件自带 page-container，其他角色保留 padding
:deep(.home-fallback) {
  padding-bottom: 24px;
}
</style>
