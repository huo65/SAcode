<template>
  <div class="driver-delivering">
    <!-- 页头 -->
    <div class="page-header">
      <h3>配送中</h3>
      <el-tag v-if="deliveringOrders.length" type="warning" effect="dark" round>
        {{ deliveringOrders.length }} 单配送中
      </el-tag>
    </div>

    <!-- 配送中列表 -->
    <div class="delivery-list" v-if="deliveringOrders.length">
      <div class="delivery-card" v-for="order in deliveringOrders" :key="order.orderInfo?.id">
        <!-- 配送进度 -->
        <div class="delivery-progress">
          <div class="progress-steps">
            <div class="step done">
              <i class="fas fa-check"></i>
              <span>已接单</span>
            </div>
            <div class="step-connector done"></div>
            <div class="step current">
              <i class="fas fa-motorcycle"></i>
              <span>配送中</span>
            </div>
            <div class="step-connector"></div>
            <div class="step">
              <i class="fas fa-flag-checkered"></i>
              <span>已送达</span>
            </div>
          </div>
        </div>

        <!-- 订单信息 -->
        <div class="delivery-info">
          <div class="info-section">
            <div class="info-label">取餐信息</div>
            <div class="info-row">
              <i class="fas fa-store"></i>
              <span>{{ order.merName || '商家' }}</span>
            </div>
            <div class="info-row">
              <i class="fas fa-location-dot"></i>
              <span>{{ order.receive || '商家地址' }}</span>
            </div>
          </div>

          <div class="info-section">
            <div class="info-label">送餐信息</div>
            <div class="info-row">
              <i class="fas fa-user"></i>
              <span>{{ order.cusName || '顾客' }}</span>
            </div>
            <div class="info-row">
              <i class="fas fa-location-dot"></i>
              <span>{{ order.delivery || '顾客地址' }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="delivery-actions">
          <div class="order-fee">
            订单金额 <strong>¥{{ Number(order.orderInfo?.account || 0).toFixed(0) }}</strong>
            <span class="delivery-fee">配送费 ¥{{ calcDeliveryFee(order) }}</span>
          </div>
          <el-button type="primary" round @click="completeDelivery(order)">
            <i class="fas fa-check" style="margin-right:4px"></i>确认送达
          </el-button>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <i class="fas fa-motorcycle"></i>
      <span>暂无配送中的订单</span>
      <p>接单后订单将出现在这里</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userInfo } from '@/store';
import { Order as OrderApi } from '@/api/apis';
import fetch from '@/api/fetch';
import { isDriverOwnedOrder, normalizeOrderItems } from '@/lib/orderDriverHelper';

const DELIVERY_RATE = 0.1;
const MIN_DELIVERY_FEE = 4;
const driverOrders = ref([]);
let pollTimer = null;

const deliveringOrders = computed(() =>
  driverOrders.value.filter(
    (item) => item?.orderInfo?.state === 1 && isDriverOwnedOrder(item, userInfo.value.id)
  )
);

const calcDeliveryFee = (item) => {
  const amount = Number(item?.orderInfo?.account || 0);
  return Math.max(MIN_DELIVERY_FEE, Math.round(amount * DELIVERY_RATE));
};

const completeDelivery = (order) => {
  ElMessageBox.confirm('确认订单已送达？', '确认送达', { type: 'success' })
    .then(() => {
      fetch(OrderApi.updateOrder, {
        orderId: order.orderInfo?.id,
        state: 2,
      }).then(() => {
        ElMessage.success('已确认送达！');
        refreshOrders();
      }).catch((err) => {
        ElMessage.error(err?.message || '操作失败');
      });
    })
    .catch(() => {});
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
.driver-delivering {
  padding: 0 0 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0 20px;

  h3 {
    margin: 0;
    font-size: 22px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary, #111827);
  }
}

.delivery-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.delivery-card {
  background: var(--card, #fff);
  border-radius: 16px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

// ============ 配送进度 ============
.delivery-progress {
  padding: 20px 24px;
  background: linear-gradient(135deg, var(--primary, #059669), #0D9488);
  color: white;
}

.progress-steps {
  display: flex;
  align-items: center;
  gap: 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;

  i { font-size: 18px; }
  span { font-size: 11px; opacity: 0.8; }

  &.done { opacity: 0.7; }
  &.current {
    opacity: 1;
    i { font-size: 22px; }
    span { font-weight: 700; opacity: 1; }
  }
}

.step-connector {
  flex: 1;
  height: 2px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 8px;
  margin-bottom: 18px;

  &.done { background: rgba(255, 255, 255, 0.7); }
}

// ============ 订单信息 ============
.delivery-info {
  padding: 18px 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-section {}

.info-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted, #9CA3AF);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary, #4B5563);
  margin-bottom: 4px;

  i {
    color: var(--text-muted, #9CA3AF);
    font-size: 11px;
    margin-top: 3px;
    width: 14px;
    text-align: center;
  }
}

// ============ 操作按钮 ============
.delivery-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 24px;
  border-top: 1px solid var(--border, #F0F0F0);
}

.order-fee {
  font-size: 13px;
  color: var(--text-secondary, #4B5563);

  strong {
    color: var(--text-primary, #111827);
    font-size: 18px;
    font-weight: 800;
  }
}

.delivery-fee {
  color: var(--primary, #059669);
  font-weight: 700;
  margin-left: 10px;
}

// ============ 空态 ============
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

@media (max-width: 768px) {
  .delivery-info {
    grid-template-columns: 1fr;
  }
}
</style>
