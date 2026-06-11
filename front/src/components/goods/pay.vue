<template>
  <div class="qr-code">
    <span>Scan to Pay</span>
    <el-radio-group v-model="payWay">
      <el-radio value="wechat" size="large">Wechat</el-radio>
      <el-radio value="alipay" size="large">AliPay</el-radio>
      <el-radio value="paypal" size="large">PayPal</el-radio>
    </el-radio-group>
    <img
      :src="payQRCodeUrl"
      @click="payBill"
      style="width: 200px; height: 200px"
    />
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
const emits = defineEmits(["pay"]);
const payWay = ref("wechat"); // "wechat" | "alibaba" | "paypal"
const payQRCodeUrlMap = {
  wechat: "/img/wechat_QR.png",
  alipay: "/img/alipay_QR.png",
  paypal: "/img/paypal_QR.png",
};
const payQRCodeUrl = computed(() => payQRCodeUrlMap[payWay.value]);

const payBill = () => {
  emits("pay");
};
</script>

<style lang="less" scoped></style>
