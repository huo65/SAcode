<template>
  <!-- customer/guest 身份 -->
  <div class="customer">
    <el-tabs v-model="activeName" @tab-click="handleClick" class="customer-tab">
      <el-tab-pane :label="t('common.goods')" name="first"><Goods /></el-tab-pane>
      <el-tab-pane v-if="curStatus === 'customer'" :label="t('common.order')" name="second"
        ><Order
      /></el-tab-pane>
      <el-tab-pane v-if="curStatus === 'customer'" :label="t('common.info')" name="third"
        ><Info
      /></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Info from "@/components/info/index.vue";
import { curStatus, refreshDataFnMap } from "@/store";

const { t } = useI18n();

const activeName = ref("first");

const tabKeyMap = {
  [t('common.goods')]: 'Goods',
  [t('common.order')]: 'Order',
  [t('common.info')]: 'Info'
};

const handleClick = (tab, event) => {
  const key = tabKeyMap[tab.props.label] || tab.props.label;
  refreshDataFnMap.value?.[key]?.();
};
</script>

<style lang="less" scoped>
.customer {
  margin: 20px;
  padding: 10px 20px;

  border-radius: 10px;
  background-color: #fff;

  &-tab {
    padding: 16px;
  }
}
</style>
