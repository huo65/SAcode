<template>
  <Header />
  <div class="home">
    <Admin v-if="curStatus === 'admin'" />
    <Customer v-if="curStatus === 'customer' || curStatus === 'guest'" />
    <Merchant v-if="curStatus === 'merchant'" />
    <Driver v-if="curStatus === 'driver'" />
  </div>
</template>

<script setup>
import { curStatus } from "@/store";
import Header from "./components/Header.vue";
import Customer from "./customer/index.vue";
import Admin from "./admin/index.vue";
import Merchant from "./merchant/index.vue";
import Driver from "./driver/index.vue";

defineProps({
  status: String,
  msg: String,
});

</script>

<style lang="less" scoped>
.home {
  padding-bottom: 24px;
}
</style>
