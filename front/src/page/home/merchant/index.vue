<template>
  <!-- merchant 身份 -->
  <div class="merchant">
    <div class="merchant-hero">
      <div>
        <p class="eyebrow">Merchant Console</p>
        <h2>课堂展示版门店运营中心</h2>
        <p class="hero-desc">
          统一查看商品、订单和门店资料，让顾客端看到的门店形象和商家端维护的运营信息保持一致。
        </p>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <span>待处理订单</span>
          <strong>{{ pendingOrderCount }}</strong>
        </div>
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

    <el-tabs v-model="activeName" @tab-click="handleClick" class="merchant-tab">
      <el-tab-pane :label="t('common.goods')" name="first"><Goods /></el-tab-pane>
      <el-tab-pane name="second">
        <template #label>
          <el-badge :value="pendingOrderCount" :hidden="pendingOrderCount <= 0" :max="99">
            <span>{{ t("common.order") }}</span>
          </el-badge>
        </template>
        <Order />
      </el-tab-pane>
      <el-tab-pane name="third">
        <template #label>
          <el-badge :value="pendingTicketCount" :hidden="pendingTicketCount <= 0" :max="99">
            <span>After-Sale</span>
          </el-badge>
        </template>
        <AfterSaleBoard scope="merchant" />
      </el-tab-pane>
      <el-tab-pane label="Store" name="fourth"><StoreManage /></el-tab-pane>
      <el-tab-pane :label="t('common.info')" name="fifth"><Info /></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import { useI18n } from "vue-i18n";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Info from "@/components/info/index.vue";
import StoreManage from "@/components/restaurant/store-manage.vue";
import AfterSaleBoard from "@/components/after-sale/index.vue";
import { ElNotification } from "element-plus";
import { AfterSale, Order as OrderApi } from "@/api/apis";
import fetch from "@/api/fetch";
import { refreshDataFnMap, userInfo } from "@/store";

const { t } = useI18n();
const activeName = ref("first");
const pendingOrderCount = ref(0);
const pendingTicketCount = ref(0);
let pollTimer = null;
let initialized = false;

const tabKeyMap = {
  [t("common.goods")]: "Goods",
  [t("common.order")]: "Order",
  "After-Sale": "AfterSale",
  Store: "Store",
  [t("common.info")]: "Info",
};

const tabNameMap = {
  first: "Goods",
  second: "Order",
  third: "AfterSale",
  fourth: "Store",
  fifth: "Info",
};

const currentTabTitle = computed(() => {
  const map = {
    first: "商品管理",
    second: "订单处理",
    third: "售后处理",
    fourth: "门店资料",
    fifth: "账号信息",
  };
  return map[activeName.value] || "门店运营";
});

const handleClick = (tab, event) => {
  const key = tabNameMap[tab.props.name] || tabKeyMap[tab.props.label] || tab.props.label;
  refreshDataFnMap.value?.[key]?.();
};

const refreshMerchantOrders = () => {
  if (activeName.value === "second") {
    refreshDataFnMap.value?.Order?.();
  }
};

const pollPendingOrders = () => {
  if (userInfo.value.type !== "mer") return;
  fetch(OrderApi.getOrderList, {
    usrId: userInfo.value.id,
    state: 0,
    timeOrder: 1,
  }).then((data) => {
    const nextCount = data?.merList?.length || 0;
    if (initialized && nextCount > pendingOrderCount.value) {
      ElNotification({
        title: "New Order",
        message: `You have ${nextCount} paid order(s) waiting for action.`,
        type: "success",
        duration: 3000,
      });
      refreshMerchantOrders();
    } else if (activeName.value === "second" && nextCount !== pendingOrderCount.value) {
      refreshMerchantOrders();
    }
    pendingOrderCount.value = nextCount;
    initialized = true;
  });
  fetch(AfterSale.stats, { scope: "merchant" }).then((data) => {
    pendingTicketCount.value = Number(data?.stats?.pending || 0);
  });
};

onMounted(() => {
  pollPendingOrders();
  pollTimer = window.setInterval(pollPendingOrders, 15000);
});

onBeforeUnmount(() => {
  if (pollTimer) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
});
</script>

<style lang="less" scoped>
.merchant {
  margin: 20px;
  padding: 14px 20px 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(185, 122, 57, 0.12), transparent 28%),
    linear-gradient(180deg, #fffefb 0%, #f7efe5 100%);
  border: 1px solid rgba(41, 28, 20, 0.08);

  &-tab {
    padding: 16px;
  }
}

.merchant-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 20px 8px 8px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #b76e2b;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.merchant-hero h2 {
  margin: 0;
  font-size: 34px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #23170f;
}

.hero-desc {
  max-width: 760px;
  margin: 10px 0 0;
  color: rgba(35, 23, 15, 0.72);
  line-height: 1.8;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(140px, 1fr));
  gap: 12px;
  min-width: 480px;
}

.hero-stat {
  padding: 18px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(35, 23, 15, 0.08);
}

.hero-stat span {
  display: block;
  color: rgba(35, 23, 15, 0.58);
  font-size: 13px;
}

.hero-stat strong {
  display: block;
  margin-top: 8px;
  color: #23170f;
  font-size: 26px;
  font-family: "Georgia", "Times New Roman", serif;
}

@media (max-width: 1100px) {
  .merchant-hero {
    flex-direction: column;
  }

  .hero-stats {
    min-width: 0;
  }
}
</style>
