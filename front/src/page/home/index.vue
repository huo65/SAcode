<template>
  <Header />
  <div class="home">
    <div v-if="isTEST" class="page-shell debug-banner">
      TEST: This is home {{ msg }} & your status is {{ curStatus }}
    </div>
    <!-- 根据身份展示对应的内容 -->
    <Admin v-if="curStatus === 'admin'" />
    <Customer v-if="curStatus === 'customer' || curStatus === 'guest'" />
    <Merchant v-if="curStatus === 'merchant'" />
    <Driver v-if="curStatus === 'driver'" />
  </div>
</template>

<script setup>
import {computed} from "vue";
import {curStatus} from "@/store";
import Header from "./components/Header.vue";
import Customer from "./customer/index.vue";
import Admin from "./admin/index.vue";
import Merchant from "./merchant/index.vue";
import Driver from "./driver/index.vue"
import { isTEST } from "@/store";

defineProps({
  status: String,
  msg: String,
});

</script>

<style lang="less" scoped>
.home {
  padding-bottom: 24px;
}

.debug-banner {
  margin-top: 14px;
  padding: 12px 16px;
  border-radius: 18px;
  background: rgba(255, 245, 229, 0.9);
  border: 1px dashed rgba(181, 78, 31, 0.22);
  color: var(--text-soft);
}
</style>
