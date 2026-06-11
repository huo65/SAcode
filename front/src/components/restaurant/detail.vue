<template>
  <el-dialog
    :model-value="visible"
    :title="restaurantInfo?.name || 'Restaurant Detail'"
    width="900"
    :before-close="closeDialog"
  >
    <div v-if="restaurantInfo" class="restaurant-detail">
      <div class="hero">
        <img
          v-if="restaurantInfo.cover || restaurantInfo.portrait"
          :src="restaurantInfo.cover || restaurantInfo.portrait"
          class="cover"
          alt="restaurant cover"
        />
        <div v-else class="cover cover-placeholder">No Image</div>
        <div class="meta">
          <h2>{{ restaurantInfo.name }}</h2>
          <p class="description">
            {{ restaurantInfo.description || "No restaurant description yet." }}
          </p>
          <p class="address">Address: {{ restaurantInfo.address || "TBD" }}</p>
          <p class="price">From {{ restaurantInfo.minPrice || 0 }}￥</p>
          <div class="tags">
            <el-tag
              v-for="category in restaurantInfo.categories || []"
              :key="category"
              size="small"
            >
              {{ category }}
            </el-tag>
          </div>
        </div>
      </div>

      <div v-if="menuGroups.length" class="menu">
        <div v-for="group in menuGroups" :key="group.name" class="menu-group">
          <h3>{{ group.name }}</h3>
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
              <div v-else class="product-image product-placeholder">No Image</div>
              <div class="product-info">
                <div class="product-header">
                  <span>{{ product.name }}</span>
                  <span>{{ product.price }}￥</span>
                </div>
                <p>{{ product.description || "No product description yet." }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="No menu available yet." />
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
  (props.restaurantInfo?.productList || []).forEach((product) => {
    const key = product.cat_name || "Others";
    if (!groups.has(key)) {
      groups.set(key, []);
    }
    groups.get(key).push(product);
  });
  return Array.from(groups.entries()).map(([name, products]) => ({
    name,
    products,
  }));
});

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
</script>

<style lang="less" scoped>
.restaurant-detail {
  .hero {
    display: flex;
    gap: 20px;
    margin-bottom: 24px;
  }

  .cover {
    width: 260px;
    height: 180px;
    object-fit: cover;
    border-radius: 10px;
  }

  .cover-placeholder,
  .product-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #909399;
  }

  .meta {
    flex: 1;
  }

  .description,
  .address,
  .price {
    color: #666;
    line-height: 1.6;
  }

  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 12px;
  }

  .menu-group {
    margin-bottom: 20px;
  }

  .product-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }

  .product-card {
    display: flex;
    gap: 12px;
    padding: 12px;
    border: 1px solid #ebeef5;
    border-radius: 10px;
    cursor: pointer;
  }

  .product-image {
    width: 96px;
    height: 96px;
    border-radius: 8px;
    object-fit: cover;
  }

  .product-info {
    flex: 1;
  }

  .product-header {
    display: flex;
    justify-content: space-between;
    font-weight: 600;
  }

  p {
    margin: 8px 0 0;
    color: #666;
  }
}
</style>
