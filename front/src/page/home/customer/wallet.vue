<template>
  <div class="customer-wallet">
    <!-- 页头 -->
    <div class="wallet-header">
      <h3>钱包中心</h3>
    </div>

    <!-- 余额卡片 -->
    <div class="balance-card">
      <div class="balance-card-bg"></div>
      <div class="balance-content">
        <div class="balance-label">账户余额</div>
        <div class="balance-value">¥{{ walletInfo.balance?.toFixed(2) || '0.00' }}</div>
        <div class="balance-meta">
          <span>累计充值 ¥{{ walletInfo.totalRecharge?.toFixed(2) || '0.00' }}</span>
          <span>累计支付 ¥{{ walletInfo.totalPay?.toFixed(2) || '0.00' }}</span>
        </div>
        <el-button class="recharge-btn" round @click="showRecharge = true">
          <i class="fas fa-plus" style="margin-right:6px"></i>充值
        </el-button>
      </div>
    </div>

    <!-- 快捷统计 -->
    <div class="wallet-stats">
      <div class="stat-item">
        <div class="stat-icon" style="background:#FEF2F2;color:#EF4444">
          <i class="fas fa-arrow-down"></i>
        </div>
        <div>
          <div class="stat-val">¥{{ walletInfo.totalRecharge?.toFixed(2) || '0.00' }}</div>
          <div class="stat-label">累计充值</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon" style="background:#ECFDF5;color:#10B981">
          <i class="fas fa-arrow-up"></i>
        </div>
        <div>
          <div class="stat-val">¥{{ walletInfo.totalPay?.toFixed(2) || '0.00' }}</div>
          <div class="stat-label">累计支付</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon" style="background:#FFF7ED;color:#F97316">
          <i class="fas fa-rotate-left"></i>
        </div>
        <div>
          <div class="stat-val">¥{{ walletInfo.totalRefund?.toFixed(2) || '0.00' }}</div>
          <div class="stat-label">累计退款</div>
        </div>
      </div>
    </div>

    <!-- 交易流水 -->
    <div class="transactions-section">
      <div class="section-head">
        <h3>交易流水</h3>
      </div>

      <div class="transaction-list" v-if="transactions.length">
        <div class="transaction-item" v-for="tx in transactions" :key="tx.id || tx.transactionId">
          <div class="tx-icon" :class="tx.type === 'recharge' ? 'tx-icon--in' : 'tx-icon--out'">
            <i :class="tx.type === 'recharge' ? 'fas fa-plus' : 'fas fa-minus'"></i>
          </div>
          <div class="tx-info">
            <div class="tx-desc">{{ tx.description || tx.desc || txTypeLabel(tx.type) }}</div>
            <div class="tx-time">{{ tx.createTime || tx.time }}</div>
          </div>
          <div class="tx-amount" :class="tx.type === 'recharge' ? 'tx-amount--in' : 'tx-amount--out'">
            {{ tx.type === 'recharge' ? '+' : '-' }}¥{{ Number(tx.amount || 0).toFixed(2) }}
          </div>
        </div>
      </div>

      <div class="empty-tip" v-else>
        <i class="fas fa-receipt"></i>
        <span>暂无交易记录</span>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog v-model="showRecharge" title="余额充值" width="420px" :close-on-click-modal="false">
      <div class="recharge-form">
        <div class="recharge-presets">
          <div
            v-for="amount in presetAmounts"
            :key="amount"
            class="preset-btn"
            :class="{ active: rechargeAmount === amount }"
            @click="rechargeAmount = amount"
          >
            ¥{{ amount }}
          </div>
        </div>
        <el-input
          v-model="rechargeAmount"
          type="number"
          placeholder="或输入自定义金额"
          :min="1"
          style="margin-top: 16px"
        >
          <template #prefix>¥</template>
        </el-input>
      </div>
      <template #footer>
        <el-button @click="showRecharge = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="doRecharge">
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { userInfo } from '@/store';
import { User } from '@/api/apis';
import fetch from '@/api/fetch';

const walletInfo = ref({});
const transactions = ref([]);
const showRecharge = ref(false);
const rechargeAmount = ref(50);
const recharging = ref(false);
const presetAmounts = [20, 50, 100, 200, 500];

const txTypeLabel = (type) => {
  const map = {
    recharge: '充值',
    pay: '支付',
    refund: '退款',
  };
  return map[type] || type || '交易';
};

const loadWallet = () => {
  if (!userInfo.value?.id) return;
  fetch(User.wallet, { usrId: userInfo.value.id }).then((data) => {
    walletInfo.value = data?.data || data || {};
  }).catch(() => {
    walletInfo.value = {};
  });
};

const loadTransactions = () => {
  if (!userInfo.value?.id) return;
  fetch(User.walletTransactions, { usrId: userInfo.value.id }).then((data) => {
    transactions.value = data?.data || data?.list || (Array.isArray(data) ? data : []);
  }).catch(() => {
    transactions.value = [];
  });
};

const doRecharge = () => {
  const amount = Number(rechargeAmount.value);
  if (!amount || amount <= 0) {
    ElMessage.warning('请输入有效的充值金额');
    return;
  }
  recharging.value = true;
  fetch(User.recharge, {
    usrId: userInfo.value.id,
    amount: amount,
  })
    .then(() => {
      ElMessage.success(`充值 ¥${amount.toFixed(2)} 成功！`);
      showRecharge.value = false;
      loadWallet();
      loadTransactions();
    })
    .catch((err) => {
      ElMessage.error(err?.message || '充值失败，请重试');
    })
    .finally(() => {
      recharging.value = false;
    });
};

onMounted(() => {
  loadWallet();
  loadTransactions();
});
</script>

<style lang="less" scoped>
.customer-wallet {
  padding: 0 0 24px;
}

.wallet-header {
  padding: 4px 0 20px;

  h3 {
    margin: 0;
    font-size: 22px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary, #1F2937);
  }
}

// ============ 余额卡片 ============
.balance-card {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  padding: 28px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, var(--primary, #EF4444), var(--accent, #F97316));
  color: white;
  min-height: 160px;
}

.balance-card-bg {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.balance-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.balance-value {
  font-size: 36px;
  font-weight: 800;
  font-family: "Georgia", "Times New Roman", serif;
  line-height: 1.2;
}

.balance-meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  opacity: 0.8;
  margin-top: 8px;
}

.recharge-btn {
  position: absolute;
  top: 28px;
  right: 28px;
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  color: white !important;
  backdrop-filter: blur(8px);

  &:hover {
    background: rgba(255, 255, 255, 0.3) !important;
  }
}

// ============ 快捷统计 ============
.wallet-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 28px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--card, #fff);
  border-radius: 14px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.stat-val {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary, #1F2937);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  margin-top: 2px;
}

// ============ 交易流水 ============
.section-head {
  padding: 0 0 14px;

  h3 {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: var(--text-primary, #1F2937);
  }
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--card, #fff);
  border-radius: 12px;
  border: 1px solid var(--border, #F0F0F0);
}

.tx-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;

  &--in {
    background: #ECFDF5;
    color: #10B981;
  }
  &--out {
    background: #FEF2F2;
    color: #EF4444;
  }
}

.tx-info {
  flex: 1;
  min-width: 0;
}

.tx-desc {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1F2937);
}

.tx-time {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  margin-top: 2px;
}

.tx-amount {
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;

  &--in {
    color: #10B981;
  }
  &--out {
    color: #EF4444;
  }
}

// ============ 充值预设 ============
.recharge-presets {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.preset-btn {
  padding: 10px 20px;
  border-radius: 10px;
  border: 1.5px solid var(--border, #F0F0F0);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  font-size: 14px;

  &:hover {
    border-color: var(--primary, #EF4444);
    color: var(--primary, #EF4444);
  }

  &.active {
    background: var(--primary, #EF4444);
    color: white;
    border-color: var(--primary, #EF4444);
  }
}

// ============ 空态 ============
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;

  i {
    font-size: 36px;
    opacity: 0.3;
  }
}

@media (max-width: 768px) {
  .wallet-stats {
    grid-template-columns: 1fr;
  }
}
</style>
