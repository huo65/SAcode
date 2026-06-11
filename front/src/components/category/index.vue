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
      ElMessage.success("Delete Category Successfully.");
      fetchCategoryData();
    })
    .finally(() => {});
};

const handleAdd = () => {
  //console.log("添加新分类：",NewCategory.value);
  if (NewCategory.value == null || NewCategory.value == "") {
    ElMessage.error("Input Empty.");
    return;
  }
  fetch(Product.addCategory, {
    name: NewCategory.value,
  })
    .then(() => {
      ElMessage.success("Add Category Successfully.");
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
  <el-dialog v-model="AddCategoryVisible" title="Add Category" width="500">
    <el-form>
      <el-form-item label="New Category" :label-width="formLabelWidth">
        <el-input v-model="NewCategory" autocomplete="off" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="(AddCategoryVisible = false), (NewCategory = '')"
          >Cancel</el-button
        >
        <el-button type="primary" @click="handleAdd()"> Confirm </el-button>
      </div>
    </template>
  </el-dialog>

  <el-descriptions
    class="margin-top"
    title="Category Management"
    :column="3"
    border
  >
  </el-descriptions>
  <el-button type="primary" size="small" @click="AddCategoryVisible = true"
    >Add New Category</el-button
  >
  <!-- Address Table -->
  <el-table :data="productCategories" style="width: 100%">
    <el-table-column label="Category" width="1200">
      <template v-slot="{ row }">
        {{ row.value }}
      </template>
    </el-table-column>
    <el-table-column fixed="right" label="Operations" width="120">
      <template v-slot="{ row }">
        <el-button @click="deleteRow(row)" link type="primary" size="small"
          >Delete</el-button
        >
      </template>
    </el-table-column>
  </el-table>
</template>

<style lang="less" scoped></style>
