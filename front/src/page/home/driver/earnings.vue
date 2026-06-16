<template>
  <div class="driver-earnings">
    <!-- 页头 -->
    <div class="earnings-hero">
      <div class="earnings-label">累计配送收入</div>
      <div class="earnings-amount">¥{{ totalEarnings.toFixed(0) }}</div>
      <div class="earnings-sub">
        配送中预计再收入 ¥{{ deliveringIncome.toFixed(0) }}
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="earnings-stats">
      <div class="earn-stat-card">
        <div class="earn-stat-val earn-stat-val--green">{{ completedCount }}</div>
        <div class="earn-stat-label">完成订单</div>
      </div>
      <div class="earn-stat-card">
        <div class="earn-stat-val">¥{{ avgPerOrder.toFixed(1) }}</div>
        <div class="earn-stat-label">单均收入</div>
      </div>
      <div class="earn-stat-card">
        <div class="earn-stat-val earn-stat-val--green">{{ avgScore }}</div>
        <div class="earn-stat-label">服务评分</div>
      </div>
      <div class="earn-stat-card">
        <div class="earn-stat-val">{{ performanceLevel }}</div>
        <div class="earn-stat-label">骑手等级</div>
      </div>
    </div>

    <!-- 收益图表 -->
    <div class="earn-chart-section">
      <h3 class="section-title">近 7 日收入</h3>
      <div class="chart-bars" v-if="chartData.length">
        <div
          v-for="(item, idx) in chartData"
          :key="idx"
          class="chart-bar"
          :style="{
            height: (item.value / chartMax * 100) + '%',
            background: idx === chartData.length - 1
              ? 'var(--primary, #059669)'
              : 'rgba(5, 150, 105, 0.55)'
          }"
          :title="`${item.label}: ¥${item.value}`"
        ></div>
      </div>
      <div class="chart-labels" v-if="chartData.length">
        <span v-for="(item, idx) in chartData" :key="idx">{{ item.label }}</span>
      </div>
    </div>

    <!-- 绩效信息 -->
    <div class="performance-section">
      <h3 class="section-title">绩效与激励</h3>
      <div class="performance-card">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="当前等级">{{ performanceLevel }}</el-descriptions-item>
          <el-descriptions-item label="等级说明">{{ performanceDesc }}</el-descriptions-item>
          <el-descriptions-item label="激励规则">{{ performanceReward }}</el-descriptions-item>
          <el-descriptions-item label="预估奖励">¥{{ estimatedBonus }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <!-- 收入明细 -->
    <div class="detail-section">
      <h3 class="section-title">收入明细</h3>
      <div class="detail-list" v-if="incomeDetails.length">
        <div class="detail-row" v-for="item in incomeDetails" :key="item.orderId">
          <div class="detail-info">
            <span class="detail-shop">{{ item.shop }}</span>
            <span class="detail-time">{{ item.time }}</span>
          </div>
          <span class="detail-fee">+¥{{ item.fee }}</span>
        </div>
      </div>
      <div class="empty-tip" v-else>
        <i class="fas fa-coins"></i>
        <span>暂无收入记录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { userInfo } from '@/store';
import { Order as OrderApi } from '@/api/apis';
import fetch from '@/api/fetch';
import { isDriverOwnedOrder, normalizeOrderItems } from '@/lib/orderDriverHelper';

const DELIVERY_RATE = 0.1;
const MIN_DELIVERY_FEE = 4;
const driverOrders = ref([]);

const ownOrders = computed(() =>
  driverOrders.value.filter((item) => isDriverOwnedOrder(item, userInfo.value.id))
);

const completedOrders = computed(() =>
  ownOrders.value.filter((item) => item?.orderInfo?.state === 2)
);

const deliveringOrders = computed(() =>
  ownOrders.value.filter((item) => item?.orderInfo?.state === 1)
);

const calcDeliveryFee = (item) => {
  const amount = Number(item?.orderInfo?.account || 0);
  return Math.max(MIN_DELIVERY_FEE, Math.round(amount * DELIVERY_RATE));
};

const totalEarnings = computed(() =>
  completedOrders.value.reduce((sum, item) => sum + calcDeliveryFee(item), 0)
);

const deliveringIncome = computed(() =>
  deliveringOrders.value.reduce((sum, item) => sum + calcDeliveryFee(item), 0)
);

const completedCount = computed(() => completedOrders.value.length);

const avgPerOrder = computed(() =>
  completedCount.value > 0 ? totalEarnings.value / completedCount.value : 0
);

const avgScore = computed(() => {
  const scored = completedOrders.value.filter((item) => Number(item?.review?.score || 0) > 0);
  if (!scored.length) return '4.8';
  return (scored.reduce((sum, item) => sum + Number(item.review.score), 0) / scored.length).toFixed(1);
});

// 绩效等级
const performanceLevel = computed(() => {
  const count = completedCount.value;
  const score = Number(avgScore.value);
  if (count >= 12 || score >= 4.8) return '金牌骑手';
  if (count >= 5 || score >= 4.5) return '稳定骑手';
  return '新手骑手';
});

const performanceDesc = computed(() => {
  const map = {
    '金牌骑手': '完成单量和评分较高，属于高绩效骑手。',
    '稳定骑手': '具备稳定接单与配送能力，属于配送骨干。',
    '新手骑手': '处于起步阶段，继续完成订单可提升等级。',
  };
  return map[performanceLevel.value] || '';
});

const performanceReward = computed(() => {
  const map = {
    '金牌骑手': '每个已完成订单额外奖励 2 元',
    '稳定骑手': '每个已完成订单额外奖励 1 元',
    '新手骑手': '完成 5 单后进入稳定骑手档',
  };
  return map[performanceLevel.value] || '';
});

const estimatedBonus = computed(() => {
  const map = { '金牌骑手': 2, '稳定骑手': 1, '新手骑手': 0 };
  return (completedCount.value * (map[performanceLevel.value] || 0)).toFixed(0);
});

// 图表数据
const chartData = computed(() => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return days.map((label) => ({
    label,
    value: Math.floor(Math.random() * 200 + 100), // Mock 数据
  }));
});

const chartMax = computed(() => Math.max(1, ...chartData.value.map((d) => d.value)));

// 收入明细
const incomeDetails = computed(() =>
  [...ownOrders.value]
    .sort((a, b) =>
      new Date(b?.orderInfo?.time || 0).getTime() -
      new Date(a?.orderInfo?.time || 0).getTime()
    )
    .slice(0, 20)
    .map((item) => ({
      orderId: item?.orderInfo?.id,
      shop: `${item?.merName || '商家'} → ${item?.cusName || '顾客'}`,
      time: item?.orderInfo?.time || '-',
      fee: calcDeliveryFee(item),
    }))
);

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
.driver-earnings {
  padding: 0 0 24px;
}

// ============ 收入头部 ============
.earnings-hero {
  border-radius: 18px;
  overflow: hidden;
  padding: 28px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, var(--primary, #059669), #0D9488);
  color: white;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    right: -20px;
    bottom: -20px;
    width: 140px;
    height: 140px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
  }
}

.earnings-label {
  font-size: 13px;
  opacity: 0.9;
}

.earnings-amount {
  font-size: 36px;
  font-weight: 800;
  font-family: "Georgia", "Times New Roman", serif;
  margin: 8px 0;
}

.earnings-sub {
  font-size: 12px;
  opacity: 0.8;
}

// ============ 统计卡片 ============
.earnings-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}

.earn-stat-card {
  background: var(--card, #fff);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.earn-stat-val {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary, #111827);

  &--green { color: var(--primary, #059669); }
}

.earn-stat-label {
  font-size: 11px;
  color: var(--text-muted, #9CA3AF);
  margin-top: 4px;
}

// ============ 图表 ============
.section-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary, #111827);
  margin-bottom: 14px;
}

.earn-chart-section {
  margin-bottom: 28px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  height: 100px;
  padding: 8px 0;
}

.chart-bar {
  flex: 1;
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  cursor: pointer;
  transition: filter 0.2s;

  &:hover { filter: brightness(1.15); }
}

.chart-labels {
  display: flex;
  gap: 6px;

  span {
    flex: 1;
    text-align: center;
    font-size: 10px;
    color: var(--text-muted, #9CA3AF);
  }
}

// ============ 绩效 ============
.performance-section {
  margin-bottom: 28px;
}

.performance-card {
  border-radius: 14px;
  overflow: hidden;
}

// ============ 明细 ============
.detail-section {
  margin-bottom: 24px;
}

.detail-list {
  display: flex;
  flex-direction: column;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border, #F0F0F0);
  font-size: 13px;

  &:last-child { border-bottom: none; }
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-shop {
  font-weight: 600;
  color: var(--text-primary, #111827);
}

.detail-time {
  font-size: 11px;
  color: var(--text-muted, #9CA3AF);
}

.detail-fee {
  font-weight: 700;
  color: var(--primary, #059669);
}

// ============ 空态 ============
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;

  i { font-size: 36px; opacity: 0.3; }
}

@media (max-width: 768px) {
  .earnings-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
