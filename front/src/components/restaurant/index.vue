<template>
  <div class="restaurant">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">City Picks</p>
        <h2>课堂展示版门店广场</h2>
        <p class="hero-desc">
          以门店为中心展示品牌信息、营业状态、配送规则和菜单风格，让课堂演示时顾客侧更像真实外卖平台。
        </p>
        <div class="hero-meta">
          <span>共 {{ restaurantList.length }} 家门店</span>
          <span>默认展示营业中优先与高评分门店</span>
          <span v-if="addressSummary">配送地址：{{ addressSummary }}</span>
        </div>
      </div>
      <div class="hero-spotlight">
        <div class="spotlight-label">精选说明</div>
        <div class="spotlight-title">
          {{ featuredRestaurant?.name || "门店展示已就绪" }}
        </div>
        <div class="spotlight-copy">
          {{
            featuredRestaurant?.promoText ||
            "支持距离 / 评分 / 起送价 / 营业中筛选，适合课堂展示完整点餐链路。"
          }}
        </div>
        <div class="spotlight-stats">
          <span>评分 {{ featuredRestaurant?.averageScore || "4.8" }}</span>
          <span>距离 {{ formatDistance(featuredRestaurant?.distanceKm) }}</span>
        </div>
      </div>
    </section>

    <section class="filter-panel">
      <div class="filter-grid">
        <el-select v-model="searchCondition.category" clearable placeholder="菜品分类">
          <el-option
            v-for="category in productCategories"
            :key="category.value"
            :label="category.label"
            :value="category.value"
          />
        </el-select>
        <el-input
          v-model="searchCondition.keyword"
          clearable
          placeholder="搜索门店 / 菜品 / 公告 / 分类"
          @keyup.enter="loadRestaurants"
        />
        <el-select v-model="searchCondition.sortBy" placeholder="排序方式">
          <el-option label="综合推荐" value="default" />
          <el-option label="距离最近" value="distance" />
          <el-option label="评分最高" value="score" />
          <el-option label="起送价最低" value="price" />
          <el-option label="评价最多" value="reviews" />
        </el-select>
        <el-select v-model="searchCondition.minScore" clearable placeholder="最低评分">
          <el-option :value="4.8" label="4.8 分以上" />
          <el-option :value="4.5" label="4.5 分以上" />
          <el-option :value="4.0" label="4.0 分以上" />
        </el-select>
        <el-switch
          v-model="searchCondition.onlyOpen"
          active-text="只看营业中"
          inactive-text="全部门店"
        />
        <div class="filter-actions">
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="loadRestaurants">刷新门店</el-button>
        </div>
      </div>
    </section>

    <section class="result-bar">
      <span>已筛选出 {{ restaurantList.length }} 家适合当前课堂展示的门店</span>
      <span class="muted">点击卡片可进入门店详情、菜单和评价展示页</span>
    </section>

    <div v-if="restaurantList.length" class="list">
      <article
        v-for="restaurant in restaurantList"
        :key="restaurant.id"
        class="card"
        @click="openRestaurant(restaurant.id)"
      >
        <div class="card-cover-wrap">
          <img
            v-if="restaurant.cover || restaurant.portrait"
            :src="restaurant.cover || restaurant.portrait"
            class="cover"
            alt="restaurant cover"
          />
          <div v-else class="cover cover-placeholder">Store Cover</div>
          <div class="cover-overlay">
            <el-tag :type="restaurant.status === 1 ? 'success' : 'info'">
              {{ restaurant.statusText || (restaurant.status === 1 ? "营业中" : "休息中") }}
            </el-tag>
            <span>{{ formatDistance(restaurant.distanceKm) }}</span>
          </div>
        </div>

        <div class="info">
          <div class="header">
            <div>
              <h3>{{ restaurant.name }}</h3>
              <p class="summary-line">
                {{ restaurant.deliveryEtaMinutes || 30 }} 分钟送达 · 配送费 ￥{{ restaurant.deliveryFee || 0 }}
              </p>
            </div>
            <div class="price-box">
              <strong>￥{{ restaurant.minOrderAmount || restaurant.minPrice || 0 }}</strong>
              <span>起送</span>
            </div>
          </div>

          <p class="description">
            {{ restaurant.description || "门店简介暂未完善。" }}
          </p>
          <p class="address">{{ restaurant.address || "暂无门店地址" }}</p>
          <p class="notice" v-if="restaurant.notice">{{ restaurant.notice }}</p>

          <div class="metric-row">
            <div class="metric-pill">
              <strong>{{ restaurant.averageScore || 0 }}</strong>
              <span>评分</span>
            </div>
            <div class="metric-pill">
              <strong>{{ restaurant.reviewCount || 0 }}</strong>
              <span>评价</span>
            </div>
            <div class="metric-pill">
              <strong>{{ restaurant.menuCount || 0 }}</strong>
              <span>菜单</span>
            </div>
          </div>

          <div class="tags">
            <el-tag
              v-for="tag in restaurant.serviceTags || []"
              :key="`service-${restaurant.id}-${tag}`"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="tags subtle">
            <el-tag
              v-for="category in restaurant.menuCategories || restaurant.categories || []"
              :key="`menu-${restaurant.id}-${category}`"
              type="warning"
              effect="light"
            >
              {{ category }}
            </el-tag>
          </div>
        </div>
      </article>
    </div>
    <el-empty v-else description="当前筛选条件下暂无门店，建议放宽筛选条件后重新查看。" />

    <RestaurantDetail
      :visible="detailVisible"
      :restaurantInfo="currentRestaurant"
      :curStatus="curStatus"
      @close="closeRestaurant"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import $store, { curStatus, productCategories, userInfo } from "@/store";
import fetch from "@/api/fetch";
import { Product, Restaurant, User } from "@/api/apis";
import RestaurantDetail from "./restaurant-dialog.vue";

const searchCondition = reactive({
  category: null,
  keyword: "",
  sortBy: "default",
  onlyOpen: true,
  minScore: null,
});

const restaurantList = ref([]);
const currentRestaurant = ref(null);
const detailVisible = ref(false);

const featuredRestaurant = computed(() => restaurantList.value[0] || null);
const addressSummary = computed(() => userInfo.value?.addr_list?.[0]?.location || "");

const formatDistance = (value) => {
  if (value == null) return "距离待计算";
  return `${Number(value).toFixed(1)} km`;
};

const getCategories = () => {
  fetch(Product.getCategories).then((data) => {
    const list = data.map((item) => ({
      label: item.name,
      value: item.name,
    }));
    $store.commit("setCategoryList", list);
  });
};

const getAddrList = () => {
  if (!userInfo.value.id || curStatus.value !== "customer") return;
  fetch(User.getAddress, { id: userInfo.value.id }).then((data) => {
    $store.commit("setUserInfo", { ...userInfo.value, addr_list: data });
  });
};

const loadRestaurants = () => {
  fetch(Restaurant.list, {
    keyword: searchCondition.keyword,
    category: searchCondition.category,
    sortBy: searchCondition.sortBy,
    onlyOpen: searchCondition.onlyOpen,
    minScore: searchCondition.minScore,
  }).then((data) => {
    restaurantList.value = data.restaurant_list || [];
  });
};

const openRestaurant = (id) => {
  fetch(Restaurant.info, { id }).then((data) => {
    currentRestaurant.value = data.restaurant_info;
    detailVisible.value = true;
  });
};

const closeRestaurant = () => {
  detailVisible.value = false;
};

const resetFilters = () => {
  searchCondition.category = null;
  searchCondition.keyword = "";
  searchCondition.sortBy = "default";
  searchCondition.onlyOpen = true;
  searchCondition.minScore = null;
  loadRestaurants();
};

const initRestaurantData = () => {
  getCategories();
  getAddrList();
  loadRestaurants();
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Restaurant",
    fn: initRestaurantData,
  });
  initRestaurantData();
});
</script>

<style lang="less" scoped>
.restaurant {
  --ink: #241712;
  --gold: #b76e2b;
  --paper: #f8efe3;
  --panel: rgba(255, 250, 244, 0.86);
  color: var(--ink);
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(300px, 0.8fr);
  gap: 18px;
  padding: 30px;
  border-radius: 30px;
  background:
    radial-gradient(circle at top left, rgba(183, 110, 43, 0.22), transparent 36%),
    linear-gradient(135deg, #fff8ef 0%, #f3e4cf 100%);
  border: 1px solid rgba(36, 23, 18, 0.08);
  box-shadow: 0 30px 70px rgba(36, 23, 18, 0.1);
}

.eyebrow {
  margin: 0 0 10px;
  color: var(--gold);
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.hero h2 {
  margin: 0;
  font-size: 42px;
  line-height: 1.1;
  font-family: "Georgia", "Times New Roman", serif;
}

.hero-desc {
  max-width: 760px;
  margin: 12px 0 0;
  color: rgba(36, 23, 18, 0.7);
  line-height: 1.8;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.hero-meta span,
.spotlight-stats span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(36, 23, 18, 0.08);
  font-size: 13px;
}

.hero-spotlight {
  padding: 22px;
  border-radius: 24px;
  background: rgba(35, 23, 15, 0.92);
  color: #fff7ee;
}

.spotlight-label {
  color: rgba(255, 247, 238, 0.68);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.spotlight-title {
  margin-top: 12px;
  font-size: 30px;
  line-height: 1.15;
  font-family: "Georgia", "Times New Roman", serif;
}

.spotlight-copy {
  margin-top: 12px;
  color: rgba(255, 247, 238, 0.78);
  line-height: 1.8;
}

.spotlight-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.filter-panel {
  margin-top: 20px;
  padding: 20px;
  border-radius: 24px;
  background: var(--panel);
  border: 1px solid rgba(36, 23, 18, 0.08);
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  align-items: center;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.result-bar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin: 18px 2px 14px;
  color: rgba(36, 23, 18, 0.72);
}

.result-bar .muted {
  color: rgba(36, 23, 18, 0.52);
}

.list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.card {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
  border-radius: 26px;
  background: rgba(255, 252, 246, 0.92);
  border: 1px solid rgba(36, 23, 18, 0.08);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
  box-shadow: 0 18px 40px rgba(36, 23, 18, 0.06);
}

.card:hover {
  transform: translateY(-4px);
  border-color: rgba(183, 110, 43, 0.3);
  box-shadow: 0 26px 50px rgba(36, 23, 18, 0.12);
}

.card-cover-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 22px;
}

.cover {
  width: 100%;
  height: 100%;
  min-height: 230px;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 230px;
  background: linear-gradient(135deg, #2e1d14 0%, #6b3a1f 100%);
  color: rgba(255, 247, 238, 0.8);
  font-family: "Georgia", "Times New Roman", serif;
}

.cover-overlay {
  position: absolute;
  inset: auto 12px 12px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(19, 15, 13, 0.55);
  backdrop-filter: blur(8px);
  color: #fff8ef;
  font-size: 13px;
}

.info {
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.header h3 {
  margin: 0;
  font-size: 30px;
  font-family: "Georgia", "Times New Roman", serif;
}

.summary-line {
  margin: 8px 0 0;
  color: rgba(36, 23, 18, 0.62);
}

.price-box {
  min-width: 96px;
  text-align: right;
}

.price-box strong {
  display: block;
  font-size: 28px;
  color: var(--gold);
  font-family: "Georgia", "Times New Roman", serif;
}

.price-box span {
  color: rgba(36, 23, 18, 0.56);
  font-size: 13px;
}

.description,
.address,
.notice {
  margin: 10px 0 0;
  color: rgba(36, 23, 18, 0.72);
  line-height: 1.75;
}

.notice {
  padding-left: 12px;
  border-left: 3px solid rgba(183, 110, 43, 0.4);
}

.metric-row {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}

.metric-pill {
  padding: 10px 14px;
  border-radius: 18px;
  background: rgba(244, 232, 218, 0.72);
}

.metric-pill strong {
  display: block;
  font-size: 20px;
  font-family: "Georgia", "Times New Roman", serif;
}

.metric-pill span {
  color: rgba(36, 23, 18, 0.58);
  font-size: 12px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.tags.subtle {
  margin-top: 10px;
}

@media (max-width: 1200px) {
  .hero,
  .list {
    grid-template-columns: 1fr;
  }

  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .card {
    grid-template-columns: 1fr;
  }
}
</style>
