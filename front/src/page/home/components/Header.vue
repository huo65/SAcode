<template>
  <div class="page-shell">
    <header class="header">
      <div class="brand">
        <div class="brand-mark">西电</div>
        <div class="brand-copy">
          <span class="brand-title">西电外卖</span>
          <span class="brand-subtitle">{{ roleLabel }}</span>
        </div>
      </div>

      <div class="user-card">
        <div class="user-avatar">
          <el-icon size="18px">
            <Avatar />
          </el-icon>
        </div>
        <div class="user-copy">
          <span class="user-eyebrow">Current Session</span>
          <strong>{{ userInfo.name || "Guest" }}</strong>
        </div>
        <el-button size="small" :type="userInfo.id ? 'danger' : 'primary'" @click="clickLogout">
          {{ userInfo.id ? t('header.logout') : t('header.login') }}
        </el-button>
      </div>
    </header>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import $store, { curStatus, userInfo } from "@/store/index.js";
import { Avatar } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";

const { t } = useI18n();

const roleLabel = computed(() => {
  const map = {
    admin: "平台管理",
    customer: "顾客中心",
    merchant: "商家中心",
    driver: "骑手中心",
    guest: "随便看看",
  };
  return map[curStatus.value] || "首页";
});

defineProps({
  nickname: {
    type: String,
    default: "Guest_001",
  },
});
const $router = useRouter();

const clickLogout = () => {
  $store.commit("clearUserInfo");
  $router.push({
    path: "/login",
  });
};
</script>

<style lang="less" scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin: 18px 0 0;
  padding: 16px 20px;
  width: 100%;
  border: 1px solid rgba(92, 46, 20, 0.08);
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 200, 87, 0.24), transparent 24%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(255, 249, 241, 0.78));
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(18px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-400));
  color: #fff;
  font-weight: 700;
  box-shadow: 0 12px 26px rgba(181, 78, 31, 0.18);
}

.brand-copy {
  display: flex;
  flex-direction: column;
}

.brand-title {
  color: var(--text-strong);
  font-size: 22px;
  font-family: var(--font-display);
}

.brand-subtitle {
  color: var(--text-soft);
  font-size: 13px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px 10px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(92, 46, 20, 0.08);
}

.user-avatar {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 244, 231, 0.92);
  color: var(--brand-600);
}

.user-copy {
  display: flex;
  flex-direction: column;
  min-width: 110px;
}

.user-eyebrow {
  color: var(--text-soft);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.user-copy strong {
  color: var(--text-strong);
  font-size: 14px;
}

@media (max-width: 960px) {
  .header {
    flex-wrap: wrap;
    padding: 16px;
  }

}

@media (max-width: 640px) {
  .user-card {
    width: 100%;
    justify-content: space-between;
    border-radius: 20px;
  }
}
</style>
