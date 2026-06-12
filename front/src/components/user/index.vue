<template>
  <div class="user-admin glass-panel">
    <div class="section-heading">
      <div>
        <span class="micro-tag">用户治理</span>
        <h3>用户管理面板</h3>
        <p>通过统一筛选与状态标签管理顾客、商家、骑手和管理员账号。</p>
      </div>
    </div>
    <div class="toolbar">
      <el-select v-model="type" clearable placeholder="角色类型" style="width: 160px">
        <el-option label="顾客" value="cus" />
        <el-option label="商家" value="mer" />
        <el-option label="骑手" value="driver" />
        <el-option label="管理员" value="admin" />
      </el-select>
      <el-button type="primary" @click="fetchUsers">刷新列表</el-button>
    </div>
    <el-table :data="users" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="用户名" />
      <el-table-column prop="type" label="角色" width="120" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="disabled" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.disabled === 1 ? 'danger' : 'success'">
            {{ row.disabled === 1 ? "已停用" : "正常" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="toggleDisabled(row)">
            {{ row.disabled === 1 ? "启用" : "停用" }}
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
    ElMessage.success("用户状态更新成功");
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
