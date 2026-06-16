<template>
  <div class="driver-overview">
    <!-- Hero 区域 -->
    <div class="overview-hero">
      <div>
        <p class="eyebrow">骑手工作台</p>
        <h2>{{ greeting }}, {{ userInfo.name || '骑手' }}</h2>
        <p class="hero-desc">
          配送链路、收入与异常集中展示，接单状态一目了然。
        </p>
      </div>
      <div class="hero-status">
        <div class="status-toggle">
          <span class="status-label">接单状态</span>
          <el-tag :type="isDriverOnline ? 'success' : 'info'" effect="dark" round>
            {{ isDriverOnline ? '在线接单' : '休息中' }}
          </el-tag>
          <el-tag v-if="orderSummary.delivering > 0" type="warning" effect="plain" round>
            配送中 {{ orderSummary.delivering }} 单
          </el-tag>
        </div>
        <el-button
          :type="isDriverOnline ? 'warning' : 'primary'"
          round
          @click="toggleDriverWorkStatus"
        >
          <i :class="isDriverOnline ? 'fas fa-moon' : 'fas fa-bolt'" style="margin-right:6px"></i>
          {{ isDriverOnline ? '切换休息' : '开始接单' }}
        </el-button>
      </div>
    </div>

    <!-- 收入快捷条 -->
    <div class="earnings-strip">
      <div class="earn-item">
        <div class="earn-val earn-val--green">¥{{ orderSummary.completedIncome }}</div>
        <div class="earn-label">今日收入</div>
      </div>
      <div class="earn-item">
        <div class="earn-val">{{ orderSummary.todayCompleted }}</div>
        <div class="earn-label">今日完成</div>
      </div>
      <div class="earn-item">
        <div class="earn-val">{{ orderSummary.avgScore }}</div>
        <div class="earn-label">平均评分</div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-card--primary">
        <div class="stat-card-icon"><i class="fas fa-bell"></i></div>
        <div class="stat-card-body">
          <div class="stat-card-value">{{ orderSummary.waiting }}</div>
          <div class="stat-card-label">待抢单</div>
        </div>
        <div class="stat-card-foot">
          超时 {{ orderSummary.timeoutWaiting }} 单
        </div>
      </div>
      <div class="stat-card stat-card--info">
        <div class="stat-card-icon"><i class="fas fa-motorcycle"></i></div>
        <div class="stat-card-body">
          <div class="stat-card-value">{{ orderSummary.delivering }}</div>
          <div class="stat-card-label">配送中</div>
        </div>
        <div class="stat-card-foot">
          预计再收入 ¥{{ orderSummary.deliveringIncome }}
        </div>
      </div>
      <div class="stat-card stat-card--success">
        <div class="stat-card-icon"><i class="fas fa-check-circle"></i></div>
        <div class="stat-card-body">
          <div class="stat-card-value">{{ orderSummary.todayCompleted }}</div>
          <div class="stat-card-label">今日完成</div>
        </div>
      </div>
      <div class="stat-card stat-card--warning">
        <div class="stat-card-icon"><i class="fas fa-exclamation-triangle"></i></div>
        <div class="stat-card-body">
          <div class="stat-card-value">{{ orderSummary.issueCount }}</div>
          <div class="stat-card-label">异常上报</div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <h3 class="section-title">快捷操作</h3>
      <div class="quick-actions">
        <div class="quick-action" @click="$router.push('/home/driver/available')">
          <i class="fas fa-list-ul"></i>
          <span>待接订单</span>
          <el-badge v-if="orderSummary.waiting > 0" :value="orderSummary.waiting" :max="99" />
        </div>
        <div class="quick-action" @click="$router.push('/home/driver/delivering')">
          <i class="fas fa-motorcycle"></i>
          <span>配送中</span>
          <el-badge v-if="orderSummary.delivering > 0" :value="orderSummary.delivering" :max="99" />
        </div>
        <div class="quick-action" @click="$router.push('/home/driver/earnings')">
          <i class="fas fa-coins"></i>
          <span>收益明细</span>
        </div>
        <div class="quick-action" @click="$router.push('/home/driver/history')">
          <i class="fas fa-history"></i>
          <span>历史订单</span>
        </div>
      </div>
    </div>

    <!-- 服务区域 -->
    <div class="service-area-section">
      <h3 class="section-title">服务区域</h3>
      <div class="service-area-form">
        <el-input
          v-model="serviceAreaInput"
          placeholder="输入服务区域关键词"
          clearable
          style="max-width: 300px"
          @keyup.enter="applyServiceArea"
        >
          <template #prefix><i class="fas fa-map-marker-alt"></i></template>
        </el-input>
        <el-button type="primary" @click="applyServiceArea">保存区域</el-button>
        <el-button v-if="driverServiceArea" @click="clearServiceArea">清空区域</el-button>
        <el-tag v-if="driverServiceArea" type="success" effect="plain" round style="margin-left:8px">
          {{ driverServiceArea }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue';
import { ElMessage } from 'element-plus';
import $store, { userInfo } from '@/store';
import fetch from '@/api/fetch';
import { Order as OrderApi } from '@/api/apis';
import { isDriverOwnedOrder, normalizeOrderItems } from '@/lib/orderDriverHelper';

const DELIVERY_RATE = 0.1;
const MIN_DELIVERY_FEE = 4;
const DISPATCH_TIMEOUT_MINUTES = 10;

const driverOrders = ref([]);
const serviceAreaInput = ref(userInfo.value.driverServiceArea || '');
let pollTimer = null;

const greeting = computed(() => {
  const h = new Date().getHours();
  if (h < 6) return '夜深了';
  if (h < 11) return '早上好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
});

const isDriverOnline = computed(() => userInfo.value.driverWorkStatus !== 'rest');
const driverServiceArea = computed(() => (userInfo.value.driverServiceArea || '').trim());
const driverIssueReports = computed(() => userInfo.value.driverIssueReports || {});

const parseOrderTime = (timeText) => {
  if (!timeText) return null;
  const normalized = String(timeText).replace(' ', 'T');
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
};

const isDispatchTimedOut = (item) => {
  if (item?.orderInfo?.state !== 3) return false;
  const date = parseOrderTime(item?.orderInfo?.time);
  if (!date) return false;
  return (Date.now() - date.getTime()) / 60000 >= DISPATCH_TIMEOUT_MINUTES;
};

const matchesDriverServiceArea = (item) => {
  if (!driverServiceArea.value) return true;
  if (isDriverOwnedOrder(item, userInfo.value.id)) return true;
  const keyword = driverServiceArea.value.toLowerCase();
  const haystack = [item?.delivery, item?.receive, item?.cusName, item?.merName]
    .filter(Boolean).join(' ').toLowerCase();
  return haystack.includes(keyword);
};

const calcDeliveryIncome = (item) => {
  const amount = Number(item?.orderInfo?.account || 0);
  return Math.max(MIN_DELIVERY_FEE, Math.round(amount * DELIVERY_RATE));
};

const getReviewScore = (item) => Number(item?.review?.score || 0);

const isSameDay = (timeText) => {
  if (!timeText) return false;
  const date = new Date(timeText);
  if (Number.isNaN(date.getTime())) return false;
  const now = new Date();
  return date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate();
};

const ownOrders = computed(() =>
  driverOrders.value.filter((item) => isDriverOwnedOrder(item, userInfo.value.id))
);

const completedOrders = computed(() =>
  ownOrders.value.filter((item) => item?.orderInfo?.state === 2)
);

const deliveringOrders = computed(() =>
  ownOrders.value.filter((item) => item?.orderInfo?.state === 1)
);

const avgReviewScore = computed(() => {
  const reviewed = completedOrders.value.filter((item) => getReviewScore(item) > 0);
  if (!reviewed.length) return '4.8';
  return (reviewed.reduce((sum, item) => sum + getReviewScore(item), 0) / reviewed.length).toFixed(1);
});

const orderSummary = computed(() => ({
  waiting: driverOrders.value.filter(
    (item) => item?.orderInfo?.state === 3 && matchesDriverServiceArea(item)
  ).length,
  timeoutWaiting: driverOrders.value.filter(
    (item) => isDispatchTimedOut(item) && matchesDriverServiceArea(item)
  ).length,
  delivering: deliveringOrders.value.length,
  todayCompleted: completedOrders.value.filter((item) => isSameDay(item?.orderInfo?.time)).length,
  completedIncome: completedOrders.value.reduce((sum, item) => sum + calcDeliveryIncome(item), 0).toFixed(0),
  deliveringIncome: deliveringOrders.value.reduce((sum, item) => sum + calcDeliveryIncome(item), 0).toFixed(0),
  avgScore: avgReviewScore.value,
  issueCount: Object.keys(driverIssueReports.value).length,
}));

const refreshDashboard = () => {
  if (!userInfo.value?.id) return;
  fetch(OrderApi.getOrderList, { usrId: userInfo.value.id, timeOrder: 1 }).then((data) => {
    driverOrders.value = normalizeOrderItems(data?.driverList || []);
  });
};

const toggleDriverWorkStatus = () => {
  const next = isDriverOnline.value ? 'rest' : 'online';
  $store.commit('setDriverWorkStatus', next);
  ElMessage.success(next === 'online' ? '已切换为在线接单' : '已切换为休息中');
};

const applyServiceArea = () => {
  const value = serviceAreaInput.value.trim();
  $store.commit('setDriverServiceArea', value);
  ElMessage.success(value ? '服务区域已更新' : '已切换为全城接单');
};

const clearServiceArea = () => {
  serviceAreaInput.value = '';
  $store.commit('setDriverServiceArea', '');
  ElMessage.success('已清空服务区域限制');
};

onMounted(() => {
  refreshDashboard();
  pollTimer = setInterval(refreshDashboard, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer);
});
</script>

<style lang="less" scoped>
.driver-overview {
  padding: 0 0 24px;
}

// ============ Hero ============
.overview-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 4px 20px;
  gap: 24px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--primary, #059669);
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  font-weight: 600;
}

.overview-hero h2 {
  margin: 0;
  font-size: 30px;
  font-family: "Georgia", "Times New Roman", serif;
  color: var(--text-primary, #111827);
}

.hero-desc {
  max-width: 480px;
  margin: 10px 0 0;
  color: var(--text-secondary, #4B5563);
  line-height: 1.8;
  font-size: 14px;
}

.hero-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
}

.status-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-label {
  font-size: 13px;
  color: var(--text-secondary, #4B5563);
  font-weight: 500;
}

// ============ 收入快捷条 ============
.earnings-strip {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  background: var(--card, #fff);
  border-radius: 16px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 18px 0;
  margin-bottom: 24px;
}

.earn-item {
  text-align: center;
  border-right: 1px solid var(--border, #F0F0F0);
  &:last-child { border-right: none; }
}

.earn-val {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary, #111827);

  &--green { color: var(--primary, #059669); }
}

.earn-label {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  margin-top: 4px;
}

// ============ 统计卡片 ============
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.stat-card {
  background: var(--card, #fff);
  border: 1px solid var(--border, #F0F0F0);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0; right: 0;
    width: 80px; height: 80px;
    border-radius: 50%;
    opacity: 0.08;
    transform: translate(30%, -30%);
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  }

  &--primary::before { background: var(--primary, #059669); }
  &--info::before { background: var(--info, #6366F1); }
  &--success::before { background: var(--success, #16A34A); }
  &--warning::before { background: var(--warning, #EAB308); }
}

.stat-card-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;

  .stat-card--primary & { background: var(--primary-light, #ECFDF5); color: var(--primary, #059669); }
  .stat-card--info & { background: var(--info-light, #EEF2FF); color: var(--info, #6366F1); }
  .stat-card--success & { background: var(--success-light, #F0FDF4); color: var(--success, #16A34A); }
  .stat-card--warning & { background: var(--warning-light, #FEFCE8); color: var(--warning, #EAB308); }
}

.stat-card-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary, #111827);
  font-family: "Georgia", "Times New Roman", serif;
}

.stat-card-label {
  font-size: 13px;
  color: var(--text-secondary, #4B5563);
}

.stat-card-foot {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  border-top: 1px dashed var(--border, #F0F0F0);
  padding-top: 8px;
}

// ============ 快捷操作 ============
.section-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary, #111827);
  margin-bottom: 14px;
}

.quick-actions-section {
  margin-bottom: 28px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 14px;
  border-radius: 14px;
  border: 1px solid var(--border, #F0F0F0);
  background: var(--card, #fff);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  i { font-size: 22px; color: var(--primary, #059669); }
  span { font-size: 13px; font-weight: 600; color: var(--text-primary, #111827); }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  }
}

// ============ 服务区域 ============
.service-area-section {
  margin-bottom: 24px;
}

.service-area-form {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

// ============ 响应式 ============
@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .overview-hero {
    flex-direction: column;
  }
  .hero-status {
    align-items: stretch;
  }
  .stats-grid,
  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
