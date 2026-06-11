<template>
  <!-- admin 身份 -->
  <div class="admin">
    <el-tabs v-model="activeName" @tab-click="handleClick" class="admin-tab">
      <el-tab-pane label="Goods" name="first"><Goods /></el-tab-pane>
      <el-tab-pane label="Order" name="second"><Order /></el-tab-pane>
      <el-tab-pane label="Category" name="third"><Category /></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Category from "@/components/category/index.vue";
import { curStatus, refreshDataFnMap } from "@/store";

const activeName = ref("first");

const handleClick = (tab, event) => {
  refreshDataFnMap.value?.[tab.props.label]?.();
};
</script>

<style lang="less" scoped>
.admin {
  margin: 20px;
  padding: 10px 20px;

  border-radius: 10px;
  background-color: #fff;

  &-tab {
    padding: 16px;
  }
}
</style>
