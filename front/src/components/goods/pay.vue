<template>
  <div class="pay-panel">
    <span class="pay-title">余额支付</span>

    <!-- 余额展示 -->
    <div class="balance-section">
      <div class="balance-label">当前余额</div>
      <div class="balance-amount">
        <span class="balance-symbol">¥</span>
        <span class="balance-value">{{ balance.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 支付金额展示 -->
    <div class="pay-amount-section">
      <span class="pay-amount-label">支付金额</span>
      <span class="pay-amount-value">¥{{ payAmount.toFixed(2) }}</span>
    </div>

    <!-- 余额充足：直接支付 -->
    <div v-if="balance >= payAmount" class="pay-sufficient">
      <el-alert type="success" :closable="false" show-icon>
        <template #default>
          将使用账户余额完成支付，确认后订单状态将更新为"已支付"。
        </template>
      </el-alert>
      <el-button type="primary" class="pay-button" @click="payBill" :loading="paying">
        确认余额支付
      </el-button>
    </div>

    <!-- 余额不足：提示充值 -->
    <div v-else class="pay-insufficient">
      <el-alert type="warning" :closable="false" show-icon>
        <template #default>
          余额不足，请先充值后再支付。
        </template>
      </el-alert>

      <!-- 充值区域 -->
      <div class="recharge-section">
        <div class="recharge-label">充值金额</div>
        <div class="recharge-options">
          <el-button
            v-for="amount in quickAmounts"
            :key="amount"
            :class="{ 'recharge-btn-active': rechargeAmount === amount }"
            @click="rechargeAmount = amount"
          >
            ¥{{ amount }}
          </el-button>
        </div>
        <el-input
          v-model.number="rechargeAmount"
          type="number"
          placeholder="输入自定义金额"
          class="recharge-input"
          :min="0"
        />
        <el-button type="primary" class="recharge-button" @click="handleRecharge" :loading="recharging">
          确认充值
        </el-button>
      </div>
    </div>

    <!-- 支付说明 -->
    <div class="pay-tip">
      <span>课堂展示版采用账户余额模拟第三方支付。</span>
      <span>充值表示第三方支付入账，支付表示订单扣款。</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { User } from "@/api/apis";
import fetch from "@/api/fetch";
import { ElMessage } from "element-plus";

const props = defineProps({
  payAmount: {
    type: Number,
    default: 0,
  },
});

const emits = defineEmits(["pay", "recharge-success"]);
const balance = ref(0);
const paying = ref(false);
const recharging = ref(false);
const rechargeAmount = ref(0);
const quickAmounts = [10, 50, 100, 200, 500];

// 获取余额
const fetchBalance = async () => {
  try {
    const data = await fetch(User.wallet);
    balance.value = Number(data?.balance ?? data?.data?.balance ?? 0);
  } catch (err) {
    console.error("获取余额失败", err);
  }
};

// 余额支付
const payBill = async () => {
  paying.value = true;
  try {
    emits("pay", "wallet");
  } finally {
    paying.value = false;
  }
};

// 充值
const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning("请输入正确的充值金额");
    return;
  }
  recharging.value = true;
  try {
    await fetch(User.recharge, { amount: rechargeAmount.value });
    ElMessage.success(`充值成功，充值金额：¥${rechargeAmount.value}`);
    rechargeAmount.value = 0;
    await fetchBalance();
    emits("recharge-success");
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || "充值失败，请稍后重试");
  } finally {
    recharging.value = false;
  }
};

onMounted(() => {
  fetchBalance();
});
</script>

<style lang="less" scoped>
.pay-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  min-width: 360px;
}

.pay-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}

.balance-section {
  text-align: center;
  background: linear-gradient(135deg, var(--primary), #f09060);
  color: white;
  border-radius: 12px;
  padding: 20px 40px;
  width: 100%;
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.balance-amount {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.balance-symbol {
  font-size: 20px;
  font-weight: 600;
}

.balance-value {
  font-size: 36px;
  font-weight: 700;
}

.pay-amount-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 16px 20px;
  background: var(--bg-card);
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.pay-amount-label {
  font-size: 15px;
  color: var(--text-soft);
}

.pay-amount-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--danger);
}

.pay-sufficient {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.pay-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.pay-insufficient {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.recharge-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.recharge-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
}

.recharge-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.recharge-options .el-button {
  flex: 1;
  min-width: 60px;
}

.recharge-btn-active {
  border-color: var(--primary) !important;
  color: var(--primary) !important;
  background: var(--primary-light) !important;
}

.recharge-input {
  width: 100%;
}

.recharge-button {
  width: 100%;
  height: 40px;
  font-weight: 600;
  border-radius: 8px;
}

.pay-tip {
  color: var(--text-soft);
  text-align: center;
  font-size: 12px;
  line-height: 1.6;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
</style>
