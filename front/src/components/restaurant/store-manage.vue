<template>
  <div class="store-manage">
    <div class="hero-panel">
      <div>
        <p class="eyebrow">店铺工作台</p>
        <h2>门店展示资料中心</h2>
        <p class="hero-desc">
          统一维护门店封面、营业状态、公告、配送规则和菜单分类，让顾客端与商家端看到的是同一套门店资料。
        </p>
      </div>
      <div class="hero-metrics">
        <div class="metric">
          <span>营业状态</span>
          <strong>{{ form.status === 1 ? "营业中" : "休息中" }}</strong>
        </div>
        <div class="metric">
          <span>配送费</span>
          <strong>￥{{ form.deliveryFee || 0 }}</strong>
        </div>
        <div class="metric">
          <span>起送价</span>
          <strong>￥{{ form.minOrderAmount || 0 }}</strong>
        </div>
      </div>
    </div>

    <div class="layout">
      <el-card class="form-card" shadow="never">
        <template #header>
          <div class="card-head">
            <span>门店资料编辑</span>
            <el-button type="primary" @click="submitStoreInfo">保存门店资料</el-button>
          </div>
        </template>

        <el-form :model="form" label-width="110px" class="store-form">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="门店名称">
                <el-input v-model="form.name" placeholder="例如：校园食堂精选店" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="营业状态">
                <el-select v-model="form.status" style="width: 100%">
                  <el-option :value="1" label="营业中" />
                  <el-option :value="0" label="休息中" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="门店 Logo">
                <el-upload
                  v-model:file-list="logoFileList"
                  :auto-upload="false"
                  :limit="1"
                  list-type="picture-card"
                  accept="image/png,image/jpeg,image/webp,image/gif"
                  :before-upload="beforeImageUpload"
                  :on-change="handleLogoChange"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="门店封面">
                <el-upload
                  v-model:file-list="coverFileList"
                  :auto-upload="false"
                  :limit="1"
                  list-type="picture-card"
                  accept="image/png,image/jpeg,image/webp,image/gif"
                  :before-upload="beforeImageUpload"
                  :on-change="handleCoverChange"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="营业时间">
            <el-input v-model="form.businessHours" placeholder="10:00-21:30" />
          </el-form-item>

          <el-form-item label="门店地址">
            <el-input v-model="form.addressText" placeholder="请输入门店地址" />
          </el-form-item>

          <el-form-item label="门店简介">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              maxlength="180"
              show-word-limit
              placeholder="一句话说明门店定位和招牌特色"
            />
          </el-form-item>

          <el-form-item label="门店公告">
            <el-input
              v-model="form.notice"
              type="textarea"
              :rows="3"
              maxlength="180"
              show-word-limit
              placeholder="例如：高峰期配送稍慢，请耐心等待"
            />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="配送费">
                <el-input-number v-model="form.deliveryFee" :min="0" :step="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="起送价">
                <el-input-number v-model="form.minOrderAmount" :min="0" :step="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="预计送达">
                <el-input-number v-model="form.deliveryEtaMinutes" :min="10" :step="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="配送范围">
                <el-input-number v-model="form.serviceRadiusKm" :min="1" :step="0.5" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="活动文案">
                <el-input v-model="form.promoText" placeholder="例如：午高峰套餐立减 6 元" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="门店标签">
            <el-input
              v-model="form.featureTags"
              placeholder="用逗号分隔，如：品牌门店,招牌套餐,现做现卖"
            />
          </el-form-item>

          <el-form-item label="菜单分类">
            <el-input
              v-model="form.menuCategories"
              placeholder="用逗号分隔，如：招牌套餐,热销主食,小吃饮品"
            />
          </el-form-item>

          <el-form-item label="配送说明">
            <el-input
              v-model="form.deliveryPolicy"
              type="textarea"
              :rows="3"
              maxlength="180"
              show-word-limit
              placeholder="例如：高峰期由骑手分批取餐，支持校园楼栋送达"
            />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="preview-card" shadow="never">
        <template #header>
          <div class="card-head">
            <span>顾客端预览</span>
            <el-tag :type="form.status === 1 ? 'success' : 'info'">
              {{ form.status === 1 ? "营业中" : "休息中" }}
            </el-tag>
          </div>
        </template>

        <div class="preview-cover">
          <img v-if="form.cover || form.logo" :src="form.cover || form.logo" alt="cover" />
          <div v-else class="preview-empty">店铺封面</div>
        </div>
        <div class="preview-content">
          <div class="preview-title-row">
            <h3>{{ form.name || "未命名门店" }}</h3>
            <span>{{ form.minOrderAmount || 0 }} 元起送</span>
          </div>
          <p class="preview-text">{{ form.description || "门店简介将在这里展示" }}</p>
          <p class="preview-text">{{ form.notice || "门店公告将在这里展示" }}</p>
          <div class="preview-meta">
            <span>配送费 ￥{{ form.deliveryFee || 0 }}</span>
            <span>{{ form.deliveryEtaMinutes || 30 }} 分钟送达</span>
            <span>{{ form.serviceRadiusKm || 5 }} km 覆盖</span>
          </div>
          <div class="preview-tags">
            <el-tag
              v-for="tag in splitCsv(form.featureTags)"
              :key="tag"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="preview-tags subtle">
            <el-tag
              v-for="tag in splitCsv(form.menuCategories)"
              :key="`menu-${tag}`"
              type="warning"
              effect="light"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </el-card>
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
          url,
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
  --bg: #f7f1e8;
  --ink: #1e1a17;
  --accent: #b76e2b;
  --panel: rgba(255, 252, 247, 0.92);
  --line: rgba(33, 26, 22, 0.1);
  color: var(--ink);
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(183, 110, 43, 0.18), transparent 32%),
    linear-gradient(135deg, #fffaf5 0%, #f4eadb 100%);
  border: 1px solid rgba(183, 110, 43, 0.18);
  box-shadow: 0 24px 60px rgba(38, 25, 11, 0.08);
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--accent);
}

.hero-panel h2 {
  margin: 0;
  font-size: 34px;
  font-family: "Georgia", "Times New Roman", serif;
}

.hero-desc {
  max-width: 720px;
  margin: 10px 0 0;
  color: rgba(30, 26, 23, 0.72);
  line-height: 1.8;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  min-width: 360px;
}

.metric {
  padding: 18px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(33, 26, 22, 0.08);
}

.metric span {
  display: block;
  color: rgba(30, 26, 23, 0.6);
  font-size: 13px;
}

.metric strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
  font-family: "Georgia", "Times New Roman", serif;
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(340px, 0.8fr);
  gap: 20px;
  margin-top: 20px;
}

.form-card,
.preview-card {
  border: 1px solid var(--line);
  border-radius: 24px;
  background: var(--panel);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  font-weight: 600;
}

.preview-cover {
  height: 220px;
  border-radius: 20px;
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
  font-size: 28px;
  font-family: "Georgia", "Times New Roman", serif;
}

.preview-text {
  color: rgba(30, 26, 23, 0.72);
  line-height: 1.75;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 14px 0;
  color: rgba(30, 26, 23, 0.75);
  font-size: 13px;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-tags.subtle {
  margin-top: 12px;
}

@media (max-width: 1200px) {
  .hero-panel,
  .layout {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-metrics {
    min-width: 0;
  }
}
</style>
