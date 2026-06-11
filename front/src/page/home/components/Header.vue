<template>
  <div class="header">
    <span class="title">DBTake-Out </span>
    <span class="nav" v-if="isTEST">
      <el-input v-model="TEST_Status"></el-input>
      <el-button @click="clickChangeStatus">改变身份</el-button>
    </span>
    <span class="user">
      <el-icon size="18px">
        <Avatar />
      </el-icon>

      <span>Welcome, {{ userInfo.name || "Guest" }}</span>

      <el-button
        size="small"
        :type="userInfo.id ? 'danger' : 'primary'"
        @click="clickLogout"
        style="margin-left: 10px"
        >{{ userInfo.id ? t('header.logout') : t('header.login') }}</el-button
      >
    </span>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
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
  padding: 4px 20px;
  width: 100%;
  height: 40px;
  background-color: #fff;

  .title {
    font-weight: 700;
    font-size: 20px;
  }

  .nav {
    margin: 0 20px;
    flex: 1;
    display: flex;
  }

  .user {
    display: flex;
    align-items: center;
    margin-left: auto;
  }
}
</style>
