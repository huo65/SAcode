<template>
  <div class="goods">
    <section class="search glass-panel">
      <div class="section-heading">
        <div>
          <span class="micro-tag">商品工作台</span>
          <h3>{{ curStatus === "merchant" ? "商品经营面板" : "商品浏览与审核面板" }}</h3>
          <p>按分类、名称、价格区间和状态快速筛选商品，并通过卡片化布局提升列表可读性。</p>
        </div>
        <div class="toolbar-actions">
          <el-button v-if="searchCondition.priceOrder == 0" type="primary" @click="order(1)">
            {{ t('goods.lowToHigh') }}
          </el-button>
          <el-button v-else type="primary" @click="order(0)">
            {{ t('goods.highToLow') }}
          </el-button>
          <el-button type="primary" @click="clickSearchGoods">
            <el-icon><Search /></el-icon>{{ t('goods.search') }}
          </el-button>
          <el-button v-if="curStatus === 'merchant'" @click="openEditModal">
            <el-icon><Plus /></el-icon>{{ t('goods.add') }}
          </el-button>
          <el-button v-if="curStatus === 'customer'" @click="toggleCart">
            <el-icon><ShoppingCart /></el-icon>{{ t('goods.cart') }}
          </el-button>
        </div>
      </div>

      <el-form class="search-form">
        <el-form-item :label="t('goods.goodsCategory')">
          <el-select v-model="searchCondition.category" clearable>
            <el-option
              v-for="category in productCategories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('goods.goodsName')">
          <el-input v-model="searchCondition.name" clearable></el-input>
        </el-form-item>
        <el-form-item :label="t('goods.minPrice')">
          <el-input v-model="searchCondition.min_price" clearable></el-input>
        </el-form-item>
        <el-form-item :label="t('goods.maxPrice')">
          <el-input v-model="searchCondition.max_price" clearable></el-input>
        </el-form-item>
        <el-form-item v-if="curStatus === 'admin'" :label="t('goods.goodsState')">
          <el-select v-model="searchCondition.state" clearable>
            <el-option :label="t('goods.notPassed')" :value="-1"></el-option>
            <el-option :label="t('goods.pendingReview')" :value="0"></el-option>
            <el-option :label="t('goods.passed')" :value="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </section>

    <section class="result-bar">
      <strong>共 {{ goodsList.length }} 个商品结果</strong>
      <span>卡片支持快速查看商品图片、描述与价格，点击后进入详情或编辑流。</span>
    </section>

    <div class="list">
      <div
        v-for="item in goodsList"
        :key="item.id"
        class="good-wrap"
        @click="openDetail(item)"
      >
        <div class="good-item">
          <img :src="item.image_list[0]" class="image" />
          <!-- <img :src="imageUrlMap[item.image_list[0]]" class="image" /> -->
        </div>
        <div class="info">
          <span class="goods-caption">{{ item.category || "精选商品" }}</span>
          <h3>{{ item.name }}</h3>
          <p>{{ item.description }}</p>
        </div>
        <div class="price">
          <div class="price-meta">
            <span class="goods-hint">{{ curStatus === "merchant" ? "点击管理商品" : "点击查看详情" }}</span>
            <strong>{{ item.price }}￥</strong>
          </div>
        </div>
      </div>
    </div>
    <Detail
      :visible="detailVisible"
      :productInfo="curProduct"
      :curStatus="curStatus"
      @close="closeDetail"
      @add="addToCart"
      @openEdit="openEditModal"
    />

    <Edit
      v-if="editVisible"
      :visible="editVisible"
      :product-info="curEditItem"
      @close="closeEditModal"
    />
    <CartDrawer
      :visible="cartVisible"
      @close="closeCart"
      @change="saveCart"
    />
  </div>
</template>

<script setup>
import { goodsCategories, prod_list } from "@/mock";
import { ref, reactive, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { imageUrlMap } from "@/mock";
import { ElMessage } from "element-plus";
import { curStatus } from "@/store";
import Edit from "./edit.vue";
import Detail from "./detail.vue";
import CartDrawer from "./cart.vue";
import $store, { userInfo, productCategories, cartList } from "@/store";
import fetch from "@/api/fetch.js";
import { User, Product, Cart } from "@/api/apis.js";
import { Search, ShoppingCart, Plus } from "@element-plus/icons-vue";

const { t } = useI18n();

/* 查询列表 */
const searchCondition = reactive({
  category: null,
  name: "",
  state: null,
  priceOrder: 0,
});
let goodsList = ref([]);
function order(v) {
  searchCondition.priceOrder = v;
  clickSearchGoods();
}

const clickSearchGoods = () => {
  // TODO: 发送请求
  console.log("搜索条件：", searchCondition);

  fetch(Product.getProductList, {
    id: userInfo.value.id,
    name: searchCondition.name,
    category: searchCondition.category,
    state: searchCondition.state,
    priceOrder: searchCondition.priceOrder,
    min_price: searchCondition.min_price,
    max_price: searchCondition.max_price,
  }).then((data) => {
    console.log(curStatus.value, "##");
    // if(curStatus.value==='customer'||curStatus.value==='guest'){
    //   goodsList.value = data.prod_list.filter(i=>{
    //     return i.state == 1
    //   })
    // }else if(curStatus.value==='merchant'){
    //   goodsList.value = data.prod_list.filter(i=>{
    //     return i.mer == userInfo.value.name || i.state == 1;
    //   })

    // }else{
    //    goodsList.value = data.prod_list
    // }
    goodsList.value = data.prod_list;
  });
};

/* 商品详情 */
const detailVisible = ref(false);
const curProduct = ref(null);
const openDetail = (item) => {
  curProduct.value = item;
  // paymentVisible.value = false;
  detailVisible.value = true;

  console.log("detail", item);
};

const closeDetail = () => {
  detailVisible.value = false;
  clickSearchGoods();
};
/* 商品新增 */
const editVisible = ref(false);
const curEditItem = ref({});
const openEditModal = (item) => {
  curEditItem.value = item;
  detailVisible.value = false;
  editVisible.value = true;
  console.log(curEditItem.value);
};
const closeEditModal = () => {
  editVisible.value = false;
  curEditItem.value = null;
  clickSearchGoods();
};

/* 获取分类信息 */
const getCategories = () => {
  fetch(Product.getCategories).then((data) => {
    console.log("setCategoryList", data);
    const list = data.map((item) => ({
      label: item.name,
      value: item.name,
    }));
    $store.commit("setCategoryList", list);
  });
};

/* 获取当前用户的地址，并添加到个人信息中 */
const getAddrList = () => {
  if (!userInfo.value.id) return;
  fetch(User.getAddress, { id: userInfo.value.id }).then((data) => {
    console.log("获取到的addr_list", data);
    $store.commit("setUserInfo", { ...userInfo.value, addr_list: data }); // 把地址添加到其中
  });
};

/* 购物车 */
const cartVisible = ref(false);

const toggleCart = () => {
  console.log("toggleCart", !cartVisible.value);
  cartVisible.value = !cartVisible.value;
  getCartInfo();
};

const closeCart = () => {
  cartVisible.value = false;
};

const getCartInfo = () => {
  if (!userInfo.value.id) return;
  fetch(Cart.getCart, { id: userInfo.value.id }).then((data) => {
    console.log("获取到的cart_list", data.cartList);

    $store.commit(
      "updateCartList",
      data.cartList.map((item) => ({
        ...item,
        checked:
          cartList.value?.find((_item) => _item.id === item.id)?.checked ??
          false,
      })) || []
    );
  });
};

const saveCart = () => {
  console.log("saveCart");
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

  console.log("addToCart", cartListFormated);
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

/* 钩子 */
onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Goods",
    fn: initGoodsData,
  });
  initGoodsData();
});
</script>

<style lang="less" scoped>
@import "../../style/theme.less";
.search {
  padding: 24px;
}

.search-form {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px 16px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-form :deep(.el-form-item__content) {
  width: 100%;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.toolbar-actions i {
  margin-right: 6px;
}

.result-bar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin: 18px 0 14px;
  padding: 14px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(92, 46, 20, 0.08);
}

.result-bar strong {
  color: var(--text-strong);
}

.result-bar span {
  color: var(--text-soft);
}

.list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;

  .good-wrap {
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding: 16px;
    min-height: 320px;
    border: 1px solid rgba(92, 46, 20, 0.08);
    border-radius: 24px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.86), rgba(255, 249, 244, 0.8));
    box-shadow: var(--shadow-soft);
    cursor: pointer;
    transition:
      transform var(--transition-base),
      box-shadow var(--transition-base),
      border-color var(--transition-base);

    &:hover {
      transform: translateY(-4px);
      border-color: rgba(218, 106, 42, 0.22);
      box-shadow: var(--shadow-card);
    }

    .good-item {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 168px;
      border-radius: 18px;
      overflow: hidden;
      background: rgba(255, 241, 225, 0.7);

      .image {
        width: 100%;
        height: 168px;
        object-fit: cover;
      }
    }

    .info {
      display: flex;
      flex-direction: column;
      flex: 1;
      gap: 8px;
      padding: 0 6px;

      .goods-caption {
        color: var(--brand-600);
        font-size: 12px;
        letter-spacing: 0.12em;
        text-transform: uppercase;
      }

      h3 {
        word-break: break-all;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        text-overflow: ellipsis;
        color: var(--text-strong);
        font-size: 24px;
        font-family: var(--font-display);
      }
      p {
        word-break: break-all;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        word-wrap: break-word;
        color: var(--text-soft);
        line-height: 1.7;
      }
    }

    .price {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      padding-top: 10px;
      border-top: 1px solid rgba(92, 46, 20, 0.08);
    }

    .price-meta {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 4px;
    }

    .goods-hint {
      color: var(--text-soft);
      font-size: 12px;
    }

    strong {
      color: @price;
      font-size: 24px;
      font-family: var(--font-display);
    }
  }
}

@media (max-width: 1200px) {
  .search-form {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .search {
    padding: 18px;
  }

  .search-form,
  .list {
    grid-template-columns: 1fr;
  }

  .result-bar,
  .section-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
</style>
