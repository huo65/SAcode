<template>
  <div class="after-sale">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">Service Recovery Desk</p>
        <h2>{{ pageTitle }}</h2>
        <p class="hero-desc">
          以课堂展示为目标，把投诉、退款、配送问题统一收口到一张售后工单台，让处理链路和结果反馈一眼可见。
        </p>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span>总工单</span>
          <strong>{{ stats.total || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>待处理</span>
          <strong>{{ stats.pending || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>处理中</span>
          <strong>{{ stats.processing || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>退款类</span>
          <strong>{{ stats.refund || 0 }}</strong>
        </div>
      </div>
    </section>

    <section class="filter-bar">
      <el-select v-model="filters.status" clearable placeholder="工单状态">
        <el-option label="待处理" value="待处理" />
        <el-option label="处理中" value="处理中" />
        <el-option label="已解决" value="已解决" />
        <el-option label="已关闭" value="已关闭" />
      </el-select>
      <el-select v-model="filters.type" clearable placeholder="工单类型">
        <el-option label="投诉反馈" value="投诉反馈" />
        <el-option label="退款问题" value="退款问题" />
        <el-option label="配送问题" value="配送问题" />
        <el-option label="商品问题" value="商品问题" />
      </el-select>
      <div class="filter-actions">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="loadTickets">刷新工单</el-button>
      </div>
    </section>

    <section class="board">
      <div class="board-head">
        <div>
          <p class="eyebrow">Ticket Queue</p>
          <h3>售后处理台</h3>
        </div>
        <span class="board-tip">点击某条工单可查看关联订单与处理记录</span>
      </div>

      <el-table :data="ticketList" stripe class="ticket-table" @row-click="openTicket">
        <el-table-column prop="id" label="工单号" min-width="130" />
        <el-table-column prop="orderId" label="订单号" min-width="120" />
        <el-table-column prop="type" label="问题类型" min-width="110" />
        <el-table-column prop="customerName" label="顾客" min-width="110" />
        <el-table-column prop="merchantName" label="商家" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status] || 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" min-width="90">
          <template #default="{ row }">￥{{ row.orderAmount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="更新时间" min-width="170" />
      </el-table>
    </section>

    <el-drawer v-model="drawerVisible" title="售后工单详情" size="46%">
      <template v-if="activeTicket">
        <div class="drawer-panel">
          <div class="drawer-meta">
            <div class="meta-card">
              <span>工单状态</span>
              <strong>{{ activeTicket.status }}</strong>
            </div>
            <div class="meta-card">
              <span>问题类型</span>
              <strong>{{ activeTicket.type }}</strong>
            </div>
            <div class="meta-card">
              <span>关联订单</span>
              <strong>{{ activeTicket.orderId }}</strong>
            </div>
          </div>

          <el-descriptions :column="1" border class="ticket-detail">
            <el-descriptions-item label="问题描述">
              {{ activeTicket.content || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="顾客 / 商家">
              {{ activeTicket.customerName || "-" }} / {{ activeTicket.merchantName || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="地址">
              {{ activeTicket.receiveAddress || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="处理备注">
              {{ activeTicket.handlerNote || "尚未填写处理备注" }}
            </el-descriptions-item>
            <el-descriptions-item label="处理人">
              {{ activeTicket.handlerName || "待分配" }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="canHandle" class="handle-form">
            <div class="section-title">更新处理状态</div>
            <el-form label-width="88px">
              <el-form-item label="新状态">
                <el-select v-model="handleForm.status" style="width: 100%">
                  <el-option label="待处理" value="待处理" />
                  <el-option label="处理中" value="处理中" />
                  <el-option label="已解决" value="已解决" />
                  <el-option v-if="isAdmin" label="已关闭" value="已关闭" />
                </el-select>
              </el-form-item>
              <el-form-item label="处理备注">
                <el-input
                  v-model="handleForm.handlerNote"
                  type="textarea"
                  :rows="4"
                  maxlength="300"
                  show-word-limit
                  placeholder="填写本次处理结果，课堂展示时用于说明平台或商家已跟进"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitHandle">保存处理结果</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>
    </el-drawer>
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
const pageTitle = computed(() => {
  if (resolvedScope.value === "admin") return "课堂展示版平台售后总控台";
  if (resolvedScope.value === "merchant") return "课堂展示版商家售后处理台";
  return "我的售后工单";
});

const statusTagType = {
  待处理: "danger",
  处理中: "warning",
  已解决: "success",
  已关闭: "info",
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
.after-sale {
  --ink: #171d2d;
  --accent: #1c6cff;
  --accent-soft: rgba(28, 108, 255, 0.12);
  --panel: rgba(248, 250, 255, 0.88);
  color: var(--ink);
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.9fr);
  gap: 20px;
  padding: 30px;
  border-radius: 30px;
  background:
    radial-gradient(circle at top left, rgba(28, 108, 255, 0.22), transparent 34%),
    linear-gradient(135deg, #f6fbff 0%, #e4edff 100%);
  border: 1px solid rgba(23, 29, 45, 0.08);
  box-shadow: 0 30px 70px rgba(23, 29, 45, 0.1);
}

.eyebrow {
  margin: 0 0 10px;
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.hero h2 {
  margin: 0;
  font-size: 40px;
  line-height: 1.1;
  font-family: "Georgia", "Times New Roman", serif;
}

.hero-desc {
  margin: 14px 0 0;
  max-width: 760px;
  color: rgba(23, 29, 45, 0.72);
  line-height: 1.8;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  padding: 18px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(23, 29, 45, 0.08);
}

.stat-card span {
  display: block;
  color: rgba(23, 29, 45, 0.58);
  font-size: 13px;
}

.stat-card strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
  font-family: "Georgia", "Times New Roman", serif;
}

.filter-bar,
.board {
  margin-top: 20px;
  padding: 20px;
  border-radius: 24px;
  background: var(--panel);
  border: 1px solid rgba(23, 29, 45, 0.08);
}

.filter-bar {
  display: grid;
  grid-template-columns: 220px 220px 1fr;
  gap: 12px;
  align-items: center;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.board-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
  align-items: flex-end;
}

.board-head h3 {
  margin: 0;
  font-size: 28px;
  font-family: "Georgia", "Times New Roman", serif;
}

.board-tip {
  color: rgba(23, 29, 45, 0.52);
}

.ticket-table {
  width: 100%;
}

.drawer-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.drawer-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.meta-card {
  padding: 14px;
  border-radius: 18px;
  background: rgba(28, 108, 255, 0.08);
}

.meta-card span {
  display: block;
  color: rgba(23, 29, 45, 0.58);
  font-size: 12px;
}

.meta-card strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  font-family: "Georgia", "Times New Roman", serif;
}

.handle-form {
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(23, 29, 45, 0.08);
}

.section-title {
  margin-bottom: 12px;
  font-weight: 600;
  color: var(--ink);
}

@media (max-width: 1200px) {
  .hero,
  .filter-bar,
  .drawer-meta {
    grid-template-columns: 1fr;
  }
}
</style>
