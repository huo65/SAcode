<script setup>
import { ref, onMounted } from "vue";
import $store, { productCategories } from "@/store";
import fetch from "@/api/fetch";
import { Product } from "@/api/apis";
import { ElMessage } from "element-plus";

const AddCategoryVisible = ref(false);
const NewCategory = ref("");

const deleteRow = (row) => {
  fetch(Product.deleteCategory, {
    category: row.value,
  })
    .then(() => {
      ElMessage.success("分类删除成功");
      fetchCategoryData();
    })
    .finally(() => {});
};

const handleAdd = () => {
  if (NewCategory.value == null || NewCategory.value == "") {
    ElMessage.error("请输入分类名称");
    return;
  }
  fetch(Product.addCategory, {
    name: NewCategory.value,
  })
    .then(() => {
      ElMessage.success("分类新增成功");
      fetchCategoryData();
    })
    .finally(() => {
      AddCategoryVisible.value = false;
      NewCategory.value = "";
    });
};

const fetchCategoryData = () => {
  fetch(Product.getCategories).then((data) => {
    const list = data.map((item) => ({
      label: item.name,
      value: item.name,
    }));
    $store.commit("setCategoryList", list);
  });
};

const initCategoryData = () => {
  fetchCategoryData();
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Category",
    fn: initCategoryData,
  });
  initCategoryData();
});
</script>

<template>
  <div class="category-route">
  <!-- 新增分类弹窗 -->
  <div v-if="AddCategoryVisible" class="modal-overlay" @click.self="AddCategoryVisible = false; NewCategory = ''">
    <div class="modal modal-sm">
      <div class="modal-header">
        <h3>新增分类</h3>
        <button class="modal-close" @click="AddCategoryVisible = false; NewCategory = ''">&times;</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">分类名称</label>
          <input v-model="NewCategory" class="form-input" placeholder="请输入分类名称" />
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="AddCategoryVisible = false; NewCategory = ''">取消</button>
        <button class="btn btn-primary" @click="handleAdd()">确定</button>
      </div>
    </div>
  </div>

  <section class="category-panel card">
    <div class="section-heading">
      <div>
        <span class="micro-tag">分类管理</span>
        <h3>分类管理工作台</h3>
        <p>用更简洁的控制面板维护商品分类，避免页面只剩一张表格的单调感。</p>
      </div>
      <button class="btn btn-primary btn-sm" @click="AddCategoryVisible = true">新增分类</button>
    </div>

    <table class="custom-table">
      <thead>
        <tr>
          <th>分类名称</th>
          <th style="width: 100px;">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in productCategories" :key="row.value">
          <td>
            <span class="category-name">{{ row.value }}</span>
          </td>
          <td>
            <button class="btn-text btn-text-danger" @click="deleteRow(row)">删除</button>
          </td>
        </tr>
        <tr v-if="productCategories.length === 0">
          <td colspan="2" class="empty-cell">暂无分类，点击右上角新增</td>
        </tr>
      </tbody>
    </table>
  </section>
  </div>
</template>

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

.category-name {
  font-weight: 600;
  color: #1a1a2e;
}

.empty-cell {
  text-align: center;
  color: rgba(0, 0, 0, 0.35);
  padding: 30px 14px !important;
}

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
}

.btn-primary {
  background: #4F46E5;
  color: #fff;

  &:hover {
    background: #4338CA;
    transform: translateY(-1px);
  }
}

.btn-outline {
  background: transparent;
  color: #4F46E5;
  border: 1px solid rgba(79, 70, 229, 0.3);

  &:hover {
    background: rgba(79, 70, 229, 0.06);
  }
}

.btn-text {
  background: none;
  border: none;
  padding: 4px 8px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 600;
  transition: color 0.2s;
}

.btn-text-danger {
  color: #DC3545;

  &:hover {
    color: #c82333;
  }
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
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

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
    border-color: #4F46E5;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
  }

  &::placeholder {
    color: rgba(0, 0, 0, 0.35);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
