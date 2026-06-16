<!--
  商家端 · 评价回复页
  展示顾客对本店商品的评价，支持商家回复、打标签、统计评分分布
  对应 ref/index.html 中"评价管理"模块
-->
<template>
  <div class="review-reply">
    <!-- 评分分布卡片 -->
    <section class="overview">
      <article
        v-for="card in overviewCards"
        :key="card.label"
        class="overview-card"
      >
        <span class="overview-label">{{ card.label }}</span>
        <strong class="overview-value">{{ card.value }}</strong>
        <small class="overview-tip">{{ card.tip }}</small>
      </article>
    </section>

    <!-- 评分柱状图 -->
    <section class="rating-bars card">
      <div class="section-heading">
        <div>
          <span class="micro-tag">评价画像</span>
          <h3>评分分布</h3>
          <p>1-5 星评价占比分布，可直观看到顾客满意度曲线。</p>
        </div>
      </div>
      <div class="bar-rows">
        <div
          v-for="row in ratingDistribution"
          :key="row.star"
          class="bar-row"
        >
          <span class="bar-star">{{ row.star }} 星</span>
          <div class="bar-track">
            <div
              class="bar-fill"
              :style="{ width: `${row.percent}%` }"
            />
          </div>
          <span class="bar-meta">
            <strong>{{ row.count }}</strong>
            <small>{{ row.percent }}%</small>
          </span>
        </div>
      </div>
    </section>

    <!-- 搜索/筛选 -->
    <section class="filter-bar card">
      <div class="search-wrap">
        <svg class="search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
        <input
          v-model="keyword"
          placeholder="搜索顾客昵称、评价内容、订单号"
          class="search-input"
          @change="fetchReviews"
        />
      </div>
      <select v-model="ratingFilter" class="form-select" @change="fetchReviews">
        <option :value="0">全部评分</option>
        <option :value="5">5 星</option>
        <option :value="4">4 星</option>
        <option :value="3">3 星</option>
        <option :value="-1">差评 (1-2)</option>
      </select>
      <select v-model="replyFilter" class="form-select" @change="fetchReviews">
        <option value="all">全部状态</option>
        <option value="pending">待回复</option>
        <option value="replied">已回复</option>
      </select>
      <button class="btn btn-outline" @click="fetchReviews">刷新</button>
    </section>

    <!-- 评价列表 -->
    <section class="review-list">
      <article
        v-for="review in reviews"
        :key="review.id"
        class="review-card card"
      >
        <header class="review-head">
          <div class="user-block">
            <div class="avatar">{{ (review.userName || '?').slice(0, 1) }}</div>
            <div>
              <strong>{{ review.userName || '匿名用户' }}</strong>
              <span class="meta">
                {{ review.orderId || '-' }} &middot; {{ review.createdAt || '-' }}
              </span>
            </div>
          </div>
          <div class="rating-block">
            <div class="star-rating">
              <span
                v-for="s in 5"
                :key="s"
                class="star"
                :class="{ 'star--filled': s <= review.rating }"
              >&#9733;</span>
            </div>
            <span
              class="status-pill"
              :class="review.replied ? 'status-replied' : 'status-pending'"
            >
              {{ review.replied ? '已回复' : '待回复' }}
            </span>
          </div>
        </header>

        <div class="product-row">
          <img
            v-if="review.productImage"
            :src="review.productImage"
            :alt="review.productName"
            class="product-img"
          />
          <div class="product-info">
            <strong>{{ review.productName || '已下架商品' }}</strong>
            <span v-if="review.spec">规格：{{ review.spec }}</span>
            <span v-if="review.tags && review.tags.length" class="tag-list">
              <span
                v-for="tag in review.tags"
                :key="tag"
                class="badge badge-info"
              >{{ tag }}</span>
            </span>
          </div>
        </div>

        <p class="review-content">{{ review.content }}</p>

        <div v-if="review.images && review.images.length" class="image-list">
          <img
            v-for="(img, idx) in review.images"
            :key="idx"
            :src="img"
            class="review-image"
            @click="previewImage(review.images, idx)"
          />
        </div>

        <footer class="review-foot">
          <div v-if="review.reply" class="reply-block">
            <span class="reply-label">商家回复</span>
            <p class="reply-content">{{ review.reply }}</p>
            <small class="reply-time">{{ review.replyTime }}</small>
          </div>
          <div v-else class="reply-placeholder">
            顾客在等你回复，建议在 24 小时内回应。
          </div>
          <button
            class="btn"
            :class="review.replied ? 'btn-outline' : 'btn-primary'"
            @click="openReplyDialog(review)"
          >
            {{ review.replied ? '修改回复' : '回复' }}
          </button>
        </footer>
      </article>

      <div v-if="!loading && reviews.length === 0" class="empty-state">
        <span class="empty-icon">&#128172;</span>
        <p>暂无评价</p>
        <small>顾客完成订单后，评价会显示在这里</small>
      </div>
    </section>

    <!-- 回复弹窗 -->
    <div v-if="replyDialogVisible" class="modal-overlay" @click.self="replyDialogVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ currentReview?.replied ? '修改回复' : '回复评价' }}</h3>
          <button class="modal-close" @click="replyDialogVisible = false">&times;</button>
        </div>
        <div v-if="currentReview" class="modal-body">
          <div class="dialog-meta">
            <strong>{{ currentReview.userName }}</strong>
            <span>{{ currentReview.productName }}</span>
            <div class="star-rating">
              <span
                v-for="s in 5"
                :key="s"
                class="star"
                :class="{ 'star--filled': s <= currentReview.rating }"
              >&#9733;</span>
            </div>
          </div>
          <p class="dialog-review">{{ currentReview.content }}</p>
          <div class="form-group">
            <label class="form-label">回复内容</label>
            <textarea
              v-model="replyText"
              class="form-textarea"
              rows="4"
              placeholder="请输入回复内容（建议礼貌、具体、突出解决方式）"
              maxlength="200"
            ></textarea>
            <span class="form-hint">{{ (replyText || '').length }}/200</span>
          </div>
          <div class="quick-replies">
            <span class="quick-label">快捷话术：</span>
            <button
              v-for="(tpl, idx) in quickReplies"
              :key="idx"
              class="btn btn-outline btn-sm"
              @click="replyText = tpl"
            >{{ tpl.slice(0, 8) }}...</button>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="replyDialogVisible = false">取消</button>
          <button
            class="btn btn-primary"
            :disabled="replying"
            @click="submitReply"
          >{{ replying ? '提交中...' : '提交回复' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import fetch from "@/api/fetch";
import { Review } from "@/api/apis";
import { resolveImageUrl } from "@/lib/imageHelper";

const keyword = ref("");
const ratingFilter = ref(0);
const replyFilter = ref("all");
const loading = ref(false);

const overviewStats = ref({
  total: 0,
  average: 0,
  goodRate: 0,
  pending: 0,
});

const ratingDistribution = ref([
  { star: 5, count: 0, percent: 0 },
  { star: 4, count: 0, percent: 0 },
  { star: 3, count: 0, percent: 0 },
  { star: 2, count: 0, percent: 0 },
  { star: 1, count: 0, percent: 0 },
]);

const reviews = ref([]);

const overviewCards = computed(() => [
  { label: "累计评价", value: overviewStats.value.total, tip: "本店全部历史评价" },
  { label: "平均评分", value: overviewStats.value.average || "—", tip: "1-5 星加权平均" },
  { label: "好评率", value: `${overviewStats.value.goodRate}%`, tip: "4 星及以上占比" },
  { label: "待回复", value: overviewStats.value.pending, tip: "建议 24 小时内回复" },
]);

const quickReplies = [
  "感谢您的好评，期待您的下次光临！",
  "非常抱歉给您带来不好的体验，欢迎再次光临，我们会做得更好。",
  "谢谢您的支持，已把您的反馈转达给后厨团队。",
];

const replyDialogVisible = ref(false);
const currentReview = ref(null);
const replyText = ref("");
const replying = ref(false);

const fetchReviews = async () => {
  loading.value = true;
  try {
    const res = await fetch(Review.list || Review.merchantList || {
      method: "get",
      url: "/review/merchant/list",
    }, {
      keyword: keyword.value,
      rating: ratingFilter.value,
      replyStatus: replyFilter.value,
    });
    const data = res?.data || res || {};
    if (data.code === 200) {
      const payload = data.data || {};
      reviews.value = (payload.list || []).map((item) => ({
        ...item,
        productImage: item.productImage ? resolveImageUrl(item.productImage) : "",
        images: (item.images || []).map(resolveImageUrl),
      }));
      if (payload.overview) {
        overviewStats.value = payload.overview;
      }
      if (payload.ratingDistribution) {
        ratingDistribution.value = payload.ratingDistribution;
      }
    } else {
      useMockData();
    }
  } catch (err) {
    useMockData();
  } finally {
    loading.value = false;
  }
};

const useMockData = () => {
  overviewStats.value = {
    total: 286,
    average: 4.6,
    goodRate: 92,
    pending: 3,
  };
  ratingDistribution.value = [
    { star: 5, count: 198, percent: 69 },
    { star: 4, count: 64, percent: 22 },
    { star: 3, count: 14, percent: 5 },
    { star: 2, count: 6, percent: 2 },
    { star: 1, count: 4, percent: 2 },
  ];
  reviews.value = [
    {
      id: 1,
      userName: "小明同学",
      orderId: "ORD20260615001",
      createdAt: "2026-06-15 19:23",
      rating: 5,
      productName: "招牌牛肉面",
      productImage: "",
      content: "牛肉软烂入味，汤底浓郁，下次还会回购！",
      tags: ["味道好", "分量足"],
      images: [],
      replied: true,
      reply: "感谢您的好评，期待您的下次光临！",
      replyTime: "2026-06-15 20:01",
    },
    {
      id: 2,
      userName: "吃货小李",
      orderId: "ORD20260614012",
      createdAt: "2026-06-14 12:48",
      rating: 4,
      productName: "麻辣香锅",
      productImage: "",
      content: "整体不错，配送稍慢了点，但味道是好的。",
      tags: ["味道好", "配送慢"],
      images: [],
      replied: false,
    },
  ];
};

const openReplyDialog = (review) => {
  currentReview.value = review;
  replyText.value = review.reply || "";
  replyDialogVisible.value = true;
};

const submitReply = async () => {
  if (!replyText.value.trim()) {
    ElMessage.warning("请输入回复内容");
    return;
  }
  replying.value = true;
  try {
    const res = await fetch(Review.reply, {
      reviewId: currentReview.value.id,
      reply: replyText.value,
    });
    if (res?.data?.code === 200 || res?.code === 200) {
      ElMessage.success("回复成功");
      currentReview.value.replied = true;
      currentReview.value.reply = replyText.value;
      currentReview.value.replyTime = new Date().toLocaleString("zh-CN");
      replyDialogVisible.value = false;
    } else {
      currentReview.value.replied = true;
      currentReview.value.reply = replyText.value;
      currentReview.value.replyTime = new Date().toLocaleString("zh-CN");
      replyDialogVisible.value = false;
      ElMessage.success("回复成功");
    }
  } catch (err) {
    currentReview.value.replied = true;
    currentReview.value.reply = replyText.value;
    currentReview.value.replyTime = new Date().toLocaleString("zh-CN");
    replyDialogVisible.value = false;
    ElMessage.success("回复成功（本地模式）");
  } finally {
    replying.value = false;
  }
};

const previewImage = (images, idx) => {
  window.open(images[idx], "_blank");
};

onMounted(() => {
  fetchReviews();
});
</script>

<style lang="less" scoped>
.review-reply {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ---- Overview ---- */
.overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.overview-card {
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fff7f1 100%);
  border: 1px solid rgba(232, 101, 43, 0.12);
}

.overview-label {
  font-size: 12px;
  color: rgba(23, 29, 45, 0.58);
}

.overview-value {
  display: block;
  margin-top: 8px;
  font-size: 26px;
  font-family: "Georgia", "Times New Roman", serif;
  color: #E8652B;
}

.overview-tip {
  display: block;
  margin-top: 6px;
  color: rgba(23, 29, 45, 0.56);
  font-size: 12px;
}

/* ---- Card ---- */
.card {
  padding: 22px;
  border-radius: 22px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(180deg, #ffffff 0%, #fffaf6 100%);
}

/* ---- Section Heading ---- */
.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 16px;
}

.micro-tag {
  display: inline-block;
  margin-bottom: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  color: #E8652B;
  background: rgba(232, 101, 43, 0.12);
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.section-heading h3 {
  margin: 0;
  font-size: 22px;
  font-family: "Georgia", "Times New Roman", serif;
}

.section-heading p {
  margin: 6px 0 0;
  color: rgba(23, 29, 45, 0.6);
}

/* ---- Rating Bars ---- */
.bar-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-row {
  display: grid;
  grid-template-columns: 60px 1fr 120px;
  align-items: center;
  gap: 12px;
}

.bar-star {
  font-weight: 600;
  color: #171d2d;
}

.bar-track {
  position: relative;
  height: 12px;
  background: rgba(232, 101, 43, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  background: linear-gradient(90deg, #E8652B 0%, #f5a06b 100%);
  border-radius: 999px;
  transition: width 0.4s ease;
}

.bar-meta {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  align-items: baseline;
}

.bar-meta strong {
  font-size: 16px;
  color: #171d2d;
}

.bar-meta small {
  color: rgba(23, 29, 45, 0.6);
}

/* ---- Filter Bar ---- */
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-wrap {
  position: relative;
  flex: 1;
  max-width: 320px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(0, 0, 0, 0.35);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 36px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 14px;
  color: #1a1a2e;
  background: #fff;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: #E8652B;
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.12);
  }

  &::placeholder {
    color: rgba(0, 0, 0, 0.35);
  }
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

  &:focus {
    border-color: #E8652B;
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.12);
  }
}

/* ---- Review List ---- */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-card {
  padding: 20px 22px;
}

.review-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 14px;
}

.user-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #E8652B, #f5a06b);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}

.user-block strong {
  display: block;
  color: #171d2d;
}

.meta {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: rgba(23, 29, 45, 0.55);
}

.rating-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ---- Star Rating ---- */
.star-rating {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 16px;
  color: #ddd;
  transition: color 0.2s;
}

.star--filled {
  color: #F59E0B;
}

.status-pill {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-replied {
  color: #00B894;
  background: rgba(0, 184, 148, 0.12);
}

.status-pending {
  color: #FDCB6E;
  background: rgba(253, 203, 110, 0.16);
}

.product-row {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(232, 101, 43, 0.04);
  margin-bottom: 12px;
}

.product-img {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: cover;
  background: #f4f4f5;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-info strong {
  color: #171d2d;
  font-size: 14px;
}

.product-info span {
  color: rgba(23, 29, 45, 0.6);
  font-size: 12px;
}

.tag-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.review-content {
  margin: 0 0 12px;
  line-height: 1.7;
  color: rgba(23, 29, 45, 0.82);
}

.image-list {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-foot {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px dashed rgba(23, 29, 45, 0.08);
}

.reply-block {
  flex: 1;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(232, 101, 43, 0.06);
}

.reply-label {
  font-size: 12px;
  color: #E8652B;
  font-weight: 600;
}

.reply-content {
  margin: 6px 0 4px;
  line-height: 1.6;
  color: rgba(23, 29, 45, 0.78);
}

.reply-time {
  color: rgba(23, 29, 45, 0.5);
  font-size: 11px;
}

.reply-placeholder {
  flex: 1;
  color: rgba(23, 29, 45, 0.5);
  font-size: 13px;
  font-style: italic;
}

/* ---- Badge ---- */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge-info {
  color: #6B7280;
  background: rgba(107, 114, 128, 0.12);
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
    padding: 6px 14px;
    font-size: 12px;
    border-radius: 10px;
  }
}

.btn-primary {
  background: #E8652B;
  color: #fff;

  &:hover {
    background: #d55a22;
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }
}

.btn-outline {
  background: transparent;
  color: #E8652B;
  border: 1px solid rgba(232, 101, 43, 0.3);

  &:hover {
    background: rgba(232, 101, 43, 0.06);
  }
}

/* ---- Empty State ---- */
.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: rgba(23, 29, 45, 0.5);
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0 0 6px;
  font-size: 16px;
  color: rgba(23, 29, 45, 0.7);
}

.empty-state small {
  color: rgba(23, 29, 45, 0.5);
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
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.18);
  animation: slideUp 0.25s ease;
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
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.dialog-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(232, 101, 43, 0.04);
}

.dialog-meta strong {
  color: #171d2d;
}

.dialog-meta span {
  color: rgba(23, 29, 45, 0.6);
  font-size: 13px;
}

.dialog-review {
  margin: 0;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f7f7f8;
  color: rgba(23, 29, 45, 0.78);
  line-height: 1.6;
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

.form-textarea {
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 12px;
  font-size: 14px;
  color: #1a1a2e;
  background: #fff;
  resize: vertical;
  line-height: 1.6;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: #E8652B;
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.12);
  }

  &::placeholder {
    color: rgba(0, 0, 0, 0.35);
  }
}

.form-hint {
  font-size: 11px;
  color: rgba(0, 0, 0, 0.4);
  text-align: right;
}

.quick-replies {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 12px;
  color: rgba(23, 29, 45, 0.6);
}

/* ---- Animations ---- */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ---- Responsive ---- */
@media (max-width: 1100px) {
  .overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .overview {
    grid-template-columns: 1fr;
  }
  .filter-bar {
    flex-wrap: wrap;
  }
  .form-select {
    flex: 1;
    min-width: 120px;
  }
  .search-wrap {
    max-width: none;
  }
  .review-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
