<template>
  <!-- customer/guest 身份 -->
  <div class="page-shell">
    <div class="customer">
      <section class="customer-hero">
        <div>
          <span class="micro-tag">{{ curStatus === "guest" ? "游客预览" : "顾客中心" }}</span>
          <h2>发现附近好店，轻松点餐下单</h2>
          <p>
            在同一工作区内快速完成门店浏览、订单查看和个人信息管理，点餐流程更清晰顺手。
          </p>
        </div>
        <div class="hero-stats">
          <article class="hero-stat">
            <span>当前模式</span>
            <strong>{{ curStatus === "guest" ? "游客浏览" : "顾客点餐" }}</strong>
          </article>
          <article class="hero-stat">
            <span>核心入口</span>
            <strong>{{ curStatus === "guest" ? "门店列表" : "门店 + 订单 + 资料" }}</strong>
          </article>
        </div>
      </section>

      <el-tabs v-model="activeName" @tab-click="handleClick" class="customer-tab">
        <el-tab-pane label="店铺" name="first"><Restaurant /></el-tab-pane>
        <el-tab-pane v-if="curStatus === 'customer'" :label="t('common.order')" name="second">
          <Order />
        </el-tab-pane>
        <el-tab-pane v-if="curStatus === 'customer'" :label="t('common.info')" name="third">
          <Info />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref } from "vue";
import { useI18n } from "vue-i18n";
import Restaurant from "@/components/restaurant/index.vue";
import Order from "@/components/order/index.vue";
import Info from "@/components/info/index.vue";
import { curStatus, refreshDataFnMap } from "@/store";

const { t } = useI18n();

const activeName = ref("first");

const tabNameMap = {
  first: "Restaurant",
  second: "Order",
  third: "Info",
};

const handleClick = (tab, event) => {
  const key = tabNameMap[tab.props.name] || tab.props.label;
  refreshDataFnMap.value?.[key]?.();
};

const openOrderTab = () => {
  if (curStatus.value !== "customer") return;
  activeName.value = "second";
  refreshDataFnMap.value?.Order?.();
};

onMounted(() => {
  window.addEventListener("navigate-orders", openOrderTab);
});

onBeforeUnmount(() => {
  window.removeEventListener("navigate-orders", openOrderTab);
});
</script>

<style lang="less" scoped>
.customer {
  padding: 20px 20px 24px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 200, 87, 0.18), transparent 26%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(255, 250, 244, 0.82));
  border: 1px solid rgba(92, 46, 20, 0.08);
  box-shadow: var(--shadow-card);

  &-tab {
    padding: 8px 4px 4px;
  }
}

.customer-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 12px 6px 20px;
}

.customer-hero h2 {
  margin-top: 14px;
  color: var(--text-strong);
  font-size: clamp(30px, 4vw, 46px);
  font-family: var(--font-display);
}

.customer-hero p {
  max-width: 760px;
  margin-top: 12px;
  color: var(--text-soft);
  line-height: 1.8;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(150px, 1fr));
  gap: 12px;
  min-width: 380px;
}

.hero-stat {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(92, 46, 20, 0.08);
}

.hero-stat span {
  display: block;
  color: var(--text-soft);
  font-size: 13px;
}

.hero-stat strong {
  display: block;
  margin-top: 8px;
  color: var(--text-strong);
  font-size: 22px;
  font-family: var(--font-display);
}

@media (max-width: 960px) {
  .customer {
    padding: 18px 14px 20px;
  }

  .customer-hero {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-stats {
    min-width: 0;
  }
}

@media (max-width: 720px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }
}
</style>
