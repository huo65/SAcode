<template>
  <div class="goods-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">{{ curStatus === 'merchant' ? '商品管理' : '商品治理' }}</h2>
        <p class="page-subtitle">{{ curStatus === 'merchant' ? '管理你的菜品，实时更新库存与价格' : '平台商品审核与管理' }}</p>
      </div>
      <div class="page-header-actions">
        <div class="header-search">
          <i class="fas fa-search"></i>
          <input
            v-model="searchCondition.name"
            type="text"
            placeholder="搜索商品名称..."
            @keyup.enter="clickSearchGoods"
          />
        </div>
        <button class="btn btn-primary" @click="clickSearchGoods">
          <i class="fas fa-search"></i> 搜索
        </button>
        <button v-if="curStatus === 'merchant'" class="btn btn-primary" @click="openEditModal">
          <i class="fas fa-plus"></i> 新增商品
        </button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar card">
      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">分类</label>
          <select v-model="searchCondition.category" class="filter-select">
            <option :value="null">全部分类</option>
            <option
              v-for="cat in productCategories"
              :key="cat.value"
              :value="cat.value"
            >{{ cat.label }}</option>
          </select>
        </div>
        <div class="filter-item">
          <label class="filter-label">最低价</label>
          <input
            v-model="searchCondition.min_price"
            type="number"
            placeholder="¥ 最低"
            class="filter-input"
          />
        </div>
        <div class="filter-item">
          <label class="filter-label">最高价</label>
          <input
            v-model="searchCondition.max_price"
            type="number"
            placeholder="¥ 最高"
            class="filter-input"
          />
        </div>
        <div v-if="curStatus === 'admin'" class="filter-item">
          <label class="filter-label">状态</label>
          <select v-model="searchCondition.state" class="filter-select">
            <option :value="null">全部状态</option>
            <option :value="-1">未通过</option>
            <option :value="0">待审核</option>
            <option :value="1">已通过</option>
          </select>
        </div>
        <div class="filter-item filter-actions">
          <button
            class="btn"
            :class="searchCondition.priceOrder === 0 ? 'btn-primary' : 'btn-outline'"
            @click="order(1)"
          >
            <i class="fas fa-sort-amount-up"></i> 价格升序
          </button>
          <button
            v-if="searchCondition.priceOrder === 1"
            class="btn btn-outline"
            @click="order(0)"
          >
            <i class="fas fa-sort-amount-down"></i> 价格降序
          </button>
        </div>
      </div>
    </div>

    <!-- 结果统计 -->
    <div class="result-bar">
      <strong>共 {{ goodsList.length }} 个商品</strong>
      <span>点击商品卡片查看详情或管理</span>
    </div>

    <!-- 商品网格 -->
    <div v-if="goodsList.length > 0" class="goods-grid">
      <div
        v-for="item in goodsList"
        :key="item.id"
        class="goods-card"
        @click="openDetail(item)"
      >
        <div class="goods-card-img">
          <img
            v-if="resolveImageUrl(item.image_list?.[0])"
            :src="resolveImageUrl(item.image_list?.[0])"
            :alt="item.name"
          />
          <div v-else class="goods-card-img-placeholder">
            <i class="fas fa-utensils"></i>
          </div>
          <span
            class="goods-card-badge"
            :class="{
              'badge-success': item.state === 1,
              'badge-warning': item.state === 0,
              'badge-danger': item.state === -1,
            }"
          >
            {{ item.state === 1 ? '已上架' : item.state === 0 ? '待审核' : '已下架' }}
          </span>
        </div>
        <div class="goods-card-body">
          <div class="goods-card-category">{{ item.category || '未分类' }}</div>
          <h3 class="goods-card-name">{{ item.name }}</h3>
          <p class="goods-card-desc">{{ item.description || '暂无描述' }}</p>
          <div class="goods-card-footer">
            <span class="goods-card-price">¥ {{ item.price }}</span>
            <span class="goods-card-stock">库存 {{ item.stock || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon"><i class="fas fa-box-open"></i></div>
      <p class="empty-text">暂无商品数据</p>
      <p class="empty-hint">尝试调整筛选条件，或新增商品</p>
    </div>

    <!-- 商品详情弹窗 -->
    <Detail
      :visible="detailVisible"
      :productInfo="curProduct"
      :curStatus="curStatus"
      @close="closeDetail"
      @add="addToCart"
      @openEdit="openEditModal"
    />

    <!-- 商品编辑弹窗 -->
    <Edit
      v-if="editVisible"
      :visible="editVisible"
      :product-info="curEditItem"
      @close="closeEditModal"
    />

    <!-- 购物车抽屉 -->
    <CartDrawer
      :visible="cartVisible"
      @close="closeCart"
      @change="saveCart"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import { curStatus } from "@/store";
import Edit from "./edit.vue";
import Detail from "./detail.vue";
import CartDrawer from "./cart.vue";
import $store, { userInfo, productCategories, cartList } from "@/store";
import fetch from "@/api/fetch";
import { User, Product, Cart } from "@/api/apis.js";
import { resolveImageUrl } from "@/lib/imageHelper";

const { t } = useI18n();

/* 查询条件 */
const searchCondition = reactive({
  category: null,
  name: "",
  state: null,
  priceOrder: 0,
  min_price: null,
  max_price: null,
});
const goodsList = ref([]);

const order = (v) => {
  searchCondition.priceOrder = v;
  clickSearchGoods();
};

const clickSearchGoods = () => {
  fetch(Product.getProductList, {
    id: userInfo.value.id,
    name: searchCondition.name || null,
    category: searchCondition.category || null,
    state: searchCondition.state,
    priceOrder: searchCondition.priceOrder,
    min_price: searchCondition.min_price,
    max_price: searchCondition.max_price,
  }).then((data) => {
    goodsList.value = data.prod_list || [];
  });
};

/* 商品详情 */
const detailVisible = ref(false);
const curProduct = ref(null);
const openDetail = (item) => {
  curProduct.value = item;
  detailVisible.value = true;
};
const closeDetail = () => {
  detailVisible.value = false;
  clickSearchGoods();
};

/* 商品编辑 */
const editVisible = ref(false);
const curEditItem = ref({});
const openEditModal = (item) => {
  curEditItem.value = item || {};
  detailVisible.value = false;
  editVisible.value = true;
};
const closeEditModal = () => {
  editVisible.value = false;
  curEditItem.value = null;
  clickSearchGoods();
};

/* 分类加载 */
const getCategories = () => {
  fetch(Product.getCategories).then((data) => {
    const list = data.map((item) => ({
      label: item.name,
      value: item.name,
    }));
    $store.commit("setCategoryList", list);
  });
};

/* 地址加载 */
const getAddrList = () => {
  if (!userInfo.value.id) return;
  fetch(User.getAddress, { id: userInfo.value.id }).then((data) => {
    $store.commit("setUserInfo", { ...userInfo.value, addr_list: data });
  });
};

/* 购物车 */
const cartVisible = ref(false);
const toggleCart = () => {
  cartVisible.value = !cartVisible.value;
  getCartInfo();
};
const closeCart = () => {
  cartVisible.value = false;
};
const getCartInfo = () => {
  if (!userInfo.value.id) return;
  fetch(Cart.getCart, { id: userInfo.value.id }).then((data) => {
    $store.commit(
      "updateCartList",
      (data.cartList || []).map((item) => ({
        ...item,
        checked: cartList.value?.find((_) => _.id === item.id)?.checked ?? false,
      })) || []
    );
  });
};
const saveCart = () => {
  const cartListFormated = cartList.value.map((item) => ({
    cus: userInfo.value.id,
    prod: item.id,
    number: item.numberInCart,
  }));
  fetch(Cart.updateCart, {
    cus: userInfo.value.id,
    cartList: cartListFormated,
  }).then(() => {
    getCartInfo();
  });
};
const addToCart = (productId, number) => {
  const cartListFormated = cartList.value.map((item) => ({
    cus: userInfo.value.id,
    prod: item.id,
    number: item.numberInCart,
  }));
  const exsitedItem = cartListFormated.find((item) => item.id === productId);
  if (exsitedItem) {
    exsitedItem.numberInCart += number;
  } else {
    cartListFormated.push({
      cus: userInfo.value.id,
      prod: productId,
      number,
    });
  }
  fetch(Cart.updateCart, {
    cus: userInfo.value.id,
    cartList: cartListFormated,
  }).then(() => {
    getCartInfo();
    ElMessage.success(t('goods.addToCartSuccess'));
  });
};

const initGoodsData = () => {
  getCategories();
  getAddrList();
  clickSearchGoods();
  getCartInfo();
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Goods",
    fn: initGoodsData,
  });
  initGoodsData();
});
</script>

<style lang="less" scoped>
@import "@/style/theme.less";

.goods-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 页面标题区 ===== */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}
.page-header-left {
  flex: 1;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}
.page-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}
.page-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.header-search {
  position: relative;
  i {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-muted);
    font-size: 13px;
  }
  input {
    width: 220px;
    height: 38px;
    border: 1px solid var(--border);
    border-radius: 20px;
    padding: 0 14px 0 36px;
    font-size: 13px;
    background: var(--bg);
    color: var(--text-primary);
    outline: none;
    transition: all 0.2s ease;
    &:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.1);
    }
  }
}
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  outline: none;
  white-space: nowrap;
}
.btn-primary {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
  &:hover {
    background: var(--primary-hover);
    box-shadow: 0 4px 12px rgba(232, 101, 43, 0.3);
  }
}
.btn-outline {
  background: transparent;
  color: var(--text-secondary);
  border-color: var(--border);
  &:hover {
    border-color: var(--primary);
    color: var(--primary);
  }
}

/* ===== 筛选栏 ===== */
.filter-bar {
  background: var(--card);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 18px 22px;
}
.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
}
.filter-select,
.filter-input {
  height: 38px;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 13px;
  background: var(--bg);
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s ease;
  min-width: 140px;
  &:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.08);
  }
}
.filter-input {
  width: 120px;
}
.filter-actions {
  margin-left: auto;
  flex-direction: row !important;
  align-items: center !important;
  gap: 8px !important;
  padding-bottom: 2px;
}

/* ===== 结果统计 ===== */
.result-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(92, 46, 20, 0.08);
  font-size: 13px;
  strong {
    color: var(--text-primary);
  }
  span {
    color: var(--text-muted);
  }
}

/* ===== 商品网格 ===== */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}
.goods-card {
  background: var(--card);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
    border-color: var(--primary-light);
  }
}
.goods-card-img {
  position: relative;
  height: 160px;
  background: linear-gradient(135deg, #ffecd2, #fcb69f);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
.goods-card-img-placeholder {
  font-size: 40px;
  color: var(--primary);
  opacity: 0.5;
}
.goods-card-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}
.badge-success {
  background: var(--success-light);
  color: #065f46;
}
.badge-warning {
  background: var(--warning-light);
  color: #92400e;
}
.badge-danger {
  background: var(--danger-light);
  color: #991b1b;
}
.goods-card-body {
  padding: 14px 16px;
}
.goods-card-category {
  font-size: 11px;
  color: var(--primary);
  font-weight: 600;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  margin-bottom: 4px;
}
.goods-card-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.goods-card-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}
.goods-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.goods-card-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--price, var(--primary));
}
.goods-card-stock {
  font-size: 12px;
  color: var(--text-muted);
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-muted);
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.4;
  }
  .empty-text {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-secondary);
    margin: 0 0 4px;
  }
  .empty-hint {
    font-size: 13px;
    margin: 0;
  }
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .goods-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }
  .page-header-actions {
    width: 100%;
    .header-search input {
      width: 100%;
    }
  }
  .filter-row {
    flex-direction: column;
  }
  .filter-item {
    width: 100%;
  }
  .filter-select,
  .filter-input {
    width: 100%;
  }
  .filter-actions {
    margin-left: 0;
  }
  .goods-grid {
    grid-template-columns: 1fr;
  }
}
</style>
