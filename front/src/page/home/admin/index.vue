<template>
  <!-- admin 身份 -->
  <div class="admin">
    <div class="admin-hero">
      <div>
        <p class="eyebrow">Platform Command</p>
        <h2>课堂展示版平台治理中心</h2>
        <p class="hero-desc">
          统一查看商品、订单、用户和售后工单，让管理端在课堂展示中同时承担平台治理、问题处理和流程闭环的角色。
        </p>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <span>待处理售后</span>
          <strong>{{ pendingTicketCount }}</strong>
        </div>
        <div class="hero-stat">
          <span>当前标签</span>
          <strong>{{ currentTabTitle }}</strong>
        </div>
      </div>
    </div>
    <el-tabs v-model="activeName" @tab-click="handleClick" class="admin-tab">
      <el-tab-pane :label="t('common.goods')" name="first"><Goods /></el-tab-pane>
      <el-tab-pane :label="t('common.order')" name="second"><Order /></el-tab-pane>
      <el-tab-pane :label="t('common.category')" name="third"><Category /></el-tab-pane>
      <el-tab-pane name="fourth">
        <template #label>
          <el-badge :value="pendingTicketCount" :hidden="pendingTicketCount <= 0" :max="99">
            <span>After-Sale</span>
          </el-badge>
        </template>
        <AfterSaleBoard scope="admin" />
      </el-tab-pane>
      <el-tab-pane :label="t('common.user')" name="fifth"><User /></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Category from "@/components/category/index.vue";
import User from "@/components/user/index.vue";
import AfterSaleBoard from "@/components/after-sale/index.vue";
import { AfterSale } from "@/api/apis";
import fetch from "@/api/fetch";
import { curStatus, refreshDataFnMap } from "@/store";

const { t } = useI18n();
const activeName = ref("first");
const pendingTicketCount = ref(0);
let pollTimer = null;

const tabKeyMap = {
  [t('common.goods')]: 'Goods',
  [t('common.order')]: 'Order',
  [t('common.category')]: 'Category',
  "After-Sale": 'AfterSale',
  [t('common.user')]: 'User'
};

const tabNameMap = {
  first: "Goods",
  second: "Order",
  third: "Category",
  fourth: "AfterSale",
  fifth: "User",
};

const currentTabTitle = computed(() => {
  const map = {
    first: "商品治理",
    second: "订单总览",
    third: "分类管理",
    fourth: "售后工单",
    fifth: "用户管理",
  };
  return map[activeName.value] || "平台治理";
});

const handleClick = (tab, event) => {
  const key = tabNameMap[tab.props.name] || tabKeyMap[tab.props.label] || tab.props.label;
  refreshDataFnMap.value?.[key]?.();
};

const pollTicketStats = () => {
  fetch(AfterSale.stats, { scope: "admin" }).then((data) => {
    pendingTicketCount.value = Number(data?.stats?.pending || 0);
  });
};

onMounted(() => {
  pollTicketStats();
  pollTimer = window.setInterval(pollTicketStats, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
});
</script>

<style lang="less" scoped>
.admin {
  margin: 20px;
  padding: 14px 20px 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(28, 108, 255, 0.12), transparent 28%),
    linear-gradient(180deg, #ffffff 0%, #eef4ff 100%);
  border: 1px solid rgba(23, 29, 45, 0.08);

  &-tab {
    padding: 16px;
  }
}

.admin-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 20px 8px 8px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #1c6cff;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.admin-hero h2 {
  margin: 0;
  font-size: 34px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #171d2d;
}

.hero-desc {
  max-width: 760px;
  margin: 10px 0 0;
  color: rgba(23, 29, 45, 0.72);
  line-height: 1.8;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(140px, 1fr));
  gap: 12px;
  min-width: 320px;
}

.hero-stat {
  padding: 18px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(23, 29, 45, 0.08);
}

.hero-stat span {
  display: block;
  color: rgba(23, 29, 45, 0.58);
  font-size: 13px;
}

.hero-stat strong {
  display: block;
  margin-top: 8px;
  color: #171d2d;
  font-size: 26px;
  font-family: "Georgia", "Times New Roman", serif;
}

@media (max-width: 1100px) {
  .admin-hero {
    flex-direction: column;
  }

  .hero-stats {
    min-width: 0;
  }
}
</style>
