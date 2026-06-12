<template>
  <div class="merchant-ops">
    <section class="hero">
      <div>
        <p class="eyebrow">Merchant Analytics</p>
        <h3>课堂展示版门店经营分析台</h3>
        <p class="hero-desc">
          聚合门店 GMV、订单趋势、热销商品、售后结构和操作日志，方便课堂上展示“经营看板 + 导出报表 + 操作留痕”的商家侧闭环。
        </p>
      </div>
      <div class="hero-actions">
        <el-button class="light-btn" @click="refreshAll">刷新经营数据</el-button>
        <el-button type="primary" @click="exportReport">导出经营报表</el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="card in summaryCards" :key="card.label" class="summary-card">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.tip }}</small>
      </article>
    </section>

    <section class="dual-grid">
      <article class="panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Sales Trend</p>
            <h4>近 7 天门店经营走势</h4>
          </div>
        </div>
        <div class="trend-list">
          <div v-for="item in dailyTrend" :key="item.date" class="trend-row">
            <div class="trend-title">
              <strong>{{ item.date }}</strong>
              <span>{{ item.orders }} 单</span>
            </div>
            <div class="progress-track">
              <div
                class="progress-fill"
                :style="{ width: `${resolveWidth(item.gmv, maxGmv)}%` }"
              />
            </div>
            <div class="trend-value">¥{{ item.gmv }}</div>
          </div>
        </div>
      </article>

      <article class="panel dark-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">After-Sale Mix</p>
            <h4>售后与操作状态</h4>
          </div>
        </div>
        <div class="mix-grid">
          <div class="mix-card">
            <span>待处理售后</span>
            <strong>{{ ticketBreakdown.pending || 0 }}</strong>
          </div>
          <div class="mix-card">
            <span>处理中</span>
            <strong>{{ ticketBreakdown.processing || 0 }}</strong>
          </div>
          <div class="mix-card">
            <span>退款类</span>
            <strong>{{ ticketBreakdown.refund || 0 }}</strong>
          </div>
          <div class="mix-card">
            <span>投诉类</span>
            <strong>{{ ticketBreakdown.complaint || 0 }}</strong>
          </div>
        </div>
        <div class="audit-preview">
          <h5>最近操作</h5>
          <div v-for="item in recentAudits" :key="item.id" class="audit-line">
            <span>{{ item.actionType }}</span>
            <strong>{{ item.targetName || item.targetId }}</strong>
            <small>{{ item.createdTime }}</small>
          </div>
        </div>
      </article>
    </section>

    <section class="dual-grid">
      <article class="panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Top Products</p>
            <h4>热销商品排行</h4>
          </div>
        </div>
        <div class="product-rank">
          <div v-for="(item, index) in topProducts" :key="item.productId" class="rank-item">
            <div class="rank-no">{{ index + 1 }}</div>
            <div class="rank-main">
              <strong>{{ item.name }}</strong>
              <span>销量 {{ item.quantity }} · 营收 ¥{{ item.revenue }}</span>
            </div>
            <el-tag size="small" :type="item.state === 1 ? 'success' : 'warning'">
              {{ item.state === 1 ? "在售" : "待审核" }}
            </el-tag>
          </div>
        </div>
      </article>

      <article class="panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Recent Orders</p>
            <h4>近期订单样本</h4>
          </div>
        </div>
        <el-table :data="recentOrders" size="small" class="clean-table">
          <el-table-column prop="orderId" label="订单号" min-width="160" />
          <el-table-column prop="stateLabel" label="状态" width="110" />
          <el-table-column prop="amount" label="金额" width="90" />
          <el-table-column prop="time" label="时间" min-width="180" />
        </el-table>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import fetch from "@/api/fetch";
import { Ops } from "@/api/apis";
import $store, { refreshDataFnMap } from "@/store";

const dashboard = ref({});

const summaryCards = computed(() => {
  const overview = dashboard.value?.overview || {};
  return [
    {
      label: "门店 GMV",
      value: `¥${overview.gmv || 0}`,
      tip: "按有效支付订单汇总",
    },
    {
      label: "有效订单",
      value: overview.paidOrders || 0,
      tip: "课堂展示版已支付至完成的订单数",
    },
    {
      label: "平均客单价",
      value: `¥${overview.avgOrderAmount || 0}`,
      tip: "便于讲解商家经营质量",
    },
    {
      label: "待处理售后",
      value: overview.pendingAfterSale || 0,
      tip: "售后工单与经营数据联动展示",
    },
    {
      label: "退款订单",
      value: overview.refundOrders || 0,
      tip: "用于课堂说明异常经营场景",
    },
    {
      label: "在售商品",
      value: overview.approvedProducts || 0,
      tip: "当前审核通过商品数量",
    },
  ];
});

const dailyTrend = computed(() => dashboard.value?.dailyTrend || []);
const topProducts = computed(() => dashboard.value?.topProducts || []);
const recentOrders = computed(() => dashboard.value?.recentOrders || []);
const recentAudits = computed(() => dashboard.value?.recentAudits || []);
const ticketBreakdown = computed(() => dashboard.value?.ticketBreakdown || {});
const maxGmv = computed(() =>
  Math.max(...dailyTrend.value.map((item) => Number(item.gmv || 0)), 1)
);

const refreshAll = () => {
  fetch(Ops.merchantDashboard).then((data) => {
    dashboard.value = data || {};
  });
};

const resolveWidth = (value, max) => {
  if (!max) return 16;
  return Math.max(12, Math.round((Number(value || 0) / max) * 100));
};

const exportReport = () => {
  fetch(Ops.merchantExport)
    .then((data) => {
      const blob = new Blob([data?.content || ""], {
        type: "text/csv;charset=utf-8;",
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = data?.fileName || "merchant-operation-report.csv";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      ElMessage.success("经营报表已导出");
    })
    .catch(() => {});
};

onMounted(() => {
  refreshAll();
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Ops",
    fn: refreshAll,
  });
  window.addEventListener("ops-permission-updated", refreshAll);
});

onBeforeUnmount(() => {
  if (refreshDataFnMap.value?.Ops) {
    $store.commit("updataRefreshDataFnMap", {
      tabLabel: "Ops",
      fn: null,
    });
  }
  window.removeEventListener("ops-permission-updated", refreshAll);
});
</script>

<style lang="less" scoped>
.merchant-ops {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero,
.summary-card,
.panel,
.mix-card,
.rank-item {
  border: 1px solid rgba(64, 39, 18, 0.08);
  box-shadow: 0 18px 48px rgba(114, 73, 33, 0.08);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(255, 209, 162, 0.32), transparent 28%),
    linear-gradient(135deg, #23150d 0%, #5e3920 46%, #fff4eb 46%, #fffaf6 100%);
}

.eyebrow,
.panel-kicker {
  margin: 0 0 10px;
  font-size: 11px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

.hero h3,
.panel h4 {
  margin: 0;
  font-family: "Georgia", "Times New Roman", serif;
}

.hero h3 {
  font-size: 34px;
  color: #fffdf9;
}

.hero-desc {
  max-width: 760px;
  margin: 12px 0 0;
  color: rgba(255, 248, 240, 0.82);
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
}

.light-btn {
  border: none;
  background: rgba(255, 255, 255, 0.88);
  color: #4c2d17;
}

.summary-grid,
.dual-grid,
.mix-grid {
  display: grid;
  gap: 16px;
}

.summary-grid {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.dual-grid {
  grid-template-columns: 1.1fr 0.9fr;
}

.summary-card {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, #ffffff 0%, #fff6ee 100%);
}

.summary-card span,
.summary-card small,
.mix-card span,
.rank-main span {
  display: block;
  color: rgba(52, 31, 16, 0.6);
}

.summary-card strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
  color: #2e1a0e;
}

.summary-card small {
  margin-top: 8px;
  line-height: 1.6;
}

.panel {
  padding: 22px;
  border-radius: 28px;
  background: linear-gradient(180deg, #ffffff 0%, #fffaf6 100%);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.panel-kicker {
  color: #b76e2b;
}

.panel h4 {
  font-size: 24px;
  color: #2e1a0e;
}

.trend-list,
.product-rank,
.audit-preview {
  display: grid;
  gap: 14px;
  margin-top: 16px;
}

.trend-row {
  display: grid;
  grid-template-columns: 140px 1fr 90px;
  align-items: center;
  gap: 14px;
}

.trend-title strong,
.rank-main strong,
.audit-preview h5 {
  color: #2e1a0e;
}

.trend-title span,
.audit-line small {
  display: block;
  margin-top: 4px;
  color: rgba(46, 26, 14, 0.58);
}

.progress-track {
  height: 12px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(183, 110, 43, 0.12);
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #b76e2b 0%, #ffd4ac 100%);
}

.trend-value {
  text-align: right;
  color: #7a461d;
  font-weight: 600;
}

.dark-panel {
  color: #fff7ef;
  background:
    radial-gradient(circle at top right, rgba(255, 212, 172, 0.18), transparent 28%),
    linear-gradient(180deg, #2b180d 0%, #402617 100%);
}

.dark-panel .panel-kicker,
.dark-panel h4,
.mix-card strong,
.audit-line span,
.audit-line strong {
  color: #fff7ef;
}

.mix-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 16px;
}

.mix-card {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
}

.mix-card strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
}

.audit-preview h5 {
  margin: 4px 0 0;
}

.audit-line {
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
}

.audit-line strong,
.audit-line small {
  display: block;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 20px;
  background: rgba(183, 110, 43, 0.06);
}

.rank-no {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  color: #fffaf6;
  font-weight: 700;
  background: linear-gradient(135deg, #b76e2b 0%, #6c3917 100%);
}

.rank-main {
  flex: 1;
}

.clean-table {
  margin-top: 16px;
}

@media (max-width: 1400px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .hero,
  .dual-grid {
    display: grid;
    grid-template-columns: 1fr;
  }

  .hero-actions {
    align-items: flex-start;
  }
}

@media (max-width: 900px) {
  .summary-grid,
  .mix-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .trend-row {
    grid-template-columns: 1fr;
  }
}
</style>
