<script setup>
import { ref, onMounted } from "vue";
import $store, { productCategories } from "@/store";
import fetch from "@/api/fetch";
import { Product } from "@/api/apis";
import { ElMessage } from "element-plus";
const AddCategoryVisible = ref(false);
const NewCategory = ref("");
const formLabelWidth = "140px";

const deleteRow = (row) => {
  // console.log("删除分类：",row.value);
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
  //console.log("添加新分类：",NewCategory.value);
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
  <el-dialog v-model="AddCategoryVisible" title="新增分类" width="500">
    <el-form>
      <el-form-item label="分类名称" :label-width="formLabelWidth">
        <el-input v-model="NewCategory" autocomplete="off" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="(AddCategoryVisible = false), (NewCategory = '')"
          >取消</el-button
        >
        <el-button type="primary" @click="handleAdd()">确定</el-button>
      </div>
    </template>
  </el-dialog>

  <section class="category-panel glass-panel">
    <div class="section-heading">
      <div>
        <span class="micro-tag">分类管理</span>
        <h3>分类管理工作台</h3>
        <p>用更简洁的控制面板维护商品分类，避免页面只剩一张表格的单调感。</p>
      </div>
      <el-button type="primary" size="small" @click="AddCategoryVisible = true">新增分类</el-button>
    </div>

    <el-table :data="productCategories" style="width: 100%">
      <el-table-column label="分类名称">
        <template v-slot="{ row }">
          {{ row.value }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template v-slot="{ row }">
          <el-button @click="deleteRow(row)" link type="primary" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<style lang="less" scoped>
.category-panel {
  padding: 22px;
}
</style>
