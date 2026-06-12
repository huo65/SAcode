<template>
  <div class="login">
    <div class="login-bg"></div>

    <div class="login-shell">
      <section class="login-brand">
        <span class="micro-tag">西电外卖</span>
        <h1>{{ t("login.title") }}</h1>
        <p class="brand-copy">
          汇聚顾客下单、商家经营、骑手配送与平台管理，提供统一顺畅的校园外卖服务。
        </p>

        <div class="brand-grid">
          <article
            v-for="item in roleHighlights"
            :key="item.title"
            class="brand-card card-hover"
          >
            <span class="brand-card-label">{{ item.tag }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </article>
        </div>
      </section>

      <section class="login-board">
        <div class="board-head">
          <p class="eyebrow">{{ curFormCase === "login" ? "账号登录" : "创建账号" }}</p>
          <h2>{{ curFormCase === "login" ? "欢迎回来" : "创建账号" }}</h2>
          <p>
            {{ curFormCase === "login"
              ? "登录后即可进入对应工作台，继续处理下单、接单、配送与管理操作。"
              : "支持顾客、商家和骑手注册，快速开通对应业务入口。" }}
          </p>
        </div>

        <div class="board-switch">
          <button
            type="button"
            class="switch-chip"
            :class="{ active: curFormCase === 'login' }"
            @click="curFormCase = 'login'"
          >
            登录
          </button>
          <button
            type="button"
            class="switch-chip"
            :class="{ active: curFormCase === 'register' }"
            @click="curFormCase = 'register'"
          >
            注册
          </button>
        </div>

        <el-form
          v-if="curFormCase === 'login'"
          ref="loginFormRef"
          class="login-form"
          :model="loginForm"
          label-position="top"
        >
          <el-form-item :label="t('login.name')" prop="name" required>
            <el-input v-model="loginForm.name" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item :label="t('login.password')" prop="password" required>
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
            />
          </el-form-item>
        </el-form>

        <el-form
          v-else
          ref="registerFormRef"
          class="login-form"
          :model="registerForm"
          :rules="registerFormRules"
          label-position="top"
        >
          <el-form-item :label="t('login.accountType')" prop="type" required>
            <el-select v-model="registerForm.type" :placeholder="t('login.selectYourType')">
              <el-option :label="t('login.customer')" value="cus"></el-option>
              <el-option :label="t('login.merchant')" value="mer"></el-option>
              <el-option :label="t('login.driver')" value="driver"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="t('login.name')" prop="name" required>
            <el-input v-model="registerForm.name" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item :label="t('login.phone')" prop="phone" required>
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item :label="t('login.password')" prop="password" required>
            <el-input v-model="registerForm.password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item :label="t('login.confirmPassword')" prop="confirm_password" required>
            <el-input
              v-model="registerForm.confirm_password"
              show-password
              placeholder="请再次输入密码"
            />
          </el-form-item>
        </el-form>

        <div class="btn-box">
          <template v-if="curFormCase === 'login'">
            <el-button type="primary" @click="clickLogIn(loginFormRef)">{{ t('login.login') }}</el-button>
            <el-button @click="guestVisit">{{ t('login.haveLook') }}</el-button>
            <el-button @click="curFormCase = 'register'">{{ t('login.signUp') }}</el-button>
          </template>
          <template v-else>
            <el-button @click="curFormCase = 'login'">{{ t('login.backToLogin') }}</el-button>
            <el-button type="primary" @click="clickSignUp(registerFormRef)">{{ t('login.signUp') }}</el-button>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import $store from "@/store/index.js";
import fetch from "@/api/fetch.js";
import { User } from "@/api/apis.js";

const { t } = useI18n();
const $router = useRouter();
const curFormCase = ref("login"); // "login" | "register"
const loginFormRef = ref();
const loginForm = reactive({
  name: "",
  password: "",
});
const clickLogIn = (loginFormRef) => {
  loginFormRef.validate((valid, fields) => {
    if (valid) {
      console.log("校验成功，发送请求");
      fetch(User.login, loginForm).then((data) => {
        console.log("登录成功", data);
        ElMessage.success(t('login.loginSuccess'));
        sessionStorage.removeItem("guestMode");
        sessionStorage.setItem("token", data.token || data.jwt || data.JWT);
        $store.commit("setUserInfo", data.user);
        $router.push({ path: "/home" });
      });
    } else {
      ElMessage.error(t('login.pleaseFinish'));
    }
  });
};

const registerFormRef = ref();
const registerForm = reactive({
  type: "",
  name: "",
  portrait: "default_avatar",
  password: "",
  phone: "",
  confirm_password: "",
});
const registerFormRules = reactive({
  confirm_password: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error(t('login.confirmPasswordRequired')));
        } else if (value !== registerForm.password) {
          callback(new Error(t('login.passwordNotMatch')));
        } else {
          callback();
        }
      },
    },
  ],
});
const clickSignUp = async (registerFormRef) => {
  await registerFormRef.validate((valid, fields) => {
    if (valid) {
      console.log("校验通过，发送请求");
      fetch(User.register, registerForm).then((res) => {
        console.log("注册成功", res);
        ElMessage.success(t('login.signUpSuccess'));
      });
    } else {
      console.log("校验失败", fields);
    }
  });
};

const guestVisit = () => {
  $store.commit("clearUserInfo");
  sessionStorage.setItem("guestMode", "1");
  $router.push({ path: "/home" });
};

const roleHighlights = [
  {
    tag: "顾客",
    title: "顾客点餐体验",
    desc: "浏览门店、查看菜单、下单支付与售后反馈完整串联。",
  },
  {
    tag: "商家",
    title: "商家经营工作台",
    desc: "集中处理商品、订单、门店资料与门店经营数据。",
  },
  {
    tag: "骑手",
    title: "骑手配送大盘",
    desc: "支持接单、配送状态、收入表现与异常上报的可视化展示。",
  },
  {
    tag: "管理",
    title: "平台治理中心",
    desc: "统一查看售后、用户状态、权限配置和平台经营信息。",
  },
];
</script>

<style lang="less" scoped>
.login {
  position: relative;
  min-height: 100vh;
  padding: 32px 16px;
  display: flex;
  align-items: center;
  justify-content: center;

  .login-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background:
      linear-gradient(135deg, rgba(35, 15, 7, 0.72), rgba(103, 42, 16, 0.5)),
      url("/B.jpg") center / cover no-repeat;
    z-index: 0;
  }
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.82fr);
  gap: 24px;
}

.login-brand,
.login-board {
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 32px;
  backdrop-filter: blur(16px);
  box-shadow: 0 24px 80px rgba(17, 7, 2, 0.24);
}

.login-brand {
  padding: 38px;
  color: rgba(255, 247, 239, 0.9);
  background: linear-gradient(180deg, rgba(53, 23, 10, 0.64), rgba(28, 11, 4, 0.48));
}

.login-brand h1 {
  margin-top: 18px;
  color: #fff7ef;
  font-family: var(--font-display);
  font-size: clamp(42px, 6vw, 72px);
  line-height: 0.95;
}

.brand-copy {
  max-width: 560px;
  margin-top: 18px;
  color: rgba(255, 243, 234, 0.76);
  font-size: 16px;
  line-height: 1.8;
}

.brand-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 34px;
}

.brand-card {
  min-height: 150px;
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.brand-card-label {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 244, 231, 0.72);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.brand-card h3 {
  margin-top: 14px;
  color: #fff6ee;
  font-size: 22px;
  font-family: var(--font-display);
}

.brand-card p {
  margin-top: 10px;
  color: rgba(255, 243, 234, 0.68);
  line-height: 1.7;
}

.login-board {
  padding: 34px 34px 28px;
  background: linear-gradient(180deg, rgba(255, 252, 249, 0.92), rgba(255, 247, 240, 0.82));
}

.board-head h2 {
  margin-top: 8px;
  color: var(--text-strong);
  font-size: 38px;
  font-family: var(--font-display);
}

.board-head p:last-child {
  margin-top: 12px;
  color: var(--text-soft);
  line-height: 1.7;
}

.eyebrow {
  color: var(--brand-600);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.board-switch {
  display: inline-flex;
  gap: 8px;
  margin: 24px 0 18px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(255, 241, 225, 0.84);
}

.switch-chip {
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  background: transparent;
  color: var(--text-soft);
  cursor: pointer;
  transition: all var(--transition-base);
}

.switch-chip.active {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-400));
  box-shadow: 0 10px 22px rgba(181, 78, 31, 0.22);
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.btn-box {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 20px;
}

.btn-box:has(> :nth-child(2):last-child) {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 1080px) {
  .login-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .login {
    padding: 16px;
  }

  .login-brand,
  .login-board {
    padding: 24px;
  }

  .brand-grid,
  .btn-box {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .brand-card,
  .switch-chip {
    transition: none;
  }
}
</style>
