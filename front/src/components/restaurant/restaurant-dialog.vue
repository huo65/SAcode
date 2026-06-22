<template>
  <section
    v-if="visible && curStatus === 'customer'"
    class="mobile-store-page"
  >
    <header class="mobile-store-nav">
      <button class="nav-icon-button nav-back-button" type="button" aria-label="返回店铺列表" @click="closeDialog">‹</button>
      <span>店铺详情</span>
      <button class="nav-icon-button" type="button" aria-label="更多操作">
        <i class="fas fa-ellipsis-h"></i>
      </button>
    </header>

    <main v-if="restaurantInfo" class="mobile-store-content">
      <section class="store-banner">
        <img
          v-if="restaurantInfo.cover || restaurantInfo.portrait"
          :src="resolveImageUrl(restaurantInfo.cover || restaurantInfo.portrait)"
          :alt="restaurantInfo.name"
        />
        <div v-else class="store-banner-placeholder"><i class="fas fa-utensils"></i></div>
        <div class="store-banner-mask"></div>
        <div class="store-banner-status">
          <span :class="restaurantInfo.status === 1 ? 'is-open' : 'is-resting'">
            {{ restaurantInfo.statusText || (restaurantInfo.status === 1 ? '营业中' : '休息中') }}
          </span>
          <span>{{ formatDistance(restaurantInfo.distanceKm) }}</span>
        </div>
      </section>

      <section class="store-summary">
        <div class="store-title-row">
          <div>
            <h2>{{ restaurantInfo.name }}</h2>
            <p>{{ restaurantInfo.description || '用心准备每一份美味。' }}</p>
          </div>
          <div class="store-score"><strong>{{ restaurantInfo.averageScore || 0 }}</strong><span>评分</span></div>
        </div>
        <div class="store-meta">
          <span><i class="far fa-clock"></i>{{ restaurantInfo.deliveryEtaMinutes || 30 }}分钟送达</span>
          <span><i class="fas fa-motorcycle"></i>配送费￥{{ restaurantInfo.deliveryFee || 0 }}</span>
          <span><i class="fas fa-bag-shopping"></i>￥{{ restaurantInfo.minOrderAmount || restaurantInfo.minPrice || 0 }}起送</span>
        </div>
        <p v-if="restaurantInfo.notice" class="store-notice"><i class="fas fa-bullhorn"></i>{{ restaurantInfo.notice }}</p>
      </section>

      <nav v-if="menuGroups.length" class="mobile-menu-tabs" aria-label="菜品分类">
        <button
          v-for="group in menuGroups"
          :key="group.name"
          type="button"
          :class="{ active: (activeMenuCategory || menuGroups[0]?.name) === group.name }"
          @click="activeMenuCategory = group.name"
        >{{ group.name }}</button>
      </nav>

      <section class="mobile-menu-section">
        <div class="mobile-section-head">
          <div><strong>{{ activeMenuCategory || '店内精选' }}</strong><span>{{ activeMenuProducts.length }} 款菜品</span></div>
          <span>点击菜品查看详情</span>
        </div>
        <div v-if="activeMenuProducts.length" class="mobile-product-list">
          <button
            v-for="product in activeMenuProducts"
            :key="product.id"
            class="mobile-product-card"
            type="button"
            @click="openProduct(product)"
          >
            <img
              v-if="product.image_list?.[0]"
              :src="resolveImageUrl(product.image_list[0])"
              :alt="product.name"
            />
            <span v-else class="mobile-product-placeholder"><i class="fas fa-bowl-food"></i></span>
            <span class="mobile-product-info">
              <strong>{{ product.name }}</strong>
              <small>{{ product.description || '新鲜现做，欢迎品尝。' }}</small>
              <span class="mobile-product-bottom"><b>￥{{ product.price }}</b><i class="fas fa-plus"></i></span>
            </span>
          </button>
        </div>
        <el-empty v-else description="暂无菜品" :image-size="72" />
      </section>

      <section class="mobile-review-section">
        <div class="mobile-review-head">
          <div>
            <strong>用户评价</strong>
            <span>{{ reviewList.length }} 条真实反馈</span>
          </div>
          <div class="mobile-review-score">
            <b>{{ restaurantInfo.averageScore || 0 }}</b>
            <span>★★★★★</span>
          </div>
        </div>
        <div v-if="reviewList.length" class="mobile-review-list">
          <article v-for="review in reviewList.slice(0, 3)" :key="review.orderId" class="mobile-review-card">
            <div class="mobile-review-card-head">
              <strong>{{ review.customerName || '匿名顾客' }}</strong>
              <span>{{ formatReviewTime(review.createdTime) }}</span>
            </div>
            <div class="mobile-review-stars">{{ '★'.repeat(Math.round(review.score || 5)) }}</div>
            <p>{{ review.content || '顾客未填写文字评价。' }}</p>
            <div v-if="review.replyContent" class="mobile-merchant-reply">
              <b>商家回复</b>{{ review.replyContent }}
            </div>
          </article>
        </div>
        <div v-else class="mobile-review-empty"><i class="far fa-comment-dots"></i> 暂无评价，期待你的第一条反馈</div>
      </section>
    </main>

    <GoodsDetail
      :visible="productVisible"
      :productInfo="selectedProduct"
      :curStatus="curStatus"
      @close="closeProduct"
    />
  </section>

  <el-dialog
    v-else
    :model-value="visible"
    :title="restaurantInfo?.name || '店铺详情'"
    width="1180"
    :before-close="closeDialog"
    top="4vh"
    :class="[
      'restaurant-modal',
      { 'restaurant-modal--customer': curStatus === 'customer' }
    ]"
  >
    <div
      v-if="restaurantInfo"
      class="restaurant-detail"
      :class="{ 'restaurant-detail--customer': curStatus === 'customer' }"
    >
      <section class="hero">
        <div class="cover-wrap">
          <img
            v-if="restaurantInfo.cover || restaurantInfo.portrait"
            :src="resolveImageUrl(restaurantInfo.cover || restaurantInfo.portrait)"
            class="cover"
            alt="店铺封面"
          />
          <div v-else class="cover cover-placeholder">店铺封面</div>
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
              <p class="eyebrow">店铺档案</p>
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
            <p class="eyebrow">菜单展示</p>
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
                  :src="resolveImageUrl(product.image_list[0])"
                  class="product-image"
                  alt="商品图片"
                />
                <div v-else class="product-image product-placeholder">菜品</div>
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
        <el-empty v-else description="暂无菜单内容" />
      </section>

      <section class="reviews">
        <div class="section-head">
          <div>
            <p class="eyebrow">顾客声音</p>
            <h3>顾客评价与商家回复</h3>
          </div>
          <span class="review-count">{{ reviewList.length }} 条记录</span>
        </div>

        <div v-if="reviewList.length" class="review-list">
          <div v-for="review in reviewList" :key="review.orderId" class="review-card">
            <div class="review-title">
              <div>
                <strong>{{ review.customerName || "匿名顾客" }}</strong>
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
        <el-empty v-else description="暂无评价" />
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
import { resolveImageUrl } from "@/lib/imageHelper";

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
const activeMenuCategory = ref("");

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
const activeMenuProducts = computed(() => {
  if (!menuGroups.value.length) return [];
  const selectedGroup = menuGroups.value.find((group) => group.name === activeMenuCategory.value);
  return (selectedGroup || menuGroups.value[0]).products;
});

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
  activeMenuCategory.value = "";
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

/* Mobile menu: dialog, category chips, and food cards are rebuilt for a narrow viewport. */
@media (max-width: 768px) {
  :deep(.restaurant-modal) {
    width: calc(100vw - 24px) !important;
    max-width: 420px;
    margin: 12px auto !important;
    border-radius: 16px;
  }

  :deep(.restaurant-modal .el-dialog__header) {
    margin-right: 0;
    padding: 16px 44px 12px 16px;
    border-bottom: 1px solid var(--border, #ebeef5);
  }

  :deep(.restaurant-modal .el-dialog__title) {
    font-size: 17px;
    font-weight: 700;
  }

  :deep(.restaurant-modal .el-dialog__body) {
    max-height: calc(100vh - 116px);
    padding: 14px 16px 20px;
    overflow-y: auto;
  }

  .hero {
    display: block;
    padding: 12px;
    border-radius: 16px;
  }

  .cover-wrap {
    height: 148px;
    border-radius: 12px;
  }

  .cover {
    min-height: 0;
    height: 148px;
  }

  .cover-badge {
    inset: auto 8px 8px;
    padding: 7px 8px;
    border-radius: 10px;
    font-size: 11px;
  }

  .hero-main {
    padding: 12px 2px 2px;
  }

  .eyebrow {
    margin-bottom: 4px;
    font-size: 10px;
  }

  .hero-title-row h2 {
    font-size: 21px;
    font-family: inherit;
    font-weight: 800;
  }

  .score-box {
    min-width: 76px;
    padding: 8px;
    border-radius: 12px;
  }

  .score-box strong {
    font-size: 20px;
    font-family: inherit;
  }

  .score-box span {
    font-size: 10px;
  }

  .description,
  .notice,
  .detail-lines {
    margin-top: 9px;
    font-size: 12px;
    line-height: 1.55;
  }

  .notice {
    padding: 8px 10px;
    border-radius: 10px;
  }

  .info-grid {
    gap: 7px;
    margin-top: 12px;
  }

  .info-card {
    padding: 8px;
    border-radius: 10px;
  }

  .info-card span {
    font-size: 10px;
  }

  .info-card strong {
    margin-top: 3px;
    font-size: 14px;
    font-family: inherit;
  }

  .tags,
  .menu-tags {
    gap: 5px;
    margin-top: 10px;
  }

  .menu-tags {
    max-height: 28px;
    overflow-x: auto;
    flex-wrap: nowrap;
  }

  .menu-tags :deep(.el-tag) {
    flex: 0 0 auto;
  }

  .menu-section,
  .reviews {
    margin-top: 16px;
    padding: 14px 12px;
    border-radius: 16px;
  }

  .section-head {
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
  }

  .section-head h3 {
    font-size: 17px;
    font-family: inherit;
    font-weight: 800;
  }

  .group-head {
    margin-bottom: 9px;
  }

  .group-head h4 {
    font-size: 15px;
    font-family: inherit;
    font-weight: 800;
  }

  .menu-group + .menu-group {
    margin-top: 16px;
  }

  .product-list {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .product-card {
    align-items: center;
    min-height: 92px;
    gap: 10px;
    padding: 10px;
    border-radius: 12px;
  }

  .product-card:active {
    transform: scale(0.985);
    background: #fafafa;
  }

  .product-image {
    flex: 0 0 76px;
    width: 76px;
    height: 76px;
    border-radius: 10px;
  }

  .product-info {
    min-width: 0;
  }

  .product-header {
    gap: 8px;
    font-size: 14px;
  }

  .product-header span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .product-header strong {
    flex: 0 0 auto;
    color: var(--primary, #ef4444);
  }

  .product-info p {
    display: -webkit-box;
    margin: 5px 0 7px;
    overflow: hidden;
    font-size: 12px;
    line-height: 1.45;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  .review-list {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}

/* The customer app is previewed inside a phone frame on desktop too.  Apply the
   same compact layout by role instead of relying solely on viewport width. */
:deep(.restaurant-modal--customer) {
  width: min(390px, calc(100vw - 24px)) !important;
  max-width: 390px;
  margin: 12px auto !important;
  border-radius: 18px;
}

:deep(.restaurant-modal--customer .el-dialog__header) {
  margin-right: 0;
  padding: 16px 44px 12px 16px;
  border-bottom: 1px solid var(--border, #ebeef5);
}

:deep(.restaurant-modal--customer .el-dialog__title) {
  font-size: 17px;
  font-weight: 700;
}

:deep(.restaurant-modal--customer .el-dialog__body) {
  max-height: calc(100vh - 116px);
  padding: 14px 16px 20px;
  overflow-y: auto;
}

.restaurant-detail--customer {
  .hero {
    display: block;
    padding: 12px;
    border-radius: 16px;
  }

  .cover-wrap,
  .cover {
    height: 148px;
    min-height: 0;
  }

  .cover-wrap {
    border-radius: 12px;
  }

  .cover-badge {
    inset: auto 8px 8px;
    padding: 7px 8px;
    border-radius: 10px;
    font-size: 11px;
  }

  .hero-main {
    padding: 12px 2px 2px;
  }

  .eyebrow {
    margin-bottom: 4px;
    font-size: 10px;
  }

  .hero-title-row h2,
  .section-head h3,
  .group-head h4 {
    font-family: inherit;
    font-weight: 800;
  }

  .hero-title-row h2 {
    font-size: 21px;
  }

  .score-box {
    min-width: 76px;
    padding: 8px;
    border-radius: 12px;
  }

  .score-box strong {
    font-size: 20px;
    font-family: inherit;
  }

  .score-box span,
  .info-card span {
    font-size: 10px;
  }

  .description,
  .notice,
  .detail-lines {
    margin-top: 9px;
    font-size: 12px;
    line-height: 1.55;
  }

  .notice {
    padding: 8px 10px;
    border-radius: 10px;
  }

  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 7px;
    margin-top: 12px;
  }

  .info-card {
    padding: 8px;
    border-radius: 10px;
  }

  .info-card strong {
    margin-top: 3px;
    font-size: 14px;
    font-family: inherit;
  }

  .tags,
  .menu-tags {
    gap: 5px;
    margin-top: 10px;
  }

  .menu-tags {
    max-height: 28px;
    overflow-x: auto;
    flex-wrap: nowrap;
  }

  .menu-tags :deep(.el-tag) {
    flex: 0 0 auto;
  }

  .menu-section,
  .reviews {
    margin-top: 16px;
    padding: 14px 12px;
    border-radius: 16px;
  }

  .section-head {
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
  }

  .section-head h3 {
    font-size: 17px;
  }

  .group-head {
    margin-bottom: 9px;
  }

  .group-head h4 {
    font-size: 15px;
  }

  .menu-group + .menu-group {
    margin-top: 16px;
  }

  .product-list,
  .review-list {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .product-card {
    align-items: center;
    min-height: 92px;
    gap: 10px;
    padding: 10px;
    border-radius: 12px;
  }

  .product-card:active {
    transform: scale(0.985);
    background: #fafafa;
  }

  .product-image {
    flex: 0 0 76px;
    width: 76px;
    height: 76px;
    border-radius: 10px;
  }

  .product-info {
    min-width: 0;
  }

  .product-header {
    gap: 8px;
    font-size: 14px;
  }

  .product-header span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .product-header strong {
    flex: 0 0 auto;
    color: var(--primary, #ef4444);
  }

  .product-info p {
    display: -webkit-box;
    margin: 5px 0 7px;
    overflow: hidden;
    font-size: 12px;
    line-height: 1.45;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }
}

/* Customer store detail is a real in-app page, not a resized desktop dialog. */
.mobile-store-page {
  position: absolute;
  inset: 0;
  z-index: 30;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f7f8fa;
  color: #1f2937;
}

.mobile-store-nav {
  height: 52px;
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  font-weight: 800;
}

.nav-icon-button {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 50%;
  background: #f6f7f8;
  color: #374151;
  cursor: pointer;
}

.nav-back-button {
  padding: 0 2px 4px 0;
  color: #1f2937;
  font-size: 32px;
  font-weight: 300;
  line-height: 24px;
}

.mobile-store-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-bottom: 22px;
}

.store-banner {
  position: relative;
  height: 176px;
  overflow: hidden;
  background: linear-gradient(135deg, #f97316, #ef4444);
}

.store-banner img,
.store-banner-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-banner-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255,255,255,.86);
  font-size: 48px;
  background: linear-gradient(135deg, #f97316, #ef4444);
}

.store-banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 42%, rgba(17,24,39,.58));
}

.store-banner-status {
  position: absolute;
  right: 14px;
  bottom: 12px;
  left: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.store-banner-status span:first-child {
  padding: 4px 9px;
  border-radius: 999px;
}

.is-open { background: #16a34a; }
.is-resting { background: #6b7280; }

.store-summary {
  padding: 16px;
  background: #fff;
}

.store-title-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  justify-content: space-between;
}

.store-title-row h2 {
  margin: 0;
  font-size: 20px;
  line-height: 1.3;
}

.store-title-row p {
  display: -webkit-box;
  margin: 5px 0 0;
  overflow: hidden;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.store-score {
  min-width: 54px;
  padding: 5px 3px;
  border-radius: 10px;
  background: #fff7ed;
  color: #f97316;
  text-align: center;
}

.store-score strong,
.store-score span { display: block; }
.store-score strong { font-size: 18px; line-height: 1.1; }
.store-score span { margin-top: 2px; font-size: 10px; }

.store-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 7px 12px;
  margin-top: 12px;
  color: #6b7280;
  font-size: 11px;
}

.store-meta span { white-space: nowrap; }
.store-meta i { margin-right: 4px; color: #ef4444; }

.store-notice {
  margin: 12px 0 0;
  padding: 8px 10px;
  border-radius: 8px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 12px;
  line-height: 1.45;
}

.store-notice i { margin-right: 6px; }

.mobile-menu-tabs {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 13px 16px 11px;
  background: #fff;
  border-top: 8px solid #f7f8fa;
  border-bottom: 1px solid #f0f0f0;
  scrollbar-width: none;
}

.mobile-menu-tabs::-webkit-scrollbar { display: none; }

.mobile-menu-tabs button {
  position: relative;
  flex: 0 0 auto;
  padding: 0 0 7px;
  border: 0;
  background: transparent;
  color: #9ca3af;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.mobile-menu-tabs button.active { color: #ef4444; font-weight: 800; }
.mobile-menu-tabs button.active::after {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 3px;
  border-radius: 3px;
  background: #ef4444;
  content: '';
}

.mobile-menu-section { padding: 14px 16px 0; }

.mobile-section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
}

.mobile-section-head strong { font-size: 16px; }
.mobile-section-head div span,
.mobile-section-head > span { color: #9ca3af; font-size: 11px; }
.mobile-section-head div span { margin-left: 6px; }

.mobile-product-list { display: flex; flex-direction: column; gap: 10px; }

.mobile-product-card {
  display: flex;
  width: 100%;
  min-height: 102px;
  gap: 11px;
  padding: 10px;
  border: 1px solid #f0f0f0;
  border-radius: 14px;
  background: #fff;
  color: inherit;
  text-align: left;
  box-shadow: 0 1px 2px rgba(0,0,0,.02);
  cursor: pointer;
}

.mobile-product-card:active { transform: scale(.985); background: #fffafa; }

.mobile-product-card > img,
.mobile-product-placeholder {
  flex: 0 0 82px;
  width: 82px;
  height: 82px;
  border-radius: 10px;
  object-fit: cover;
}

.mobile-product-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fff1f2;
  color: #ef4444;
  font-size: 24px;
}

.mobile-product-info {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.mobile-product-info > strong {
  overflow: hidden;
  font-size: 14px;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-product-info > small {
  display: -webkit-box;
  margin-top: 5px;
  overflow: hidden;
  color: #9ca3af;
  font-size: 11px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.mobile-product-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.mobile-product-bottom b { color: #ef4444; font-size: 16px; }
.mobile-product-bottom i {
  width: 23px;
  height: 23px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
}

.mobile-review-section {
  margin-top: 16px;
  padding: 16px;
  background: #fff;
  border-top: 8px solid #f7f8fa;
}

.mobile-review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.mobile-review-head > div:first-child strong,
.mobile-review-head > div:first-child span {
  display: block;
}

.mobile-review-head > div:first-child strong { font-size: 17px; }
.mobile-review-head > div:first-child span { margin-top: 2px; color: #9ca3af; font-size: 11px; }

.mobile-review-score { text-align: right; }
.mobile-review-score b { margin-right: 4px; color: #f97316; font-size: 18px; }
.mobile-review-score span { color: #f59e0b; font-size: 10px; letter-spacing: -1px; }

.mobile-review-list { display: flex; flex-direction: column; gap: 10px; }

.mobile-review-card {
  padding: 12px;
  border-radius: 12px;
  background: #f9fafb;
}

.mobile-review-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #1f2937;
  font-size: 12px;
}

.mobile-review-card-head span { color: #9ca3af; font-size: 10px; }
.mobile-review-stars { margin-top: 4px; color: #f59e0b; font-size: 11px; letter-spacing: 1px; }

.mobile-review-card > p {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 12px;
  line-height: 1.55;
}

.mobile-merchant-reply {
  margin-top: 8px;
  padding: 7px 8px;
  border-left: 2px solid #ef4444;
  border-radius: 0 7px 7px 0;
  background: #fff1f2;
  color: #9f1239;
  font-size: 11px;
  line-height: 1.5;
}

.mobile-merchant-reply b { margin-right: 5px; }

.mobile-review-empty {
  padding: 18px 0 6px;
  color: #9ca3af;
  font-size: 12px;
  text-align: center;
}

.mobile-review-empty i { margin-right: 4px; color: #ef4444; }
</style>
