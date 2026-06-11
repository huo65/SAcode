<template>
  <div class="restaurant">
    <div class="search">
      <el-form :inline="true">
        <el-form-item label="Category">
          <el-select
            v-model="searchCondition.category"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="category in productCategories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Keyword">
          <el-input
            v-model="searchCondition.keyword"
            clearable
            style="width: 240px"
            placeholder="restaurant / dish / category"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRestaurants">Search</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="restaurantList.length" class="list">
      <div
        v-for="restaurant in restaurantList"
        :key="restaurant.id"
        class="card"
        @click="openRestaurant(restaurant.id)"
      >
        <img
          v-if="restaurant.cover || restaurant.portrait"
          :src="restaurant.cover || restaurant.portrait"
          class="cover"
          alt="restaurant cover"
        />
        <div v-else class="cover cover-placeholder">No Image</div>
        <div class="info">
          <div class="header">
            <h3>{{ restaurant.name }}</h3>
            <span>{{ restaurant.minPrice || 0 }}￥起</span>
          </div>
          <p class="description">
            {{ restaurant.description || "No restaurant description yet." }}
          </p>
          <p class="address">{{ restaurant.address || "No address yet." }}</p>
          <div class="tags">
            <el-tag
              v-for="category in restaurant.categories || []"
              :key="category"
              size="small"
            >
              {{ category }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-else description="No restaurants found." />

    <RestaurantDetail
      :visible="detailVisible"
      :restaurantInfo="currentRestaurant"
      :curStatus="curStatus"
      @close="closeRestaurant"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import $store, { curStatus, productCategories, userInfo } from "@/store";
import fetch from "@/api/fetch";
import { Product, Restaurant, User } from "@/api/apis";
import RestaurantDetail from "./restaurant-dialog.vue";

const searchCondition = reactive({
  category: null,
  keyword: "",
});

const restaurantList = ref([]);
const currentRestaurant = ref(null);
const detailVisible = ref(false);

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
  .list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }

  .card {
    display: flex;
    gap: 16px;
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 12px;
    cursor: pointer;
    transition: border-color 0.2s ease;

    &:hover {
      border-color: #e6a23c;
    }
  }

  .cover {
    width: 180px;
    height: 140px;
    border-radius: 10px;
    object-fit: cover;
  }

  .cover-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #909399;
    font-size: 14px;
  }

  .info {
    flex: 1;
  }

  .header {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  .description,
  .address {
    color: #666;
    line-height: 1.6;
  }

  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 10px;
  }
}
</style>
