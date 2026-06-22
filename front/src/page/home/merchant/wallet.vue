<template>
  <div class="merchant-wallet">
    <section class="wallet-hero">
      <div><p>MERCHANT WALLET</p><h2>收款与退款流水</h2><span>顾客支付后订单金额将自动计入本店钱包；退款时将同步冲回。</span></div>
      <div class="balance"><small>当前可用余额</small><strong>¥{{ money(wallet.balance) }}</strong></div>
    </section>
    <section class="summary">
      <article><span>累计订单收入</span><strong>¥{{ money(wallet.totalMerchantIncome) }}</strong></article>
      <article><span>累计退款扣回</span><strong>¥{{ money(wallet.totalMerchantRefund) }}</strong></article>
      <article><span>净订单收入</span><strong>¥{{ money(netIncome) }}</strong></article>
    </section>
    <section class="ledger">
      <header><h3>最近收支</h3><button @click="load">刷新</button></header>
      <table><thead><tr><th>类型</th><th>金额</th><th>订单号</th><th>说明</th><th>时间</th></tr></thead><tbody>
        <tr v-for="row in transactions" :key="row.id"><td><span :class="row.type === 'MERCHANT_INCOME' ? 'income' : 'refund'">{{ label(row.type) }}</span></td><td :class="Number(row.amount) >= 0 ? 'amount-in' : 'amount-out'">{{ Number(row.amount) >= 0 ? '+' : '' }}¥{{ money(row.amount) }}</td><td>{{ row.relatedOrderId || '-' }}</td><td>{{ row.remark || '-' }}</td><td>{{ row.createdTime || '-' }}</td></tr>
        <tr v-if="!transactions.length"><td colspan="5">暂无订单收支流水</td></tr>
      </tbody></table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import fetch from '@/api/fetch';
import { User } from '@/api/apis';
const wallet = ref({});
const transactions = ref([]);
const money = (value) => Number(value || 0).toFixed(2);
const netIncome = computed(() => Number(wallet.value.totalMerchantIncome || 0) - Number(wallet.value.totalMerchantRefund || 0));
const label = (type) => type === 'MERCHANT_INCOME' ? '订单收入' : type === 'MERCHANT_REFUND' ? '退款扣回' : type;
const load = async () => {
  const [walletData, transactionData] = await Promise.all([fetch(User.wallet), fetch(User.walletTransactions, { limit: 100 })]);
  wallet.value = walletData?.data || walletData || {};
  const rows = transactionData?.data?.transactions || transactionData?.transactions || transactionData?.data || transactionData || [];
  transactions.value = Array.isArray(rows) ? rows.filter((row) => ['MERCHANT_INCOME', 'MERCHANT_REFUND'].includes(row.type)) : [];
};
onMounted(load);
</script>

<style scoped lang="less">
.merchant-wallet { display:grid; gap:18px; }.wallet-hero { display:flex; justify-content:space-between; gap:24px; padding:28px; border-radius:24px; color:#fff; background:linear-gradient(135deg,#3b1d0b,#d96a2d); }.wallet-hero p { margin:0 0 8px; font-size:12px; letter-spacing:.14em; }.wallet-hero h2 { margin:0 0 10px; }.wallet-hero span { color:rgba(255,255,255,.82); }.balance { text-align:right; }.balance small,.balance strong { display:block; }.balance strong { margin-top:8px; font-size:32px; }.summary { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; }.summary article,.ledger { padding:20px; border-radius:18px; background:#fff; border:1px solid #f0e3da; }.summary span,.summary strong { display:block; }.summary span { color:#806a5b; }.summary strong { margin-top:10px; font-size:24px; color:#42200e; }.ledger header { display:flex; justify-content:space-between; align-items:center; }.ledger h3 { margin:0; }.ledger button { border:0; border-radius:8px; padding:8px 14px; color:#fff; background:#d85d23; cursor:pointer; }.ledger table { width:100%; margin-top:16px; border-collapse:collapse; text-align:left; }.ledger th,.ledger td { padding:12px 8px; border-bottom:1px solid #f1e9e3; }.income,.amount-in { color:#15803d; }.refund,.amount-out { color:#dc2626; } @media (max-width:760px) { .wallet-hero { display:grid; }.balance { text-align:left; }.summary { grid-template-columns:1fr; }.ledger { overflow-x:auto; }.ledger table { min-width:680px; } }
</style>
