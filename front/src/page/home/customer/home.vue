<template>
  <div class="customer-home">
    <!-- Hero 区域 -->
    <div class="home-hero">
      <div class="hero-left">
        <p class="eyebrow">西电外卖</p>
        <h2>{{ greeting }}, {{ userInfo.name || '同学' }}</h2>
        <p class="hero-desc">发现附近好店，轻松点餐下单，配送直达宿舍楼。</p>
      </div>
      <div class="hero-right">
        <div class="hero-search">
          <i class="fas fa-search"></i>
          <el-input
            v-model="searchText"
            placeholder="搜索商家、菜品"
            :prefix-icon="null"
            clearable
            @keyup.enter="onSearch"
          />
        </div>
        <div class="hero-time">
          <i class="far fa-clock"></i>
          <span>{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <!-- Banner 横幅 -->
    <div class="home-banner">
      <div class="banner-content">
        <div class="banner-text">
          <div class="banner-title">新用户专享</div>
          <div class="banner-sub">首单立减15元，限时抢享</div>
          <el-button class="banner-btn" round @click="$router.push('/home/customer/restaurants')">
            立即点餐 <i class="fas fa-chevron-right" style="font-size:10px;margin-left:4px"></i>
          </el-button>
        </div>
      </div>
      <div class="banner-deco"></div>
      <div class="banner-deco2"></div>
    </div>

    <!-- 分类图标 -->
    <div class="category-grid">
      <div
        v-for="cat in categories"
        :key="cat.label"
        class="cat-item"
        @click="onCategoryClick(cat)"
      >
        <div class="cat-icon" :style="{ background: cat.bg }">{{ cat.emoji }}</div>
        <div class="cat-label">{{ cat.label }}</div>
      </div>
    </div>

    <!-- 附近好店 -->
    <div class="section-head">
      <h3 class="section-title">附近好店</h3>
      <span class="section-more" @click="$router.push('/home/customer/restaurants')">
        更多 <i class="fas fa-chevron-right" style="font-size:9px"></i>
      </span>
    </div>

    <div class="restaurant-grid" v-if="restaurants.length">
      <div
        v-for="r in restaurants"
        :key="r.id"
        class="rest-card"
        @click="onRestaurantClick(r)"
      >
        <div class="rest-img">
          <img v-if="r.image" :src="r.image" alt="" />
          <div v-else class="rest-img-placeholder" :style="{ background: r.placeholderBg }">
            {{ r.emoji || '🍽️' }}
          </div>
          <div class="rest-tag" v-if="r.tag">{{ r.tag }}</div>
          <div class="rest-discount" v-if="r.discount">{{ r.discount }}</div>
        </div>
        <div class="rest-info">
          <div class="rest-name">{{ r.name }}</div>
          <div class="rest-meta">
            <span class="rating" v-if="r.rating"><i class="fas fa-star"></i> {{ r.rating }}</span>
            <span v-if="r.monthSales">月售{{ r.monthSales }}</span>
            <span v-if="r.minOrder">¥{{ r.minOrder }}起送</span>
            <span v-if="r.distance">{{ r.distance }}</span>
          </div>
          <div class="rest-tags" v-if="r.cuisine || r.deliveryTime">
            <span v-if="r.cuisine">{{ r.cuisine }}</span>
            <span v-if="r.deliveryTime">{{ r.deliveryTime }}分钟</span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <i class="fas fa-store-slash"></i>
      <span>暂无餐厅数据</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { userInfo } from '@/store';
import { Restaurant as RestaurantApi, Product as ProductApi } from '@/api/apis';
import fetch from '@/api/fetch';
import { resolveImageUrl } from '@/lib/imageHelper';

const router = useRouter();
const searchText = ref('');
const currentTime = ref('');
const restaurants = ref([]);
let timer = null;

const greeting = computed(() => {
  const h = new Date().getHours();
  if (h < 6) return '夜深了';
  if (h < 11) return '早上好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
});

const updateTime = () => {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  currentTime.value = `${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const categories = [
  { label: '中餐', emoji: '🍜', bg: '#FEF2F2', type: '中餐' },
  { label: '西式快餐', emoji: '🍔', bg: '#FFF7ED', type: '西式快餐' },
  { label: '轻食', emoji: '🥗', bg: '#ECFDF5', type: '轻食' },
  { label: '日韩', emoji: '🍣', bg: '#EFF6FF', type: '日韩料理' },
  { label: '饮品', emoji: '🧋', bg: '#F5F3FF', type: '饮品' },
  { label: '甜点', emoji: '🍰', bg: '#FFFBEB', type: '甜点' },
  { label: '麻辣', emoji: '🔥', bg: '#FFF1F2', type: '麻辣烫' },
  { label: '煲仔', emoji: '🥘', bg: '#F0FDF4', type: '煲仔饭' },
  { label: '卷饼', emoji: '🌯', bg: '#EFF6FF', type: '卷饼' },
  { label: '全部分类', emoji: '📋', bg: '#F8FAFC', type: '' },
];

// 随机 emoji 和渐变色列表（为没有图片的餐厅生成占位）
const placeholderBgs = [
  'linear-gradient(135deg,#FFECD2,#FCB69F)',
  'linear-gradient(135deg,#C6FFDD,#FBD786)',
  'linear-gradient(135deg,#FF9A9E,#FECFEF)',
  'linear-gradient(135deg,#A8EDEA,#FED6E3)',
  'linear-gradient(135deg,#D4FC79,#96E6A1)',
];
const placeholderEmojis = ['🍗', '🐟', '🍔', '🍣', '🥗', '🍜', '🍰', '🧋'];

const loadRestaurants = () => {
  fetch(RestaurantApi.list).then((data) => {
    const list = data?.restaurant_list || data?.data || data?.list || data || [];
    restaurants.value = (Array.isArray(list) ? list : []).map((r, idx) => ({
      id: r.id || r.restaurantId,
      name: r.name || r.restaurantName || '未知餐厅',
      image: resolveImageUrl(r.cover || r.logo || r.portrait || r.image || r.imageUrl || ''),
      rating: r.averageScore || r.rating || r.score || null,
      monthSales: r.reviewCount || r.monthSales || r.saleNum || null,
      minOrder: r.minOrderAmount || r.minOrder || r.startPrice || 0,
      distance: r.distanceKm ? `${Number(r.distanceKm).toFixed(1)}km` : r.distance || null,
      cuisine: r.menuCategories?.[0] || r.cuisine || r.categoryName || null,
      deliveryTime: r.deliveryEtaMinutes || r.deliveryTime || r.avgDeliveryTime || null,
      tag: r.status === 1 ? '营业中' : (idx === 0 ? '品牌' : idx === 1 ? '人气' : ''),
      discount: r.promoText || r.discount || (idx < 2 ? `满${30 + idx * 20}减${5 + idx * 3}` : ''),
      placeholderBg: placeholderBgs[idx % placeholderBgs.length],
      emoji: placeholderEmojis[idx % placeholderEmojis.length],
    }));
  }).catch(() => {
    restaurants.value = [];
  });
};

const onSearch = () => {
  if (!searchText.value.trim()) return;
  router.push({ path: '/home/customer/restaurants', query: { keyword: searchText.value } });
};

const onCategoryClick = (cat) => {
  if (cat.type) {
    router.push({ path: '/home/customer/restaurants', query: { category: cat.type } });
  } else {
    router.push('/home/customer/restaurants');
  }
};

const onRestaurantClick = (r) => {
  // 跳转到餐厅详情（当前复用 Restaurant 组件，通过 query 传 id）
  router.push({ path: '/home/customer/restaurants', query: { id: r.id } });
};

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 60000);
  loadRestaurants();
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<style lang="less" scoped>
.customer-home {
  padding: 0 0 24px;
}

// ============ Hero ============
.home-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 4px 20px;
  gap: 24px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--primary, #EF4444);
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  font-weight: 600;
}

.home-hero h2 {
  margin: 0;
  font-size: 30px;
  font-family: "Georgia", "Times New Roman", serif;
  color: var(--text-primary, #1F2937);
}

.hero-desc {
  max-width: 480px;
  margin: 10px 0 0;
  color: var(--text-secondary, #6B7280);
  line-height: 1.8;
  font-size: 14px;
}

.hero-right {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}

.hero-search {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 280px;

  i {
    color: var(--text-muted, #9CA3AF);
    font-size: 14px;
  }

  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 20px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06);
    }
  }
}

.hero-time {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--border, #F0F0F0);
  border-radius: 999px;
  color: var(--text-secondary, #6B7280);
  font-size: 13px;
  font-weight: 600;

  i { color: var(--primary, #EF4444); }
}

// ============ Banner ============
.home-banner {
  position: relative;
  height: 130px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--primary, #EF4444), var(--accent, #F97316));
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 0 28px;
  margin-bottom: 24px;
}

.banner-text {
  position: relative;
  z-index: 1;
  color: white;
}

.banner-title {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.3;
}

.banner-sub {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}

.banner-btn {
  margin-top: 12px;
  background: white !important;
  color: var(--primary, #EF4444) !important;
  border: none !important;
  font-weight: 700 !important;
  font-size: 12px !important;
}

.banner-deco {
  position: absolute;
  right: -10px;
  bottom: -10px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}

.banner-deco2 {
  position: absolute;
  right: 40px;
  top: -20px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

// ============ 分类图标 ============
.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}

.cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.15s ease;

  &:hover {
    transform: translateY(-2px);
  }
}

.cat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.cat-label {
  font-size: 12px;
  color: var(--text-secondary, #6B7280);
  font-weight: 500;
}

// ============ Section ============
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #1F2937);
  font-family: "Georgia", "Times New Roman", serif;
}

.section-more {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;

  &:hover {
    color: var(--primary, #EF4444);
  }
}

// ============ 餐厅卡片 ============
.restaurant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.rest-card {
  background: var(--card, #fff);
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }
}

.rest-img {
  height: 120px;
  position: relative;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.rest-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  opacity: 0.7;
}

.rest-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: var(--primary, #EF4444);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 4px;
}

.rest-discount {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.95);
  color: var(--accent, #F97316);
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 4px;
}

.rest-info {
  padding: 12px 14px;
}

.rest-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, #1F2937);
  margin-bottom: 4px;
}

.rest-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  margin-bottom: 6px;

  .rating {
    color: var(--accent, #F97316);
    font-weight: 700;
  }

  .rating i {
    font-size: 10px;
  }
}

.rest-tags {
  display: flex;
  gap: 4px;

  span {
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 3px;
    background: var(--primary-50, #FFF1F2);
    color: var(--primary, #EF4444);
    font-weight: 500;
  }
}

// ============ 空态 ============
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;

  i {
    font-size: 36px;
    opacity: 0.3;
  }
}

// ============ 响应式 ============
@media (max-width: 1100px) {
  .category-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (max-width: 768px) {
  .home-hero {
    flex-direction: column;
  }
  .hero-right {
    align-items: stretch;
    width: 100%;
  }
  .hero-search {
    min-width: 0;
    width: 100%;
  }
  .category-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
  }
  .cat-icon {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }
  .restaurant-grid {
    grid-template-columns: 1fr;
  }
}
</style>
