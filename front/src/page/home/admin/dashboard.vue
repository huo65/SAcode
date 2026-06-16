<template>
  <div class="a-dash">
    <!-- Hero 统计卡片 -->
    <section class="overview-row">
      <article
        v-for="card in overviewCards"
        :key="card.label"
        class="overview-card"
        :class="`ov--${card.tone}`"
      >
        <div class="ov-head">
          <span class="ov-label">{{ card.label }}</span>
          <div class="ov-icon">
            <i :class="card.icon"></i>
          </div>
        </div>
        <strong class="ov-value">{{ card.value }}</strong>
        <span v-if="card.trend" class="ov-trend" :class="card.trendUp ? 'tr-up' : 'tr-down'">
          <i class="fas" :class="card.trendUp ? 'fa-arrow-up' : 'fa-arrow-down'"></i>
          {{ card.trend }}
        </span>
      </article>
    </section>

    <!-- 系统状态提示 -->
    <div v-if="systemOk" class="alert-banner alert-info">
      <i class="fas fa-info-circle"></i>
      <span>系统运行正常，当前在线商家 <strong>{{ onlineMerchants }}</strong> 家，活跃骑手 <strong>{{ onlineRiders }}</strong> 人</span>
    </div>

    <section class="content-grid">
      <!-- 左：趋势图 -->
      <article class="panel panel-chart">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Revenue Trend</p>
            <h4>平台交易走势</h4>
          </div>
          <div class="chart-legend">
            <span class="leg-item"><i style="background:var(--primary)"></i>GMV</span>
            <span class="leg-item"><i style="background:#A5B4FC"></i>订单量</span>
          </div>
        </div>
        <div class="chart-area" v-if="trendData.length">
          <div v-for="(item, idx) in trendData" :key="item.date" class="chart-col">
            <div class="chart-bars">
              <div class="chart-bar bar-gmv" :style="{ height: resolveH(item.gmv, maxGmv) + '%' }"></div>
              <div class="chart-bar bar-orders" :style="{ height: resolveH(item.orders, maxOrders) + '%' }"></div>
            </div>
            <span class="chart-label">{{ item.date.slice(5) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="50" />
      </article>

      <!-- 右：快捷操作 + 最新动态 -->
      <div class="side-panels">
        <!-- 快捷操作 -->
        <article class="panel panel-quick">
          <div class="panel-head">
            <div>
              <p class="panel-kicker">Quick Actions</p>
              <h4>治理快捷入口</h4>
            </div>
          </div>
          <div class="quick-grid">
            <div
              v-for="q in quickEntries"
              :key="q.key"
              class="quick-card"
              @click="$router.push(q.path)"
            >
              <div class="quick-icon" :class="`qi--${q.tone}`">
                <i :class="q.icon"></i>
              </div>
              <div class="quick-text">
                <strong>{{ q.label }}</strong>
                <span>{{ q.tip }}</span>
              </div>
              <i class="fas fa-chevron-right quick-arrow"></i>
            </div>
          </div>
        </article>

        <!-- 最新动态 -->
        <article class="panel panel-activity">
          <div class="panel-head">
            <div>
              <p class="panel-kicker">Live Activity</p>
              <h4>最新动态</h4>
            </div>
          </div>
          <div class="activity-list">
            <div v-for="(act, idx) in activities" :key="idx" class="activity-item">
              <div class="activity-dot" :class="`ad--${act.tone}`"></div>
              <div class="activity-body">
                <div class="activity-text" v-html="act.text"></div>
                <div class="activity-time">{{ act.time }}</div>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>

    <!-- 第二行：待处理工单 + 区域排行 -->
    <section class="content-grid">
      <article class="panel panel-tickets">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Pending Tickets</p>
            <h4>待处理售后工单</h4>
          </div>
          <el-button size="small" type="primary" @click="$router.push('/home/admin/after-sale')">查看全部</el-button>
        </div>
        <el-table :data="pendingTickets" size="small" class="clean-table" style="width:100%">
          <el-table-column prop="id" label="工单号" width="150" />
          <el-table-column prop="orderId" label="订单号" width="160" />
          <el-table-column prop="userName" label="顾客" width="100" />
          <el-table-column prop="type" label="类型" width="90" />
          <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="提交时间" width="160" />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="handleTicket(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!pendingTickets.length" description="暂无待处理工单" :image-size="50" />
      </article>

      <article class="panel panel-ranking">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Area Ranking</p>
            <h4>区域订单排行</h4>
          </div>
        </div>
        <el-table :data="areaRanking" size="small" class="clean-table" style="width:100%">
          <el-table-column prop="rank" label="#" width="60" align="center">
            <template #default="{ row }">
              <span :style="row.rank <= 3 ? 'color:var(--primary);font-weight:800' : ''">{{ row.rank }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="area" label="区域" />
          <el-table-column prop="orders" label="今日订单" width="120" align="right" />
          <el-table-column prop="ratio" label="占比" width="100" align="right" />
          <el-table-column prop="trend" label="趋势" width="100" align="center">
            <template #default="{ row }">
              <span :style="row.trendUp ? 'color:var(--success)' : 'color:var(--danger)'">
                <i class="fas" :class="row.trendUp ? 'fa-arrow-up' : 'fa-arrow-down'"></i>
                {{ Math.abs(row.trend) }}%
              </span>
            </template>
          </el-table-column>
        </el-table>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import fetch from "@/api/fetch";
import { Ops } from "@/api/apis";

const router = useRouter();

const dashboard = ref({});
const trendData = computed(() => dashboard.value?.dailyTrend || []);
const recentOrders = computed(() => dashboard.value?.recentOrders || []);
const pendingTickets = ref([]);

// 系统状态
const systemOk = ref(true);
const onlineMerchants = ref(1247);
const onlineRiders = ref(856);

// 概览卡片
const overviewCards = computed(() => {
  const o = dashboard.value?.overview || {};
  return [
    { label: "今日总订单", value: (o.totalOrders || 42386).toLocaleString(), icon: "fas fa-receipt", tone: "indigo", trend: "+9.2%", trendUp: true },
    { label: "平台总营收", value: `¥${((o.gmv || 1865000) / 10000).toFixed(1)}万`, icon: "fas fa-yen-sign", tone: "green", trend: "+7.8%", trendUp: true },
    { label: "活跃商家", value: (o.activeMerchants || 1247).toLocaleString(), icon: "fas fa-store", tone: "blue", trend: "+23", trendUp: true },
    { label: "在线骑手", value: (o.onlineRiders || 856).toLocaleString(), icon: "fas fa-motorcycle", tone: "amber", trend: "-12", trendUp: false },
    { label: "注册用户", value: `${((o.totalUsers || 586420) / 10000).toFixed(1)}万`, icon: "fas fa-users", tone: "purple", trend: "+1,280", trendUp: true },
  ];
});

// 快捷入口
const quickEntries = [
  { key: "goods",      label: "商品审核", tip: "处理待审核商品", icon: "fas fa-box-open",    path: "/home/admin/goods",       tone: "indigo" },
  { key: "after-sale", label: "售后工单", tip: "处理退款/换货", icon: "fas fa-undo-alt",     path: "/home/admin/after-sale",  tone: "red" },
  { key: "merchant",   label: "商家管理", tip: "管理入驻商家", icon: "fas fa-store",        path: "/home/admin/merchant",    tone: "blue" },
  { key: "rider",      label: "骑手管理", tip: "管理骑手账号", icon: "fas fa-motorcycle",   path: "/home/admin/rider",       tone: "amber" },
  { key: "finance",    label: "财务中心", tip: "平台资金审计", icon: "fas fa-landmark",     path: "/home/admin/finance",     tone: "green" },
  { key: "settings",   label: "系统设置", tip: "配置平台参数", icon: "fas fa-sliders-h",   path: "/home/admin/settings",    tone: "purple" },
];

// 最新动态
const activities = ref([
  { text: "<strong>川味小馆</strong> 提交了入驻申请", tone: "green", time: "3分钟前" },
  { text: "骑手 <strong>张伟</strong> 被投诉，待处理", tone: "red", time: "8分钟前" },
  { text: "<strong>美团渠道</strong> 订单量突破 10,000 单/日", tone: "blue", time: "15分钟前" },
  { text: "商家 <strong>汉堡王国贸店</strong> 营业执照即将过期", tone: "amber", time: "32分钟前" },
  { text: "系统完成 <strong>V2.8.1</strong> 版本更新部署", tone: "purple", time: "1小时前" },
]);

// 区域排行
const areaRanking = ref([
  { rank: 1, area: "朝阳区", orders: 12846, ratio: "30.3%", trend: 5.2, trendUp: true },
  { rank: 2, area: "海淀区", orders: 9432,  ratio: "22.3%", trend: 3.8, trendUp: true },
  { rank: 3, area: "西城区", orders: 6218,  ratio: "14.7%", trend: -1.2, trendUp: false },
  { rank: 4, area: "东城区", orders: 5684,  ratio: "13.4%", trend: 2.1, trendUp: true },
  { rank: 5, area: "丰台区", orders: 4106,  ratio: "9.7%",  trend: 0.5, trendUp: true },
]);

const maxGmv = computed(() => Math.max(1, ...trendData.value.map(d => Number(d.gmv || 0))));
const maxOrders = computed(() => Math.max(1, ...trendData.value.map(d => Number(d.orders || 0))));

const resolveH = (v, max) => {
  if (!max) return 8;
  return Math.max(10, Math.round((Number(v || 0) / max) * 100));
};

const handleTicket = (ticket) => {
  router.push(`/home/admin/after-sale?ticketId=${ticket.id}`);
};

const loadDashboard = () => {
  fetch(Ops.adminDashboard).then((data) => {
    dashboard.value = data || {};
    // mock 工单
    if (!data?.pendingTickets?.length) {
      pendingTickets.value = [
        { id: "AS20260616018", orderId: "ORD202606160088", userName: "王女士", type: "退款", reason: "商品与描述不符", createdAt: "2026-06-16 14:23" },
        { id: "AS20260616017", orderId: "ORD202606150062", userName: "李先生", type: "换货", reason: "配送错误", createdAt: "2026-06-16 12:08" },
        { id: "AS20260616016", orderId: "ORD202606150045", userName: "张同学", type: "退款", reason: "未收到餐品", createdAt: "2026-06-16 10:42" },
      ];
    } else {
      pendingTickets.value = data.pendingTickets;
    }
  });
};

onMounted(() => {
  loadDashboard();
});
</script>

<style lang="less" scoped>
.a-dash {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-bottom: 32px;
}

/* ===== 概览卡片行 ===== */
.overview-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
.overview-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 20px 22px 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}
.overview-card::before {
  content: "";
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2.5px;
}
.ov--indigo::before  { background: var(--primary); }
.ov--green::before   { background: var(--success); }
.ov--blue::before    { background: var(--info); }
.ov--amber::before   { background: #D97706; }
.ov--purple::before  { background: #7C3AED; }

.ov-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ov-label {
  font-size: 12.5px;
  color: var(--text-muted);
  font-weight: 500;
}
.ov-icon {
  width: 34px; height: 34px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px;
}
.ov--indigo  .ov-icon { background: var(--primary-light); color: var(--primary); }
.ov--green   .ov-icon { background: var(--success-light); color: var(--success); }
.ov--blue    .ov-icon { background: var(--info-light);    color: var(--info); }
.ov--amber   .ov-icon { background: var(--warning-light); color: #D97706; }
.ov--purple  .ov-icon { background: #F5F3FF;          color: #7C3AED; }

.ov-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
  display: block;
}
.ov-trend {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 5px;
  width: fit-content;
}
.tr-up   { background: var(--success-light); color: #065F46; }
.tr-down { background: var(--danger-light);  color: #991B1B; }

/* ===== Alert Banner ===== */
.alert-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
}
.alert-info {
  background: var(--info-light);
  color: #075985;
  border: 1px solid #BAE6FD;
}
.alert-info i { font-size: 15px; }

/* ===== Content Grid ===== */
.content-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 18px;
}

/* ===== Panel 通用 ===== */
.panel {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border);
}
.panel-kicker {
  margin: 0 0 4px;
  font-size: 10px;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  font-weight: 700;
  color: var(--primary);
}
.panel-head h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

/* ===== Chart ===== */
.chart-legend {
  display: flex;
  gap: 14px;
  font-size: 11px;
  color: var(--text-muted);
}
.leg-item {
  display: flex;
  align-items: center;
  gap: 5px;
}
.leg-item i {
  width: 8px; height: 8px;
  border-radius: 2px;
  display: inline-block;
}
.chart-area {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  height: 200px;
  padding: 16px 0 0;
}
.chart-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.chart-bars {
  width: 100%;
  height: 140px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
}
.chart-bar {
  width: 36%;
  min-height: 4px;
  border-radius: 4px 4px 2px 2px;
  transition: height 0.4s ease;
}
.bar-gmv     { background: linear-gradient(180deg, var(--primary), #818CF8); }
.bar-orders  { background: linear-gradient(180deg, #A5B4FC, #C7D2FE); }
.chart-label {
  font-size: 10px;
  color: var(--text-muted);
  font-weight: 600;
}

/* ===== Quick Grid ===== */
.quick-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--bg);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.quick-card:hover {
  background: var(--primary-light);
  transform: translateX(2px);
}
.quick-icon {
  width: 38px; height: 38px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px;
  color: #fff;
  flex-shrink: 0;
}
.qi--indigo { background: linear-gradient(135deg, #4F46E5, #6366F1); }
.qi--red     { background: linear-gradient(135deg, #DC2626, #EF4444); }
.qi--blue    { background: linear-gradient(135deg, #0284C7, #0EA5E9); }
.qi--amber   { background: linear-gradient(135deg, #D97706, #F59E0B); }
.qi--green   { background: linear-gradient(135deg, #059669, #10B981); }
.qi--purple  { background: linear-gradient(135deg, #7C3AED, #8B5CF6); }

.quick-text {
  flex: 1; min-width: 0;
  strong { display: block; font-size: 14px; font-weight: 700; color: var(--text-primary); }
  span   { display: block; font-size: 11px; color: var(--text-muted); margin-top: 1px; }
}
.quick-arrow {
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
}

/* ===== Activity List ===== */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.activity-item:last-child { border-bottom: none; }
.activity-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}
.ad--green  { background: var(--success); }
.ad--red     { background: var(--danger); }
.ad--blue    { background: var(--info); }
.ad--amber   { background: var(--warning); }
.ad--purple  { background: #7C3AED; }

.activity-body { flex: 1; min-width: 0; }
.activity-text { color: var(--text-secondary); line-height: 1.5; }
.activity-text strong { color: var(--text-primary); font-weight: 600; }
.activity-time { font-size: 11px; color: var(--text-muted); margin-top: 2px; }

/* ===== Table ===== */
.clean-table {
  font-size: 13px;
}
.clean-table :deep(th) {
  background: var(--bg) !important;
  font-size: 11px !important;
  font-weight: 600 !important;
  color: var(--text-muted) !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.clean-table :deep(td) {
  padding: 10px 14px !important;
}

/* ===== Responsive ===== */
@media (max-width: 1600px) {
  .overview-row { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 1200px) {
  .overview-row { grid-template-columns: repeat(2, 1fr); }
  .content-grid   { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .overview-row { grid-template-columns: 1fr; }
  .panel { padding: 16px; }
}
</style>
