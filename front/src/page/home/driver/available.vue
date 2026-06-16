<template>
  <div class="driver-available">
    <!-- 页头 -->
    <div class="page-header">
      <h3>待接订单</h3>
      <el-tag v-if="availableOrders.length" type="danger" effect="dark" round>
        {{ availableOrders.length }} 单待接
      </el-tag>
    </div>

    <!-- 服务区域筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="serviceAreaInput"
        placeholder="按服务区域筛选"
        clearable
        size="small"
        style="max-width: 220px"
        @keyup.enter="applyServiceArea"
      >
        <template #prefix><i class="fas fa-map-marker-alt"></i></template>
      </el-input>
      <el-button size="small" @click="applyServiceArea">筛选</el-button>
      <el-button v-if="driverServiceArea" size="small" @click="clearServiceArea">全城</el-button>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-if="availableOrders.length">
      <div class="order-card" v-for="order in availableOrders" :key="order.orderInfo?.id">
        <div class="order-card-top">
          <span class="order-id">#{{ order.orderInfo?.id }}</span>
          <span class="order-time" :class="isUrgent(order) ? 'urgent' : 'normal'">
            <i class="fas fa-clock" style="font-size:10px"></i>
            {{ isUrgent(order) ? '即将超时' : '等待接单' }}
          </span>
        </div>
        <div class="order-shop">
          <i class="fas fa-store"></i>
          {{ order.merName || '商家' }}
        </div>
        <div class="order-addr">
          <i class="fas fa-location-dot"></i>
          {{ order.delivery || order.receive || '配送地址' }}
        </div>
        <div class="order-customer">
          <i class="fas fa-user"></i>
          {{ order.cusName || '顾客' }}
        </div>
        <div class="order-card-footer">
          <div class="order-fee">
            配送费 <strong>¥{{ calcDeliveryFee(order) }}</strong>
          </div>
          <el-button type="primary" round size="small" @click="acceptOrder(order)">
            <i class="fas fa-check" style="margin-right:4px"></i>接单
          </el-button>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <i class="fas fa-inbox"></i>
      <span>暂无待接订单</span>
      <p>在线等待中，有新订单将自动推送</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import $store, { userInfo } from '@/store';
import { Order as OrderApi } from '@/api/apis';
import fetch from '@/api/fetch';
import { isDriverOwnedOrder, normalizeOrderItems } from '@/lib/orderDriverHelper';

const driverOrders = ref([]);
const serviceAreaInput = ref(userInfo.value.driverServiceArea || '');
let pollTimer = null;

const DELIVERY_RATE = 0.1;
const MIN_DELIVERY_FEE = 4;
const DISPATCH_TIMEOUT_MINUTES = 10;

const driverServiceArea = computed(() => (userInfo.value.driverServiceArea || '').trim());

const availableOrders = computed(() => {
  return driverOrders.value.filter((item) => {
    if (item?.orderInfo?.state !== 3) return false;
    if (isDriverOwnedOrder(item, userInfo.value.id)) return false;
    if (!driverServiceArea.value) return true;
    const keyword = driverServiceArea.value.toLowerCase();
    const haystack = [item?.delivery, item?.receive, item?.cusName, item?.merName]
      .filter(Boolean).join(' ').toLowerCase();
    return haystack.includes(keyword);
  });
});

const calcDeliveryFee = (item) => {
  const amount = Number(item?.orderInfo?.account || 0);
  return Math.max(MIN_DELIVERY_FEE, Math.round(amount * DELIVERY_RATE));
};

const isUrgent = (item) => {
  if (!item?.orderInfo?.time) return false;
  const date = new Date(String(item.orderInfo.time).replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) return false;
  return (Date.now() - date.getTime()) / 60000 >= DISPATCH_TIMEOUT_MINUTES;
};

const acceptOrder = (order) => {
  fetch(OrderApi.updateOrder, {
    orderId: order.orderInfo?.id,
    state: 1,
    driverId: userInfo.value.id,
  }).then(() => {
    ElMessage.success('接单成功！');
    refreshOrders();
  }).catch((err) => {
    ElMessage.error(err?.message || '接单失败');
  });
};

const applyServiceArea = () => {
  const value = serviceAreaInput.value.trim();
  $store.commit('setDriverServiceArea', value);
  ElMessage.success(value ? '区域筛选已更新' : '已切换为全城');
};

const clearServiceArea = () => {
  serviceAreaInput.value = '';
  $store.commit('setDriverServiceArea', '');
  ElMessage.success('已切换为全城接单');
};

const refreshOrders = () => {
  if (!userInfo.value?.id) return;
  fetch(OrderApi.getOrderList, { usrId: userInfo.value.id, timeOrder: 1 }).then((data) => {
    driverOrders.value = normalizeOrderItems(data?.driverList || []);
  });
};

onMounted(() => {
  refreshOrders();
  pollTimer = setInterval(refreshOrders, 10000);
});

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer);
});
</script>

<style lang="less" scoped>
.driver-available {
  padding: 0 0 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0 16px;

  h3 {
    margin: 0;
    font-size: 22px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary, #111827);
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  padding: 18px 20px;
  background: var(--card, #fff);
  border-radius: 14px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.order-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-id {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  font-weight: 500;
}

.order-time {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 4px;

  &.urgent {
    background: #FEF2F2;
    color: #DC2626;
  }
  &.normal {
    background: #ECFDF5;
    color: #059669;
  }
}

.order-shop {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, #111827);
  margin-bottom: 6px;

  i {
    color: var(--primary, #059669);
    margin-right: 6px;
    font-size: 13px;
  }
}

.order-addr, .order-customer {
  font-size: 12px;
  color: var(--text-secondary, #4B5563);
  margin-bottom: 4px;

  i {
    color: var(--text-muted, #9CA3AF);
    margin-right: 6px;
    font-size: 10px;
    width: 14px;
  }
}

.order-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid var(--border, #F0F0F0);
}

.order-fee {
  font-size: 13px;
  color: var(--text-secondary, #4B5563);

  strong {
    color: var(--primary, #059669);
    font-size: 18px;
    font-weight: 800;
  }
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 60px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;
  text-align: center;

  i { font-size: 48px; opacity: 0.3; }
  p { font-size: 12px; margin: 4px 0 0; }
}
</style>
