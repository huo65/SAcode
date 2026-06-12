<template>
  <el-dialog
    :model-value="visible"
    :title="restaurantInfo?.name || 'Restaurant Detail'"
    width="1180"
    :before-close="closeDialog"
    top="4vh"
    class="restaurant-modal"
  >
    <div v-if="restaurantInfo" class="restaurant-detail">
      <section class="hero">
        <div class="cover-wrap">
          <img
            v-if="restaurantInfo.cover || restaurantInfo.portrait"
            :src="restaurantInfo.cover || restaurantInfo.portrait"
            class="cover"
            alt="restaurant cover"
          />
          <div v-else class="cover cover-placeholder">Store Cover</div>
          <div class="cover-badge">
            <el-tag :type="restaurantInfo.status === 1 ? 'success' : 'info'">
              {{ restaurantInfo.statusText || (restaurantInfo.status === 1 ? "营业中" : "休息中") }}
            </el-tag>
            <span>{{ formatDistance(restaurantInfo.distanceKm) }}</span>
          </div>
        </div>

        <div class="hero-main">
          <div class="hero-title-row">
            <div>
              <p class="eyebrow">Store Profile</p>
              <h2>{{ restaurantInfo.name }}</h2>
            </div>
            <div class="score-box">
              <strong>{{ restaurantInfo.averageScore || 0 }}</strong>
              <span>{{ restaurantInfo.reviewCount || 0 }} 条评价</span>
            </div>
          </div>

          <p class="description">
            {{ restaurantInfo.description || "门店简介暂未完善。" }}
          </p>
          <p class="notice" v-if="restaurantInfo.notice">
            {{ restaurantInfo.notice }}
          </p>

          <div class="info-grid">
            <div class="info-card">
              <span>营业时间</span>
              <strong>{{ restaurantInfo.businessHours || "-" }}</strong>
            </div>
            <div class="info-card">
              <span>配送费</span>
              <strong>￥{{ restaurantInfo.deliveryFee || 0 }}</strong>
            </div>
            <div class="info-card">
              <span>起送价</span>
              <strong>￥{{ restaurantInfo.minOrderAmount || restaurantInfo.minPrice || 0 }}</strong>
            </div>
            <div class="info-card">
              <span>预计送达</span>
              <strong>{{ restaurantInfo.deliveryEtaMinutes || 30 }} 分钟</strong>
            </div>
          </div>

          <div class="detail-lines">
            <div>
              <strong>门店地址：</strong>
              <span>{{ restaurantInfo.address || "暂无门店地址" }}</span>
            </div>
            <div>
              <strong>配送说明：</strong>
              <span>{{ restaurantInfo.deliveryPolicy || "暂无配送说明" }}</span>
            </div>
            <div v-if="restaurantInfo.promoText">
              <strong>活动文案：</strong>
              <span>{{ restaurantInfo.promoText }}</span>
            </div>
          </div>

          <div class="tags">
            <el-tag
              v-for="tag in restaurantInfo.serviceTags || []"
              :key="tag"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </section>

      <section class="menu-section">
        <div class="section-head">
          <div>
            <p class="eyebrow">Menu Curation</p>
            <h3>门店菜单与分类</h3>
          </div>
          <div class="menu-tags">
            <el-tag
              v-for="group in menuGroups"
              :key="`anchor-${group.name}`"
              type="warning"
              effect="light"
            >
              {{ group.name }}
            </el-tag>
          </div>
        </div>

        <div v-if="menuGroups.length" class="menu">
          <div v-for="group in menuGroups" :key="group.name" class="menu-group">
            <div class="group-head">
              <h4>{{ group.name }}</h4>
              <span>{{ group.products.length }} 款</span>
            </div>
            <div class="product-list">
              <div
                v-for="product in group.products"
                :key="product.id"
                class="product-card"
                @click="openProduct(product)"
              >
                <img
                  v-if="product.image_list?.[0]"
                  :src="product.image_list[0]"
                  class="product-image"
                  alt="product cover"
                />
                <div v-else class="product-image product-placeholder">Dish</div>
                <div class="product-info">
                  <div class="product-header">
                    <span>{{ product.name }}</span>
                    <strong>{{ product.price }}￥</strong>
                  </div>
                  <p>{{ product.description || "该菜品暂无更多介绍。" }}</p>
                  <el-tag size="small" effect="plain">
                    {{ product.cat_name || "店内精选" }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="No menu available yet." />
      </section>

      <section class="reviews">
        <div class="section-head">
          <div>
            <p class="eyebrow">Guest Voices</p>
            <h3>顾客评价与商家回复</h3>
          </div>
          <span class="review-count">{{ reviewList.length }} 条记录</span>
        </div>

        <div v-if="reviewList.length" class="review-list">
          <div v-for="review in reviewList" :key="review.orderId" class="review-card">
            <div class="review-title">
              <div>
                <strong>{{ review.customerName || "Anonymous" }}</strong>
                <span class="review-time">{{ formatReviewTime(review.createdTime) }}</span>
              </div>
              <el-rate :model-value="review.score" disabled />
            </div>
            <p class="review-content">{{ review.content }}</p>
            <div v-if="review.replyContent" class="merchant-reply">
              <strong>商家回复</strong>
              <p>{{ review.replyContent }}</p>
              <span>{{ formatReviewTime(review.replyTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="No reviews yet." />
      </section>
    </div>

    <GoodsDetail
      :visible="productVisible"
      :productInfo="selectedProduct"
      :curStatus="curStatus"
      @close="closeProduct"
    />
  </el-dialog>
</template>

<script setup>
import { computed, ref } from "vue";
import GoodsDetail from "@/components/goods/detail.vue";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  restaurantInfo: {
    type: Object,
    default: null,
  },
  curStatus: {
    type: String,
    default: "guest",
  },
});

const emit = defineEmits(["close"]);

const selectedProduct = ref(null);
const productVisible = ref(false);

const menuGroups = computed(() => {
  const groups = new Map();
  const preferredOrder = props.restaurantInfo?.menuCategories || [];
  preferredOrder.forEach((name) => groups.set(name, []));

  (props.restaurantInfo?.productList || []).forEach((product) => {
    const key = product.cat_name || preferredOrder[0] || "店内精选";
    if (!groups.has(key)) {
      groups.set(key, []);
    }
    groups.get(key).push(product);
  });

  return Array.from(groups.entries())
    .filter(([, products]) => products.length > 0)
    .map(([name, products]) => ({
      name,
      products,
    }));
});

const reviewList = computed(() => props.restaurantInfo?.reviewList || []);

const formatDistance = (value) => {
  if (value == null) return "距离待计算";
  return `${Number(value).toFixed(1)} km`;
};

const openProduct = (product) => {
  selectedProduct.value = product;
  productVisible.value = true;
};

const closeProduct = () => {
  productVisible.value = false;
};

const closeDialog = () => {
  closeProduct();
  emit("close");
};

const formatReviewTime = (value) => {
  if (!value) return "-";
  return String(value).replace("T", " ");
};
</script>

<style lang="less" scoped>
.restaurant-detail {
  --ink: #241712;
  --gold: #b76e2b;
  --panel: rgba(255, 251, 245, 0.9);
  color: var(--ink);
}

.hero {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 22px;
  padding: 18px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(183, 110, 43, 0.16), transparent 34%),
    linear-gradient(135deg, #fff8ef 0%, #f1e0c7 100%);
}

.cover-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
}

.cover {
  width: 100%;
  height: 100%;
  min-height: 300px;
  object-fit: cover;
}

.cover-placeholder,
.product-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #251812 0%, #6a381f 100%);
  color: rgba(255, 247, 238, 0.78);
  font-family: "Georgia", "Times New Roman", serif;
}

.cover-badge {
  position: absolute;
  inset: auto 14px 14px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(20, 16, 13, 0.55);
  color: #fff8ef;
  backdrop-filter: blur(10px);
}

.hero-main {
  padding: 6px 6px 6px 0;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--gold);
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.hero-title-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.hero-title-row h2 {
  margin: 0;
  font-size: 38px;
  line-height: 1.1;
  font-family: "Georgia", "Times New Roman", serif;
}

.score-box {
  min-width: 140px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  text-align: center;
}

.score-box strong {
  display: block;
  font-size: 34px;
  color: var(--gold);
  font-family: "Georgia", "Times New Roman", serif;
}

.score-box span {
  color: rgba(36, 23, 18, 0.58);
  font-size: 13px;
}

.description,
.notice {
  margin: 14px 0 0;
  line-height: 1.8;
  color: rgba(36, 23, 18, 0.72);
}

.notice {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.58);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.info-card {
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(36, 23, 18, 0.08);
}

.info-card span {
  display: block;
  color: rgba(36, 23, 18, 0.58);
  font-size: 12px;
}

.info-card strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  font-family: "Georgia", "Times New Roman", serif;
}

.detail-lines {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
  color: rgba(36, 23, 18, 0.76);
  line-height: 1.7;
}

.tags,
.menu-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.menu-section,
.reviews {
  margin-top: 26px;
  padding: 22px;
  border-radius: 26px;
  background: var(--panel);
  border: 1px solid rgba(36, 23, 18, 0.08);
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-end;
  margin-bottom: 18px;
}

.section-head h3 {
  margin: 0;
  font-size: 30px;
  font-family: "Georgia", "Times New Roman", serif;
}

.group-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.group-head h4 {
  margin: 0;
  font-size: 24px;
  font-family: "Georgia", "Times New Roman", serif;
}

.menu-group + .menu-group {
  margin-top: 24px;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.product-card {
  display: flex;
  gap: 12px;
  padding: 14px;
  border: 1px solid rgba(36, 23, 18, 0.08);
  border-radius: 18px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.72);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 34px rgba(36, 23, 18, 0.08);
}

.product-image {
  width: 110px;
  height: 110px;
  border-radius: 14px;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}

.product-header strong {
  color: var(--gold);
}

.product-info p {
  margin: 10px 0 12px;
  color: rgba(36, 23, 18, 0.66);
  line-height: 1.7;
}

.review-count {
  color: rgba(36, 23, 18, 0.56);
}

.review-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.review-card {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(36, 23, 18, 0.08);
  background: rgba(255, 255, 255, 0.72);
}

.review-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.review-time {
  display: block;
  margin-top: 6px;
  color: rgba(36, 23, 18, 0.46);
  font-size: 12px;
}

.review-content {
  margin: 12px 0;
  color: rgba(36, 23, 18, 0.72);
  line-height: 1.8;
}

.merchant-reply {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(244, 232, 218, 0.72);
  color: rgba(36, 23, 18, 0.72);
}

.merchant-reply p {
  margin: 8px 0 6px;
}

@media (max-width: 1200px) {
  .hero,
  .review-list,
  .product-list {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
