<template>
  <div class="page-shell">
    <header class="header">
      <div class="brand">
        <div class="brand-mark">DB</div>
        <div class="brand-copy">
          <span class="brand-title">DBTake-Out</span>
          <span class="brand-subtitle">{{ roleLabel }}</span>
        </div>
      </div>

      <div v-if="isTEST" class="nav">
        <el-input v-model="TEST_Status" placeholder="切换测试身份"></el-input>
        <el-button @click="clickChangeStatus">改变身份</el-button>
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
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import $store, { curStatus, userInfo } from "@/store/index.js";
import { Avatar } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";

const { t } = useI18n();

// TEST:
const isTEST = computed(() => $store.state.isTEST);
const TEST_Status = ref(String(curStatus.value));
const clickChangeStatus = () => {
  console.log("changeStatus", TEST_Status.value);
  $store.commit("changeStatus", TEST_Status.value);
};

const roleLabel = computed(() => {
  const map = {
    admin: "平台治理视角",
    customer: "顾客点餐视角",
    merchant: "商家经营视角",
    driver: "骑手配送视角",
    guest: "游客浏览视角",
  };
  return map[curStatus.value] || "课堂展示入口";
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

.nav {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 420px;
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

  .nav {
    order: 3;
    flex: 1 0 100%;
    max-width: none;
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
