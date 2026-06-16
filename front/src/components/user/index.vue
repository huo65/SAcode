<template>
  <div class="user-admin card">
    <div class="section-heading">
      <div>
        <span class="micro-tag">用户治理</span>
        <h3>用户管理面板</h3>
        <p>通过统一筛选与状态标签管理顾客、商家、骑手和管理员账号。</p>
      </div>
    </div>
    <div class="toolbar">
      <select v-model="type" class="form-select" @change="fetchUsers">
        <option value="">全部角色</option>
        <option value="cus">顾客</option>
        <option value="mer">商家</option>
        <option value="driver">骑手</option>
        <option value="admin">管理员</option>
      </select>
      <button class="btn btn-primary btn-sm" @click="fetchUsers">刷新列表</button>
    </div>

    <table class="custom-table">
      <thead>
        <tr>
          <th style="width: 90px;">ID</th>
          <th>用户名</th>
          <th style="width: 120px;">角色</th>
          <th>手机号</th>
          <th style="width: 100px;">状态</th>
          <th style="width: 100px;">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in users" :key="row.id">
          <td>{{ row.id }}</td>
          <td class="user-cell">
            <div class="avatar">{{ (row.name || '?').slice(0, 1) }}</div>
            <span>{{ row.name }}</span>
          </td>
          <td>
            <span class="badge" :class="getRoleBadge(row.type)">{{ getRoleLabel(row.type) }}</span>
          </td>
          <td>{{ row.phone }}</td>
          <td>
            <span class="badge" :class="row.disabled === 1 ? 'badge-danger' : 'badge-success'">
              {{ row.disabled === 1 ? '已停用' : '正常' }}
            </span>
          </td>
          <td>
            <button
              class="btn btn-sm"
              :class="row.disabled === 1 ? 'btn-success' : 'btn-warning'"
              @click="toggleDisabled(row)"
            >
              {{ row.disabled === 1 ? '启用' : '停用' }}
            </button>
          </td>
        </tr>
        <tr v-if="users.length === 0">
          <td colspan="6" class="empty-cell">暂无用户数据</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import fetch from "@/api/fetch";
import { User } from "@/api/apis";

const type = ref("");
const users = ref([]);

const getRoleLabel = (type) => {
  const map = { cus: '顾客', mer: '商家', driver: '骑手', admin: '管理员' };
  return map[type] || type;
};

const getRoleBadge = (type) => {
  const map = { cus: 'badge-primary', mer: 'badge-warning', driver: 'badge-info', admin: 'badge-admin' };
  return map[type] || 'badge-info';
};

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
.card {
  padding: 22px;
  border-radius: 22px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(180deg, #ffffff 0%, #f7f9ff 100%);
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
}

.micro-tag {
  display: inline-block;
  margin-bottom: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  color: #4F46E5;
  background: rgba(79, 70, 229, 0.12);
  letter-spacing: 0.18em;
  text-transform: uppercase;
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

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.form-select {
  padding: 10px 36px 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 14px;
  color: #1a1a2e;
  background: #fff;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath d='M3 5l3 3 3-3' stroke='%23999' stroke-width='1.5' fill='none'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  outline: none;
  cursor: pointer;
  min-width: 160px;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: #4F46E5;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
  }
}

/* ---- Custom Table ---- */
.custom-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;

  th {
    text-align: left;
    padding: 10px 14px;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.55);
    border-bottom: 2px solid rgba(79, 70, 229, 0.1);
    font-size: 12px;
    background: rgba(79, 70, 229, 0.03);
  }

  td {
    padding: 12px 14px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    color: #1a1a2e;
    vertical-align: middle;
  }

  tr:hover td {
    background: rgba(79, 70, 229, 0.03);
  }
}

.empty-cell {
  text-align: center;
  color: rgba(0, 0, 0, 0.35);
  padding: 30px 14px !important;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4F46E5, #818cf8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
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
.badge-danger  { color: #DC3545; background: rgba(220, 53, 69, 0.12); }
.badge-warning { color: #D97706; background: rgba(217, 119, 6, 0.12); }
.badge-primary { color: #4F46E5; background: rgba(79, 70, 229, 0.12); }
.badge-info    { color: #6B7280; background: rgba(107, 114, 128, 0.12); }
.badge-admin   { color: #7C3AED; background: rgba(124, 58, 237, 0.12); }

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
    padding: 6px 14px;
    font-size: 12px;
    border-radius: 10px;
  }
}

.btn-primary {
  background: #4F46E5;
  color: #fff;

  &:hover {
    background: #4338CA;
  }
}

.btn-success {
  background: #059669;
  color: #fff;

  &:hover {
    background: #047857;
  }
}

.btn-warning {
  background: #D97706;
  color: #fff;

  &:hover {
    background: #B45309;
  }
}
</style>
