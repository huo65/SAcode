<template>
  <div class="customer-review">
    <!-- 页头 -->
    <div class="review-header">
      <h3>我的评价</h3>
    </div>

    <!-- 评价列表 -->
    <div class="review-list" v-if="reviews.length">
      <div class="review-card" v-for="review in reviews" :key="review.id || review.reviewId">
        <div class="review-card-head">
          <span class="review-shop">{{ review.restaurantName || review.shopName || '餐厅' }}</span>
          <span class="review-date">{{ review.createTime || review.time }}</span>
        </div>
        <div class="review-rating">
          <el-rate
            :model-value="review.rating || review.score || 5"
            disabled
            :colors="['#EF4444', '#F97316', '#EF4444']"
            size="small"
          />
        </div>
        <div class="review-content" v-if="review.content || review.comment">
          {{ review.content || review.comment }}
        </div>
        <div class="review-reply" v-if="review.reply">
          <div class="reply-label">商家回复：</div>
          <div class="reply-content">{{ review.reply }}</div>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <i class="fas fa-star"></i>
      <span>暂无评价记录</span>
      <p>完成订单后可以对商家进行评价</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { userInfo } from '@/store';
import { Review } from '@/api/apis';
import fetch from '@/api/fetch';

const reviews = ref([]);

const loadReviews = () => {
  if (!userInfo.value?.id) return;
  fetch(Review.customerList, { usrId: userInfo.value.id }).then((data) => {
    reviews.value = data?.data || data?.list || (Array.isArray(data) ? data : []);
  }).catch(() => {
    reviews.value = [];
  });
};

onMounted(() => {
  loadReviews();
});
</script>

<style lang="less" scoped>
.customer-review {
  padding: 0 0 24px;
}

.review-header {
  padding: 4px 0 20px;

  h3 {
    margin: 0;
    font-size: 22px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary, #1F2937);
  }
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-card {
  padding: 18px 20px;
  background: var(--card, #fff);
  border-radius: 14px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.review-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.review-shop {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, #1F2937);
}

.review-date {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
}

.review-rating {
  margin-bottom: 8px;
}

.review-content {
  font-size: 14px;
  color: var(--text-secondary, #6B7280);
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-reply {
  padding: 12px 14px;
  background: #F8FAFC;
  border-radius: 10px;
  border-left: 3px solid var(--primary, #EF4444);
}

.reply-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--primary, #EF4444);
  margin-bottom: 4px;
}

.reply-content {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  line-height: 1.6;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 60px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;
  text-align: center;

  i {
    font-size: 48px;
    opacity: 0.3;
  }

  p {
    font-size: 12px;
    margin: 4px 0 0;
  }
}
</style>
