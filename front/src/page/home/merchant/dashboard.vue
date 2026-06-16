<template>
  <div class="merchant-dashboard">
    <!-- Hero 区域 -->
    <div class="dashboard-hero">
      <div>
        <p class="eyebrow">商家工作台</p>
        <h2>{{ greeting }}, {{ userInfo.name || '老板' }}</h2>
        <p class="hero-desc">
          这里是门店运营总览，今日关键指标、最新订单与待办事项一目了然。
        </p>
      </div>
      <div class="hero-time">
        <i class="far fa-clock"></i>
        <span>{{ currentTime }}</span>
      </div>
    </div>

    <!-- 4 个统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="stat-card"
        :class="`stat-card--${card.tone}`"
      >
        <div class="stat-card-head">
          <div class="stat-card-icon">
            <i :class="card.icon"></i>
          </div>
          <span class="badge" :class="`badge--${card.tone}`" v-if="card.trend">
            <i class="fas" :class="card.trendUp ? 'fa-arrow-up' : 'fa-arrow-down'"></i>
            {{ card.trend }}
          </span>
        </div>
        <div class="stat-card-body">
          <div class="stat-card-value">{{ card.value }}</div>
          <div class="stat-card-label">{{ card.label }}</div>
        </div>
        <div class="stat-card-foot" v-if="card.sub">
          <span>{{ card.sub }}</span>
        </div>
      </div>
    </div>

    <!-- 双栏：经营趋势 + 快捷操作 -->
    <div class="dashboard-grid">
      <!-- 左：销售趋势 -->
      <article class="panel glass-panel">
        <div class="panel-head">
          <div>
            <p class="micro-tag">经营趋势</p>
            <h3>近 7 天销售走势</h3>
          </div>
          <el-button-group>
            <el-button size="small" :type="trendRange === 7 ? 'primary' : ''" @click="setTrendRange(7)">7 天</el-button>
            <el-button size="small" :type="trendRange === 30 ? 'primary' : ''" @click="setTrendRange(30)">30 天</el-button>
          </el-button-group>
        </div>
        <div class="trend-chart">
          <div v-for="item in trendData" :key="item.date" class="trend-col">
            <div class="trend-bar-wrap">
              <div
                class="trend-bar"
                :style="{ height: resolveHeight(item.gmv) + '%' }"
                :title="`${item.date}: ¥${item.gmv}`"
              ></div>
            </div>
            <span class="trend-date">{{ item.date.slice(5) }}</span>
            <span class="trend-val">¥{{ item.gmv }}</span>
          </div>
        </div>
        <div class="empty-tip" v-if="!trendData.length">
          <i class="fas fa-chart-line"></i>
          <span>暂无销售数据</span>
        </div>
      </article>

      <!-- 右：快捷操作 -->
      <article class="panel glass-panel">
        <div class="panel-head">
          <div>
            <p class="micro-tag">快捷操作</p>
            <h3>日常管理</h3>
          </div>
        </div>
        <div class="quick-actions">
          <div
            v-for="action in quickActions"
            :key="action.label"
            class="quick-action"
            :class="`quick-action--${action.tone}`"
            @click="onQuickAction(action)"
          >
            <i :class="action.icon"></i>
            <span>{{ action.label }}</span>
            <small v-if="action.hint">{{ action.hint }}</small>
          </div>
        </div>
      </article>
    </div>

    <!-- 待办 + 最新订单 -->
    <div class="dashboard-grid dashboard-grid--third">
      <article class="panel glass-panel">
        <div class="panel-head">
          <div>
            <p class="micro-tag">待办事项</p>
            <h3>需要您处理</h3>
          </div>
          <el-badge :value="todoList.length" :hidden="todoList.length === 0" :max="99" type="danger">
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
        <div class="empty-tip" v-else>
          <i class="fas fa-check-circle"></i>
          <span>暂无待办事项</span>
        </div>
      </article>

      <article class="panel glass-panel panel--wide">
        <div class="panel-head">
          <div>
            <p class="micro-tag">最新动态</p>
            <h3>最近订单</h3>
          </div>
          <el-button size="small" @click="$router.push('/home/merchant/orders')">查看全部</el-button>
        </div>
        <ul class="recent-orders" v-if="recentOrders.length">
          <li v-for="(order, idx) in recentOrders" :key="idx" class="recent-order">
            <div class="recent-order-id">#{{ order.id }}</div>
            <div class="recent-order-info">
              <strong>{{ order.customer }}</strong>
              <span>{{ order.items }}</span>
            </div>
            <div class="recent-order-amount">¥{{ order.amount }}</div>
            <span class="badge" :class="`badge--${order.tone}`">{{ order.statusLabel }}</span>
          </li>
        </ul>
        <div class="empty-tip" v-else>
          <i class="fas fa-receipt"></i>
          <span>暂无最新订单</span>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { userInfo } from '@/store';
import { Ops, Order as OrderApi, AfterSale } from '@/api/apis';
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
  const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()];
  currentTime.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${w} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

// ==================== 统计卡片 ====================
const pendingOrderCount = ref(0);
const pendingTicketCount = ref(0);
const todayGmv = ref(0);
const totalGoods = ref(0);

const statCards = computed(() => [
  {
    label: '今日营业额',
    value: `¥${todayGmv.value || 0}`,
    icon: 'fas fa-yen-sign',
    tone: 'primary',
    sub: '统计至当前',
    trend: '+12.5%',
    trendUp: true,
  },
  {
    label: '待处理订单',
    value: pendingOrderCount.value,
    icon: 'fas fa-clipboard-list',
    tone: 'warning',
    sub: '需要立即处理',
    trend: pendingOrderCount.value > 0 ? '需关注' : '已清空',
    trendUp: false,
  },
  {
    label: '待处理售后',
    value: pendingTicketCount.value,
    icon: 'fas fa-headset',
    tone: 'danger',
    sub: '工单数量',
    trend: pendingTicketCount.value > 5 ? '+' + pendingTicketCount.value : '正常',
    trendUp: false,
  },
  {
    label: '在售商品',
    value: totalGoods.value,
    icon: 'fas fa-box-open',
    tone: 'success',
    sub: '当前上架数',
    trend: '持续更新',
    trendUp: true,
  },
]);

// ==================== 销售趋势 ====================
const trendRange = ref(7);
const trendData = ref([]);
const trendMax = computed(() => Math.max(1, ...trendData.value.map(d => d.gmv)));
const resolveHeight = (v) => Math.max(4, (v / trendMax.value) * 100);

const setTrendRange = (range) => {
  trendRange.value = range;
  loadDashboard();
};

// ==================== 快捷操作 ====================
const quickActions = [
  { label: '商品管理', icon: 'fas fa-box-open', tone: 'primary', path: '/home/merchant/goods', hint: '上新 / 下架' },
  { label: '订单处理', icon: 'fas fa-clipboard-list', tone: 'warning', path: '/home/merchant/orders', hint: '接单 / 退款' },
  { label: '售后工单', icon: 'fas fa-headset', tone: 'danger', path: '/home/merchant/after-sale', hint: '处理客诉' },
  { label: '门店资料', icon: 'fas fa-store', tone: 'info', path: '/home/merchant/store', hint: '编辑信息' },
  { label: '经营分析', icon: 'fas fa-chart-line', tone: 'success', path: '/home/merchant/ops', hint: '查看报表' },
  { label: '账号信息', icon: 'fas fa-user-cog', tone: 'default', path: '/home/merchant/info', hint: '个人设置' },
];

const onQuickAction = (action) => {
  router.push(action.path);
};

// ==================== 待办列表 ====================
const todoList = ref([]);

const buildTodoList = () => {
  const list = [];
  if (pendingOrderCount.value > 0) {
    list.push({
      tag: `${pendingOrderCount.value} 单待接`,
      text: '有新的已支付订单需要处理',
      tone: 'warning',
      time: '刚刚',
    });
  }
  if (pendingTicketCount.value > 0) {
    list.push({
      tag: `${pendingTicketCount.value} 张工单`,
      text: '售后工单等待处理',
      tone: 'danger',
      time: '今天',
    });
  }
  todoList.value = list;
};

// ==================== 最近订单 ====================
const recentOrders = ref([]);

const statusMap = {
  0: { label: '待接单', tone: 'warning' },
  1: { label: '已接单', tone: 'primary' },
  2: { label: '配送中', tone: 'info' },
  3: { label: '已完成', tone: 'success' },
  4: { label: '已取消', tone: 'default' },
  5: { label: '退款中', tone: 'danger' },
};

const loadRecentOrders = () => {
  if (userInfo.value.type !== 'mer') return;
  fetch(OrderApi.getOrderList, {
    usrId: userInfo.value.id,
    state: -1,
    timeOrder: -1,
    pageSize: 5,
  }).then((data) => {
    const list = data?.merList || data?.list || [];
    recentOrders.value = list.slice(0, 5).map(o => {
      const status = statusMap[o.state] || { label: '未知', tone: 'default' };
      return {
        id: o.id || o.orderId,
        customer: o.userName || o.usrName || '匿名顾客',
        items: o.goods?.map(g => g.name).join('、') || '订单详情',
        amount: o.amount || o.totalPrice || 0,
        statusLabel: status.label,
        tone: status.tone,
      };
    });
  }).catch(() => {
    recentOrders.value = [];
  });
};

// ==================== 加载仪表盘 ====================
const loadDashboard = () => {
  fetch(Ops.merchantDashboard, { range: trendRange.value }).then((data) => {
    if (data?.trend) {
      trendData.value = data.trend;
    } else {
      // 兜底 mock 数据
      trendData.value = Array.from({ length: trendRange.value }, (_, i) => {
        const d = new Date();
        d.setDate(d.getDate() - (trendRange.value - 1 - i));
        return {
          date: d.toISOString().slice(0, 10),
          gmv: Math.floor(Math.random() * 2000 + 800),
        };
      });
    }
    if (data?.summary) {
      todayGmv.value = data.summary.todayGmv ?? 0;
      totalGoods.value = data.summary.totalGoods ?? 0;
    }
  }).catch(() => {
    // 后端未就绪时使用本地 mock
    trendData.value = Array.from({ length: trendRange.value }, (_, i) => {
      const d = new Date();
      d.setDate(d.getDate() - (trendRange.value - 1 - i));
      return {
        date: d.toISOString().slice(0, 10),
        gmv: Math.floor(Math.random() * 2000 + 800),
      };
    });
  });
};

const loadPendingCounts = () => {
  if (userInfo.value.type !== 'mer') return;
  fetch(OrderApi.getOrderList, {
    usrId: userInfo.value.id,
    state: 0,
    timeOrder: 1,
  }).then((data) => {
    pendingOrderCount.value = data?.merList?.length || 0;
    buildTodoList();
  });
  fetch(AfterSale.stats, { scope: 'merchant' }).then((data) => {
    pendingTicketCount.value = Number(data?.stats?.pending || 0);
    buildTodoList();
  });
};

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 60000);
  loadDashboard();
  loadPendingCounts();
  loadRecentOrders();
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<style lang="less" scoped>
.merchant-dashboard {
  padding: 0 0 24px;
}

// ============ Hero 区 ============
.dashboard-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 4px 24px;
  gap: 24px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--primary);
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  font-weight: 600;
}

.dashboard-hero h2 {
  margin: 0;
  font-size: 32px;
  font-family: "Georgia", "Times New Roman", serif;
  color: var(--text-primary);
}

.hero-desc {
  max-width: 720px;
  margin: 10px 0 0;
  color: fade(#1a1d21, 72%);
  line-height: 1.8;
  font-size: 14px;
}

.hero-time {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--border);
  border-radius: 999px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;

  i { color: var(--primary); }
}

// ============ 统计卡片 ============
.stat-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 22px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 90px;
    height: 90px;
    border-radius: 50%;
    opacity: 0.08;
    transform: translate(30%, -30%);
    transition: all 0.3s ease;
  }

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  }

  &--primary::before { background: var(--primary); }
  &--success::before { background: var(--success); }
  &--warning::before { background: var(--warning); }
  &--danger::before  { background: var(--danger); }
}

.stat-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;

  .stat-card--primary & { background: var(--primary-light); color: var(--primary); }
  .stat-card--success & { background: var(--success-light); color: var(--success); }
  .stat-card--warning & { background: var(--warning-light); color: darken(#FDCB6E, 15%); }
  .stat-card--danger  & { background: var(--danger-light); color: var(--danger); }
}

.stat-card-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  font-family: "Georgia", "Times New Roman", serif;
  line-height: 1.2;
}

.stat-card-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
  font-weight: 500;
}

.stat-card-foot {
  font-size: 12px;
  color: var(--text-muted);
  border-top: 1px dashed var(--border);
  padding-top: 10px;
  margin-top: 4px;
}

// ============ 双栏 Grid ============
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.dashboard-grid--third {
  grid-template-columns: 1fr 1.6fr;
}

@media (max-width: 1100px) {
  .dashboard-grid,
  .dashboard-grid--third {
    grid-template-columns: 1fr;
  }
}

// ============ Panel 通用 ============
.panel {
  padding: 22px 24px;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;

  h3 {
    margin: 4px 0 0;
    font-size: 18px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary);
  }
}

// ============ 趋势图（纯 CSS 条形） ============
.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 180px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.trend-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.trend-bar-wrap {
  width: 100%;
  height: 110px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.trend-bar {
  width: 70%;
  min-height: 6px;
  background: linear-gradient(180deg, var(--primary), lighten(#E8652B, 12%));
  border-radius: 6px 6px 2px 2px;
  transition: height 0.4s ease;
  position: relative;

  &:hover {
    filter: brightness(1.1);
  }
}

.trend-date {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 600;
}

.trend-val {
  font-size: 11px;
  color: var(--text-secondary);
  font-weight: 700;
}

// ============ 快捷操作 ============
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.quick-action {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 14px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.2s ease;
  align-items: flex-start;
  text-align: left;

  i {
    font-size: 20px;
    margin-bottom: 4px;
  }

  span {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
  }

  small {
    font-size: 11px;
    color: var(--text-muted);
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  }

  &--primary i { color: var(--primary); }
  &--success i { color: var(--success); }
  &--warning i { color: darken(#FDCB6E, 15%); }
  &--danger  i { color: var(--danger); }
  &--info    i { color: var(--info); }
  &--default i { color: var(--text-secondary); }
}

// ============ 待办 ============
.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid var(--border);
  font-size: 13px;
}

.todo-text {
  flex: 1;
  color: var(--text-primary);
  font-weight: 500;
}

.todo-time {
  font-size: 11px;
  color: var(--text-muted);
}

// ============ 最近订单 ============
.recent-orders {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-order {
  display: grid;
  grid-template-columns: 70px 1fr 90px 90px;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--border);
  border-radius: 12px;
  font-size: 13px;

  .recent-order-id {
    color: var(--text-muted);
    font-weight: 600;
    font-family: "Consolas", monospace;
  }

  .recent-order-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;

    strong { color: var(--text-primary); font-weight: 600; }
    span {
      color: var(--text-muted);
      font-size: 11px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .recent-order-amount {
    color: var(--primary);
    font-weight: 700;
    text-align: right;
  }
}

// ============ 空态 ============
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 0;
  color: var(--text-muted);
  font-size: 13px;

  i { font-size: 28px; opacity: 0.4; }
}
</style>
