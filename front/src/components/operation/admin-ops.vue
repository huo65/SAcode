<template>
  <div class="ops-admin">
    <section class="ops-hero">
      <div>
        <p class="eyebrow">平台运营</p>
        <h3>平台经营与治理中枢</h3>
        <p class="hero-desc">
          将平台 GMV、订单趋势、角色规模、权限配置与审计留痕集中到一个界面，帮助管理端快速完成数据查看和权限治理。
        </p>
      </div>
      <div class="hero-actions">
        <button class="btn btn-ghost" @click="refreshAll">刷新看板</button>
        <span class="badge badge-warning">轻量 RBAC 即时生效</span>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="card in overviewCards" :key="card.label" class="metric-card">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.tip }}</small>
      </article>
    </section>

    <section class="content-grid">
      <article class="panel trend-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">平台趋势</p>
            <h4>近 7 天平台交易走势</h4>
          </div>
          <div class="mini-pills">
            <span>GMV</span>
            <span>订单量</span>
          </div>
        </div>
        <div class="trend-chart">
          <div
            v-for="item in dailyTrend"
            :key="item.date"
            class="trend-item"
          >
            <div class="trend-bars">
              <div
                class="bar gmv-bar"
                :style="{ height: `${resolveBarHeight(item.gmv, maxGmv)}%` }"
              />
              <div
                class="bar order-bar"
                :style="{ height: `${resolveBarHeight(item.orders, maxOrders)}%` }"
              />
            </div>
            <div class="trend-meta">
              <strong>{{ item.gmv }}</strong>
              <span>{{ item.orders }} 单</span>
              <small>{{ formatDate(item.date) }}</small>
            </div>
          </div>
        </div>
      </article>

      <article class="panel role-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">角色分布</p>
            <h4>当前角色与治理分布</h4>
          </div>
        </div>
        <div class="role-grid">
          <div v-for="card in roleCards" :key="card.label" class="role-card">
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
          </div>
        </div>
        <table class="custom-table">
          <thead>
            <tr>
              <th>订单状态</th>
              <th style="width: 90px;">数量</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in stateDistribution" :key="item.label">
              <td>{{ item.label }}</td>
              <td>{{ item.count }}</td>
            </tr>
            <tr v-if="stateDistribution.length === 0">
              <td colspan="2" class="empty-cell">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </article>
    </section>

    <section class="content-grid">
      <article class="panel permission-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">权限矩阵</p>
            <h4>角色-菜单-动作权限配置</h4>
          </div>
          <span class="badge badge-success">修改后立即影响菜单与关键接口</span>
        </div>

        <div class="permission-groups">
          <div
            v-for="group in roleGroups"
            :key="group.roleCode"
            class="permission-group"
          >
            <div class="group-head">
              <div>
                <h5>{{ group.roleName }}</h5>
                <p>{{ group.roleCode === 'admin' ? '平台治理角色' : '门店运营角色' }}</p>
              </div>
              <span class="badge badge-primary">{{ enabledCount(group.permissions) }}/{{ group.permissions.length }}</span>
            </div>
            <div class="permission-list">
              <label
                v-for="item in group.permissions"
                :key="`${group.roleCode}-${item.permissionKey}`"
                class="permission-item"
              >
                <div>
                  <strong>{{ item.permissionName }}</strong>
                  <span>{{ item.permissionKey }}</span>
                </div>
                <label class="toggle-switch">
                  <input
                    type="checkbox"
                    :checked="item.enabled === 1"
                    @change="(e) => handlePermissionChange(group.roleCode, item, e.target.checked)"
                  />
                  <span class="toggle-slider"></span>
                </label>
              </label>
            </div>
          </div>
        </div>
      </article>

      <article class="panel audit-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">审计日志</p>
            <h4>关键操作审计日志</h4>
          </div>
          <div class="audit-search-wrap">
            <svg class="search-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            <input
              v-model="auditKeyword"
              placeholder="搜索操作人/详情"
              class="audit-search"
              @change="fetchAuditLogs"
            />
          </div>
        </div>
        <table class="custom-table">
          <thead>
            <tr>
              <th style="width: 160px;">动作</th>
              <th style="width: 120px;">操作人</th>
              <th style="width: 140px;">目标</th>
              <th>说明</th>
              <th style="width: 180px;">时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in auditLogs" :key="item.id || item.createdTime">
              <td>
                <span class="badge badge-info">{{ item.actionType }}</span>
              </td>
              <td>{{ item.actorName }}</td>
              <td>{{ item.targetName }}</td>
              <td class="td-ellipsis">{{ item.detail }}</td>
              <td>{{ item.createdTime }}</td>
            </tr>
            <tr v-if="auditLogs.length === 0">
              <td colspan="5" class="empty-cell">暂无审计日志</td>
            </tr>
          </tbody>
        </table>
      </article>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <p class="panel-kicker">近期订单</p>
          <h4>最近订单样本</h4>
        </div>
      </div>
      <table class="custom-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th style="width: 120px;">状态</th>
            <th style="width: 100px;">金额</th>
            <th style="width: 90px;">件数</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in recentOrders" :key="item.orderId">
            <td>{{ item.orderId }}</td>
            <td>{{ item.stateLabel }}</td>
            <td>{{ item.amount }}</td>
            <td>{{ item.itemCount }}</td>
            <td>{{ item.time }}</td>
          </tr>
          <tr v-if="recentOrders.length === 0">
            <td colspan="5" class="empty-cell">暂无订单数据</td>
          </tr>
        </tbody>
      </table>
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
const roleGroups = ref([]);
const auditLogs = ref([]);
const auditKeyword = ref("");

const overviewCards = computed(() => {
  const overview = dashboard.value?.overview || {};
  return [
    {
      label: "平台 GMV",
      value: `¥${overview.gmv || 0}`,
      tip: "按有效支付订单汇总",
    },
    {
      label: "有效订单",
      value: overview.paidOrders || 0,
      tip: "已支付、备餐、待配送、配送中、已完成",
    },
    {
      label: "待处理售后",
      value: overview.pendingAfterSale || 0,
      tip: "便于串联平台治理与售后闭环",
    },
    {
      label: "活跃商家",
      value: overview.activeMerchants || 0,
      tip: "基于当前用户角色统计",
    },
    {
      label: "顾客规模",
      value: overview.customers || 0,
      tip: "当前顾客账号数量",
    },
    {
      label: "审核通过商品",
      value: overview.approvedProducts || 0,
      tip: "平台可售商品总量",
    },
  ];
});

const roleCards = computed(() => {
  const roleStats = dashboard.value?.roleStats || {};
  const permissionSummary = dashboard.value?.permissionSummary || {};
  return [
    { label: "管理员", value: roleStats.admin || 0 },
    { label: "商家", value: roleStats.merchant || 0 },
    { label: "顾客", value: roleStats.customer || 0 },
    { label: "骑手", value: roleStats.driver || 0 },
    { label: "管理员菜单", value: permissionSummary.adminMenus || 0 },
    { label: "商家动作", value: permissionSummary.merchantActions || 0 },
  ];
});

const dailyTrend = computed(() => dashboard.value?.dailyTrend || []);
const stateDistribution = computed(
  () => dashboard.value?.orderStateDistribution || []
);
const recentOrders = computed(() => dashboard.value?.recentOrders || []);
const maxGmv = computed(() =>
  Math.max(...dailyTrend.value.map((item) => Number(item.gmv || 0)), 1)
);
const maxOrders = computed(() =>
  Math.max(...dailyTrend.value.map((item) => Number(item.orders || 0)), 1)
);

const refreshAll = () => {
  fetch(Ops.adminDashboard).then((data) => {
    dashboard.value = data || {};
  });
  fetch(Ops.permissionList).then((data) => {
    roleGroups.value = data?.roleGroups || [];
  });
  fetchAuditLogs();
};

const fetchAuditLogs = () => {
  fetch(Ops.auditList, { keyword: auditKeyword.value }).then((data) => {
    auditLogs.value = data?.auditLogs || [];
  });
};

const handlePermissionChange = (roleCode, item, enabled) => {
  fetch(Ops.permissionUpdate, {
    roleCode,
    permissionKey: item.permissionKey,
    enabled: enabled ? 1 : 0,
  })
    .then(() => {
      ElMessage.success("权限配置已更新");
      refreshAll();
      window.dispatchEvent(new CustomEvent("ops-permission-updated"));
    })
    .catch(() => {
      refreshAll();
    });
};

const enabledCount = (permissions = []) =>
  permissions.filter((item) => item.enabled === 1).length;

const resolveBarHeight = (value, max) => {
  if (!max) return 8;
  return Math.max(12, Math.round((Number(value || 0) / max) * 100));
};

const formatDate = (date) => (date || "").slice(5);

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
.ops-admin {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ops-hero,
.panel,
.metric-card,
.role-card,
.permission-group {
  border: 1px solid rgba(18, 31, 54, 0.08);
  box-shadow: 0 18px 48px rgba(34, 73, 140, 0.08);
}

.ops-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(41, 122, 255, 0.28), transparent 30%),
    linear-gradient(135deg, #0f1b31 0%, #13284b 48%, #eef5ff 48%, #f7fbff 100%);
  color: #fff;
}

.eyebrow,
.panel-kicker {
  margin: 0 0 10px;
  font-size: 11px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

.ops-hero h3,
.panel h4,
.group-head h5 {
  margin: 0;
  font-family: "Georgia", "Times New Roman", serif;
}

.ops-hero h3 {
  font-size: 34px;
}

.hero-desc {
  max-width: 760px;
  margin: 12px 0 0;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.82);
}

.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.metric-grid,
.content-grid,
.role-grid {
  display: grid;
  gap: 16px;
}

.metric-grid {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.content-grid {
  grid-template-columns: 1.35fr 1fr;
}

.metric-card {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, #ffffff 0%, #f4f8ff 100%);
}

.metric-card span,
.role-card span,
.permission-item span {
  display: block;
  color: rgba(18, 31, 54, 0.58);
}

.metric-card strong,
.role-card strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
  color: #101a2a;
}

.metric-card small {
  display: block;
  margin-top: 8px;
  color: rgba(18, 31, 54, 0.56);
  line-height: 1.6;
}

.panel {
  padding: 22px;
  border-radius: 28px;
  background: linear-gradient(180deg, #ffffff 0%, #f7faff 100%);
}

.panel-head,
.group-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.panel-kicker {
  color: #2f66d6;
}

.panel h4 {
  font-size: 24px;
  color: #101a2a;
}

.mini-pills {
  display: flex;
  gap: 8px;
}

.mini-pills span {
  padding: 8px 12px;
  border-radius: 999px;
  color: #24498c;
  background: rgba(47, 102, 214, 0.08);
}

.trend-chart {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
  min-height: 300px;
  margin-top: 18px;
}

.trend-item {
  padding: 12px 10px;
  border-radius: 20px;
  background: rgba(17, 43, 84, 0.04);
}

.trend-bars {
  display: flex;
  align-items: end;
  justify-content: center;
  gap: 10px;
  height: 180px;
}

.bar {
  width: 22px;
  border-radius: 999px 999px 12px 12px;
}

.gmv-bar {
  background: linear-gradient(180deg, #1f5fff 0%, #7eb8ff 100%);
}

.order-bar {
  background: linear-gradient(180deg, #0f1b31 0%, #6f8cc0 100%);
}

.trend-meta {
  margin-top: 12px;
  text-align: center;
}

.trend-meta strong,
.permission-item strong {
  display: block;
  color: #0f1b31;
}

.trend-meta span,
.trend-meta small {
  display: block;
  margin-top: 4px;
  color: rgba(15, 27, 49, 0.6);
}

.role-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin: 16px 0;
}

.role-card {
  padding: 16px;
  border-radius: 20px;
  background: rgba(20, 45, 89, 0.04);
}

.permission-groups {
  display: grid;
  gap: 16px;
  margin-top: 18px;
}

.permission-group {
  padding: 18px;
  border-radius: 22px;
  background: rgba(17, 43, 84, 0.03);
}

.group-head p {
  margin: 6px 0 0;
  color: rgba(16, 26, 42, 0.56);
}

.permission-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.permission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #fff;
  cursor: pointer;
}

/* ---- Toggle Switch (replaces el-switch) ---- */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background: #cbd5e1;
  border-radius: 999px;
  transition: background 0.3s;

  &::before {
    content: "";
    position: absolute;
    height: 18px;
    width: 18px;
    left: 3px;
    bottom: 3px;
    background: white;
    border-radius: 50%;
    transition: transform 0.3s;
  }
}

.toggle-switch input:checked + .toggle-slider {
  background: #4F46E5;
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

/* ---- Audit Search ---- */
.audit-search-wrap {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(0, 0, 0, 0.35);
  pointer-events: none;
}

.audit-search {
  width: 220px;
  padding: 8px 14px 8px 34px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 13px;
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

/* ---- Custom Table ---- */
.custom-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  margin-top: 16px;

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
  }

  tr:hover td {
    background: rgba(79, 70, 229, 0.03);
  }
}

.td-ellipsis {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-cell {
  text-align: center;
  color: rgba(0, 0, 0, 0.35);
  padding: 30px 12px !important;
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

.btn-ghost {
  background: rgba(255, 255, 255, 0.86);
  color: #13284b;
  border: none;

  &:hover {
    background: #fff;
  }
}

/* ---- Responsive ---- */
@media (max-width: 1400px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .ops-hero,
  .content-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-actions {
    align-items: flex-start;
  }
}

@media (max-width: 900px) {
  .metric-grid,
  .role-grid,
  .trend-chart {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
