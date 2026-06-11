<template>
  <div class="login">
    <div class="login-bg"></div>
    <div class="login-title">{{ t('login.title') }}</div>
    <div class="login-board">
      <el-form
        class="login-form"
        v-if="curFormCase === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        label-width="auto"
      >
        <el-form-item :label="t('login.name')" prop="name" required>
          <el-input v-model="loginForm.name" />
        </el-form-item>
        <el-form-item :label="t('login.password')" prop="password" required>
          <el-input
            type="password"
            show-password
            v-model="loginForm.password"
          />
        </el-form-item>
      </el-form>

      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerFormRules"
        label-width="auto"
      >
        <el-form-item :label="t('login.accountType')" prop="type" required>
          <el-select v-model="registerForm.type" :placeholder="t('login.selectYourType')">
            <el-option :label="t('login.customer')" value="cus"></el-option>
            <el-option :label="t('login.merchant')" value="mer"></el-option>
            <el-option :label="t('login.driver')" value="driver"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('login.name')" prop="name" required>
          <el-input v-model="registerForm.name" />
        </el-form-item>
        <el-form-item :label="t('login.phone')" prop="phone" required>
          <el-input v-model="registerForm.phone" />
        </el-form-item>
        <el-form-item :label="t('login.password')" prop="password" required>
          <el-input v-model="registerForm.password" show-password />
        </el-form-item>
        <el-form-item :label="t('login.confirmPassword')" prop="confirm_password" required>
          <el-input v-model="registerForm.confirm_password" show-password />
        </el-form-item>
      </el-form>

      <div class="btn-box">
        <template v-if="curFormCase === 'login'">
          <el-button @click="clickLogIn(loginFormRef)">{{ t('login.login') }}</el-button>
          <el-button @click="guestVisit">{{ t('login.haveLook') }}</el-button>
          <el-button type="primary" @click="() => (curFormCase = 'register')"
            >{{ t('login.signUp') }}</el-button
          >
        </template>
        <template v-else>
          <el-button @click="() => (curFormCase = 'login')"
            >{{ t('login.backToLogin') }}</el-button
          >
          <el-button type="primary" @click="clickSignUp(registerFormRef)"
            >{{ t('login.signUp') }}</el-button
          >
        </template>
      </div>
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
</script>

<style lang="less" scoped>

.login {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100px;
  .login-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url('/B.jpg');
    background-size: cover; 
    opacity: 0.8; 
    z-index: -1; 
  }


  .el-input {
    --el-input-width: 220px;
  }

  &-title {
    margin-bottom: 20px;
  line-height: 50px;
  text-align: center;
  font-size: 30px;
  font-weight: bolder;
  color: #fff;
  text-shadow: 2px 2px 4px #000000;
  }

  &-board {
    padding: 40px 60px;
    border-radius: 10px;
    background-color: rgba(255, 255, 255, 0.8);

    .login-form {
      padding: 20px;
      
    }

    .btn-box {
      display: flex;
      justify-content: space-around;
      padding: 0 16px;
      
    }
  }
}
</style>
