<template>
  <div class="store-manage">
    <!-- Hero 指标卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-card--orange">
        <span class="stat-label">营业状态</span>
        <strong class="stat-value">{{ form.status === 1 ? '营业中' : '休息中' }}</strong>
      </div>
      <div class="stat-card stat-card--red">
        <span class="stat-label">配送费</span>
        <strong class="stat-value">&yen;{{ form.deliveryFee || 0 }}</strong>
      </div>
      <div class="stat-card stat-card--amber">
        <span class="stat-label">起送价</span>
        <strong class="stat-value">&yen;{{ form.minOrderAmount || 0 }}</strong>
      </div>
      <div class="stat-card stat-card--blue">
        <span class="stat-label">预计送达</span>
        <strong class="stat-value">{{ form.deliveryEtaMinutes || 30 }} 分钟</strong>
      </div>
    </div>

    <div class="layout">
      <!-- 左：编辑表单 -->
      <div class="card form-card">
        <div class="card-header">
          <h3 class="card-title">门店资料编辑</h3>
          <button class="btn btn-primary" @click="submitStoreInfo">保存门店资料</button>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">门店名称</label>
            <input v-model="form.name" class="form-input" placeholder="例如：校园食堂精选店" />
          </div>
          <div class="form-group">
            <label class="form-label">营业状态</label>
            <select v-model="form.status" class="form-select">
              <option :value="1">营业中</option>
              <option :value="0">休息中</option>
            </select>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">门店 Logo</label>
            <el-upload
              v-model:file-list="logoFileList"
              :auto-upload="false"
              :limit="1"
              list-type="picture-card"
              accept="image/png,image/jpeg,image/webp,image/gif"
              :before-upload="beforeImageUpload"
              :on-change="handleLogoChange"
              class="store-upload"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="form-group">
            <label class="form-label">门店封面</label>
            <el-upload
              v-model:file-list="coverFileList"
              :auto-upload="false"
              :limit="1"
              list-type="picture-card"
              accept="image/png,image/jpeg,image/webp,image/gif"
              :before-upload="beforeImageUpload"
              :on-change="handleCoverChange"
              class="store-upload"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">营业时间</label>
            <input v-model="form.businessHours" class="form-input" placeholder="10:00-21:30" />
          </div>
          <div class="form-group">
            <label class="form-label">门店地址</label>
            <input v-model="form.addressText" class="form-input" placeholder="请输入门店地址" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">门店简介</label>
          <textarea v-model="form.description" class="form-textarea" rows="3" maxlength="180" placeholder="一句话说明门店定位和招牌特色"></textarea>
          <span class="form-hint">{{ (form.description || '').length }}/180</span>
        </div>

        <div class="form-group">
          <label class="form-label">门店公告</label>
          <textarea v-model="form.notice" class="form-textarea" rows="3" maxlength="180" placeholder="例如：高峰期配送稍慢，请耐心等待"></textarea>
          <span class="form-hint">{{ (form.notice || '').length }}/180</span>
        </div>

        <div class="form-grid form-grid--3">
          <div class="form-group">
            <label class="form-label">配送费</label>
            <input v-model.number="form.deliveryFee" type="number" min="0" step="1" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">起送价</label>
            <input v-model.number="form.minOrderAmount" type="number" min="0" step="1" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">预计送达</label>
            <input v-model.number="form.deliveryEtaMinutes" type="number" min="10" step="1" class="form-input" />
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">配送范围</label>
            <input v-model.number="form.serviceRadiusKm" type="number" min="1" step="0.5" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">活动文案</label>
            <input v-model="form.promoText" class="form-input" placeholder="例如：午高峰套餐立减 6 元" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">门店标签</label>
          <input v-model="form.featureTags" class="form-input" placeholder="用逗号分隔，如：品牌门店,招牌套餐,现做现卖" />
        </div>

        <div class="form-group">
          <label class="form-label">菜单分类</label>
          <input v-model="form.menuCategories" class="form-input" placeholder="用逗号分隔，如：招牌套餐,热销主食,小吃饮品" />
        </div>

        <div class="form-group">
          <label class="form-label">配送说明</label>
          <textarea v-model="form.deliveryPolicy" class="form-textarea" rows="3" maxlength="180" placeholder="例如：高峰期由骑手分批取餐，支持校园楼栋送达"></textarea>
          <span class="form-hint">{{ (form.deliveryPolicy || '').length }}/180</span>
        </div>
      </div>

      <!-- 右：顾客端预览 -->
      <div class="card preview-card">
        <div class="card-header">
          <h3 class="card-title">顾客端预览</h3>
          <span class="badge" :class="form.status === 1 ? 'badge-success' : 'badge-info'">
            {{ form.status === 1 ? '营业中' : '休息中' }}
          </span>
        </div>

        <div class="preview-cover">
          <img v-if="form.cover || form.logo" :src="resolveImageUrl(form.cover || form.logo)" alt="cover" />
          <div v-else class="preview-empty">店铺封面</div>
        </div>
        <div class="preview-content">
          <div class="preview-title-row">
            <h3>{{ form.name || '未命名门店' }}</h3>
            <span class="preview-min-order">{{ form.minOrderAmount || 0 }} 元起送</span>
          </div>
          <p class="preview-text">{{ form.description || '门店简介将在这里展示' }}</p>
          <p class="preview-text">{{ form.notice || '门店公告将在这里展示' }}</p>
          <div class="preview-meta">
            <span>配送费 &yen;{{ form.deliveryFee || 0 }}</span>
            <span>{{ form.deliveryEtaMinutes || 30 }} 分钟送达</span>
            <span>{{ form.serviceRadiusKm || 5 }} km 覆盖</span>
          </div>
          <div class="preview-tags">
            <span v-for="tag in splitCsv(form.featureTags)" :key="tag" class="badge badge-primary">{{ tag }}</span>
          </div>
          <div class="preview-tags">
            <span v-for="tag in splitCsv(form.menuCategories)" :key="`menu-${tag}`" class="badge badge-warning">{{ tag }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { Restaurant } from "@/api/apis";
import fetch from "@/api/fetch";
import $store from "@/store";
import {
  getFileNameFromUrl,
  resolveImageUrl,
  uploadImageFromRawFile,
  validateImageFile,
} from "@/lib/imageHelper";

const form = reactive({
  id: "",
  merchantId: "",
  name: "",
  logo: "",
  cover: "",
  description: "",
  notice: "",
  status: 1,
  businessHours: "10:00-21:30",
  deliveryFee: 4,
  minOrderAmount: 18,
  serviceRadiusKm: 5,
  deliveryEtaMinutes: 28,
  featureTags: "",
  menuCategories: "",
  addressText: "",
  deliveryPolicy: "",
  promoText: "",
});

const logoFileList = ref([]);
const coverFileList = ref([]);

const sanitizeText = (value = "") =>
  String(value || "")
    .replace(/课堂展示版/g, "")
    .replace(/课堂展示/g, "")
    .replace(/演示/g, "")
    .replace(/\s{2,}/g, " ")
    .trim();

const splitCsv = (value) =>
  String(value || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);

const buildUploadFileList = (url = "") =>
  url
    ? [
        {
          name: getFileNameFromUrl(url),
          storedUrl: url,
          url: resolveImageUrl(url),
          status: "success",
        },
      ]
    : [];

const beforeImageUpload = (file) => {
  const { valid, message } = validateImageFile(file);
  if (!valid) {
    ElMessage.error(message);
    return false;
  }
  return false;
};

const handleSingleImageChange = (filesRef, file, fileList) => {
  const { valid, message } = validateImageFile(file);
  if (!valid) {
    ElMessage.error(message);
    filesRef.value = fileList.filter((item) => item.uid !== file.uid);
    return;
  }
  filesRef.value = fileList.slice(-1);
};

const handleLogoChange = (file, fileList) => {
  handleSingleImageChange(logoFileList, file, fileList);
};

const handleCoverChange = (file, fileList) => {
  handleSingleImageChange(coverFileList, file, fileList);
};

const loadStoreInfo = () => {
  fetch(Restaurant.manageInfo).then((data) => {
    Object.assign(form, data?.restaurant_manage_info || {});
    form.description = sanitizeText(form.description);
    form.notice = sanitizeText(form.notice);
    form.addressText = sanitizeText(form.addressText);
    form.deliveryPolicy = sanitizeText(form.deliveryPolicy);
    form.promoText = sanitizeText(form.promoText);
    form.featureTags = splitCsv(form.featureTags).map((item) => sanitizeText(item)).join(",");
    form.menuCategories = splitCsv(form.menuCategories).map((item) => sanitizeText(item)).join(",");
    logoFileList.value = buildUploadFileList(form.logo);
    coverFileList.value = buildUploadFileList(form.cover);
  });
};

const submitStoreInfo = async () => {
  try {
    const nextLogo =
      logoFileList.value.find((item) => item.raw)?.raw || null;
    const nextCover =
      coverFileList.value.find((item) => item.raw)?.raw || null;

    if (nextLogo) {
      form.logo = await uploadImageFromRawFile(nextLogo, "restaurant");
    } else if (!logoFileList.value.length) {
      form.logo = "";
    }

    if (nextCover) {
      form.cover = await uploadImageFromRawFile(nextCover, "restaurant");
    } else if (!coverFileList.value.length) {
      form.cover = "";
    }
  } catch (error) {
    ElMessage.error(error?.message || "图片上传失败");
    return;
  }

  fetch(Restaurant.manageUpdate, { ...form }).then(() => {
    ElMessage.success("门店资料已保存");
    loadStoreInfo();
  });
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Store",
    fn: loadStoreInfo,
  });
  loadStoreInfo();
});
</script>

<style lang="less" scoped>
.store-manage {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ---- Stats Grid ---- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(180deg, #ffffff 0%, #fffaf6 100%);

  &--orange { border-color: rgba(232, 101, 43, 0.18); }
  &--red    { border-color: rgba(220, 53, 69, 0.18); background: linear-gradient(180deg, #fff 0%, #fff5f5 100%); }
  &--amber  { border-color: rgba(245, 158, 11, 0.18); background: linear-gradient(180deg, #fff 0%, #fffbeb 100%); }
  &--blue   { border-color: rgba(59, 130, 246, 0.18); background: linear-gradient(180deg, #fff 0%, #eff6ff 100%); }
}

.stat-label {
  display: block;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  margin-bottom: 6px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #1a1a2e;
}

.stat-card--orange .stat-value { color: #E8652B; }
.stat-card--red .stat-value    { color: #DC3545; }
.stat-card--amber .stat-value  { color: #D97706; }
.stat-card--blue .stat-value   { color: #3B82F6; }

/* ---- Layout ---- */
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(340px, 0.8fr);
  gap: 20px;
}

/* ---- Card ---- */
.card {
  padding: 22px;
  border-radius: 22px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(180deg, #ffffff 0%, #fffaf6 100%);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #1a1a2e;
}

/* ---- Form Grid ---- */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;

  &--3 {
    grid-template-columns: 1fr 1fr 1fr;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 4px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.65);
}

.form-input,
.form-select,
.form-textarea {
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 14px;
  color: #1a1a2e;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;

  &:focus {
    border-color: #E8652B;
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.12);
  }

  &::placeholder {
    color: rgba(0, 0, 0, 0.35);
  }
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath d='M3 5l3 3 3-3' stroke='%23999' stroke-width='1.5' fill='none'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
}

.form-textarea {
  resize: vertical;
  line-height: 1.6;
}

.form-hint {
  font-size: 11px;
  color: rgba(0, 0, 0, 0.4);
  text-align: right;
}

/* ---- Upload (keep el-upload but style container) ---- */
.store-upload {
  :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
    border-radius: 14px;
    border: 2px dashed rgba(232, 101, 43, 0.3);
    background: rgba(232, 101, 43, 0.04);
    transition: border-color 0.2s;

    &:hover {
      border-color: #E8652B;
    }
  }

  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 100px;
    height: 100px;
    border-radius: 14px;
  }
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

.badge-success {
  color: #059669;
  background: rgba(5, 150, 105, 0.12);
}

.badge-info {
  color: #6B7280;
  background: rgba(107, 114, 128, 0.12);
}

.badge-primary {
  color: #E8652B;
  background: rgba(232, 101, 43, 0.12);
}

.badge-warning {
  color: #D97706;
  background: rgba(217, 119, 6, 0.12);
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
}

.btn-primary {
  background: #E8652B;
  color: #fff;

  &:hover {
    background: #d55a22;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(232, 101, 43, 0.3);
  }
}

/* ---- Preview Card ---- */
.preview-cover {
  height: 220px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(135deg, #221711 0%, #4a2a1a 100%);
}

.preview-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(255, 255, 255, 0.78);
  font-family: "Georgia", "Times New Roman", serif;
  font-size: 24px;
}

.preview-content {
  margin-top: 18px;
}

.preview-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.preview-title-row h3 {
  margin: 0;
  font-size: 24px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #1a1a2e;
}

.preview-min-order {
  font-size: 13px;
  color: #E8652B;
  font-weight: 600;
}

.preview-text {
  color: rgba(0, 0, 0, 0.6);
  line-height: 1.75;
  margin: 8px 0;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 14px 0;
  color: rgba(0, 0, 0, 0.6);
  font-size: 13px;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

/* ---- Responsive ---- */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .form-grid,
  .form-grid--3 {
    grid-template-columns: 1fr;
  }
}
</style>
