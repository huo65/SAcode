<script setup>
import { computed, ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import $store, { userInfo } from "@/store";
import fetch from "@/api/fetch";
import { User } from "@/api/apis";
import { ElMessage } from "element-plus";
import { STATUS_MAP } from "@/constant";
import { resolveImageUrl, uploadImageFromRawFile, validateImageFile } from "@/lib/imageHelper.js";

const modifyFormVisible = ref(false);
const modifyAddressVisible = ref(false);
const modifyAddressData = ref("");
const previewImageUrl = ref(null);
const fileInput = ref(null);
const router = useRouter();

const chooseFile = () => {
  fileInput.value.click();
};

const uploadFile = async (event) => {
  const file = event.target.files[0];
  if (file) {
    const { valid, message } = validateImageFile(file);
    if (!valid) {
      ElMessage.error(message);
      event.target.value = "";
      return;
    }
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImageUrl.value = e.target.result;
    };
    reader.readAsDataURL(file);
    try {
      modifyData.portrait = await uploadImageFromRawFile(file, "avatar");
      modifyUserPortrait();
    } catch (error) {
      previewImageUrl.value = null;
      ElMessage.error(error?.message || "头像上传失败");
    } finally {
      event.target.value = "";
    }
  }
};

const modifyData = reactive({
  id: "",
  type: "",
  name: "",
  portrait: "",
  password: "",
  phone: "",
  description: "",
  ...userInfo.value,
});

const isDriverUser = computed(() => userInfo.value?.type === "driver");
const isCustomerUser = computed(() => ["cus", "customer"].includes(userInfo.value?.type));
const customerServiceLinks = [
  { label: "钱包中心", icon: "fas fa-wallet", path: "/home/customer/wallet" },
  { label: "我的订单", icon: "fas fa-receipt", path: "/home/customer/orders" },
  { label: "我的评价", icon: "fas fa-star", path: "/home/customer/review" },
  { label: "售后工单", icon: "fas fa-headset", path: "/home/customer/after-sale" },
  { label: "地址管理", icon: "fas fa-location-dot", path: "/home/customer/address" },
];
const driverServiceLinks = [
  { label: "待接订单", icon: "fas fa-list-check", path: "/home/driver/available" },
  { label: "配送中", icon: "fas fa-motorcycle", path: "/home/driver/delivering" },
  { label: "历史订单", icon: "fas fa-clock-rotate-left", path: "/home/driver/history" },
  { label: "收益明细", icon: "fas fa-coins", path: "/home/driver/earnings" },
];
const profileServiceLinks = computed(() => {
  if (isCustomerUser.value) return customerServiceLinks;
  if (isDriverUser.value) return driverServiceLinks;
  return [];
});

const goService = (path) => {
  if (path) router.push(path);
};
const driverProfileFields = computed(() => ({
  driverIdCard: modifyData.driverIdCard || "",
  driverVehicle: modifyData.driverVehicle || "",
  driverEmergencyContact: modifyData.driverEmergencyContact || "",
  driverServiceArea: modifyData.driverServiceArea || userInfo.value.driverServiceArea || "",
}));

const addressData = ref([]);
const currentUserType = computed(() => {
  return (
    STATUS_MAP[userInfo.value?.type] || {
      label: "未知身份",
      value: userInfo.value?.type || "-",
    }
  );
});

const fetchAddressData = async () => {
  fetch(User.getAddress, { id: userInfo.value.id }).then((data) => {
    addressData.value = data;
    $store.commit("setUserInfo", { ...userInfo.value, addr_list: data });
  });
};

const handleDelete = (row) => {
  fetch(User.deleteAddress, {
    addressId: row.addrId,
  })
    .then(() => {
      ElMessage.success("地址删除成功");
      fetchAddressData();
    })
    .finally(() => {
      modifyAddressVisible.value = false;
    });
};

const handleAdd = () => {
  if (!modifyAddressData.value) return;
  fetch(User.addAddress, {
    usr: userInfo.value.id,
    location: modifyAddressData.value,
  })
    .then(() => {
      ElMessage.success("地址新增成功");
      fetchAddressData();
    })
    .finally(() => {
      modifyAddressVisible.value = false;
    });
};

const modifyUserInfo = () => {
  const payload = {
    id: modifyData.id,
    type: modifyData.type,
    name: modifyData.name,
    portrait: modifyData.portrait,
    password: modifyData.password,
    phone: modifyData.phone,
    description: modifyData.description,
  };
  fetch(User.updateInfo, payload)
    .then((data) => {
      ElMessage.success("资料更新成功");
      fetch(User.getInfo, { id: userInfo.value.id }).then((data) => {
        const newInfo = {
          ...userInfo.value,
          ...data,
          type: userInfo.value.type,
        };
        $store.commit("setUserInfo", newInfo);
        if (isDriverUser.value) {
          $store.commit("patchUserInfo", driverProfileFields.value);
        }
      });
    })
    .finally(() => {
      modifyFormVisible.value = false;
    });
};

const modifyUserPortrait = () => {
  fetch(User.updateInfo, {
    id: modifyData.id,
    portrait: modifyData.portrait,
  }).then((data) => {
    ElMessage.success("头像更新成功");
    fetch(User.getInfo, { id: userInfo.value.id }).then((data) => {
      const newInfo = {
        ...userInfo.value,
        ...data,
        type: userInfo.value.type,
      };
      $store.commit("setUserInfo", newInfo);
    });
  });
};

const initInfoData = () => {
  fetchAddressData();
  if (isCustomerUser.value) {
    fetchWalletData();
  }
};

const walletData = ref({
  balance: 0,
  totalRecharge: 0,
  totalPay: 0,
  totalRefund: 0,
});
const walletTransactions = ref([]);
const walletTransactionsLoading = ref(false);
const showRechargeDialog = ref(false);
const rechargeAmount = ref(0);
const recharging = ref(false);

const fetchWalletData = async () => {
  try {
    const [walletRes, txRes] = await Promise.all([
      fetch(User.wallet),
      fetch(User.walletTransactions, { page: 1, pageSize: 10 }),
    ]);
    walletData.value = walletRes || walletData.value;
    walletTransactions.value = txRes?.list || txRes || [];
  } catch (err) {
    console.error("获取钱包数据失败", err);
  }
};

const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning("请输入正确的充值金额");
    return;
  }
  recharging.value = true;
  try {
    await fetch(User.recharge, { amount: rechargeAmount.value });
    ElMessage.success(`充值成功，充值金额：¥${rechargeAmount.value}`);
    showRechargeDialog.value = false;
    rechargeAmount.value = 0;
    await fetchWalletData();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || "充值失败，请稍后重试");
  } finally {
    recharging.value = false;
  }
};

const getTxTypeBadge = (type) => {
  const map = {
    RECHARGE: { label: '充值', cls: 'badge-success' },
    PAY: { label: '支付', cls: 'badge-primary' },
    REFUND: { label: '退款', cls: 'badge-warning' },
  };
  return map[type] || { label: type, cls: 'badge-info' };
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Info",
    fn: initInfoData,
  });
  initInfoData();
});
</script>

<template>
  <!-- 编辑资料弹窗 -->
  <div v-if="modifyFormVisible" class="modal-overlay" @click.self="modifyFormVisible = false">
    <div class="modal">
      <div class="modal-header">
        <h3>编辑资料</h3>
        <button class="modal-close" @click="modifyFormVisible = false">&times;</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input v-model="modifyData.name" class="form-input" autocomplete="off" />
        </div>
        <div class="form-group">
          <label class="form-label">个人简介</label>
          <input v-model="modifyData.description" class="form-input" autocomplete="off" />
        </div>
        <div class="form-group">
          <label class="form-label">手机号</label>
          <input v-model="modifyData.phone" class="form-input" autocomplete="off" />
        </div>
        <template v-if="isDriverUser">
          <div class="form-group">
            <label class="form-label">身份证号</label>
            <input v-model="modifyData.driverIdCard" class="form-input" placeholder="请输入身份证号" />
          </div>
          <div class="form-group">
            <label class="form-label">交通工具</label>
            <input v-model="modifyData.driverVehicle" class="form-input" placeholder="电动车 / 摩托车 / 自行车" />
          </div>
          <div class="form-group">
            <label class="form-label">服务区域</label>
            <input v-model="modifyData.driverServiceArea" class="form-input" placeholder="例如：大学城 / 科技园" />
          </div>
          <div class="form-group">
            <label class="form-label">紧急联系人</label>
            <input v-model="modifyData.driverEmergencyContact" class="form-input" placeholder="紧急联系人电话" />
          </div>
        </template>
        <div class="form-group">
          <label class="form-label">登录密码</label>
          <input v-model="modifyData.password" type="password" class="form-input" autocomplete="off" />
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="modifyFormVisible = false">取消</button>
        <button class="btn btn-primary" @click="modifyUserInfo">保存</button>
      </div>
    </div>
  </div>

  <!-- 新增地址弹窗 -->
  <div v-if="modifyAddressVisible" class="modal-overlay" @click.self="modifyAddressVisible = false; modifyAddressData = ''">
    <div class="modal modal-sm">
      <div class="modal-header">
        <h3>新增地址</h3>
        <button class="modal-close" @click="modifyAddressVisible = false; modifyAddressData = ''">&times;</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">地址内容</label>
          <input v-model="modifyAddressData" class="form-input" autocomplete="off" />
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="modifyAddressVisible = false; modifyAddressData = ''">取消</button>
        <button class="btn btn-primary" @click="handleAdd()">保存</button>
      </div>
    </div>
  </div>

  <!-- 充值弹窗 -->
  <div v-if="showRechargeDialog" class="modal-overlay" @click.self="showRechargeDialog = false">
    <div class="modal modal-sm">
      <div class="modal-header">
        <h3>余额充值</h3>
        <button class="modal-close" @click="showRechargeDialog = false">&times;</button>
      </div>
      <div class="modal-body">
        <div class="recharge-label">充值金额</div>
        <div class="recharge-options">
          <button
            v-for="amount in [10, 50, 100, 200, 500]"
            :key="amount"
            class="btn btn-outline"
            :class="{ 'btn-recharge-active': rechargeAmount === amount }"
            @click="rechargeAmount = amount"
          >&yen;{{ amount }}</button>
        </div>
        <div class="form-group" style="margin-top: 12px;">
          <input
            v-model.number="rechargeAmount"
            type="number"
            class="form-input"
            placeholder="输入自定义金额"
            :min="0"
          />
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="showRechargeDialog = false">取消</button>
        <button class="btn btn-primary" :disabled="recharging" @click="handleRecharge">
          {{ recharging ? '充值中...' : '确认充值' }}
        </button>
      </div>
    </div>
  </div>

  <div class="profile-shell">
    <aside class="profile-aside card">
      <span class="micro-tag">个人中心</span>
      <div class="avatar-wrap">
        <img
          v-if="previewImageUrl"
          :src="previewImageUrl"
          @click="chooseFile"
          alt="预览"
          class="preview-image"
        />
        <img
          v-else
          :src="resolveImageUrl(userInfo.portrait, '/storage/avatar/default_avatar.jpg')"
          @click="chooseFile"
          class="preview-image"
        />
      </div>
      <input
        type="file"
        ref="fileInput"
        accept="image/*"
        @change="uploadFile"
        style="display: none"
      />
      <div class="profile-copy">
        <h3>{{ userInfo.name || "当前账号" }}</h3>
        <p>{{ currentUserType.label }}</p>
      </div>
      <div class="aside-actions">
        <button class="btn btn-primary btn-block" @click="chooseFile">更新头像</button>
        <button class="btn btn-outline btn-block" @click="modifyFormVisible = true">编辑资料</button>
      </div>

      <!-- 钱包余额卡片（仅顾客可见） -->
      <div v-if="isCustomerUser" class="wallet-card">
        <div class="wallet-label">钱包余额</div>
        <div class="wallet-balance">
          <span class="wallet-symbol">&yen;</span>
          <span class="wallet-value">{{ walletData.balance.toFixed(2) }}</span>
        </div>
        <div class="wallet-stats">
          <div class="wallet-stat-item">
            <span class="stat-label">累计充值</span>
            <span class="stat-value">&yen;{{ walletData.totalRecharge.toFixed(2) }}</span>
          </div>
          <div class="wallet-stat-item">
            <span class="stat-label">累计消费</span>
            <span class="stat-value">&yen;{{ walletData.totalPay.toFixed(2) }}</span>
          </div>
        </div>
        <button class="btn btn-wallet-recharge" @click="showRechargeDialog = true">充值余额</button>
      </div>

      <div v-if="profileServiceLinks.length" class="service-card">
        <div class="service-card-title">常用服务</div>
        <button
          v-for="item in profileServiceLinks"
          :key="item.path"
          type="button"
          class="service-link"
          @click="goService(item.path)"
        >
          <span><i :class="item.icon"></i>{{ item.label }}</span>
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </aside>

    <section class="profile-main">
      <!-- 账号概览 -->
      <div class="profile-card card">
        <div class="section-heading">
          <div>
            <span class="micro-tag">账号概览</span>
            <h3>账号信息概览</h3>
            <p>用更清晰的层级展示身份、联系方式与个人描述，查看信息更直观。</p>
          </div>
          <button class="btn btn-primary btn-sm" @click="modifyFormVisible = true">编辑</button>
        </div>

        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">电话</span>
            <span class="info-value">{{ userInfo.phone }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">角色</span>
            <span class="info-value">{{ currentUserType.label }}</span>
          </div>
          <div class="info-row info-row--full">
            <span class="info-label">个人简介</span>
            <span class="info-value">{{ userInfo.description || "暂无个人描述，可在编辑资料中补充。" }}</span>
          </div>
        </div>
      </div>

      <!-- 骑手资料 -->
      <div v-if="isDriverUser" class="profile-card card">
        <div class="section-heading">
          <div>
            <span class="micro-tag">骑手资料</span>
            <h3>骑手资料与服务配置</h3>
            <p>补充服务区域、车辆与紧急联系人信息，方便统一管理配送资料。</p>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">接单状态</span>
            <span class="info-value">
              <span class="badge" :class="userInfo.driverWorkStatus === 'rest' ? 'badge-warning' : 'badge-success'">
                {{ userInfo.driverWorkStatus === "rest" ? "休息中" : "在线接单" }}
              </span>
            </span>
          </div>
          <div class="info-row">
            <span class="info-label">服务区域</span>
            <span class="info-value">{{ userInfo.driverServiceArea || modifyData.driverServiceArea || "全城接单" }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">交通工具</span>
            <span class="info-value">{{ userInfo.driverVehicle || modifyData.driverVehicle || "-" }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">身份证号</span>
            <span class="info-value">
              {{
                userInfo.driverIdCard || modifyData.driverIdCard
                  ? `${String(userInfo.driverIdCard || modifyData.driverIdCard).slice(0, 4)}********${String(userInfo.driverIdCard || modifyData.driverIdCard).slice(-4)}`
                  : "-"
              }}
            </span>
          </div>
          <div class="info-row info-row--full">
            <span class="info-label">紧急联系人</span>
            <span class="info-value">{{ userInfo.driverEmergencyContact || modifyData.driverEmergencyContact || "-" }}</span>
          </div>
        </div>
      </div>

      <!-- 地址簿 -->
      <div class="profile-card card">
        <div class="section-heading">
          <div>
            <span class="micro-tag">地址簿</span>
            <h3>地址管理</h3>
            <p>将常用地址集中管理，支持快速新增与删除，便于下单时快速选择。</p>
          </div>
          <button class="btn btn-primary btn-sm" @click="modifyAddressVisible = true">新增地址</button>
        </div>

        <table class="custom-table">
          <thead>
            <tr>
              <th>地址</th>
              <th style="width: 100px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in addressData" :key="row.addrId">
              <td>{{ row.location }}</td>
              <td>
                <button class="btn-text btn-text-danger" @click="handleDelete(row)">删除</button>
              </td>
            </tr>
            <tr v-if="addressData.length === 0">
              <td colspan="2" class="empty-cell">暂无地址，点击右上角新增</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 钱包流水（仅顾客可见） -->
      <div v-if="isCustomerUser" class="profile-card card">
        <div class="section-heading">
          <div>
            <span class="micro-tag">钱包流水</span>
            <h3>最近交易记录</h3>
            <p>查看充值、支付、退款等钱包变动记录。</p>
          </div>
        </div>

        <table class="custom-table">
          <thead>
            <tr>
              <th>类型</th>
              <th>金额</th>
              <th>余额</th>
              <th>说明</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in walletTransactions" :key="row.id">
              <td>
                <span class="badge" :class="getTxTypeBadge(row.type).cls">
                  {{ getTxTypeBadge(row.type).label }}
                </span>
              </td>
              <td>
                <span :class="row.amount > 0 ? 'amount-positive' : 'amount-negative'">
                  {{ row.amount > 0 ? '+' : '' }}{{ row.amount.toFixed(2) }}
                </span>
              </td>
              <td>&yen;{{ row.balanceAfter.toFixed(2) }}</td>
              <td>{{ row.description || '-' }}</td>
              <td>{{ row.createdAt }}</td>
            </tr>
            <tr v-if="walletTransactions.length === 0">
              <td colspan="5" class="empty-cell">暂无交易记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<style lang="less" scoped>
.profile-shell {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
}

.card {
  padding: 22px;
  border-radius: 22px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
}

.profile-main {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ---- Avatar ---- */
.avatar-wrap {
  margin: 20px 0 18px;
}

.preview-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  cursor: pointer;
  border-radius: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.02);
  }
}

.profile-copy h3 {
  color: #1a1a2e;
  font-size: 28px;
  font-family: "Georgia", "Times New Roman", serif;
}

.profile-copy p {
  margin: 8px 0 18px;
  color: rgba(0, 0, 0, 0.5);
}

.aside-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ---- Micro Tag ---- */
.micro-tag {
  display: inline-block;
  margin-bottom: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--primary, #E8652B);
  background: var(--primary-light, rgba(232, 101, 43, 0.12));
}

/* ---- Section Heading ---- */
.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
}

.section-heading h3 {
  margin: 0;
  font-size: 20px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #1a1a2e;
}

.section-heading p {
  margin: 6px 0 0;
  color: rgba(0, 0, 0, 0.5);
}

/* ---- Info Grid (replaces el-descriptions) ---- */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 14px;
  overflow: hidden;
}

.info-row {
  padding: 14px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  border-right: 1px solid rgba(0, 0, 0, 0.05);

  &--full {
    grid-column: 1 / -1;
    border-right: none;
  }
}

.info-row:nth-child(3n) {
  border-right: none;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  display: block;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  margin-bottom: 4px;
}

.info-value {
  color: #1a1a2e;
  font-size: 14px;
  line-height: 1.5;
}

/* ---- Badge ---- */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge-success { color: #059669; background: rgba(5, 150, 105, 0.12); }
.badge-warning { color: #D97706; background: rgba(217, 119, 6, 0.12); }
.badge-primary { color: #E8652B; background: rgba(232, 101, 43, 0.12); }
.badge-info    { color: #6B7280; background: rgba(107, 114, 128, 0.12); }

/* ---- Button ---- */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 22px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &-sm {
    padding: 7px 16px;
    font-size: 13px;
    border-radius: 10px;
  }

  &-block {
    width: 100%;
  }
}

.btn-primary {
  background: var(--primary, #E8652B);
  color: #fff;

  &:hover {
    filter: brightness(0.92);
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }
}

.btn-outline {
  background: transparent;
  color: var(--primary, #E8652B);
  border: 1px solid rgba(232, 101, 43, 0.3);

  &:hover {
    background: rgba(232, 101, 43, 0.06);
  }
}

.btn-text {
  background: none;
  border: none;
  padding: 4px 8px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 600;
  color: var(--primary, #E8652B);
  transition: color 0.2s;

  &:hover {
    opacity: 0.8;
  }
}

.btn-text-danger {
  color: #DC3545;

  &:hover {
    color: #c82333;
  }
}

/* ---- Custom Table (replaces el-table) ---- */
.custom-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;

  th {
    text-align: left;
    padding: 10px 14px;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.55);
    border-bottom: 2px solid rgba(0, 0, 0, 0.08);
    font-size: 12px;
    background: rgba(0, 0, 0, 0.02);
  }

  td {
    padding: 12px 14px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    color: #1a1a2e;
    vertical-align: middle;
  }

  tr:hover td {
    background: rgba(0, 0, 0, 0.015);
  }
}

.empty-cell {
  text-align: center;
  color: rgba(0, 0, 0, 0.35);
  padding: 30px 14px !important;
}

.amount-positive {
  color: #059669;
  font-weight: 600;
}

.amount-negative {
  color: #DC3545;
  font-weight: 600;
}

/* ---- Wallet Card ---- */
.wallet-card {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, var(--primary, #E8652B), #f09060);
  border-radius: 16px;
  color: white;
  text-align: center;
}

.wallet-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.wallet-balance {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 12px;
}

.wallet-symbol {
  font-size: 18px;
  font-weight: 600;
}

.wallet-value {
  font-size: 32px;
  font-weight: 700;
}

.wallet-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 12px;
  font-size: 12px;
}

.wallet-stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.wallet-stat-item .stat-label {
  opacity: 0.8;
}

.wallet-stat-item .stat-value {
  font-weight: 600;
  font-size: 14px;
}

.btn-wallet-recharge {
  width: 100%;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.5) !important;
  color: white !important;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3) !important;
  }
}

.service-card {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 22px rgba(17, 24, 39, 0.06);
}

.service-card-title {
  font-size: 13px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 10px;
}

.service-link {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: rgba(0, 0, 0, 0.72);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  padding: 0 10px;
  transition: background 0.18s ease, color 0.18s ease;

  span {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }

  i:first-child {
    width: 16px;
    color: var(--primary, #E8652B);
  }

  i:last-child {
    font-size: 11px;
    color: rgba(0, 0, 0, 0.32);
  }

  &:hover {
    background: rgba(232, 101, 43, 0.08);
    color: var(--primary, #E8652B);
  }
}

/* ---- Recharge Dialog ---- */
.recharge-label {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.recharge-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  .btn {
    flex: 1;
    min-width: 60px;
  }
}

.btn-recharge-active {
  border-color: var(--primary, #E8652B) !important;
  color: var(--primary, #E8652B) !important;
  background: var(--primary-light, rgba(232, 101, 43, 0.12)) !important;
}

/* ---- Modal ---- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

.modal {
  width: 520px;
  max-width: 92vw;
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.18);
  animation: slideUp 0.25s ease;

  &-sm {
    width: 420px;
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #1a1a2e;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 10px;
  font-size: 18px;
  color: rgba(0, 0, 0, 0.5);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;

  &:hover {
    background: rgba(0, 0, 0, 0.1);
  }
}

.modal-body {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

/* ---- Form ---- */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.65);
}

.form-input {
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 14px;
  color: #1a1a2e;
  background: #fff;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: var(--primary, #E8652B);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.12);
  }

  &::placeholder {
    color: rgba(0, 0, 0, 0.35);
  }
}

/* ---- Animations ---- */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ---- Responsive ---- */
@media (max-width: 960px) {
  .profile-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-row {
    border-right: none;
  }

  .info-row:nth-child(3n) {
    border-right: none;
  }
}
</style>
