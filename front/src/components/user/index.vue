<template>
  <div class="user-admin glass-panel">
    <div class="section-heading">
      <div>
        <span class="micro-tag">User Governance</span>
        <h3>用户管理面板</h3>
        <p>通过统一筛选与状态标签管理顾客、商家、骑手和管理员账号。</p>
      </div>
    </div>
    <div class="toolbar">
      <el-select v-model="type" clearable placeholder="role" style="width: 160px">
        <el-option label="customer" value="cus" />
        <el-option label="merchant" value="mer" />
        <el-option label="driver" value="driver" />
        <el-option label="admin" value="admin" />
      </el-select>
      <el-button type="primary" @click="fetchUsers">Refresh</el-button>
    </div>
    <el-table :data="users" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="type" label="Role" width="120" />
      <el-table-column prop="phone" label="Phone" />
      <el-table-column prop="disabled" label="Status" width="120">
        <template #default="{ row }">
          <el-tag :type="row.disabled === 1 ? 'danger' : 'success'">
            {{ row.disabled === 1 ? "Disabled" : "Active" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Action" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="toggleDisabled(row)">
            {{ row.disabled === 1 ? "Enable" : "Disable" }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import fetch from "@/api/fetch";
import { User } from "@/api/apis";

const type = ref("");
const users = ref([]);

const fetchUsers = () => {
  fetch(User.listUsers, { type: type.value }).then((data) => {
    users.value = data || [];
  });
};

const toggleDisabled = (row) => {
  fetch(User.updateUserDisabled, {
    id: row.id,
    disabled: row.disabled === 1 ? 0 : 1,
  }).then(() => {
    ElMessage.success("Update user status successfully");
    fetchUsers();
  });
};

onMounted(fetchUsers);
</script>

<style lang="less" scoped>
.user-admin {
  padding: 22px;

  .toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 16px;
  }
}
</style>
