<template>
  <Header />
  <div class="home">
    <span v-if="isTEST"
      >TEST: This is home {{ msg }} & your status is {{ curStatus }}</span
    >
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

<style lang="less" scoped></style>
