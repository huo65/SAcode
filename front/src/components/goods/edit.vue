<template>
  <!-- 商品信息编辑页面 -->
  <div class="edit-wrap">
    <el-dialog
      :model-value="visible"
      :title="productInfo && productInfo.id ? 'Edit Product' : 'Create Product'"
      width="500"
      :before-close="close"
    >
      <el-form
        ref="productInfoRef"
        :model="productInfoForm"
        :rules="productInfoRules"
        label-width="auto"
        class="edit-form"
      >
        <el-form-item label="name" prop="name" required>
          <el-input v-model="productInfoForm.name"></el-input>
        </el-form-item>
        <el-form-item label="category" prop="cat_name" required>
          <el-select v-model="productInfoForm.cat_name"
            ><el-option
              v-for="category in productCategories"
              :label="category.label"
              :value="category.value"
            ></el-option
          ></el-select>
        </el-form-item>
        <el-form-item label="description" prop="description" required>
          <el-input
            type="textarea"
            v-model="productInfoForm.description"
          ></el-input>
        </el-form-item>
        <el-form-item label="price" prop="price" required>
          <el-input type="number" v-model="productInfoForm.price"></el-input>
        </el-form-item>
        <el-form-item label="stock" prop="number" required>
          <el-input type="number" v-model="productInfoForm.number"></el-input>
        </el-form-item>
        <el-form-item label="images" prop="image_list" required>
          <el-upload
            v-model:file-list="fileList"
            class="upload-demo"
            multiple
            show-file-list
            :limit="3"
            :before-upload="beforeUpload"
            :on-change="handleImagesChange"
            :on-exceed="handleExceed"
          >
            <el-button type="primary" plain>Click to upload</el-button>
            <template #tip>
              <div class="el-upload__tip">
                jpg/png files with a size less than 500KB.
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <div class="footer">
        <el-button @click="close">Cancel</el-button>
        <el-button type="primary" @click="submit(productInfoRef)"
          >Submit</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, reactive, watch } from "vue";
import { productCategories } from "@/store";
import { ElMessage } from "element-plus";
import fetch from "@/api/fetch.js";
import { Product } from "@/api/apis.js";
import { userInfo } from "@/store";
import {
  getFileNameFromUrl,
  uploadImageListFromRawFiles,
} from "@/lib/imageHelper";
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  productInfo: {
    type: Object,
    default: null,
  },
});
/* 商品信息 */
const productInfoRef = ref();
const productInfoFormDefault = {
  name: "",
  description: "",
  price: null,
  mer: "",
  cat_name: "",
  number: null,
  image_list: [],
};
const productInfoForm = ref({
  name: "",
  description: "",
  price: null,
  mer: "",
  cat_name: "",
  number: null,
  image_list: [],
});
const productInfoRules = reactive({
  image_list: [
    {
      validator: (rule, value, callback) => {
        if (!value || !value.length) {
          callback(new Error("At least 1 picture"));
        } else {
          callback();
        }
      },
    },
  ],
});

/* 上传图片 */
const fileList = ref([]);
const beforeUpload = () => {
  return false;
};
const handleImagesChange = () => {};

const handleExceed = () => {
  ElMessage.error("No more than 3 pictures");
};

const emit = defineEmits(["close"]);
const close = () => {
  emit("close");
  productInfoForm.value = { ...productInfoFormDefault };
  fileList.value = [];
};

const buildExistingFileList = (imageList = []) =>
  imageList.map((url) => ({
    name: getFileNameFromUrl(url),
    url,
    status: "success",
  }));

watch(
  () => props.productInfo,
  (val) => {
    if (val?.id) {
      productInfoForm.value = {
        ...productInfoFormDefault,
        ...val,
        image_list: [...(val.image_list || [])],
      };
      fileList.value = buildExistingFileList(val.image_list || []);
    } else {
      productInfoForm.value = { ...productInfoFormDefault };
      fileList.value = [];
    }
  },
  { immediate: true, deep: true }
);
const submit = async (productInfoRef) => {
  const existingUrls = fileList.value
    .filter((item) => !item.raw && item.url)
    .map((item) => item.url);
  const newFiles = fileList.value.filter((item) => item.raw);

  try {
    const uploadedUrls = await uploadImageListFromRawFiles(newFiles, "product");
    productInfoForm.value.image_list = [...existingUrls, ...uploadedUrls];
  } catch (error) {
    ElMessage.error(error?.message || "Upload product images failed");
    return;
  }

  productInfoRef.validate((valid) => {
    if (valid) {
      if (productInfoForm.value.id) {
        fetch(Product.editProductInfo, {
          ...productInfoForm.value,
          price: Number(productInfoForm.value.price),
          number: Number(productInfoForm.value.number),
          mer: userInfo.value.id,
        }).then(() => {
          ElMessage.success("edit the product successfully");
          close();
        });
      } else {
        fetch(Product.addProduct, {
          ...productInfoForm.value,
          price: Number(productInfoForm.value.price),
          number: Number(productInfoForm.value.number),
          mer: userInfo.value.id,
        }).then(() => {
          ElMessage.success("Add the product successfully");
          close();
        });
      }
    } else {
      ElMessage.error("please finish the fields");
    }
  });
};
</script>

<style lang="less" scoped></style>
