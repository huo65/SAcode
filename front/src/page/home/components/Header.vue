<template>
  <header class="app-header">
    <button class="hamburger" type="button" @click="emit('toggle-mobile')" aria-label="打开侧边栏">
      <i class="fas fa-bars"></i>
    </button>

    <div class="brand">
      <div class="brand-mark">西</div>
      <div class="brand-copy">
        <span class="brand-title">西电外卖</span>
        <span class="brand-subtitle">{{ roleLabel }}</span>
      </div>
    </div>

    <div class="header-right">
      <div class="user-card">
        <div class="user-avatar">
          <el-icon size="18px"><Avatar /></el-icon>
        </div>
        <div class="user-copy">
          <span class="user-eyebrow">{{ t('header.session') || '当前账号' }}</span>
          <strong>{{ userInfo.name || 'Guest' }}</strong>
        </div>
        <el-button size="small" :type="userInfo.id ? 'danger' : 'primary'" @click="clickLogout">
          {{ userInfo.id ? t('header.logout') : t('header.login') }}
        </el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import $store, { curStatus, userInfo } from '@/store/index.js';
import { Avatar } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const emit = defineEmits(['toggle-mobile']);
const { t } = useI18n();

const roleLabel = computed(() => {
  const map = {
    admin: '平台管理中心',
    customer: '顾客中心',
    merchant: '商家管理中心',
    driver: '骑手工作平台',
    guest: '随便看看',
  };
  return map[curStatus.value] || '首页';
});

const $router = useRouter();
const clickLogout = () => {
  $store.commit('clearUserInfo');
  $router.push({ path: '/login' });
};
</script>

<style lang="less" scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 24px;
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(12px);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 50;
}

.hamburger {
  display: none;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--card);
  cursor: pointer;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  font-size: 14px;
  transition: var(--transition);

  &:hover {
    background: var(--bg);
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary), lighten(#E8652B, 10%));
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 8px 18px rgba(232, 101, 43, 0.22);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.brand-title {
  color: var(--text-primary);
  font-size: 17px;
  font-weight: 700;
  font-family: "Georgia", "Times New Roman", serif;
}

.brand-subtitle {
  color: var(--text-muted);
  font-size: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--border);
}

.user-avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-light);
  color: var(--primary);
}

.user-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-eyebrow {
  color: var(--text-muted);
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.user-copy strong {
  color: var(--text-primary);
  font-size: 13px;
}

@media (max-width: 768px) {
  .app-header {
    padding: 10px 14px;
  }
  .hamburger {
    display: inline-flex;
  }
  .brand-copy {
    display: none;
  }
  .user-copy {
    display: none;
  }
}
</style>
