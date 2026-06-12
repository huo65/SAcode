<template>
  <div class="pay-panel">
    <span class="pay-title">请选择支付方式</span>
    <el-radio-group v-model="payWay" class="pay-options">
      <el-radio value="mock" size="large">模拟支付</el-radio>
      <el-radio value="alipay" size="large">支付宝</el-radio>
    </el-radio-group>
    <div class="pay-tip">
      <span v-if="payWay === 'mock'">点击后将直接完成支付，并更新订单状态。</span>
      <span v-else>将打开支付宝沙箱支付页，支付完成后自动刷新订单状态。</span>
    </div>
    <img
      v-if="payWay === 'alipay'"
      :src="payQRCodeUrl"
      alt="alipay"
      style="width: 200px; height: 200px"
    />
    <el-button type="primary" @click="payBill">
      {{ payWay === "alipay" ? "打开支付宝" : "立即支付" }}
    </el-button>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
const emits = defineEmits(["pay"]);
const payWay = ref("mock");
const payQRCodeUrlMap = {
  alipay: "/img/alipay_QR.png",
};
const payQRCodeUrl = computed(() => payQRCodeUrlMap[payWay.value]);

const payBill = () => {
  emits("pay", payWay.value);
};
</script>

<style lang="less" scoped>
.pay-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}

.pay-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-strong);
}

.pay-options {
  display: flex;
  gap: 18px;
}

.pay-tip {
  color: var(--text-soft);
  text-align: center;
  line-height: 1.6;
}
</style>
