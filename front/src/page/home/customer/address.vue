<template>
  <div class="customer-address">
    <!-- 页头 -->
    <div class="addr-header">
      <h3>地址管理</h3>
      <el-button type="primary" round size="small" @click="openAddDialog">
        <i class="fas fa-plus" style="margin-right:6px"></i>新增地址
      </el-button>
    </div>

    <!-- 地址列表 -->
    <div class="addr-list" v-if="addressList.length">
      <div class="addr-card" v-for="addr in addressList" :key="addr.id">
        <div class="addr-main">
          <div class="addr-name-row">
            <span class="addr-name">{{ addr.consignee || addr.name }}</span>
            <span class="addr-phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.isDefault" size="small" type="danger" effect="plain" round>默认</el-tag>
          </div>
          <div class="addr-detail">
            {{ addr.province || '' }}{{ addr.city || '' }}{{ addr.district || '' }}{{ addr.detail || addr.address }}
          </div>
        </div>
        <div class="addr-actions">
          <el-button text size="small" @click="editAddress(addr)">
            <i class="fas fa-pen"></i> 编辑
          </el-button>
          <el-button text size="small" type="danger" @click="deleteAddress(addr)">
            <i class="fas fa-trash"></i> 删除
          </el-button>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <i class="fas fa-map-marker-alt"></i>
      <span>暂无收货地址</span>
      <el-button type="primary" round size="small" @click="openAddDialog">添加地址</el-button>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="editingAddr ? '编辑地址' : '新增地址'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="80px" label-position="top">
        <el-form-item label="收货人">
          <el-input v-model="form.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址（楼栋/门牌号等）"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userInfo } from '@/store';
import { User } from '@/api/apis';
import fetch from '@/api/fetch';

const addressList = ref([]);
const showDialog = ref(false);
const editingAddr = ref(null);
const saving = ref(false);

const form = ref({
  consignee: '',
  phone: '',
  detail: '',
  isDefault: false,
});

const loadAddresses = () => {
  if (!userInfo.value?.id) return;
  fetch(User.getAddress, { usrId: userInfo.value.id }).then((data) => {
    addressList.value = data?.data || data?.list || (Array.isArray(data) ? data : []);
  }).catch(() => {
    addressList.value = [];
  });
};

const openAddDialog = () => {
  editingAddr.value = null;
  form.value = { consignee: '', phone: '', detail: '', isDefault: false };
  showDialog.value = true;
};

const editAddress = (addr) => {
  editingAddr.value = addr;
  form.value = {
    consignee: addr.consignee || addr.name || '',
    phone: addr.phone || '',
    detail: addr.detail || addr.address || '',
    isDefault: !!addr.isDefault,
  };
  showDialog.value = true;
};

const saveAddress = () => {
  if (!form.value.consignee || !form.value.phone || !form.value.detail) {
    ElMessage.warning('请填写完整的地址信息');
    return;
  }
  saving.value = true;
  const payload = {
    usrId: userInfo.value.id,
    consignee: form.value.consignee,
    phone: form.value.phone,
    detail: form.value.detail,
    isDefault: form.value.isDefault ? 1 : 0,
  };
  if (editingAddr.value) {
    payload.id = editingAddr.value.id;
  }
  fetch(User.addAddress, payload)
    .then(() => {
      ElMessage.success(editingAddr.value ? '地址已更新' : '地址添加成功');
      showDialog.value = false;
      loadAddresses();
    })
    .catch((err) => {
      ElMessage.error(err?.message || '保存失败');
    })
    .finally(() => {
      saving.value = false;
    });
};

const deleteAddress = (addr) => {
  ElMessageBox.confirm('确定删除该地址吗？', '提示', { type: 'warning' })
    .then(() => {
      fetch(User.deleteAddress, { id: addr.id, usrId: userInfo.value.id }).then(() => {
        ElMessage.success('地址已删除');
        loadAddresses();
      });
    })
    .catch(() => {});
};

onMounted(() => {
  loadAddresses();
});
</script>

<style lang="less" scoped>
.customer-address {
  padding: 0 0 24px;
}

.addr-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0 20px;

  h3 {
    margin: 0;
    font-size: 22px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary, #1F2937);
  }
}

.addr-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.addr-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: var(--card, #fff);
  border-radius: 14px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }
}

.addr-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.addr-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, #1F2937);
}

.addr-phone {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
}

.addr-detail {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  line-height: 1.6;
}

.addr-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;

  i {
    font-size: 48px;
    opacity: 0.3;
  }
}
</style>
