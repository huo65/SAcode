<template>
  <!-- merchant 身份 -->
  <div class="merchant">
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
      <el-tab-pane :label="t('common.info')" name="third"><Info /></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useI18n } from "vue-i18n";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Info from "@/components/info/index.vue";
import { ElNotification } from "element-plus";
import { Order as OrderApi } from "@/api/apis";
import fetch from "@/api/fetch";
import { refreshDataFnMap, userInfo } from "@/store";

const { t } = useI18n();
const activeName = ref("first");
const pendingOrderCount = ref(0);
let pollTimer = null;
let initialized = false;

const tabKeyMap = {
  [t("common.goods")]: "Goods",
  [t("common.order")]: "Order",
  [t("common.info")]: "Info",
};

const tabNameMap = {
  first: "Goods",
  second: "Order",
  third: "Info",
};

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
  padding: 10px 20px;

  border-radius: 10px;
  background-color: #fff;

  &-tab {
    padding: 16px;
  }
}
</style>
