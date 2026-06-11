<template>
  <!-- admin 身份 -->
  <div class="admin">
    <el-tabs v-model="activeName" @tab-click="handleClick" class="admin-tab">
      <el-tab-pane :label="t('common.goods')" name="first"><Goods /></el-tab-pane>
      <el-tab-pane :label="t('common.order')" name="second"><Order /></el-tab-pane>
      <el-tab-pane :label="t('common.category')" name="third"><Category /></el-tab-pane>
      <el-tab-pane :label="t('common.user')" name="fourth"><User /></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useI18n } from "vue-i18n";
import Goods from "@/components/goods/index.vue";
import Order from "@/components/order/index.vue";
import Category from "@/components/category/index.vue";
import User from "@/components/user/index.vue";
import { curStatus, refreshDataFnMap } from "@/store";

const { t } = useI18n();
const activeName = ref("first");

const tabKeyMap = {
  [t('common.goods')]: 'Goods',
  [t('common.order')]: 'Order',
  [t('common.category')]: 'Category',
  [t('common.user')]: 'User'
};

const handleClick = (tab, event) => {
  const key = tabKeyMap[tab.props.label] || tab.props.label;
  refreshDataFnMap.value?.[key]?.();
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
