<template>
  <div class="after-sale-page">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card orange">
        <div class="stat-icon orange"><i class="fas fa-ticket-alt"></i></div>
        <div class="stat-label">总工单</div>
        <div class="stat-value">{{ stats.total || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-icon red"><i class="fas fa-exclamation-circle"></i></div>
        <div class="stat-label">待处理</div>
        <div class="stat-value">{{ stats.pending || 0 }}</div>
      </div>
      <div class="stat-card amber">
        <div class="stat-icon amber"><i class="fas fa-spinner"></i></div>
        <div class="stat-label">处理中</div>
        <div class="stat-value">{{ stats.processing || 0 }}</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-icon blue"><i class="fas fa-undo-alt"></i></div>
        <div class="stat-label">退款类</div>
        <div class="stat-value">{{ stats.refund || 0 }}</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="card filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">工单状态</label>
          <select v-model="filters.status" class="filter-select" @change="loadTickets">
            <option value="">全部状态</option>
            <option value="待处理">待处理</option>
            <option value="处理中">处理中</option>
            <option value="已解决">已解决</option>
            <option value="已关闭">已关闭</option>
          </select>
        </div>
        <div class="filter-item">
          <label class="filter-label">问题类型</label>
          <select v-model="filters.type" class="filter-select" @change="loadTickets">
            <option value="">全部类型</option>
            <option value="投诉反馈">投诉反馈</option>
            <option value="退款问题">退款问题</option>
            <option value="配送问题">配送问题</option>
            <option value="商品问题">商品问题</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-outline" @click="resetFilters"><i class="fas fa-undo"></i> 重置</button>
          <button class="btn btn-primary" @click="loadTickets"><i class="fas fa-sync-alt"></i> 刷新</button>
        </div>
      </div>
    </div>

    <!-- 工单表格 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title"><i class="fas fa-clipboard-list"></i> 售后工单</div>
        <span class="card-hint">点击工单行查看详情与处理记录</span>
      </div>
      <div class="tbl-wrap">
        <table v-if="ticketList.length > 0">
          <thead>
            <tr>
              <th>工单号</th>
              <th>订单号</th>
              <th>问题类型</th>
              <th>顾客</th>
              <th>商家</th>
              <th>状态</th>
              <th>金额</th>
              <th>更新时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in ticketList" :key="row.id" @click="openTicket(row)">
              <td class="td-id">{{ row.id }}</td>
              <td>{{ row.orderId }}</td>
              <td>{{ row.type }}</td>
              <td>{{ row.customerName || '-' }}</td>
              <td>{{ row.merchantName || '-' }}</td>
              <td><span class="badge" :class="getStatusBadgeClass(row.status)">{{ row.status }}</span></td>
              <td class="td-price">¥{{ row.orderAmount || 0 }}</td>
              <td class="td-time">{{ row.updatedTime || '-' }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state">
          <div class="empty-icon"><i class="fas fa-inbox"></i></div>
          <p class="empty-text">暂无工单数据</p>
        </div>
      </div>
    </div>

    <!-- 工单详情弹窗 -->
    <div v-if="drawerVisible" class="modal-overlay show" @click.self="drawerVisible = false">
      <div class="modal modal-lg">
        <div class="modal-header">
          <h3>售后工单详情</h3>
          <button class="modal-close" @click="drawerVisible = false"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body" v-if="activeTicket">
          <div class="detail-meta">
            <div class="meta-card">
              <div class="meta-label">工单状态</div>
              <span class="badge" :class="getStatusBadgeClass(activeTicket.status)">{{ activeTicket.status }}</span>
            </div>
            <div class="meta-card">
              <div class="meta-label">问题类型</div>
              <strong>{{ activeTicket.type }}</strong>
            </div>
            <div class="meta-card">
              <div class="meta-label">关联订单</div>
              <strong>{{ activeTicket.orderId }}</strong>
            </div>
          </div>

          <div class="detail-info">
            <div class="info-row">
              <span class="info-label">问题描述</span>
              <span class="info-value">{{ activeTicket.content || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">顾客 / 商家</span>
              <span class="info-value">{{ activeTicket.customerName || '-' }} / {{ activeTicket.merchantName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">地址</span>
              <span class="info-value">{{ activeTicket.receiveAddress || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">处理备注</span>
              <span class="info-value">{{ activeTicket.handlerNote || '尚未填写' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">处理人</span>
              <span class="info-value">{{ activeTicket.handlerName || '待分配' }}</span>
            </div>
          </div>

          <!-- 处理表单 -->
          <div v-if="canHandle" class="handle-section">
            <h4 class="handle-title">更新处理状态</h4>
            <div class="form-group">
              <label class="form-label">新状态</label>
              <select v-model="handleForm.status" class="form-select">
                <option value="待处理">待处理</option>
                <option value="处理中">处理中</option>
                <option value="已解决">已解决</option>
                <option v-if="isAdmin" value="已关闭">已关闭</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">处理备注</label>
              <textarea
                v-model="handleForm.handlerNote"
                class="form-textarea"
                rows="4"
                maxlength="300"
                placeholder="填写本次处理结果，例如退款说明、补偿方案或沟通记录"
              ></textarea>
              <div class="form-counter">{{ (handleForm.handlerNote || '').length }}/300</div>
            </div>
            <button class="btn btn-primary" @click="submitHandle">保存处理结果</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { AfterSale } from "@/api/apis";
import fetch from "@/api/fetch";
import $store, { userInfo } from "@/store";

const props = defineProps({
  scope: {
    type: String,
    default: "",
  },
});

const filters = reactive({
  status: "",
  type: "",
});

const ticketList = ref([]);
const stats = ref({});
const drawerVisible = ref(false);
const activeTicket = ref(null);
const handleForm = reactive({
  status: "处理中",
  handlerNote: "",
});

const resolvedScope = computed(() => {
  if (props.scope) return props.scope;
  if (userInfo.value.type === "admin") return "admin";
  if (userInfo.value.type === "mer") return "merchant";
  return "customer";
});

const isAdmin = computed(() => userInfo.value.type === "admin");
const canHandle = computed(() => userInfo.value.type === "admin" || userInfo.value.type === "mer");

const getStatusBadgeClass = (status) => {
  const map = {
    待处理: "badge-danger",
    处理中: "badge-warning",
    已解决: "badge-success",
    已关闭: "badge-info",
  };
  return map[status] || "badge-info";
};

const loadTickets = () => {
  fetch(AfterSale.list, {
    scope: resolvedScope.value,
    status: filters.status,
    type: filters.type,
  }).then((data) => {
    ticketList.value = data?.ticketList || [];
  });
  fetch(AfterSale.stats, { scope: resolvedScope.value }).then((data) => {
    stats.value = data?.stats || {};
  });
};

const resetFilters = () => {
  filters.status = "";
  filters.type = "";
  loadTickets();
};

const openTicket = (row) => {
  activeTicket.value = row;
  handleForm.status = row.status || "处理中";
  handleForm.handlerNote = row.handlerNote || "";
  drawerVisible.value = true;
};

const submitHandle = () => {
  if (!activeTicket.value?.id) {
    ElMessage.error("请选择工单");
    return;
  }
  if (!handleForm.handlerNote.trim()) {
    ElMessage.error("请填写处理备注");
    return;
  }
  fetch(AfterSale.update, {
    id: activeTicket.value.id,
    status: handleForm.status,
    handlerNote: handleForm.handlerNote.trim(),
  }).then(() => {
    ElMessage.success("工单处理结果已保存");
    drawerVisible.value = false;
    loadTickets();
  });
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "AfterSale",
    fn: loadTickets,
  });
  loadTickets();
});
</script>

<style lang="less" scoped>
@import "@/style/theme.less";

.after-sale-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}
.stat-card {
  background: var(--card);
  border-radius: var(--radius);
  padding: 20px 22px;
  border: 1px solid var(--border);
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
  }
  &.orange::after { background: var(--primary); }
  &.red::after { background: var(--danger); }
  &.amber::after { background: #F59E0B; }
  &.blue::after { background: var(--info); }
}
.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  margin-bottom: 12px;
  &.orange { background: var(--primary-light); color: var(--primary); }
  &.red { background: var(--danger-light); color: var(--danger); }
  &.amber { background: var(--warning-light); color: #D69E00; }
  &.blue { background: var(--info-light); color: var(--info); }
}
.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 4px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
}

/* ===== 筛选栏 ===== */
.filter-bar {
  padding: 18px 22px;
}
.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
}
.filter-select {
  height: 38px;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 13px;
  background: var(--bg);
  color: var(--text-primary);
  outline: none;
  min-width: 160px;
  transition: all 0.2s ease;
  &:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.08);
  }
}
.filter-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* ===== 通用按钮 ===== */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  outline: none;
  white-space: nowrap;
}
.btn-primary {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
  &:hover { background: var(--primary-hover); box-shadow: 0 4px 12px rgba(232, 101, 43, 0.3); }
}
.btn-outline {
  background: transparent;
  color: var(--text-secondary);
  border-color: var(--border);
  &:hover { border-color: var(--primary); color: var(--primary); }
}

/* ===== 卡片 ===== */
.card {
  background: var(--card);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  overflow: hidden;
}
.card-header {
  padding: 18px 22px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  i { color: var(--primary); }
}
.card-hint {
  font-size: 12px;
  color: var(--text-muted);
}

/* ===== 表格 ===== */
.tbl-wrap {
  overflow-x: auto;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th {
  text-align: left;
  padding: 12px 16px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border);
  background: var(--bg);
}
td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
  color: var(--text-primary);
  vertical-align: middle;
}
tr {
  cursor: pointer;
  transition: all 0.15s ease;
  &:hover td {
    background: rgba(232, 101, 43, 0.03);
  }
  &:last-child td {
    border-bottom: none;
  }
}
.td-id {
  font-weight: 600;
  color: var(--primary);
}
.td-price {
  font-weight: 700;
  color: var(--price, var(--primary));
}
.td-time {
  color: var(--text-muted);
  font-size: 12px;
}

/* ===== 徽标 ===== */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.badge-danger { background: var(--danger-light); color: #991B1B; }
.badge-warning { background: var(--warning-light); color: #92600A; }
.badge-success { background: var(--success-light); color: #065F46; }
.badge-info { background: var(--info-light); color: #075CAA; }

/* ===== 详情弹窗 ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
}
.modal-overlay.show { display: flex; }
.modal {
  background: var(--card);
  border-radius: 16px;
  width: 520px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
}
.modal-lg { width: 600px; }
.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  h3 {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0;
  }
}
.modal-close {
  width: 32px; height: 32px; border-radius: 8px; border: none;
  background: var(--bg); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-muted); transition: all 0.2s ease;
  &:hover { background: var(--danger-light); color: var(--danger); }
}
.modal-body { padding: 24px; }
.modal-footer {
  padding: 16px 24px; border-top: 1px solid var(--border);
  display: flex; align-items: center; justify-content: flex-end; gap: 10px;
}

/* ===== 详情内部 ===== */
.detail-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.meta-card {
  padding: 14px;
  border-radius: 10px;
  background: var(--bg);
  border: 1px solid var(--border);
}
.meta-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.meta-card strong {
  font-size: 16px;
  color: var(--text-primary);
}
.detail-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}
.info-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  font-size: 13px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  &:last-child { border-bottom: none; }
}
.info-label {
  color: var(--text-muted);
  font-weight: 500;
  min-width: 90px;
  flex-shrink: 0;
}
.info-value {
  color: var(--text-primary);
}

/* ===== 处理表单 ===== */
.handle-section {
  padding: 20px;
  border-radius: 12px;
  background: var(--bg);
  border: 1px solid var(--border);
}
.handle-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px;
}
.form-group { margin-bottom: 16px; }
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.form-select {
  width: 100%;
  height: 38px;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 13px;
  background: var(--card);
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s ease;
  &:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.08);
  }
}
.form-textarea {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--card);
  outline: none;
  resize: vertical;
  box-sizing: border-box;
  transition: all 0.2s ease;
  &:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.08);
  }
}
.form-counter {
  text-align: right;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 20px;
  color: var(--text-muted);
  .empty-icon { font-size: 40px; margin-bottom: 12px; opacity: 0.4; }
  .empty-text { font-size: 14px; font-weight: 600; color: var(--text-secondary); margin: 0; }
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .filter-row { flex-direction: column; }
  .filter-item { width: 100%; }
  .filter-select { width: 100%; }
  .filter-actions { margin-left: 0; width: 100%; }
  .detail-meta { grid-template-columns: 1fr; }
  .modal-lg { width: calc(100vw - 32px); }
}
</style>
