<template>
  <!-- 商品信息编辑页面 -->
  <div class="edit-wrap">
    <el-dialog
      :model-value="visible"
      :title="productInfo && productInfo.id ? '编辑商品' : '新增商品'"
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
        <el-form-item label="商品名称" prop="name" required>
          <el-input v-model="productInfoForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="cat_name" required>
          <el-select v-model="productInfoForm.cat_name" placeholder="请选择分类"
            ><el-option
              v-for="category in productCategories"
              :label="category.label"
              :value="category.value"
            ></el-option
          ></el-select>
        </el-form-item>
        <el-form-item label="商品描述" prop="description" required>
          <el-input
            type="textarea"
            v-model="productInfoForm.description"
            placeholder="请输入商品描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="商品价格" prop="price" required>
          <el-input type="number" v-model="productInfoForm.price" placeholder="请输入价格"></el-input>
        </el-form-item>
        <el-form-item label="库存数量" prop="number" required>
          <el-input type="number" v-model="productInfoForm.number" placeholder="请输入库存"></el-input>
        </el-form-item>
        <el-form-item label="商品图片" prop="image_list" required>
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            class="upload-demo"
            list-type="picture-card"
            show-file-list
            :limit="3"
            accept="image/png,image/jpeg,image/webp,image/gif"
            :before-upload="beforeUpload"
            :on-change="handleImagesChange"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            :http-request="uploadSingleImage"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG、WEBP、GIF 格式，单张不超过 2MB，最多上传 3 张。
              </div>
            </template>
          </el-upload>
          <div v-if="uploadSummary" class="upload-summary">{{ uploadSummary }}</div>
        </el-form-item>
      </el-form>

      <div class="footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="submit(productInfoRef)"
          >保存商品</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, watch } from "vue";
import { productCategories } from "@/store";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import fetch from "@/api/fetch.js";
import { Product } from "@/api/apis.js";
import { userInfo } from "@/store";
import {
  getFileNameFromUrl,
  uploadImageFromRawFileWithProgress,
  validateImageFile,
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
const uploadRef = ref();
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
        if (!fileList.value.length) {
          callback(new Error("请至少上传 1 张商品图片"));
        } else if (
          fileList.value.some(
            (item) => item.status === "uploading" || (!item.url && item.raw)
          )
        ) {
          callback(new Error("图片上传中，请稍后再提交"));
        } else if (
          fileList.value.some((item) => item.status === "fail" || item.status === "error")
        ) {
          callback(new Error("存在上传失败的图片，请删除后重新上传"));
        } else {
          callback();
        }
      },
    },
  ],
});

/* 上传图片 */
const fileList = ref([]);
const resolveUploadedUrl = (file) =>
  file?.url || file?.response?.url || file?.response?.data?.url || "";

const normalizeUploadFiles = (uploadFiles = []) =>
  uploadFiles.map((item) => {
    const url = resolveUploadedUrl(item);
    if (!url) {
      return item;
    }
    return {
      ...item,
      url,
      name: item.name || getFileNameFromUrl(url),
      status: item.status === "fail" ? "fail" : "success",
      percentage: 100,
    };
  });

const syncImageList = () => {
  productInfoForm.value.image_list = fileList.value
    .map((item) => resolveUploadedUrl(item))
    .filter(Boolean);
};

const isRetainableUploadFile = (file) => {
  if (resolveUploadedUrl(file)) {
    return true;
  }
  if (!file?.raw) {
    return true;
  }
  return validateImageFile(file).valid;
};

const beforeUpload = (file) => {
  const { valid, message } = validateImageFile(file);
  if (!valid) {
    ElMessage.error(message);
    return false;
  }
  return true;
};
const handleImagesChange = (_file, uploadFiles) => {
  fileList.value = normalizeUploadFiles(uploadFiles.filter(isRetainableUploadFile));
  syncImageList();
  productInfoRef.value?.validateField("image_list").catch(() => {});
};

const handleRemove = (_file, uploadFiles) => {
  fileList.value = normalizeUploadFiles(uploadFiles.filter(isRetainableUploadFile));
  syncImageList();
  productInfoRef.value?.validateField("image_list").catch(() => {});
};

const handleExceed = () => {
  ElMessage.error("最多只能上传 3 张图片");
};

const uploadSingleImage = async (options) => {
  const { file, onError, onProgress, onSuccess } = options;
  try {
    const url = await uploadImageFromRawFileWithProgress(
      file,
      "product",
      (percent, event) => {
        onProgress?.({ percent }, event);
      }
    );
    const targetFile = fileList.value.find((item) => item.uid === file.uid);
    if (targetFile) {
      targetFile.url = url;
      targetFile.response = { url };
      targetFile.status = "success";
      targetFile.percentage = 100;
      targetFile.name = targetFile.name || file.name || getFileNameFromUrl(url);
    }
    file.url = url;
    onSuccess?.({ url }, file);
    fileList.value = normalizeUploadFiles(fileList.value);
    syncImageList();
    productInfoRef.value?.validateField("image_list").catch(() => {});
  } catch (error) {
    onError?.(error);
    ElMessage.error(error?.message || "商品图片上传失败");
  }
};

const uploadSummary = computed(() => {
  const uploadingFiles = fileList.value.filter((item) => item.status === "uploading");
  const uploadingCount = uploadingFiles.length;
  const successCount = fileList.value.filter((item) => item.url).length;
  if (uploadingCount > 0) {
    const averagePercent = Math.round(
      uploadingFiles.reduce((sum, item) => sum + Number(item.percentage || 0), 0) /
        uploadingCount
    );
    return `正在上传 ${uploadingCount} 张图片，平均进度 ${averagePercent}% ，已完成 ${successCount} 张`;
  }
  if (successCount > 0) {
    return `已成功上传 ${successCount} 张图片`;
  }
  return "";
});

const emit = defineEmits(["close"]);
const close = () => {
  emit("close");
  productInfoForm.value = { ...productInfoFormDefault };
  fileList.value = [];
  uploadRef.value?.clearFiles?.();
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
      syncImageList();
    } else {
      productInfoForm.value = { ...productInfoFormDefault };
      fileList.value = [];
    }
  },
  { immediate: true, deep: true }
);
const submit = async (productInfoRef) => {
  syncImageList();

  productInfoRef.validate((valid) => {
    if (valid) {
      if (productInfoForm.value.id) {
        fetch(Product.editProductInfo, {
          ...productInfoForm.value,
          price: Number(productInfoForm.value.price),
          number: Number(productInfoForm.value.number),
          mer: userInfo.value.id,
        }).then(() => {
          ElMessage.success("商品修改成功");
          close();
        });
      } else {
        fetch(Product.addProduct, {
          ...productInfoForm.value,
          price: Number(productInfoForm.value.price),
          number: Number(productInfoForm.value.number),
          mer: userInfo.value.id,
        }).then(() => {
          ElMessage.success("商品新增成功");
          close();
        });
      }
    } else {
      ElMessage.error("请完善商品信息后再提交");
    }
  });
};
</script>

<style lang="less" scoped>
.upload-summary {
  margin-top: 8px;
  color: #606266;
  font-size: 13px;
}
</style>
