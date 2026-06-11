<template>
  <!-- driver 身份 -->
  <div class="driver">
    <el-tabs v-model="activeName" @tab-click="handleClick" class="customer-tab">
      <el-tab-pane label="Goods" name="first"><Goods /></el-tab-pane>
      <el-tab-pane label="Order" name="second"><Order /></el-tab-pane>
      <el-tab-pane label="Info" name="third"><Info /></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from "vue";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Info from "@/components/info/index.vue";
import { curStatus, refreshDataFnMap } from "@/store";

const activeName = ref("first");

const handleClick = (tab, event) => {
  refreshDataFnMap.value?.[tab.props.label]?.();
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
