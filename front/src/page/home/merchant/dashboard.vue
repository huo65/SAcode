<template>
  <div class="m-dash">
    <!-- Hero 区 -->
    <header class="dash-hero">
      <div class="hero-left">
        <p class="hero-eyebrow">Merchant Dashboard</p>
        <h1>{{ greeting }}，{{ userInfo.name || '老板' }}</h1>
        <p class="hero-desc">这里是经营数据总览，关键指标与待办一目了然。</p>
      </div>
      <div class="hero-time">
        <i class="far fa-clock"></i>
        <span>{{ currentTime }}</span>
      </div>
    </header>

    <!-- 4 个统计卡片 -->
    <section class="stats-grid">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="stat-card"
        :class="`stat--${card.tone}`"
      >
        <div class="stat-accent"></div>
        <div class="stat-icon">
          <i :class="card.icon"></i>
        </div>
        <div class="stat-body">
          <span class="stat-label">{{ card.label }}</span>
          <strong class="stat-value">{{ card.value }}</strong>
          <span v-if="card.sub" class="stat-sub">{{ card.sub }}</span>
        </div>
        <span v-if="card.trend" class="stat-trend" :class="card.trendUp ? 'trend-up' : 'trend-down'">
          <i class="fas" :class="card.trendUp ? 'fa-arrow-up' : 'fa-arrow-down'"></i>
          {{ card.trend }}
        </span>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="quick-section">
      <h3 class="section-title"><i class="fas fa-bolt"></i> 快捷操作</h3>
      <div class="quick-grid">
        <div
          v-for="action in quickActions"
          :key="action.label"
          class="quick-card"
          :class="`quick--${action.tone}`"
          @click="$router.push(action.path)"
        >
          <div class="quick-icon">
            <i :class="action.icon"></i>
          </div>
          <div class="quick-text">
            <strong>{{ action.label }}</strong>
            <span>{{ action.hint }}</span>
          </div>
          <i class="fas fa-chevron-right quick-arrow"></i>
        </div>
      </div>
    </section>

    <!-- 双栏：销售趋势 + 待办/最新订单 -->
    <section class="content-grid">
      <!-- 左：销售趋势 -->
      <article class="panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Revenue Trend</p>
            <h4>营收走势</h4>
          </div>
          <el-radio-group v-model="trendRange" size="small" @change="loadDashboard">
            <el-radio-button :label="7">7 天</el-radio-button>
            <el-radio-button :label="30">30 天</el-radio-button>
          </el-radio-group>
        </div>
        <div class="chart-area" v-if="trendData.length">
          <div v-for="(item, idx) in trendData" :key="item.date" class="chart-col">
            <div class="chart-bar-wrap">
              <div
                class="chart-bar"
                :style="{ height: resolveH(item.gmv) + '%' }"
                :title="`${item.date}\n¥${item.gmv}`"
              ></div>
            </div>
            <span class="chart-label">{{ item.date.slice(5) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="60" />
      </article>

      <!-- 右：待办 + 最新订单 -->
      <div class="side-panels">
        <!-- 待办 -->
        <article class="panel panel-todo">
          <div class="panel-head">
            <div>
              <p class="panel-kicker">Pending</p>
              <h4>待办事项</h4>
            </div>
            <el-badge :value="todoList.length" :hidden="!todoList.length" :max="99" type="danger">
              <el-button size="small" @click="$router.push('/home/merchant/orders')">查看全部</el-button>
            </el-badge>
          </div>
          <ul class="todo-list" v-if="todoList.length">
            <li v-for="(item, idx) in todoList" :key="idx">
              <span class="badge" :class="`badge--${item.tone}`">{{ item.tag }}</span>
              <span class="todo-text">{{ item.text }}</span>
              <span class="todo-time">{{ item.time }}</span>
            </li>
          </ul>
          <el-empty v-else description="暂无待办" :image-size="50" />
        </article>

        <!-- 最新订单 -->
        <article class="panel panel-recent">
          <div class="panel-head">
            <div>
              <p class="panel-kicker">Latest Orders</p>
              <h4>最新订单</h4>
            </div>
            <el-button size="small" @click="$router.push('/home/merchant/orders')">查看全部</el-button>
          </div>
          <ul class="recent-list" v-if="recentOrders.length">
            <li v-for="(order, idx) in recentOrders" :key="idx" class="recent-item">
              <span class="recent-id">#{{ order.id }}</span>
              <div class="recent-info">
                <strong>{{ order.customer }}</strong>
                <span>{{ order.items }}</span>
              </div>
              <span class="recent-amount">¥{{ order.amount }}</span>
              <span class="badge" :class="`badge--${order.tone}`">{{ order.statusLabel }}</span>
            </li>
          </ul>
          <el-empty v-else description="暂无订单" :image-size="50" />
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { userInfo } from '@/store';
import { Ops, Order as OrderApi, AfterSale, User } from '@/api/apis';
import fetch from '@/api/fetch';

const router = useRouter();
const currentTime = ref('');
let timer = null;

const greeting = computed(() => {
  const h = new Date().getHours();
  if (h < 6) return '夜深了';
  if (h < 11) return '早上好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
});

const updateTime = () => {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  const w = ['周日','周一','周二','周三','周四','周五','周六'][d.getDay()];
  currentTime.value = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${w} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const pendingOrderCount = ref(0);
const pendingTicketCount = ref(0);
const todayGmv = ref(0);
const totalGoods = ref(0);
const walletBalance = ref(0);
const walletIncome = ref(0);

const statCards = computed(() => [
  {
    label: '今日营业额',
    value: `¥${todayGmv.value || 0}`,
    icon: 'fas fa-yen-sign',
    tone: 'orange',
    sub: '统计至当前',
    trend: '+12.5%',
    trendUp: true,
  },
  {
    label: '待处理订单',
    value: pendingOrderCount.value,
    icon: 'fas fa-clipboard-list',
    tone: 'blue',
    sub: '需要立即处理',
    trend: pendingOrderCount.value > 0 ? `${pendingOrderCount.value} 单待接` : '已清空',
    trendUp: false,
  },
  {
    label: '待处理售后',
    value: pendingTicketCount.value,
    icon: 'fas fa-headset',
    tone: 'red',
    sub: '工单数量',
    trend: pendingTicketCount.value > 0 ? `+${pendingTicketCount.value}` : '正常',
    trendUp: false,
  },
  {
    label: '在售商品',
    value: totalGoods.value,
    icon: 'fas fa-box-open',
    tone: 'green',
    sub: '当前上架数',
    trend: '持续更新',
    trendUp: true,
  },
  {
    label: 'Wallet balance',
    value: `¥${walletBalance.value || 0}`,
    icon: 'fas fa-wallet',
    tone: 'orange',
    sub: `Order income ¥${walletIncome.value || 0}`,
    trend: 'Updated from payment ledger',
    trendUp: true,
  },
]);

const trendRange = ref(7);
const trendData = ref([]);
const trendMax = computed(() => Math.max(1, ...trendData.value.map(d => d.gmv)));
const resolveH = (v) => Math.max(4, (v / trendMax.value) * 100);

const quickActions = [
  { label: '商品管理', icon: 'fas fa-box-open', tone: 'orange', path: '/home/merchant/goods', hint: '上新 / 下架' },
  { label: '订单处理', icon: 'fas fa-clipboard-list', tone: 'blue', path: '/home/merchant/orders', hint: '接单 / 出餐' },
  { label: '售后工单', icon: 'fas fa-headset', tone: 'red', path: '/home/merchant/after-sale', hint: '处理客诉' },
  { label: '门店设置', icon: 'fas fa-store', tone: 'amber', path: '/home/merchant/store', hint: '编辑信息' },
  { label: 'Merchant Wallet', icon: 'fas fa-wallet', tone: 'green', path: '/home/merchant/wallet', hint: 'Income / refunds' },
];

const todoList = ref([]);
const buildTodo = () => {
  const list = [];
  if (pendingOrderCount.value > 0) {
    list.push({ tag: `${pendingOrderCount.value} 单待接`, text: '有新的已支付订单需要处理', tone: 'blue', time: '刚刚' });
  }
  if (pendingTicketCount.value > 0) {
    list.push({ tag: `${pendingTicketCount.value} 张工单`, text: '售后工单等待处理', tone: 'red', time: '今天' });
  }
  todoList.value = list;
};

const recentOrders = ref([]);
const statusMap = {
  0: { label: '待接单', tone: 'warning' },
  1: { label: '已接单', tone: 'primary' },
  2: { label: '配送中', tone: 'info' },
  3: { label: '已完成', tone: 'success' },
  4: { label: '已取消', tone: 'info' },
  5: { label: '退款中', tone: 'danger' },
};

const loadRecent = () => {
  if (userInfo.value.type !== 'mer') return;
  fetch(OrderApi.getOrderList, { usrId: userInfo.value.id, state: -1, timeOrder: 1, pageSize: 5 }).then((data) => {
    const list = data?.merList || data?.list || [];
    recentOrders.value = list.slice(0, 5).map(o => {
      const s = statusMap[o.state] || { label: '未知', tone: 'info' };
      return {
        id: o.id || o.orderId,
        customer: o.userName || o.usrName || '匿名顾客',
        items: o.goods?.map(g => g.name).join('、') || '订单详情',
        amount: o.amount || o.totalPrice || 0,
        statusLabel: s.label,
        tone: s.tone,
      };
    });
  }).catch(() => { recentOrders.value = []; });
};

const loadDashboard = () => {
  fetch(Ops.merchantDashboard, { range: trendRange.value }).then((data) => {
    if (data?.trend) trendData.value = data.trend;
    if (data?.summary) {
      todayGmv.value = data.summary.todayGmv ?? 0;
      totalGoods.value = data.summary.totalGoods ?? 0;
    }
  }).catch(() => {
    // mock
    trendData.value = Array.from({ length: trendRange.value }, (_, i) => {
      const d = new Date(); d.setDate(d.getDate() - (trendRange.value - 1 - i));
      return { date: d.toISOString().slice(0, 10), gmv: Math.floor(Math.random() * 2000 + 800) };
    });
  });
};

const loadPending = () => {
  if (userInfo.value.type !== 'mer') return;
  fetch(OrderApi.getOrderList, { usrId: userInfo.value.id, state: 0, timeOrder: 1 }).then((data) => {
    pendingOrderCount.value = data?.merList?.length || 0;
    buildTodo();
  });
  fetch(AfterSale.stats, { scope: 'merchant' }).then((data) => {
    pendingTicketCount.value = Number(data?.stats?.pending || 0);
    buildTodo();
  });
};

const loadWallet = () => {
  fetch(User.wallet).then((data) => {
    const wallet = data?.data || data || {};
    walletBalance.value = Number(wallet.balance || 0);
    walletIncome.value = Number(wallet.totalMerchantIncome || 0);
  }).catch(() => {
    walletBalance.value = 0;
    walletIncome.value = 0;
  });
};

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 60000);
  loadDashboard();
  loadPending();
  loadRecent();
  loadWallet();
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<style lang="less" scoped>
.m-dash {
  padding: 0 0 32px;
}

/* ===== Hero ===== */
.dash-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px 4px 28px;
  gap: 24px;
  flex-wrap: wrap;
}
.hero-eyebrow {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  font-weight: 700;
  color: var(--primary);
}
.dash-hero h1 {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
}
.hero-desc {
  max-width: 680px;
  margin: 10px 0 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
}
.hero-time {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 999px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  i { color: var(--primary); }
}

/* ===== Stats Grid ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
  margin-bottom: 28px;
}
.stat-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 22px 24px 18px;
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}
.stat-accent {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
}
.stat--orange .stat-accent { background: var(--primary); }
.stat--blue   .stat-accent { background: var(--info); }
.stat--red    .stat-accent { background: var(--danger); }
.stat--green  .stat-accent { background: var(--success); }
.stat--amber  .stat-accent { background: #F59E0B; }

.stat-icon {
  width: 42px; height: 42px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px;
  margin-bottom: 10px;
}
.stat--orange .stat-icon { background: var(--primary-light); color: var(--primary); }
.stat--blue   .stat-icon { background: var(--info-light);    color: var(--info); }
.stat--red    .stat-icon { background: var(--danger-light); color: var(--danger); }
.stat--green  .stat-icon { background: var(--success-light); color: var(--success); }
.stat--amber  .stat-icon { background: var(--warning-light); color: #D97706; }

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
  margin: 4px 0;
  display: block;
}
.stat-sub {
  font-size: 12px;
  color: var(--text-muted);
  display: block;
  margin-top: 2px;
}
.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  margin-top: 6px;
  width: fit-content;
}
.trend-up   { background: var(--success-light); color: #065F46; }
.trend-down { background: var(--danger-light);  color: #991B1B; }

/* ===== Quick Actions ===== */
.quick-section {
  margin-bottom: 28px;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 14px;
  display: flex; align-items: center; gap: 8px;
  i { color: var(--primary); font-size: 14px; }
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.quick-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(0,0,0,0.05);
  border-color: var(--primary);
}
.quick-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
  color: #fff;
  flex-shrink: 0;
}
.quick--orange .quick-icon { background: linear-gradient(135deg, var(--primary), #FF8A5C); }
.quick--blue   .quick-icon { background: linear-gradient(135deg, var(--info), #38BDF8); }
.quick--red    .quick-icon { background: linear-gradient(135deg, var(--danger), #FB923C); }
.quick--amber  .quick-icon { background: linear-gradient(135deg, #F59E0B, #FBBF24); }

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

/* ===== Content Grid ===== */
.content-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
}
.side-panels {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== Panel ===== */
.panel {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: hidden;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding-bottom: 12px;
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
.chart-area {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  height: 200px;
  padding: 12px 0 0;
}
.chart-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.chart-bar-wrap {
  width: 100%;
  height: 140px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.chart-bar {
  width: 60%;
  min-height: 4px;
  border-radius: 6px 6px 2px 2px;
  background: linear-gradient(180deg, var(--primary), lighten(#E8652B, 20%));
  transition: height 0.4s ease;
}
.chart-bar:hover { filter: brightness(1.08); }
.chart-label {
  font-size: 10px;
  color: var(--text-muted);
  font-weight: 600;
}

/* ===== Todo List ===== */
.todo-list {
  list-style: none;
  padding: 0; margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.todo-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: var(--bg);
  border-radius: 10px;
  font-size: 13px;
}
.todo-text { flex: 1; color: var(--text-primary); font-weight: 500; }
.todo-time { font-size: 11px; color: var(--text-muted); white-space: nowrap; }

/* ===== Recent Orders ===== */
.recent-list {
  list-style: none;
  padding: 0; margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.recent-item {
  display: grid;
  grid-template-columns: 70px 1fr 80px auto;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: var(--bg);
  border-radius: 10px;
  font-size: 13px;
}
.recent-id {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 600;
}
.recent-info {
  min-width: 0;
  strong { display: block; color: var(--text-primary); font-weight: 600; font-size: 13px; }
  span   { display: block; color: var(--text-muted); font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
.recent-amount {
  font-weight: 700;
  color: var(--primary);
  text-align: right;
  font-size: 14px;
}

/* ===== Badge ===== */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}
.badge--warning { background: var(--warning-light); color: #92400E; }
.badge--primary  { background: var(--info-light);    color: #075985; }
.badge--info     { background: #F1F5F9;       color: #475569; }
.badge--success  { background: var(--success-light); color: #065F46; }
.badge--danger   { background: var(--danger-light); color: #991B1B; }

/* ===== Responsive ===== */
@media (max-width: 1400px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .quick-grid  { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 1100px) {
  .content-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .stats-grid  { grid-template-columns: 1fr; }
  .quick-grid  { grid-template-columns: 1fr; }
  .recent-item { grid-template-columns: 60px 1fr 70px auto; gap: 6px; font-size: 12px; }
  .dash-hero   { padding: 8px 0 20px; }
  .dash-hero h1 { font-size: 24px; }
}
</style>
