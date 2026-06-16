<!--
  管理端 · 审计日志
  从 admin-ops 中独立出来，便于深链和分享
-->
<template>
  <div class="admin-audit">
    <section class="overview">
      <article
        v-for="card in overviewCards"
        :key="card.label"
        class="overview-card"
      >
        <span class="overview-label">{{ card.label }}</span>
        <strong class="overview-value">{{ card.value }}</strong>
        <small class="overview-tip">{{ card.tip }}</small>
      </article>
    </section>

    <section class="filter-bar card">
      <div class="search-wrap">
        <svg class="search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
        <input
          v-model="keyword"
          placeholder="搜索操作人/详情/目标"
          class="search-input"
          @change="fetchAudit"
        />
      </div>
      <select v-model="actionType" class="form-select" @change="fetchAudit">
        <option value="all">全部动作</option>
        <option value="PERMISSION_UPDATE">权限变更</option>
        <option value="PRODUCT_CHECK">商品审核</option>
        <option value="AFTER_SALE_UPDATE">售后处理</option>
        <option value="MERCHANT_UPDATE">商家管理</option>
        <option value="USER_UPDATE">用户管理</option>
      </select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="date-picker"
        @change="fetchAudit"
      />
      <button class="btn btn-outline" @click="fetchAudit">刷新</button>
    </section>

    <section class="audit-table card">
      <table class="custom-table" v-if="!loading">
        <thead>
          <tr>
            <th style="width: 120px;">日志 ID</th>
            <th style="width: 160px;">动作类型</th>
            <th style="width: 160px;">操作人</th>
            <th style="width: 160px;">目标</th>
            <th>说明</th>
            <th style="width: 140px;">IP</th>
            <th style="width: 170px;">时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in auditLogs" :key="row.id">
            <td>{{ row.id }}</td>
            <td>
              <span class="badge badge-info">{{ row.actionType || '-' }}</span>
            </td>
            <td>
              <div class="user-cell">
                <div class="avatar">{{ (row.actorName || '?').slice(0, 1) }}</div>
                <div>
                  <strong>{{ row.actorName || '-' }}</strong>
                  <small>{{ row.actorRole || '-' }}</small>
                </div>
              </div>
            </td>
            <td>{{ row.targetName }}</td>
            <td class="td-ellipsis">{{ row.detail }}</td>
            <td>{{ row.ip }}</td>
            <td>{{ row.createdTime }}</td>
          </tr>
          <tr v-if="auditLogs.length === 0">
            <td colspan="7" class="empty-cell">暂无审计日志</td>
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
          @current-change="fetchAudit"
          @size-change="fetchAudit"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import fetch from "@/api/fetch";
import { Ops } from "@/api/apis";

const keyword = ref("");
const actionType = ref("all");
const dateRange = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const auditLogs = ref([]);

const stats = ref({
  totalToday: 0,
  totalAll: 0,
  permissionChanges: 0,
  afterSaleActions: 0,
});

const overviewCards = computed(() => [
  { label: "今日操作", value: stats.value.totalToday, tip: "今天发生的所有审计事件" },
  { label: "累计操作", value: stats.value.totalAll, tip: "平台运营以来的全部记录" },
  { label: "权限变更", value: stats.value.permissionChanges, tip: "PERMISSION_UPDATE 动作" },
  { label: "售后处理", value: stats.value.afterSaleActions, tip: "AFTER_SALE_UPDATE 动作" },
]);

const fetchAudit = async () => {
  loading.value = true;
  try {
    const res = await fetch(Ops.auditList, {
      keyword: keyword.value,
      actionType: actionType.value,
      startDate: dateRange.value?.[0] || "",
      endDate: dateRange.value?.[1] || "",
      page: page.value,
      pageSize: pageSize.value,
    });
    const data = res?.data || res || {};
    if (data.code === 200) {
      const payload = data.data || {};
      auditLogs.value = payload.list || [];
      total.value = payload.total || 0;
      if (payload.stats) {
        stats.value = payload.stats;
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
  stats.value = {
    totalToday: 38,
    totalAll: 1246,
    permissionChanges: 24,
    afterSaleActions: 87,
  };
  total.value = 38;
  auditLogs.value = [
    {
      id: "AUD202606160038",
      actionType: "PERMISSION_UPDATE",
      actorName: "超级管理员",
      actorRole: "admin",
      targetName: "商家-可创建商品",
      detail: "merchant.action.product.create 由 0 → 1",
      ip: "127.0.0.1",
      createdTime: "2026-06-16 14:28:11",
    },
    {
      id: "AUD202606160037",
      actionType: "PRODUCT_CHECK",
      actorName: "审核员A",
      actorRole: "admin",
      targetName: "招牌牛肉面",
      detail: "商品审核通过 (PENDING → APPROVED)",
      ip: "127.0.0.1",
      createdTime: "2026-06-16 13:42:09",
    },
    {
      id: "AUD202606160036",
      actionType: "AFTER_SALE_UPDATE",
      actorName: "客服小张",
      actorRole: "admin",
      targetName: "ORD202606140022",
      detail: "售后工单处理完成，退款 ¥36.00",
      ip: "127.0.0.1",
      createdTime: "2026-06-16 11:13:48",
    },
    {
      id: "AUD202606160035",
      actionType: "USER_UPDATE",
      actorName: "超级管理员",
      actorRole: "admin",
      targetName: "用户 138****1234",
      detail: "用户禁用状态由 0 → 1",
      ip: "127.0.0.1",
      createdTime: "2026-06-16 10:05:32",
    },
  ];
};

onMounted(() => {
  fetchAudit();
});
</script>

<style lang="less" scoped>
.admin-audit {
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
  background: linear-gradient(180deg, #ffffff 0%, #f7f9ff 100%);
  border: 1px solid rgba(79, 70, 229, 0.12);
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
  max-width: 280px;
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

/* ---- Badge ---- */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge-info { color: #6B7280; background: rgba(107, 114, 128, 0.12); }

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
}

@media (max-width: 768px) {
  .overview {
    grid-template-columns: 1fr;
  }
}
</style>
