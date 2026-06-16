<!--
  管理端 · 钱包流水面板
  查询平台所有用户的充值/支付/退款流水，统计平台总余额
-->
<template>
  <div class="admin-wallet">
    <!-- 平台汇总卡片 -->
    <section class="overview">
      <article
        v-for="card in overviewCards"
        :key="card.label"
        class="overview-card"
        :class="`overview-card--${card.tone}`"
      >
        <span class="overview-label">{{ card.label }}</span>
        <strong class="overview-value">{{ card.value }}</strong>
        <small class="overview-tip">{{ card.tip }}</small>
      </article>
    </section>

    <!-- 类型分布柱状图 -->
    <section class="type-bars card">
      <div class="section-heading">
        <div>
          <span class="micro-tag">类型分布</span>
          <h3>近 30 天流水类型</h3>
          <p>按交易类型汇总金额与笔数，快速识别平台资金流向。</p>
        </div>
      </div>
      <div class="bar-rows">
        <div
          v-for="row in typeDistribution"
          :key="row.type"
          class="bar-row"
        >
          <span class="bar-type">{{ row.typeName }}</span>
          <div class="bar-track">
            <div
              class="bar-fill"
              :class="`bar-fill--${row.tone}`"
              :style="{ width: `${row.percent}%` }"
            />
          </div>
          <span class="bar-meta">
            <strong>&yen;{{ row.amount.toLocaleString() }}</strong>
            <small>{{ row.count }} 笔</small>
          </span>
        </div>
      </div>
    </section>

    <!-- 筛选/搜索 -->
    <section class="filter-bar card">
      <div class="search-wrap">
        <svg class="search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
        <input
          v-model="keyword"
          placeholder="搜索用户昵称、订单号、流水号"
          class="search-input"
          @change="fetchWallet"
        />
      </div>
      <select v-model="typeFilter" class="form-select" @change="fetchWallet">
        <option value="all">全部类型</option>
        <option value="RECHARGE">充值</option>
        <option value="PAY">支付</option>
        <option value="REFUND">退款</option>
      </select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="date-picker"
        @change="fetchWallet"
      />
      <button class="btn btn-outline" @click="exportData">导出 CSV</button>
      <button class="btn btn-outline" @click="fetchWallet">刷新</button>
    </section>

    <!-- 流水列表 -->
    <section class="wallet-table card">
      <table class="custom-table" v-if="!loading">
        <thead>
          <tr>
            <th style="width: 120px;">流水号</th>
            <th style="width: 100px;">类型</th>
            <th>用户</th>
            <th style="width: 140px;">金额</th>
            <th style="width: 120px;">余额</th>
            <th style="width: 140px;">关联订单</th>
            <th>说明</th>
            <th style="width: 170px;">时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in transactions" :key="row.id">
            <td>{{ row.id }}</td>
            <td>
              <span class="badge" :class="getTxBadge(row.type).cls">{{ getTxBadge(row.type).label }}</span>
            </td>
            <td>
              <div class="user-cell">
                <div class="avatar">{{ (row.userName || '?').slice(0, 1) }}</div>
                <div>
                  <strong>{{ row.userName || '-' }}</strong>
                  <small>{{ row.userPhone || row.userId || '' }}</small>
                </div>
              </div>
            </td>
            <td>
              <span :class="row.amount > 0 ? 'amount-positive' : 'amount-negative'">
                {{ row.amount > 0 ? '+' : '' }}&yen;{{ row.amount.toFixed(2) }}
              </span>
            </td>
            <td>&yen;{{ row.balanceAfter?.toFixed(2) || '-' }}</td>
            <td>{{ row.orderId }}</td>
            <td class="td-ellipsis">{{ row.description }}</td>
            <td>{{ row.createdAt }}</td>
          </tr>
          <tr v-if="transactions.length === 0">
            <td colspan="8" class="empty-cell">暂无流水记录</td>
          </tr>
        </tbody>
      </table>

      <div v-if="loading" class="loading-state">加载中...</div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchWallet"
          @size-change="fetchWallet"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import fetch from "@/api/fetch";
import { Ops } from "@/api/apis";

const keyword = ref("");
const typeFilter = ref("all");
const dateRange = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);

const platformStats = ref({
  totalRecharge: 0,
  totalPay: 0,
  totalRefund: 0,
  netInflow: 0,
});

const transactions = ref([]);
const typeDistribution = ref([]);

const getTxBadge = (type) => {
  const map = {
    RECHARGE: { label: '充值', cls: 'badge-success' },
    PAY: { label: '支付', cls: 'badge-primary' },
    REFUND: { label: '退款', cls: 'badge-warning' },
  };
  return map[type] || { label: type, cls: 'badge-info' };
};

const overviewCards = computed(() => [
  { label: "累计充值", value: `¥${platformStats.value.totalRecharge.toLocaleString()}`, tip: "用户充入平台总金额", tone: "primary" },
  { label: "累计消费", value: `¥${platformStats.value.totalPay.toLocaleString()}`, tip: "用户支付订单总金额", tone: "warning" },
  { label: "累计退款", value: `¥${platformStats.value.totalRefund.toLocaleString()}`, tip: "售后退款总金额", tone: "info" },
  { label: "净流入", value: `¥${platformStats.value.netInflow.toLocaleString()}`, tip: "充值 - 退款", tone: "success" },
]);

const fetchWallet = async () => {
  loading.value = true;
  try {
    const res = await fetch(Ops.walletTransactions, {
      keyword: keyword.value,
      type: typeFilter.value,
      startDate: dateRange.value?.[0] || "",
      endDate: dateRange.value?.[1] || "",
      page: page.value,
      pageSize: pageSize.value,
    });
    const data = res?.data || res || {};
    if (data.code === 200) {
      const payload = data.data || {};
      transactions.value = payload.list || [];
      total.value = payload.total || 0;
      if (payload.platformStats) {
        platformStats.value = payload.platformStats;
      }
      if (payload.typeDistribution) {
        typeDistribution.value = payload.typeDistribution;
      }
    } else {
      useMockData();
    }
  } catch (err) {
    useMockData();
  } finally {
    loading.value = false;
  }
};

const useMockData = () => {
  platformStats.value = {
    totalRecharge: 286450.5,
    totalPay: 198320.0,
    totalRefund: 8240.0,
    netInflow: 278210.5,
  };
  typeDistribution.value = [
    { type: "RECHARGE", typeName: "充值", amount: 286450, count: 1248, percent: 56, tone: "primary" },
    { type: "PAY", typeName: "支付", amount: 198320, count: 3267, percent: 38, tone: "warning" },
    { type: "REFUND", typeName: "退款", amount: 8240, count: 86, percent: 6, tone: "info" },
  ];
  total.value = 4601;
  transactions.value = [
    {
      id: "TX202606160012",
      type: "RECHARGE",
      userName: "小明同学",
      userPhone: "138****1234",
      amount: 100,
      balanceAfter: 285.5,
      orderId: "-",
      description: "用户充值，金额 ¥100",
      createdAt: "2026-06-16 14:23:11",
    },
    {
      id: "TX202606160011",
      type: "PAY",
      userName: "吃货小李",
      userPhone: "139****5678",
      amount: -58.5,
      balanceAfter: 124.0,
      orderId: "ORD202606160008",
      description: "支付订单 ORD202606160008",
      createdAt: "2026-06-16 13:48:02",
    },
    {
      id: "TX202606160010",
      type: "REFUND",
      userName: "王女士",
      userPhone: "136****9012",
      amount: 36.0,
      balanceAfter: 260.0,
      orderId: "ORD202606140022",
      description: "售后退款（订单取消）",
      createdAt: "2026-06-16 11:12:48",
    },
    {
      id: "TX202606160009",
      type: "RECHARGE",
      userName: "张先生",
      userPhone: "137****3456",
      amount: 200,
      balanceAfter: 320.0,
      orderId: "-",
      description: "用户充值，金额 ¥200",
      createdAt: "2026-06-16 10:05:33",
    },
  ];
};

const exportData = () => {
  const header = ["流水号", "类型", "用户", "金额", "余额后", "订单号", "说明", "时间"];
  const rows = transactions.value.map((row) => [
    row.id,
    getTxBadge(row.type).label,
    row.userName,
    row.amount,
    row.balanceAfter,
    row.orderId,
    row.description,
    row.createdAt,
  ]);
  const csv = [header, ...rows]
    .map((line) => line.map((cell) => `"${(cell ?? "").toString().replace(/"/g, '""')}"`).join(","))
    .join("\n");
  const blob = new Blob(["\ufeff" + csv], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `wallet_transactions_${Date.now()}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  ElMessage.success("已导出 CSV");
};

onMounted(() => {
  fetchWallet();
});
</script>

<style lang="less" scoped>
.admin-wallet {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ---- Overview ---- */
.overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.overview-card {
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f6f8ff 100%);
  border: 1px solid rgba(79, 70, 229, 0.12);

  &--success {
    background: linear-gradient(180deg, #ffffff 0%, #f0fdf4 100%);
    border-color: rgba(5, 150, 105, 0.12);
  }

  &--warning {
    background: linear-gradient(180deg, #ffffff 0%, #fff8eb 100%);
    border-color: rgba(245, 158, 11, 0.12);
  }

  &--info {
    background: linear-gradient(180deg, #ffffff 0%, #f4f6fa 100%);
    border-color: rgba(99, 102, 241, 0.12);
  }
}

.overview-label {
  font-size: 12px;
  color: rgba(23, 29, 45, 0.6);
}

.overview-value {
  display: block;
  margin-top: 8px;
  font-size: 26px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #4F46E5;
}

.overview-card--success .overview-value { color: #059669; }
.overview-card--warning .overview-value { color: #F59E0B; }
.overview-card--info .overview-value { color: #6366F1; }

.overview-tip {
  display: block;
  margin-top: 6px;
  color: rgba(23, 29, 45, 0.5);
  font-size: 12px;
}

/* ---- Card ---- */
.card {
  padding: 22px;
  border-radius: 22px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(180deg, #ffffff 0%, #f7f9ff 100%);
}

/* ---- Section Heading ---- */
.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 16px;
}

.micro-tag {
  display: inline-block;
  margin-bottom: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  color: #4F46E5;
  background: rgba(79, 70, 229, 0.12);
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.section-heading h3 {
  margin: 0;
  font-size: 22px;
  font-family: "Georgia", "Times New Roman", serif;
}

.section-heading p {
  margin: 6px 0 0;
  color: rgba(23, 29, 45, 0.6);
}

/* ---- Bar Rows ---- */
.bar-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-row {
  display: grid;
  grid-template-columns: 100px 1fr 200px;
  align-items: center;
  gap: 12px;
}

.bar-type {
  font-weight: 600;
  color: #171d2d;
}

.bar-track {
  position: relative;
  height: 12px;
  background: rgba(79, 70, 229, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  border-radius: 999px;
  transition: width 0.4s ease;

  &--primary { background: linear-gradient(90deg, #4F46E5 0%, #818cf8 100%); }
  &--warning { background: linear-gradient(90deg, #F59E0B 0%, #fbbf24 100%); }
  &--info { background: linear-gradient(90deg, #6366F1 0%, #a5b4fc 100%); }
  &--success { background: linear-gradient(90deg, #059669 0%, #34d399 100%); }
}

.bar-meta {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  align-items: baseline;
}

.bar-meta strong {
  font-size: 16px;
  color: #171d2d;
}

.bar-meta small {
  color: rgba(23, 29, 45, 0.6);
}

/* ---- Filter Bar ---- */
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-wrap {
  position: relative;
  flex: 1;
  min-width: 220px;
  max-width: 320px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(0, 0, 0, 0.35);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 36px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 14px;
  color: #1a1a2e;
  background: #fff;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: #4F46E5;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
  }

  &::placeholder {
    color: rgba(0, 0, 0, 0.35);
  }
}

.form-select {
  padding: 10px 36px 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 14px;
  color: #1a1a2e;
  background: #fff;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath d='M3 5l3 3 3-3' stroke='%23999' stroke-width='1.5' fill='none'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  outline: none;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: #4F46E5;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
  }
}

.date-picker {
  width: 280px;
}

/* ---- Custom Table ---- */
.custom-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;

  th {
    text-align: left;
    padding: 10px 12px;
    font-weight: 600;
    color: rgba(18, 31, 54, 0.6);
    border-bottom: 2px solid rgba(79, 70, 229, 0.1);
    font-size: 12px;
    background: rgba(79, 70, 229, 0.03);
  }

  td {
    padding: 10px 12px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    color: #0f1b31;
    vertical-align: middle;
  }

  tr:hover td {
    background: rgba(79, 70, 229, 0.03);
  }
}

.td-ellipsis {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-cell {
  text-align: center;
  color: rgba(0, 0, 0, 0.35);
  padding: 30px 12px !important;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: rgba(0, 0, 0, 0.4);
}

/* ---- User Cell ---- */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4F46E5, #818cf8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}

.user-cell strong {
  display: block;
  color: #171d2d;
  font-size: 13px;
}

.user-cell small {
  display: block;
  color: rgba(23, 29, 45, 0.55);
  font-size: 11px;
}

.amount-positive {
  color: #059669;
  font-weight: 600;
}

.amount-negative {
  color: #E17055;
  font-weight: 600;
}

/* ---- Badge ---- */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge-success { color: #059669; background: rgba(5, 150, 105, 0.12); }
.badge-warning { color: #D97706; background: rgba(217, 119, 6, 0.12); }
.badge-primary { color: #4F46E5; background: rgba(79, 70, 229, 0.12); }
.badge-info    { color: #6B7280; background: rgba(107, 114, 128, 0.12); }

/* ---- Button ---- */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 22px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline {
  background: transparent;
  color: #4F46E5;
  border: 1px solid rgba(79, 70, 229, 0.3);

  &:hover {
    background: rgba(79, 70, 229, 0.06);
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* ---- Responsive ---- */
@media (max-width: 1100px) {
  .overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .bar-row {
    grid-template-columns: 80px 1fr 160px;
  }
}

@media (max-width: 768px) {
  .overview {
    grid-template-columns: 1fr;
  }
  .bar-row {
    grid-template-columns: 70px 1fr 120px;
  }
  .bar-meta strong {
    font-size: 13px;
  }
}
</style>
