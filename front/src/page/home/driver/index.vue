<template>
  <!-- driver 身份 -->
  <div class="driver">
    <div class="status-bar">
      <div class="status-text">
        <span>接单状态</span>
        <el-tag :type="isDriverOnline ? 'success' : 'info'">
          {{ isDriverOnline ? "在线接单" : "休息中" }}
        </el-tag>
        <el-tag v-if="isDriverBusy" type="warning">忙碌配送中</el-tag>
        <el-tag v-if="driverServiceArea" type="primary">
          服务区域: {{ driverServiceArea }}
        </el-tag>
      </div>
      <div class="status-actions">
        <el-input
          v-model="serviceAreaInput"
          placeholder="输入服务区域关键词"
          clearable
          style="width: 220px"
          @keyup.enter="applyServiceArea"
        />
        <el-button @click="applyServiceArea">保存区域</el-button>
        <el-button v-if="driverServiceArea" @click="clearServiceArea">
          清空区域
        </el-button>
        <el-button
          :type="isDriverOnline ? 'warning' : 'primary'"
          @click="toggleDriverWorkStatus"
        >
          {{ isDriverOnline ? "切换为休息" : "切换为在线" }}
        </el-button>
      </div>
    </div>

    <div class="dashboard">
      <div class="summary-card">
        <div class="summary-label">待抢单</div>
        <div class="summary-value">{{ orderSummary.waiting }}</div>
        <div class="summary-desc">
          当前待骑手接单的订单，超时 {{ orderSummary.timeoutWaiting }} 单
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-label">配送中</div>
        <div class="summary-value">{{ orderSummary.delivering }}</div>
        <div class="summary-desc">属于当前骑手的进行中配送</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">今日完成</div>
        <div class="summary-value">{{ orderSummary.todayCompleted }}</div>
        <div class="summary-desc">按订单更新时间统计今日完成单</div>
      </div>
      <div class="summary-card income-card">
        <div class="summary-label">累计配送收入</div>
        <div class="summary-value">￥{{ orderSummary.completedIncome }}</div>
        <div class="summary-desc">
          配送中预计再收入 ￥{{ orderSummary.deliveringIncome }}
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-label">展示评分</div>
        <div class="summary-value">{{ orderSummary.avgScore }}</div>
        <div class="summary-desc">基于已完成订单评价的课堂展示版评分</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">异常上报</div>
        <div class="summary-value">{{ orderSummary.issueCount }}</div>
        <div class="summary-desc">配送异常、联系不上顾客等本地记录</div>
      </div>
    </div>

    <div class="dashboard-actions">
      <el-button type="primary" @click="incomeVisible = true">收入明细</el-button>
      <el-button @click="performanceVisible = true">绩效与激励</el-button>
    </div>

    <div class="dashboard-tip">
      课堂展示版收入规则：每单配送收入按 `max(4, 订单金额 × 10%)` 估算；超时 10 分钟的待接单会进入优先重派展示队列
    </div>

    <el-tabs v-model="activeName" @tab-click="handleClick" class="customer-tab">
      <el-tab-pane :label="t('common.order')" name="first"><Order /></el-tab-pane>
      <el-tab-pane :label="t('common.info')" name="second"><Info /></el-tab-pane>
    </el-tabs>

    <el-drawer v-model="incomeVisible" title="骑手收入明细" size="48%">
      <div class="drawer-tip">
        已完成订单展示结算收入，配送中订单展示预计收入，供课堂讲解使用。
      </div>
      <el-table :data="incomeDetails" style="width: 100%">
        <el-table-column prop="orderId" label="订单号" min-width="120" />
        <el-table-column prop="statusText" label="状态" min-width="100" />
        <el-table-column prop="finishTime" label="时间" min-width="170" />
        <el-table-column prop="account" label="订单金额" min-width="100" />
        <el-table-column prop="deliveryFee" label="配送费" min-width="90" />
        <el-table-column prop="bonus" label="奖励" min-width="80" />
        <el-table-column prop="scoreText" label="评分" min-width="80" />
        <el-table-column prop="reportStatus" label="异常状态" min-width="120" />
      </el-table>
    </el-drawer>

    <el-dialog v-model="performanceVisible" title="绩效与激励" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="当前等级">
          {{ performanceSummary.level }}
        </el-descriptions-item>
        <el-descriptions-item label="等级说明">
          {{ performanceSummary.levelDesc }}
        </el-descriptions-item>
        <el-descriptions-item label="课堂展示版激励">
          {{ performanceSummary.rewardRule }}
        </el-descriptions-item>
        <el-descriptions-item label="本期预估奖励">
          ￥{{ performanceSummary.estimatedBonus }}
        </el-descriptions-item>
        <el-descriptions-item label="异常说明">
          已上报异常 {{ orderSummary.issueCount }} 单，演示时可说明系统已保留配送异常记录。
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import Order from "@/components/order/index.vue";
import Info from "@/components/info/index.vue";
import $store, { refreshDataFnMap } from "@/store";
import { userInfo } from "@/store";
import fetch from "@/api/fetch";
import { Order as OrderApi } from "@/api/apis";

const { t } = useI18n();
const activeName = ref("first");
const driverOrders = ref([]);
const incomeVisible = ref(false);
const performanceVisible = ref(false);
let dashboardTimer = null;

const DELIVERY_RATE = 0.1;
const MIN_DELIVERY_FEE = 4;
const DISPATCH_TIMEOUT_MINUTES = 10;
const isDriverOnline = computed(
  () => userInfo.value.driverWorkStatus !== "rest"
);
const isDriverBusy = computed(() => orderSummary.value.delivering > 0);
const driverServiceArea = computed(
  () => (userInfo.value.driverServiceArea || "").trim()
);
const serviceAreaInput = ref(userInfo.value.driverServiceArea || "");
const driverIssueReports = computed(() => userInfo.value.driverIssueReports || {});

const parseOrderTime = (timeText) => {
  if (!timeText) return null;
  const normalized = String(timeText).replace(" ", "T");
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
  if (item?.orderInfo?.driverId === userInfo.value.id) return true;
  const keyword = driverServiceArea.value.toLowerCase();
  const haystack = [item?.delivery, item?.receive, item?.cusName, item?.merName]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
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
  return (
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  );
};

const ownOrders = computed(() =>
  driverOrders.value.filter((item) => item?.orderInfo?.driverId === userInfo.value.id)
);

const completedOrders = computed(() =>
  ownOrders.value.filter((item) => item?.orderInfo?.state === 2)
);

const deliveringOrders = computed(() =>
  ownOrders.value.filter((item) => item?.orderInfo?.state === 1)
);

const avgReviewScore = computed(() => {
  const reviewedOrders = completedOrders.value.filter((item) => getReviewScore(item) > 0);
  if (!reviewedOrders.length) return "4.8";
  return (
    reviewedOrders.reduce((sum, item) => sum + getReviewScore(item), 0) /
    reviewedOrders.length
  ).toFixed(1);
});

const performanceSummary = computed(() => {
  const completedCount = completedOrders.value.length;
  const avgScore = Number(avgReviewScore.value);
  if (completedCount >= 12 || avgScore >= 4.8) {
    return {
      level: "金牌骑手",
      levelDesc: "完成单量和展示评分较高，适合课堂展示高绩效骑手。",
      rewardRule: "每个已完成订单额外奖励 2 元。",
      estimatedBonus: (completedCount * 2).toFixed(0),
    };
  }
  if (completedCount >= 5 || avgScore >= 4.5) {
    return {
      level: "稳定骑手",
      levelDesc: "具备稳定接单与配送能力，课堂展示中可解释为骨干运力。",
      rewardRule: "每个已完成订单额外奖励 1 元。",
      estimatedBonus: completedCount.toFixed(0),
    };
  }
  return {
    level: "新手骑手",
    levelDesc: "刚完成基础配送闭环，适合演示新人培养阶段。",
    rewardRule: "完成 5 单后进入稳定骑手档。",
    estimatedBonus: "0",
  };
});

const incomeDetails = computed(() =>
  [...ownOrders.value]
    .sort(
      (a, b) =>
        new Date(b?.orderInfo?.time || 0).getTime() -
        new Date(a?.orderInfo?.time || 0).getTime()
    )
    .map((item) => {
      const fee = calcDeliveryIncome(item);
      const report = driverIssueReports.value?.[item?.orderInfo?.id];
      const bonusUnit =
        performanceSummary.value.level === "金牌骑手"
          ? 2
          : performanceSummary.value.level === "稳定骑手"
            ? 1
            : 0;
      const score = getReviewScore(item);
      return {
        orderId: item?.orderInfo?.id,
        statusText: item?.orderInfo?.state === 2 ? "已完成" : "配送中",
        finishTime: item?.orderInfo?.time || "-",
        account: `￥${Number(item?.orderInfo?.account || 0).toFixed(0)}`,
        deliveryFee: `￥${fee}`,
        bonus: `￥${item?.orderInfo?.state === 2 ? bonusUnit : 0}`,
        scoreText: score ? `${score}/5` : "待评价",
        reportStatus: report ? `${report.type} / ${report.status}` : "正常",
      };
    })
);

const orderSummary = computed(() => {
  return {
    waiting: driverOrders.value.filter(
      (item) =>
        item?.orderInfo?.state === 3 && matchesDriverServiceArea(item)
    ).length,
    timeoutWaiting: driverOrders.value.filter((item) => isDispatchTimedOut(item))
      .filter((item) => matchesDriverServiceArea(item)).length,
    delivering: deliveringOrders.value.length,
    todayCompleted: completedOrders.value.filter((item) =>
      isSameDay(item?.orderInfo?.time)
    ).length,
    completedIncome: completedOrders.value
      .reduce((sum, item) => sum + calcDeliveryIncome(item), 0)
      .toFixed(0),
    deliveringIncome: deliveringOrders.value
      .reduce((sum, item) => sum + calcDeliveryIncome(item), 0)
      .toFixed(0),
    avgScore: avgReviewScore.value,
    issueCount: Object.keys(driverIssueReports.value).length,
  };
});

const refreshDashboard = () => {
  if (!userInfo.value?.id) return;
  fetch(OrderApi.getOrderList, {
    usrId: userInfo.value.id,
    timeOrder: 1,
  }).then((data) => {
    driverOrders.value = data?.driverList || [];
  });
};

const toggleDriverWorkStatus = () => {
  const nextStatus = isDriverOnline.value ? "rest" : "online";
  $store.commit("setDriverWorkStatus", nextStatus);
  ElMessage.success(
    nextStatus === "online" ? "已切换为在线接单" : "已切换为休息中"
  );
};

const applyServiceArea = () => {
  const value = serviceAreaInput.value.trim();
  $store.commit("setDriverServiceArea", value);
  ElMessage.success(value ? "服务区域已更新" : "已切换为全城接单");
};

const clearServiceArea = () => {
  serviceAreaInput.value = "";
  $store.commit("setDriverServiceArea", "");
  ElMessage.success("已清空服务区域限制");
};

const tabKeyMap = {
  [t('common.order')]: 'Order',
  [t('common.info')]: 'Info'
};

const handleClick = (tab, event) => {
  const key = tabKeyMap[tab.props.label] || tab.props.label;
  refreshDataFnMap.value?.[key]?.();
  refreshDashboard();
};

onMounted(() => {
  refreshDashboard();
  dashboardTimer = window.setInterval(refreshDashboard, 15000);
});

onUnmounted(() => {
  if (dashboardTimer) {
    window.clearInterval(dashboardTimer);
    dashboardTimer = null;
  }
});
</script>

<style lang="less" scoped>
.driver {
  margin: 20px;
  padding: 10px 20px;

  border-radius: 10px;
  background-color: #fff;

  &-tab {
    padding: 16px;
  }
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
}

.status-text {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #303133;
  font-size: 14px;
}

.status-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dashboard {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card {
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border: 1px solid #ebeef5;
}

.summary-label {
  color: #909399;
  font-size: 14px;
}

.summary-value {
  margin: 10px 0 8px;
  color: #303133;
  font-size: 28px;
  font-weight: 700;
}

.summary-desc {
  color: #606266;
  font-size: 13px;
}

.income-card {
  background: linear-gradient(135deg, #ecf5ff 0%, #ffffff 100%);
}

.dashboard-tip {
  margin-bottom: 16px;
  color: #606266;
  font-size: 13px;
}

.dashboard-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.drawer-tip {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}
</style>
