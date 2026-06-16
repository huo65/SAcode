<template>
  <div class="driver-history">
    <!-- 页头 -->
    <div class="page-header">
      <h3>历史订单</h3>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="filter-tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="history-list" v-if="filteredOrders.length">
      <div class="history-card" v-for="order in filteredOrders" :key="order.orderInfo?.id">
        <div class="history-card-top">
          <span class="history-id">#{{ order.orderInfo?.id }}</span>
          <span class="history-status" :class="order.orderInfo?.state === 2 ? 'completed' : 'cancelled'">
            {{ order.orderInfo?.state === 2 ? '已完成' : '已取消' }}
          </span>
        </div>
        <div class="history-shop">{{ order.merName || '商家' }} → {{ order.cusName || '顾客' }}</div>
        <div class="history-route">
          <i class="fas fa-route"></i>
          {{ order.delivery || order.receive || '配送路线' }}
        </div>
        <div class="history-card-bottom">
          <span class="history-fee">+¥{{ calcDeliveryFee(order) }}</span>
          <span class="history-time">{{ order.orderInfo?.time || '-' }}</span>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <i class="fas fa-history"></i>
      <span>暂无配送记录</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { userInfo } from '@/store';
import { Order as OrderApi } from '@/api/apis';
import fetch from '@/api/fetch';
import { isDriverOwnedOrder, normalizeOrderItems } from '@/lib/orderDriverHelper';

const driverOrders = ref([]);
const activeTab = ref('all');

const DELIVERY_RATE = 0.1;
const MIN_DELIVERY_FEE = 4;

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'completed', label: '已完成' },
  { key: 'cancelled', label: '已取消' },
];

const ownCompletedOrCancelled = computed(() =>
  driverOrders.value.filter((item) => {
    if (!isDriverOwnedOrder(item, userInfo.value.id)) return false;
    return item?.orderInfo?.state === 2 || item?.orderInfo?.state === 4;
  })
);

const filteredOrders = computed(() => {
  if (activeTab.value === 'completed') {
    return ownCompletedOrCancelled.value.filter((item) => item?.orderInfo?.state === 2);
  }
  if (activeTab.value === 'cancelled') {
    return ownCompletedOrCancelled.value.filter((item) => item?.orderInfo?.state === 4);
  }
  return ownCompletedOrCancelled.value;
});

const calcDeliveryFee = (item) => {
  const amount = Number(item?.orderInfo?.account || 0);
  return Math.max(MIN_DELIVERY_FEE, Math.round(amount * DELIVERY_RATE));
};

const loadOrders = () => {
  if (!userInfo.value?.id) return;
  fetch(OrderApi.getOrderList, { usrId: userInfo.value.id, timeOrder: 1 }).then((data) => {
    driverOrders.value = normalizeOrderItems(data?.driverList || []);
  });
};

onMounted(() => {
  loadOrders();
});
</script>

<style lang="less" scoped>
.driver-history {
  padding: 0 0 24px;
}

.page-header {
  padding: 4px 0 16px;

  h3 {
    margin: 0;
    font-size: 22px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary, #111827);
  }
}

.filter-tabs {
  display: flex;
  gap: 0;
  background: var(--card, #fff);
  border-radius: 12px;
  border: 1px solid var(--border, #F0F0F0);
  margin-bottom: 18px;
  overflow: hidden;
}

.filter-tab {
  flex: 1;
  padding: 10px 0;
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted, #9CA3AF);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;

  &:hover {
    color: var(--text-secondary, #4B5563);
  }

  &.active {
    color: var(--primary, #059669);
    font-weight: 700;
    border-bottom-color: var(--primary, #059669);
    background: var(--primary-light, #ECFDF5);
  }
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-card {
  padding: 16px 18px;
  background: var(--card, #fff);
  border-radius: 12px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.history-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-id {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
}

.history-status {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;

  &.completed {
    background: #ECFDF5;
    color: #059669;
  }
  &.cancelled {
    background: #FEF2F2;
    color: #DC2626;
  }
}

.history-shop {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #111827);
  margin-bottom: 4px;
}

.history-route {
  font-size: 12px;
  color: var(--text-secondary, #4B5563);

  i {
    color: var(--text-muted, #9CA3AF);
    font-size: 10px;
    margin-right: 4px;
  }
}

.history-card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--border, #F0F0F0);
}

.history-fee {
  font-size: 16px;
  font-weight: 800;
  color: var(--primary, #059669);
}

.history-time {
  font-size: 11px;
  color: var(--text-muted, #9CA3AF);
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 60px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;

  i { font-size: 48px; opacity: 0.3; }
}
</style>
